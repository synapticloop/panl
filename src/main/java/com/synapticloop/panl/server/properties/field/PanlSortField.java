package com.synapticloop.panl.server.properties.field;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.tokeniser.token.QueryOperandLpseToken;
import com.synapticloop.panl.server.tokeniser.token.SortLpseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PanlSortField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlSortField.class);

	public PanlSortField(String lpseCode, String propertyKey, Properties properties, String collectionName) throws PanlServerException {
		super(lpseCode, propertyKey, collectionName);
	}

	@Override public String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return("");
	}

	@Override public String getCanonicalLpsePath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder(panlLpseCode);
		if(panlTokenMap.containsKey(panlLpseCode)) {
			SortLpseToken lpseToken = (SortLpseToken) panlTokenMap.get(panlLpseCode).get(0);
			if(lpseToken.getIsValid()) {
				sb.append(lpseToken.getPanlFacetCode());
				sb.append(lpseToken.getSortCode());
			} else {
				sb.append("-");
			}
		} else {
			sb.append("-");
		}
		return(sb.toString());
	}

	@Override public Logger getLogger() {
		return(LOGGER);
	}

}
