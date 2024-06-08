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
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class SortingProcessor extends Processor {


	public SortingProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	/**
	 * <p>There are two Sorting URIs - An additive URI, and a replacement URI,
	 * unlike other LPSE codes - these are a finite, set number of sort fields
	 * which are defined by the panl.sort.fields property.</p>
	 *
	 * @param panlTokenMap the map of LPSE codes to list of panl tokens
	 *
	 * @return The JSON object with the keys and relevant URI paths
	 */
	public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, QueryResponse queryResponse) {
		String before = "";
		String lpseCode = collectionProperties.getPanlParamSort();

		// Run through the sorting order
		JSONObject jsonObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder("/");
		StringBuilder lpse = new StringBuilder();

		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			String thisLpseCode = lpseField.getLpseCode();
			if(!lpseCode.equals(thisLpseCode)) {
				if(panlTokenMap.containsKey(thisLpseCode)) {
					lpseUri.append(lpseField.getResetUriPath(panlTokenMap, collectionProperties));
					lpse.append(lpseField.getResetLpseCode(panlTokenMap, collectionProperties));
				}
			} else {
				before = lpse.toString();
				lpse.setLength(0);
			}
		}

		lpse.append("/");

		// This is the default sorting order (by relevance)
		String finalBefore = lpseUri + before + collectionProperties.getPanlParamSort();

		JSONObject relevanceSort = new JSONObject();
		relevanceSort.put(JSON_KEY_NAME, JSON_VALUE_RELEVANCE);
		relevanceSort.put(JSON_KEY_REPLACE_DESC, finalBefore + "-" + lpse);
		jsonObject.put(JSON_KEY_RELEVANCE, relevanceSort);

		// These are the defined sort fields
		JSONArray sortFieldsArray = new JSONArray();

		for (String sortFieldLpseCode : collectionProperties.getSortFieldLpseCodes()) {
			String sortFieldName = collectionProperties.getSolrFieldNameFromLpseCode(sortFieldLpseCode);
			if (null != sortFieldName) {
				JSONObject sortObject = new JSONObject();
				sortObject.put(JSON_KEY_NAME, collectionProperties.getPanlNameFromPanlCode(sortFieldLpseCode));
				sortObject.put(JSON_KEY_FACET_NAME, collectionProperties.getSolrFieldNameFromLpseCode(sortFieldLpseCode));
				sortObject.put(JSON_KEY_REPLACE_DESC, finalBefore + sortFieldLpseCode + "-" + lpse);
				sortObject.put(JSON_KEY_REPLACE_ASC, finalBefore + sortFieldLpseCode + "+" + lpse);
				sortFieldsArray.put(sortObject);
			}
		}

		jsonObject.put(JSON_KEY_FIELDS, sortFieldsArray);

		return (jsonObject);
	}
}
