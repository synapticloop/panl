package com.synapticloop.panl.server.handler.fielderiser.field.param;

/*
 * Copyright (c) 2008-2024 synapticloop.
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
 *  IN THE SOFTWARE.
 */

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.QueryOperandLpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PanlQueryOperandField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlQueryOperandField.class);
	public static final String SOLR_PARAM_Q_OP = "q.op";

	public PanlQueryOperandField(String lpseCode, String propertyKey, Properties properties, String solrCollection) throws PanlServerException {
		super(lpseCode, properties, propertyKey, solrCollection);
	}

	@Override
	public String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return ("");
	}

	/**
	 * <p>Get the canonical URI path, this will only return something if the
	 * query operand is different from the default AND/OR operand as set in the
	 * collection.panl.properties file.</p>
	 *
	 * @param panlTokenMap The panl token map to query
	 * @param collectionProperties The collection properties for this CaFUPs
	 *
	 * @return The URI part for this token
	 */
	@Override
	public String getCanonicalLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder(lpseCode);
		if (panlTokenMap.containsKey(lpseCode)) {
			QueryOperandLpseToken queryOperandLpseToken = (QueryOperandLpseToken) panlTokenMap.get(lpseCode).get(0);

			if (queryOperandLpseToken.getIsValid()) {
				if (queryOperandLpseToken.getLpseQueryOperand().equals(collectionProperties.getDefaultQueryOperand())) {
					return ("");
				}

				// at this point, the user has:
				//   1. passed through a query operand in the LPSE Code
				//   2. It is a valid value
				//   3. It is not the same as the default
				sb.append(queryOperandLpseToken.getLpseQueryOperand());
			} else {
				// not a valid token - return nothing
				return ("");
			}
		} else {
			// they haven't changed the query operand - return nothing
			return ("");
		}

		return (sb.toString());
	}

	@Override public Logger getLogger() {
		return (LOGGER);
	}

	@Override public List<String> explainAdditional() {
		List<String> explanations = new ArrayList<>();
		explanations.add("The query operand which maps to the 'q.op' parameter of Solr");
		return(explanations);
	}

	public void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokenList) {
		if (!lpseTokenList.isEmpty()) {
			solrQuery.setParam(SOLR_PARAM_Q_OP, ((QueryOperandLpseToken) lpseTokenList.get(0)).getQOpValue());
		}
	}

	@Override public String getLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return (getCanonicalLpseCode(panlTokenMap, collectionProperties));
	}

	public String getLpseCode(LpseToken token, CollectionProperties collectionProperties) {
		QueryOperandLpseToken queryOperandLpseToken = (QueryOperandLpseToken) token;

		String lpseQueryOperand = queryOperandLpseToken.getLpseQueryOperand();
		if (lpseQueryOperand.equals(collectionProperties.getDefaultQueryOperand())) {
			return ("");
		} else {
			return (queryOperandLpseToken.getLpseCode() + lpseQueryOperand);
		}
	}

	public String getURIPath(LpseToken token, CollectionProperties collectionProperties) {
		return ("");
	}

	@Override
	public String getURIPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return ("");
	}

	@Override public void appendToAvailableObjectInternal(JSONObject jsonObject) {

	}
	@Override public LpseToken instantiateToken(CollectionProperties collectionProperties, String lpseCode, String query, StringTokenizer valueTokeniser, LpseTokeniser lpseTokeniser) {
		return(new QueryOperandLpseToken(collectionProperties, this.lpseCode, lpseTokeniser));
	}

	@Override protected void logDetails() {
		getLogger().info("[ Solr collection: '{}' ] Query operand parameter mapped to '{}'.",
				solrCollection,
				lpseCode);
	}

}
