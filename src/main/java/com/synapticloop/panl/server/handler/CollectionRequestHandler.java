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
import com.synapticloop.panl.server.handler.fielderiser.field.facet.PanlFacetField;
import com.synapticloop.panl.server.handler.fielderiser.field.facet.PanlRangeFacetField;
import com.synapticloop.panl.server.handler.helper.CollectionHelper;
import com.synapticloop.panl.server.handler.processor.*;
import com.synapticloop.panl.server.handler.tokeniser.token.param.QueryOperandLpseToken;
import com.synapticloop.panl.server.handler.webapp.util.ResourceHelper;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.NumRowsLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.PageNumLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.QueryLpseToken;
import com.synapticloop.panl.util.Constants;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.protocol.HttpContext;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.synapticloop.panl.server.handler.processor.Processor.*;

/**
 * <p>The collection request handler acts as the intermediary between the
 * incoming http request and the underlying SOLR server.</p>
 *
 * @author synapticloop
 */
public class CollectionRequestHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionRequestHandler.class);

	public static String CODES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234567890";
	public static String CODES_AND_METADATA = CODES + "[].+-()";

	private final String solrCollection;
	private final CollectionProperties collectionProperties;
	private final PanlProperties panlProperties;
	private final PanlClient panlClient;
	private final String panlCollectionUri;

	// These are the processors, which processes the Solr response and creates
	// the Panl response object
	private final ActiveProcessor activeProcessor;
	private final SearchFieldsProcessor searchFieldsProcessor;
	private final PaginationProcessor paginationProcessor;
	private final SortingProcessor sortingProcessor;
	private final QueryOperandProcessor queryOperandProcessor;
	private final FieldsProcessor fieldsProcessor;
	private final AvailableProcessor availableProcessor;
	private final CanonicalURIProcessor canonicalURIProcessor;

	/**
	 * <p>The collection request handler which maps a single collection and its
	 * related FieldSets to a URI path</p>
	 *
	 * <p>The URI path is made up of
	 * <code>&lt;panl_collection_uri&gt;/&lt;field_set_name&gt;/</code>
	 * </p>
	 *
	 * @param solrCollection The solr collection name to retrieve the search results from
	 * @param panlCollectionUri The name of the collection that the Panl server is bound to.
	 * @param panlProperties The panl base properties, for connection to the Solr server
	 * @param collectionProperties The collection properties
	 *
	 * @throws PanlServerException If there was an error with the request
	 */
	public CollectionRequestHandler(
		String solrCollection,
		String panlCollectionUri,
		PanlProperties panlProperties,
		CollectionProperties collectionProperties) throws PanlServerException {

		LOGGER.info("[ Solr collection '{}' ] Initialising Panl collection URI {}", solrCollection, panlCollectionUri);

		this.solrCollection = solrCollection;
		this.panlCollectionUri = panlCollectionUri;
		this.collectionProperties = collectionProperties;
		this.panlProperties = panlProperties;

		this.panlClient = CollectionHelper.getPanlClient(
			panlProperties.getSolrjClient(),
			solrCollection,
			panlProperties,
			collectionProperties);

		this.activeProcessor = new ActiveProcessor(collectionProperties);
		this.searchFieldsProcessor = new SearchFieldsProcessor(collectionProperties);
		this.paginationProcessor = new PaginationProcessor(collectionProperties);
		this.sortingProcessor = new SortingProcessor(collectionProperties);
		this.queryOperandProcessor = new QueryOperandProcessor(collectionProperties);
		this.fieldsProcessor = new FieldsProcessor(collectionProperties);
		this.availableProcessor = new AvailableProcessor(collectionProperties);
		this.canonicalURIProcessor = new CanonicalURIProcessor(collectionProperties);
	}


	/**
	 * <p>Handle the request: split the URI path and (optionally a query
	 * parameter) into LPSE tokens, build the SolrQuery, send it to the Solr
	 * server and parse the response.</p>
	 *
	 * <p>This handler also handles the More Facets response by looking for a
	 * context attribute of <code>Constants.Context.Panl.LPSE_CODE</code>,
	 * which, if it exists, will only return the details for that specific facet
	 * code.</p>
	 *
	 * @param uri The URI of the request
	 * @param query The query parameter
	 * @param context The passed in HttpContext for this request - this will only
	 *   be used for the more facets request
	 *
	 * @return The string body of the request
	 *
	 * @throws PanlServerException If there was an error parsing or connecting to
	 * the Solr server.
	 */
	public String handleRequest(
			String uri,
			String query,
			HttpContext context) throws PanlServerException, PanlNotFoundException {

		long startNanos = System.nanoTime();

		// check to ensure that the more facets LPSE code is correct
		String contextLpseCode =
				(String) context.getAttribute(Constants.Context.Panl.LPSE_CODE);
		if (null != contextLpseCode) {
			String solrFieldName = collectionProperties.getSolrFieldNameFromLpseCode(contextLpseCode);
			if (null == solrFieldName) {
				throw new PanlNotFoundException("Unknown LPSE code of " + contextLpseCode);
			}
		}

		String[] lpsePath = uri.split("/");
		String fieldSet = lpsePath[2];

		List<LpseToken> lpseTokens = parseLpse(uri, query);

		long parseRequestNanos = System.nanoTime() - startNanos;

		startNanos = System.nanoTime();

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
				NumRowsLpseToken numRowsLpseToken = (NumRowsLpseToken) lpseToken;
				numRows = numRowsLpseToken.getNumRows();

				// if the number of rows is greater than the maximum number of rows,
				// then set it to the maximum number of rows
				if(numRows > collectionProperties.getMaxNumResultsPerPage()) {
					numRows = collectionProperties.getMaxNumResultsPerPage();
					numRowsLpseToken.setNumRows(numRows);
				}

			} else if (lpseToken instanceof PageNumLpseToken) {
				// if we have a query string - we always reset the page number to the
				// first page.
				PageNumLpseToken pageNumLpseToken = (PageNumLpseToken) lpseToken;
				if (!query.isBlank()) {
					pageNumLpseToken.setPageNum(1);
				}
				pageNum = pageNumLpseToken.getPageNum();
			}
		}

		boolean isMoreFacets = false;

		try (SolrClient solrClient = panlClient.getClient()) {
			// we set the default query - to be overridden later if one exists
			SolrQuery solrQuery = panlClient.getQuery(query);
			// set the operand - to be over-ridden later if it is in the URI path
			solrQuery.setParam(Constants.Parameter.Solr.Q_OP, collectionProperties.getSolrDefaultQueryOperand());

			// if we have something in the context - set it to this value
			if (null != context.getAttribute(Constants.Context.Panl.FACET_LIMIT)) {
				solrQuery.setFacetLimit((Integer) context.getAttribute(Constants.Context.Panl.FACET_LIMIT));
				isMoreFacets = true;
			} else {
				solrQuery.setFacetLimit(collectionProperties.getSolrFacetLimit());
			}

			// we are checking for the empty fieldsets
			List<String> resultFieldsForFieldSet = collectionProperties.getResultFieldsForFieldSet(fieldSet);
			for (String fieldName : resultFieldsForFieldSet) {
				solrQuery.addField(fieldName);
			}

			collectionProperties.setFacetMinCounts(solrQuery, panlTokenMap);

			if (collectionProperties.getHighlight()) {
				solrQuery.setParam(Constants.Parameter.Solr.HL_FL, "*");
				solrQuery.setParam(Constants.Parameter.Solr.HL, "on");
			}

			// this may be overridden by the lpse status
			solrQuery.setRows(collectionProperties.getNumResultsPerPage());

			// At this point we are either going to get all facet fields that
			// have a when set, or we are just looking for more facets for a single
			// one

			if (null != contextLpseCode) {
				// we are looking for 'more facets', so we only need this one
				isMoreFacets = true;

				solrQuery.addFacetField(collectionProperties.getSolrFieldNameFromLpseCode(contextLpseCode));

				// now we also want to order them as well - as by default Solr will
				// order them by index rather than count - which may not be what the
				// user wants
				BaseField lpseField = collectionProperties.getLpseField(contextLpseCode);
				if (!lpseField.getIsFacetSortByIndex()) {
					solrQuery.add("f." + lpseField.getSolrFieldName() + ".facet.sort", "count");
				}
			} else {
				// no we need to go through all tokens and only return the ones that we
				// need to be displayed

				solrQuery.addFacetField(collectionProperties.getWhenUnlessSolrFacetFields(lpseTokens));

				for (PanlFacetField facetIndexSortField : collectionProperties.getFacetIndexSortFields()) {
					solrQuery.add("f." + facetIndexSortField.getSolrFieldName() + ".facet.sort", "index");
				}
			}

			boolean hasStats = false;
			for (BaseField lpseField : collectionProperties.getLpseFields()) {
				lpseField.applyToQuery(solrQuery, panlTokenMap, collectionProperties);

				if (!isMoreFacets) {
					if (lpseField instanceof PanlRangeFacetField) {
						solrQuery.add(Constants.Parameter.Solr.STATS_FIELD, lpseField.getSolrFieldName());
						if (!hasStats) {
							solrQuery.add(Constants.Parameter.Solr.STATS, Constants.BOOLEAN_TRUE_VALUE);
							hasStats = true;
						}
					}
				}
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
			solrQuery.setRows(numRows);

			// this is done for the empty fieldset
			if (resultFieldsForFieldSet.isEmpty()) {
				solrQuery.setRows(0);
			}

			LOGGER.debug(solrQuery.toString());

			long buildRequestNanos = System.nanoTime() - startNanos;
			startNanos = System.nanoTime();
			final QueryResponse solrQueryResponse = solrClient.query(this.solrCollection, solrQuery);

			long sendAnReceiveNanos = System.nanoTime() - startNanos;

			return (parseResponse(
					fieldSet,
					lpseTokens,
					solrQueryResponse,
					parseRequestNanos,
					buildRequestNanos,
					sendAnReceiveNanos));

		} catch (Exception e) {
			throw new PanlServerException("Could not query the Solr instance, message was: " + e.getMessage(), e);
		}
	}

	/**
	 * <p>Parse the solrj response and add the Panl JSON information to it</p>
	 *
	 * @param fieldSet The fieldSet for this query
	 * @param lpseTokens The parsed URI and panl tokens
	 * @param solrQueryResponse The Solrj response to be parsed
	 * @param parseRequestNanos The start time for this query in nanoseconds
	 * @param buildRequestNanos The number of nanos it took to build the request
	 * @param sendAndReceiveNanos The number of nanos it took to send the request
	 *
	 * @return a JSON Object as a string with the appended panl response
	 */
	private String parseResponse(
			String fieldSet,
			List<LpseToken> lpseTokens,
			QueryResponse solrQueryResponse,
			long parseRequestNanos,
			long buildRequestNanos,
			long sendAndReceiveNanos) {

		// set up the JSON response object
		JSONObject solrJsonObject = new JSONObject(solrQueryResponse.jsonStr());
		JSONObject panlObject = new JSONObject();


		long startNanos = System.nanoTime();

		// set up the data structure
		Map<String, List<LpseToken>> panlTokenMap = new HashMap<>();
		for (LpseToken lpseToken : lpseTokens) {

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
				lpseTokenTemp.sort((lpseToken1, lpseToken2) -> {
					if (lpseToken1 == null ||
						lpseToken2 == null ||
						!lpseToken1.getIsValid() ||
						lpseToken1.getValue() == null ||
						lpseToken2.getValue() == null ||
						!lpseToken2.getIsValid()) {
						// either one is invalid and won't be sent through or generate a
						// canonical URI
						return (0);
					} else {
						return (lpseToken1.getValue().compareTo(lpseToken2.getValue()));
					}
				});
			}
		}

		panlObject.put(Constants.Json.Panl.AVAILABLE, availableProcessor.processToObject(panlTokenMap, solrQueryResponse));

		// now we are going to add the dynamic range if they exist
		JSONObject statsObject = solrJsonObject.optJSONObject(Constants.Json.Solr.STATS);
		if (null != statsObject) {
			JSONObject statsFieldObjects = statsObject.optJSONObject(Constants.Json.Solr.STATS_FIELDS);
			if (null != statsFieldObjects) {
				Iterator<String> keys = statsFieldObjects.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					JSONObject valueObject = statsFieldObjects.getJSONObject(key);
					// now that we have the value, go through the range facets and get the right one.
					JSONArray jsonArray = panlObject.getJSONObject(Constants.Json.Panl.AVAILABLE)
					                                .getJSONArray(Constants.Json.Panl.RANGE_FACETS);
					for (Object object : jsonArray) {
						JSONObject rangeObject = (JSONObject) object;
						if (rangeObject.getString(Constants.Json.Panl.FACET_NAME).equals(key)) {
							rangeObject.put(Constants.Json.Panl.DYNAMIC_MIN, valueObject.optInt(Constants.Json.Panl.MIN, -1));
							rangeObject.put(Constants.Json.Panl.DYNAMIC_MAX, valueObject.optInt(Constants.Json.Panl.MAX, -1));
						}
					}
				}
			}
		}

		solrJsonObject.remove(Constants.Json.Solr.STATS);

		if(this.panlProperties.getRemoveSolrJsonKeys()) {
			solrJsonObject.remove(Constants.Json.Solr.FACET_COUNTS);
			solrJsonObject.getJSONObject(Constants.Json.Solr.RESPONSE_HEADER).remove(Constants.Json.Solr.PARAMS);
		}

		// now we need to go through the range facets and remove any that are
		// suppressed

		JSONArray removedRanges = new JSONArray();
		for (Object jsonObject : panlObject.getJSONObject(Constants.Json.Panl.AVAILABLE).getJSONArray(Constants.Json.Panl.FACETS)) {
			JSONObject facetObject = (JSONObject) jsonObject;
			String lpseCode = facetObject.getString(Constants.Json.Panl.PANL_CODE);
			if (!collectionProperties.getIsSuppressedRangeFacet(lpseCode)) {
				removedRanges.put(facetObject);
			}
		}

		panlObject.getJSONObject(Constants.Json.Panl.AVAILABLE).put(Constants.Json.Panl.FACETS, removedRanges);

		panlObject.put(Constants.Json.Panl.ACTIVE, activeProcessor.processToObject(panlTokenMap));
		panlObject.put(Constants.Json.Panl.SEARCH, searchFieldsProcessor.processToObject(panlTokenMap));
		panlObject.put(Constants.Json.Panl.PAGINATION, paginationProcessor.processToObject(panlTokenMap, solrQueryResponse));
		panlObject.put(Constants.Json.Panl.SORTING, sortingProcessor.processToObject(panlTokenMap));
		panlObject.put(Constants.Json.Panl.QUERY_OPERAND, queryOperandProcessor.processToObject(panlTokenMap));
		panlObject.put(Constants.Json.Panl.FIELDS, fieldsProcessor.processToObject(panlTokenMap, fieldSet));
		panlObject.put(Constants.Json.Panl.CANONICAL_URI, canonicalURIProcessor.processToString(panlTokenMap));

		// now add in the timings
		JSONObject timingsObject = new JSONObject();

		long buildResponseTime = System.nanoTime() - startNanos;

		// add in some statistics
		timingsObject.put(Constants.Json.Panl.PARSE_REQUEST_TIME,
				TimeUnit.NANOSECONDS.toMillis(parseRequestNanos));
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

		solrJsonObject.put(Constants.Json.Response.ERROR, false);

		JSONArray facetOrderJsonArray = collectionProperties.getPanlLpseFacetOrderJsonArray();
		// TODO - we possibly need to remove the facets which are not active -
		// TODO - this could be done on the front end and would make it faster
		// TODO - rather than done server side.
		panlObject.put(Constants.Json.Panl.FACETORDER, facetOrderJsonArray);

		// now put the panl to solr field mappings in
		solrJsonObject.put(Constants.Json.Panl.PANL, panlObject);

		// Add the 'extra' object - if it exists
		if(null != collectionProperties.getJsonExtraObject()) {
			panlObject.put(Constants.Json.Panl.EXTRA, collectionProperties.getJsonExtraObject());
		}

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
	 *  query will replace any existing query in 	the lpse encoded URI
	 *
	 * @return The parse URI as a List of <code>PanlToken</code>
	 */
	public List<LpseToken> parseLpse(String uri, String query) {
		List<LpseToken> lpseTokens = new ArrayList<>();
		Set<String> existingTokens = new HashSet<>();

		String[] lpseUriPath = uri.split("/");

		boolean hasQueryParam = false;
		String queryParam = "";
		String queryOperand = null;

		List<NameValuePair> parse = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
		for (NameValuePair nameValuePair : parse) {
			if (nameValuePair.getName().equals(collectionProperties.getFormQueryRespondTo())) {
				hasQueryParam = true;
				queryParam = nameValuePair.getValue();
				continue;
			}

			if (nameValuePair.getName().equals(collectionProperties.getFormQueryOperand())) {
				queryOperand = nameValuePair.getValue();
			}
		}

		if (lpseUriPath.length > 3) {
			String lpseEncoding = URLDecoder.decode(lpseUriPath[lpseUriPath.length - 1], StandardCharsets.UTF_8).replaceAll(" ", "+");

			LpseTokeniser lpseTokeniser = new LpseTokeniser(lpseEncoding, CODES_AND_METADATA, true);

			StringTokenizer valueTokeniser = new StringTokenizer(uri, "/", false);
			// we need to skip the first two - as they will be the collection and the
			// field set
			valueTokeniser.nextToken();
			valueTokeniser.nextToken();

			while (lpseTokeniser.hasMoreTokens()) {
				String token = lpseTokeniser.nextToken();

				List<LpseToken> parsedLpseTokens = LpseToken.getLpseTokens(
					collectionProperties,
					token,
					query,
					valueTokeniser,
					lpseTokeniser);

				for (LpseToken lpseToken : parsedLpseTokens) {
					if (lpseToken instanceof QueryLpseToken && hasQueryParam) {
						// at this point we have a query LPSE token, and a query on the URL
						// which will override it.
						lpseToken.setIsValid(false);
						continue;
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
		}

		if (hasQueryParam && !queryParam.isBlank()) {
			lpseTokens.add(new QueryLpseToken(collectionProperties, query, collectionProperties.getPanlParamQuery()));
			if(null != queryOperand) {
				lpseTokens.add(new QueryOperandLpseToken(collectionProperties, collectionProperties.getPanlParamQueryOperand(), queryOperand));
			}
		}

		for (LpseToken lpseToken : lpseTokens) {
			LOGGER.debug(lpseToken.explain());
		}

		return (lpseTokens);
	}

	/**
	 * <p>Get the valid URLs as a JSON array.</p>
	 *
	 * @return The valid URLs as a JSON array
	 */
	public JSONArray getValidUrls() {
		return (collectionProperties.getValidUrls());
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
	 * used for debugging/explanation/information usage with the Panl results explainer web app.</p>
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
	 * @return The names for the result fields that will be returned with this handler.
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

	/**
	 * <p>Get the defined LPSE order.</p>
	 *
	 * @return The List of the LPSE order
	 */
	public List<String> getLpseOrder() {
		return (collectionProperties.getPanlLpseOrderList());
	}

	/**
	 * <p>Return the URL parameter key that this request handler will respond to</p>
	 *
	 * @return The URL parameter key to respond to
	 */
	public String getFormQueryRespondTo() {
		return (collectionProperties.getFormQueryRespondTo());
	}

	/**
	 * <p>Return the collection properties for this handler</p>
	 *
	 * @return The Collection properties for this handler
	 */
	public CollectionProperties getCollectionProperties() {
		return collectionProperties;
	}

	/**
	 * <p>Return the Panl Client</p>
	 *
	 * @return The panl Client
	 */
	public PanlClient getPanlClient() {
		return panlClient;
	}
}
