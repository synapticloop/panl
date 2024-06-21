package com.synapticloop.panl.server.handler.tokeniser.token.facet.bean;

public class PreviousNextValueBean {
	private final String previousNext;
	private final String designator;
	private final String value;

	public PreviousNextValueBean(String previousNext, String designator, String value) {
		this.previousNext = previousNext;
		this.designator = designator;
		this.value = value;
	}

	public String getPreviousNext() {
		return previousNext;
	}

	public String getDesignator() {
		return designator;
	}

	public String getValue() {
		return value;
	}
}
