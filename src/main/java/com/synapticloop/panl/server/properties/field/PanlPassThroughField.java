package com.synapticloop.panl.server.properties.field;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PanlPassThroughField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlPassThroughField.class);

	public PanlPassThroughField(String lpseCode, String propertyKey, Properties properties, String collectionName) throws PanlServerException {
		super(lpseCode, propertyKey, collectionName);

		populateParamSuffixAndPrefix(properties, propertyKey);
	}

	@Override
	public String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return ("");
	}

	@Override
	public String getCanonicalLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return ("");
	}

	@Override public List<String> explain() {
		List<String> temp = new ArrayList<>();
		temp.add("FIELD CONFIG [ " +
				this.getClass().getSimpleName() +
				" ] LPSE code '" +
				panlLpseCode +
				"'.");

		return (temp);
	}

	@Override public Logger getLogger() {
		return (LOGGER);
	}

	@Override public String getExplainDescription() {
		return ("This field is ignored by the Panl server and is not passed through to Solr.");
	}

	public void applyToQueryInternal(SolrQuery solrQuery, Map<String, List<LpseToken>> panlTokenMap) {
		// do nothing
	}

}