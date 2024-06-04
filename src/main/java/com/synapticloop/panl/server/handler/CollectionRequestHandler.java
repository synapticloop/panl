package com.synapticloop.panl.server.handler;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.generator.bean.Collection;
import com.synapticloop.panl.server.client.PanlClient;
import com.synapticloop.panl.server.handler.helper.CollectionHelper;
import com.synapticloop.panl.server.handler.processor.*;
import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.properties.PanlProperties;
import com.synapticloop.panl.server.properties.field.BaseField;
import com.synapticloop.panl.server.tokeniser.PanlTokeniser;
import com.synapticloop.panl.server.tokeniser.token.*;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>The collection request handler acts as the intermediary between the
 * incoming http request and the underlying SOLR server.</p>
 *
 * @author synapticloop
 */
public class CollectionRequestHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionRequestHandler.class);

	public static final String SOLR_PARAM_Q_OP = "q.op";

	private final String collectionName;
	private final CollectionProperties collectionProperties;
	private final PanlClient panlClient;

	private final ActiveProcessor activeProcessor;
	private final PaginationProcessor paginationProcessor;
	private final TimingsProcessor timingsProcessor;
	private final SortingProcessor sortingProcessor;
	private final QueryOperandProcessor queryOperandProcessor;
	private final FieldsProcessor fieldsProcessor;
	private final AvailableProcessor availableProcessor;
	private final CanonicalURIProcessor canonicalURIProcessor;

	public CollectionRequestHandler(String collectionName, PanlProperties panlProperties, CollectionProperties collectionProperties) throws PanlServerException {
		LOGGER.info("[{}] Initialising collection", collectionName);

		this.collectionName = collectionName;
		this.collectionProperties = collectionProperties;

		panlClient = CollectionHelper.getPanlClient(panlProperties.getSolrjClient(), collectionName, panlProperties, collectionProperties);

		this.activeProcessor = new ActiveProcessor(collectionProperties);
		this.paginationProcessor = new PaginationProcessor(collectionProperties);
		this.timingsProcessor = new TimingsProcessor(collectionProperties);
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

			// this may be overridden by the lpse status
			solrQuery.setRows(collectionProperties.getNumResultsPerPage());

			solrQuery.addFacetField(collectionProperties.getSolrFacetFields());

			// now we need to go through the panl facets and add them

			int numRows = 0;
			int pageNum = 0;

			// set up the data structure
			Map<String, List<LpseToken>> panlTokenMap = new HashMap<>();

			for (LpseToken lpseToken : lpseTokens) {
				String panlLpseCode = lpseToken.getLpseCode();

				List<LpseToken> lpseTokenList = panlTokenMap.get(panlLpseCode);
				if (null == lpseTokenList) {
					lpseTokenList = new ArrayList<>();
				}
				lpseTokenList.add(lpseToken);
				panlTokenMap.put(panlLpseCode, lpseTokenList);

				if (lpseToken instanceof NumRowsLpseToken) {
					numRows = ((NumRowsLpseToken) lpseToken).getNumRows();
				} else if (lpseToken instanceof PageLpseToken) {
					pageNum = ((PageLpseToken) lpseToken).getPageNum();
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
			final QueryResponse response = solrClient.query(this.collectionName, solrQuery);

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
			String panlLpseCode = lpseToken.getLpseCode();

			List<LpseToken> lpseTokenList = panlTokenMap.get(panlLpseCode);
			if (null == lpseTokenList) {
				lpseTokenList = new ArrayList<>();
			}
			lpseTokenList.add(lpseToken);
			panlTokenMap.put(panlLpseCode, lpseTokenList);
		}

		// we are going to sort the tokens alphabetically by value to build the
		// canonical URI
		for (String key : panlTokenMap.keySet()) {
			List<LpseToken> lpseTokenTemp = panlTokenMap.get(key);
			lpseTokenTemp.sort(Comparator.comparing(LpseToken::getValue));
		}

		SolrDocumentList solrDocuments = (SolrDocumentList) response.getResponse().get("response");
		long numFound = solrDocuments.getNumFound();

		panlObject.put("available", availableProcessor.processToArray(panlTokenMap, response));
		panlObject.put("active", activeProcessor.processToObject(panlTokenMap));
		panlObject.put("pagination", paginationProcessor.processToObject(panlTokenMap, numFound ));
		panlObject.put("timings", timingsProcessor.processToObject(panlTokenMap, parseRequestNanos, buildRequestNanos, sendAndReceiveNanos, System.nanoTime() - startNanos));
		panlObject.put("sorting", sortingProcessor.processToObject(panlTokenMap));
		panlObject.put("query_operand", queryOperandProcessor.processToObject(panlTokenMap));
		panlObject.put("fields", fieldsProcessor.processToObject(panlTokenMap));
		panlObject.put("canonical_uri", canonicalURIProcessor.processToString(panlTokenMap));

		solrJsonObject.put("error", false);

		// last thing - we want to put the panl to solr field mappings in
		solrJsonObject.put("panl", panlObject);

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
	 * /&lt;collection_name&gt;
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

		String[] searchQuery = uri.split("/");

		boolean hasQuery = false;

		if (searchQuery.length > 3) {
			String lpseEncoding = searchQuery[searchQuery.length - 1];

			PanlTokeniser lpseTokeniser = new PanlTokeniser(lpseEncoding, Collection.CODES_AND_METADATA, true);

			StringTokenizer valueTokeniser = new StringTokenizer(uri, "/", false);
			// we need to skip the first two - as they will be the collection and the
			// field set
			valueTokeniser.nextToken();
			valueTokeniser.nextToken();

			while (lpseTokeniser.hasMoreTokens()) {
				String token = lpseTokeniser.nextToken();
				LpseToken lpseToken = LpseToken.getLpseToken(collectionProperties, token, query, valueTokeniser, lpseTokeniser);

				// TODO - some sort of logic here...
				if (!hasQuery && !query.isBlank()) {
					lpseTokens.add(new QueryLpseToken(query, collectionProperties.getPanlParamQuery()));
				}

				lpseTokens.add(lpseToken);
			}
		}

		for (LpseToken lpseToken : lpseTokens) {
			System.out.println(lpseToken.explain());
		}

		return (lpseTokens);
	}

	public String getValidUrlString() {
		return (collectionProperties.getValidUrlsString());
	}

	public boolean isValidResultsFields(String path) {
		return (collectionProperties.isValidResultFieldsName(path));
	}

	public String getCollectionName() {
		return collectionName;
	}

	public List<String> getResultFieldsNames() {
		return (new ArrayList<>(collectionProperties.getResultFieldsNames()));
	}

}
