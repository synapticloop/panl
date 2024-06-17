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
 *  IN THE SOFTWARE.
 */

import com.synapticloop.panl.server.handler.results.util.ResourceHelper;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.json.JSONObject;

import static com.synapticloop.panl.server.handler.results.util.ResourceHelper.*;

/**
 * <p>This is the default handler for all requests and simply passes the
 * processing to the appropriate collection for execution.</p>
 *
 * @author synapticloop
 */
public class PanlRequestHandler implements HttpRequestHandler {
	private final PanlProperties panlProperties;
	private final CollectionRequestHandler collectionRequestHandler;

	/**
	 * <p></p>
	 *
	 * @param collectionRequestHandler The collection that will handle this request
	 */
	public PanlRequestHandler(PanlProperties panlProperties, CollectionRequestHandler collectionRequestHandler) {
		super();
		this.panlProperties = panlProperties;
		this.collectionRequestHandler = collectionRequestHandler;
	}

	/**
	 * <p>Do some initial checking on the request (including the query string if
	 * one is available) and pass it off to the CollectionRequestHandler.</p>
	 *
	 * <p>This request handler also returns the 500 internal error status if
	 * there was an error processing the request.</p>
	 *
	 * @param request the HTTP request.
	 * @param response the HTTP response.
	 * @param context the HTTP execution context. (which is ignored by this processor)
	 */
	@Override public void handle(HttpRequest request, HttpResponse response, HttpContext context) {

		// the first thing that we are going to do is to ensure that we have a
		// valid request
		String uri = request.getRequestLine().getUri();
		int startParam = uri.indexOf('?');
		String query = "";
		if (startParam != -1) {
			query = uri.substring(startParam + 1);
			uri = uri.substring(0, startParam);
		}

		String[] paths = uri.split("/");
		if (paths.length < 3 ||
				paths[2].isBlank() ||
				!collectionRequestHandler.isValidResultsFields(paths[2])) {

			response.setStatusCode(HttpStatus.SC_NOT_FOUND);
			response.setEntity(
					new StringEntity(
							collectionRequestHandler.getValidUrlString(),
							ResourceHelper.CONTENT_TYPE_JSON));
			return;
		}

		try {
			response.setStatusCode(HttpStatus.SC_OK);
			response.setEntity(
					new StringEntity(
							collectionRequestHandler.handleRequest(uri, query),
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
				e.printStackTrace();
				response.setEntity(new StringEntity(jsonObject.toString(), ResourceHelper.CONTENT_TYPE_JSON));
			} else {
				jsonObject.put(JSON_KEY_MESSAGE, JSON_VALUE_MESSAGE_500);
			}
		}
	}
}
