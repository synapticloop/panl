package com.synapticloop.panl.generator.bean;

public class PanlProperty {
	private final String panlProperty;
	private final String panlValue;
	private final boolean hideProperty;
	private String solrClassName;

	public PanlProperty(String panlProperty, String panlValue, int lpseNum) {
		this.panlProperty = panlProperty;

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < lpseNum; i ++) {
			sb.append(panlValue);
		}
		this.panlValue = sb.toString();
		this.hideProperty = false;
	}

	public PanlProperty(String panlProperty, String panlValue) {
		this.panlProperty = panlProperty;
		this.panlValue = panlValue;
		this.hideProperty = false;
	}

	public PanlProperty(String panlProperty, String panlValue, boolean hideProperty) {
		this.panlProperty = panlProperty;
		this.panlValue = panlValue;
		this.hideProperty = hideProperty;
	}

	public String getPanlValue() {
		return(panlValue);
	}

	public String toProperties() {
		if(hideProperty) {
			return(panlValue);
		} else {
			return (panlProperty + "=" + panlValue);
		}
	}

	public void setSolrClassName(String solrClassName) {
		this.solrClassName = solrClassName;
	}
}
