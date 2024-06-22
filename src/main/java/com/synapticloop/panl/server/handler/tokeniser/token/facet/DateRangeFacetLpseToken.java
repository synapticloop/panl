package com.synapticloop.panl.server.handler.tokeniser.token.facet;

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

import com.synapticloop.panl.server.handler.fielderiser.field.facet.PanlDateRangeFacetField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.bean.PreviousNextValueBean;

import java.util.StringTokenizer;

public class DateRangeFacetLpseToken extends LpseToken {
	public static final String TOKEN_TYPE = "facet";

	// TODO - should be done in the BaseField
	private String solrField = null;

	private String previousNext;
	private String solrRangeDesignator;
	private String designator;

	public DateRangeFacetLpseToken(
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

		if (!valueTokeniser.hasMoreTokens()) {
			this.isValid = false;
			return;
		}

		// at this point we are good to attempt to validate
		this.originalValue = valueTokeniser.nextToken();

		if (collectionProperties.hasFacetCode(lpseCode)) {
			this.solrField = collectionProperties.getSolrFieldNameFromLpseCode(lpseCode);

			PanlDateRangeFacetField lpseField = (PanlDateRangeFacetField) collectionProperties.getLpseField(this.lpseCode);


			PreviousNextValueBean previousNextValueBean = lpseField.getDecodedRangeValue(this.originalValue);
			if (null == previousNextValueBean) {
				this.isValid = false;
			} else {
				this.value = previousNextValueBean.getValue();
				this.previousNext = previousNextValueBean.getPreviousNext();
				this.solrRangeDesignator = previousNextValueBean.getSolrRangeDesignator();
				this.designator = previousNextValueBean.getDesignator();
			}
		} else {
			this.isValid = false;
		}

	}

	@Override public String explain() {
		return ("PANL " +
				(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				" <facet (DATE)>    LPSE code '" +
				this.lpseCode +
				"' (solr field '" +
				this.solrField +
				"') with parsed value '" +
				this.previousNext +
				" " +
				value +
				" " +
				this.solrRangeDesignator +
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

	public String getPreviousNext() {
		return previousNext;
	}

	public String getSolrRangeDesignator() {
		return solrRangeDesignator;
	}

	public String getDesignator() {
		return (designator);
	}
}
