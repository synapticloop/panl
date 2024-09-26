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

import com.synapticloop.panl.exception.PanlNotFoundException;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import com.synapticloop.panl.server.handler.webapp.util.ResourceHelper;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.synapticloop.panl.server.handler.webapp.util.ResourceHelper.*;

/**
 * <p>This is the handler which will return more facets for a specific facet
 * to then be populated.</p>
 */
public class PanlMoreFacetsHandler implements HttpRequestHandler {
	public static final String PANL_MORE_FACETS_BINDING = "/panl-more-facets/";

	public static final String QUERY_PARAM_CODE = "code";
	public static final String QUERY_PARAM_LIMIT = "limit";

	public static final String CONTEXT_KEY_LPSE_CODE = "lpse_code";
	public static final String CONTEXT_KEY_FACET_LIMIT = "facet_limit";

	private final PanlProperties panlProperties;
	private final List<CollectionRequestHandler> collectionRequestHandlers;
	private final Map<String, CollectionRequestHandler> validCollections = new HashMap<>();
	private final JSONArray validUrls = new JSONArray();

	/**
	 * <p>Instantiate the Panl more facets handle.</p>
	 *
	 * @param panlProperties            The panl properties
	 * @param collectionRequestHandlers The collection request handler
	 */
	public PanlMoreFacetsHandler(PanlProperties panlProperties, List<CollectionRequestHandler> collectionRequestHandlers) {
		this.panlProperties = panlProperties;
		this.collectionRequestHandlers = collectionRequestHandlers;
		for (CollectionRequestHandler collectionRequestHandler : collectionRequestHandlers) {
			validCollections.put(collectionRequestHandler.getPanlCollectionUri(), collectionRequestHandler);
			validUrls.put(PANL_MORE_FACETS_BINDING + collectionRequestHandler.getPanlCollectionUri() + "/");
		}
	}

	/**
	 * <p>Return the JSON object that contains just the facet that is required.</p>
	 *
	 * @param request  the HTTP request.
	 * @param response the HTTP response.
	 * @param context  the HTTP execution context.
	 */
	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context) {

		// the first thing that we are going to do is to ensure that we have a
		// valid uri with the correct parameters
		String uri = request.getRequestLine().getUri() + "?";
		boolean isGoodRequest = false;
		String lpseCode = null;
		Integer facetLimit = null;
		try {
			final List<NameValuePair> pairs = new URIBuilder(request.getRequestLine().getUri()).getQueryParams();
			for (NameValuePair pair : pairs) {
				if (pair.getName().equals(QUERY_PARAM_CODE)) {
					lpseCode = pair.getValue();
				} else if (pair.getName().equals(QUERY_PARAM_LIMIT)) {
					try {
						facetLimit = Integer.parseInt(pair.getValue());
					} catch (NumberFormatException ignored) {
						// do nothing
					}
				}
			}

			if (null != lpseCode && facetLimit != null) {
				isGoodRequest = true;
			}
		} catch (URISyntaxException e) {

		}

		uri = uri.substring(0, uri.indexOf('?'));

		String[] paths = uri.split("/");
		if (isGoodRequest && (paths.length > 3 && validCollections.containsKey(paths[2]))) {
			StringBuilder stringBuilder = new StringBuilder("/");

			// rebuild the
			int i = 0;
			for (String path : paths) {
				switch (i) {
					case 0:
					case 1:
						i++;
						continue;
					case 3:
						stringBuilder.append(CollectionProperties.FIELDSETS_EMPTY);
						break;
					default:
						stringBuilder.append(path);
				}
				i++;
				stringBuilder.append("/");
			}


			try {
				CollectionRequestHandler collectionRequestHandler = validCollections.get(paths[2]);
				context.setAttribute(CONTEXT_KEY_LPSE_CODE, lpseCode);
				context.setAttribute(CONTEXT_KEY_FACET_LIMIT, facetLimit);
				JSONObject jsonObject = new JSONObject(
					collectionRequestHandler.handleRequest(
						stringBuilder.toString(),
						"",
						context));

				// now that we have the JSON object - time to remove the things we don't need
				jsonObject.remove("responseHeader");
				jsonObject.remove("response");
				jsonObject.remove("facet_counts");

				JSONObject panlJsonObject = jsonObject.getJSONObject("panl");

				panlJsonObject.remove("pagination");
				panlJsonObject.remove("active");
				panlJsonObject.remove("query_operand");
				panlJsonObject.remove("timings");
				panlJsonObject.remove("canonical_uri");

				// now go through and get the facet that we want

				// now go through the available facets and place them in the correct place
				JSONObject availableJsonObject = panlJsonObject.getJSONObject("available");

				// regular facets
				for (Object regularFacets : availableJsonObject.getJSONArray("facets")) {
					JSONObject regularFacetObject = (JSONObject) regularFacets;
					String panlCode = regularFacetObject.getString("panl_code");
					if (panlCode.equals(lpseCode)) {
						regularFacetObject.put("facet_limit", facetLimit);
						panlJsonObject.put("facet", regularFacetObject);
						break;
					}
				}

				// lastly remove the facets
				panlJsonObject.remove("query_respond_to");
				panlJsonObject.remove("sorting");
				panlJsonObject.remove("available");
				panlJsonObject.remove("fields");

				response.setStatusCode(HttpStatus.SC_OK);
				response.setEntity(
					new StringEntity(
						jsonObject.toString(),
						ResourceHelper.CONTENT_TYPE_JSON)
				);
			} catch (PanlNotFoundException e) {
				return404Message(response);
			} catch (Exception e) {
				response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(JSON_KEY_ERROR, true);
				jsonObject.put(JSON_KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
				if (panlProperties.getUseVerbose500Messages()) {
					jsonObject.put(JSON_KEY_MESSAGE,
						String.format("Class: %s, message: %s.",
							e.getClass().getCanonicalName(),
							e.getMessage()));

					response.setEntity(new StringEntity(jsonObject.toString(), ResourceHelper.CONTENT_TYPE_JSON));
				} else {
					jsonObject.put(JSON_KEY_MESSAGE, JSON_VALUE_MESSAGE_500);
				}
			}
		} else {
			return404Message(response);
		}
	}

	private void return404Message(HttpResponse response) {

		JSONObject jsonObject = new JSONObject();

		jsonObject.put(JSON_KEY_ERROR, true);
		jsonObject.put(JSON_KEY_STATUS, HttpStatus.SC_NOT_FOUND);
		if (panlProperties.getUseVerbose404Messages()) {
			jsonObject.put(JSON_KEY_MESSAGE, PanlDefaultHandler.JSON_VALUE_MESSAGE);
			jsonObject.put(JSON_KEY_VALID_URLS, validUrls);
		} else {
			jsonObject.put(JSON_KEY_MESSAGE, JSON_VALUE_MESSAGE_404);
		}

		response.setEntity(
			new StringEntity(jsonObject.toString(),
				ResourceHelper.CONTENT_TYPE_JSON));
	}
}
