package com.synapticloop.panl.server.handler;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.generator.bean.Collection;
import com.synapticloop.panl.server.client.*;
import com.synapticloop.panl.server.handler.token.*;
import com.synapticloop.panl.server.properties.BaseProperties;
import com.synapticloop.panl.server.properties.CollectionProperties;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

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

	private final String validUrls = "";

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

		String[] searchQuery = uri.split("/");
		String resultFields = searchQuery[2];

		List<PanlToken> panlTokens = parseLpse(uri, query);

		try (SolrClient solrClient = panlClient.getClient()) {
			// we set the default query - to be overridden later if one exists

			SolrQuery solrQuery = panlClient.getQuery(query);
			for (String fieldName : collectionProperties.getResultFieldsForName(resultFields)) {
				solrQuery.addField(fieldName);
			}

			solrQuery.setFacetMinCount(collectionProperties.getFacetMinCount());

			// this may be overridden by the lpse status
			solrQuery.setRows(collectionProperties.getResultRows());

			solrQuery.addFacetField(collectionProperties.getFacetFields());

			// now we need to go through the panl facets and add them

			for(PanlToken panlToken: panlTokens) {
				panlToken.applyToQuery(solrQuery);
			}

			final QueryResponse response = solrClient.query(this.collectionName, solrQuery);
			return (parseResponse(panlTokens, response));
		} catch (IOException | SolrServerException e) {
			throw new PanlServerException("Could not query the Solr instance.", e);
		}
	}

	/**
	 * <p>Parse the solrj response and add the panl information to it</p>
	 *
	 * @param panlTokens The parsed URI and panl tokens
	 * @param response The Solrj response to be parsed
	 * @return a JSON Object as a string with the appended panl response
	 */
	private String parseResponse(List<PanlToken> panlTokens, QueryResponse response) {

		// set up the data structure
		Map<String, List<PanlToken>> panlTokenMap = new HashMap<>();
		for(PanlToken panlToken: panlTokens) {
			String panlLpseCode = panlToken.getPanlLpseCode();

			List<PanlToken> panlTokenList = panlTokenMap.get(panlLpseCode);
			if(null == panlTokenList) {
				panlTokenList = new ArrayList<PanlToken>();
			}
			panlTokenList.add(panlToken);
			panlTokenMap.put(panlLpseCode, panlTokenList);
		}

		JSONObject solrJsonObject = new JSONObject(response.jsonStr());
		JSONObject panlObject = new JSONObject();


		SolrDocumentList solrDocuments = (SolrDocumentList)response.getResponse().get("response");
		long numFound = solrDocuments.getNumFound();
		long start = solrDocuments.getStart();


		JSONArray panlFacets = new JSONArray();

		for (FacetField facetField : response.getFacetFields()) {
			if(facetField.getValueCount() != 0) {
				JSONObject facetObject = new JSONObject();
				facetObject.put("name", facetField.getName());

				JSONArray facetValueArrays = new JSONArray();
				for (FacetField.Count value : facetField.getValues()) {
					JSONObject facetValueObject = new JSONObject();
					facetValueObject.put("value", String.format("%s", value.getName()));
					facetValueObject.put("count", value.getCount());
					facetValueArrays.put(facetValueObject);
				}

				facetObject.put("values", facetValueArrays);
				panlFacets.put(facetObject);
			}
		}

		panlObject.put("facet_fields", panlFacets);

		solrJsonObject.put("panl", panlObject);

		return (solrJsonObject.toString());
	}

	/**
	 * <p>Parse the uri and optional query string.</p>
	 *
	 * <p>If the query string is not empty, then this will overwrite any query
	 * that is set in the lpse URI.</p>
	 *
	 * <p>The URI will always be of the form</p>
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

			while (lpseTokeniser.hasMoreTokens()) {
				String token = lpseTokeniser.nextToken();

				if (token.equals(collectionProperties.getPanlParamQuery())) {
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
		}

		for (PanlToken panlToken : panlTokens) {
			System.out.println(panlToken.explain());
		}

		return(panlTokens);
	}

	public String getValidUrlsJSON() {
		return (collectionProperties.getValidUrlsJson());
	}

	public boolean isValidResultsFields(String path) {
		return (collectionProperties.isValidResultFieldsName(path));
	}
}
