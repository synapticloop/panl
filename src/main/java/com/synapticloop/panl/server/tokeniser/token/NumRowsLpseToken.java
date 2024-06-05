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

public class NumRowsLpseToken extends LpseToken {
	private CollectionProperties collectionProperties;

	private int numRows;

	public NumRowsLpseToken(CollectionProperties collectionProperties, String panlLpseCode, StringTokenizer valueTokenizer) {
		super(panlLpseCode);
		this.collectionProperties = collectionProperties;

		int numRowsTemp;

		if (valueTokenizer.hasMoreTokens()) {
			String numRowsTempString = "";
			BaseField lpseField = collectionProperties.getLpseField(panlLpseCode);
			if(null != lpseField) {
				numRowsTempString = lpseField.getDecodedValue(valueTokenizer.nextToken());
			} else {
				this.isValid = false;
			}

			try {
				numRowsTemp = Integer.parseInt(numRowsTempString);
			} catch (NumberFormatException e) {
				isValid = false;
				numRowsTemp = collectionProperties.getNumResultsPerPage();
			}
		} else {
			isValid = false;
			numRowsTemp = collectionProperties.getNumResultsPerPage();
		}
		this.value = Integer.toString(numRowsTemp);
		this.numRows = numRowsTemp;
	}

	@Override public String explain() {
		// TODO - suffix and prefix
		return ("PANL " +
				(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				" <rows>          LPSE code '" +
				this.lpseCode +
				"' using " +
				(this.isValid ? "parsed" : "default") +
				" value of '" +
				this.numRows +
				"'.");
	}

	@Override public void applyToQuery(SolrQuery solrQuery) {
		solrQuery.setRows(this.numRows);
	}

	@Override public String getType() {
		return("numrows");
	}

	public int getNumRows() {
		return(this.numRows);
	}
}
