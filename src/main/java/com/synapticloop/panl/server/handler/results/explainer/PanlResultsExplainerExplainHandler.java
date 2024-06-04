package com.synapticloop.panl.server.handler.results.explainer;

import com.synapticloop.panl.generator.bean.Collection;
import com.synapticloop.panl.server.handler.CollectionRequestHandler;
import com.synapticloop.panl.server.handler.results.util.ResourceHelper;
import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.properties.field.BaseField;
import com.synapticloop.panl.server.tokeniser.PanlTokeniser;
import com.synapticloop.panl.server.tokeniser.token.*;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class PanlResultsExplainerExplainHandler implements HttpRequestHandler {
	private final CollectionProperties collectionProperties;
	
	public PanlResultsExplainerExplainHandler(CollectionProperties collectionProperties, List<CollectionRequestHandler> collectionRequestHandlers) {
		this.collectionProperties = collectionProperties;

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("error", 404);
		jsonObject.put("message", "Could not find a PANL request url, see 'valid_urls' array.");
		JSONArray validUrls = new JSONArray();
		for (CollectionRequestHandler collectionRequestHandler: collectionRequestHandlers) {
			validUrls.put("/" +collectionRequestHandler.getCollectionName() + "/*");
		}
		jsonObject.put("valid_urls", validUrls);
	}

	@Override public void handle(HttpRequest request, HttpResponse response, HttpContext context) {
		String uri = request.getRequestLine().getUri();
		int startParam = uri.indexOf('?');
		String query = "";
		if (startParam != -1) {
			query = uri.substring(startParam + 1);
			uri = uri.substring(0, startParam);
		}

		List<LpseToken> lpseTokens = parseLpse(uri, query);
		JSONArray jsonArray = new JSONArray();
		for (LpseToken lpseToken : lpseTokens) {
			jsonArray.put(lpseToken.explain());
		}

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("explanation", jsonArray);

		jsonObject.put("configuration", getConfiguration());

		response.setStatusCode(HttpStatus.SC_OK);
		response.setEntity(
				new StringEntity(jsonObject.toString(),
						ResourceHelper.CONTENT_TYPE_JSON));

	}

	private JSONArray getConfiguration() {
		JSONArray jsonArray = new JSONArray();

		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			jsonArray.put(lpseField.explain());
		}


		return(jsonArray);
	}

	private List<LpseToken> parseLpse(String uri, String query) {
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
				LpseToken lpseToken = null;
				if (token.equals(collectionProperties.getPanlParamQuery())) {
					hasQuery = true;
					lpseToken = new QueryLpseToken(
							query,
							token,
							valueTokeniser);
				} else if (token.equals(collectionProperties.getPanlParamSort())) {
					lpseToken = new SortLpseToken(
							collectionProperties,
							token,
							lpseTokeniser);
				} else if (token.equals(collectionProperties.getPanlParamQueryOperand())) {
					lpseToken = new QueryOperandLpseToken(
							collectionProperties,
							token,
							lpseTokeniser);
				} else if (token.equals(collectionProperties.getPanlParamNumRows())) {
					lpseToken = new NumRowsLpseToken(
							collectionProperties,
							token,
							valueTokeniser);
				} else if (token.equals(collectionProperties.getPanlParamPage())) {
					lpseToken = new PageLpseToken(
							collectionProperties,
							token,
							valueTokeniser);

				} else if (token.equals(collectionProperties.getPanlParamPassThrough())) {
					lpseToken = new PassThroughLpseToken(
							collectionProperties,
							token,
							valueTokeniser);
				} else {
					StringBuilder facet = new StringBuilder(token);
					// it is a facet field
					while (token.length() < collectionProperties.getPanlLpseNum()) {
						facet.append(lpseTokeniser.nextToken());
					}

					// now we have the facetField
					lpseToken = new FacetLpseToken(
							collectionProperties,
							facet.toString(),
							lpseTokeniser,
							valueTokeniser);
				}

				lpseTokens.add(lpseToken);
			}
		}

		if (!hasQuery && !query.isBlank()) {
			lpseTokens.add(new QueryLpseToken(query, collectionProperties.getPanlParamQuery()));
		}

		return (lpseTokens);
	}
}
