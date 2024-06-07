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
import com.synapticloop.panl.server.handler.fielderiser.field.PanlFacetField;
import com.synapticloop.panl.server.handler.tokeniser.token.FacetLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActiveProcessor extends Processor {
	public static final String JSON_KEY_VALUE = "value";
	public static final String JSON_KEY_URI = "uri";
	public static final String JSON_KEY_PANL_CODE = "panl_code";
	public static final String JSON_KEY_FACET_NAME = "facet_name";
	public static final String JSON_KEY_NAME = "name";
	public static final String JSON_KEY_ENCODED = "encoded";
	public static final String JSON_KEY_VALUE_TO = "value_to";
	public static final String JSON_KEY_IS_RANGE_FACET = "is_range_facet";
	public static final String JSON_KEY_IS_OR_FACET = "is_or_facet";

	public ActiveProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	@Override public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, Object... params) {
		JSONObject jsonObject = new JSONObject();

		// Get all the LPSE tokens
		List<LpseToken> lpseTokens = new ArrayList<>();
		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			List<LpseToken> lpseTokenList = panlTokenMap.getOrDefault(lpseField.getLpseCode(), new ArrayList<>());
			for(LpseToken lpseToken: lpseTokenList) {
				if(lpseToken.getIsValid()) {
					lpseTokens.add(lpseToken);
				}
			}
		}

		// go through each of the tokens and generate the removal URL

		List<String> uriComponents = new ArrayList<>();
		List<String> lpseComponents = new ArrayList<>();

		for (LpseToken lpseToken : lpseTokens) {
			BaseField lpseField = collectionProperties.getLpseField(lpseToken.getLpseCode());
			if(null != lpseField && lpseToken.getIsValid()) {
				lpseComponents.add(lpseField.getLpseCode(lpseToken, collectionProperties));
				uriComponents.add(lpseField.getURIPath(lpseToken, collectionProperties));
			}
		}

		int i = 0;
		for (LpseToken lpseToken : lpseTokens) {
			String tokenType = lpseToken.getType();
			JSONArray jsonArray = jsonObject.optJSONArray(tokenType, new JSONArray());

			JSONObject removeObject = new JSONObject();

			removeObject.put(JSON_KEY_VALUE, lpseToken.getValue());
			if(lpseToken instanceof FacetLpseToken) {
				FacetLpseToken facetLpseToken = (FacetLpseToken) lpseToken;
				if(facetLpseToken.getIsRangeToken()) {
					removeObject.put(JSON_KEY_VALUE_TO, facetLpseToken.getToValue());
				}

				removeObject.put(JSON_KEY_IS_RANGE_FACET, facetLpseToken.getIsRangeToken());
			}

			removeObject.put(JSON_KEY_URI, getRemoveURIFromPath(i, uriComponents, lpseComponents));

			String lpseCode = lpseToken.getLpseCode();
			removeObject.put(JSON_KEY_PANL_CODE, lpseCode);
			removeObject.put(JSON_KEY_FACET_NAME, collectionProperties.getSolrFieldNameFromLpseCode(lpseCode));
			removeObject.put(JSON_KEY_NAME, collectionProperties.getPanlNameFromPanlCode(lpseCode));

			BaseField lpseField = collectionProperties.getLpseField(lpseCode);

			removeObject.put(JSON_KEY_IS_OR_FACET, lpseField.getIsOrFacet());

			removeObject.put(JSON_KEY_ENCODED, lpseField.getEncodedPanlValue(lpseToken));

			jsonArray.put(removeObject);
			i++;
			jsonObject.put(tokenType, jsonArray);
		}
		return (jsonObject);
	}

	private static String getRemoveURIFromPath(int skipNumber, List<String> uriComponents, List<String> lpseComponents) {
		StringBuilder uri = new StringBuilder();
		StringBuilder lpse = new StringBuilder();
		for (int i = 0; i < uriComponents.size(); i++) {
			if (i != skipNumber) {
				uri.append(uriComponents.get(i));
				lpse.append(lpseComponents.get(i));
			}
		}

		String test = "/" + uri + lpse + "/";

		if (test.equals("//")) {
			return ("/");
		} else {
			return test;
		}
	}
}
