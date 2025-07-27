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

package com.synapticloop.panl.generator.bean.field;

public class PanlBoolField extends BasePanlField {
	protected PanlBoolField(String lpseCode,
			String solrFieldName,
			String solrFieldType,
			String schemaXmlLine,
			boolean isFacet,
			boolean isMultiValued,
			boolean isUniqueKey) {

		super(lpseCode, solrFieldName, solrFieldType, schemaXmlLine, isFacet, isMultiValued, isUniqueKey);
	}

	@Override public String getAdditionalProperties() {
		StringBuilder stringBuilder = new StringBuilder(getPrefixSuffix());
		stringBuilder
				.append("# Because this is a Boolean field, you can use a boolean value replacement for\n")
				.append("# either the true value, the false value, or neither.  This makes a more human-\n")
				.append("# readable URL\n")
				.append(String.format("#panl.bool.%s.true=is-%s\n", lpseCode, solrFieldName))
				.append(String.format("#panl.bool.%s.false=is-not-%s\n", lpseCode, solrFieldName))
				.append("# You may also want to be able to display this as a checkbox which will pass\n")
				.append("# through either a 'true' or 'false' value when selected\n")
				.append(String.format("#panl.bool.checkbox.%s=true_or_false\n", lpseCode));
		return (stringBuilder.toString());
	}
}
