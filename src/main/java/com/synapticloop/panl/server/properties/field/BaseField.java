package com.synapticloop.panl.server.properties.field;

public class BaseField {
	protected static final String PROPERTY_KEY_PANL_FIELD = "panl.field.";
	protected static final String PROPERTY_KEY_PANL_NAME = "panl.name.";

	protected String panlLpseCode;
	protected String panlFieldName;
	protected String solrFieldName;

	public String getPanlLpseCode() {
		return panlLpseCode;
	}

	public String getPanlFieldName() {
		return panlFieldName;
	}

	public String getSolrFieldName() {
		return solrFieldName;
	}
}
