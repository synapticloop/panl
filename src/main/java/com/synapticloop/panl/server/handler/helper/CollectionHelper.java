package com.synapticloop.panl.server.handler.helper;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.client.*;
import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.properties.PanlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionHelper.class);

	public static PanlClient getPanlClient(String solrJClient, String collectionName, PanlProperties panlProperties, CollectionProperties collectionProperties) throws PanlServerException {
		LOGGER.info("[{}] Looking up solrjClient of '{}'", collectionName, solrJClient);

		switch (solrJClient) {
			case "Http2SolrClient":
				return (new PanlHttp2SolrClient(collectionName, panlProperties, collectionProperties));
			case "HttpJdkSolrClient":
				return (new PanlHttpJdkSolrClient(collectionName, panlProperties, collectionProperties));
			case "LBHttp2SolrClient":
				return (new PanlLBHttp2SolrClient(collectionName, panlProperties, collectionProperties));
			case "CloudSolrClient":
				return (new PanlCloudSolrClient(collectionName, panlProperties, collectionProperties));
			default:
				throw new PanlServerException("Unknown property value for 'solrj.client' of '" + solrJClient + "', available values are 'Http2SolrClient', 'HttpJdkSolrClient', 'LBHttp2SolrClient', or 'CloudSolrClient'.");
		}

	}
}
