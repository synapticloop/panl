package com.synapticloop.panl.server.client;

import com.synapticloop.panl.server.bean.Collection;

import java.util.Properties;

public abstract class PanlClient {
	private final Properties properties;
	private final Collection collection;

	public PanlClient(Properties properties, Collection collection) {
		this.properties = properties;
		this.collection = collection;
	}

	public abstract PanlClient getClient();
}
