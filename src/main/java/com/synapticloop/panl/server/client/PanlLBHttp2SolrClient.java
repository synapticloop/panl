package com.synapticloop.panl.server.client;

import com.synapticloop.panl.server.bean.Collection;

import java.util.Properties;

public class PanlLBHttp2SolrClient extends PanlClient {
	public PanlLBHttp2SolrClient(Properties properties, Collection collection) {
		super(properties, collection);
	}

	@Override
	public PanlClient getClient() {
		return null;
	}
}
