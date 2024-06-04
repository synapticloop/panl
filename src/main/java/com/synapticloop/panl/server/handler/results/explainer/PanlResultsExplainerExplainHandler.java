package com.synapticloop.panl.server.handler.results.explainer;

/*
 * Copyright (c) 2008-2024 synapticloop.
 *
 * https://github.com/synapticloop/panl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 *  IN THE SOFTWARE.
 */

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
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class PanlResultsExplainerExplainHandler implements HttpRequestHandler {
	private final List<CollectionProperties> collectionPropertiesList;
	
	public PanlResultsExplainerExplainHandler(List<CollectionProperties> collectionPropertiesList, List<CollectionRequestHandler> collectionRequestHandlers) {
		this.collectionPropertiesList = collectionPropertiesList;

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

		uri.split("/");

		// TODO - this most certainly is wrong
		CollectionProperties collectionProperties = collectionPropertiesList.get(0);
		List<LpseToken> lpseTokens = parseLpse(collectionProperties, uri, query);
		JSONArray jsonArray = new JSONArray();
		for (LpseToken lpseToken : lpseTokens) {
			jsonArray.put(lpseToken.explain());
		}


		JSONObject jsonObject = new JSONObject();
		jsonObject.put("explanation", jsonArray);

		jsonObject.put("configuration", getConfiguration(collectionProperties));

		response.setStatusCode(HttpStatus.SC_OK);
		response.setEntity(
				new StringEntity(jsonObject.toString(),
						ResourceHelper.CONTENT_TYPE_JSON));

	}

	private JSONArray getConfiguration(CollectionProperties collectionProperties) {
		JSONArray jsonArray = new JSONArray();

		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			jsonArray.put(lpseField.explain());
		}


		return(jsonArray);
	}

	private List<LpseToken> parseLpse(CollectionProperties collectionProperties, String uri, String query) {
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
					while (token.length() < collectionProperties.getPanlLpseLength()) {
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
