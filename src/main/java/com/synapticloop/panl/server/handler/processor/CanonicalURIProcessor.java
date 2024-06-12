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

import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * <p>Get the canonical URI Path for the query.  This will always return more
 * than an empty path as it includes the following default LPSE path
 * components:</p>
 *
 * <ul>
 *   <li>The Sort order - sorted by relevance by default and will display
 *   nothing.</li>
 *   <li>The page number - page 1 by default</li>
 *   <li>The number of results per page - this is defined, or 10 by default</li>
 *   <li>The query operand - this is defined, or OR by default</li>
 * </ul>
 *
 * <p><strong>NOTE:</strong> The pass-through parameter, if defined, will
 * __NEVER__ be included.</p>
 */
public class CanonicalURIProcessor extends Processor {

	public CanonicalURIProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	@Override public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, QueryResponse queryResponse) {
		return(new JSONObject());
	}

	@Override public String processToString(Map<String, List<LpseToken>> panlTokenMap) {
		StringBuilder canonicalUri = new StringBuilder("/");
		StringBuilder canonicalLpse = new StringBuilder();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			canonicalUri.append(baseField.getCanonicalUriPath(panlTokenMap, collectionProperties));
			canonicalLpse.append(baseField.getCanonicalLpseCode(panlTokenMap, collectionProperties));
		}

		return (canonicalUri.toString() + canonicalLpse + "/");
	}
}
