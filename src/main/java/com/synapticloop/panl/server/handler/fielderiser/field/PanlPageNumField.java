package com.synapticloop.panl.server.handler.fielderiser.field;

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

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.PageLpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PanlPageNumField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlPageNumField.class);

	public PanlPageNumField(String lpseCode, String propertyKey, Properties properties, String collectionName) throws PanlServerException {
		super(lpseCode, properties, propertyKey, collectionName);

		populateParamSuffixAndPrefix();

		logDetails();
	}

	/**
	 * <p>Get the canonical URI path for the page number.  This __ALWAYS__
	 * returns a URI path. The path will be encoded with a prefix and/or
	 * suffix if they have been set.</p>
	 *
	 * @param panlTokenMap The token map with all fields and a list of their
	 * 		values
	 * @param collectionProperties The collection properties
	 *
	 * @return The URI path, never an empty string
	 */
	@Override
	public String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder();
		if (panlTokenMap.containsKey(lpseCode) && !panlTokenMap.get(lpseCode).isEmpty()) {

			// get the first token out of the list - there can be only one, so we
			// choose the first one.

			PageLpseToken pageLpseToken = (PageLpseToken) panlTokenMap.get(lpseCode).get(0);
			sb.append(getEncodedPanlValue(Integer.toString(pageLpseToken.getPageNum())));
		} else {
			sb.append(getEncodedPanlValue("1"));
		}

		sb.append("/");
		return (sb.toString());
	}

	@Override
	public String getCanonicalLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return (lpseCode);
	}

	/**
	 * <p>When resetting the page number, it is always blank, whether or not it
	 * exists in the panl token map.  This is because when we add/remove etc, we
	 * go back to the first page always</p>
	 *
	 * @param panlTokenMap
	 * @param collectionProperties
	 * @return
	 */
	public String getResetUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return("");
//		StringBuilder sb = new StringBuilder();
//		if (panlTokenMap.containsKey(lpseCode)) {
//			sb.append(getEncodedPanlValue("1"));
//			sb.append("/");
//		}
//
//		return (sb.toString());
	}

	public String getResetLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return("");
//		if (panlTokenMap.containsKey(lpseCode)) {
//			return (lpseCode);
//		}
//		return ("");
	}

	@Override public Logger getLogger() {
		return (LOGGER);
	}

	@Override public String getExplainDescription() {
		return ("The page number of the results (works in conjunction with the number of results).");
	}

	public void applyToQueryInternal(SolrQuery solrQuery, Map<String, List<LpseToken>> panlTokenMap) {

	}

}
