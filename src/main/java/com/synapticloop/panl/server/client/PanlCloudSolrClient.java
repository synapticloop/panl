package com.synapticloop.panl.server.client;

import com.synapticloop.panl.server.bean.Collection;

import java.util.Properties;

public class PanlCloudSolrClient extends PanlClient {
	public PanlCloudSolrClient(Properties properties, Collection collection) {
		super(properties, collection);
	}

	@Override
	public PanlClient getClient() {
		return null;
	}
}
