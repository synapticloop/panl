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
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;

import java.util.StringTokenizer;

public class NumRowsLpseToken extends LpseToken {
	private int numRows;

	public NumRowsLpseToken(CollectionProperties collectionProperties, String lpseCode, StringTokenizer valueTokenizer) {
		super(lpseCode, collectionProperties);

		this.numRows = collectionProperties.getNumResultsPerPage();

		if (valueTokenizer.hasMoreTokens()) {
			BaseField lpseField = collectionProperties.getLpseField(lpseCode);

			// do we have a lpseField
			if(null != lpseField) {
				this.originalValue = valueTokenizer.nextToken();
				this.value = lpseField.getDecodedValue(originalValue);
			} else {
				this.isValid = false;
			}

			try {
				numRows = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				isValid = false;
			}

			if(numRows <= 0) {
				isValid = false;
			}
		} else {
			isValid = false;
		}

		if(!isValid) {
			// reset it to the default
			this.numRows = collectionProperties.getNumResultsPerPage();
		}
	}

	@Override public String explain() {
		// TODO - suffix and prefix
		return ("PANL " +
				(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				" <num_rows>        LPSE code '" +
				this.lpseCode +
				"' original URI path value '" +
				this.originalValue +
				"' using " +
				(this.isValid ? "parsed" : "default") +
				" value of '" +
				this.numRows +
				"'.");
	}

	@Override public String getType() {
		return("numrows");
	}

	public int getNumRows() {
		return(this.numRows);
	}

	/**
	 * <p>Override the value of this token with the new number of rows.</p>
	 *
	 * @param numRows The override number of rows for this token
	 */
	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}

	/**
	 * <p>Return the equivalence value for this token, which will always be
	 * <code>&lt;lpse_code&gt;/</code> as you may ony ever have one number of
	 * rows per LPSE URI path part.</p>
	 *
	 * @return The equivalence values
	 */
	@Override public String getEquivalenceValue() {
		return(this.getLpseCode() + "/");
	}

}
