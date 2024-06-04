package com.synapticloop.panl.server.handler.processor;

/*
 * Copyright (c) 2008-2024 synapticloop.
 *
 * https://github.com/synapticloop/panl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 *  IN THE SOFTWARE.
 */

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
