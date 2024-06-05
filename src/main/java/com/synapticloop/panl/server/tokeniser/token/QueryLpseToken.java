package com.synapticloop.panl.server.tokeniser.token;

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

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.solr.client.solrj.SolrQuery;

import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public class QueryLpseToken extends LpseToken {
	private boolean isOverride;

	public QueryLpseToken(
			String queryFromUri,
			String panlLpseCode) {
		this(queryFromUri, panlLpseCode, null);
	}

	public QueryLpseToken(
			String queryFromUri,
			String panlLpseCode,
			StringTokenizer valueTokeniser) {
		super(panlLpseCode);

		if(null != valueTokeniser && valueTokeniser.hasMoreTokens()) {
			this.value = valueTokeniser.nextToken();
		}

		for (NameValuePair nameValuePair : URLEncodedUtils.parse(queryFromUri, StandardCharsets.UTF_8)) {
			// TODO - do we want to allow people to change this???
			if(nameValuePair.getName().equals("q")) {
				this.value = nameValuePair.getValue();
				isOverride = true;
				break;
			}
		}
	}

	@Override public String getUriPathComponent() {
		if(null != value) {
			return (value + "/");
		} else {
			return("");
		}
	}

	@Override public String getLpseComponent() {
		if(null != value) {
			return (lpseCode);
		} else {
			return("");
		}
	}

	@Override public String explain() {
		return ("PANL [  VALID  ] <query>         LPSE code '" +
				this.lpseCode +
				"' with value '" +
				value +
				"'" +
				(isOverride ? " (Overridden by query parameter).": ".")
		);
	}

	@Override public void applyToQuery(SolrQuery solrQuery) {
		solrQuery.setQuery(value);
	}

	@Override public String getType() {
		return("query");
	}

}
