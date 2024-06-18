package com.synapticloop.panl.server.handler.fielderiser.field.validator;

public class NumberValidator implements Validator {
	@Override public String validate(String value) {
		String replaced = value.replaceAll("[^0-9]", "");
		if (replaced.isBlank()) {
			return (null);
		} else {
			return replaced;
		}
	}
}
