package com.synapticloop.panl.server.handler.fielderiser.field.facet;

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
import com.synapticloop.panl.server.handler.fielderiser.field.BasePrefixSuffixField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.FacetLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.OrFacetLpseToken;
import com.synapticloop.panl.util.Constants;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * <p>A Panl facet field comes in five flavours:</p>
 *
 * <ol>
 *   <li>A regular facet,</li>
 *   <li>A RANGE facet,</li>
 *   <li>An OR facet, or</li>
 *   <li>A BOOLEAN facet</li>
 *   <li>A DATE Range facet - </li>
 * </ol>
 *
 * <p>This class deals with the regular facet</p>
 *
 * {@link com.synapticloop.panl.server.handler.fielderiser.field.facet.PanlRangeFacetField RANGE facet (PanlRangeFacetField)}
 * {@link com.synapticloop.panl.server.handler.fielderiser.field.facet.PanlOrFacetField RANGE facet (PanlOrFacetField)}
 * {@link com.synapticloop.panl.server.handler.fielderiser.field.facet.PanlBooleanFacetField RANGE facet (PanlBooleanFacetField)}
 * {@link com.synapticloop.panl.server.handler.fielderiser.field.facet.PanlDateRangeFacetField RANGE facet (PanlDateRangeFacetField)}
 */
public class PanlFacetField extends BasePrefixSuffixField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlFacetField.class);

	protected String solrFieldType;

	public PanlFacetField(
			String lpseCode,
			String propertyKey,
			Properties properties,
			String solrCollection,
			String panlCollectionUri,
			int lpseLength) throws PanlServerException {

		super(lpseCode, propertyKey, properties, solrCollection, panlCollectionUri, lpseLength);

		populateSolrFieldType();

		validateProperties();

		populateSolrFieldTypeValidation();

		populatePanlAndSolrFieldNames();

		logDetails();
	}

	private void populateSolrFieldType() {
		this.solrFieldType = properties.getProperty(Constants.Property.Panl.PANL_TYPE + lpseCode);
	}

	@Override public Logger getLogger() {
		return (LOGGER);
	}

	@Override public List<String> explainAdditional() {
		List<String> explanations = new ArrayList<>();
		explanations.add("A Solr field that can be used as a facet, returned in the field set, or configured to be sorted by.");
		explanations.addAll(super.explainAdditional());
		return(explanations);
	}


	protected void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokenList, CollectionProperties collectionProperties) {
		// At this point, we just have regular facets.
		for (LpseToken lpseToken : lpseTokenList) {
			FacetLpseToken facetLpseToken = (FacetLpseToken) lpseToken;
			solrQuery.addFilterQuery(
						facetLpseToken.getSolrField() +
									":\"" +
									facetLpseToken.getValue() +
									"\"");
		}
	}

	@Override
	public List<LpseToken> instantiateTokens(CollectionProperties collectionProperties, String lpseCode, String query, StringTokenizer valueTokeniser, LpseTokeniser lpseTokeniser) {
		if (this.valueSeparator != null) {
			// we have an or separator
			return (FacetLpseToken.getSeparatedLpseTokens(
					valueSeparator,
					collectionProperties,
					this.lpseCode,
					lpseTokeniser,
					valueTokeniser));

		} else {
			return(List.of(new FacetLpseToken(collectionProperties, this.lpseCode, lpseTokeniser, valueTokeniser)));
		}
	}

	@Override public String getPanlFieldType() {
		return("FACET");
	}
}
