package com.synapticloop.panl.generator.bean;

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

/**
 * <p>A field is a one to one mapping of the SOlr fields defined in the Solr
 * schema file and encapsulates all information required to generate the
 * example properties.</p>
 *
 * @author synapticloop
 */
public class PanlField {
	private final String lpseCode;
	private final String solrFieldName;
	private final String schemaXmlLine;
	private final String solrFieldType;
	private final boolean isFacet;

	/**
	 * <p>Instantiate a field</p>
	 *
	 * @param lpseCode The LPSE code that is assigned to the Solr field
	 * @param solrFieldName The name of the field from the schema
	 * @param schemaXmlLine The generated field definition line from the
	 * 		managed-schema.xml line
	 * @param solrFieldType The field storage type for the Solr field
	 */
	public PanlField(String lpseCode, String solrFieldName, String solrFieldType, String schemaXmlLine, boolean isFacet) {
		this.lpseCode = lpseCode;
		this.solrFieldName = solrFieldName;
		this.solrFieldType = solrFieldType;
		this.schemaXmlLine = schemaXmlLine;
		this.isFacet = isFacet;
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
		String booleanFieldText = "";
		String dateFieldText = "";

		String prefixSuffix = "# The following two properties are optional and the values should be changed\n" +
				String.format("#panl.prefix.%s=prefix\n", lpseCode) +
				String.format("#panl.suffix.%s=suffix\n", lpseCode);

		if (solrFieldType.equals("solr.BoolField")) {
			booleanFieldText = "# Because this is a Boolean field, you can use a boolean value replacement for\n" +
					"# either the true value, the false value, or neither.  This makes a more human-\n" +
					"# readable URL\n" +
					String.format("#panl.bool.%s.true=is-%s\n", lpseCode, solrFieldName) +
					String.format("#panl.bool.%s.false=is-not-%s\n", lpseCode, solrFieldName);
		}

		if (solrFieldType.equals("solr.DatePointField")) {
			dateFieldText = "# Because this is a Date field, there are special queries that can be applied\n" +
					"# for a date range. You can query for results up to NOW, or from NOW onwards.\n" +
					"# Either, or both of these properties may be set\n" +
					String.format("#panl.date.%s.previous=previous \n", lpseCode) +
					String.format("#panl.date.%s.next=next \n", lpseCode) +
					String.format("#panl.date.%s.years=\\ years\n", lpseCode) +
					String.format("#panl.date.%s.months=\\ months\n", lpseCode) +
					String.format("#panl.date.%s.days=\\ days\n", lpseCode) +
					String.format("#panl.date.%s.hours=\\ hours\n", lpseCode);
			// no prefix or suffix for date fields
			prefixSuffix = "";
		}

		// TODO - add in all of the properties (RANGE etc)
		return (String.format("\n# %s\n", schemaXmlLine) +
				(!isFacet ? "# This configuration can __ONLY__ ever be a field as it is not indexed in Solr\n" : "# This configuration can be either a field or a facet as it is indexed in Solr\n") +
				String.format("panl.%s.%s=%s\n", (isFacet ? "facet" : "field"), lpseCode, solrFieldName) +
				String.format("panl.name.%s=%s\n", lpseCode, getPrettyName(solrFieldName)) +
				String.format("panl.type.%s=%s\n", lpseCode, solrFieldType) +
				prefixSuffix +
				booleanFieldText +
				dateFieldText +
				"# If you want this facet to only appear if another facet has already been \n" +
				"# passed through then add the LPSE code(s) in a comma separated list below\n" +
				String.format("#panl.when.%s=\n", lpseCode) +
				"# By default Solr will always return facet ordered by count descending (i.e.\n" +
				"# The largest counts first) - uncomment the below line to return it in value\n" +
				"# order (e.g. alphabetical/numerical)\n" +
				String.format("#panl.facetsort.%s=index\n", lpseCode)
		);
	}

	public String getLpseCode() {
		return (lpseCode);
	}
}
