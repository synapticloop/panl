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
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.JSONObject;

import java.util.*;

public class FieldsProcessor extends Processor {
	public FieldsProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	@Override public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, QueryResponse queryResponse) {
		return(collectionProperties.getSolrFieldToPanlNameLookup());
	}

	public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, String fieldSet) {
		JSONObject solrFieldToPanlNameLookup = collectionProperties.getSolrFieldToPanlNameLookup();

		JSONObject returnObject = new JSONObject();
		for (String fieldName : collectionProperties.getResultFieldsForFieldSet(fieldSet)) {
			if(null != solrFieldToPanlNameLookup.opt(fieldName)) {
				returnObject.put(fieldName, solrFieldToPanlNameLookup.get(fieldName));
			}
		}

		return(returnObject);
	}
}
