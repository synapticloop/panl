package com.synapticloop.panl.server.handler.fielderiser.field.param;

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
 * IN THE SOFTWARE.
 */

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.QueryLpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class PanlQueryField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlQueryField.class);

	public PanlQueryField(String lpseCode, String propertyKey, Properties properties, String solrCollection, String panlCollectionUri) throws PanlServerException {
		super(lpseCode, properties, propertyKey, solrCollection, panlCollectionUri);
	}

	/**
	 * <p>We only apply the first query</p>
	 *
	 * @param solrQuery The Solr Query to apply to
	 * @param lpseTokenList The list of tokens
	 */
	public void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokenList) {
		if(!lpseTokenList.isEmpty()) {
			solrQuery.setQuery("\"" + lpseTokenList.get(0).getValue() + "\"");
		}
	}

	@Override public Logger getLogger() {
		return(LOGGER);
	}

	@Override public List<String> explainAdditional() {
		List<String> explanations = new ArrayList<>();
		explanations.add("The search text or phrase query which maps to the 'q' parameter of Solr.");
		return(explanations);
	}

	@Override public void appendToAvailableObjectInternal(JSONObject jsonObject) {

	}

	@Override public LpseToken instantiateToken(CollectionProperties collectionProperties, String lpseCode, String query, StringTokenizer valueTokeniser, LpseTokeniser lpseTokeniser) {
		return(new QueryLpseToken(collectionProperties, this.lpseCode, query, valueTokeniser));
	}

	@Override protected void logDetails() {
		getLogger().info("[ Solr/Panl: '{}/{}' ] Query parameter mapped to '{}'.",
				solrCollection,
				panlCollectionUri,
				lpseCode);
	}

	@Override public List<String> explain() {
		List<String> temp = new ArrayList<>();
		temp.add("PARAM CONFIG [ " +
				this.getClass().getSimpleName() +
				" ] LPSE code '" +
				lpseCode +
				"'.");

		temp.addAll(explainAdditional());
		temp.addAll(WARNING_MESSAGES);
		return(temp);
	}

}
