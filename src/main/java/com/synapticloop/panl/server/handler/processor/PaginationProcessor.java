package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.properties.field.BaseField;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.tokeniser.token.NumRowsLpseToken;
import com.synapticloop.panl.server.tokeniser.token.PageLpseToken;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PaginationProcessor extends Processor {

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
		paginationObject.put("num_per_page", numPerPage);
		paginationObject.put("page_num", pageNumber);
		long numPages = numFound / numPerPage;
		if (numFound % numPerPage != 0) {
			numPages++;
		}
		paginationObject.put("num_pages", numPages);

		StringBuilder uriPath = new StringBuilder("/");
		StringBuilder lpseCode = new StringBuilder();

		JSONObject pageUris = new JSONObject();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			//
			if (!baseField.getPanlLpseCode().equals(panlParamPageLpseCode)) {
				uriPath.append(baseField.getURIPath(panlTokenMap, collectionProperties));
				lpseCode.append(baseField.getLpseCode(panlTokenMap, collectionProperties));
			} else {
				pageUris.put("before", uriPath + baseField.getPanlPrefix());
				// clear the sting builder
				lpseCode.append(baseField.getPanlLpseCode());
				uriPath.setLength(0);
			}
		}

		BaseField panlPageNumField = collectionProperties.getLpseField(panlParamPageLpseCode);

		String afterValue = panlPageNumField.getPanlSuffix() + "/" + uriPath + lpseCode + "/";
		pageUris.put("after", afterValue);

		if (pageNumber < numPages) {
			pageUris.put("next", pageUris.getString("before") + (pageNumber + 1) + pageUris.getString("after"));
		}

		if (pageNumber > 1) {
			pageUris.put("previous", pageUris.getString("before") + (pageNumber - 1) + pageUris.getString("after"));
		}

		paginationObject.put("page_uris", pageUris);

		paginationObject.put("num_per_page_uris",
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
			if (!baseField.getPanlLpseCode().equals(replaceLpseCode)) {
				uriPath.append(baseField.getResetUriPath(panlTokenMap, collectionProperties));
				lpseCode.append(baseField.getResetLpseCode(panlTokenMap, collectionProperties));
			} else {
				pageUris.put("before", uriPath + baseField.getPanlPrefix());
				// clear the sting builder
				lpseCode.append(baseField.getPanlLpseCode());
				uriPath.setLength(0);
			}
		}

		BaseField baseField = collectionProperties.getLpseField(replaceLpseCode);

		pageUris.put("after", baseField.getPanlSuffix() + "/" + uriPath + lpseCode + "/");

		return (pageUris);
	}
}
