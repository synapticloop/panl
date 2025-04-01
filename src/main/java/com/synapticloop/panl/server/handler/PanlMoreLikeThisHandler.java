package com.synapticloop.panl.server.handler;

/*
 * Copyright (c) 2008-2025 synapticloop.
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

import com.synapticloop.panl.exception.PanlNotFoundException;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.client.PanlClient;
import com.synapticloop.panl.server.handler.helper.CollectionHelper;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import com.synapticloop.panl.server.handler.webapp.util.ResourceHelper;
import com.synapticloop.panl.util.Constants;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * <p>This is the handler which will return more the 'More Like This' Solr
 * query.</p>
 *
 *
 * @author Synapticloop
 */
public class PanlMoreLikeThisHandler extends BaseResponseHandler implements HttpRequestHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlMoreLikeThisHandler.class);

	private static final Set<String> ALLOWABLE_PARAMETERS = new HashSet<>();
	static {
		ALLOWABLE_PARAMETERS.add("mlt.fl");
		ALLOWABLE_PARAMETERS.add("mlt.mintf");
		ALLOWABLE_PARAMETERS.add("mlt.mindf");
		ALLOWABLE_PARAMETERS.add("mlt.maxdf");
		ALLOWABLE_PARAMETERS.add("mlt.maxdfpct");
		ALLOWABLE_PARAMETERS.add("mlt.minwl");
		ALLOWABLE_PARAMETERS.add("mlt.maxwl");
		ALLOWABLE_PARAMETERS.add("mlt.maxqt");
		ALLOWABLE_PARAMETERS.add("mlt.maxntp");
		ALLOWABLE_PARAMETERS.add("mlt.boost");
		ALLOWABLE_PARAMETERS.add("mlt.qf");
		ALLOWABLE_PARAMETERS.add("mlt.interestingTerms");
		ALLOWABLE_PARAMETERS.add("mlt.match.include");
		ALLOWABLE_PARAMETERS.add("mlt.match.offset");
	}

	public static final String PANL_URL_BINDING_MORE_LIKE_THIS = "/panl-more-like-this/";

	private final PanlClient panlClient;
	private final String solrCollection;
	private final String panlCollectionUri;
	private final CollectionProperties collectionProperties;

	/**
	 * <p>Instantiate the Panl more facets handler.</p>
	 *
	 * @param solrCollection The solr collection name to retrieve the search results from
	 * @param panlCollectionUri The name of the collection that the Panl server is bound to.
	 * @param panlProperties The panl base properties, for connection to the Solr server
	 * @param collectionProperties The collection properties
	 *
	 * @throws PanlServerException If there was an error with the request
	 */
	public PanlMoreLikeThisHandler(
			String solrCollection,
			String panlCollectionUri,
			PanlProperties panlProperties,
			CollectionProperties collectionProperties) throws PanlServerException {
		super(panlProperties);

		this.solrCollection = solrCollection;
		this.panlCollectionUri = panlCollectionUri;
		this.collectionProperties = collectionProperties;

		this.panlClient = CollectionHelper.getPanlClient(
				panlProperties.getSolrjClient(),
				solrCollection,
				panlProperties,
				collectionProperties);
	}

	/**
	 * <p>Return the JSON object with the Solr more like this response.</p>
	 *
	 * @param request the HTTP request.
	 * @param response the HTTP response.
	 * @param context the HTTP execution context.
	 */
	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context) {
		try (SolrClient solrClient = panlClient.getClient()) {
			SolrQuery solrQuery = new SolrQuery();
			solrQuery.setMoreLikeThis(true);
			solrQuery.setRequestHandler(collectionProperties.getMltHandler());

			// need to put in the additional parameters
			QueryResponse queryResponse = solrClient.query(this.solrCollection, solrQuery);

			JSONObject solrJsonObject = new JSONObject(queryResponse.jsonStr());
			solrJsonObject.put(Constants.Json.Response.ERROR, false);

			response.setEntity(new StringEntity(solrJsonObject.toString(), ResourceHelper.CONTENT_TYPE_JSON));
			response.setStatusCode(HttpStatus.SC_OK);
			return;
		} catch (IOException | SolrServerException e) {
			set500ResponseMessage(response, e);
		}
	}

	@Override protected Logger getLogger() {
		return(LOGGER);
	}
}
