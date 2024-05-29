package com.synapticloop.panl.server.client;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.properties.PanlProperties;
import com.synapticloop.panl.server.properties.CollectionProperties;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;

public class PanlHttp2SolrClient extends PanlClient {
	public PanlHttp2SolrClient(String collectionName, PanlProperties panlProperties, CollectionProperties collectionProperties) throws PanlServerException {
		super(collectionName, panlProperties, collectionProperties);
	}

	@Override
	public SolrClient getClient() {
		return(new Http2SolrClient.Builder(panlProperties.getSolrSearchServerUrl()).build());
	}
}
