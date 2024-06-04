package com.synapticloop.panl.server.properties.field;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.tokeniser.token.NumRowsLpseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PanlNumRowsField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlNumRowsField.class);

	public PanlNumRowsField(String lpseCode, String propertyKey, Properties properties, String collectionName) throws PanlServerException {
		super(lpseCode, propertyKey, collectionName);

		populateParamSuffixAndPrefix(properties, propertyKey);
	}

	@Override
	public Logger getLogger() {
		return (LOGGER);
	}

	@Override
	public String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder();
		if (panlTokenMap.containsKey(panlLpseCode)) {
			NumRowsLpseToken numRowsLpseToken = (NumRowsLpseToken) panlTokenMap.get(panlLpseCode).get(0);
			sb.append(getEncodedPanlValue(Integer.toString(numRowsLpseToken.getNumRows())));
		} else {
			sb.append(getEncodedPanlValue(Integer.toString(collectionProperties.getNumResultsPerPage())));
		}

		sb.append("/");
		return (sb.toString());
	}

	@Override public String getCanonicalLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return(panlLpseCode);
	}
}
