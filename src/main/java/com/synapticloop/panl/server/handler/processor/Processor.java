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
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * <p>Base processor that all other processors inherit from.</p>
 *
 * @author synapticloop
 */
public abstract class Processor {
	//
	// static strings for json keys
	//
	public static final String JSON_KEY_ADD_URI_ASC = "add_uri_asc";
	public static final String JSON_KEY_ADD_URI_DESC = "add_uri_desc";
	public static final String JSON_KEY_AFTER = "after";
	public static final String JSON_KEY_AFTER_MAX_VALUE = "after_max_value";
	public static final String JSON_KEY_AND = "AND";
	public static final String JSON_KEY_BEFORE = "before";
	public static final String JSON_KEY_BEFORE_MIN_VALUE = "before_min_value";
	public static final String JSON_KEY_COUNT = "count";
	public static final String JSON_KEY_DURING = "during";
	public static final String JSON_KEY_ENCODED = "encoded";
	public static final String JSON_KEY_FACETS = "facets";
	public static final String JSON_KEY_FACET_NAME = "facet_name";
	public static final String JSON_KEY_FIELDS = "fields";
	public static final String JSON_KEY_INVERSE_URI = "inverse_uri";
	public static final String JSON_KEY_IS_DESCENDING = "is_descending";
	public static final String JSON_KEY_IS_OR_FACET = "is_or_facet";
	public static final String JSON_KEY_IS_RANGE_FACET = "is_range_facet";
	public static final String JSON_KEY_MAX = "max";
	public static final String JSON_KEY_MIN = "min";
	public static final String JSON_KEY_NAME = "name";
	public static final String JSON_KEY_NEXT = "next";
	public static final String JSON_KEY_NUM_PAGES = "num_pages";
	public static final String JSON_KEY_NUM_PER_PAGE = "num_per_page";
	public static final String JSON_KEY_NUM_PER_PAGE_URIS = "num_per_page_uris";
	public static final String JSON_KEY_OR = "OR";
	public static final String JSON_KEY_PAGE_NUM = "page_num";
	public static final String JSON_KEY_PAGE_URIS = "page_uris";
	public static final String JSON_KEY_PANL_CODE = "panl_code";
	public static final String JSON_KEY_PREFIX = "prefix";
	public static final String JSON_KEY_PREVIOUS = "previous";
	public static final String JSON_KEY_RANGE_FACETS = "range_facets";
	public static final String JSON_KEY_RANGE_MAX_VALUE = "range_max_value";
	public static final String JSON_KEY_RANGE_MIN_VALUE = "range_min_value";
	public static final String JSON_KEY_REMOVE_URI = "remove_uri";
	public static final String JSON_KEY_SET_URI_ASC = "set_uri_asc";
	public static final String JSON_KEY_SET_URI_DESC = "set_uri_desc";
	public static final String JSON_KEY_SOLR_JSON_KEY_RESPONSE = "response";
	public static final String JSON_KEY_SUFFIX = "suffix";
	public static final String JSON_KEY_URIS = "uris";
	public static final String JSON_KEY_VALUE = "value";
	public static final String JSON_KEY_VALUES = "values";
	public static final String JSON_KEY_VALUE_TO = "value_to";

	//
	// static strings for json values
	//
	public static final String JSON_VALUE_NO_INFIX_REPLACEMENT = "~";

	//
	// static strings for the forward slash
	//
	public static final String FORWARD_SLASH = "/";

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
	 * @return Always returns an empty string
	 */
	public String processToString(Map<String, List<LpseToken>> panlTokenMap) {
		return ("");
	}

}