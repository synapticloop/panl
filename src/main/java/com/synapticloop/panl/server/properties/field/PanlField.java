package com.synapticloop.panl.server.properties.field;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PanlField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlField.class);

	public PanlField(String lpseCode, String propertyKey, Properties properties, String collectionName, int panlLpseNum) throws PanlServerException {
		super(lpseCode, propertyKey, collectionName, panlLpseNum);

		// fields don't have prefixes/suffixes or URIparts
		populatePanlAndSolrFieldNames(properties, lpseCode);
	}


	@Override
	public String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return ("");
	}

	@Override
	public String getCanonicalLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return ("");
	}

	@Override
	public Logger getLogger() {
		return (LOGGER);
	}

	@Override public String getExplainDescription() {
		return("A Solr field that can be configured to be sorted by, or returned in the field set.");
	}

}
