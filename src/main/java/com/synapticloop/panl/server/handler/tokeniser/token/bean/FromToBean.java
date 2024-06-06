package com.synapticloop.panl.server.handler.tokeniser.token.bean;

public class FromToBean {
	private final String from;
	private final String to;

	public FromToBean(String from, String to) {
		this.from = from;
		this.to = to;
	}

	public String getFromValue() {
		return from;
	}

	public String getToValue() {
		return to;
	}
}
