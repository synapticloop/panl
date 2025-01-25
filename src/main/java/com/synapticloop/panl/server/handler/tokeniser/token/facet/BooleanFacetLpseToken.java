package com.synapticloop.panl.server.handler.tokeniser.token.facet;

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

import com.synapticloop.panl.server.handler.fielderiser.field.facet.PanlBooleanFacetField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;

import java.util.StringTokenizer;

/**
 * <p>As the name would suggest, BOOLEAN facet can have only one of two values</p>
 *
 * <ul>
 *   <li><code>true</code>, or</li>
 *   <li><code>false</code></li>
 * </ul>
 *
 * <p>BOOLEAN facets can have a prefix, a suffix, neither, or both as well as a
 * true/false value replacement.</p>
 *
 * @author syanapticloop
 */
public class BooleanFacetLpseToken extends LpseToken {
	public static final String TOKEN_TYPE = "facet";

	private String solrField = null;

	public BooleanFacetLpseToken(
			CollectionProperties collectionProperties,
			String lpseCode,
			LpseTokeniser lpseTokeniser,
			StringTokenizer valueTokeniser) {

		super(lpseCode, collectionProperties);

		StringBuilder sb = new StringBuilder(lpseCode);
		int i = sb.length();
		while (i < collectionProperties.getLpseLength()) {
			if (lpseTokeniser.hasMoreTokens()) {
				sb.append(lpseTokeniser.nextToken());
			}
			i++;
		}

		this.lpseCode = sb.toString();

		if(!valueTokeniser.hasMoreTokens()) {
			this.isValid = false;
			return;
		}

		// at this point we are good to attempt to validate
		this.originalValue = valueTokeniser.nextToken();

		if (collectionProperties.hasFacetCode(lpseCode)) {
			this.solrField = collectionProperties.getSolrFieldNameFromLpseCode(lpseCode);

			PanlBooleanFacetField lpseField = (PanlBooleanFacetField) collectionProperties.getLpseField(this.lpseCode);

			this.value = lpseField.getDecodedValue(this.originalValue);

			if(null == this.value) {
				this.isValid = false;
			}
		} else {
			this.isValid = false;
		}

	}

	@Override public String explain() {
		return ("PANL " +
				(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				" <facet (BOOLEAN)>  LPSE code '" +
				this.lpseCode +
				"' (solr field '" +
				this.solrField +
				"') with parsed value '" +
				value +
				"', incoming value '" +
				this.originalValue +
				"'.");
	}

	public String getInverseBooleanValue(LpseToken lpseToken) {
		PanlBooleanFacetField lpseField = (PanlBooleanFacetField) collectionProperties.getLpseField(this.lpseCode);
		if(value.equals("true")) {
			return(lpseField.getEncodedPanlValue("false"));
		} else {
			return(lpseField.getEncodedPanlValue("true"));
		}

	}

	@Override public String getType() {
		return TOKEN_TYPE;
	}

	public String getSolrField() {
		return solrField;
	}

	@Override public boolean getCanHaveMultiple() {
		return (true);
	}
}
