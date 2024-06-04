package com.synapticloop.panl.server.properties.field;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
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

	@Override public String getExplainDescription() {
		return("The text query which maps to the 'q' parameter of Solr.");
	}

	public void applyToQueryInternal(SolrQuery solrQuery, Map<String, List<LpseToken>> panlTokenMap) {
		if(panlTokenMap.containsKey(panlLpseCode)) {
			LpseToken lpseToken = panlTokenMap.get(panlLpseCode).get(0);
			solrQuery.setQuery(lpseToken.getValue());
		}
	}

}
