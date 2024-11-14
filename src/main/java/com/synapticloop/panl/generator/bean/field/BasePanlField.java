package com.synapticloop.panl.generator.bean.field;

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

public abstract class BasePanlField {
	protected final String lpseCode;
	protected final String solrFieldName;
	protected final String schemaXmlLine;
	protected final String solrFieldType;
	protected final boolean isFacetable;
	protected final boolean isMultiValued;

	public static BasePanlField getPanlField(
		String lpseCode,
		String solrFieldName,
		String solrFieldType,
		String schemaXmlLine,
		boolean isFacetable,
		boolean isMultiValued) {

		switch (solrFieldType) {
			case "solr.BoolField":
				return (new PanlBoolField(lpseCode, solrFieldName, solrFieldType, schemaXmlLine, isFacetable, isMultiValued));
			case "solr.TextField":
			case "solr.StrField":
			case "solr.UUIDField":
				return (new PanlTextField(lpseCode, solrFieldName, solrFieldType, schemaXmlLine, isFacetable, isMultiValued));
			case "solr.IntPointField":
			case "solr.FloatPointField":
			case "solr.LongPointField":
			case "solr.DoublePointField":
				return (new PanlRangeField(lpseCode, solrFieldName, solrFieldType, schemaXmlLine, isFacetable, isMultiValued));
			case "solr.DatePointField":
				return (new PanlDateRangeField(lpseCode, solrFieldName, solrFieldType, schemaXmlLine, isFacetable,
					isMultiValued));
		}

		return (new PanlUnsupportedField(lpseCode, solrFieldName, solrFieldType, schemaXmlLine, isFacetable,
			isMultiValued));
	}

	protected BasePanlField(String lpseCode,
		String solrFieldName,
		String solrFieldType,
		String schemaXmlLine,
		boolean isFacetable,
		boolean isMultiValued) {

		this.lpseCode = lpseCode;
		this.solrFieldName = solrFieldName;
		this.solrFieldType = solrFieldType;
		this.schemaXmlLine = schemaXmlLine;
		this.isFacetable = isFacetable;
		this.isMultiValued = isMultiValued;
	}

	private String getPrettyName(String name) {
		StringBuilder sb = new StringBuilder();
		boolean shouldUppercase = true;
		for (char c : name.toCharArray()) {
			switch (c) {
				case '_':
				case '-':
					shouldUppercase = true;
					sb.append(" ");
					break;
				default:
					if (shouldUppercase) {
						sb.append(String.valueOf(c).toUpperCase());
					} else {
						sb.append(c);
					}
					shouldUppercase = false;
			}
		}
		return (sb.toString().trim());
	}

	public String toProperties() {
		StringBuilder stringBuilder = new StringBuilder(String.format("\n# %s\n", schemaXmlLine));
		if (isFacetable) {
			stringBuilder.append("# This configuration can be either a field or a facet as it is indexed in Solr\n");
		} else {
			stringBuilder.append("# This configuration can __ONLY__ ever be a field as it is not indexed in Solr\n");
		}

		stringBuilder.append(String.format("panl.%s.%s=%s\n", (isFacetable ? "facet" : "field"), lpseCode, solrFieldName))
		             .append(String.format("panl.name.%s=%s\n", lpseCode, getPrettyName(solrFieldName)))
		             .append(String.format("panl.type.%s=%s\n", lpseCode, solrFieldType));

		if (isFacetable) {
			if (isMultiValued) {
				stringBuilder.append("# This Solr field is configured as multiValued, and is added as a property for the\n")
				             .append("# so that single page search can be generated properly.  You __SHOULD_NOT___\n")
				             .append("# change this unless the underlying Solr schema changes\n")
				             .append(String.format("panl.multivalue.%s=%b\n", lpseCode, isMultiValued));
			} else {
				// this is a good candidate for an OR facet - but not a date
				if(!(this instanceof PanlDateRangeField)) {
					stringBuilder.append("# This field is a candidate for an OR facet, you may wish to configure the\n")
					             .append("# following properties\n")
					             .append(String.format("#panl.or.facet.%s=true\n", lpseCode))
					             .append(String.format("#panl.or.always.%s=true\n", lpseCode));
				}
			}

			stringBuilder.append(getAdditionalProperties());

			stringBuilder.append("# If you want this facet to only appear if another facet has already been \n")
			             .append("# passed through then add the LPSE code(s) in a comma separated list below\n")
			             .append(String.format("#panl.when.%s=\n", lpseCode));
		}


		return (stringBuilder.toString());
	}

	protected String getPrefixSuffix() {
		return ("# The following two properties are optional and may be set to any value\n" +
			String.format("#panl.prefix.%s=prefix\n", lpseCode) +
			String.format("#panl.suffix.%s=suffix\n", lpseCode));
	}

	protected String getSortOrder() {
		return ("# By default Solr will always return facet ordered by count descending (i.e.\n" +
			"# The largest counts first) - uncomment the below line to return it in value\n" +
			"# order (e.g. alphabetical/numerical)\n" +
			String.format("#panl.facetsort.%s=index\n", lpseCode));
	}

	/**
	 * <p>Get any additional properties for this particular BasePanlField type</p>
	 *
	 * @return The formatted property string for additional properties
	 */
	protected abstract String getAdditionalProperties();

	public String getLpseCode() {
		return lpseCode;
	}
}
