package com.synapticloop.panl.server.properties;

import com.synapticloop.panl.util.PropertyHelper;

import java.util.Properties;

public class BaseProperties {
	private final boolean facetEnabled;
	private final int facetMinCount;
	private final int resultRows;
	private final String panlResultsViewerUrl;
	private final String solrjClient;
	private final String solrSearchServerUrl;

	public BaseProperties(Properties properties) {
		this.facetEnabled = properties.getProperty("solr.facet.enabled", "true").equals("true");
		this.facetMinCount = PropertyHelper.getIntProperty(properties, "solr.facet.min.count", 1);
		this.resultRows = PropertyHelper.getIntProperty(properties, "solr.rows", 10);
		this.panlResultsViewerUrl = properties.getProperty("panl.results.viewer.url", "/panl-results-viewer/");
		this.solrjClient = properties.getProperty("solrj.client", "CloudSolrClient");
		this.solrSearchServerUrl = properties.getProperty("solr.search.server.url", "http://localhost:8983/solr");
	}



	public boolean getFacetEnabled() {
		return facetEnabled;
	}

	public int getFacetMinCount() {
		return facetMinCount;
	}

	public int getResultRows() {
		return resultRows;
	}

	public String getPanlResultsViewerUrl() {
		return panlResultsViewerUrl;
	}

	public String getSolrjClient() { return (solrjClient); }

	public String getSolrSearchServerUrl() {
		return (solrSearchServerUrl);
	}
}
