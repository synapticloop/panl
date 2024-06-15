package com.synapticloop.panl.server.client;

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

import com.synapticloop.panl.server.handler.properties.PanlProperties;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public abstract class PanlClient {
	public static final Logger LOGGER = LoggerFactory.getLogger(PanlClient.class);

	protected final String solrCollection;
	protected final PanlProperties panlProperties;
	protected final CollectionProperties collectionProperties;

	public PanlClient(String solrCollection, PanlProperties panlProperties, CollectionProperties collectionProperties) {
		this.solrCollection = solrCollection;
		this.panlProperties = panlProperties;
		this.collectionProperties = collectionProperties;
	}

	public abstract SolrClient getClient();

	/**
	 * <p>Return the solr query string from the URL - i.e. in the normal GET
	 * method for parameters <code>q=search query</code></p>
	 *
	 * <p>The key __MUST__ always be <code>q</code></p>
	 *
	 * @param query The query string
	 *
	 * @return The Solr query with the query set
	 */
	public SolrQuery getQuery(String query) {
		String thisQuery = "*:*";

		for (NameValuePair nameValuePair : URLEncodedUtils.parse(query, StandardCharsets.UTF_8)) {
			// TODO - make this a configured value ???
			if(nameValuePair.getName().equals("q")) {
				thisQuery = nameValuePair.getValue();
				break;
			}
		}
		return(new SolrQuery(thisQuery));
	}
}
