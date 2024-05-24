package com.synapticloop.panl.server.handler;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.client.*;
import com.synapticloop.panl.server.properties.BaseProperties;
import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.field.BaseField;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>The collection request handler acts as the intermediary between the
 * incoming http request and the underlying SOLR server.</p>
 *
 * @author synapticloop
 */
public class CollectionRequestHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionRequestHandler.class);

	private final String collectionName;
	private final CollectionProperties collectionProperties;
	private final PanlClient panlClient;

	private final String validUrls = "";

	public CollectionRequestHandler(String collectionName, BaseProperties baseProperties, CollectionProperties collectionProperties) throws PanlServerException {
		this.collectionName = collectionName;
		this.collectionProperties = collectionProperties;

		LOGGER.info("[{}] Initialising collection", collectionName);

		String solrjClient = baseProperties.getSolrjClient();
		LOGGER.info("[{}] Utilising solrjClient of '{}'", collectionName, solrjClient);

		switch(solrjClient) {
			case "Http2SolrClient":
				panlClient = new PanlHttp2SolrClient(collectionName, baseProperties, collectionProperties);
				break;
			case "HttpJdkSolrClient":
				panlClient = new PanlHttpJdkSolrClient(collectionName, baseProperties, collectionProperties);
				break;
			case "LBHttp2SolrClient":
				panlClient = new PanlLBHttp2SolrClient(collectionName, baseProperties, collectionProperties);
				break;
			case "CloudSolrClient":
				panlClient = new PanlCloudSolrClient(collectionName, baseProperties, collectionProperties);
				break;
			default:
				throw new PanlServerException("Unknown property value for 'solrj.client' of '" + solrjClient + "', available values are 'Http2SolrClient', 'HttpJdkSolrClient', 'LBHttp2SolrClient', or 'CloudSolrClient'.");
		}
	}


	public String getCollectionName() {
		return collectionName;
	}

	public List<String> getResultFieldsNames() {
		return(new ArrayList<>(collectionProperties.getResultFieldsNames()));
	}


	public String request(String uri, String query) throws PanlServerException {

		String[] searchQuery = uri.split("/");
		String resultFields = searchQuery[2];

		LpseHandler lpseHandler = new LpseHandler(collectionProperties.getLpseFields(), collectionProperties.getPanlLpseNum());
		lpseHandler.populate(uri, query);



		try(SolrClient solrClient = panlClient.getClient()) {
			SolrQuery solrQuery = panlClient.getQuery(query);
			for (String fieldName : collectionProperties.getResultFieldsForName(resultFields)) {
				solrQuery.addField(fieldName);
			}

			solrQuery.setFacetMinCount(collectionProperties.getFacetMinCount());
			solrQuery.setRows(collectionProperties.getResultRows());

			solrQuery.addFacetField(collectionProperties.getFacetFields());

			// now we need to go through the panl facets and add them


			final QueryResponse response = solrClient.query(this.collectionName, solrQuery);
			return(parseResponse(response));
		} catch (IOException | SolrServerException e) {
			throw new PanlServerException("Could not query the Solr instance.", e);
		}
	}

	/**
	 * <p>Parse the solrj response and add the panl information to it</p>
	 *
	 * @param response The Solrj response to be parsed
	 * @return a JSON Object as a string with the appended panl response
	 */
	private String parseResponse(QueryResponse response) {

		JSONObject solrJsonObject = new JSONObject(response.jsonStr());

		// now we are going to add all panl facets as well
		JSONObject panlObject = new JSONObject();
		panlObject.put("one", "two");
		solrJsonObject.append("panl", panlObject);

		return (solrJsonObject.toString());
	}

	public String getValidUrlsJSON() {
		return(collectionProperties.getValidUrlsJson());
	}

	public boolean isValidResultsFields(String path) {
		return(collectionProperties.isValidResultFieldsName(path));
	}
}
