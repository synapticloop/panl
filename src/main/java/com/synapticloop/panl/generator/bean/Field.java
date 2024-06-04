package com.synapticloop.panl.generator.bean;

public class Field {
	private final String code;
	private final String field;
	private final String schemaXml;
	private final String solrClassName;

	public Field(String code, String field, String schemaXml, String solrClassName) {
		this.code = code;
		this.field = field;
		this.schemaXml = schemaXml;
		this.solrClassName = solrClassName;
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
		if(solrClassName.equals("solr.BoolField")) {
			booleanFieldText = "# Because this is a Boolean field, you can change these values to something more human-readable\n" +
					"# Either, or both of these properties may be set\n" +
					String.format("#panl.bool.%s.true=is-%s\n", code, field) +
					String.format("#panl.bool.%s.false=is-not-%s\n", code, field);
		}

		return (String.format("\n# %s\n", schemaXml) +
				String.format("panl.facet.%s=%s\n", code, field) +
				String.format("panl.name.%s=%s\n", code, getPrettyName(field)) +
				String.format("panl.type.%s=%s\n", code, solrClassName) +
				"# The following two properties are optional and the values should be changed\n" +
				String.format("#panl.prefix.%s=prefix\n", code) +
				String.format("#panl.suffix.%s=suffix\n", code) +
				booleanFieldText
		);
	}

	public String getCode() {
		return (code);
	}
}
