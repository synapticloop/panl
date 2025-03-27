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
import com.synapticloop.panl.util.Constants;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.JSONObject;
import java.util.Map;
import java.util.List;

public class QueryOperandProcessor extends Processor {

	public QueryOperandProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, QueryResponse queryResponse) {
		String before = "";
		String panlParamQueryOperand = collectionProperties.getPanlParamQueryOperand();

		JSONObject jsonObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder(Constants.FORWARD_SLASH);
		StringBuilder lpseCode = new StringBuilder();

		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			if(!panlParamQueryOperand.equals(lpseField.getLpseCode())) {
				lpseUri.append(lpseField.getResetUriPath(panlTokenMap, collectionProperties));
				lpseCode.append(lpseField.getResetLpseCode(panlTokenMap, collectionProperties));
			} else {
				before = lpseCode.toString();
				lpseCode.setLength(0);
			}
		}

		lpseCode.append(Constants.FORWARD_SLASH);

		String finalBefore = lpseUri + before + collectionProperties.getPanlParamQueryOperand();

		jsonObject.put(Constants.Json.Panl.OR, finalBefore + "-" + lpseCode);
		jsonObject.put(Constants.Json.Panl.AND, finalBefore + "+" + lpseCode);

		return (jsonObject);
	}
}
