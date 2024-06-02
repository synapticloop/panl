package com.synapticloop.panl.generator.bean;

public class PanlProperty {
	private final String panlPropertyName;
	private final String panlPropertyValue;
	private final boolean hideProperty;
	private String solrClassName;

	public PanlProperty(String panlPropertyName, String panlPropertyValue, int lpseNum) {
		this.panlPropertyName = panlPropertyName;

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < lpseNum; i ++) {
			sb.append(panlPropertyValue);
		}
		this.panlPropertyValue = sb.toString();
		this.hideProperty = false;
	}

	public PanlProperty(String panlPropertyName, String panlPropertyValue) {
		this.panlPropertyName = panlPropertyName;
		this.panlPropertyValue = panlPropertyValue;
		this.hideProperty = false;
	}

	public PanlProperty(String panlPropertyName, String panlPropertyValue, boolean hideProperty) {
		this.panlPropertyName = panlPropertyName;
		this.panlPropertyValue = panlPropertyValue;
		this.hideProperty = hideProperty;
	}

	public String getPanlPropertyValue() {
		return(panlPropertyValue);
	}

	public String toProperties() {
		if(hideProperty) {
			return(panlPropertyValue);
		} else {
			return (panlPropertyName + "=" + panlPropertyValue);
		}
	}

	public void setSolrClassName(String solrClassName) {
		this.solrClassName = solrClassName;
	}
}
