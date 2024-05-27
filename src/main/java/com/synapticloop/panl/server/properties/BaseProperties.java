package com.synapticloop.panl.server.properties;

import java.util.Properties;

public class BaseProperties {
	private final String panlResultsViewerUrl;
	private final String solrjClient;
	private final String solrSearchServerUrl;

	public BaseProperties(Properties properties) {
		// this property does not need to be set;

		String panlResultsViewerUrlTemp = properties.getProperty("panl.results.viewer.url", null);
		if(null != panlResultsViewerUrlTemp) {
			if(!panlResultsViewerUrlTemp.endsWith("*")) {
				this.panlResultsViewerUrl = panlResultsViewerUrlTemp + "*";
			} else {
				this.panlResultsViewerUrl = panlResultsViewerUrlTemp;
			}
		} else {
			this.panlResultsViewerUrl = null;
		}

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
