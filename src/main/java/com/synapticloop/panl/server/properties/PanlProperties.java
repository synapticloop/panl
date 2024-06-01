package com.synapticloop.panl.server.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class PanlProperties {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlProperties.class);

	private final boolean panlResultsTestingUrls;
	private final String solrjClient;
	private final String solrSearchServerUrl;
	private final boolean panlStatus404Verbose;
	private final boolean panlStatus500Verbose;

	public PanlProperties(Properties properties) {
		this.panlResultsTestingUrls = properties.getProperty("panl.results.testing.urls", "false").equals("true");
		// TODO - WARN error logging message
		this.solrjClient = properties.getProperty("solrj.client", "CloudSolrClient");
		// TODO - WARN error logging message
		this.solrSearchServerUrl = properties.getProperty("solr.search.server.url", "http://localhost:8983/solr");
		this.panlStatus404Verbose = properties.getProperty("panl.status.404.verbose", "false").equals("true");
		this.panlStatus500Verbose = properties.getProperty("panl.status.500.verbose", "false").equals("true");
	}

	public boolean getPanlResultsTestingUrls() {
		return panlResultsTestingUrls;
	}

	public String getSolrjClient() { return (solrjClient); }

	public String getSolrSearchServerUrl() {
		return (solrSearchServerUrl);
	}

	public boolean getPanlStatus404Verbose() {
		return (panlStatus404Verbose);
	}

	public boolean getPanlStatus500Verbose() {
		return (panlStatus500Verbose);
	}
}
