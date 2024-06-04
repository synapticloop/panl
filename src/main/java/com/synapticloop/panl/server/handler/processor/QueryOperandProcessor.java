package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.properties.field.BaseField;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import org.json.JSONObject;
import java.util.Map;
import java.util.List;

public class QueryOperandProcessor extends Processor {

	public QueryOperandProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, Object... params) {
		String before = "";
		String panlLpseCodeQueryOperand = collectionProperties.getPanlParamQueryOperand();

		JSONObject jsonObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder("/");
		StringBuilder lpseCode = new StringBuilder();

		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			if(!panlLpseCodeQueryOperand.equals(lpseField.getPanlLpseCode())) {
				lpseUri.append(lpseField.getResetUriPath(panlTokenMap, collectionProperties));
				lpseCode.append(lpseField.getResetLpseCode(panlTokenMap, collectionProperties));
			} else {
				before = lpseCode.toString();
				lpseCode.setLength(0);
			}
		}

		lpseCode.append("/");

		// This is the default sorting order (by relevance)
		String finalBefore = lpseUri + before + collectionProperties.getPanlParamQueryOperand();

		jsonObject.put("OR", finalBefore + "-" + lpseCode);
		jsonObject.put("AND", finalBefore + "+" + lpseCode);

		return (jsonObject);
	}
}
