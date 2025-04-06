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

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.helper.TimingsHelper;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import com.synapticloop.panl.server.handler.properties.holder.MoreLikeThisHolder;
import com.synapticloop.panl.server.handler.webapp.util.ResourceHelper;
import com.synapticloop.panl.util.Constants;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
	public static final String PANL_URL_BINDING_MORE_LIKE_THIS = "/panl-more-like-this/";

	private final List<CollectionRequestHandler> collectionRequestHandlers;

	private final Map<String, CollectionRequestHandler> validCollectionsMap = new HashMap<>();

	/**
	 * <p>Instantiate the Panl more facets handler.</p>
	 *
	 * @param panlProperties The panl properties
	 * @param collectionRequestHandlers The collection request handler
	 *
	 * @throws PanlServerException If there was an error with the request
	 */
	public PanlMoreLikeThisHandler(
			PanlProperties panlProperties,
			List<CollectionRequestHandler> collectionRequestHandlers) throws PanlServerException {

		super(panlProperties);
		this.collectionRequestHandlers = collectionRequestHandlers;

		for (CollectionRequestHandler collectionRequestHandler : collectionRequestHandlers) {
			if(collectionRequestHandler.getCollectionProperties().getMoreLikeThisHolder().getIsMltEnabled()) {
				for (String resultFieldsName : collectionRequestHandler.getResultFieldsNames()) {
					validCollectionsMap.put(collectionRequestHandler.getPanlCollectionUri(), collectionRequestHandler);
					validUrls.put(PANL_URL_BINDING_MORE_LIKE_THIS + collectionRequestHandler.getPanlCollectionUri() + "/" + resultFieldsName + "/");
					collectionRequestHandler.getCollectionProperties().getResultFieldsForFieldSet(resultFieldsName);
				}
			}
		}
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
		TimingsHelper timingsHelper = new TimingsHelper();

		String uri = request.getRequestLine().getUri();
		String[] paths = uri.split("/");

		if(paths.length < 5) {
			set404ResponseMessage(response);
			return;
		}

		CollectionRequestHandler collectionRequestHandler = validCollectionsMap.get(paths[2]);
		String fieldSet = paths[3];
		String uniqueKeyValue = paths[4];

		if(null == collectionRequestHandler) {
			set404ResponseMessage(response);
			return;
		}

		List<String> resultFieldsForFieldSet = collectionRequestHandler.getCollectionProperties().getResultFieldsForFieldSet(fieldSet);

		if(resultFieldsForFieldSet.isEmpty()) {
			set404ResponseMessage(response);
			return;
		}


		timingsHelper.markParseInboundRequestEnd();
		MoreLikeThisHolder moreLikeThisHolder = collectionRequestHandler.getCollectionProperties().getMoreLikeThisHolder();

		// if MLT is not enabled for this collection, then return a 404
		if(!moreLikeThisHolder.getIsMltEnabled()) {
			set404ResponseMessage(response);
			return;
		}

		// at this point MLT is enabled and we are ready to serve the response
		try (SolrClient solrClient = collectionRequestHandler.getPanlClient().getClient()) {
			SolrQuery solrQuery = new SolrQuery();

			try {
				moreLikeThisHolder.applyMltToQuery(solrQuery, resultFieldsForFieldSet, uniqueKeyValue);
				timingsHelper.markBuildOutboundRequestEnd();
			} catch(PanlServerException ex) {
				// the only time that this happens if the MLT is not enabled - this
				// shouldn't happen as it was checked as the first part of the method
				// call.
				set404ResponseMessage(response);
				return;
			}

			LOGGER.debug(solrQuery.toString());

			int numRetries = 1;
			boolean hasSolrShardError = true;

			JSONObject solrJsonObject = new JSONObject();
			// TODO - whilst this is technically true - it is actually that Solr
			// TODO - couldn't find the more like this handler query....
			// TODO - Should probably do a code that indicates a retry
			// TODO - For now it is an SC No Content error code (that will be weird...
			// TODO - and probably not correct)
			solrJsonObject.put(Constants.Json.Response.STATUS, HttpStatus.SC_NO_CONTENT);
			solrJsonObject.put(Constants.Json.Response.ERROR, true);

			JSONObject panlJsonObject = new JSONObject();

			while(hasSolrShardError && numRetries < 6) {
				QueryResponse queryResponse = solrClient.query(collectionRequestHandler.getSolrCollection(), solrQuery);
				solrJsonObject = new JSONObject(queryResponse.jsonStr());
				if(!solrJsonObject.isNull(Constants.Json.Solr.RESPONSE)) {
					hasSolrShardError = false;
					solrJsonObject.put(Constants.Json.Response.STATUS, HttpStatus.SC_OK);
					solrJsonObject.put(Constants.Json.Response.ERROR, false);
				}
				panlJsonObject.put(Constants.Json.Panl.NUM_RETRIES, numRetries);
				numRetries++;
			}

			timingsHelper.markSendOutboundRequestEnd();
			timingsHelper.markBuildInboundResponseEnd();
			timingsHelper.addTimings(panlJsonObject);
			solrJsonObject.put(Constants.Json.Panl.PANL, panlJsonObject);


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
