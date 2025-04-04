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
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.OrFacetLpseToken;
import com.synapticloop.panl.util.Constants;
import com.synapticloop.panl.util.PanlLPSEHelper;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PanlOrFacetField extends PanlFacetField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlOrFacetField.class);


	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	//                            OR Facet properties                          //
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	protected boolean isAlwaysOr = false;

	public PanlOrFacetField(String lpseCode, String propertyKey, Properties properties, String solrCollection,
			String panlCollectionUri, int lpseLength) throws PanlServerException {
		super(lpseCode, propertyKey, properties, solrCollection, panlCollectionUri, lpseLength);

		this.isAlwaysOr =
				properties.getProperty(Constants.Property.Panl.PANL_OR_ALWAYS + lpseCode, "false").equalsIgnoreCase("true");
		this.valueSeparator = properties.getProperty(Constants.Property.Panl.PANL_OR_SEPARATOR + lpseCode, null);
	}

	@Override public List<String> explainAdditional() {
		List<String> explanations = new ArrayList<>(super.explainAdditional());
		explanations.add(
				"Is an OR facet which will allow multiple selections of this facet, consequently increasing the number of results.");
		return (explanations);
	}

	@Override public Logger getLogger() {
		return (LOGGER);
	}

	@Override protected void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokenList, CollectionProperties collectionProperties) {
		// if there is only one... no need to do anything different
		if (lpseTokenList.size() == 1) {
			OrFacetLpseToken facetLpseToken = (OrFacetLpseToken) lpseTokenList.get(0);

			solrQuery.addFilterQuery(
					facetLpseToken.getSolrField() +
							":\"" +
							facetLpseToken.getValue() +
							"\"");
			return;
		}

		StringBuilder stringBuilder = new StringBuilder();
		boolean isFirst = true;
		// at this point, we are going through the OR filters
		for (LpseToken lpseToken : lpseTokenList) {
			OrFacetLpseToken orFacetLpseToken = (OrFacetLpseToken) lpseToken;
			if (isFirst) {
				stringBuilder
						.append(orFacetLpseToken.getSolrField())
						.append(":(");
			}

			if (!isFirst) {
				stringBuilder.append(" OR ");
			}

			stringBuilder
					.append("\"")
					.append(orFacetLpseToken.getValue())
					.append("\"");

			isFirst = false;
		}

		stringBuilder.append(")");
		solrQuery.addFilterQuery(stringBuilder.toString());
	}

	@Override public void appendToAvailableObjectInternal(JSONObject jsonObject) {
		jsonObject.put(Constants.Json.Panl.IS_OR_FACET, true);
		if (null != valueSeparator) {
			jsonObject.put(Constants.Json.Panl.VALUE_SEPARATOR, valueSeparator);
		}
	}

	/**
	 * <p>OR facets are different, in that they will return a currently selected
	 * facet and more</p>
	 *
	 * @param facetObject The facet object to append to
	 * @param collectionProperties The collection properties
	 * @param panlTokenMap The incoming Panl tokens
	 * @param existingLpseValues The existing LPSE values
	 * @param facetCountValues The facet count values
	 * @param numFound Number of results found
	 * @param numFoundExact Whether the number of results were exact
	 *
	 * @return Whether any values were appended
	 */
	@Override public boolean appendAvailableValues(
			JSONObject facetObject,
			CollectionProperties collectionProperties,
			Map<String, List<LpseToken>> panlTokenMap,
			Set<String> existingLpseValues,
			List<FacetField.Count> facetCountValues,
			long numFound,
			boolean numFoundExact) {

		JSONArray facetValueArrays = new JSONArray();

		// if we currently have the facet value, don't do anything
		Set<String> currentValueSet = new HashSet<>();
		for (LpseToken lpseToken : panlTokenMap.getOrDefault(this.lpseCode, new ArrayList<>())) {
			currentValueSet.add(lpseToken.getValue());
		}


		for (FacetField.Count value : facetCountValues) {
			boolean shouldAdd = true;
			// at this point - we need to see whether we already have the 'value'
			// as a facet - as there is no need to have it again

			String valueName = value.getName();

			if (currentValueSet.contains(valueName)) {
				continue;
			}

			// also, if the count of the number of found results is the same as
			// the number of the count of the facet - then we may not need to
			// include it
			if (!panlIncludeSameNumberFacets &&
					numFound == value.getCount() &&
					numFoundExact) {
				shouldAdd = false;
			}

			if (shouldAdd) {
				JSONObject facetValueObject = new JSONObject();
				facetValueObject.put(Constants.Json.Panl.VALUE, valueName);
				facetValueObject.put(Constants.Json.Panl.COUNT, value.getCount());
				facetValueObject.put(Constants.Json.Panl.ENCODED, getEncodedPanlValue(valueName));

				// the OR encoding has no prefix or suffix - but only if it has a value
				// separator
				if(null != valueSeparator) {
					facetValueObject.put(Constants.Json.Panl.ENCODED_MULTI, PanlLPSEHelper.encodeURIPath(valueName));
				}
				facetValueArrays.put(facetValueObject);
			}
		}

		// if we don't have any values for this facet, don't put it in

		if (!facetValueArrays.isEmpty()) {
			facetObject.put(Constants.Json.Panl.VALUES, facetValueArrays);
			facetObject.put(Constants.Json.Panl.FACET_LIMIT, collectionProperties.getSolrFacetLimit());
			if (null != lpseCode) {
				facetObject.put(Constants.Json.Panl.URIS,
						getAdditionURIObject(
								collectionProperties,
								this,
								panlTokenMap));
				return (true);
			}
		}

		return (false);
	}

	/**
	 * <p>Instantiate OR facet tokens.  This works differently from other fields
	 * in that it may have a separator character, with only one LPSE code.</p>
	 *
	 * @param collectionProperties The collection properties
	 * @param lpseCode The lpseCode for this field
	 * @param query The query parameter
	 * @param valueTokeniser The value tokeniser
	 * @param lpseTokeniser The lpse tokeniser
	 *
	 * @return The list of OR tokens
	 */
	@Override public List<LpseToken> instantiateTokens(
			CollectionProperties collectionProperties, String lpseCode,
			String query,
			StringTokenizer valueTokeniser,
			LpseTokeniser lpseTokeniser) {

		if (this.valueSeparator != null) {
			// we have an or separator
			return (OrFacetLpseToken.getSeparatedLpseTokens(
					valueSeparator,
					collectionProperties,
					this.lpseCode,
					lpseTokeniser,
					valueTokeniser));

		} else {
			return (List.of(new OrFacetLpseToken(collectionProperties, this.lpseCode, lpseTokeniser, valueTokeniser)));
		}
	}

	@Override public void addToRemoveObject(JSONObject removeObject, LpseToken lpseToken) {
		removeObject.put(Constants.Json.Panl.IS_OR_FACET, true);
	}

	/**
	 * <p>Return whether this is an ALWAYS OR facet field, an always OR field will
	 * return the facet values for this specific facet, even if the facet will not increase the number of results (i.e.
	 * the facet count is zero).</p>
	 *
	 * @return Whether this is an ALWAYS OR facet field
	 */
	public boolean getIsAlwaysOr() {
		return (isAlwaysOr);
	}


	@Override public String getPanlFieldType() {
		return("OR");
	}

}
