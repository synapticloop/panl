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

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.generator.bean.PanlCollection;
import com.synapticloop.panl.server.PanlServer;
import com.synapticloop.panl.server.client.PanlClient;
import com.synapticloop.panl.server.handler.helper.CollectionHelper;
import com.synapticloop.panl.server.handler.helper.PanlInboundTokenHolder;
import com.synapticloop.panl.server.handler.processor.*;
import com.synapticloop.panl.server.handler.results.util.ResourceHelper;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.NumRowsLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.PageNumLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.QueryLpseToken;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>The collection request handler acts as the intermediary between the
 * incoming http request and the underlying SOLR server.</p>
 *
 * @author synapticloop
 */
public class CollectionRequestHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionRequestHandler.class);

	public static final String SOLR_PARAM_HL_FL = "hl.fl";
	public static final String SOLR_PARAM_Q_OP = "q.op";

	public static final String JSON_KEY_ACTIVE = "active";
	public static final String JSON_KEY_AVAILABLE = "available";
	public static final String JSON_KEY_CANONICAL_URI = "canonical_uri";
	public static final String JSON_KEY_FIELDS = "fields";
	public static final String JSON_KEY_PAGINATION = "pagination";
	public static final String JSON_KEY_PANL = "panl";
	public static final String JSON_KEY_PANL_BUILD_REQUEST_TIME = "panl_build_request_time";
	public static final String JSON_KEY_PANL_BUILD_RESPONSE_TIME = "panl_build_response_time";
	public static final String JSON_KEY_PANL_PARSE_REQUEST_TIME = "panl_parse_request_time";
	public static final String JSON_KEY_PANL_SEND_REQUEST_TIME = "panl_send_request_time";
	public static final String JSON_KEY_PANL_TOTAL_TIME = "panl_total_time";
	public static final String JSON_KEY_QUERY_OPERAND = "query_operand";
	public static final String JSON_KEY_QUERY_RESPOND_TO = "query_respond_to";
	public static final String JSON_KEY_SORTING = "sorting";
	public static final String JSON_KEY_TIMINGS = "timings";

	private final String solrCollection;
	private final CollectionProperties collectionProperties;
	private final PanlClient panlClient;

	// These are the processors, which processes the Solr response and creates
	// the Panl response object
	private final ActiveProcessor activeProcessor;
	private final PaginationProcessor paginationProcessor;
	private final SortingProcessor sortingProcessor;
	private final QueryOperandProcessor queryOperandProcessor;
	private final FieldsProcessor fieldsProcessor;
	private final AvailableProcessor availableProcessor;
	private final CanonicalURIProcessor canonicalURIProcessor;
	private final String panlCollectionUri;

	/**
	 * <p>The collection request handler which maps a single collection and its
	 * related fieldsets to a URI path</p>
	 *
	 * <p>The URI path is made up of
	 * <code>&lt;collection_uri&gt;/&lt;field_set_name&gt;/</code></p>
	 *
	 * @param solrCollection The solr collection name to retrieve the search
	 * 		results from
	 * @param panlCollectionUri The name of the collection that the Panl server
	 * 		is bound to.
	 * @param panlProperties The panl base properties, for connection to the Solr
	 * 		server
	 * @param collectionProperties The collection properties
	 *
	 * @throws PanlServerException If there was an error with the request
	 */
	public CollectionRequestHandler(String solrCollection,
	                                String panlCollectionUri,
	                                PanlProperties panlProperties,
	                                CollectionProperties collectionProperties) throws PanlServerException {
		LOGGER.info("[ Solr collection '{}' ] Initialising Panl collection URI {}", solrCollection, panlCollectionUri);

		this.solrCollection = solrCollection;
		this.panlCollectionUri = panlCollectionUri;
		this.collectionProperties = collectionProperties;

		panlClient = CollectionHelper.getPanlClient(panlProperties.getSolrjClient(), solrCollection, panlProperties, collectionProperties);

		this.activeProcessor = new ActiveProcessor(collectionProperties);
		this.paginationProcessor = new PaginationProcessor(collectionProperties);
		this.sortingProcessor = new SortingProcessor(collectionProperties);
		this.queryOperandProcessor = new QueryOperandProcessor(collectionProperties);
		this.fieldsProcessor = new FieldsProcessor(collectionProperties);
		this.availableProcessor = new AvailableProcessor(collectionProperties);
		this.canonicalURIProcessor = new CanonicalURIProcessor(collectionProperties);
	}


	/**
	 * <p>Handle the request: split the URI path and (optionally a query
	 * parameter) into Lpse tokens, build the SolrQuery, send it to the Solr
	 * server and parse the response.</p>
	 *
	 * @param uri The URI of the request
	 * @param query The query parameter
	 *
	 * @return The string body of the request
	 *
	 * @throws PanlServerException If there was an error parsing or connecting to
	 * 		the Solr server.
	 */
	public String handleRequest(String uri, String query) throws PanlServerException {
		long startNanos = System.nanoTime();

		String[] searchQuery = uri.split("/");
		String resultFields = searchQuery[2];

		List<LpseToken> lpseTokens = parseLpse(uri, query);

		long parseRequestNanos = System.nanoTime() - startNanos;

		startNanos = System.nanoTime();

		try (SolrClient solrClient = panlClient.getClient()) {
			// we set the default query - to be overridden later if one exists
			SolrQuery solrQuery = panlClient.getQuery(query);
			// set the operand - to be over-ridden later if it is in the URI path
			solrQuery.setParam(SOLR_PARAM_Q_OP, collectionProperties.getSolrDefaultQueryOperand());
			solrQuery.setFacetLimit(collectionProperties.getSolrFacetLimit());

			for (String fieldName : collectionProperties.getResultFieldsForName(resultFields)) {
				solrQuery.addField(fieldName);
			}

			solrQuery.setFacetMinCount(collectionProperties.getFacetMinCount());
			if (collectionProperties.getHighlight()) {
				solrQuery.setParam(SOLR_PARAM_HL_FL, "*");
			}

			// this may be overridden by the lpse status
			solrQuery.setRows(collectionProperties.getNumResultsPerPage());

			solrQuery.addFacetField(collectionProperties.getSolrFacetFields());

			// now we need to go through the panl facets and add them

			int numRows = 0;
			int pageNum = 0;

			// set up the data structure
			Map<String, List<LpseToken>> panlTokenMap = new HashMap<>();

			for (LpseToken lpseToken : lpseTokens) {
				String lpseCode = lpseToken.getLpseCode();

				List<LpseToken> lpseTokenList = panlTokenMap.get(lpseCode);
				if (null == lpseTokenList) {
					lpseTokenList = new ArrayList<>();
				}

				// only adding valid tokens
				if (lpseToken.getIsValid()) {
					lpseTokenList.add(lpseToken);
					panlTokenMap.put(lpseCode, lpseTokenList);
				}

				if (lpseToken instanceof NumRowsLpseToken) {
					numRows = ((NumRowsLpseToken) lpseToken).getNumRows();
				} else if (lpseToken instanceof PageNumLpseToken) {
					pageNum = ((PageNumLpseToken) lpseToken).getPageNum();
				}
			}

			for (BaseField lpseField : collectionProperties.getLpseFields()) {
				lpseField.applyToQuery(solrQuery, panlTokenMap);
			}


			// we may not have a numrows start
			if (numRows == 0) {
				numRows = collectionProperties.getNumResultsPerPage();
			}
			if (pageNum == 0) {
				pageNum = 1;
			}

			// now we need to set the start
			solrQuery.setStart((pageNum - 1) * numRows);
			System.out.println(solrQuery);

			long buildRequestNanos = System.nanoTime() - startNanos;
			startNanos = System.nanoTime();
			final QueryResponse response = solrClient.query(this.solrCollection, solrQuery);

			long sendAnReceiveNanos = System.nanoTime() - startNanos;
			return (parseResponse(
					lpseTokens,
					response,
					parseRequestNanos,
					buildRequestNanos,
					sendAnReceiveNanos));

		} catch (Exception e) {
			e.printStackTrace();
			throw new PanlServerException("Could not query the Solr instance, message was: " + e.getMessage(), e);
		}
	}

	/**
	 * <p>Parse the solrj response and add the panl information to it</p>
	 *
	 * @param lpseTokens The parsed URI and panl tokens
	 * @param response The Solrj response to be parsed
	 * @param parseRequestNanos The start time for this query in nanoseconds
	 * @param buildRequestNanos The number of nanos it took to build the request
	 * @param sendAndReceiveNanos The number of nanos it took to send the request
	 *
	 * @return a JSON Object as a string with the appended panl response
	 */
	private String parseResponse(
			List<LpseToken> lpseTokens,
			QueryResponse response,
			long parseRequestNanos,
			long buildRequestNanos,
			long sendAndReceiveNanos) {

		// set up the JSON response object
		JSONObject solrJsonObject = new JSONObject(response.jsonStr());
		JSONObject panlObject = new JSONObject();


		long startNanos = System.nanoTime();

		// set up the data structure
		Map<String, List<LpseToken>> panlTokenMap = new HashMap<>();
		for (LpseToken lpseToken : lpseTokens) {
			// These codes are ignored, just carry on
			if (collectionProperties.getIsIgnoredLpseCode(lpseToken.getLpseCode())) {
				continue;
			}

			String lpseCode = lpseToken.getLpseCode();

			List<LpseToken> lpseTokenList = panlTokenMap.get(lpseCode);
			if (null == lpseTokenList) {
				lpseTokenList = new ArrayList<>();
			}
			lpseTokenList.add(lpseToken);
			panlTokenMap.put(lpseCode, lpseTokenList);
		}

		// we are going to sort the tokens alphabetically by value to build the
		// canonical URI
		for (String key : panlTokenMap.keySet()) {
			List<LpseToken> lpseTokenTemp = panlTokenMap.get(key);
			// but we don't sort on the sort tokens - as there is an order to them
			if (!key.equals(collectionProperties.getPanlParamSort())) {
				lpseTokenTemp.sort((o1, o2) -> {
					if (o1 == null ||
							o2 == null ||
							!o1.getIsValid() ||
							o1.getValue() == null ||
							o2.getValue() == null ||
							!o2.getIsValid()) {
						// either one is invalid and won't be sent through or generate a
						// canonical URI
						return (0);
					} else {
						return (o1.getValue().compareTo(o2.getValue()));
					}
				});
			}
		}

		panlObject.put(JSON_KEY_AVAILABLE, availableProcessor.processToObject(panlTokenMap, response));
		panlObject.put(JSON_KEY_ACTIVE, activeProcessor.processToObject(panlTokenMap));
		panlObject.put(JSON_KEY_PAGINATION, paginationProcessor.processToObject(panlTokenMap, response));
		panlObject.put(JSON_KEY_SORTING, sortingProcessor.processToObject(panlTokenMap));
		panlObject.put(JSON_KEY_QUERY_OPERAND, queryOperandProcessor.processToObject(panlTokenMap));
		panlObject.put(JSON_KEY_FIELDS, fieldsProcessor.processToObject(panlTokenMap));
		panlObject.put(JSON_KEY_CANONICAL_URI, canonicalURIProcessor.processToString(panlTokenMap));
		panlObject.put(JSON_KEY_QUERY_RESPOND_TO, collectionProperties.getFormQueryRespondTo());

		// now add in the timings
		JSONObject timingsObject = new JSONObject();

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

		solrJsonObject.put(ResourceHelper.JSON_KEY_ERROR, false);

		// last thing - we want to put the panl to solr field mappings in
		solrJsonObject.put(JSON_KEY_PANL, panlObject);

		return (solrJsonObject.toString());
	}

	/**
	 * <p>Parse the uri and optionally the query string if it exists.</p>
	 *
	 * <p><strong>NOTE:</strong> If the query string is not empty, then this will
	 * overwrite any query that is set in the lpse URI.</p>
	 *
	 * <p>The URI will always be of the form</p>
	 *
	 * <pre>
	 * /&lt;collection_uri&gt;
	 *   /&lt;field_set&gt;
	 *   /&lt;facet_field&gt;
	 *   /...
	 *   /&lt;facet_field&gt;
	 *   /&lt;lpse_encoding&gt;
	 *   /
	 * </pre>
	 *
	 * <p><strong>NOTE:</strong> If there is an error within the lpse encoding,
	 * or the URI (For example, if the URI is manually entered or tampered with)
	 * the parser will not throw an error, however the results will not be as
	 * expected.</p>
	 *
	 * @param uri The URI to parse
	 * @param query the query to parse - if the query string exists, then this
	 * 		query will replace any existing query in the lpse encoded URI
	 *
	 * @return The parse URI as a List of <code>PanlToken</code>
	 */
	public List<LpseToken> parseLpse(String uri, String query) {
		List<LpseToken> lpseTokens = new ArrayList<>();
		Set<String> existingTokens = new HashSet<>();

		String[] lpseUriPath = uri.split("/");

		boolean hasQuery = false;

		if (lpseUriPath.length > 3) {
			String lpseEncoding = lpseUriPath[lpseUriPath.length - 1];

			LpseTokeniser lpseTokeniser = new LpseTokeniser(lpseEncoding, PanlCollection.CODES_AND_METADATA, true);

			StringTokenizer valueTokeniser = new StringTokenizer(uri, "/", false);
			// we need to skip the first two - as they will be the collection and the
			// field set
			valueTokeniser.nextToken();
			valueTokeniser.nextToken();

			while (lpseTokeniser.hasMoreTokens()) {
				String token = lpseTokeniser.nextToken();
				LpseToken lpseToken = LpseToken.getLpseToken(collectionProperties, token, query, valueTokeniser, lpseTokeniser);
				if (lpseToken instanceof QueryLpseToken) {
					hasQuery = true;
				}

				lpseTokens.add(lpseToken);
				String equivalenceValue = lpseToken.getEquivalenceValue();
				if (existingTokens.contains(equivalenceValue)) {
					lpseToken.setIsValid(false);
				} else {
					existingTokens.add(equivalenceValue);
				}
			}
		}

		if (!hasQuery && !query.isBlank()) {
			lpseTokens.add(new QueryLpseToken(collectionProperties, query, collectionProperties.getPanlParamQuery()));
		}

		for (LpseToken lpseToken : lpseTokens) {
			System.out.println(lpseToken.explain());
		}

		return (lpseTokens);
	}

	/**
	 * <p>Get the valid URLs as a JSON array string.</p>
	 *
	 * @return The valid URLs as a JSON array string
	 */
	public String getValidUrlsJSONArrayString() {
		return (collectionProperties.getValidUrlsJSONArrayString());
	}

	/**
	 * <p>Return whether this is a valid results field for this path, i.e. this
	 * collection handler can respond to this.</p>
	 *
	 * @param path The path to check to see whether this handler can respond to is
	 *
	 * @return Whether this is a valid results field for this path
	 */
	public boolean isValidResultsFields(String path) {
		return (collectionProperties.isValidResultFieldsName(path));
	}

	/**
	 * <p>Get the Solr collection that this handler will connect to.  This is
	 * used for debugging/explanation/information usage with the Panl results
	 * explainer web app.</p>
	 *
	 * @return The solr collection that this handler will connect to.
	 */
	public String getSolrCollection() {
		return solrCollection;
	}

	/**
	 * <p>Get the names for the result fields that will be returned with this
	 * handler.</p>
	 *
	 * @return The names for the result fields that will be returned with this
	 * 		handler.
	 */
	public List<String> getResultFieldsNames() {
		return (new ArrayList<>(collectionProperties.getResultFieldsNames()));
	}

	/**
	 * <p>Get the Panl collection URI for this handler</p>
	 *
	 * @return The Panl collection URI for this handler
	 */
	public String getPanlCollectionUri() {
		return panlCollectionUri;
	}
}
