package com.synapticloop.panl.server.properties;

import java.util.Properties;

public class PanlProperties {
	private final boolean panlResultsViewerUrl;
	private final String solrjClient;
	private final String solrSearchServerUrl;

	public PanlProperties(Properties properties) {
		this.panlResultsViewerUrl = properties.getProperty("panl.results.viewer.url", "false").equals("true");
		// TODO - WARN error logging message
		this.solrjClient = properties.getProperty("solrj.client", "CloudSolrClient");
		// TODO - WARN error logging message
		this.solrSearchServerUrl = properties.getProperty("solr.search.server.url", "http://localhost:8983/solr");
	}

	public boolean getPanlResultsViewerUrl() {
		return panlResultsViewerUrl;
	}

	public String getSolrjClient() { return (solrjClient); }

	public String getSolrSearchServerUrl() {
		return (solrSearchServerUrl);
	}
}
