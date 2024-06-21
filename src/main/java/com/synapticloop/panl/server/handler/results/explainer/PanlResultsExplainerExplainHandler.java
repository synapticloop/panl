package com.synapticloop.panl.server.handler.results.explainer;

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
 *  IN THE SOFTWARE.
 */

import com.synapticloop.panl.generator.bean.PanlCollection;
import com.synapticloop.panl.server.handler.CollectionRequestHandler;
import com.synapticloop.panl.server.handler.results.util.ResourceHelper;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.*;
import com.synapticloop.panl.server.handler.tokeniser.token.param.QueryLpseToken;
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
	}

	@Override public void handle(HttpRequest request, HttpResponse response, HttpContext context) {
		String uri = request.getRequestLine().getUri();
		int startParam = uri.indexOf('?');
		String query = "";
		if (startParam != -1) {
			query = uri.substring(startParam + 1);
			uri = uri.substring(0, startParam);
		}

		String[] splits = uri.split("/");
		if (splits.length < 5) {
			// #TODO return a 404 or something
			return;
		}

		// the first part is the panl collection URI that we need


		CollectionProperties collectionProperties = null;
		String panlCollectionUri = splits[3];
		for(CollectionProperties collectionPropertiesTemp : collectionPropertiesList) {
			if(collectionPropertiesTemp.getPanlCollectionUri().equals(panlCollectionUri)) {
				collectionProperties = collectionPropertiesTemp;
				break;
			}
		}

		if(null == collectionProperties) {
			return;
		}

		List<LpseToken> lpseTokens = parseLpse(collectionProperties, uri, query);
		JSONArray jsonArray = new JSONArray();
		for (LpseToken lpseToken : lpseTokens) {
			jsonArray.put(lpseToken.explain());
		}


		JSONObject jsonObject = new JSONObject();
		jsonObject.put("explanation", jsonArray);

		jsonObject.put("parameters", getLpseParameters(collectionProperties));

		jsonObject.put("configuration", getLpseConfiguration(collectionProperties));

		response.setStatusCode(HttpStatus.SC_OK);
		response.setEntity(
				new StringEntity(jsonObject.toString(),
						ResourceHelper.CONTENT_TYPE_JSON));

	}
	private JSONArray getLpseParameters(CollectionProperties collectionProperties) {
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(createParameterDetails(
				CollectionProperties.PROPERTY_KEY_PANL_PARAM_QUERY,
				collectionProperties.getPanlParamQuery(),
				"The LPSE code for the user search query."));
		jsonArray.put(createParameterDetails(
				CollectionProperties.PROPERTY_KEY_PANL_PARAM_SORT,
				collectionProperties.getPanlParamSort(),
				"The sort fields available: " + collectionProperties.getSortFieldLpseCodes()));

		return(jsonArray);
	}

	private JSONObject createParameterDetails(String propertyKey, Object value, String description) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("property", propertyKey);
		jsonObject.put("value", value);
		jsonObject.put("description", description);

		return (jsonObject);
	}

	private JSONArray getLpseConfiguration(CollectionProperties collectionProperties) {
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

		if (searchQuery.length > 5) {
			String lpseEncoding = searchQuery[searchQuery.length - 1];

			LpseTokeniser lpseTokeniser = new LpseTokeniser(lpseEncoding, PanlCollection.CODES_AND_METADATA, true);

			StringTokenizer valueTokeniser = new StringTokenizer(uri, "/", false);
			// we need to skip the first two - as they will be the collection and the
			// field set
			valueTokeniser.nextToken();
			valueTokeniser.nextToken();
			valueTokeniser.nextToken();
			valueTokeniser.nextToken();

			while (lpseTokeniser.hasMoreTokens()) {
				String token = lpseTokeniser.nextToken();
				LpseToken lpseToken = LpseToken.getLpseToken(collectionProperties, token, query, valueTokeniser, lpseTokeniser);
				lpseTokens.add(lpseToken);
			}
		}

		if (!hasQuery && !query.isBlank()) {
			lpseTokens.add(new QueryLpseToken(collectionProperties, query, collectionProperties.getPanlParamQuery()));
		}

		return (lpseTokens);
	}
}
