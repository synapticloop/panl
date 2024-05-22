package com.synapticloop.panl.server;

import java.util.Properties;

public class BaseProperties {
	private final boolean facetEnabled;
	private final int facetMinCount;
	private final int resultRows;
	private final String panlResultsViewerUrl;
	public BaseProperties(Properties properties) {
		facetEnabled = properties.getProperty("solr.facet.enabled", "true").equals("true");
		facetMinCount = getIntProperty(properties, "solr.facet.min.count", 1);
		resultRows = getIntProperty(properties, "solr.rows", 10);
		panlResultsViewerUrl = properties.getProperty("panl.results.viewer.url", "/panl-results-viewer/");
	}

	private int getIntProperty(Properties properties, String key, int defaultValue) {
		try {
			return(Integer.parseInt(properties.getProperty(key, defaultValue + "")));
		} catch(NumberFormatException e) {
			return(defaultValue);
		}
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
}
