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

import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.QueryLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.QueryOperandLpseToken;
import com.synapticloop.panl.util.Constants;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>The search fields processor adds information to the Panl JSON response
 * object about the search fields that are available</p>
 *
 * @author synapticloop
 */
public class SearchFieldsProcessor extends Processor {

	public SearchFieldsProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	/**
	 *
	 * @param panlTokenMap The map of LPSE codes to the list of tokens
	 * @param queryResponse The Solr query response
	 *
	 * @return The JSON object with the response object
	 */
	@Override public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, QueryResponse queryResponse) {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		// now we are going to add the search fields - but only if they exist
		Map<String, String> searchCodesMap = collectionProperties.getSearchCodesMap();
		String panlParamQuery = collectionProperties.getPanlParamQuery();
		Set<String> activeSearchCodes = new HashSet<>();
		String keyword = "";

		if(panlTokenMap.containsKey(panlParamQuery)) {
			QueryLpseToken queryLpseToken = (QueryLpseToken)panlTokenMap.get(panlParamQuery).get(0);
			activeSearchCodes.addAll(queryLpseToken.getSearchableLpseFields());
			keyword = queryLpseToken.getValue();
		}

		String panlParamQueryOperand = collectionProperties.getPanlParamQueryOperand();
		String operand = collectionProperties.getDefaultQueryOperand();

		if(panlTokenMap.containsKey(panlParamQueryOperand)) {
			QueryOperandLpseToken queryOperandLpseToken = (QueryOperandLpseToken)panlTokenMap.get(panlParamQueryOperand).get(0);
			operand = queryOperandLpseToken.getValue();
		}

		if(!searchCodesMap.isEmpty()) {
			for (String panlCode : searchCodesMap.keySet()) {
				String value = searchCodesMap.get(panlCode);

				JSONObject searchFieldsObject = new JSONObject();
				searchFieldsObject.put(Constants.Json.Panl.PANL_CODE, panlCode);
				searchFieldsObject.put(Constants.Json.Panl.VALUE, collectionProperties.getPanlNameFromSearchLpseCode(panlCode));

				// now put in whether this is active...
				searchFieldsObject.put(Constants.Json.Panl.ACTIVE, activeSearchCodes.contains(value));

				jsonArray.put(searchFieldsObject);
			}
			jsonObject.put(Constants.Json.Panl.FIELDS, jsonArray);
		}

		jsonObject.put(Constants.Json.Panl.QUERY_RESPOND_TO, collectionProperties.getFormQueryRespondTo());
		jsonObject.put(Constants.Json.Panl.QUERY_OPERAND_RESPOND_TO, collectionProperties.getFormQueryOperand());

		// determine whether we have a query operand - else pass through the default

		jsonObject.put(Constants.Json.Panl.QUERY_OPERAND_SELECTED, operand);
		jsonObject.put(Constants.Json.Panl.KEYWORD, keyword);

		return jsonObject;
	}

}
