package com.synapticloop.panl.server.properties.field;

import com.synapticloop.panl.exception.PanlServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class PanlQueryField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlQueryField.class);

	public PanlQueryField(String lpseCode, String propertyKey, Properties properties, String collectionName) throws PanlServerException {
		super(lpseCode, propertyKey, collectionName);
	}

	@Override
	public Logger getLogger() {
		return(LOGGER);
	}

}
