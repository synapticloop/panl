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

import java.util.List;
import java.util.Map;

/**
 * <p>Base processor that all other processors inherit from.  Holds JSON keys,
 * and defines base methods that can be overridden.</p>
 *
 * @author synapticloop
 */
public abstract class Processor {

	/**
	 * <p>The collection properties for this request.</p>
	 */
	protected final CollectionProperties collectionProperties;

	/**
	 * <p>Base instantiation of parent class.</p>
	 *
	 * @param collectionProperties The collection properties to use
	 */
	protected Processor(CollectionProperties collectionProperties) {
		this.collectionProperties = collectionProperties;
	}

	/**
	 * <p>Process the panlTokenMap to a JSONObject.</p>
	 *
	 * @param panlTokenMap The map of LPSE codes to the list of tokens
	 * @param queryResponse The Solr query response
	 *
	 * @return The JSON object with the keys set.
	 */
	public abstract JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, QueryResponse queryResponse);

	/**
	 * <p>Process the panl token map to a JSON object.  This will call the
	 * <code>processToObject(panlTokenMap, queryResponse)</code> method with a
	 * null queryResponse.</p>
	 *
	 * @param panlTokenMap The panl token map to interrogate
	 *
	 * @return The JSON object for return to the user
	 *
	 * @see #processToObject(Map, QueryResponse)
	 */
	public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap) {
		return (processToObject(panlTokenMap, null));
	}

	/**
	 * <p>Process the panlTokenMap to a string, will return an empty string
	 * unless overridden.</p>
	 *
	 * @param panlTokenMap The panl token map to interrogate
	 *
	 * @return Always returns an empty string unless overridden
	 */
	public String processToString(Map<String, List<LpseToken>> panlTokenMap) {
		return ("");
	}
}
