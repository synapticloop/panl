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

import com.synapticloop.panl.server.client.PanlClient;
import com.synapticloop.panl.server.handler.fielderiser.field.param.PanlQueryField;
import com.synapticloop.panl.server.handler.helper.TimingsHelper;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import com.synapticloop.panl.server.handler.webapp.util.ResourceHelper;
import com.synapticloop.panl.util.Constants;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
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

/**
 * <p>This is the handler which will return the lookahead documents for a the
 * specific search query.</p>
 *
 * <p>In effect, this will do a Solr query, without requesting any facets.</p>
 *
 * @author Synapticloop
 */
public class PanlLookaheadHandler extends BaseResponseHandler implements HttpRequestHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlLookaheadHandler.class);

	public static final String PANL_URL_BINDING_LOOKAHEAD = "/panl-lookahead/";

	private final Map<String, CollectionRequestHandler> validCollectionsMap = new HashMap<>();

	private final Map<String, String> queryRespondToMap = new HashMap<>();

	/**
	 * <p>Instantiate the Panl lookahead handler.</p>
	 *
	 * @param panlProperties The panl properties
	 * @param collectionRequestHandlers The collection request handler
	 */
	public PanlLookaheadHandler(PanlProperties panlProperties, List<CollectionRequestHandler> collectionRequestHandlers) {
		super(panlProperties);

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
		// TODO - this may be a array index out of bounds.... need to check first...
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

	/**
	 * <p>Do the Solr request with the query parameter, requesting no facets and
	 * requesting the number of rows as set by the <code>solr.numrows.lookahead</code>
	 * parameter.</p>
	 *
	 * @param collectionRequestHandler The collection request handler that this
	 *        handler is bound to.
	 * @param response The response object
	 * @param query The query parameter in the for of <code>&lt;query.respond.to&gt;=some+value</code>
	 * @param fieldSet The fieldset that is requested
	 * @param startNanos The time in nanos that this request was started
	 * @param parseRequestNanos The number of nanos that this request took to
	 *        parse
	 */
	private void doRequest(
			CollectionRequestHandler collectionRequestHandler,
			HttpResponse response,
			String query,
			String fieldSet,
			long startNanos,
			long parseRequestNanos) {

		CollectionProperties collectionProperties = collectionRequestHandler.getCollectionProperties();
		int numRows = collectionProperties.getNumResultsLookahead();

		PanlClient panlClient = collectionRequestHandler.getPanlClient();
		try (SolrClient solrClient = panlClient.getClient()) {

			SolrQuery solrQuery = panlClient.getQuery();
			// now parse the query
			String thisQuery = "*:*";

			for (NameValuePair nameValuePair : URLEncodedUtils.parse(query, StandardCharsets.UTF_8)) {
				if(nameValuePair.getName().equals(collectionProperties.getFormQueryRespondTo())) {
					StringBuilder sb = new StringBuilder();
					boolean isFirst = true;
					for (String parseKeyword : PanlQueryField.parseKeywords(nameValuePair.getValue())) {
						if(!isFirst) {
							sb.append(" ");
						}

						sb.append("\"")
						       .append(parseKeyword)
						       .append("\"");
						isFirst = false;
					}
					thisQuery = sb.toString();
					break;
				}
			}

			solrQuery.setQuery(thisQuery);

			// add in the default query operation
			solrQuery.setParam(Constants.Parameter.Solr.Q_OP, collectionProperties.getSolrDefaultQueryOperand());

			List<String> resultFieldsForName = collectionProperties.getResultFieldsForFieldSet(fieldSet);
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

			solrJsonObject.remove(Constants.Json.Solr.RESPONSE_HEADER);

			long sendAndReceiveNanos = System.nanoTime() - startNanos - parseRequestNanos - buildRequestNanos;

			long buildResponseTime = System.nanoTime() - startNanos;

			// add in some statistics
			timingsObject.put(Constants.Json.Panl.PARSE_REQUEST_TIME, TimeUnit.NANOSECONDS.toMillis(parseRequestNanos));
			timingsObject.put(Constants.Json.Panl.BUILD_REQUEST_TIME, TimeUnit.NANOSECONDS.toMillis(buildRequestNanos));
			timingsObject.put(Constants.Json.Panl.SEND_REQUEST_TIME, TimeUnit.NANOSECONDS.toMillis(sendAndReceiveNanos));

			timingsObject.put(Constants.Json.Panl.BUILD_RESPONSE_TIME, TimeUnit.NANOSECONDS.toMillis(buildResponseTime));
			timingsObject.put(Constants.Json.Panl.TOTAL_TIME, TimeUnit.NANOSECONDS.toMillis(
				parseRequestNanos +
					buildRequestNanos +
					sendAndReceiveNanos +
					buildResponseTime
			));

			panlObject.put(Constants.Json.Panl.TIMINGS, timingsObject);
			solrJsonObject.put(Constants.Json.Panl.PANL, panlObject);

			response.setEntity(new StringEntity(solrJsonObject.toString(), ResourceHelper.CONTENT_TYPE_JSON));

			response.setStatusCode(HttpStatus.SC_OK);
		} catch(Exception e) {
			set500ResponseMessage(response, e);
			// left the return statement here in case we want to do more after the
			// non-excepted method completes.
			return;
		}
	}

	@Override protected Logger getLogger() {
		return(LOGGER);
	}
}
