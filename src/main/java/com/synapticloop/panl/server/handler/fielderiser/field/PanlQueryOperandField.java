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
import com.synapticloop.panl.server.handler.tokeniser.token.QueryOperandLpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PanlQueryOperandField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlQueryOperandField.class);

	public PanlQueryOperandField(String lpseCode, String propertyKey, Properties properties, String collectionName) throws PanlServerException {
		super(lpseCode, properties, propertyKey, collectionName);

		logDetails();
	}

	@Override public String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return("");
	}

	@Override public String getCanonicalLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder(lpseCode);
		if(panlTokenMap.containsKey(lpseCode)) {
			QueryOperandLpseToken lpseToken = (QueryOperandLpseToken) panlTokenMap.get(lpseCode).get(0);
			if(lpseToken.getIsValid()) {
				sb.append(lpseToken.getQueryOperand());
			} else {
				sb.append(collectionProperties.getDefaultQueryOperand());
			}
		} else {
			sb.append(collectionProperties.getDefaultQueryOperand());
		}
		return(sb.toString());
	}

	public String getResetUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return("");
	}

	@Override public Logger getLogger() {
		return(LOGGER);
	}

	@Override public String getExplainDescription() {
		return("The query operand which maps to the 'q.op' parameter of Solr");
	}

	public void applyToQueryInternal(SolrQuery solrQuery, Map<String, List<LpseToken>> panlTokenMap) {
		if(panlTokenMap.containsKey(lpseCode)) {
			QueryOperandLpseToken lpseToken = (QueryOperandLpseToken)panlTokenMap.get(lpseCode).get(0);
			solrQuery.setParam("q.op", lpseToken.getQOpValue());
		}
	}

}
