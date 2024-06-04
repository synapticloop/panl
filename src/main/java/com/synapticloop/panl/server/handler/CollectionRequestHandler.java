package com.synapticloop.panl.server.handler;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.generator.bean.Collection;
import com.synapticloop.panl.server.client.*;
import com.synapticloop.panl.server.handler.helper.CollectionHelper;
import com.synapticloop.panl.server.properties.PanlProperties;
import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.properties.field.BaseField;
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

		// we are going to sort the tokens alphabetically by value to build the
		// canonical URI
		for (String key : panlTokenMap.keySet()) {
			List<LpseToken> lpseTokenTemp = panlTokenMap.get(key);
			lpseTokenTemp.sort(Comparator.comparing(LpseToken::getValue));
		}

		SolrDocumentList solrDocuments = (SolrDocumentList) response.getResponse().get("response");
		long numFound = solrDocuments.getNumFound();
		boolean numFoundExact = solrDocuments.getNumFoundExact();


		JSONArray panlFacets = new JSONArray();
		Map<String, JSONObject> panlFacetOrderMap = new HashMap<>();

		populateDataStructures(
				response,
				panlLookupMap,
				numFound,
				numFoundExact,
				panlTokenMap,
				panlFacetOrderMap);

		// put it into the facet array in the requested order

		for (String lpseCode : collectionProperties.getLpseOrder()) {
			if (panlFacetOrderMap.containsKey(lpseCode)) {
				panlFacets.put(panlFacetOrderMap.get(lpseCode));
			}
		}

		availableObjects.put("facets", panlFacets);

//		panlObject.put("active", getRemovalURIPaths(lpseTokens));
		panlObject.put("active", getRemovalURIObject(lpseTokens));
		panlObject.put("available", panlFacets);
		panlObject.put("pagination", getPaginationURIObject(panlTokenMap, numFound));
		panlObject.put("sorting", getSortingURIPaths(panlTokenMap));

		panlObject.put("query_operand", getQueryOperandURIPaths(panlTokenMap));

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

		panlObject.put("canonical_uri", getCanonicalUriPath(panlTokenMap));

		// last thing - we want to put the panl to solr field mappings in
		panlObject.put("fields", collectionProperties.getSolrFieldToPanlNameLookup());
		solrJsonObject.put("panl", panlObject);

		return (solrJsonObject.toString());
	}

	private void populateDataStructures(
			QueryResponse response,
			Map<String, Set<String>> panlLookupMap,
			long numFound,
			boolean numFoundExact,
			Map<String, List<LpseToken>> panlTokenMap,
			Map<String, JSONObject> panlFacetOrderMap) {

		for (FacetField facetField : response.getFacetFields()) {
			if (facetField.getValueCount() != 0) {
				JSONObject facetObject = new JSONObject();
				facetObject.put("facet_name", facetField.getName());
				facetObject.put("name", collectionProperties.getPanlNameFromSolrFieldName(facetField.getName()));

				JSONArray facetValueArrays = new JSONArray();
				String panlCodeFromSolrFacetName = collectionProperties.getPanlCodeFromSolrFacetFieldName(facetField.getName());
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
								getAdditionURIObject(
										panlCodeFromSolrFacetName,
										panlTokenMap));
					}
					panlFacetOrderMap.put(panlCodeFromSolrFacetName, facetObject);
				}
			}
		}
	}

	/**
	 * <p>There are two Sorting URIs - An additive URI, and a replacement URI,
	 * unlike other LPSE codes - these are a finite, set number of sort fields
	 * which are defined by the panl.sort.fields property.</p>
	 *
	 * @param panlTokenMap
	 *
	 * @return The JSON object with the keys and relevant URI paths
	 */
	private JSONObject getSortingURIPaths(Map<String, List<LpseToken>> panlTokenMap) {
		String before = "";
		String panlLpseCode = collectionProperties.getPanlParamSort();

		// Run through the sorting order
		JSONObject jsonObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder("/");
		StringBuilder lpse = new StringBuilder();

		for (String lpseOrder : collectionProperties.getLpseOrder()) {
			// because the sort order does not have any URI path component, we can
			// just add the URI components to it safely
			if (!panlLpseCode.equals(lpseOrder)) {
				// keep on adding things
				if (panlTokenMap.containsKey(lpseOrder)) {
					for (LpseToken token : panlTokenMap.get(lpseOrder)) {
						lpseUri.append(token.getResetUriPathComponent());
						// however we do not want to add the lpse component
						lpse.append(token.getLpseComponent());
					}
				}
			} else {
				before = lpse.toString();
				lpse.setLength(0);
			}
		}
		lpse.append("/");

		// This is the default sorting order (by relevance)
		String finalBefore = lpseUri + before + collectionProperties.getPanlParamSort();

		JSONObject relevanceSort = new JSONObject();
		relevanceSort.put("name", "Relevance");
		relevanceSort.put("replace_desc", finalBefore + "-" + lpse);
		jsonObject.put("relevance", relevanceSort);

		// These are the defined sort fields
		JSONObject sortFieldsObject = new JSONObject();

		for (String sortFieldLpseCode : collectionProperties.getSortFieldLpseCodes()) {
			String sortFieldName = collectionProperties.getSolrFieldNameFromPanlLpseCode(sortFieldLpseCode);
			if (null != sortFieldName) {
				JSONObject sortObject = new JSONObject();
				sortObject.put("name", collectionProperties.getPanlNameFromPanlCode(sortFieldLpseCode));
				sortObject.put("replace_desc", finalBefore + sortFieldLpseCode + "-" + lpse);
				sortObject.put("replace_asc", finalBefore + sortFieldLpseCode + "+" + lpse);
				sortFieldsObject.put(sortFieldName, sortObject);
			}
		}

		jsonObject.put("fields", sortFieldsObject);

		return (jsonObject);
	}

	private JSONObject getQueryOperandURIPaths(Map<String, List<LpseToken>> panlTokenMap) {
		String before = "";
		String panlLpseCode = collectionProperties.getPanlParamQueryOperand();

		// Run through the sorting order
		JSONObject jsonObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder("/");
		StringBuilder lpse = new StringBuilder();

		for (String lpseOrder : collectionProperties.getLpseOrder()) {
			// because the sort order does not have any URI path component, we can
			// just add the URI components to it safely
			if (!panlLpseCode.equals(lpseOrder)) {
				// keep on adding things
				if (panlTokenMap.containsKey(lpseOrder)) {
					for (LpseToken token : panlTokenMap.get(lpseOrder)) {
						lpseUri.append(token.getResetUriPathComponent());
						// however we do not want to add the lpse component
						lpse.append(token.getLpseComponent());
					}
				}
			} else {
				before = lpse.toString();
				lpse.setLength(0);
			}
		}
		lpse.append("/");

		// This is the default sorting order (by relevance)
		String finalBefore = lpseUri + before + collectionProperties.getPanlParamQueryOperand();

		jsonObject.put("OR", finalBefore + "-" + lpse);
		jsonObject.put("AND", finalBefore + "+" + lpse);

		return (jsonObject);
	}

	/**
	 * <p>Get the canonical URI Path for the query.  This will always return more
	 * than an empty path as it includes the following default LPSE path
	 * components:</p>
	 *
	 * <ul>
	 *   <li>The Sort order - sorted by relevance by default.</li>
	 *   <li>The page number - page 1 by default</li>
	 *   <li>The number of results per page - this is defined, or 10 by default</li>
	 *   <li>The query operand - this is defined, or 10 by default</li>
	 * </ul>
	 *
	 * <p><strong>NOTE:</strong> The pass-through parameter, if defined, will
	 * never be included.</p>
	 *
	 * @param panlTokenMap The panlToken map for the query
	 *
	 * @return The canonical URI path for this query
	 */
	private String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap) {
		StringBuilder canonicalUri = new StringBuilder("/");
		StringBuilder canonicalLpse = new StringBuilder();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			canonicalUri.append(baseField.getCanonicalUriPath(panlTokenMap, collectionProperties));
			canonicalLpse.append(baseField.getCanonicalLpseCode(panlTokenMap, collectionProperties));
		}

		return (canonicalUri.toString() + canonicalLpse + "/");
	}

	private JSONObject getPaginationURIObject(Map<String, List<LpseToken>> panlTokenMap, long numFound) {

		int numPerPage = collectionProperties.getNumResultsPerPage();
		// If it is set in the URI, get the updated value
		List<LpseToken> lpseTokens = panlTokenMap.getOrDefault(collectionProperties.getPanlParamNumRows(), new ArrayList<>());
		if (!lpseTokens.isEmpty()) {
			int numRows = ((NumRowsLpseToken) lpseTokens.get(0)).getNumRows();
			if (numRows > 0) {
				numPerPage = numRows;
			}
		}

		int pageNumber = 1;
		String panlParamPageLpseCode = collectionProperties.getPanlParamPage();
		lpseTokens = panlTokenMap.getOrDefault(panlParamPageLpseCode, new ArrayList<>());
		if (!lpseTokens.isEmpty()) {
			int pageNum = ((PageLpseToken) lpseTokens.get(0)).getPageNum();
			if (pageNum > 0) {
				pageNumber = pageNum;
			}
		}

		JSONObject paginationObject = new JSONObject();
		paginationObject.put("num_per_page", numPerPage);
		paginationObject.put("page_num", pageNumber);
		long numPages = numFound / numPerPage;
		if (numFound % numPerPage != 0) {
			numPages++;
		}
		paginationObject.put("num_pages", numPages);

		StringBuilder uriPath = new StringBuilder("/");
		StringBuilder lpseCode = new StringBuilder();

		JSONObject pageUris = new JSONObject();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			//
			if (!baseField.getPanlLpseCode().equals(panlParamPageLpseCode)) {
				uriPath.append(baseField.getURIPath(panlTokenMap, collectionProperties));
				lpseCode.append(baseField.getLpseCode(panlTokenMap, collectionProperties));
			} else {
				pageUris.put("before", uriPath + baseField.getPanlPrefix());
				// clear the sting builder
				lpseCode.append(baseField.getPanlLpseCode());
				uriPath.setLength(0);
			}
		}

		BaseField panlPageNumField = collectionProperties.getLpseField(panlParamPageLpseCode);

		String afterValue = panlPageNumField.getPanlSuffix() + "/" + uriPath + lpseCode + "/";
		pageUris.put("after", afterValue);

		if (pageNumber < numPages) {
			pageUris.put("next", pageUris.getString("before") + (pageNumber + 1) + pageUris.getString("after"));
		}

		if (pageNumber > 1) {
			pageUris.put("previous", pageUris.getString("before") + (pageNumber - 1) + pageUris.getString("after"));
		}

		paginationObject.put("page_uris", pageUris);

		paginationObject.put("num_per_page_uris",
				getReplacementResetURIObject(
						collectionProperties.getPanlParamNumRows(),
						panlTokenMap));

		return (paginationObject);
	}

	/**
	 * <p>Get the URI JSONObject with a replacement of a code, resetting all
	 * LPSE codes that are needed.</p>
	 *
	 * <p>This returns a JSON object with two keys:</p>
	 *
	 * <ul>
	 *   <li><code>before</code> The URI path that goes before the value.</li>
	 *   <li><code>after</code> The URI path that goes after the value.</li>
	 * </ul>
	 *
	 * @param replaceLpseCode The LPSE code to be replaced
	 * @param panlTokenMap The tokens parsed from the inbound request
	 *
	 * @return The JSON object
	 */
	private JSONObject getReplacementResetURIObject(String replaceLpseCode, Map<String, List<LpseToken>> panlTokenMap) {
		StringBuilder uriPath = new StringBuilder("/");
		StringBuilder lpseCode = new StringBuilder();

		JSONObject pageUris = new JSONObject();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			if (!baseField.getPanlLpseCode().equals(replaceLpseCode)) {
				uriPath.append(baseField.getResetUriPath(panlTokenMap, collectionProperties));
				lpseCode.append(baseField.getResetLpseCode(panlTokenMap, collectionProperties));
			} else {
				pageUris.put("before", uriPath + baseField.getPanlPrefix());
				// clear the sting builder
				lpseCode.append(baseField.getPanlLpseCode());
				uriPath.setLength(0);
			}
		}

		BaseField baseField = collectionProperties.getLpseField(replaceLpseCode);

		pageUris.put("after", baseField.getPanlSuffix() + "/" + uriPath + lpseCode + "/");

		return (pageUris);
	}

	private JSONObject getRemovalURIObject(List<LpseToken> lpseTokens) {
		JSONObject jsonObject = new JSONObject();

		// go through each of the tokens and generate the removal URL

		List<String> uriComponents = new ArrayList<>();
		List<String> lpseComponents = new ArrayList<>();

		for (LpseToken lpseToken : lpseTokens) {
			BaseField lpseField = collectionProperties.getLpseField(lpseToken.getLpseCode());
			if(null != lpseField) {
				lpseComponents.add(lpseField.getLpseCode(lpseToken, collectionProperties));
				uriComponents.add(lpseField.getURIPath(lpseToken, collectionProperties));
			}
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
			removeObject.put("facet_name", collectionProperties.getSolrFieldNameFromPanlLpseCode(panlLpseCode));
			removeObject.put("name", collectionProperties.getPanlNameFromPanlCode(panlLpseCode));
			jsonArray.put(removeObject);
			i++;
			jsonObject.put(tokenType, jsonArray);
		}
		return (jsonObject);
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
			removeObject.put("facet_name", collectionProperties.getSolrFieldNameFromPanlLpseCode(panlLpseCode));
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
	 * <p>Get the addition URI Object for facets.  The addition URI will always
	 * reset the page number LPSE code</p>
	 *
	 * @param additionLpseCode The LPSE code to add to the URI
	 * @param panlTokenMap The Map of existing tokens that are already in the URI
	 *
	 * @return The addition URI
	 */

	private JSONObject getAdditionURIObject(String additionLpseCode, Map<String, List<LpseToken>> panlTokenMap) {
		JSONObject additionObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder("/");
		StringBuilder lpseCode = new StringBuilder();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			if (panlTokenMap.containsKey(baseField.getPanlLpseCode())) {
				lpseUri.append(baseField.getResetUriPath(panlTokenMap, collectionProperties));
				lpseCode.append(baseField.getResetLpseCode(panlTokenMap, collectionProperties));
			}
			if (baseField.getPanlLpseCode().equals(additionLpseCode)) {
				additionObject.put("before", lpseUri.toString());
				lpseUri.setLength(0);
				lpseCode.append(baseField.getLpseCode(panlTokenMap, collectionProperties));
			}
		}

		additionObject.append("after", "/" + lpseUri + lpseCode + "/");
		return (additionObject);
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
	 * 		query will replace any existing query in the lpse encoded
	 * 		URI
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
				} else if (token.equals(collectionProperties.getPanlParamPassThrough())) {
					lpseTokens.add(
							new PassThroughLpseToken(
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
