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

package com.synapticloop.panl.editor.tab.collection;

import com.synapticloop.panl.server.handler.properties.CollectionProperties;

import java.util.Properties;

public class PanlField {
	private String schemaLine;

	private boolean isFacet = true;
	private boolean isOrFacet = false;
	private boolean isRangeFacet = false;
	private boolean isBooleanFacet = false;
	private boolean useAsSortField = false;

	private String name;
	private String type;
	private String prefix;
	private String suffix;
	private String when;
	private String trueValue = "";
	private String falseValue = "";

	public PanlField(Properties properties, String lpseCode) {
		// this is a dummy line which is pre-generated
		this.schemaLine = properties.getProperty("schema.line." + lpseCode);
		this.isFacet = "true".equals(properties.getProperty("panl.facet." + lpseCode, "false"));
		this.isOrFacet = "true".equals(properties.getProperty("panl.or.facet." + lpseCode, "false"));
		this.isRangeFacet  = "true".equals(properties.getProperty("panl.range.facet." + lpseCode, "false"));
		this.name = properties.getProperty("panl.name." + lpseCode);
		this.type = properties.getProperty("panl.type." + lpseCode);

		if(null != this.type && this.type.equals("solr.BoolField")) {
			this.isBooleanFacet = true;
		}

		this.trueValue = properties.getProperty("panl.bool." + lpseCode + ".true", "");
		this.falseValue = properties.getProperty("panl.bool." + lpseCode + ".false", "");

		this.prefix = properties.getProperty("panl.prefix." + lpseCode);
		this.suffix = properties.getProperty("panl.suffix." + lpseCode);
		this.when = properties.getProperty("panl.when." + lpseCode);

//		this.useAsSortField = properties.getProperty("panl.sort.fields", "").contains(this.name);
	}
}
