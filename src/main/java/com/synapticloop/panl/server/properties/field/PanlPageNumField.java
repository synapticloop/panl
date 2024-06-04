package com.synapticloop.panl.server.properties.field;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.tokeniser.token.PageLpseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PanlPageNumField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlPageNumField.class);

	public PanlPageNumField(String lpseCode, String propertyKey, Properties properties, String collectionName) throws PanlServerException {
		super(lpseCode, propertyKey, collectionName);

		populateParamSuffixAndPrefix(properties, propertyKey);
	}

	@Override public String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder();
		if(panlTokenMap.containsKey(panlLpseCode)) {
			PageLpseToken pageLpseToken = (PageLpseToken) panlTokenMap.get(panlLpseCode).get(0);
			sb.append(getEncodedPanlValue(Integer.toString(pageLpseToken.getPageNum())));
		} else {
			sb.append(getEncodedPanlValue("1"));
		}

		sb.append("/");
		return(sb.toString());
	}

	@Override public String getCanonicalLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return(panlLpseCode);
	}

	public String getResetUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder();
		if(panlTokenMap.containsKey(panlLpseCode)) {
			sb.append(getEncodedPanlValue("1"));
			sb.append("/");
		}

		return(sb.toString());
	}

	public String getResetLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		if(panlTokenMap.containsKey(panlLpseCode)) {
			return(panlLpseCode);
		}
		return("");
	}

	@Override public Logger getLogger() {
		return(LOGGER);
	}

	@Override public String getExplainDescription() {
		return("The page number of the results (works in conjunction with the number of results).");
	}

}
