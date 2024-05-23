package com.synapticloop.panl.server.properties;

import com.synapticloop.panl.util.PropertyHelper;

import java.util.Properties;

public class BaseProperties {
	private final String panlResultsViewerUrl;
	private final String solrjClient;
	private final String solrSearchServerUrl;

	public BaseProperties(Properties properties) {
		this.panlResultsViewerUrl = properties.getProperty("panl.results.viewer.url", "/panl-results-viewer/");
		this.solrjClient = properties.getProperty("solrj.client", "CloudSolrClient");
		this.solrSearchServerUrl = properties.getProperty("solr.search.server.url", "http://localhost:8983/solr");
	}

	public String getPanlResultsViewerUrl() {
		return panlResultsViewerUrl;
	}

	public String getSolrjClient() { return (solrjClient); }

	public String getSolrSearchServerUrl() {
		return (solrSearchServerUrl);
	}
}
