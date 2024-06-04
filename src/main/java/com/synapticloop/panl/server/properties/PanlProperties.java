package com.synapticloop.panl.server.properties;

/*
 * Copyright (c) 2008-2024 synapticloop.
 *
 * https://github.com/synapticloop/panl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
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
