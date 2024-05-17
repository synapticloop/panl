package com.synapticloop.panl.generator.bean;

public class Field {
	private final String code;
	private final String field;

	public Field(String code, String field) {
		this.code = code;
		this.field = field;
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
					if(shouldUppercase) {
						sb.append(String.valueOf(c).toUpperCase());
					} else {
						sb.append(c);
					}
					shouldUppercase = false;
			}
		}
		return(sb.toString().trim());
	}

	public String toProperties() {
		return("panl.field." +
				code +
				"=" +
				field +
				"\n" +
				"panl.name." +
				code +
				"=" +
				getPrettyName(field) +
				"\n");
	}

	public String getCode() {
		return (code);
	}
}
