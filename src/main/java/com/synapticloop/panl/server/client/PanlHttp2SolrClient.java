package com.synapticloop.panl.server.client;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.BaseProperties;
import com.synapticloop.panl.server.bean.Collection;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;

import java.util.Properties;

public class PanlHttp2SolrClient extends PanlClient {
	public PanlHttp2SolrClient(String collectionName, BaseProperties baseProperties, Properties collectionProperties) throws PanlServerException {
		super(collectionName, baseProperties, collectionProperties);
	}

	@Override
	public SolrClient getClient() {
		return(new Http2SolrClient.Builder(baseProperties.getSolrSearchServerUrl()).build());
	}
}
