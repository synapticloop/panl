package com.synapticloop.panl.server.client;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.properties.PanlProperties;
import com.synapticloop.panl.server.properties.CollectionProperties;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudHttp2SolrClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PanlCloudSolrClient extends PanlClient {
	private final List<String> solrUrls = new ArrayList<>();

	public PanlCloudSolrClient(String collectionName, PanlProperties panlProperties, CollectionProperties collectionProperties) throws PanlServerException {
		super(collectionName, panlProperties, collectionProperties);

		Collections.addAll(solrUrls, panlProperties.getSolrSearchServerUrl().split(","));
}

	@Override
	public SolrClient getClient() {
		return(new CloudHttp2SolrClient.Builder(solrUrls).build());
	}
}
