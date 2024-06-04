package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
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
	 * @param panlTokenMap
	 *
	 * @return The JSON object with the keys and relevant URI paths
	 */
	public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, Object... params) {
		String before = "";
		String panlLpseCode = collectionProperties.getPanlParamSort();

		// Run through the sorting order
		JSONObject jsonObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder("/");
		StringBuilder lpse = new StringBuilder();

		for (String lpseOrder : collectionProperties.getLpseOrder()) {
			// because the sort order does not have any URI path component, we can
			// just add the URI components to it safely
			if (!panlLpseCode.equals(lpseOrder)) {
				// keep on adding things
				if (panlTokenMap.containsKey(lpseOrder)) {
					for (LpseToken token : panlTokenMap.get(lpseOrder)) {
						lpseUri.append(token.getResetUriPathComponent());
						// however we do not want to add the lpse component
						lpse.append(token.getLpseComponent());
					}
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
		relevanceSort.put("name", "Relevance");
		relevanceSort.put("replace_desc", finalBefore + "-" + lpse);
		jsonObject.put("relevance", relevanceSort);

		// These are the defined sort fields
		JSONObject sortFieldsObject = new JSONObject();

		for (String sortFieldLpseCode : collectionProperties.getSortFieldLpseCodes()) {
			String sortFieldName = collectionProperties.getSolrFieldNameFromPanlLpseCode(sortFieldLpseCode);
			if (null != sortFieldName) {
				JSONObject sortObject = new JSONObject();
				sortObject.put("name", collectionProperties.getPanlNameFromPanlCode(sortFieldLpseCode));
				sortObject.put("replace_desc", finalBefore + sortFieldLpseCode + "-" + lpse);
				sortObject.put("replace_asc", finalBefore + sortFieldLpseCode + "+" + lpse);
				sortFieldsObject.put(sortFieldName, sortObject);
			}
		}

		jsonObject.put("fields", sortFieldsObject);

		return (jsonObject);
	}
}
