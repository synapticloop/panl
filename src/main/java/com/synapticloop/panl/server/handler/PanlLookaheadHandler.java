package com.synapticloop.panl.server.handler;

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

import com.synapticloop.panl.server.client.PanlClient;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import com.synapticloop.panl.server.handler.webapp.util.ResourceHelper;
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
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.synapticloop.panl.server.handler.CollectionRequestHandler.*;
import static com.synapticloop.panl.server.handler.webapp.util.ResourceHelper.*;

/**
 * <p>This is the handler which will return more facets for a specific facet
 * to then be populated.</p>
 *
 * <p>In effect, this will do a complete Solr query, setting the facet and
 * limit for the request, discarding any of the un-wanted response objects, and
 * just returning the requested facets.</p>
 *
 * @author Synapticloop
 */
public class PanlLookaheadHandler implements HttpRequestHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlLookaheadHandler.class);

	public static final String PANL_URL_BINDING_LOOKAHEAD = "/panl-lookahead/";

	private final PanlProperties panlProperties;
	private final Map<String, CollectionRequestHandler> validCollectionsMap = new HashMap<>();

	private final Map<String, String> queryRespondToMap = new HashMap<>();
	private final JSONArray validUrls = new JSONArray();

	/**
	 * <p>Instantiate the Panl lookahead handler.</p>
	 *
	 * @param panlProperties The panl properties
	 * @param collectionRequestHandlers The collection request handler
	 */
	public PanlLookaheadHandler(PanlProperties panlProperties, List<CollectionRequestHandler> collectionRequestHandlers) {
		this.panlProperties = panlProperties;

		for (CollectionRequestHandler collectionRequestHandler : collectionRequestHandlers) {
			queryRespondToMap.put(collectionRequestHandler.getPanlCollectionUri(), collectionRequestHandler.getFormQueryRespondTo());
			validCollectionsMap.put(collectionRequestHandler.getPanlCollectionUri(), collectionRequestHandler);
			validUrls.put(PANL_URL_BINDING_LOOKAHEAD + collectionRequestHandler.getPanlCollectionUri() + "/");
		}
	}

	/**
	 * <p>Return the JSON object that contains just the facet that is required.</p>
	 *
	 * @param request the HTTP request.
	 * @param response the HTTP response.
	 * @param context the HTTP execution context.
	 */
	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context) {
		long startNanos = System.nanoTime();

		String uri = request.getRequestLine().getUri();
		// do we have a query string?
		int indexOfQuestionMark = uri.indexOf('?');
		if(indexOfQuestionMark == -1) {
			set404ResponseMessage(response);
			return;
		}

		// if we do - get the query string and reset the uri
		uri = uri.substring(0, indexOfQuestionMark);

		// now check the CaFUP
		String[] paths = uri.split("/");
		CollectionRequestHandler collectionRequestHandler = validCollectionsMap.get(paths[2]);
		if(paths.length < 4 || null == collectionRequestHandler) {
			set404ResponseMessage(response);
			return;
		}

		String collection = paths[2];
		String fieldSet = paths[3];

		// at this point we need to check the query param that it matches the collection
		String queryRepondTo = queryRespondToMap.get(collection);
		String query = null;
		boolean isGoodRequest = false;
		try {
			final List<NameValuePair> pairs = new URIBuilder(request.getRequestLine().getUri()).getQueryParams();
			for (NameValuePair pair : pairs) {
				if(pair.getName().equals(queryRepondTo)) {
					isGoodRequest = true;
					query = queryRepondTo + "=" + URLDecoder.decode(pair.getValue(), StandardCharsets.UTF_8);
					break;
				}
			}
		} catch (URISyntaxException e) {
			set500ResponseMessage(response, e);
			return;
		}

		if(!isGoodRequest) {
			set404ResponseMessage(response);
			return;
		}

		// now we need to do the request - but with no facets
		doRequest(collectionRequestHandler, response, query, fieldSet, startNanos, (System.nanoTime() - startNanos));
	}

	private void doRequest(
			CollectionRequestHandler collectionRequestHandler,
			HttpResponse response,
			String query,
			String fieldSet,
			long startNanos,
			long parseRequestNanos) {

		CollectionProperties collectionProperties = collectionRequestHandler.getCollectionProperties();
		int numRows = collectionProperties.getNumResultsPerPage();

		PanlClient panlClient = collectionRequestHandler.getPanlClient();
		try (SolrClient solrClient = panlClient.getClient()) {
			SolrQuery solrQuery = panlClient.getQuery(query);
			List<String> resultFieldsForName = collectionProperties.getResultFieldsForName(fieldSet);
			if(null != resultFieldsForName) {
				for (String fieldName : resultFieldsForName) {
					solrQuery.addField(fieldName);
				}
			}
			solrQuery.setRows(numRows);
			solrQuery.setStart(0);

			LOGGER.debug(solrQuery.toString());

			long buildRequestNanos = System.nanoTime() - startNanos - parseRequestNanos;
			final QueryResponse solrQueryResponse = solrClient.query(collectionRequestHandler.getSolrCollection(), solrQuery);
			JSONObject solrJsonObject = new JSONObject(solrQueryResponse.jsonStr());
			JSONObject panlObject = new JSONObject();
			JSONObject timingsObject = new JSONObject();

			long sendAndReceiveNanos = System.nanoTime() - startNanos - parseRequestNanos - buildRequestNanos;

			long buildResponseTime = System.nanoTime() - startNanos;

			// add in some statistics
			timingsObject.put(JSON_KEY_PANL_PARSE_REQUEST_TIME, TimeUnit.NANOSECONDS.toMillis(parseRequestNanos));
			timingsObject.put(JSON_KEY_PANL_BUILD_REQUEST_TIME, TimeUnit.NANOSECONDS.toMillis(buildRequestNanos));
			timingsObject.put(JSON_KEY_PANL_SEND_REQUEST_TIME, TimeUnit.NANOSECONDS.toMillis(sendAndReceiveNanos));

			timingsObject.put(JSON_KEY_PANL_BUILD_RESPONSE_TIME, TimeUnit.NANOSECONDS.toMillis(buildResponseTime));
			timingsObject.put(JSON_KEY_PANL_TOTAL_TIME, TimeUnit.NANOSECONDS.toMillis(
				parseRequestNanos +
					buildRequestNanos +
					sendAndReceiveNanos +
					buildResponseTime
			));

			panlObject.put(JSON_KEY_TIMINGS, timingsObject);
			solrJsonObject.put(JSON_KEY_PANL, panlObject);

			response.setEntity(new StringEntity(solrJsonObject.toString(), ResourceHelper.CONTENT_TYPE_JSON));

			response.setStatusCode(HttpStatus.SC_OK);
		} catch(Exception e) {
			set500ResponseMessage(response, e);
			return;
		}

	}

	private void set500ResponseMessage(HttpResponse response, Exception e) {
		LOGGER.error("Internal server error, message was '{}'", e.getMessage(), e);
		response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(JSON_KEY_ERROR, true);
		jsonObject.put(JSON_KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
		if (panlProperties.getUseVerbose500Messages()) {
			jsonObject.put(JSON_KEY_MESSAGE,
				String.format("Class: %s, message: %s.",
					e.getClass().getCanonicalName(),
					e.getMessage()));

			response.setEntity(new StringEntity(jsonObject.toString(), ResourceHelper.CONTENT_TYPE_JSON));
		} else {
			jsonObject.put(JSON_KEY_MESSAGE, JSON_VALUE_MESSAGE_500);
		}
	}

	private void set404ResponseMessage(HttpResponse response) {
		response.setStatusCode(HttpStatus.SC_NOT_FOUND);

		JSONObject jsonObject = new JSONObject();

		jsonObject.put(JSON_KEY_ERROR, true);
		jsonObject.put(JSON_KEY_STATUS, HttpStatus.SC_NOT_FOUND);
		if (panlProperties.getUseVerbose404Messages()) {
			jsonObject.put(JSON_KEY_MESSAGE, PanlDefaultHandler.JSON_VALUE_MESSAGE);
			jsonObject.put(JSON_KEY_VALID_URLS, validUrls);
		} else {
			jsonObject.put(JSON_KEY_MESSAGE, JSON_VALUE_MESSAGE_404);
		}

		response.setEntity(
			new StringEntity(jsonObject.toString(),
				ResourceHelper.CONTENT_TYPE_JSON));
	}
}
