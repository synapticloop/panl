package com.synapticloop.panl.server.handler.properties;

/*
 * Copyright (c) 2008-2024 synapticloop.
 *
 * https://github.com/synapticloop/panl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 *  IN THE SOFTWARE.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class PanlProperties {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlProperties.class);

	public static final String PROPERTY_KEY_PANL_RESULTS_TESTING_URLS = "panl.results.testing.urls";
	public static final String PROPERTY_KEY_SOLRJ_CLIENT = "solrj.client";
	public static final String PROPERTY_KEY_SOLR_SEARCH_SERVER_URL = "solr.search.server.url";
	public static final String PROPERTY_KEY_PANL_STATUS_404_VERBOSE = "panl.status.404.verbose";
	public static final String PROPERTY_KEY_PANL_STATUS_500_VERBOSE = "panl.status.500.verbose";

	public static final String DEFAULT_CLOUD_SOLR_CLIENT = "CloudSolrClient";
	public static final String DEFAULT_SOLR_URL = "http://localhost:8983/solr";
	public static final String DEFAULT_FALSE = "false";
	public static final String DEFAULT_TRUE = "true";

	private final String solrjClient;
	private final String solrSearchServerUrl;

	private final boolean hasPanlResultsTestingUrls;
	private final boolean panlStatus404Verbose;
	private final boolean panlStatus500Verbose;

	/**
	 * <p>Instantiate the Panl properties which defines what Solr server to
	 * connect to, the SolrJ client, whether to use verbose messaging for 404
	 * and/or 500 error messages, and whether to serve the Panl results testing
	 * URL handlers.</p>
	 *
	 * @param properties The properties file
	 */
	public PanlProperties(Properties properties) {
		this.hasPanlResultsTestingUrls = properties.getProperty(PROPERTY_KEY_PANL_RESULTS_TESTING_URLS, DEFAULT_FALSE).equals(DEFAULT_TRUE);

		String solrjClientTemp;
		solrjClientTemp = properties.getProperty(PROPERTY_KEY_SOLRJ_CLIENT, null);
		if(solrjClientTemp == null) {
			LOGGER.warn("Property '{}' could not be found, defaulting to '{}'", PROPERTY_KEY_SOLRJ_CLIENT, DEFAULT_CLOUD_SOLR_CLIENT);
			solrjClientTemp = DEFAULT_CLOUD_SOLR_CLIENT;
		}

		this.solrjClient = solrjClientTemp;

		String solrSearchServerUrlTemp;
		solrSearchServerUrlTemp = properties.getProperty(PROPERTY_KEY_SOLR_SEARCH_SERVER_URL, null);
		if(solrSearchServerUrlTemp == null) {
			LOGGER.warn("Property '{}' could not be found, defaulting to '{}'", PROPERTY_KEY_SOLR_SEARCH_SERVER_URL, DEFAULT_SOLR_URL);
			solrSearchServerUrlTemp = DEFAULT_SOLR_URL;
		}

		this.solrSearchServerUrl = solrSearchServerUrlTemp;
		this.panlStatus404Verbose = properties.getProperty(PROPERTY_KEY_PANL_STATUS_404_VERBOSE, DEFAULT_FALSE).equals(DEFAULT_TRUE);
		this.panlStatus500Verbose = properties.getProperty(PROPERTY_KEY_PANL_STATUS_500_VERBOSE, DEFAULT_FALSE).equals(DEFAULT_TRUE);
	}

	/**
	 * <p>Return whether the Panl results testing URLs are available to service
	 * requests.  This should probably not be set 'true' for production
	 * deployments, or at the very least, not allowed to be accessed externally.</p>
	 *
	 * @return Whether the Panl results testing URL(s) are available
	 */
	public boolean getHasPanlResultsTestingUrls() {
		return hasPanlResultsTestingUrls;
	}

	/**
	 * <p>Return the configured SolrJ client.</p>
	 *
	 * @return The SolrJ client to use
	 */
	public String getSolrjClient() { return (solrjClient); }

	/**
	 * <p>Return the Solr server URL(s).  If there is more than one URL, this
	 * will be a comma separated string of URLs</p>
	 *
	 * @return The Solr server URL(s)
	 */
	public String getSolrSearchServerUrl() {
		return (solrSearchServerUrl);
	}

	/**
	 * <p>Whether to use verbose 404 messages.</p>
	 *
	 * @return Whether to return verbose 404 messages
	 */
	public boolean getUseVerbose404Messages() {
		return (panlStatus404Verbose);
	}

	/**
	 * <p>Whether to use verbose 500 messages.</p>
	 *
	 * @return Whether to return verbose 500 messages
	 */
	public boolean getUseVerbose500Messages() {
		return (panlStatus500Verbose);
	}
}
