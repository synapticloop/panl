package com.synapticloop.panl.server.bean;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.BaseProperties;
import com.synapticloop.panl.server.client.*;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.rapidoid.http.Req;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Collection {
	private static final Logger LOGGER = LoggerFactory.getLogger(Collection.class);

	private final String collectionName;
	private final Properties collectionProperties;
	private final PanlClient panlClient;

	public Collection(String collectionName, BaseProperties baseProperties, Properties collectionProperties) throws PanlServerException {
		this.collectionName = collectionName;
		this.collectionProperties = collectionProperties;

		String solrjClient = baseProperties.getSolrjClient();
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


	public String request(Req req) throws PanlServerException {
		try(SolrClient solrClient = panlClient.getClient()) {
			SolrQuery solrQuery = panlClient.getQuery(req);
			final QueryResponse response = solrClient.query(collectionName, solrQuery);
			return(parseResponse(response));
		} catch (IOException | SolrServerException e) {
			throw new PanlServerException("Could not query the Solr instance.", e);
		}
	}

	private String parseResponse(QueryResponse response) {
		String jsonStr = response.jsonStr();
		System.out.println(jsonStr);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("hello", "baby");
		jsonObject.put("one", 3);
		// now we are going to add all panl facets as well

		return (jsonObject.toString());
	}

}
