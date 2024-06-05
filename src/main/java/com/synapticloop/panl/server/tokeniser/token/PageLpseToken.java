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

import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.properties.field.BaseField;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.StringTokenizer;

public class PageLpseToken extends LpseToken {
	private int pageNum = 0;
	private final CollectionProperties collectionProperties;

	public PageLpseToken(CollectionProperties collectionProperties, String panlLpseCode, StringTokenizer valueTokenizer) {
		super(panlLpseCode);
		this.collectionProperties = collectionProperties;

		int pageNumTemp;

		if (valueTokenizer.hasMoreTokens()) {
			String pageNumTempString = "";
			BaseField lpseField = collectionProperties.getLpseField(panlLpseCode);
			if(null != lpseField) {
				pageNumTempString = lpseField.getDecodedValue(valueTokenizer.nextToken());
			} else {
				this.isValid = false;
			}

			try {
				pageNumTemp = Integer.parseInt(pageNumTempString);
			} catch (NumberFormatException e) {
				isValid = false;
				pageNumTemp = 1;
			}
		} else {
			isValid = false;
			pageNumTemp = 1;
		}

		if (pageNumTemp <= 0) {
			pageNumTemp = 1;
		}

		this.value = Integer.toString(pageNumTemp);
		this.pageNum = pageNumTemp;
	}

	@Override public String getUriPathComponent() {
		return (getURIComponentForPageNumber(this.pageNum));
	}

	@Override public String getLpseComponent() {
		return "";
	}

	private String getURIComponentForPageNumber(int pageNum) {
		if(isValid) {
			return (collectionProperties.getLpseField(lpseCode).getEncodedPanlValue(Integer.toString(pageNum)) + "/");
		} else {
			return("");
		}
	}

	@Override public String explain() {
		return ("PANL " +
				(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				" <page>          LPSE code '" +
				this.lpseCode +
				"' using " +
				(this.isValid ? "parsed" : "default") +
				" value of '" +
				this.pageNum +
				"'.");
	}

	@Override public String getType() {
		return ("page");
	}

	public int getPageNum() {
		return (this.pageNum);
	}
}
