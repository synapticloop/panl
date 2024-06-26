package com.synapticloop.panl.server.handler.tokeniser.token.param;

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

import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class QueryLpseToken extends LpseToken {
	private boolean isOverride;

	public QueryLpseToken(
			CollectionProperties collectionProperties,
			String queryFromUri,
			String lpseCode) {
		this(collectionProperties, lpseCode, queryFromUri, null);
	}

	public QueryLpseToken(
			CollectionProperties collectionProperties,
			String lpseCode,
			String queryFromUri,
			StringTokenizer valueTokeniser) {
		super(lpseCode, collectionProperties);

		if (null != valueTokeniser && valueTokeniser.hasMoreTokens()) {
			this.value = valueTokeniser.nextToken();
		}

		for (NameValuePair nameValuePair : URLEncodedUtils.parse(queryFromUri, StandardCharsets.UTF_8)) {
			if (nameValuePair.getName().equals(collectionProperties.getFormQueryRespondTo())) {
				this.value = nameValuePair.getValue();

				isOverride = true;
				break;
			}
		}
	}

	@Override public String explain() {
		return ("PANL [  VALID  ] <query>           LPSE code '" +
				this.lpseCode +
				"' with value '" +
				value +
				"'" +
				(isOverride ? " (Overridden by query parameter)." : ".")
		);
	}

	@Override public String getType() {
		return ("query");
	}

}
