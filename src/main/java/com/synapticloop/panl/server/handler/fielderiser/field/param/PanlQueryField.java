package com.synapticloop.panl.server.handler.fielderiser.field.param;

/*
 * Copyright (c) 2008-2025 synapticloop.
 *
 * https://github.com/synapticloop/panl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.QueryLpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PanlQueryField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlQueryField.class);

	public PanlQueryField(String lpseCode, String propertyKey, Properties properties, String solrCollection, String panlCollectionUri) throws PanlServerException {
		super(lpseCode, properties, propertyKey, solrCollection, panlCollectionUri);
	}

	/**
	 * <p>We only apply the first query</p>
	 *
	 * @param solrQuery The Solr Query to apply to
	 * @param lpseTokenList The list of tokens
	 */
	@Override public void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokenList, CollectionProperties collectionProperties) {
		applyOperandToQuery(solrQuery, lpseTokenList, collectionProperties.getSolrDefaultQueryOperand(), collectionProperties);
	}

	@Override protected void applyToQueryInternalOperand(
			SolrQuery solrQuery,
			List<LpseToken> lpseTokenList,
			List<LpseToken> queryLpseTokenList,
			CollectionProperties collectionProperties) {

		if(!queryLpseTokenList.isEmpty()) {
			String value = queryLpseTokenList.get(0).getValue().equals("+") ? "AND" : "OR";
			applyOperandToQuery(solrQuery, lpseTokenList, value, collectionProperties);
		} else {
			applyOperandToQuery(solrQuery, lpseTokenList, collectionProperties.getSolrDefaultQueryOperand(), collectionProperties);
		}
	}

	private void applyOperandToQuery(
			SolrQuery solrQuery,
			List<LpseToken> lpseTokenList,
			String queryOperand,
			CollectionProperties collectionProperties) {

		if(!lpseTokenList.isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder();
			QueryLpseToken queryLpseToken = (QueryLpseToken)lpseTokenList.get(0);
			List<String> searchableLpseFields = queryLpseToken.getSearchableLpseFields();
			if(searchableLpseFields.isEmpty()) {
				// just do the default search as per usual - need to split on the
				// default query operand
				boolean isFirst = true;
				StringBuilder querySb = new StringBuilder();
				for (String queryValue : queryLpseToken.getValue().split(" ")) {
					if(!isFirst) {
						querySb.append(" ");
					}

					if(!queryValue.trim().isEmpty()) {
						querySb.append("\"" + queryValue + "\"");
						isFirst = false;
					}
				}

				solrQuery.setQuery(querySb.toString());
			} else {
				boolean isFirst = true;
				// There have been passed through queries for specific search fields,
				// so we add them to the query, using the default query operator
				for (String searchableLpseField : searchableLpseFields) {
					if(!isFirst) {
						stringBuilder.append(" ")
								.append(queryOperand)
								.append(" ");
					}
					isFirst = false;
					boolean isFirstValue = true;
					for (String queryLpseValue : queryLpseToken.getValue().split(" ")) {
						if(!isFirstValue) {
							stringBuilder.append(" " + queryOperand + " ");
						}
						if(!queryLpseValue.trim().isEmpty()) {
							stringBuilder
									.append(searchableLpseField)
									.append(":\"")
									.append(queryLpseValue)
									.append("\"")
									.append(collectionProperties.getSpecificSearchBoost(searchableLpseField));
							isFirstValue = false;
						}

					}
				}
				solrQuery.setQuery(stringBuilder.toString());
			}
		}
	}

	@Override public Logger getLogger() {
		return(LOGGER);
	}

	@Override public List<String> explainAdditional() {
		List<String> explanations = new ArrayList<>();
		explanations.add("The search text or phrase query which maps to the 'q' parameter of Solr.");
		return(explanations);
	}

	@Override public void appendToAvailableObjectInternal(JSONObject jsonObject) {

	}

	@Override public List<LpseToken> instantiateTokens(CollectionProperties collectionProperties, String lpseCode, String query, StringTokenizer valueTokeniser, LpseTokeniser lpseTokeniser) {
		return(List.of(new QueryLpseToken(collectionProperties, this.lpseCode, query, valueTokeniser, lpseTokeniser)));
	}


	@Override public String getCanonicalLpseCode(
			Map<String, List<LpseToken>> panlTokenMap,
			CollectionProperties collectionProperties) {

		StringBuilder stringBuilder = new StringBuilder();

		List<LpseToken> lpseTokens = panlTokenMap.get(collectionProperties.getPanlParamQuery());
		if (null != lpseTokens && !lpseTokens.isEmpty()) {
			QueryLpseToken queryLpseToken = (QueryLpseToken) lpseTokens.get(0);
			stringBuilder.append(queryLpseToken.getLpseCode());

			List<String> searchableLpseFields = queryLpseToken.getSearchableLpseFields();
			if(!searchableLpseFields.isEmpty()) {
				stringBuilder.append("(");
				for(String searchableLpseField : searchableLpseFields) {
					stringBuilder.append(collectionProperties.getSearchFieldsMap().get(searchableLpseField));
				}
				stringBuilder.append(")");
			}
		}

		return(stringBuilder.toString());
	}

	@Override public String getLpseCode(Map<String, List<LpseToken>> panlTokenMap,
			CollectionProperties collectionProperties) {
		return(getCanonicalLpseCode(panlTokenMap, collectionProperties));
	}

	@Override public String getLpseCode(LpseToken token, CollectionProperties collectionProperties) {
		StringBuilder stringBuilder = new StringBuilder();
		QueryLpseToken queryLpseToken = (QueryLpseToken) token;
		stringBuilder.append(queryLpseToken.getLpseCode());

		List<String> searchableLpseFields = queryLpseToken.getSearchableLpseFields();
		if(!searchableLpseFields.isEmpty()) {
			stringBuilder.append("(");
			for(String searchableLpseField : searchableLpseFields) {
				stringBuilder.append(collectionProperties.getSearchFieldsMap().get(searchableLpseField));
			}
			stringBuilder.append(")");
		}

		return(stringBuilder.toString());
	}

	@Override protected void logDetails() {
		getLogger().info("[ Solr/Panl: '{}/{}' ] Query parameter mapped to '{}'.",
				solrCollection,
				panlCollectionUri,
				lpseCode);
	}

	@Override public List<String> explain() {
		List<String> temp = new ArrayList<>();
		temp.add("PARAM CONFIG [ " +
				this.getClass().getSimpleName() +
				" ] LPSE code '" +
				lpseCode +
				"'.");

		temp.addAll(explainAdditional());
		temp.addAll(WARNING_MESSAGES);
		return(temp);
	}
}
