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

import com.synapticloop.panl.server.handler.fielderiser.field.facet.PanlOrFacetField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class OrFacetLpseToken extends LpseToken {
	public static final String TOKEN_TYPE = "facet";

	private boolean hasMultivalueSeparator = false;

	public OrFacetLpseToken(
				CollectionProperties collectionProperties,
				String lpseCode,
				LpseTokeniser lpseTokeniser,
				StringTokenizer valueTokeniser) {

		super(lpseCode, collectionProperties);

		StringBuilder sb = new StringBuilder(lpseCode);
		int i = sb.length();
		while(i < collectionProperties.getLpseLength()) {
			if (lpseTokeniser.hasMoreTokens()) {
				sb.append(lpseTokeniser.nextToken());
			}
			i++;
		}

		this.lpseCode = sb.toString();

		if (!valueTokeniser.hasMoreTokens()) {
			this.isValid = false;
			return;
		}

		// at this point we are good to attempt to validate
		this.originalValue = valueTokeniser.nextToken();

		if (collectionProperties.hasFacetCode(lpseCode)) {
			this.solrField = collectionProperties.getSolrFieldNameFromLpseCode(lpseCode);

			PanlOrFacetField lpseField = (PanlOrFacetField) collectionProperties.getLpseField(this.lpseCode);

			this.value = lpseField.getDecodedValue(this.originalValue);

			if (null == this.value) {
				this.isValid = false;
			}
		} else {
			this.isValid = false;
		}
	}

	public OrFacetLpseToken(OrFacetLpseToken originalOrFacetToken, String value, boolean hasMultivalueSeparator) {
		// we are not going to need the collection properties
		super(originalOrFacetToken.lpseCode, null);
		this.originalValue = originalOrFacetToken.originalValue;
		this.solrField = originalOrFacetToken.solrField;
		this.value = value;
		this.isValid = originalOrFacetToken.isValid;
		this.hasMultivalueSeparator = hasMultivalueSeparator;
	}

	public static List<LpseToken> getSeparatedLpseTokens(
			String valueSeparator,
			CollectionProperties collectionProperties,
			String lpseCode,
			LpseTokeniser lpseTokeniser,
			StringTokenizer valueTokeniser) {

		OrFacetLpseToken orFacetLpseToken = new OrFacetLpseToken(
				collectionProperties,
				lpseCode,
				lpseTokeniser,
				valueTokeniser);

		String values = orFacetLpseToken.getValue();
		// the value will have the separator value
		List<LpseToken> lpseTokens = new ArrayList<>();
		if (null != values) {
			for(String value : values.split(valueSeparator)) {
				lpseTokens.add(new OrFacetLpseToken(orFacetLpseToken, value, true));
			}
		}
		return (lpseTokens);
	}

	@Override public String explain() {
		return ("PANL " +
						(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
						(this.hasMultivalueSeparator ? " <facet (OR SEP)>     LPSE code '" : " <facet (OR)>         LPSE code '") +
						this.lpseCode +
						"' (solr field '" +
						this.solrField +
						"') with parsed value '" +
						value +
						"', incoming value '" +
						this.originalValue +
						"'.");
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
