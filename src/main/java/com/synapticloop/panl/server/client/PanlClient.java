package com.synapticloop.panl.server.client;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.properties.BaseProperties;
import com.synapticloop.panl.server.properties.CollectionProperties;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

	/**
	 * <p>Return the solr query string from the URL - i.e. in the normal GET
	 * method for parameters <code>q=search query</code></p>
	 *
	 * <p>The key __MUST__ always be <code>q</code></p>
	 *
	 * @param query The query string
	 *
	 * @return The Solr query with the query set
	 */
	public SolrQuery getQuery(String query) {
		String thisQuery = "*.*";

		for (NameValuePair nameValuePair : URLEncodedUtils.parse(query, StandardCharsets.UTF_8)) {
			if(nameValuePair.getName().equals("q")) {
				thisQuery = nameValuePair.getValue();
				break;
			}
		}
		return(new SolrQuery(thisQuery));
	}
}
