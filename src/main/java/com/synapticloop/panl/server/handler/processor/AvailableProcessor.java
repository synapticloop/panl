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

import com.synapticloop.panl.server.handler.fielderiser.CollectionProperties;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
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
	public static final String JSON_KEY_IS_RANGE_FACET = "is_range_facet";
	public static final String JSON_KEY_RANGE_FACETS = "range_facets";
	public static final String JSON_KEY_FACETS = "facets";
	public static final String JSON_KEY_MIN = "min";
	public static final String JSON_KEY_MAX = "max";
	public static final String JSON_KEY_DURING = "during";

	public AvailableProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	@Override public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, Object... params) {
		JSONObject jsonObject = new JSONObject();
		QueryResponse response = (QueryResponse) params[0];

		List<LpseToken> lpseTokens = new ArrayList<>();
		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			lpseTokens.addAll(panlTokenMap.getOrDefault(lpseField.getLpseCode(), new ArrayList<>()));
		}

		SolrDocumentList solrDocuments = (SolrDocumentList) response.getResponse().get(SOLR_JSON_KEY_RESPONSE);
		long numFound = solrDocuments.getNumFound();
		boolean numFoundExact = solrDocuments.getNumFoundExact();

		Map<String, Set<String>> panlLookupMap = new HashMap<>();
		for (LpseToken lpseToken : lpseTokens) {
			String panlLpseValue = lpseToken.getValue();
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

		for (FacetField facetField : response.getFacetFields()) {
			// if we have an or Facet and this is an or facet, then we keep all
			// values, otherwise we strip out the xero values

			if (facetField.getValueCount() != 0) {
				JSONObject facetObject = new JSONObject();
				facetObject.put(JSON_KEY_FACET_NAME, facetField.getName());
				facetObject.put(JSON_KEY_NAME, collectionProperties.getPanlNameFromSolrFieldName(facetField.getName()));

				JSONArray facetValueArrays = new JSONArray();
				String lpseCode = collectionProperties.getPanlCodeFromSolrFacetFieldName(facetField.getName());
				facetObject.put(JSON_KEY_IS_OR_FACET, collectionProperties.getIsOrFacetField(lpseCode));
				boolean isRangeFacetField = collectionProperties.getIsRangeFacetField(lpseCode);
				facetObject.put(JSON_KEY_IS_RANGE_FACET, isRangeFacetField);


				facetObject.put(JSON_KEY_PANL_CODE, lpseCode);
				List<FacetField.Count> values = facetField.getValues();
				for (FacetField.Count value : values) {
					// at this point - we need to see whether we already have the 'value'
					// as a facet - as there is no need to have it again
					boolean shouldAdd = true;

					String valueName = value.getName();

					if (panlLookupMap.containsKey(lpseCode)) {
						if (panlLookupMap.get(lpseCode).contains(valueName)) {
							shouldAdd = false;
						}
					}

					// if we have an or Facet and this is an or facet, then we keep all
					// values, otherwise we strip out the xero values
					if (collectionProperties.getHasOrFacetFields()) {
						BaseField lpseField = collectionProperties.getLpseField(lpseCode);
						if (!lpseField.getIsOrFacet()) {
							if (value.getCount() == 0) {
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

					BaseField lpseField = collectionProperties.getLpseField(lpseCode);

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
					if (null != lpseCode) {
						facetObject.put(JSON_KEY_URIS,
								getAdditionURIObject(
										collectionProperties.getLpseField(lpseCode),
										panlTokenMap,
										false));
					}
					panlFacetOrderMap.put(lpseCode, facetObject);
				}
			}
		}

		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			if (panlFacetOrderMap.containsKey(lpseField.getLpseCode())) {
				panlFacets.put(panlFacetOrderMap.get(lpseField.getLpseCode()));
			}
		}

		jsonObject.put(JSON_KEY_FACETS, panlFacets);

		JSONArray rangeFacetAray = new JSONArray();
		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			if (lpseField.getIsRangeFacet()) {
				// put this in the array please
				JSONObject facetObject = new JSONObject();
				String lpseCode = lpseField.getLpseCode();
				facetObject.put(JSON_KEY_FACET_NAME, collectionProperties.getSolrFieldNameFromLpseCode(lpseCode));
				facetObject.put(JSON_KEY_NAME, collectionProperties.getPanlNameFromPanlCode(lpseCode));
				facetObject.put(JSON_KEY_PANL_CODE, lpseCode);
				facetObject.put(JSON_KEY_MIN, lpseField.getMinRange());
				facetObject.put(JSON_KEY_MAX, lpseField.getMaxRange());

				// addition URIs are a little bit different...
				JSONObject additionURIObject = getAdditionURIObject(lpseField, panlTokenMap, true);
				if (lpseField.getHasRangeMidfix()) {
					additionURIObject.put(JSON_KEY_DURING, lpseField.getRangeMidfix());
				} else {
					additionURIObject.put(JSON_KEY_DURING, "/");
				}
				facetObject.put(JSON_KEY_URIS, additionURIObject);
				rangeFacetAray.put(facetObject);
			}
		}

		jsonObject.put(JSON_KEY_RANGE_FACETS, rangeFacetAray);
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
		StringBuilder lpseUri = new StringBuilder("/");
		StringBuilder lpseCode = new StringBuilder();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			if (panlTokenMap.containsKey(baseField.getLpseCode())) {
				lpseUri.append(baseField.getResetUriPath(panlTokenMap, collectionProperties));
				lpseCode.append(baseField.getResetLpseCode(panlTokenMap, collectionProperties));
			}

			if (baseField.getLpseCode().equals(additionLpseCode)) {
				additionObject.put(JSON_KEY_BEFORE, lpseUri.toString());
				lpseUri.setLength(0);
				lpseCode.append(baseField.getLpseCode());

				// if this is a range, then there is a different format
				if(shouldRange && lpseField.getHasRangeMidfix()) {
					lpseCode.append((lpseField.getHasRangeMidfix() ? "-" : "+"));
					lpseCode.append(lpseField.getLpseCode());
				}
			}
		}

			additionObject.put(JSON_KEY_AFTER, "/" + lpseUri + lpseCode + "/");
		return (additionObject);
	}


}