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

import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <p>A Facet LPSE token collects facets for the Solr implementation.  The
 * token corresponds to one of the following facet types:</p>
 *
 * <ol>
 *   <li>Regular Facet</li>
 *   <li>Boolean Facet</li>
 *   <li>OR Facet</li>
 *   <li>RANGE Facet</li>
 * </ol>
 *
 * <p>All of the above tokens perform the same except for the OR token.</p>
 *
 * <p>The LPSE code for all (except RANGE) tokens is in the following format:</p>
 *
 * <p><code>/&lt;lpse_code&gt;/</code></p>
 *
 * <p>For a RANGE facet, the LPSE code is one of:</p>
 *
 * <p><code>/&lt;lpse_code&gt;+&lt;lpse_code&gt;/</code>, or
 * <code>/&lt;lpse_code&gt;+&lt;lpse_code&gt;/</code></p>
 *
 * <p>With the <code>+</code> or <code>-</code> determining how the URI path
 * will look.</p>
 *
 * <p>For the <code>+</code> - i.e. <code>/&lt;lpse_code&gt;+&lt;lpse_code&gt;/</code>
 * the values for this token will be separated by a forward slash
 * <code>/</code> in the URI path, e.g.</p>
 *
 * <p><code>/10/20/&lt;lpse_code&gt;+&lt;lpse_code&gt;/</code></p>
 *
 * <p>For the <code>-</code> - i.e. <code>/&lt;lpse_code&gt;-&lt;lpse_code&gt;/</code>
 * the values for this token will be separated by the infix replacement which
 * is defined by the <code>panl.range.&lt;lpse_code&gt;.infix</code>
 * property.  This would translate to:</p>
 *
 * <p><code>/10-to-20/&lt;lpse_code&gt;-&lt;lpse_code&gt;/</code></p>
 */
public class FacetLpseToken extends LpseToken {
	public static final String TOKEN_TYPE = "facet";

	protected boolean isRangeToken = false;
	protected boolean hasInfix = false;
	private boolean hasMultivalueSeparator = false;

	public FacetLpseToken(
			CollectionProperties collectionProperties,
			String lpseCode,
			LpseTokeniser lpseTokeniser,
			StringTokenizer valueTokeniser) {

		super(lpseCode, collectionProperties);

		StringBuilder sb = new StringBuilder(lpseCode);
		int i = sb.length();
		while (i < collectionProperties.getLpseLength()) {
			if (lpseTokeniser.hasMoreTokens()) {
				sb.append(URLDecoder.decode(lpseTokeniser.nextToken(), StandardCharsets.UTF_8));
			}
			i++;
		}

		this.lpseCode = sb.toString();

		BaseField lpseField = collectionProperties.getLpseField(this.lpseCode);


		if (null != lpseField) {
			this.isUniqueKey = lpseField.getIsUniqueKey();
			if(!valueTokeniser.hasMoreTokens()) {
				this.isValid = false;
			} else {
				// we have a token - get it
				this.originalValue = valueTokeniser.nextToken();
				this.value = lpseField.getDecodedValue(this.originalValue);
				if (null == this.value) {
						isValid = false;
				}
			}
		} else {
			this.isValid = false;
		}

		if (collectionProperties.hasFacetCode(lpseCode)) {
			this.solrField = collectionProperties.getSolrFieldNameFromLpseCode(lpseCode);
		} else {
			this.isValid = false;
		}
	}

	public FacetLpseToken(FacetLpseToken originalFacetToken, String value, boolean hasMultivalueSeparator) {
		// we are not going to need the collection properties
		super(originalFacetToken.lpseCode, null);
		this.originalValue = originalFacetToken.originalValue;
		this.solrField = originalFacetToken.solrField;
		this.value = value;
		this.isValid = originalFacetToken.isValid;
		this.hasMultivalueSeparator = hasMultivalueSeparator;
		this.isUniqueKey = originalFacetToken.isUniqueKey;
	}

	public static List<LpseToken> getSeparatedLpseTokens(
			String valueSeparator,
			CollectionProperties collectionProperties,
			String lpseCode,
			LpseTokeniser lpseTokeniser,
			StringTokenizer valueTokeniser) {

		FacetLpseToken facetLpseToken = new FacetLpseToken(
				collectionProperties,
				lpseCode,
				lpseTokeniser,
				valueTokeniser);

		String values = facetLpseToken.getValue();
		// the value will have the separator value
		List<LpseToken> lpseTokens = new ArrayList<>();
		if (null != values) {
			for(String value : values.split(valueSeparator)) {
				lpseTokens.add(new FacetLpseToken(facetLpseToken, value, true));
			}
		}
		return (lpseTokens);
	}

	@Override public String explain() {
		return ("PANL " +
				(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				(this.hasMultivalueSeparator ? " <facet (multi SEP)>  LPSE code '" : " <facet>              LPSE code '") +
				this.lpseCode +
				"' (solr field '" +
				this.solrField +
				"') with parsed value '" +
				value +
				"', incoming value '" +
				this.originalValue +
				"'." +
				(this.isUniqueKey? " [UNIQUE_KEY]" : ""));
	}

	@Override public String getType() {
		return TOKEN_TYPE;
	}

	public String getSolrField() {
		return solrField;
	}

	public boolean getIsRangeToken() {
		return isRangeToken;
	}

	public boolean getHasInfix() {
		return hasInfix;
	}

	@Override public boolean getCanHaveMultiple() {
		return (true);
	}
}
