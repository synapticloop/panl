package com.synapticloop.panl.server.client;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.properties.BaseProperties;
import com.synapticloop.panl.server.properties.CollectionProperties;
import org.apache.http.HttpRequest;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class PanlClient {
	public static final Logger LOGGER = LoggerFactory.getLogger(PanlClient.class);
	public static final String PROPERTY_KEY_PANL_RESULTS_FIELDS = "panl.results.fields.";
	protected final String collectionName;
	protected final BaseProperties baseProperties;
	protected final CollectionProperties collectionProperties;

	public PanlClient(String collectionName, BaseProperties baseProperties, CollectionProperties collectionProperties) throws PanlServerException {
		this.collectionName = collectionName;
		this.baseProperties = baseProperties;
		this.collectionProperties = collectionProperties;
	}

	public abstract SolrClient getClient();

	public SolrQuery getQuery(String query) {
		String thisQuery = "*.*";
		if(!query.isBlank()) {
			String[] splits = query.split("=");
			if(splits.length == 2) {
				thisQuery = splits[1];
			}
		}

		return(new SolrQuery(thisQuery));
	}
}
