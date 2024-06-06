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
import com.synapticloop.panl.server.handler.tokeniser.token.NumRowsLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.PageLpseToken;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PaginationProcessor extends Processor {

	public static final String JSON_KEY_NUM_PER_PAGE = "num_per_page";
	public static final String JSON_KEY_PAGE_NUM = "page_num";
	public static final String JSON_KEY_NUM_PAGES = "num_pages";
	public static final String JSON_KEY_BEFORE = "before";
	public static final String JSON_KEY_AFTER = "after";
	public static final String JSON_KEY_NEXT = "next";
	public static final String JSON_KEY_PREVIOUS = "previous";
	public static final String JSON_KEY_PAGE_URIS = "page_uris";
	public static final String JSON_KEY_NUM_PER_PAGE_URIS = "num_per_page_uris";

	public PaginationProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, Object... params) {

		int numPerPage = collectionProperties.getNumResultsPerPage();
		// If it is set in the URI, get the updated value
		List<LpseToken> lpseTokens = panlTokenMap.getOrDefault(collectionProperties.getPanlParamNumRows(), new ArrayList<>());
		if (!lpseTokens.isEmpty()) {
			int numRows = ((NumRowsLpseToken) lpseTokens.get(0)).getNumRows();
			if (numRows > 0) {
				numPerPage = numRows;
			}
		}

		int pageNumber = 1;
		String panlParamPageLpseCode = collectionProperties.getPanlParamPage();
		lpseTokens = panlTokenMap.getOrDefault(panlParamPageLpseCode, new ArrayList<>());
		if (!lpseTokens.isEmpty()) {
			int pageNum = ((PageLpseToken) lpseTokens.get(0)).getPageNum();
			if (pageNum > 0) {
				pageNumber = pageNum;
			}
		}

		long numFound = (Long)params[0];
		JSONObject paginationObject = new JSONObject();
		paginationObject.put(JSON_KEY_NUM_PER_PAGE, numPerPage);
		paginationObject.put(JSON_KEY_PAGE_NUM, pageNumber);
		long numPages = numFound / numPerPage;
		if (numFound % numPerPage != 0) {
			numPages++;
		}
		paginationObject.put(JSON_KEY_NUM_PAGES, numPages);

		StringBuilder uriPath = new StringBuilder("/");
		StringBuilder lpseCode = new StringBuilder();

		JSONObject pageUris = new JSONObject();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			//
			if (!baseField.getLpseCode().equals(panlParamPageLpseCode)) {
				uriPath.append(baseField.getURIPath(panlTokenMap, collectionProperties));
				lpseCode.append(baseField.getLpseCode(panlTokenMap, collectionProperties));
			} else {
				pageUris.put(JSON_KEY_BEFORE, uriPath + baseField.getPanlPrefix());
				// clear the sting builder
				lpseCode.append(baseField.getLpseCode());
				uriPath.setLength(0);
			}
		}

		BaseField panlPageNumField = collectionProperties.getLpseField(panlParamPageLpseCode);

		String afterValue = panlPageNumField.getPanlSuffix() + "/" + uriPath + lpseCode + "/";
		pageUris.put(JSON_KEY_AFTER, afterValue);

		if (pageNumber < numPages) {
			pageUris.put(JSON_KEY_NEXT, pageUris.getString(JSON_KEY_BEFORE) + (pageNumber + 1) + pageUris.getString(JSON_KEY_AFTER));
		}

		if (pageNumber > 1) {
			pageUris.put(JSON_KEY_PREVIOUS, pageUris.getString(JSON_KEY_BEFORE) + (pageNumber - 1) + pageUris.getString(JSON_KEY_AFTER));
		}

		paginationObject.put(JSON_KEY_PAGE_URIS, pageUris);

		paginationObject.put(JSON_KEY_NUM_PER_PAGE_URIS,
				getReplacementResetURIObject(
						collectionProperties.getPanlParamNumRows(),
						panlTokenMap,
						collectionProperties));

		return (paginationObject);
	}

	/**
	 * <p>Get the URI JSONObject with a replacement of a code, resetting all
	 * LPSE codes that are needed.</p>
	 *
	 * <p>This returns a JSON object with two keys:</p>
	 *
	 * <ul>
	 *   <li><code>before</code> The URI path that goes before the value.</li>
	 *   <li><code>after</code> The URI path that goes after the value.</li>
	 * </ul>
	 *
	 * @param replaceLpseCode The LPSE code to be replaced
	 * @param panlTokenMap The tokens parsed from the inbound request
	 *
	 * @return The JSON object
	 */
	private static JSONObject getReplacementResetURIObject(String replaceLpseCode, Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder uriPath = new StringBuilder("/");
		StringBuilder lpseCode = new StringBuilder();

		JSONObject pageUris = new JSONObject();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			if (!baseField.getLpseCode().equals(replaceLpseCode)) {
				uriPath.append(baseField.getResetUriPath(panlTokenMap, collectionProperties));
				lpseCode.append(baseField.getResetLpseCode(panlTokenMap, collectionProperties));
			} else {
				pageUris.put(JSON_KEY_BEFORE, uriPath + baseField.getPanlPrefix());
				// clear the sting builder
				lpseCode.append(baseField.getLpseCode());
				uriPath.setLength(0);
			}
		}

		BaseField baseField = collectionProperties.getLpseField(replaceLpseCode);

		pageUris.put(JSON_KEY_AFTER, baseField.getPanlSuffix() + "/" + uriPath + lpseCode + "/");

		return (pageUris);
	}
}
