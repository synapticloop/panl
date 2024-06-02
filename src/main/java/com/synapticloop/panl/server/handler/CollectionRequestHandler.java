package com.synapticloop.panl.server.handler;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.generator.bean.Collection;
import com.synapticloop.panl.server.client.*;
import com.synapticloop.panl.server.handler.helper.CollectionHelper;
import com.synapticloop.panl.server.properties.PanlProperties;
import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.tokeniser.PanlTokeniser;
import com.synapticloop.panl.server.tokeniser.token.*;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
	public static final String SOLR_PARAM_Q_OP = "q.op";

	private final String collectionName;
	private final CollectionProperties collectionProperties;
	private final PanlClient panlClient;

	public CollectionRequestHandler(String collectionName, PanlProperties panlProperties, CollectionProperties collectionProperties) throws PanlServerException {
		LOGGER.info("[{}] Initialising collection", collectionName);

		this.collectionName = collectionName;
		this.collectionProperties = collectionProperties;

		panlClient = CollectionHelper.getPanlClient(panlProperties.getSolrjClient(), collectionName, panlProperties, collectionProperties);
	}


	public String handleRequest(String uri, String query) throws PanlServerException {
		long startNanos = System.nanoTime();

		String[] searchQuery = uri.split("/");
		String resultFields = searchQuery[2];

		List<LpseToken> lpseTokens = parseLpse(uri, query);

		long parseRequestNanos = System.nanoTime() - startNanos;

		startNanos = System.nanoTime();

		try (SolrClient solrClient = panlClient.getClient()) {

			// we set the default query - to be overridden later if one exists
			// TODO - get rid of this
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
			for (LpseToken lpseToken : lpseTokens) {
				if (lpseToken instanceof NumRowsLpseToken) {
					numRows = ((NumRowsLpseToken) lpseToken).getNumRows();
				} else if (lpseToken instanceof PageLpseToken) {
					pageNum = ((PageLpseToken) lpseToken).getPageNum();
				}
				lpseToken.applyToQuery(solrQuery);
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
			throw new PanlServerException("Could not query the Solr instance, message was: " + e.getMessage(), e);
		}
	}

	/**
	 * <p>Parse the solrj response and add the panl information to it</p>
	 *
	 * @param lpseTokens          The parsed URI and panl tokens
	 * @param response            The Solrj response to be parsed
	 * @param parseRequestNanos   The start time for this query in nanoseconds
	 * @param buildRequestNanos   The number of nanos it took to build the request
	 * @param sendAndReceiveNanos The number of nanos it took to send the request
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
		JSONObject availableObjects = new JSONObject();


		long startNanos = System.nanoTime();


		// set up the data structures
		Map<String, Set<String>> panlLookupMap = new HashMap<>();
		for (LpseToken lpseToken : lpseTokens) {
			String panlLpseValue = lpseToken.getValue();
			if (null != panlLpseValue) {
				String panlLpseCode = lpseToken.getLpseCode();
				Set<String> valueSet = panlLookupMap.get(panlLpseCode);

				if (null == valueSet) {
					valueSet = new HashSet<>();
				}
				valueSet.add(panlLpseValue);
				panlLookupMap.put(panlLpseCode, valueSet);
			}
		}


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

		for (String key : panlTokenMap.keySet()) {
			List<LpseToken> lpseTokenTemp = panlTokenMap.get(key);
			lpseTokenTemp.sort(Comparator.comparing(LpseToken::getValue));
		}

		SolrDocumentList solrDocuments = (SolrDocumentList) response.getResponse().get("response");
		long numFound = solrDocuments.getNumFound();
		boolean numFoundExact = solrDocuments.getNumFoundExact();
		long start = solrDocuments.getStart();

		//


		JSONArray panlFacets = new JSONArray();
		Map<String, JSONObject> panlFacetOrderMap = new HashMap<>();

		for (FacetField facetField : response.getFacetFields()) {
			if (facetField.getValueCount() != 0) {
				JSONObject facetObject = new JSONObject();
				facetObject.put("facet_name", facetField.getName());
				facetObject.put("name", collectionProperties.getPanlNameFromSolrFacetName(facetField.getName()));

				JSONArray facetValueArrays = new JSONArray();
				String panlCodeFromSolrFacetName = collectionProperties.getPanlCodeFromSolrFacetName(facetField.getName());
				facetObject.put("panl_code", panlCodeFromSolrFacetName);
				for (FacetField.Count value : facetField.getValues()) {
					// at this point - we need to see whether we already have the 'value'
					// as a facet - as there is no need to have it again
					boolean shouldAdd = true;

					String valueName = value.getName();

					if (panlLookupMap.containsKey(panlCodeFromSolrFacetName)) {
						if (panlLookupMap.get(panlCodeFromSolrFacetName).contains(valueName)) {
							shouldAdd = false;
						}
					}

					// also, if the count of the number of found results is the same as
					// the number of the count of the facet - then we may not need to
					// include it
					if (!collectionProperties.getPanlIncludeSameNumberFacets() &&
							numFound == value.getCount() &&
							numFoundExact) {
						shouldAdd = false;
					}


					if (shouldAdd) {
						JSONObject facetValueObject = new JSONObject();
						facetValueObject.put("value", valueName);
						facetValueObject.put("count", value.getCount());
						facetValueObject.put("encoded", URLEncoder.encode(
								collectionProperties.getConvertedToPanlValue(
										panlCodeFromSolrFacetName,
										valueName), StandardCharsets.UTF_8));
						facetValueArrays.put(facetValueObject);
					}
				}

				int length = facetValueArrays.length();
				boolean shouldIncludeFacet = true;
				switch (length) {
					case 0:
						shouldIncludeFacet = false;
						break;
					case 1:
						shouldIncludeFacet = collectionProperties.getPanlIncludeSingleFacets();
						break;
				}

				// if we don't have any values for this facet, don't put it in

				if (shouldIncludeFacet) {
					facetObject.put("values", facetValueArrays);
					if (null != panlCodeFromSolrFacetName) {
						facetObject.put("uris",
								getAdditionURI(
										panlCodeFromSolrFacetName,
										panlTokenMap));
					}
					panlFacetOrderMap.put(panlCodeFromSolrFacetName, facetObject);
				}
			}
		}

		// put it into the facet array in the requested order
		for (String lpseCode : collectionProperties.getLpseOrder()) {
			if (panlFacetOrderMap.containsKey(lpseCode)) {
				panlFacets.put(panlFacetOrderMap.get(lpseCode));
			}
		}

		availableObjects.put("facets", panlFacets);

		panlObject.put("active", getRemovalURIPaths(lpseTokens));
		panlObject.put("available", panlFacets);
		panlObject.put("pagination", getPaginationURIPaths(lpseTokens, panlTokenMap, numFound));
		panlObject.put("sorting", getSortingURIPaths(lpseTokens, panlTokenMap));

		solrJsonObject.put("error", false);

		JSONObject timingsObject = new JSONObject();
		// add in some statistics
		timingsObject.put("panl_parse_request_time", TimeUnit.NANOSECONDS.toMillis(parseRequestNanos));
		timingsObject.put("panl_build_request_time", TimeUnit.NANOSECONDS.toMillis(buildRequestNanos));
		timingsObject.put("panl_send_request_time", TimeUnit.NANOSECONDS.toMillis(sendAndReceiveNanos));

		long buildResponse = System.nanoTime() - startNanos;
		timingsObject.put("panl_build_response_time", TimeUnit.NANOSECONDS.toMillis(buildResponse));
		timingsObject.put("panl_total_time", TimeUnit.NANOSECONDS.toMillis(
				parseRequestNanos +
						buildRequestNanos +
						sendAndReceiveNanos +
						buildResponse
		));
		panlObject.put("timings", timingsObject);

		panlObject.put("canonical_uri", getCanonicalUri(panlTokenMap));

		// last thing - we want to put the panl to solr field mappings in
		JSONObject solrFieldMappings = new JSONObject();
		// TODO - this should be statically generated at runtime
		for (String solrFacetField : collectionProperties.getSolrFacetFields()) {
			solrFieldMappings.put(solrFacetField, collectionProperties.getPanlNameFromSolrFacetName(solrFacetField));
		}

		panlObject.put("fields", solrFieldMappings);
		solrJsonObject.put("panl", panlObject);

		return (solrJsonObject.toString());
	}

	/**
	 * <p>There are two Sorting URIs - An additive URI, and a replacement URI,
	 * unlike other LPSE codes - these are a finite, set number of sort fields
	 * which are defined by the panl.sort.fields property.</p>
	 *
	 * @param lpseTokens
	 * @param panlTokenMap
	 * @return
	 */
	private JSONObject getSortingURIPaths(List<LpseToken> lpseTokens, Map<String, List<LpseToken>> panlTokenMap) {
		String before = "";
		String after = "";
		String panlLpseCode = collectionProperties.getPanlParamSort();

		// Run through the sorting order
		JSONObject jsonObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder("/");
		StringBuilder lpse = new StringBuilder();

		for (String lpseOrder : collectionProperties.getLpseOrder()) {
			// because the sort order does not have any URI path component, we can
			// just add all of the URI components to it safely
			if (panlTokenMap.containsKey(lpseOrder)) {
				for (LpseToken token : panlTokenMap.get(lpseOrder)) {
					lpseUri.append(token.getResetUriPathComponent());
					lpse.append(token.getLpseComponent());
				}
				}
			if (!panlLpseCode.equals(lpseOrder)) {
				// then add the LPSE code to the lpse stringbuffer

			} else {
				// we have found the sort order token
				before = lpse.toString();
				lpse.setLength(0);
			}
		}
		lpse.append("/");

		// no go through all of the sort fields
		String finalBefore = lpseUri + before + collectionProperties.getPanlParamSort();

		JSONObject relevanceSort = new JSONObject();
		relevanceSort.put("replace_desc",  finalBefore + "+" + lpse);
		relevanceSort.put("replace_asc", finalBefore  + "-" + lpse);
		relevanceSort.put("name", "Relevance");
		jsonObject.put("relevance", relevanceSort);

		JSONObject sortFieldsObject = new JSONObject();

		for (String sortFieldLpse : collectionProperties.getSortFields()) {
			JSONObject sortObject = new JSONObject();
			String sortFieldName = collectionProperties.getSolrFacetNameFromPanlLpseCode(sortFieldLpse);
			sortObject.put("name", collectionProperties.getPanlNameFromPanlCode(sortFieldLpse));
			sortObject.put("replace_desc", finalBefore + sortFieldLpse + "+" + lpse);
			sortObject.put("replace_asc", finalBefore + sortFieldLpse + "-" + lpse);
			sortFieldsObject.put(sortFieldName, sortObject);
		}

		jsonObject.put("fields", sortFieldsObject);

		return (jsonObject);
	}

	private String getCanonicalUri(Map<String, List<LpseToken>> panlTokenMap) {
		StringBuilder lpseUri = new StringBuilder("/");
		StringBuilder lpse = new StringBuilder();

		for (String lpseOrder : collectionProperties.getLpseOrder()) {
			if (panlTokenMap.containsKey(lpseOrder)) {
				for (LpseToken token : panlTokenMap.get(lpseOrder)) {
					lpseUri.append(token.getResetUriPathComponent());
					lpse.append(token.getLpseComponent());
				}
			}
		}

		// we need to add all metadata components to the URI path as well

		String retVal = lpseUri.toString() + lpse + "/";
		if (retVal.length() == 2) {
			// in this instance, there is no URI paths, or LPSE codes, so just
			// return a single slash
			return ("/");
		}

		return retVal;
	}

	private JSONObject getPaginationURIPaths(List<LpseToken> lpseTokens, Map<String, List<LpseToken>> panlTokenMap, long numFound) {
		JSONObject paginationObject = new JSONObject();
		JSONObject jsonObject = new JSONObject();

		// get the page number and num results per page
		long numPerPage = 0;
		long pageNum = 0;
		for (LpseToken lpseToken : lpseTokens) {
			if (lpseToken instanceof NumRowsLpseToken) {
				numPerPage = ((NumRowsLpseToken) lpseToken).getNumRows();
			} else if (lpseToken instanceof PageLpseToken) {
				pageNum = ((PageLpseToken) lpseToken).getPageNum();
			}
		}

		// we may not have a numrows start
		if (numPerPage == 0) {
			numPerPage = collectionProperties.getNumResultsPerPage();
		}
		if (pageNum == 0) {
			pageNum = 1;
		}

		paginationObject.put("num_per_page", numPerPage);
		paginationObject.put("page_num", pageNum);
		long numPages = numFound / numPerPage;
		if (numFound % numPerPage != 0) {
			numPages++;
		}

		paginationObject.put("num_pages", numPages);

		// now for the pagination URI path
		StringBuilder lpseUri = new StringBuilder("/");
		StringBuilder lpse = new StringBuilder();

		String panlLpseCode = collectionProperties.getPanlParamPage();

		for (String lpseOrder : collectionProperties.getLpseOrder()) {
			// if the current panl token's lpse matches that of the panlLpseOrder,
			// then we need to add to lpseCode and the uri
			if (panlLpseCode.equals(lpseOrder)) {
				jsonObject.put("before", lpseUri + collectionProperties.getPrefixForLpseCode(panlLpseCode));
				// clear the sting builder
				lpseUri.setLength(0);
				lpse.append(panlLpseCode);
			} else {
				if (panlTokenMap.containsKey(lpseOrder)) {
					// this is not additive - it is replacement
					for (LpseToken token : panlTokenMap.get(lpseOrder)) {
						lpseUri.append(token.getUriPathComponent());
						lpse.append(token.getLpseComponent());
					}
				}
			}
		}

		jsonObject.put("after", collectionProperties.getSuffixForLpseCode(panlLpseCode) + "/" + lpseUri + lpse + "/");

		if (pageNum < numPages) {
			jsonObject.put("next", jsonObject.getString("before") + (pageNum + 1) + jsonObject.getString("after"));
		}

		if (pageNum > 1) {
			jsonObject.put("previous", jsonObject.getString("before") + (pageNum - 1) + jsonObject.getString("after"));
		}

		paginationObject.put("page_uris", jsonObject);
		paginationObject.put("num_per_page_uris",
				getReplacementURI(
						collectionProperties.getPanlParamNumRows(),
						panlTokenMap));

		return (paginationObject);
	}

	private JSONObject getRemovalURIPaths(List<LpseToken> lpseTokens) {
		JSONObject jsonObject = new JSONObject();

		// go through each of the tokens and generate the removal URL

		List<String> uriComponents = new ArrayList<>();
		List<String> lpseComponents = new ArrayList<>();

		for (LpseToken lpseToken : lpseTokens) {
			uriComponents.add(lpseToken.getUriPathComponent());
			lpseComponents.add(lpseToken.getLpseComponent());
		}
		int i = 0;
		for (LpseToken lpseToken : lpseTokens) {
			String tokenType = lpseToken.getType();
			JSONArray jsonArray = jsonObject.optJSONArray(tokenType, new JSONArray());

			JSONObject removeObject = new JSONObject();
			removeObject.put("value", lpseToken.getValue());
			removeObject.put("uri", getRemoveURIFromPath(i, uriComponents, lpseComponents));

			String panlLpseCode = lpseToken.getLpseCode();
			removeObject.put("panl_code", panlLpseCode);
			removeObject.put("facet_name", collectionProperties.getNameFromCode(panlLpseCode));
			removeObject.put("name", collectionProperties.getPanlNameFromPanlCode(panlLpseCode));
			jsonArray.put(removeObject);
			i++;
			jsonObject.put(tokenType, jsonArray);
		}
		return (jsonObject);
	}

	private String getRemoveURIFromPath(int skipNumber, List<String> uriComponents, List<String> lpseComponents) {
		StringBuilder uri = new StringBuilder();
		StringBuilder lpse = new StringBuilder();
		for (int i = 0; i < uriComponents.size(); i++) {
			if (i != skipNumber) {
				uri.append(uriComponents.get(i));
				lpse.append(lpseComponents.get(i));
			}
		}

		String test = "/" + uri + lpse + "/";

		if (test.equals("//")) {
			return ("/");
		} else {
			return test;
		}
	}

	/**
	 * <p>Get the addition URI for facets.  The addition URI will always reset
	 * the page number LPSE code</p>
	 *
	 * @param panlLpseCode The LPSE code to add to the URI
	 * @param panlTokenMap The Map of existing tokens that are already in the URI
	 * @return The addition URI
	 */
	private JSONObject getAdditionURI(String panlLpseCode, Map<String, List<LpseToken>> panlTokenMap) {
		JSONObject jsonObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder("/");
		StringBuilder lpse = new StringBuilder();

		for (String lpseOrder : collectionProperties.getLpseOrder()) {
			// do we currently have some codes for this?

			if (panlTokenMap.containsKey(lpseOrder)) {
				// TODO - need to order these alphabetically...
				for (LpseToken token : panlTokenMap.get(lpseOrder)) {
					lpseUri.append(token.getResetUriPathComponent());
					lpse.append(token.getLpseComponent());
				}
			}

			// if the current panl token's lpse matches that of the panlLpseOrder,
			// then we need to add to lpseCode and the uri
			if (panlLpseCode.equals(lpseOrder)) {
				jsonObject.put("before", lpseUri.toString());
				// clear the sting builder
				lpseUri.setLength(0);
				lpse.append(panlLpseCode);
			}
		}

		jsonObject.put("after", "/" + lpseUri + lpse + "/");
		return (jsonObject);
	}

	private JSONObject getReplacementURI(String panlLpseCode, Map<String, List<LpseToken>> panlTokenMap) {
		JSONObject jsonObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder("/");
		StringBuilder lpse = new StringBuilder();

		for (String lpseOrder : collectionProperties.getLpseOrder()) {
			if (!panlLpseCode.equals(lpseOrder)) {
				if (panlTokenMap.containsKey(lpseOrder)) {
					for (LpseToken token : panlTokenMap.get(lpseOrder)) {
						lpseUri.append(token.getResetUriPathComponent());
						lpse.append(token.getLpseComponent());
					}
				}
			} else {
				// if the current panl token's lpse matches that of the panlLpseOrder,
				// then we need to add to lpseCode and the uri
				jsonObject.put("before", lpseUri + collectionProperties.getPrefixForLpseCode(panlLpseCode));
				// clear the sting builder
				lpseUri.setLength(0);
				lpse.append(panlLpseCode);
			}
		}

		jsonObject.put("after", collectionProperties.getSuffixForLpseCode(panlLpseCode) + "/" + lpseUri + lpse + "/");
		return (jsonObject);
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
	 * @param uri   The URI to parse
	 * @param query the query to parse - if the query string exists, then this
	 *              query will replace any existing query in the lpse encoded
	 *              URI
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

				if (token.equals(collectionProperties.getPanlParamQuery())) {
					hasQuery = true;
					lpseTokens.add(
							new QueryLpseToken(
									query,
									token,
									valueTokeniser));

				} else if (token.equals(collectionProperties.getPanlParamSort())) {
					lpseTokens.add(
									new SortLpseToken(
													collectionProperties,
													token,
													lpseTokeniser));

				} else if (token.equals(collectionProperties.getPanlParamQueryOperand())) {
					lpseTokens.add(
									new QueryOperandLpseToken(
													collectionProperties,
													token,
													lpseTokeniser));

				} else if (token.equals(collectionProperties.getPanlParamNumRows())) {
					lpseTokens.add(
							new NumRowsLpseToken(
									collectionProperties,
									token,
									valueTokeniser));
				} else if (token.equals(collectionProperties.getPanlParamPage())) {
					lpseTokens.add(
							new PageLpseToken(
									collectionProperties,
									token,
									valueTokeniser));
				} else if (token.equals(collectionProperties.getPanlParamPassthrough())) {
					lpseTokens.add(
							new PassthroughLpseToken(
									collectionProperties,
									token,
									valueTokeniser));
				} else {
					StringBuilder facet = new StringBuilder(token);
					// it is a facet field
					while (token.length() < collectionProperties.getPanlLpseNum()) {
						facet.append(lpseTokeniser.nextToken());
					}

					// now we have the facetField
					lpseTokens.add(
							new FacetLpseToken(
									collectionProperties,
									facet.toString(),
									lpseTokeniser,
									valueTokeniser));
				}
			}

			// If we don't have a query - then parse the query
		}
		if (!hasQuery && !query.isBlank()) {
			lpseTokens.add(new QueryLpseToken(query, collectionProperties.getPanlParamQuery()));
		}

		for (LpseToken lpseToken : lpseTokens) {
			System.out.println(lpseToken.explain());
		}

		return (lpseTokens);
	}

	public String getValidUrlsJSON() {
		return (collectionProperties.getValidUrlsJson());
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
