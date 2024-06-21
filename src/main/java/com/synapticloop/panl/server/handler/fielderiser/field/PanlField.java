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
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.RangeFacetLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.PageNumLpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PanlField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlField.class);

	public PanlField(String lpseCode, String propertyKey, Properties properties, String solrCollection, int lpseLength) throws PanlServerException {
		super(lpseCode, propertyKey, properties, solrCollection, lpseLength);

		// fields don't have prefixes/suffixes or URI parts
		populatePanlAndSolrFieldNames();

		logDetails();
	}


	@Override
	public String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return ("");
	}

	@Override
	public String getCanonicalLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return ("");
	}

	@Override
	public Logger getLogger() {
		return (LOGGER);
	}

	@Override public List<String> explainAdditional() {
		List<String> explanations = new ArrayList<>();
		explanations.add("A Solr field that can be configured to be sorted by, or returned in the field set.");
		return(explanations);
	}

	public void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokenList) {
		// do nothing
	}

	@Override public void appendAvailableObjectInternal(JSONObject jsonObject) {

	}

	public LpseToken instantiateToken(CollectionProperties collectionProperties, String lpseCode, String query, StringTokenizer valueTokeniser, LpseTokeniser lpseTokeniser) {
		return(new PageNumLpseToken(collectionProperties, this.lpseCode, valueTokeniser));
	}

}
