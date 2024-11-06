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

public class PanlRangeField extends BasePanlField {
	protected PanlRangeField(String lpseCode,
		String solrFieldName,
		String solrFieldType,
		String schemaXmlLine,
		boolean isFacet,
		boolean isMultiValued) {

		super(lpseCode, solrFieldName, solrFieldType, schemaXmlLine, isFacet, isMultiValued);
	}

	@Override public String getAdditionalProperties() {
		StringBuilder stringBuilder = new StringBuilder(getPrefixSuffix());
		stringBuilder.append("# This field can be configured as a range facet by setting the following\n")
		             .append("# properties\n")
		             .append("#panl.range.facet.w=true\n")
		             .append("#panl.range.min.w=0\n")
		             .append("#panl.range.max.w=100\n")
		             .append("#panl.range.prefix.w=range_prefix\n")
		             .append("#panl.range.infix.w=range_infix\n")
		             .append("#panl.range.suffix.w=range_suffix\n")
		             .append("#panl.range.min.value.w=min_value\n")
		             .append("#panl.range.max.value.w=max_value\n")
		             .append("#panl.range.min.wildcard.w=true\n")
		             .append("#panl.range.max.wildcard.w=true\n");

		stringBuilder.append(getSortOrder());
		return (stringBuilder.toString());
	}
}
