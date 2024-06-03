package com.synapticloop.panl.server.properties.field;

import com.synapticloop.panl.exception.PanlServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class FacetFieldPanl extends PanlBaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacetFieldPanl.class);


	public FacetFieldPanl(String panlFacetProperty, Properties properties, String collectionName, int panlLpseNum) throws PanlServerException {
		super(panlFacetProperty, properties, collectionName, panlLpseNum);
	}

	@Override
	public Logger getLogger() {
		return(LOGGER);
	}
}
