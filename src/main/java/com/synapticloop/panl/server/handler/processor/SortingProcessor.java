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
 * IN THE SOFTWARE.
 */

import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.SortLpseToken;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * <p>The Sorting processor adds in the sorting options that are available for
 * the Panl JSON return object.</p>
 */
public class SortingProcessor extends Processor {


	public static final String SORTING_OPTION_DESC = "-";
	public static final String SORTING_OPTION_ASC = "+";

	public SortingProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	/**
	 * <p>There are two Sorting URIs - An additive URI, and a replacement URI,
	 * unlike other LPSE codes - these are a finite, set number of sort fields which are defined by the
	 * <code>panl.sort.fields</code> property.</p>
	 *
	 * @param panlTokenMap the map of LPSE codes to list of panl tokens
	 *
	 * @return The JSON object with the keys and relevant URI paths
	 */
	public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, QueryResponse queryResponse) {
		String before = "";
		String panlParamSortLpseKey = collectionProperties.getPanlParamSort();

		// Run through the sorting order
		JSONObject jsonObject = new JSONObject();
		StringBuilder replaceLpseUri = new StringBuilder(FORWARD_SLASH);
		StringBuilder lpseCode = new StringBuilder();

		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			String thisLpseCode = lpseField.getLpseCode();
			if (!panlParamSortLpseKey.equals(thisLpseCode)) {
				if (panlTokenMap.containsKey(thisLpseCode)) {
					replaceLpseUri.append(lpseField.getResetUriPath(panlTokenMap, collectionProperties));
					lpseCode.append(lpseField.getResetLpseCode(panlTokenMap, collectionProperties));
				}
			} else {
				before = lpseCode.toString();
				lpseCode.setLength(0);
			}
		}

		lpseCode.append(FORWARD_SLASH);

		// This is the reset URI link (i.e. remove all sort orders and go back to
		// sorting by relevance descending)
		String finalBefore = replaceLpseUri + before;
		if (finalBefore.length() + lpseCode.length() == 2) {
			// we have nothing in the URI - i.e. the URI is "//"
			jsonObject.put(JSON_KEY_REMOVE_URI, FORWARD_SLASH);
		} else {
			jsonObject.put(JSON_KEY_REMOVE_URI, finalBefore + lpseCode);
		}

		// These are the available sort fields
		JSONArray sortFieldsArray = new JSONArray();

		// build up a data set of all the active sorting, and the sort order URI key

		// now build the before and after maps for the addition uris
		StringBuilder sortLpse = new StringBuilder();
		String sortBefore = "";

		HashMap<String, String> activeSortings = new HashMap<>();

		for (LpseToken lpseToken : panlTokenMap.getOrDefault(panlParamSortLpseKey, new ArrayList<>())) {
			SortLpseToken sortLpseToken = (SortLpseToken) lpseToken;
			activeSortings.put(sortLpseToken.getLpseSortCode(), sortLpseToken.getSortOrderUriKey());
			sortLpse.append(panlParamSortLpseKey)
			        .append(sortLpseToken.getLpseSortCode())
			        .append(sortLpseToken.getSortOrderUriKey());
		}
		sortBefore = sortLpse.toString();


		for (String sortFieldLpseCode : collectionProperties.getSortFieldLpseCodes()) {
			String sortFieldName = collectionProperties.getSolrFieldNameFromLpseCode(sortFieldLpseCode);

			if (null != sortFieldName) {
				JSONObject sortObject = new JSONObject();

				sortObject.put(JSON_KEY_NAME, collectionProperties.getPanlNameFromPanlCode(sortFieldLpseCode));
				sortObject.put(JSON_KEY_FACET_NAME, collectionProperties.getSolrFieldNameFromLpseCode(sortFieldLpseCode));
				sortObject.put(JSON_KEY_SET_URI_DESC,
					finalBefore + panlParamSortLpseKey + sortFieldLpseCode + SORTING_OPTION_DESC + lpseCode);
				sortObject.put(JSON_KEY_SET_URI_ASC,
					finalBefore + panlParamSortLpseKey + sortFieldLpseCode + SORTING_OPTION_ASC + lpseCode);


				// Now for the add fields
				if (!activeSortings.containsKey(sortFieldLpseCode)) {
					// at this point we need to know the ordering of the lpseCode fields,
					// whether we are before, or after the selected index

					sortObject.put(JSON_KEY_ADD_URI_DESC,
						finalBefore +
							sortBefore +
							panlParamSortLpseKey +
							sortFieldLpseCode +
							SORTING_OPTION_DESC +
							lpseCode);

					sortObject.put(JSON_KEY_ADD_URI_ASC,
						finalBefore +
							sortBefore +
							panlParamSortLpseKey +
							sortFieldLpseCode +
							SORTING_OPTION_ASC +
							lpseCode);
				}
				sortFieldsArray.put(sortObject);
			}
		}

		jsonObject.put(JSON_KEY_FIELDS, sortFieldsArray);

		return (jsonObject);
	}
}
