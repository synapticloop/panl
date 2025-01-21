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
import com.synapticloop.panl.server.handler.tokeniser.token.facet.BooleanFacetLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.SortLpseToken;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <p>The active processor adds all of the current active filters that are
 * passed through THE lpse PATH the returned panl JSON object.</p>
 *
 * @author synapticloop
 */
public class ActiveProcessor extends Processor {

	public static final String JSON_KEY_SORT_FIELDS = "sort_fields";

	public ActiveProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	@Override public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, QueryResponse queryResponse) {
		JSONObject jsonObject = new JSONObject();

		// Get all the LPSE tokens
		List<LpseToken> lpseTokens = new ArrayList<>();
		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			// These codes are ignored, just carry on
			if (collectionProperties.getIsIgnoredLpseCode(lpseField.getLpseCode())) {
				continue;
			}

			List<LpseToken> lpseTokenList = panlTokenMap.getOrDefault(lpseField.getLpseCode(), new ArrayList<>());
			for (LpseToken lpseToken : lpseTokenList) {
				if (lpseToken.getIsValid()) {
					lpseTokens.add(lpseToken);
				}
			}
		}

		// go through each of the tokens and generate the removal URL

		List<String> uriComponents = new ArrayList<>();
		List<String> lpseComponents = new ArrayList<>();

		// the following set contains whether we have added an or separator code to
		// the LPSEComponents - if we have, then we have already added the LPSE
		// code, which only requires one code, not multiple
		Set<String> foundOrSeparator = new HashSet<>();
		for (LpseToken lpseToken : lpseTokens) {
			BaseField lpseField = collectionProperties.getLpseField(lpseToken.getLpseCode());
			if (null != lpseField && lpseToken.getIsValid()) {
				// if it is an or separator - we only need one LPSE token
				if(collectionProperties.getIsOrSeparatorFacetField(lpseToken.getLpseCode())) {
					if(!foundOrSeparator.contains(lpseField.getLpseCode())) {
						lpseComponents.add(lpseField.getResetLpseCode(panlTokenMap, collectionProperties));
					}
					foundOrSeparator.add(lpseField.getLpseCode());
				} else {
					lpseComponents.add(lpseField.getResetLpseCode(panlTokenMap, collectionProperties));
				}

				uriComponents.add(lpseField.getResetUriPath(lpseToken, collectionProperties));
			}
		}

		// clear as this is used for the incrementor 'skipNumber' below
		foundOrSeparator.clear();

		JSONObject activeSortObject = new JSONObject();
		int skipNumber = 0;
		int lpseSkipNumber = 0;
		for (LpseToken lpseToken : lpseTokens) {
			String tokenType = lpseToken.getType();
			String lpseCode = lpseToken.getLpseCode();
			BaseField lpseField = collectionProperties.getLpseField(lpseCode);

			boolean shouldAddObject = true;

			JSONArray jsonArray = jsonObject.optJSONArray(tokenType, new JSONArray());
			JSONObject removeObject = new JSONObject();


			removeObject.put(JSON_KEY_VALUE, lpseToken.getValue());

			System.out.println(getRemoveURIFromPath(skipNumber, lpseTokens, collectionProperties));

			removeObject.put(JSON_KEY_REMOVE_URI, getRemoveURIFromPath(skipNumber, lpseSkipNumber, uriComponents, lpseComponents));
			removeObject.put(JSON_KEY_PANL_CODE, lpseCode);

			// add any additional keys that are required by the children of the base
			// fields
			lpseField.addToRemoveObject(removeObject, lpseToken);

			// Sort objects are a little bit different, as they add to two different
			// JSON objects, the sort order, and the sort order lookups
			if (lpseToken instanceof SortLpseToken) {
				SortLpseToken sortLpseToken = (SortLpseToken) lpseToken;

				String solrFacetField = sortLpseToken.getSolrFacetField();
				String panlNameFromSolrFieldName = collectionProperties.getPanlNameFromSolrFieldName(solrFacetField);
				if (null != solrFacetField) {
					removeObject.put(JSON_KEY_FACET_NAME, solrFacetField);
					removeObject.put(JSON_KEY_NAME, panlNameFromSolrFieldName);
					removeObject.put(JSON_KEY_IS_DESCENDING, sortLpseToken.getSortOrderUriKey()
					                                                      .equals(SortLpseToken.SORT_ORDER_URI_KEY_DESCENDING));
					removeObject.put(JSON_KEY_ENCODED, URLEncoder.encode(panlNameFromSolrFieldName, StandardCharsets.UTF_8));
					removeObject.put(JSON_KEY_INVERSE_URI, getSortReplaceURI(sortLpseToken, uriComponents, lpseComponents));
					activeSortObject.put(solrFacetField, true);
				} else {
					shouldAddObject = false;
				}
			} else if (lpseToken instanceof BooleanFacetLpseToken) {
				BooleanFacetLpseToken booleanFacetLpseToken = (BooleanFacetLpseToken) lpseToken;
				removeObject.put(JSON_KEY_INVERSE_URI,
				                 getBooleanReplaceURI(booleanFacetLpseToken, uriComponents, lpseComponents));
				removeObject.put(JSON_KEY_FACET_NAME, collectionProperties.getSolrFieldNameFromLpseCode(lpseCode));
				removeObject.put(JSON_KEY_NAME, collectionProperties.getPanlNameFromPanlCode(lpseCode));
				removeObject.put(JSON_KEY_ENCODED, lpseField.getEncodedPanlValue(lpseToken));

			} else {
				removeObject.put(JSON_KEY_FACET_NAME, collectionProperties.getSolrFieldNameFromLpseCode(lpseCode));
				removeObject.put(JSON_KEY_NAME, collectionProperties.getPanlNameFromPanlCode(lpseCode));
				removeObject.put(JSON_KEY_ENCODED, lpseField.getEncodedPanlValue(lpseToken));
			}


			if (shouldAddObject && lpseToken.getCanHaveMultiple()) {
				jsonArray.put(removeObject);
			}

			if(collectionProperties.getIsOrSeparatorFacetField(lpseToken.getLpseCode())) {
				if(!foundOrSeparator.contains(lpseField.getLpseCode())) {
					lpseSkipNumber++;
				}
				foundOrSeparator.add(lpseField.getLpseCode());
			} else {
				lpseSkipNumber++;
			}
			skipNumber++;

			if (!activeSortObject.isEmpty()) {
				jsonObject.put(JSON_KEY_SORT_FIELDS, activeSortObject);
			}

			if (lpseToken.getCanHaveMultiple()) {
				jsonObject.put(tokenType, jsonArray);
			} else {
				jsonObject.put(tokenType, removeObject);
			}
		}
		return (jsonObject);
	}

	private String getRemoveURIFromPath(int skipNumber, List<LpseToken> lpseTokens, CollectionProperties collectionProperties) {
		Set<String> foundOrSeparator = new HashSet<>();
		Set<String> startedOrSeparator = new HashSet<>();

		StringBuilder uri = new StringBuilder();
		StringBuilder lpse = new StringBuilder();
		for(int i = 0; i < lpseTokens.size(); i++) {
			LpseToken lpseToken = lpseTokens.get(i);
			String lpseCode = lpseToken.getLpseCode();
			if(i != skipNumber) {
				if(collectionProperties.getIsOrSeparatorFacetField(lpseCode)) {
					if(!foundOrSeparator.contains(lpseCode)) {
						lpse.append(lpseCode);
					}
					foundOrSeparator.add(lpseCode);
				} else {
					lpse.append(lpseCode);
				}
			}

			// now for the uri component
			if(i != skipNumber) {
				if(collectionProperties.getIsOrSeparatorFacetField(lpseCode)) {
					BaseField lpseField = collectionProperties.getLpseField(lpseToken.getLpseCode());
					if(!startedOrSeparator.contains(lpseCode)) {
						uri.append(lpseField.getEncodedPanlValue(lpseToken))
						   .append("/");

					}
					startedOrSeparator.add(lpseCode);
				} else {
					uri.append(lpseToken.getValue())
						.append("/");
				}
			}
		}
		return returnValidURIPath(uri, lpse);
	}

		private String getRemoveURIFromPath(int skipNumber, int lpseSkipNumber, List<String> uriComponents, List<String> lpseComponents) {
		StringBuilder uri = new StringBuilder();
		StringBuilder lpse = new StringBuilder();
		for (int i = 0; i < uriComponents.size(); i++) {
			if (i != skipNumber) {
				uri.append(uriComponents.get(i));
			}
		}

		for (int i = 0; i < lpseComponents.size(); i++) {
			if (i != lpseSkipNumber) {
				lpse.append(lpseComponents.get(i));
			}
		}

		return returnValidURIPath(uri, lpse);
	}

	private String getSortReplaceURI(SortLpseToken sortLpseToken, List<String> uriComponents,
	                                 List<String> lpseComponents) {
		String sortLpseUriCode =
			sortLpseToken.getLpseCode() +
				sortLpseToken.getLpseSortCode() +
				sortLpseToken.getSortOrderUriKey();
		String inverseSortUriCode =
			sortLpseToken.getLpseCode() +
				sortLpseToken.getLpseSortCode() +
				sortLpseToken.getInverseSortOrderUriKey();

		StringBuilder uri = new StringBuilder();
		StringBuilder lpse = new StringBuilder();

		boolean found = false;
		for (int i = 0; i < uriComponents.size(); i++) {
			if (!found && sortLpseUriCode.equals(lpseComponents.get(i))) {
				found = true;
				lpse.append(inverseSortUriCode);
			} else {
				lpse.append(lpseComponents.get(i));
			}
			uri.append(uriComponents.get(i));
		}

		return returnValidURIPath(uri, lpse);
	}

	private String getBooleanReplaceURI(BooleanFacetLpseToken booleanFacetLpseToken, List<String> uriComponents,
	                                    List<String> lpseComponents) {
		String booleanLpseCode = booleanFacetLpseToken.getLpseCode();
		String inverseBooleanValue = booleanFacetLpseToken.getInverseBooleanValue(booleanFacetLpseToken);

		StringBuilder uri = new StringBuilder();
		StringBuilder lpse = new StringBuilder();

		boolean found = false;
		for (int i = 0; i < uriComponents.size(); i++) {
			if (!found && booleanLpseCode.equals(lpseComponents.get(i))) {
				found = true;
				lpse.append(booleanFacetLpseToken.getLpseCode());
				uri.append(inverseBooleanValue)
				   .append("/");
			} else {
				lpse.append(lpseComponents.get(i));
				uri.append(uriComponents.get(i));
				if (uri.length() > 0 && uri.charAt(uri.length() - 1) != '/') {
					uri.append("/");
				}
			}
		}

		return returnValidURIPath(uri, lpse);
	}

	/**
	 * <p>Return a valid URI path, in effect this will test to see whether there
	 * is a uri part and a LPSE path.  If there isn't then it will return a single forward slash '<code>/</code>'</p>
	 *
	 * @param uri The URI to test
	 * @param lpse The LPSE code to test
	 *
	 * @return The valid encoded path
	 */
	private static String returnValidURIPath(StringBuilder uri, StringBuilder lpse) {
		String test = "/" + uri + lpse + "/";

		if (test.equals("//")) {
			return ("/");
		} else {
			return test;
		}
	}
}
