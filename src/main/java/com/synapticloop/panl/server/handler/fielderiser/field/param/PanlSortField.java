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
import com.synapticloop.panl.server.handler.tokeniser.token.param.SortLpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>The sort field controls the order in which the results are sorted.  This
 * field does not have a URI path value, only a URI LPSE code.</p>
 *
 * @author synapticloop
 */
public class PanlSortField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlSortField.class);

	public PanlSortField(String lpseCode, String propertyKey, Properties properties, String solrCollection) throws PanlServerException {
		super(lpseCode, properties, propertyKey, solrCollection);

		populatePanlAndSolrFieldNames();
		logDetails();
	}

	@Override public String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return("");
	}

	@Override public String getCanonicalLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder();

		if(panlTokenMap.containsKey(lpseCode)) {
			for (LpseToken lpseToken : panlTokenMap.getOrDefault(lpseCode, new ArrayList<>())) {
				SortLpseToken sortLpseToken = (SortLpseToken) lpseToken;
				if (lpseToken.getIsValid()) {
					sb.append(lpseCode);
					sb.append(sortLpseToken.getLpseSortCode());
					sb.append(sortLpseToken.getSortOrderUriKey());
				} else {
					return("");
				}
			}
		}
		return(sb.toString());
	}

	@Override public String getURIPath(LpseToken token, CollectionProperties collectionProperties) {
		return("");
	}

	@Override public String getLpseCode(LpseToken token, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder(token.getLpseCode());
		SortLpseToken sortLpseToken = (SortLpseToken) token;
		if(sortLpseToken.getIsValid()) {
			sb.append(sortLpseToken.getLpseSortCode());
			sb.append(sortLpseToken.getSortOrderUriKey());
		} else {
			sb.append("-");
		}
		return(sb.toString());
	}

	@Override
	public String getURIPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return("");
	}

	@Override
	public String getLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		if(panlTokenMap.containsKey(collectionProperties.getPanlParamSort())) {
			return(getCanonicalLpseCode(panlTokenMap, collectionProperties));
		}
		return("");
	}

	@Override public Logger getLogger() {
		return(LOGGER);
	}

	@Override public List<String> explain() {
		List<String> temp = new ArrayList<>();
		temp.add("FIELD CONFIG [ " +
				this.getClass().getSimpleName() +
				" ] LPSE code '" +
				lpseCode +
				"'.");

		return(temp);
	}

	@Override public List<String> explainAdditional() {
		List<String> explanations = new ArrayList<>();
		explanations.add("The sort order (default is relevance descending), but can be any PanlField, or PanlFacet.");
		return(explanations);
	}

	public void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokenList) {
		List<SolrQuery.SortClause> sortClauses = new ArrayList<>();
		for (LpseToken lpseToken : lpseTokenList) {
			SortLpseToken sortLpseToken = (SortLpseToken)lpseToken;
			if(sortLpseToken.getIsValid()) {
				sortClauses.add(SolrQuery.SortClause.create(sortLpseToken.getSolrFacetField(), sortLpseToken.getSortOrder()));
			}
		}

		solrQuery.setSorts(sortClauses);
	}

	@Override public void appendAvailableObjectInternal(JSONObject jsonObject) {

	}

	@Override public LpseToken instantiateToken(CollectionProperties collectionProperties, String lpseCode, String query, StringTokenizer valueTokeniser, LpseTokeniser lpseTokeniser) {
		return(new SortLpseToken(collectionProperties, this.lpseCode, lpseTokeniser));
	}
}
