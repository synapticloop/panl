package com.synapticloop.panl.server.handler.fielderiser.field.facet;

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
 * IN THE SOFTWARE.
 */

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.OrFacetLpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.synapticloop.panl.server.handler.processor.Processor.*;
import static com.synapticloop.panl.server.handler.processor.Processor.FORWARD_SLASH;

public class PanlOrFacetField extends PanlFacetField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlOrFacetField.class);

	public static final String JSON_KEY_IS_OR_FACET = "is_or_facet";
	public static final String JSON_KEY_OR_SEPARATOR = "or_separator";

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	//                            OR Facet properties                          //
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	protected boolean isAlwaysOr = false;
	protected String orSeparator = null;

	public PanlOrFacetField(String lpseCode, String propertyKey, Properties properties, String solrCollection, String panlCollectionUri, int lpseLength) throws PanlServerException {
		super(lpseCode, propertyKey, properties, solrCollection, panlCollectionUri, lpseLength);

		this.isAlwaysOr = properties.getProperty(PROPERTY_KEY_PANL_OR_ALWAYS + lpseCode, "false").equalsIgnoreCase("true");
		this.orSeparator = properties.getProperty(PROPERTY_KEY_PANL_OR_SEPARATOR + lpseCode, null);
	}

	@Override public List<String> explainAdditional() {
		List<String> explanations = new ArrayList<>(super.explainAdditional());
		explanations.add("Is an OR facet which will allow multiple selections of this facet, consequently increasing the number of results.");
		return (explanations);
	}

	@Override public Logger getLogger() {
		return (LOGGER);
	}

	@Override protected void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokenList) {
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
		jsonObject.put(JSON_KEY_IS_OR_FACET, true);
		if(null != orSeparator) {
			jsonObject.put(JSON_KEY_OR_SEPARATOR, orSeparator);
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
				facetValueObject.put(JSON_KEY_VALUE, valueName);
				facetValueObject.put(JSON_KEY_COUNT, value.getCount());
				facetValueObject.put(JSON_KEY_ENCODED, getEncodedPanlValue(valueName));
				// the OR encoding has no prefix or suffix
				facetValueObject.put(JSON_KEY_ENCODED_OR, URLEncoder.encode(valueName, StandardCharsets.UTF_8));
				facetValueArrays.put(facetValueObject);
			}
		}

		// if we don't have any values for this facet, don't put it in

		if (!facetValueArrays.isEmpty()) {
			facetObject.put(JSON_KEY_VALUES, facetValueArrays);
			facetObject.put(JSON_KEY_FACET_LIMIT, collectionProperties.getSolrFacetLimit());
			if (null != lpseCode) {
				facetObject.put(JSON_KEY_URIS,
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
	 * <p>Get the Start of the OR URI path for this field</p>
	 *
	 * @param panlTokenMap The token map with the LPSe codes and values
	 *
	 * @return The URI path
	 */
	private String getOrURIPathStart(Map<String, List<LpseToken>> panlTokenMap) {
		StringBuilder sb = new StringBuilder();

		if(orSeparator != null) {
			if (hasValuePrefix) {
				sb.append(URLEncoder.encode(valuePrefix, StandardCharsets.UTF_8));
			}
		}

		if (panlTokenMap.containsKey(lpseCode)) {
			if(orSeparator != null) {

				List<String> validValues = new ArrayList<>();

				for (LpseToken lpseToken : panlTokenMap.get(lpseCode)) {
					if (lpseToken.getIsValid() && lpseToken.getValue() != null) {
						validValues.add(lpseToken.getValue());
					}
				}

				if(!validValues.isEmpty()) {
					for(String validValue: validValues) {
						sb.append(URLEncoder.encode(validValue, StandardCharsets.UTF_8));
						sb.append(URLEncoder.encode(orSeparator, StandardCharsets.UTF_8));
					}
				}
			} else {
				for (LpseToken lpseToken : panlTokenMap.get(lpseCode)) {
					if (lpseToken.getIsValid()) {
						sb.append(getEncodedPanlValue(lpseToken));
						sb.append("/");
					}
				}
				return(sb.toString());
			}
		}

		if(orSeparator != null) {
			if (hasValueSuffix) {
				sb.append(URLEncoder.encode(valueSuffix, StandardCharsets.UTF_8));
			}
		}

		return (sb.toString());
	}

	/**
	 * <p>This is an OR facet, so we can additional </p>
	 *
	 * @param collectionProperties The collection properties
	 * @param lpseField The LPSE field that this applies to
	 * @param panlTokenMap The inbound Panl tokens
	 *
	 * @return The JSON object with the URIs for adding this field to the
	 * 		existing search URI.
	 */
	@Override
	protected JSONObject getAdditionURIObject(
			CollectionProperties collectionProperties,
			BaseField lpseField,
			Map<String, List<LpseToken>> panlTokenMap) {

		JSONObject additionObject = new JSONObject();

		StringBuilder lpseUri = new StringBuilder(FORWARD_SLASH);
		StringBuilder lpseUriBefore = new StringBuilder();
		StringBuilder lpseUriCode = new StringBuilder();

		Map<String, Boolean> lpseCodeMap = new HashMap<>();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			// we need to add in any other token values in the correct order
			String orderedLpseCode = baseField.getLpseCode();

			if (orderedLpseCode.equals(this.lpseCode)) {
				// we have found the current LPSE code, so reset the URI and add it to
				// the after
				if(orSeparator != null) {
					lpseUri.append(getOrURIPathStart(panlTokenMap));
				} else {
					lpseUri.append(baseField.getURIPath(panlTokenMap, collectionProperties));
				}

				lpseUriBefore.append(lpseUri);
				lpseUri.setLength(0);

				if(orSeparator != null) {
					if(!lpseCodeMap.containsKey(this.lpseCode)) {
						lpseUriCode.append(this.lpseCode);
					}
					lpseCodeMap.put(this.lpseCode, true);
				} else {
					lpseUriCode.append(baseField.getLpseCode(panlTokenMap, collectionProperties));
					lpseUriCode.append(this.lpseCode);
				}
			} else {
				// if we don't have a current token, just carry on
				if (!panlTokenMap.containsKey(orderedLpseCode)) {
					continue;
				}

				// normally
				lpseUri.append(baseField.getURIPath(panlTokenMap, collectionProperties));
				int numTokens = panlTokenMap.get(orderedLpseCode).size();
				if(numTokens == 1) {
					// if we have a range facet - we need to make sure that we are
					// encoding it correctly there can only be one range token for the
					// panl field (no over-lapping ranges, or distinct ranges)

					// if it is not a range facet - then this won't do any harm and is a
					// better implementation
					lpseUriCode.append(baseField.getLpseCode(panlTokenMap.get(orderedLpseCode).get(0), collectionProperties));
				} else {
					lpseUriCode.append(new String(new char[numTokens]).replace("\0", baseField.getLpseCode()));
				}
			}
		}

		additionObject.put(JSON_KEY_BEFORE, lpseUriBefore.toString());

		additionObject.put(JSON_KEY_AFTER, FORWARD_SLASH + lpseUri + lpseUriCode + FORWARD_SLASH);
		return (additionObject);
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

		if(this.orSeparator != null) {
			// we have an or separator
			return(OrFacetLpseToken.getSeparatedLpseTokens(orSeparator, collectionProperties, this.lpseCode, lpseTokeniser, valueTokeniser));
		} else {
			return (List.of(new OrFacetLpseToken(collectionProperties, this.lpseCode, lpseTokeniser, valueTokeniser)));
		}
	}

	@Override public void addToRemoveObject(JSONObject removeObject, LpseToken lpseToken) {
		removeObject.put(JSON_KEY_IS_OR_FACET, true);
	}

	/**
	 * <p>Return whether this is an ALWAYS OR facet field</p>
	 *
	 * @return Whether this is an ALWAYS OR facet field
	 */
	public boolean getIsAlwaysOr() {
		return(isAlwaysOr);
	}

	/**
	 * <p>Return the OR separator for OR field values.  This will return null if
	 * no OR separator is configured.</p>
	 *
	 * @return the string for the OR separator, or null if not set
	 */
	public String getOrSeparator() {
		return orSeparator;
	}
}
