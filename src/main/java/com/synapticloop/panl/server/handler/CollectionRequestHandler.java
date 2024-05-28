package com.synapticloop.panl.server.handler;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.generator.bean.Collection;
import com.synapticloop.panl.server.client.*;
import com.synapticloop.panl.server.handler.token.*;
import com.synapticloop.panl.server.properties.BaseProperties;
import com.synapticloop.panl.server.properties.CollectionProperties;
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

	private final String collectionName;
	private final CollectionProperties collectionProperties;
	private final PanlClient panlClient;

	public CollectionRequestHandler(String collectionName, BaseProperties baseProperties, CollectionProperties collectionProperties) throws PanlServerException {
		this.collectionName = collectionName;
		this.collectionProperties = collectionProperties;

		LOGGER.info("[{}] Initialising collection", collectionName);

		String solrjClient = baseProperties.getSolrjClient();
		LOGGER.info("[{}] Utilising solrjClient of '{}'", collectionName, solrjClient);

		switch (solrjClient) {
			case "Http2SolrClient":
				panlClient = new PanlHttp2SolrClient(collectionName, baseProperties, collectionProperties);
				break;
			case "HttpJdkSolrClient":
				panlClient = new PanlHttpJdkSolrClient(collectionName, baseProperties, collectionProperties);
				break;
			case "LBHttp2SolrClient":
				panlClient = new PanlLBHttp2SolrClient(collectionName, baseProperties, collectionProperties);
				break;
			case "CloudSolrClient":
				panlClient = new PanlCloudSolrClient(collectionName, baseProperties, collectionProperties);
				break;
			default:
				throw new PanlServerException("Unknown property value for 'solrj.client' of '" + solrjClient + "', available values are 'Http2SolrClient', 'HttpJdkSolrClient', 'LBHttp2SolrClient', or 'CloudSolrClient'.");
		}
	}


	public String getCollectionName() {
		return collectionName;
	}

	public List<String> getResultFieldsNames() {
		return (new ArrayList<>(collectionProperties.getResultFieldsNames()));
	}


	public String request(String uri, String query) throws PanlServerException {
		long startNanos = System.nanoTime();

		String[] searchQuery = uri.split("/");
		String resultFields = searchQuery[2];

		List<PanlToken> panlTokens = parseLpse(uri, query);

		long parseNanos = System.nanoTime() - startNanos;

		startNanos = System.nanoTime();

		try (SolrClient solrClient = panlClient.getClient()) {

			// we set the default query - to be overridden later if one exists
			// TODO - get rid of this
			SolrQuery solrQuery = panlClient.getQuery(query);
			for (String fieldName : collectionProperties.getResultFieldsForName(resultFields)) {
				solrQuery.addField(fieldName);
			}

			solrQuery.setFacetMinCount(collectionProperties.getFacetMinCount());

			// this may be overridden by the lpse status
			solrQuery.setRows(collectionProperties.getResultRows());

			solrQuery.addFacetField(collectionProperties.getFacetFields());

			// now we need to go through the panl facets and add them

			for (PanlToken panlToken : panlTokens) {
				panlToken.applyToQuery(solrQuery);
			}

			long buildNanos = System.nanoTime() - startNanos;
			startNanos = System.nanoTime();

			// TODO - this needs to be set properly
			solrQuery.setParam("q.op", "AND");

			final QueryResponse response = solrClient.query(this.collectionName, solrQuery);

			long requestNanos = System.nanoTime() - startNanos;
			return (parseResponse(
					panlTokens,
					response,
					parseNanos,
					buildNanos,
					requestNanos));

		} catch (Exception e) {
			throw new PanlServerException("Could not query the Solr instance, message was: " + e.getMessage(), e);
		}
	}

	/**
	 * <p>Parse the solrj response and add the panl information to it</p>
	 *
	 * @param panlTokens        The parsed URI and panl tokens
	 * @param response          The Solrj response to be parsed
	 * @param parseRequestNanos The start time for this query in nanoseconds
	 * @param buildRequestNanos The number of nanos it took to build the request
	 * @param sendRequestNanos  The number of nanos it took to send the request
	 * @return a JSON Object as a string with the appended panl response
	 */
	private String parseResponse(
			List<PanlToken> panlTokens,
			QueryResponse response,
			long parseRequestNanos,
			long buildRequestNanos,
			long sendRequestNanos) {

		// set up the JSON response object
		JSONObject solrJsonObject = new JSONObject(response.jsonStr());
		JSONObject panlObject = new JSONObject();
		JSONObject activeObjects = new JSONObject();
		JSONObject availableObjects = new JSONObject();


		long startNanos = System.nanoTime();


		// set up the data structures
		Map<String, Set<String>> panlLookupMap = new HashMap<>();
		for (PanlToken panlToken : panlTokens) {
			String panlLpseValue = panlToken.getPanlLpseValue();
			if (null != panlLpseValue) {
				String panlLpseCode = panlToken.getPanlLpseCode();
				Set<String> valueSet = panlLookupMap.get(panlLpseCode);

				if (null == valueSet) {
					valueSet = new HashSet<>();
				}
				valueSet.add(panlLpseValue);
				panlLookupMap.put(panlLpseCode, valueSet);
			}
		}

		// set up the data structure
		Map<String, List<PanlToken>> panlTokenMap = new HashMap<>();
		for (PanlToken panlToken : panlTokens) {
			String panlLpseCode = panlToken.getPanlLpseCode();

			List<PanlToken> panlTokenList = panlTokenMap.get(panlLpseCode);
			if (null == panlTokenList) {
				panlTokenList = new ArrayList<>();
			}
			panlTokenList.add(panlToken);
			panlTokenMap.put(panlLpseCode, panlTokenList);
		}


		SolrDocumentList solrDocuments = (SolrDocumentList) response.getResponse().get("response");
		long numFound = solrDocuments.getNumFound();
		long start = solrDocuments.getStart();


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

					if (shouldAdd) {
						JSONObject facetValueObject = new JSONObject();
						facetValueObject.put("value", valueName);
						facetValueObject.put("count", value.getCount());
						facetValueObject.put("encoded", URLEncoder.encode(
								collectionProperties.getPrefixSuffixForValue(
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
										new PanlFacetToken(panlCodeFromSolrFacetName),
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

		JSONObject timingsObject = new JSONObject();
		// add in some statistics
		timingsObject.put("panl_parse_request_time", TimeUnit.NANOSECONDS.toMillis(parseRequestNanos));
		timingsObject.put("panl_build_request_time", TimeUnit.NANOSECONDS.toMillis(buildRequestNanos));
		timingsObject.put("panl_send_request_time", TimeUnit.NANOSECONDS.toMillis(sendRequestNanos));

		long buildResponse = System.nanoTime() - startNanos;
		timingsObject.put("panl_build_response_time", TimeUnit.NANOSECONDS.toMillis(buildResponse));
		timingsObject.put("panl_total_time", TimeUnit.NANOSECONDS.toMillis(
				parseRequestNanos +
						buildRequestNanos +
						sendRequestNanos +
						buildResponse
		));
		panlObject.put("timings", timingsObject);
		panlObject.put("active", activeObjects);
		panlObject.put("available", availableObjects);

		solrJsonObject.put("error", false);

		solrJsonObject.put("panl", panlObject);

		return (solrJsonObject.toString());
	}

	private JSONObject getAdditionURI(PanlToken panlToken, Map<String, List<PanlToken>> panlTokenMap) {
		JSONObject jsonObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder("/");
		StringBuilder lpse = new StringBuilder();

		String panlLpseCode = panlToken.getPanlLpseCode();

		for (String lpseOrder : collectionProperties.getLpseOrder()) {
			// do we currently have some codes for this?

			if (panlTokenMap.containsKey(lpseOrder)) {
				for (PanlToken token : panlTokenMap.get(lpseOrder)) {
					lpseUri.append(token.getUriComponent());
					lpse.append(token.getLpseComponent());
				}
			}

			// if the current panl token's lpse matches that of the panlLpseOrder,
			// then we need to add to lpseCode and the uri
			if (panlLpseCode.equals(lpseOrder)) {
				jsonObject.put("before", lpseUri.toString());
				// clear the sting builder
				lpseUri.setLength(0);
				lpse.append(panlToken.getLpseComponent());
			}
		}

		jsonObject.put("after", "/" + lpseUri + lpse + "/");
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
	public List<PanlToken> parseLpse(String uri, String query) {
		List<PanlToken> panlTokens = new ArrayList<>();

		String[] searchQuery = uri.split("/");

		if (searchQuery.length > 3) {
			String lpseEncoding = searchQuery[searchQuery.length - 1];

			PanlStringTokeniser lpseTokeniser = new PanlStringTokeniser(lpseEncoding, Collection.CODES_AND_METADATA, true);

			StringTokenizer valueTokeniser = new StringTokenizer(uri, "/", false);
			// we need to skip the first two - as they will be the collection and the
			// field set
			valueTokeniser.nextToken();
			valueTokeniser.nextToken();

			boolean hasQuery = false;
			while (lpseTokeniser.hasMoreTokens()) {
				String token = lpseTokeniser.nextToken();

				if (token.equals(collectionProperties.getPanlParamQuery())) {
					hasQuery = true;
					panlTokens.add(
							new PanlQueryToken(
									query,
									token,
									valueTokeniser));

				} else if (token.equals(collectionProperties.getPanlParamSort())) {
					panlTokens.add(
							new PanlSortToken(
									collectionProperties,
									token,
									lpseTokeniser));

				} else if (token.equals(collectionProperties.getPanlParamNumRows())) {
					panlTokens.add(
							new PanlNumRowsToken(
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
					panlTokens.add(
							new PanlFacetToken(
									collectionProperties,
									facet.toString(),
									lpseTokeniser,
									valueTokeniser));
				}
			}

			// If we don't have a query - then parse the query
			if (!hasQuery && !query.isBlank()) {
				panlTokens.add(new PanlQueryToken(query,
						collectionProperties.getPanlParamQuery(),
						valueTokeniser));
			}
		}

		for (PanlToken panlToken : panlTokens) {
			System.out.println(panlToken.explain());
		}

		return (panlTokens);
	}

	public String getValidUrlsJSON() {
		return (collectionProperties.getValidUrlsJson());
	}

	public boolean isValidResultsFields(String path) {
		return (collectionProperties.isValidResultFieldsName(path));
	}
}
