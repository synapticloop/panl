package com.synapticloop.panl.server.client;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.BaseProperties;
import com.synapticloop.panl.server.bean.Collection;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;

import java.util.Properties;

public class PanlLBHttp2SolrClient extends PanlClient {
	public PanlLBHttp2SolrClient(String collectionName, BaseProperties baseProperties, Properties collectionProperties) throws PanlServerException {
		super(collectionName, baseProperties, collectionProperties);
	}

	@Override
	public SolrClient getClient() {
		return(new HttpJdkSolrClient.Builder(baseProperties.getSolrSearchServerUrl()).build());
	}
}
