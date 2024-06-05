package com.synapticloop.panl.server.properties.field;

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
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PanlQueryField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlQueryField.class);

	public PanlQueryField(String lpseCode, String propertyKey, Properties properties, String collectionName) throws PanlServerException {
		super(lpseCode, propertyKey, collectionName);

		logDetails();
	}

	@Override
	public Logger getLogger() {
		return(LOGGER);
	}

	@Override public String getExplainDescription() {
		return("The text query which maps to the 'q' parameter of Solr.");
	}

	public void applyToQueryInternal(SolrQuery solrQuery, Map<String, List<LpseToken>> panlTokenMap) {
		if(panlTokenMap.containsKey(panlLpseCode)) {
			LpseToken lpseToken = panlTokenMap.get(panlLpseCode).get(0);
			solrQuery.setQuery(lpseToken.getValue());
		}
	}

}
