package com.synapticloop.panl.server.properties.field;

import com.synapticloop.panl.exception.PanlServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class PanlNumRowsField extends PanlBaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlNumRowsField.class);

	public PanlNumRowsField(String panlFacetProperty, Properties properties, String collectionName) throws PanlServerException {
		super(panlFacetProperty, properties, collectionName);
	}

	@Override
	public Logger getLogger() {
		return(LOGGER);
	}

	@Override
	public String getConvertedToPanlValue(String value) {
		return "";
	}

	@Override
	public String getConvertedFromPanlValue(String value) {
		return "";
	}

	public String getURIPathComponent(String value) {
		return("");
	}

	public String getLpsePathComponent(String value) {
		return("");
	}

}
