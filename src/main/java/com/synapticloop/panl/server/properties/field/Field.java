package com.synapticloop.panl.server.properties.field;

import com.synapticloop.panl.exception.PanlServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class Field extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(Field.class);

	public Field(String panlFacetProperty, Properties properties, String collectionName, int panlLpseNum) throws PanlServerException {
		this.solrFieldName = properties.getProperty(panlFacetProperty);
		this.panlLpseCode = panlFacetProperty.substring(PROPERTY_KEY_PANL_FIELD.length());

		if (panlLpseCode.length() != panlLpseNum) {
			throw new PanlServerException(PROPERTY_KEY_PANL_FIELD + panlLpseCode + " property key is of invalid length - should be " + panlLpseNum);
		}

		LOGGER.info("[{}] Mapping Solr field named '{}' to panl key '{}'", collectionName, solrFieldName, panlLpseCode);

		String panlFieldNameTemp = properties.getProperty(PROPERTY_KEY_PANL_NAME + panlLpseCode, null);
		if (null == panlFieldNameTemp) {
			LOGGER.warn("[{}] Could not find a name for Panl field LPSE code '{}', using Solr field name '{}'", collectionName, panlLpseCode, solrFieldName);
			this.panlFieldName = solrFieldName;
		} else {
			this.panlFieldName = panlFieldNameTemp;
			LOGGER.info("[{}] Found a name for Panl field LPSE code '{}', using '{}'", collectionName, panlLpseCode, panlFieldName);
		}
	}
}
