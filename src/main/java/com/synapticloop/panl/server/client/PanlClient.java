package com.synapticloop.panl.server.client;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.BaseProperties;
import com.synapticloop.panl.server.bean.Collection;
import com.synapticloop.panl.util.PropertyHelper;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.rapidoid.http.Req;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class PanlClient {
	public static final Logger LOGGER = LoggerFactory.getLogger(PanlClient.class);
	public static final String PROPERTY_KEY_PANL_RESULTS_FIELDS = "panl.results.fields.";
	protected final String collectionName;
	protected final BaseProperties baseProperties;
	protected final Properties collectionProperties;

	private final Map<String, List<String>> resultFieldsMap = new HashMap<>();

	public PanlClient(String collectionName, BaseProperties baseProperties, Properties collectionProperties) throws PanlServerException {
		this.collectionName = collectionName;
		this.baseProperties = baseProperties;
		this.collectionProperties = collectionProperties;

		// setup the return fields
		List<String> resultFieldProperties = PropertyHelper.getPropertiesStartsWith(collectionProperties, PROPERTY_KEY_PANL_RESULTS_FIELDS);
		for(String resultFieldProperty: resultFieldProperties) {
			addResultsFields(resultFieldProperty.substring(PROPERTY_KEY_PANL_RESULTS_FIELDS.length()), collectionProperties.getProperty(resultFieldProperty));
		}
	}

	private void addResultsFields(String resultFieldsName, String resultFields) throws PanlServerException {
		if(resultFieldsMap.containsKey(resultFieldsName)) {
			throw new PanlServerException("panl.results.fields.'" + resultFieldsName + "' is already defined.");
		}

		LOGGER.info("Adding result fields with key '{}', and fields '{}'.", resultFieldsName, resultFields);
		List<String> fields = new ArrayList<>(Arrays.asList(resultFields.split(",")));
		resultFieldsMap.put(resultFieldsName, fields);
	}

	public abstract SolrClient getClient();

	public SolrQuery getQuery(Req req) {
		// If we have a query - then this will override any other things

		String query = req.query();
//		if(null == query || query.isBlank()) {
//			query = "*.*";
//		}


		SolrQuery solrQuery = new SolrQuery("*.*");
//		solrQuery.addField();
		return solrQuery;
	}
}
