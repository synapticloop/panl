package com.synapticloop.panl.server.handler.fielderiser.field.param;

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

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.fielderiser.field.BasePrefixSuffixField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.NumRowsLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.PageNumLpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PanlNumRowsField extends BasePrefixSuffixField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlNumRowsField.class);

	public PanlNumRowsField(String lpseCode, String propertyKey, Properties properties, String solrCollection, String panlCollectionUri) throws PanlServerException {
		super(lpseCode, propertyKey, properties, solrCollection, panlCollectionUri);
	}

	@Override public Logger getLogger() {
		return (LOGGER);
	}

	/**
	 * <p>Get the canonical URI path for the number of results.  This __ALWAYS__
	 * returns a URI path.  The path will be encoded with a prefix and/or
	 * suffix if they have been set.</p>
	 *
	 * @param panlTokenMap The token map with all fields and a list of their
	 * 		values
	 * @param collectionProperties The collection properties
	 *
	 * @return The URI path, never an empty string
	 */
	@Override public String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap,
		CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder();
		if (panlTokenMap.containsKey(lpseCode) && !panlTokenMap.get(lpseCode).isEmpty()) {

			// we are ony getting the first token from the list - there can only be
			// one - so we choose the first one

			NumRowsLpseToken numRowsLpseToken = (NumRowsLpseToken) panlTokenMap.get(lpseCode).get(0);
			sb.append(getEncodedPanlValue(Integer.toString(numRowsLpseToken.getNumRows())));
		} else {
			sb.append(getEncodedPanlValue(Integer.toString(collectionProperties.getNumResultsPerPage())));
		}

		sb.append("/");
		return (sb.toString());
	}

	@Override public String getCanonicalLpseCode(Map<String, List<LpseToken>> panlTokenMap,
		CollectionProperties collectionProperties) {
		return (lpseCode);
	}

	@Override public List<String> explainAdditional() {
		List<String> explanations = new ArrayList<>();
		explanations.add("The number of results to return per query.");
		return(explanations);
	}

	@Override public void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokenList) {
		// do nothing - this relies on other data and is set by the handler
	}

	@Override public List<LpseToken> instantiateTokens(CollectionProperties collectionProperties, String lpseCode, String query, StringTokenizer valueTokeniser, LpseTokeniser lpseTokeniser) {
		return(List.of(new NumRowsLpseToken(collectionProperties, this.lpseCode, valueTokeniser)));
	}

	@Override protected void logDetails() {
		getLogger().info("[ Solr/Panl '{}/{}' ] Number of results per page parameter mapped to '{}'.",
				solrCollection,
				panlCollectionUri,
				lpseCode);
	}
}
