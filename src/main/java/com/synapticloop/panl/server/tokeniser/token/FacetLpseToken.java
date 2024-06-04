package com.synapticloop.panl.server.tokeniser.token;

/*
 * Copyright (c) 2008-2024 synapticloop.
 *
 * https://github.com/synapticloop/panl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 *  IN THE SOFTWARE.
 */

import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.properties.field.BaseField;
import com.synapticloop.panl.server.tokeniser.PanlTokeniser;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.StringTokenizer;

public class FacetLpseToken extends LpseToken {
	private String solrField = null;
	private CollectionProperties collectionProperties;

	public FacetLpseToken(
			CollectionProperties collectionProperties,
			String panlLpseCode,
			PanlTokeniser lpseTokeniser,
			StringTokenizer valueTokeniser) {
		super(panlLpseCode);
		this.collectionProperties = collectionProperties;

		StringBuilder sb = new StringBuilder(panlLpseCode);
		int i = 1;
		while (i < collectionProperties.getPanlLpseLength()) {
			if (lpseTokeniser.hasMoreTokens()) {
				sb.append(lpseTokeniser.nextToken());
			}
			i++;
		}

		this.lpseCode = sb.toString();

		BaseField lpseField = collectionProperties.getLpseField(this.lpseCode);
		if (null != lpseField) {
			this.originalValue = valueTokeniser.nextToken();
			this.value = lpseField.getDecodedValue(this.originalValue);

			if (null == this.value) {
				isValid = false;
			}
		} else {
			this.isValid = false;
		}

		if (collectionProperties.hasFacetCode(panlLpseCode)) {
			this.solrField = collectionProperties.getSolrFieldNameFromPanlLpseCode(panlLpseCode);
		} else {
			this.isValid = false;
		}
	}

	@Override public String getUriPathComponent() {
		if (isValid) {
			BaseField lpseField = collectionProperties.getLpseField(this.lpseCode);
			return(lpseField.getEncodedPanlValue(this.value) + "/");
		} else {
			return ("");
		}
	}

	@Override public String getLpseComponent() {
		if (isValid) {
			return (this.lpseCode);
		} else {
			return ("");
		}
	}

	@Override public String explain() {
		return ("PANL " +
				(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				" <facet>         LPSE code '" +
				this.lpseCode +
				"' (solr field '" +
				this.solrField +
				"') with parsed value '" +
				value +
				"', incoming value '" +
				this.originalValue +
				"'.");
	}

	@Override public void applyToQuery(SolrQuery solrQuery) {
		if (isValid) {
			solrQuery.addFilterQuery(this.solrField + ":\"" + value + "\"");
		}
	}

	@Override public String getType() {
		return ("facet");
	}

	public String getSolrField() {
		return solrField;
	}
}
