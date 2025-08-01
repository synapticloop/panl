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

import com.synapticloop.panl.server.handler.fielderiser.field.facet.PanlRangeFacetField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.bean.FromToBean;

import java.util.StringTokenizer;

/**
 * <p>A Facet LPSE token collects facets for the Solr implementation.  The
 * token corresponds to one of the following facet types:</p>
 *
 * <ol>
 *   <li>Regular Facet</li>
 *   <li>Boolean Facet</li>
 *   <li>OR Facet</li>
 *   <li>DATE Facet</li>
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
public class RangeFacetLpseToken extends LpseToken {
	public static final String TOKEN_TYPE = "facet";

	private String toValue = null;
	protected boolean hasInfix = false;
	protected boolean isRangeToken = false;

	/**
	 * <p>Instantiate a RANGE facet.</p>
	 *
	 * @param collectionProperties The collection properties
	 * @param lpseCode The lpse code to respond to
	 * @param lpseTokeniser The lpse tokeniser for the URI
	 * @param valueTokeniser The value tokeniser for the URI
	 */
	public RangeFacetLpseToken(
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

		// at this point, we need to determine whether this is a range token by
		// looking at the next lpse token - if it is a +, or a -
		if (lpseTokeniser.hasMoreTokens()) {
			String possibleRangeDesignator = lpseTokeniser.nextToken();
			if (possibleRangeDesignator.equals("+") || possibleRangeDesignator.equals("-")) {
				// this is a range query, the next part should be the same as the
				// current LPSE code - find it
				if (possibleRangeDesignator.equals("-")) {
					this.hasInfix = true;
				}

				this.isRangeToken = true;
			} else {
				// this is not a range, just a single value
				lpseTokeniser.decrementCurrentPosition();
			}
		}

		PanlRangeFacetField panlRangeFacetField = (PanlRangeFacetField) collectionProperties.getLpseField(this.lpseCode);

		if (null != panlRangeFacetField) {
			if (!valueTokeniser.hasMoreTokens()) {
				this.isValid = false;
			} else {
				// we have a token - get it
				this.originalValue = valueTokeniser.nextToken();
			}
		}

		// we now have the original value and the token  should be valid so far...
		if(!this.isValid) {
			return;
		}

		// now we need to decode the original value
		if(this.isRangeToken) {
			FromToBean fromToBean = panlRangeFacetField.getDecodedRangeValues(this.originalValue);
			if (null == fromToBean) {
				this.isValid = false;
			} else {
				this.value = fromToBean.getFromValue();
				this.toValue = fromToBean.getToValue();
			}
		} else {
			// this is a singular value
			this.value = panlRangeFacetField.getDecodedValue(this.originalValue);

			if (null == this.value) {
				isValid = false;
			}
		}


		if (collectionProperties.hasFacetCode(lpseCode)) {
			this.solrField = collectionProperties.getSolrFieldNameFromLpseCode(lpseCode);
		} else {
			this.isValid = false;
		}
	}

	@Override public String explain() {
		return ("PANL " +
				(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				(isRangeToken ? " <facet (RANGE)>      LPSE code '" : " <facet (RANGE UNIQ)> LPSE code '") +
				this.lpseCode +
				"' (solr field '" +
				this.solrField +
				"') with parsed value '" +
				("RANGE(" + value + ":" + toValue + ") " + (hasInfix ? "with" : "without") + " infix" ) +
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

	public String getToValue() {
		return toValue;
	}

	public boolean getHasInfix() {
		return hasInfix;
	}

	public boolean getIsRangeToken() {
		return (isRangeToken);
	}

	@Override public boolean getCanHaveMultiple() {
		return (true);
	}
}
