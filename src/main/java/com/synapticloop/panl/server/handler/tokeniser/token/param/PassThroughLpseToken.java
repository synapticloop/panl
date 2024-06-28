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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public class PassThroughLpseToken extends LpseToken {
	public PassThroughLpseToken(
			CollectionProperties collectionProperties,
			String lpseCode,
			StringTokenizer valueTokeniser) {
		super(lpseCode, collectionProperties);

		if(valueTokeniser.hasMoreTokens()) {
			this.value = URLDecoder.decode(
					valueTokeniser.nextToken(),
					StandardCharsets.UTF_8);
		}
	}

	@Override public String explain() {
		return ("PANL [  VALID  ] <passthrough>     LPSE code '" +
				this.lpseCode +
				"' with value '" +
				value +
				"'.");
	}

	@Override public String getType() {
		return ("passthrough");
	}

	/**
	 * <p>The equivalence value is always an empty string - these tokens are
	 * ignored.</p>
	 *
	 * @return An empty string
	 */
	@Override public String getEquivalenceValue() {
		return("");
	}

}
