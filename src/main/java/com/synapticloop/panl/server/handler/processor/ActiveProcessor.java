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

import com.synapticloop.panl.server.handler.fielderiser.field.facet.PanlOrFacetField;
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
 * <p>The active processor adds all current active filters that are passed through the lpse PATH the returned panl
 * JSON object.  Additionally, this will generate the remove links for this particular facet.</p>
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

		for (LpseToken lpseToken : lpseTokens) {
			BaseField lpseField = collectionProperties.getLpseField(lpseToken.getLpseCode());
			if (null != lpseField && lpseToken.getIsValid()) {
				lpseComponents.add(lpseField.getResetLpseCode(lpseToken, collectionProperties));
				//				lpseComponents.add(lpseField.getResetLpseCode(panlTokenMap, collectionProperties));

				uriComponents.add(lpseField.getResetUriPath(lpseToken, collectionProperties));
			}
		}

		JSONObject activeSortObject = new JSONObject();
		int skipNumber = 0;
		for (LpseToken lpseToken : lpseTokens) {
			String tokenType = lpseToken.getType();
			String lpseCode = lpseToken.getLpseCode();
			BaseField lpseField = collectionProperties.getLpseField(lpseCode);

			boolean shouldAddObject = true;

			JSONArray jsonArray = jsonObject.optJSONArray(tokenType, new JSONArray());
			JSONObject removeObject = new JSONObject();


			removeObject.put(JSON_KEY_VALUE, lpseToken.getValue());

			removeObject.put(JSON_KEY_REMOVE_URI, getRemoveURIFromPath(
					skipNumber,
					lpseTokens,
					lpseComponents,
					collectionProperties));

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

					removeObject.put(JSON_KEY_IS_DESCENDING,
							sortLpseToken.getSortOrderUriKey().equals(SortLpseToken.SORT_ORDER_URI_KEY_DESCENDING));

					removeObject.put(JSON_KEY_ENCODED,
							URLEncoder.encode(panlNameFromSolrFieldName, StandardCharsets.UTF_8));

					removeObject.put(JSON_KEY_INVERSE_URI,
							getSortInverseURI(
									sortLpseToken,
									lpseTokens,
									lpseComponents,
									collectionProperties));


					activeSortObject.put(solrFacetField, true);
				} else {
					shouldAddObject = false;
				}
			} else if (lpseToken instanceof BooleanFacetLpseToken) {
				BooleanFacetLpseToken booleanFacetLpseToken = (BooleanFacetLpseToken) lpseToken;

				removeObject.put(JSON_KEY_INVERSE_URI,
						getBooleanInverseURI(booleanFacetLpseToken, lpseTokens, lpseComponents, collectionProperties));


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

	/**
	 * <p>Get the removal URI path for the current lpseTokens</p>
	 *
	 * @param skipNumber The LPSE token to skip (starting at index 0
	 * @param lpseTokens The list of LPSE tokens
	 * @param collectionProperties The collection properties
	 *
	 * @return The String URL that will remove this particular facet
	 */
	private String getRemoveURIFromPath(
			int skipNumber,
			List<LpseToken> lpseTokens,
			List<String> lpseComponents,
			CollectionProperties collectionProperties) {

		// if we are currently looking at LPSE code which is an OR separator
		boolean hasMultivalueSeparator = false;
		String previousLpseCode = "";
		String previousValueSuffix = "";

		StringBuilder uri = new StringBuilder();
		StringBuilder lpse = new StringBuilder();

		Set<String> lpseComponentsAdded = new HashSet<>();

		for (int i = 0; i < lpseTokens.size(); i++) {
			LpseToken lpseToken = lpseTokens.get(i);
			String lpseCode = lpseToken.getLpseCode();

			if (i != skipNumber) {
				BaseField lpseField = collectionProperties.getLpseField(lpseToken.getLpseCode());

				String lpseComponent = lpseComponents.get(i);
				if (lpseCode.equals(previousLpseCode)) {
					// we just carry on
					if (hasMultivalueSeparator) {
						// the previous LPSE code is an or separator, we only need to add
						// the value, with the OR SEPARATOR
						uri.append(
								URLEncoder.encode(lpseField.getValueSeparator() + lpseToken.getValue(),
										StandardCharsets.UTF_8));
						previousValueSuffix = lpseField.getValueSuffix();
					} else {
						// not currently an or separator - get the full value
						// if the previous lpse code was an or Separator, add the value
						// suffix
						if (lpseField.getHasURIComponent()) {
							uri.append(lpseField.getEncodedPanlValue(lpseToken))
							   .append("/");
						} else {
							if (!lpseComponentsAdded.contains(lpseComponent)) {
								lpse.append(lpseComponent);
							}
							lpseComponentsAdded.add(lpseComponent);
						}
					}
				} else {
					// need to check for sorting/operands... which are different
					if (lpseField.getHasURIComponent()) {
						lpse.append(lpseCode);
					} else {
						if (!lpseComponentsAdded.contains(lpseComponent)) {
							lpse.append(lpseComponent);
						}
						lpseComponentsAdded.add(lpseComponent);
					}
					// the current and previous are different
					if (hasMultivalueSeparator) {
						// the previous LPSE code is an or Separator - we don't know whether
						// this one is - we will test for it, but we shall add the value
						// suffix to it.
						uri.append(URLEncoder.encode(previousValueSuffix, StandardCharsets.UTF_8))
						   .append("/");
					}

					if (collectionProperties.getIsMultiValuedSeparatorFacetField(lpseCode)) {
						hasMultivalueSeparator = true;
						// this is the start of an OR separator
						uri.append(URLEncoder.encode(lpseField.getValuePrefix() + lpseToken.getValue(), StandardCharsets.UTF_8));
					} else {
						hasMultivalueSeparator = false;

						if (lpseField.getHasURIComponent()) {
							uri.append(lpseField.getEncodedPanlValue(lpseToken))
							   .append("/");
						} else {
							if (!lpseComponentsAdded.contains(lpseComponent)) {
								lpse.append(lpseComponent);
							}
							lpseComponentsAdded.add(lpseComponent);
						}
					}
				}

				previousLpseCode = lpseCode;
				previousValueSuffix = lpseField.getValueSuffix();
			}
		}

		// if we still are in an or separator and we have no more tokens to process
		// then we have a dangling suffix that may need to be added
		if (hasMultivalueSeparator) {
			uri.append(
					   URLEncoder.encode(previousValueSuffix, StandardCharsets.UTF_8))
			   .append("/");
		}

		return returnValidURIPath(uri, lpse);
	}

	/**
	 * <p>Generate the inverse for a BOOLEAN facet which only changes the 'true'
	 * value to 'false' and vice versa.</p>
	 *
	 * @param sortLpseToken The facet token to work on
	 * @param lpseTokens The list of LPSE tokens
	 * @param lpseComponents The LPSE components
	 * @param collectionProperties The collection properties
	 *
	 * @return The inverse URI
	 */
	private String getSortInverseURI(
			SortLpseToken sortLpseToken,
			List<LpseToken> lpseTokens,
			List<String> lpseComponents,
			CollectionProperties collectionProperties) {

		String sortLpseUriCode =
				sortLpseToken.getLpseCode() +
						sortLpseToken.getLpseSortCode() +
						sortLpseToken.getSortOrderUriKey();
		String inverseSortUriCode =
				sortLpseToken.getLpseCode() +
						sortLpseToken.getLpseSortCode() +
						sortLpseToken.getInverseSortOrderUriKey();

		// if we are currently looking at LPSE code which has a multivalue separator
		boolean hasMultivalueSeparator = false;
		String previousLpseCode = "";
		String previousValueSuffix = "";

		StringBuilder uri = new StringBuilder();
		StringBuilder lpse = new StringBuilder();
		Set<String> lpseComponentsAdded = new HashSet<>();

		for (int i = 0; i < lpseTokens.size(); i++) {
			LpseToken lpseToken = lpseTokens.get(i);
			String lpseCode = lpseToken.getLpseCode();

			BaseField lpseField = collectionProperties.getLpseField(lpseToken.getLpseCode());

			boolean found = false;
			String lpseComponent = lpseComponents.get(i);
			if (!found && sortLpseUriCode.equals(lpseComponent)) {
				if (hasMultivalueSeparator) {
					uri.append(
							   URLEncoder.encode(previousValueSuffix, StandardCharsets.UTF_8))
					   .append("/");
				}
				hasMultivalueSeparator = false;
				found = true;
				lpse.append(inverseSortUriCode);
			} else {
				// we need to go through the facets
				if (lpseCode.equals(previousLpseCode)) {
					// we just carry on
					if (hasMultivalueSeparator) {
						// the previous LPSE code has a multivalue separator, we only need
						// to add the value, with the value SEPARATOR
						uri.append(
								URLEncoder.encode(lpseField.getValueSeparator() + lpseToken.getValue(),
										StandardCharsets.UTF_8));
						previousValueSuffix = lpseField.getValueSuffix();
					} else {
						// not currently has a multivalue separator - get the full value
						// if the previous lpse code has a multivalue Separator, add the value
						// suffix
						if (lpseField.getHasURIComponent()) {
							uri.append(lpseField.getEncodedPanlValue(lpseToken))
							   .append("/");
						} else {
							if (!lpseComponentsAdded.contains(lpseComponent)) {
								lpse.append(lpseComponent);
							}
							lpseComponentsAdded.add(lpseComponent);
						}
					}
				} else {
					if (lpseField.getHasURIComponent()) {
						lpse.append(lpseCode);
					} else {
						if (!lpseComponentsAdded.contains(lpseComponent)) {
							lpse.append(lpseComponent);
						}
						lpseComponentsAdded.add(lpseComponent);
					}
					// the current and previous are different
					if (hasMultivalueSeparator) {
						// the previous LPSE code has a multivalue Separator - we don't know whether
						// this one is - we will test for it, but we shall add the value
						// suffix to it.
						uri.append(URLEncoder.encode(previousValueSuffix, StandardCharsets.UTF_8))
						   .append("/");
					}

					if (collectionProperties.getIsMultiValuedSeparatorFacetField(lpseCode)) {
						hasMultivalueSeparator = true;
						// this is the start of a multivalue separator
						uri.append(URLEncoder.encode(lpseField.getValuePrefix() + lpseToken.getValue(), StandardCharsets.UTF_8));
					} else {
						hasMultivalueSeparator = false;

						// for sort fields (and operands) and anything which doesn't have a
						// URI path - we want to skip putting in any value or forward slash

						if (lpseField.getHasURIComponent()) {
							uri.append(lpseField.getEncodedPanlValue(lpseToken))
							   .append("/");
						} else {
							if (!lpseComponentsAdded.contains(lpseComponent)) {
								lpse.append(lpseComponent);
							}
							lpseComponentsAdded.add(lpseComponent);
						}
					}
				}
			}

			previousLpseCode = lpseCode;
			previousValueSuffix = lpseField.getValueSuffix();

		}

		// if we still are in multivalue separator and we have no more tokens to process
		// then we have a dangling suffix that may need to be added
		if (hasMultivalueSeparator) {
			uri.append(
					   URLEncoder.encode(previousValueSuffix, StandardCharsets.UTF_8))
			   .append("/");
		}


		return returnValidURIPath(uri, lpse);
	}

	/**
	 * <p>Generate the inverse for a BOOLEAN facet which only changes the 'true'
	 * value to 'false' and vice versa.</p>
	 *
	 * @param booleanFacetLpseToken The facet token to work on
	 * @param lpseTokens The list of LPSE tokens
	 * @param collectionProperties The collection properties
	 *
	 * @return The inverse URI
	 */
	private String getBooleanInverseURI(
			BooleanFacetLpseToken booleanFacetLpseToken,
			List<LpseToken> lpseTokens,
			List<String> lpseComponents,
			CollectionProperties collectionProperties) {

		String booleanLpseCode = booleanFacetLpseToken.getLpseCode();
		String inverseBooleanValue = booleanFacetLpseToken.getInverseBooleanValue(booleanFacetLpseToken);

		// if we are currently looking at LPSE code which is an OR separator
		boolean hasMultivalueSeparator = false;
		String previousLpseCode = "";
		String previousValueSuffix = "";

		StringBuilder uri = new StringBuilder();
		StringBuilder lpse = new StringBuilder();
		Set<String> lpseComponentsAdded = new HashSet<>();

		for (int i = 0; i < lpseTokens.size(); i++) {
			LpseToken lpseToken = lpseTokens.get(i);
			String lpseCode = lpseToken.getLpseCode();
			String lpseComponent = lpseComponents.get(i);

			BaseField lpseField = collectionProperties.getLpseField(lpseToken.getLpseCode());

			boolean found = false;
			if (!found && booleanLpseCode.equals(lpseTokens.get(i).getLpseCode())) {
				if (hasMultivalueSeparator) {
					uri.append(
							   URLEncoder.encode(previousValueSuffix, StandardCharsets.UTF_8))
					   .append("/");
				}
				hasMultivalueSeparator = false;
				found = true;
				lpse.append(booleanFacetLpseToken.getLpseCode());
				uri.append(inverseBooleanValue)
				   .append("/");
			} else {
				// we need to go through the facets
				if (lpseCode.equals(previousLpseCode)) {
					// we just carry on
					if (hasMultivalueSeparator) {
						// the previous LPSE code is an or separator, we only need to add
						// the value, with the OR SEPARATOR
						uri.append(
								URLEncoder.encode(lpseField.getValueSeparator() + lpseToken.getValue(),
										StandardCharsets.UTF_8));
						previousValueSuffix = lpseField.getValueSuffix();
					} else {
						// not currently an or separator - get the full value
						// if the previous lpse code was an or Separator, add the value
						// suffix
						if (lpseField.getHasURIComponent()) {
							uri.append(lpseField.getEncodedPanlValue(lpseToken))
							   .append("/");
						} else {
							if(!lpseComponentsAdded.contains(lpseComponent)) {
								lpse.append(lpseComponent);
							}
							lpseComponentsAdded.add(lpseComponent);
						}
					}
				} else {
					if (lpseField.getHasURIComponent()) {
						lpse.append(lpseCode);
					} else {
						if (!lpseComponentsAdded.contains(lpseComponent)) {
							lpse.append(lpseComponent);
						}
						lpseComponentsAdded.add(lpseComponent);
					}
					// the current and previous are different
					if (hasMultivalueSeparator) {
						// the previous LPSE code is an or Separator - we don't know whether
						// this one is - we will test for it, but we shall add the value
						// suffix to it.
						uri.append(URLEncoder.encode(previousValueSuffix, StandardCharsets.UTF_8))
						   .append("/");
					}

					if (collectionProperties.getIsMultiValuedSeparatorFacetField(lpseCode)) {
						hasMultivalueSeparator = true;
						// this is the start of an OR separator
						uri.append(URLEncoder.encode(lpseField.getValuePrefix() + lpseToken.getValue(), StandardCharsets.UTF_8));
					} else {
						hasMultivalueSeparator = false;
						if (lpseField.getHasURIComponent()) {
							uri.append(lpseField.getEncodedPanlValue(lpseToken))
							   .append("/");
						}
					}
				}
			}

			previousLpseCode = lpseCode;
			previousValueSuffix = lpseField.getValueSuffix();
		}

		// if we still are in an or separator and we have no more tokens to process
		// then we have a dangling suffix that may need to be added
		if (hasMultivalueSeparator) {
			uri.append(
					URLEncoder.encode(previousValueSuffix, StandardCharsets.UTF_8))
			   .append("/");
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
