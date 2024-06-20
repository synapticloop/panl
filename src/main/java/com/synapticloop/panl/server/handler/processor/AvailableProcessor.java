package com.synapticloop.panl.server.handler.processor;

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

import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.FacetLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <p>Process all facets (including RANGE and OR) facets that are available for
 * selection by the user.</p>
 *
 * @author synapticloop
 */
public class AvailableProcessor extends Processor {

	public AvailableProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	@Override public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, QueryResponse queryResponse) {
		JSONObject jsonObject = new JSONObject();

		List<LpseToken> lpseTokens = new ArrayList<>();

		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			// These codes are ignored, just carry on
			if(collectionProperties.getIsIgnoredLpseCode(lpseField.getLpseCode())) {
				continue;
			}
			lpseTokens.addAll(panlTokenMap.getOrDefault(lpseField.getLpseCode(), new ArrayList<>()));
		}

		SolrDocumentList solrDocuments = (SolrDocumentList) queryResponse.getResponse().get(JSON_KEY_SOLR_JSON_KEY_RESPONSE);
		long numFound = solrDocuments.getNumFound();
		boolean numFoundExact = solrDocuments.getNumFoundExact();

		Map<String, Set<String>> panlLookupMap = new HashMap<>();
		for (LpseToken lpseToken : lpseTokens) {
			String panlLpseValue = lpseToken.getValue();
			// These codes are ignored, just carry on
			if(collectionProperties.getIsIgnoredLpseCode(lpseToken.getLpseCode())) {
				continue;
			}

			if (null != panlLpseValue) {
				String lpseCode = lpseToken.getLpseCode();
				Set<String> valueSet = panlLookupMap.get(lpseCode);

				if (null == valueSet) {
					valueSet = new HashSet<>();
				}
				valueSet.add(panlLpseValue);
				panlLookupMap.put(lpseCode, valueSet);
			}
		}

		JSONArray panlFacets = new JSONArray();
		Map<String, JSONObject> panlFacetOrderMap = new LinkedHashMap<>();

		JSONArray rangeFacetArray = new JSONArray();

		for (FacetField facetField : queryResponse.getFacetFields()) {
			// These codes are ignored, just carry on
			if(collectionProperties.getIsIgnoredLpseCode(collectionProperties.getPanlCodeFromSolrFacetFieldName(facetField.getName()))) {
				continue;
			}

			String lpseCode = collectionProperties.getPanlCodeFromSolrFacetFieldName(facetField.getName());
			BaseField baseField = collectionProperties.getLpseField(lpseCode);

			if (facetField.getValueCount() != 0) {
				JSONObject facetObject = new JSONObject();
				JSONObject rangeFacetObject = new JSONObject();
				baseField.appendAvailableFacetObject(facetObject);
				if(baseField.appendAvailableValues(facetObject, collectionProperties, panlTokenMap, facetField.getValues(), numFound, numFoundExact)) {
					panlFacetOrderMap.put(lpseCode, facetObject);
				}

				baseField.addToAdditionObject(facetObject, panlTokenMap);

				if(baseField.appendAvailableRangeValues(rangeFacetObject, collectionProperties, panlTokenMap)) {
					rangeFacetArray.put(rangeFacetObject);
				}
			}
		}


		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			if (panlFacetOrderMap.containsKey(lpseField.getLpseCode())) {
				panlFacets.put(panlFacetOrderMap.get(lpseField.getLpseCode()));
			}
		}

		jsonObject.put(JSON_KEY_FACETS, panlFacets);

		jsonObject.put(JSON_KEY_RANGE_FACETS, rangeFacetArray);
		return (jsonObject);
	}

	/**
	 * <p>Get the addition URI Object for facets.  The addition URI will always
	 * reset the page number LPSE code</p>
	 *
	 * @param lpseField The LPSE field to add to the URI
	 * @param panlTokenMap The Map of existing tokens that are already in the URI
	 *
	 * @return The addition URI
	 */

	private JSONObject getAdditionURIObject(BaseField lpseField, Map<String, List<LpseToken>> panlTokenMap, boolean shouldRange) {
		String additionLpseCode = lpseField.getLpseCode();
		JSONObject additionObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder(FORWARD_SLASH);
		StringBuilder lpseUriAfterMax = new StringBuilder();
		StringBuilder lpseCode = new StringBuilder();

		// TODO - clean up this logic
		for (BaseField baseField : collectionProperties.getLpseFields()) {
			if(shouldRange) {
				// whether we have an addition to the URI - we are going to ignore it
				// as it is always a replacement (you cannot have two ranges available)

			}

			if (panlTokenMap.containsKey(baseField.getLpseCode()) &&
							!(shouldRange &&
									baseField.getLpseCode().equals(additionLpseCode))) {

				String resetUriPath = baseField.getResetUriPath(panlTokenMap, collectionProperties);
				lpseUri.append(resetUriPath);

				if(lpseUriAfterMax.length() != 0) {
					lpseUriAfterMax.append(resetUriPath);
				}

				lpseCode.append(baseField.getResetLpseCode(panlTokenMap, collectionProperties));
			}

			if (baseField.getLpseCode().equals(additionLpseCode)) {
				if (shouldRange) {
					// depends on whether there is an infix
					// at this point we want to also do the min value replacement, if it
					// exists
					if(null != baseField.getRangeMinValueReplacement()) {
						additionObject.put(JSON_KEY_BEFORE_MIN_VALUE, lpseUri.toString() + URLEncoder.encode(baseField.getRangeMinValueReplacement(), StandardCharsets.UTF_8));
					}

					if (lpseField.getHasRangeInfix()) {
						// we have an infix - we will be using the range value prefix/suffix
						lpseUri.append(URLEncoder.encode(baseField.getRangePrefix(), StandardCharsets.UTF_8));
					} else {
						// we don't have an infix - we will be using the value prefix/suffix
						lpseUri.append(URLEncoder.encode(baseField.getValuePrefix(), StandardCharsets.UTF_8));
					}

					lpseCode.append(lpseField.getLpseCode());
					lpseCode.append((lpseField.getHasRangeInfix() ? "-" : "+"));

					if (baseField.getHasRangeInfix()) {
						// we have the infix
						additionObject.put(JSON_KEY_HAS_INFIX, true);
						additionObject.put(JSON_KEY_DURING, URLEncoder.encode(baseField.getRangeValueInfix(), StandardCharsets.UTF_8));
					} else {
						// we shall use the value suffix and prefix;
						additionObject.put(JSON_KEY_HAS_INFIX, false);
						additionObject.put(
								JSON_KEY_DURING,
								URLEncoder.encode(baseField.getValueSuffix(), StandardCharsets.UTF_8) +
										JSON_VALUE_NO_INFIX_REPLACEMENT +
										URLEncoder.encode(baseField.getValuePrefix(), StandardCharsets.UTF_8));
					}
				}

				additionObject.put(JSON_KEY_BEFORE, lpseUri.toString());
				lpseUri.setLength(0);
				lpseCode.append(baseField.getLpseCode());

				if(shouldRange) {
					if(baseField.getHasRangeInfix()) {
						lpseUri.append(URLEncoder.encode(baseField.getRangeSuffix(), StandardCharsets.UTF_8));
					} else {
						lpseUri.append(URLEncoder.encode(baseField.getValueSuffix(), StandardCharsets.UTF_8));
					}

					if(null != baseField.getRangeMaxValueReplacement()) {
						lpseUriAfterMax.append(URLEncoder.encode(baseField.getRangeMaxValueReplacement(), StandardCharsets.UTF_8))
								.append(FORWARD_SLASH);
					}
				}
				lpseUri.append(FORWARD_SLASH);
			}
		}

		additionObject.put(JSON_KEY_AFTER, lpseUri.toString() + lpseCode.toString() + FORWARD_SLASH);
		additionObject.put(JSON_KEY_AFTER_MAX_VALUE, lpseUriAfterMax.toString() + lpseCode.toString() + FORWARD_SLASH);
		return (additionObject);
	}
}