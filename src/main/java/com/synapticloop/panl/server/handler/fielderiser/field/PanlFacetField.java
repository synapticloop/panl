package com.synapticloop.panl.server.handler.fielderiser.field;

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
import com.synapticloop.panl.server.handler.tokeniser.token.FacetLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PanlFacetField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlFacetField.class);

	public PanlFacetField(String lpseCode, String propertyKey, Properties properties, String collectionName, int lpseLength) throws PanlServerException {
		super(lpseCode, properties, propertyKey, collectionName, lpseLength);

		populateSuffixAndPrefix();
		populateBooleanReplacements();
		populateSolrFieldType();
		populatePanlAndSolrFieldNames();

		populateRanges();
		// lastly, we are going to check to see whether this is an 'OR' field
		populateFacetOr();


		logDetails();
	}

	@Override
	public Logger getLogger() {
		return (LOGGER);
	}

	@Override public String getExplainDescription() {
		return ("A Solr field that can be used as a facet, returned in the field set, or configured to be sorted by.");
	}

	private void applyRangeFacetToQuery(SolrQuery solrQuery, List<LpseToken> lpseTokens) {
		for (LpseToken lpseToken : lpseTokens) {
			FacetLpseToken facetLpseToken = (FacetLpseToken) lpseToken;

			// even though this field is set to be a range facet, we still allow
			// single values

			if(((FacetLpseToken) lpseToken).getIsRangeToken()) {
				solrQuery.addFilterQuery(
						String.format("%s:[%s TO %s]",
								facetLpseToken.getSolrField(),
								facetLpseToken.getValue(),
								facetLpseToken.getToValue()));
			} else {
				solrQuery.addFilterQuery(String.format("%s:\"%s\"",
						facetLpseToken.getSolrField(),
						facetLpseToken.getValue()));
			}
		}
	}

	private void applyOrFacetToQuery(SolrQuery solrQuery, List<LpseToken> lpseTokens) {
		// if there is only one...
		if (lpseTokens.size() == 1) {
			FacetLpseToken facetLpseToken = (FacetLpseToken) lpseTokens.get(0);

			solrQuery.addFilterQuery(
					String.format("%s:\"%s\"",
							facetLpseToken.getSolrField(),
							facetLpseToken.getValue()));
			return;
		}

		StringBuilder stringBuilder = new StringBuilder();
		boolean isFirst = true;
		// at this point, we are going through the or filters
		for (LpseToken lpseToken : lpseTokens) {
			FacetLpseToken facetLpseToken = (FacetLpseToken) lpseToken;
			if (isFirst) {
				stringBuilder
						.append(facetLpseToken.getSolrField())
						.append(":(");
			}

			if (!isFirst) {
				stringBuilder.append(" OR ");
			}

			stringBuilder
					.append("\"")
					.append(facetLpseToken.getValue())
					.append("\"");

			isFirst = false;
		}

		stringBuilder.append(")");
		solrQuery.addFilterQuery(stringBuilder.toString());
	}

	protected void applyToQueryInternal(SolrQuery solrQuery, Map<String, List<LpseToken>> panlTokenMap) {
		List<LpseToken> lpseTokens = panlTokenMap.get(getLpseCode());

		// check to see whether this is a RANGE facet

		if (isRangeFacet) {
			applyRangeFacetToQuery(solrQuery, lpseTokens);
			return;
		}

		if (isOrFacet) {
			applyOrFacetToQuery(solrQuery, lpseTokens);
			return;
		}

		// At this point, we just have regular facets.
		for (LpseToken lpseToken : lpseTokens) {
			FacetLpseToken facetLpseToken = (FacetLpseToken) lpseToken;
			solrQuery.addFilterQuery(String.format("%s:\"%s\"",
					facetLpseToken.getSolrField(),
					facetLpseToken.getValue()));
		}
	}
}
