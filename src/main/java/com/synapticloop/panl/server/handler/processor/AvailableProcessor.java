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

import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.properties.field.BaseField;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class AvailableProcessor extends Processor {

	public static final String SOLR_JSON_KEY_RESPONSE = "response";
	public static final String JSON_KEY_FACET_NAME = "facet_name";
	public static final String JSON_KEY_NAME = "name";
	public static final String JSON_KEY_PANL_CODE = "panl_code";
	public static final String JSON_KEY_VALUE = "value";
	public static final String JSON_KEY_COUNT = "count";
	public static final String JSON_KEY_ENCODED = "encoded";
	public static final String JSON_KEY_VALUES = "values";
	public static final String JSON_KEY_URIS = "uris";
	public static final String JSON_KEY_BEFORE = "before";
	public static final String JSON_KEY_AFTER = "after";
	public static final String JSON_KEY_IS_OR_FACET = "is_or_facet";

	public AvailableProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	@Override public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, Object... params) {
		return(new JSONObject());
	}

	@Override public JSONArray processToArray(Map<String, List<LpseToken>> panlTokenMap, Object... params) {
		QueryResponse response = (QueryResponse) params[0];

		List<LpseToken> lpseTokens = new ArrayList<>();
		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			lpseTokens.addAll(panlTokenMap.getOrDefault(lpseField.getPanlLpseCode(), new ArrayList<>()));
		}

		SolrDocumentList solrDocuments = (SolrDocumentList) response.getResponse().get(SOLR_JSON_KEY_RESPONSE);
		long numFound = solrDocuments.getNumFound();
		boolean numFoundExact = solrDocuments.getNumFoundExact();

		Map<String, Set<String>> panlLookupMap = new HashMap<>();
		for (LpseToken lpseToken : lpseTokens) {
			String panlLpseValue = lpseToken.getValue();
			if (null != panlLpseValue) {
				String panlLpseCode = lpseToken.getLpseCode();
				Set<String> valueSet = panlLookupMap.get(panlLpseCode);

				if (null == valueSet) {
					valueSet = new HashSet<>();
				}
				valueSet.add(panlLpseValue);
				panlLookupMap.put(panlLpseCode, valueSet);
			}
		}

		JSONArray panlFacets = new JSONArray();
		Map<String, JSONObject> panlFacetOrderMap = new LinkedHashMap<>();

		for (FacetField facetField : response.getFacetFields()) {
			// if we have an or Facet and this is an or facet, then we keep all
			// values, otherwise we strip out the xero values

			if (facetField.getValueCount() != 0) {
				JSONObject facetObject = new JSONObject();
				facetObject.put(JSON_KEY_FACET_NAME, facetField.getName());
				facetObject.put(JSON_KEY_NAME, collectionProperties.getPanlNameFromSolrFieldName(facetField.getName()));

				JSONArray facetValueArrays = new JSONArray();
				String panlCodeFromSolrFacetName = collectionProperties.getPanlCodeFromSolrFacetFieldName(facetField.getName());
				facetObject.put(JSON_KEY_IS_OR_FACET, collectionProperties.getIsOrFacetField(panlCodeFromSolrFacetName));

				facetObject.put(JSON_KEY_PANL_CODE, panlCodeFromSolrFacetName);
				for (FacetField.Count value : facetField.getValues()) {
					// at this point - we need to see whether we already have the 'value'
					// as a facet - as there is no need to have it again
					boolean shouldAdd = true;

					String valueName = value.getName();

					if (panlLookupMap.containsKey(panlCodeFromSolrFacetName)) {
						if (panlLookupMap.get(panlCodeFromSolrFacetName).contains(valueName)) {
							shouldAdd = false;
						}
					}
					// if we have an or Facet and this is an or facet, then we keep all
					// values, otherwise we strip out the xero values
					if(collectionProperties.getHasOrFacetFields()) {
						BaseField lpseField = collectionProperties.getLpseField(panlCodeFromSolrFacetName);
						if(!lpseField.getIsOrFacet()) {
							if(value.getCount() == 0) {
								shouldAdd = false;
							}
						}
					}


					// also, if the count of the number of found results is the same as
					// the number of the count of the facet - then we may not need to
					// include it
					if (!collectionProperties.getPanlIncludeSameNumberFacets() &&
							numFound == value.getCount() &&
							numFoundExact) {
						shouldAdd = false;
					}

					BaseField lpseField = collectionProperties.getLpseField(panlCodeFromSolrFacetName);

					if (shouldAdd) {
						JSONObject facetValueObject = new JSONObject();
						facetValueObject.put(JSON_KEY_VALUE, valueName);
						facetValueObject.put(JSON_KEY_COUNT, value.getCount());
						facetValueObject.put(JSON_KEY_ENCODED, lpseField.getEncodedPanlValue(valueName));
						facetValueArrays.put(facetValueObject);
					}
				}

				int length = facetValueArrays.length();
				boolean shouldIncludeFacet = true;
				switch (length) {
					case 0:
						shouldIncludeFacet = false;
						break;
					case 1:
						shouldIncludeFacet = collectionProperties.getPanlIncludeSingleFacets();
						break;
				}

				// if we don't have any values for this facet, don't put it in

				if (shouldIncludeFacet) {
					facetObject.put(JSON_KEY_VALUES, facetValueArrays);
					if (null != panlCodeFromSolrFacetName) {
						facetObject.put(JSON_KEY_URIS,
								getAdditionURIObject(
										panlCodeFromSolrFacetName,
										panlTokenMap));
					}
					panlFacetOrderMap.put(panlCodeFromSolrFacetName, facetObject);
				}
			}
		}

		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			if (panlFacetOrderMap.containsKey(lpseField.getPanlLpseCode())) {
				panlFacets.put(panlFacetOrderMap.get(lpseField.getPanlLpseCode()));
			}
		}

		return(panlFacets);
	}

	/**
	 * <p>Get the addition URI Object for facets.  The addition URI will always
	 * reset the page number LPSE code</p>
	 *
	 * @param additionLpseCode The LPSE code to add to the URI
	 * @param panlTokenMap The Map of existing tokens that are already in the URI
	 *
	 * @return The addition URI
	 */

	private JSONObject getAdditionURIObject(String additionLpseCode, Map<String, List<LpseToken>> panlTokenMap) {
		JSONObject additionObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder("/");
		StringBuilder lpseCode = new StringBuilder();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			if (panlTokenMap.containsKey(baseField.getPanlLpseCode())) {
				lpseUri.append(baseField.getResetUriPath(panlTokenMap, collectionProperties));
				lpseCode.append(baseField.getResetLpseCode(panlTokenMap, collectionProperties));
			}

			if (baseField.getPanlLpseCode().equals(additionLpseCode)) {
				additionObject.put(JSON_KEY_BEFORE, lpseUri.toString());
				lpseUri.setLength(0);
				lpseCode.append(baseField.getPanlLpseCode());
			}
		}

		additionObject.append(JSON_KEY_AFTER, "/" + lpseUri + lpseCode + "/");
		return (additionObject);
	}
}