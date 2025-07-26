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
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.NumRowsLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.PageNumLpseToken;
import com.synapticloop.panl.util.Constants;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PaginationProcessor extends Processor {

	public PaginationProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, QueryResponse queryResponse) {
		SolrDocumentList solrDocuments =
				(SolrDocumentList) queryResponse.getResponse().get(Constants.Json.Solr.RESPONSE);
		long numFound = solrDocuments.getNumFound();

		// TODO - maybe check if the results would be out of bounds....
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
			int pageNum = ((PageNumLpseToken) lpseTokens.get(0)).getPageNum();
			if (pageNum > 0) {
				pageNumber = pageNum;
			}
		}

		JSONObject paginationObject = new JSONObject();
		paginationObject.put(Constants.Json.Panl.NUM_RESULTS, numFound);
		paginationObject.put(Constants.Json.Panl.NUM_RESULTS_EXACT, solrDocuments.getNumFoundExact());
		paginationObject.put(Constants.Json.Panl.NUM_PER_PAGE, numPerPage);
		paginationObject.put(Constants.Json.Panl.PAGE_NUM, pageNumber);
		long numPages = numFound / numPerPage;
		if (numFound % numPerPage != 0) {
			numPages++;
		}
		paginationObject.put(Constants.Json.Panl.NUM_PAGES, numPages);

		StringBuilder uriPath = new StringBuilder(Constants.FORWARD_SLASH);
		StringBuilder lpseCode = new StringBuilder();

		JSONObject pageUris = new JSONObject();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			if (!baseField.getLpseCode().equals(panlParamPageLpseCode)) {
				uriPath.append(baseField.getURIPath(panlTokenMap, collectionProperties));
				lpseCode.append(baseField.getLpseCode(panlTokenMap, collectionProperties));
			} else {
				pageUris.put(Constants.Json.Panl.BEFORE, uriPath + baseField.getValuePrefix());
				// clear the sting builder
				lpseCode.append(baseField.getLpseCode());
				uriPath.setLength(0);
			}
		}

		BaseField panlPageNumField = collectionProperties.getLpseField(panlParamPageLpseCode);

		String afterValue = panlPageNumField.getValueSuffix() + Constants.FORWARD_SLASH + uriPath + lpseCode + Constants.FORWARD_SLASH;
		pageUris.put(Constants.Json.Panl.AFTER, afterValue);

		if (pageNumber < numPages) {
			pageUris.put(Constants.Json.Panl.NEXT, pageUris.getString(Constants.Json.Panl.BEFORE) + (pageNumber + 1) + pageUris.getString(Constants.Json.Panl.AFTER));
		}

		if (pageNumber > 1) {
			pageUris.put(Constants.Json.Panl.PREVIOUS, pageUris.getString(Constants.Json.Panl.BEFORE) + (pageNumber - 1) + pageUris.getString(Constants.Json.Panl.AFTER));
		}

		paginationObject.put(Constants.Json.Panl.PAGE_URIS, pageUris);

		paginationObject.put(Constants.Json.Panl.NUM_PER_PAGE_URIS,
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
		StringBuilder uriPath = new StringBuilder(Constants.FORWARD_SLASH);
		StringBuilder lpseCode = new StringBuilder();

		JSONObject pageUris = new JSONObject();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			if (!baseField.getLpseCode().equals(replaceLpseCode)) {
				uriPath.append(baseField.getResetUriPath(panlTokenMap, collectionProperties));
				lpseCode.append(baseField.getResetLpseCode(panlTokenMap, collectionProperties));
			} else {
				pageUris.put(Constants.Json.Panl.BEFORE, uriPath + baseField.getValuePrefix());
				// clear the sting builder
				lpseCode.append(baseField.getLpseCode());
				uriPath.setLength(0);
			}
		}

		BaseField baseField = collectionProperties.getLpseField(replaceLpseCode);

		pageUris.put(Constants.Json.Panl.AFTER,
				baseField.getValueSuffix() +
						Constants.FORWARD_SLASH +
						uriPath +
						lpseCode +
						Constants.FORWARD_SLASH);

		return (pageUris);
	}
}
