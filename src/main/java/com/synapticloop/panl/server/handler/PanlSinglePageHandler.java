package com.synapticloop.panl.server.handler;

/*
 * Copyright (c) 2008-2025 synapticloop.
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

import com.synapticloop.panl.server.handler.properties.PanlProperties;
import com.synapticloop.panl.server.handler.webapp.util.ResourceHelper;
import com.synapticloop.panl.util.Constants;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>This is the single page handler which will return the configuration for a
 * specific CaFUP so that a single search page may be built.</p>
 *
 * @author synapticloop
 */
public class PanlSinglePageHandler extends BaseResponseHandler implements HttpRequestHandler {
	private final static Logger LOGGER = LoggerFactory.getLogger(PanlSinglePageHandler.class);

	public static final String PANL_URL_BINDING_SINGLE_PAGE = "/panl-single-page/";

	private final Map<String, CollectionRequestHandler> validCollections = new HashMap<>();

	/**
	 * <p>Instantiate the Panl configuration handle.</p>
	 *
	 * @param panlProperties The panl properties
	 * @param collectionRequestHandlers The collection request handler
	 */
	public PanlSinglePageHandler(PanlProperties panlProperties, List<CollectionRequestHandler> collectionRequestHandlers) {
		super(panlProperties);

		for(CollectionRequestHandler collectionRequestHandler : collectionRequestHandlers) {
			validCollections.put(collectionRequestHandler.getPanlCollectionUri(), collectionRequestHandler);
			validUrls.put(PANL_URL_BINDING_SINGLE_PAGE + collectionRequestHandler.getPanlCollectionUri() + "/");
		}
	}

	/**
	 * <p>Return the JSON object that contains all configuration.</p>
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
						"/" + paths[2] + "/" + Constants.Url.Panl.FIELDSETS_EMPTY + "/",
						"",
						context));

				// now that we have the JSON object - time to remove the things we don't need
				jsonObject.remove(Constants.Json.Solr.RESPONSE_HEADER);
				jsonObject.remove(Constants.Json.Solr.RESPONSE);
				jsonObject.remove(Constants.Json.Solr.FACET_COUNTS);

				JSONObject panlJsonObject = jsonObject.getJSONObject(Constants.Json.Panl.PANL);

				panlJsonObject.remove(Constants.Json.Panl.PAGINATION);
				panlJsonObject.remove(Constants.Json.Panl.ACTIVE);
				panlJsonObject.remove(Constants.Json.Panl.QUERY_OPERAND);
				panlJsonObject.remove(Constants.Json.Panl.TIMINGS);
				panlJsonObject.remove(Constants.Json.Panl.CANONICAL_URI);

				// now to add the data that we do need
				List<String> lpseOrders = collectionRequestHandler.getLpseOrder();
				panlJsonObject.put(Constants.Json.Panl.LPSE_ORDER, new ArrayList<>());
				int i = 0;
				JSONObject lpseLookupObject = new JSONObject();
				for(String lpseOrder: lpseOrders) {
					lpseLookupObject.put(lpseOrder, i);
					i++;
				}

				panlJsonObject.put(Constants.Json.Panl.LPSE_LOOKUP, lpseLookupObject);

				// now go through the available facets and place them in the correct place
				JSONObject availableJsonObject = panlJsonObject.getJSONObject(Constants.Json.Panl.AVAILABLE);

				// regular facets
				for (Object regularFacets : availableJsonObject.getJSONArray(Constants.Json.Panl.FACETS)) {
					JSONObject regularFacetObject = (JSONObject) regularFacets;
					String panlCode = regularFacetObject.getString(Constants.Json.Panl.PANL_CODE);
					if(null != panlCode) {
						int lpseOrder = lpseLookupObject.optInt(panlCode, -1);
						if(lpseOrder != -1) {
							panlJsonObject.getJSONArray(Constants.Json.Panl.LPSE_ORDER).put(lpseOrder, regularFacetObject);
						}
					}
				}

				// range facets always need to go after regular facets, as they are
				// both returned, and the range must overwrite the regular one
				for (Object rangeFacets : availableJsonObject.getJSONArray(Constants.Json.Panl.RANGE_FACETS)) {
					JSONObject rangeFacetObject = (JSONObject) rangeFacets;
					String panlCode = rangeFacetObject.getString(Constants.Json.Panl.PANL_CODE);
					if(null != panlCode) {
						int lpseOrder = lpseLookupObject.optInt(panlCode, -1);
						if(lpseOrder != -1) {
							rangeFacetObject.put(Constants.Json.Panl.IS_RANGE_FACETS, true);
							panlJsonObject.getJSONArray(Constants.Json.Panl.LPSE_ORDER).put(lpseOrder, rangeFacetObject);
						}
					}
				}

				// date range facets next
				for (Object rangeFacets : availableJsonObject.getJSONArray(Constants.Json.Panl.DATE_RANGE_FACETS)) {
					JSONObject rangeFacetObject = (JSONObject) rangeFacets;
					String panlCode = rangeFacetObject.getString(Constants.Json.Panl.PANL_CODE);
					if(null != panlCode) {
						int lpseOrder = lpseLookupObject.optInt(panlCode, -1);
						if(lpseOrder != -1) {
							rangeFacetObject.put(Constants.Json.Panl.IS_DATE_RANGE_FACET, true);
							panlJsonObject.getJSONArray(Constants.Json.Panl.LPSE_ORDER).put(lpseOrder, rangeFacetObject);
						}
					}
				}

				// lastly remove the facets
				panlJsonObject.remove(Constants.Json.Panl.SORTING);
				panlJsonObject.remove(Constants.Json.Panl.AVAILABLE);
				panlJsonObject.remove(Constants.Json.Panl.FIELDS);

				response.setStatusCode(HttpStatus.SC_OK);
				response.setEntity(
						new StringEntity(
								jsonObject.toString(),
								ResourceHelper.CONTENT_TYPE_JSON)
				);
			} catch (Exception e) {
				set500ResponseMessage(response, e);
			}
		} else {
			set404ResponseMessage(response);
		}
	}

	@Override protected Logger getLogger() {
		return(LOGGER);
	}
}
