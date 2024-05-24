package com.synapticloop.panl.server.handler.field;

public abstract class BaseField {
	protected final String panlCode;

	protected BaseField(String panlCode) {
		this.panlCode = panlCode;
	}
}
