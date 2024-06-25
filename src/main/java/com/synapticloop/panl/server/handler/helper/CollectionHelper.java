package com.synapticloop.panl.server.handler.helper;

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

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.client.*;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionHelper.class);

	public static PanlClient getPanlClient(String solrJClient, String solrCollection, PanlProperties panlProperties, CollectionProperties collectionProperties) throws PanlServerException {
		LOGGER.info("[ Solr collection '{}' ] Looking up solrjClient of '{}'", solrCollection, solrJClient);

		switch (solrJClient) {
			case "Http2SolrClient":
				return (new PanlHttp2SolrClient(solrCollection, panlProperties, collectionProperties));
			case "HttpJdkSolrClient":
				return (new PanlHttpSolrClient(solrCollection, panlProperties, collectionProperties));
			case "LBHttp2SolrClient":
				return (new PanlLBHttp2SolrClient(solrCollection, panlProperties, collectionProperties));
			case "CloudSolrClient":
				return (new PanlCloudSolrClient(solrCollection, panlProperties, collectionProperties));
			default:
				throw new PanlServerException("Unknown property value for 'solrj.client' of '" + solrJClient + "', available values are 'Http2SolrClient', 'HttpJdkSolrClient', 'LBHttp2SolrClient', or 'CloudSolrClient'.");
		}

	}
}
