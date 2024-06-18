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
 *  IN THE SOFTWARE.
 */

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PanlPassThroughField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlPassThroughField.class);
	private final boolean panlParamPassThroughCanonical;

	public PanlPassThroughField(String lpseCode, String propertyKey, Properties properties, String solrCollection) throws PanlServerException {
		super(lpseCode, properties, propertyKey, solrCollection);

		this.panlParamPassThroughCanonical = properties.getProperty("panl.param.passthrough.canonical", "false").equals("true");
		populateParamSuffixAndPrefix();

		logDetails();
	}

	@Override
	public String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		if(this.panlParamPassThroughCanonical) {
			StringBuilder sb = new StringBuilder();
			if (panlTokenMap.containsKey(lpseCode)) {
				for (LpseToken lpseToken : panlTokenMap.get(lpseCode)) {
					if (lpseToken.getIsValid()) {
						sb.append(getEncodedPanlValue(lpseToken));
						sb.append("/");
					}
				}
			}
			return (sb.toString());
		} else {
			return ("");
		}
	}

	@Override
	public String getCanonicalLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		if(this.panlParamPassThroughCanonical) {
			return(this.lpseCode);
		} else {
			return ("");
		}
	}

	@Override public List<String> explain() {
		List<String> temp = new ArrayList<>();
		temp.add("FIELD CONFIG [ " +
				this.getClass().getSimpleName() +
				" ] LPSE code '" +
				lpseCode +
				"'.");

		return (temp);
	}

	@Override public Logger getLogger() {
		return (LOGGER);
	}

	@Override public List<String> explainAdditional() {
		List<String> explanations = new ArrayList<>();
		explanations.add("This field is ignored by the Panl server and is not passed through to Solr.");
		return(explanations);
	}

	public void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokenList) {
		// do nothing
	}

}
