package com.synapticloop.panl.server.handler.processor;

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

import com.synapticloop.panl.server.handler.fielderiser.field.facet.PanlDateRangeFacetField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.util.Constants;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONArray;
import org.json.JSONObject;

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

		SolrDocumentList solrDocuments =
				(SolrDocumentList) queryResponse.getResponse().get(Constants.Json.Solr.RESPONSE);
		long numFound = solrDocuments.getNumFound();
		boolean numFoundExact = solrDocuments.getNumFoundExact();

		JSONArray panlFacets = new JSONArray();
		Map<String, JSONObject> panlFacetOrderMap = new LinkedHashMap<>();

		JSONArray rangeFacetArray = new JSONArray();
		JSONArray dateRangeFacetArray = new JSONArray();

		for (FacetField facetField : queryResponse.getFacetFields()) {
			// These codes are ignored, just carry on
			if(collectionProperties.getIsIgnoredLpseCode(collectionProperties.getPanlCodeFromSolrFacetFieldName(facetField.getName()))) {
				continue;
			}

			String lpseCode = collectionProperties.getPanlCodeFromSolrFacetFieldName(facetField.getName());
			BaseField baseField = collectionProperties.getLpseField(lpseCode);

			// unlikely to get a facet field that wasn't selected...
			if(null == baseField) {
				continue;
			}

			if (facetField.getValueCount() != 0) {
				JSONObject facetObject = new JSONObject();
				baseField.appendToAvailableFacetObject(facetObject);
				if(baseField.appendAvailableValues(
						facetObject,
						collectionProperties,
						panlTokenMap,
						panlLookupMap.getOrDefault(lpseCode, new HashSet<>()),
						facetField.getValues(),
						numFound,
						numFoundExact)) {

					panlFacetOrderMap.put(lpseCode, facetObject);
				}

				baseField.addToAdditionObject(facetObject, panlTokenMap);
			}

			// these range facets will always appear
			JSONObject rangeFacetObject = new JSONObject();
			if(baseField.appendAvailableRangeValues(rangeFacetObject, collectionProperties, panlTokenMap)) {
				rangeFacetArray.put(rangeFacetObject);
			}

		}

		// Date ranges always appear, but they are not included in the field set
		for (PanlDateRangeFacetField dateRangeFacetField : collectionProperties.getDateRangeFacetFields()) {
			JSONObject dateRangeFacetObject = new JSONObject();
			if(dateRangeFacetField.appendAvailableDateRangeValues(dateRangeFacetObject, collectionProperties, panlTokenMap)) {
				dateRangeFacetArray.put(dateRangeFacetObject);
			}
		}

		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			if (panlFacetOrderMap.containsKey(lpseField.getLpseCode())) {
				panlFacets.put(panlFacetOrderMap.get(lpseField.getLpseCode()));
			}
		}

		jsonObject.put(Constants.Json.Panl.FACETS, panlFacets);
		jsonObject.put(Constants.Json.Panl.RANGE_FACETS, rangeFacetArray);
		jsonObject.put(Constants.Json.Panl.DATE_RANGE_FACETS, dateRangeFacetArray);
		return (jsonObject);
	}
}
