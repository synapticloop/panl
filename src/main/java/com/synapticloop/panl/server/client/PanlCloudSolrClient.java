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
 * IN THE SOFTWARE.
 */

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudHttp2SolrClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PanlCloudSolrClient extends PanlClient {
	public static final String PREFIX_ZOOKEPER = "zookeeper:";
	private final List<String> solrUrls = new ArrayList<>();
	private boolean hasZookeeper = false;

	public PanlCloudSolrClient(String solrCollection, PanlProperties panlProperties, CollectionProperties collectionProperties) throws PanlServerException {
		super(solrCollection, panlProperties, collectionProperties);

		String[] urls = panlProperties.getSolrSearchServerUrl().split(",");
		for (String url : urls) {
			if (url.toLowerCase().startsWith(PREFIX_ZOOKEPER)) {
				hasZookeeper = true;
				solrUrls.add(url.substring(PREFIX_ZOOKEPER.length()));
			} else {
				solrUrls.add(url);
			}
		}
	}

	@Override
	public SolrClient getClient() {
		if (hasZookeeper) {
			return (new CloudHttp2SolrClient.Builder(solrUrls, Optional.empty()).build());
		} else {
			return (new CloudHttp2SolrClient.Builder(solrUrls).build());
		}
	}
}
