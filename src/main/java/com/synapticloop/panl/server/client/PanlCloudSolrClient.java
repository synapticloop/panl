package com.synapticloop.panl.server.client;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.BaseProperties;
import com.synapticloop.panl.server.bean.Collection;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudHttp2SolrClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PanlCloudSolrClient extends PanlClient {
	private final List<String> solrUrls = new ArrayList<>();

	public PanlCloudSolrClient(String collectionName, BaseProperties baseProperties, Properties collectionProperties) throws PanlServerException {
		super(collectionName, baseProperties, collectionProperties);

		for (String url : baseProperties.getSolrSearchServerUrl().split(",")) {
			solrUrls.add(url);
		}
}

	@Override
	public SolrClient getClient() {
		return(new CloudHttp2SolrClient.Builder(solrUrls).build());
	}
}
