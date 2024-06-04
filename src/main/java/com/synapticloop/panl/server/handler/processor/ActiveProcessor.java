package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.properties.field.BaseField;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ActiveProcessor extends Processor {
	public ActiveProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	@Override public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, Object... params) {
		JSONObject jsonObject = new JSONObject();

		// Get all the LPSE tokens
		List<LpseToken> lpseTokens = new ArrayList<>();
		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			lpseTokens.addAll(panlTokenMap.getOrDefault(lpseField.getPanlLpseCode(), new ArrayList<>()));
		}


		// go through each of the tokens and generate the removal URL

		List<String> uriComponents = new ArrayList<>();
		List<String> lpseComponents = new ArrayList<>();

		for (LpseToken lpseToken : lpseTokens) {
			BaseField lpseField = collectionProperties.getLpseField(lpseToken.getLpseCode());
			if(null != lpseField) {
				lpseComponents.add(lpseField.getLpseCode(lpseToken, collectionProperties));
				uriComponents.add(lpseField.getURIPath(lpseToken, collectionProperties));
			}
		}

		int i = 0;
		for (LpseToken lpseToken : lpseTokens) {
			String tokenType = lpseToken.getType();
			JSONArray jsonArray = jsonObject.optJSONArray(tokenType, new JSONArray());

			JSONObject removeObject = new JSONObject();
			removeObject.put("value", lpseToken.getValue());
			removeObject.put("uri", getRemoveURIFromPath(i, uriComponents, lpseComponents));

			String panlLpseCode = lpseToken.getLpseCode();
			removeObject.put("panl_code", panlLpseCode);
			removeObject.put("facet_name", collectionProperties.getSolrFieldNameFromPanlLpseCode(panlLpseCode));
			removeObject.put("name", collectionProperties.getPanlNameFromPanlCode(panlLpseCode));
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
