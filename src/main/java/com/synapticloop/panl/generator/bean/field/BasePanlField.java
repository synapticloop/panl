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

package com.synapticloop.panl.generator.bean.field;

public abstract class BasePanlField {
	private final String lpseCode;
	private final String solrFieldName;
	private final String schemaXmlLine;
	private final String solrFieldType;
	private final boolean isFacet;
	private final boolean isMultiValued;

	public static BasePanlField getPanlField(
		String lpseCode,
		String solrFieldName,
		String solrFieldType,
		String schemaXmlLine,
		boolean isFacet,
		boolean isMultiValued) {

		switch (solrFieldType) {
			case "solr.BoolField":
				return new PanlBoolField(lpseCode, solrFieldName, solrFieldType, schemaXmlLine, isFacet, isMultiValued);
		}

		return(new PanlUnsupportedField(lpseCode, solrFieldName, solrFieldType, schemaXmlLine, isFacet, isMultiValued));
	}

	protected BasePanlField(String lpseCode,
		String solrFieldName,
		String solrFieldType,
		String schemaXmlLine,
		boolean isFacet,
		boolean isMultiValued) {

		this.lpseCode = lpseCode;
		this.solrFieldName = solrFieldName;
		this.solrFieldType = solrFieldType;
		this.schemaXmlLine = schemaXmlLine;
		this.isFacet = isFacet;
		this.isMultiValued = isMultiValued;
	}
}
