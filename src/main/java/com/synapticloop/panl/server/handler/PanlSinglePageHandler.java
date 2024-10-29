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

import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import com.synapticloop.panl.server.handler.webapp.util.ResourceHelper;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static com.synapticloop.panl.server.handler.webapp.util.ResourceHelper.*;

/**
 * <p>This is the single page handler which will return the configuration
 * for a specific CaFUP so that a single search page may be built.</p>
 *
 * @author synapticloop
 */
public class PanlSinglePageHandler implements HttpRequestHandler {
	public static final String PANL_URL_BINDING_SINGLE_PAGE = "/panl-single-page/";

	private final PanlProperties panlProperties;
	private final Map<String, CollectionRequestHandler> validCollections = new HashMap<>();
	private final JSONArray validUrls = new JSONArray();

	/**
	 * <p>Instantiate the Panl configuration handle.</p>
	 *
	 * @param panlProperties The panl properties
	 * @param collectionRequestHandlers The collection request handler
	 */
	public PanlSinglePageHandler(PanlProperties panlProperties, List<CollectionRequestHandler> collectionRequestHandlers) {		this.panlProperties = panlProperties;
		for(CollectionRequestHandler collectionRequestHandler : collectionRequestHandlers) {
			validCollections.put(collectionRequestHandler.getPanlCollectionUri(), collectionRequestHandler);
			validUrls.put(PANL_URL_BINDING_SINGLE_PAGE + collectionRequestHandler.getPanlCollectionUri() + "/");
		}
	}

	/**
	 * <p>Return the JSON object that contains all of the configuration.</p>
	 *
	 * @param request the HTTP request.
	 * @param response the HTTP response.
	 * @param context the HTTP execution context.
	 */
	@Override public void handle(HttpRequest request, HttpResponse response, HttpContext context) {

		// the first thing that we are going to do is to ensure that we have a
		// valid request
		String uri = request.getRequestLine().getUri();

		String[] paths = uri.split("/");
		if (paths.length == 3  && validCollections.containsKey(paths[2])) {
			try {
				CollectionRequestHandler collectionRequestHandler = validCollections.get(paths[2]);
				JSONObject jsonObject = new JSONObject(
					collectionRequestHandler.handleRequest(
						"/" + paths[2] + "/" + CollectionProperties.FIELDSETS_EMPTY + "/",
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

				// now to add the data that we do need
				List<String> lpseOrders = collectionRequestHandler.getLpseOrder();
				panlJsonObject.put("lpse_order", new ArrayList<>());
				int i = 0;
				JSONObject lpseLookupObject = new JSONObject();
				for(String lpseOrder: lpseOrders) {
					lpseLookupObject.put(lpseOrder, i);
					i++;
				}

				panlJsonObject.put("lpse_lookup", lpseLookupObject);

				// now go through the available facets and place them in the correct place
				JSONObject availableJsonObject = panlJsonObject.getJSONObject("available");

				// regular facets
				for (Object regularFacets : availableJsonObject.getJSONArray("facets")) {
					JSONObject regularFacetObject = (JSONObject) regularFacets;
					String panlCode = regularFacetObject.getString("panl_code");
					if(null != panlCode) {
						int lpseOrder = lpseLookupObject.optInt(panlCode, -1);
						if(lpseOrder != -1) {
							panlJsonObject.getJSONArray("lpse_order").put(lpseOrder, regularFacetObject);
						}
					}
				}

				// range facets always need to go after regular facets, as they are
				// both returned, and the range must overwrite the regular one
				for (Object rangeFacets : availableJsonObject.getJSONArray("range_facets")) {
					JSONObject rangeFacetObject = (JSONObject) rangeFacets;
					String panlCode = rangeFacetObject.getString("panl_code");
					if(null != panlCode) {
						int lpseOrder = lpseLookupObject.optInt(panlCode, -1);
						if(lpseOrder != -1) {
							rangeFacetObject.put("is_range_facet", true);
							panlJsonObject.getJSONArray("lpse_order").put(lpseOrder, rangeFacetObject);
						}
					}
				}

				// date range facets next
				for (Object rangeFacets : availableJsonObject.getJSONArray("date_range_facets")) {
					JSONObject rangeFacetObject = (JSONObject) rangeFacets;
					String panlCode = rangeFacetObject.getString("panl_code");
					if(null != panlCode) {
						int lpseOrder = lpseLookupObject.optInt(panlCode, -1);
						if(lpseOrder != -1) {
							rangeFacetObject.put("is_date_range_facet", true);
							panlJsonObject.getJSONArray("lpse_order").put(lpseOrder, rangeFacetObject);
						}
					}
				}

				// lastly remove the facets
				panlJsonObject.remove("sorting");
				panlJsonObject.remove("available");
				panlJsonObject.remove("fields");

				response.setStatusCode(HttpStatus.SC_OK);
				response.setEntity(
						new StringEntity(
								jsonObject.toString(),
								ResourceHelper.CONTENT_TYPE_JSON)
				);
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
}
