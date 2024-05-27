package com.synapticloop.panl.server.client;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.properties.BaseProperties;
import com.synapticloop.panl.server.properties.CollectionProperties;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudHttp2SolrClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PanlCloudSolrClient extends PanlClient {
	private final List<String> solrUrls = new ArrayList<>();

	public PanlCloudSolrClient(String collectionName, BaseProperties baseProperties, CollectionProperties collectionProperties) throws PanlServerException {
		super(collectionName, baseProperties, collectionProperties);

		Collections.addAll(solrUrls, baseProperties.getSolrSearchServerUrl().split(","));
}

	@Override
	public SolrClient getClient() {
		return(new CloudHttp2SolrClient.Builder(solrUrls).build());
	}
}
