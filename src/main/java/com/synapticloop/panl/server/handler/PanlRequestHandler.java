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
import com.synapticloop.panl.server.handler.webapp.util.ResourceHelper;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.synapticloop.panl.server.handler.webapp.util.ResourceHelper.*;

/**
 * <p>This is the default handler for all requests and simply passes the
 * processing to the appropriate collection for execution.</p>
 *
 * @author synapticloop
 */
public class PanlRequestHandler extends BaseResponseHandler implements HttpRequestHandler {
	private final static Logger LOGGER = LoggerFactory.getLogger(PanlRequestHandler.class);

	private final CollectionRequestHandler collectionRequestHandler;

	/**
	 * <p>Instantiate The Panl request handler which will bind the request URL to
	 * the collection request handler.</p>
	 *
	 * @param panlProperties The panl properties file
	 * @param collectionRequestHandler The collection request handler that will handle this request
	 */
	public PanlRequestHandler(PanlProperties panlProperties, CollectionRequestHandler collectionRequestHandler) {
		super(panlProperties);
		this.validUrls = collectionRequestHandler.getValidUrls();
		this.collectionRequestHandler = collectionRequestHandler;
	}

	/**
	 * <p>Do some initial checking on the request (including the query string if
	 * one is available) and pass it off to the CollectionRequestHandler.</p>
	 *
	 * <p>This request handler returns a 500 internal error HTTP status code if
	 * there was an error processing the request.</p>
	 *
	 * <p>This request handler returns a 404 internal error HTTPS status code if
	 * there is no registered <code>CollectionRequestHandler</code> registered for the URL.</p>
	 *
	 * @param request the HTTP request - the incoming request
	 * @param response the HTTP response - the outgoing response
	 * @param context the HTTP execution context. (which is ignored by this processor)
	 *
	 * @see CollectionRequestHandler
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

			set404ResponseMessage(response);
			return;
		}

		try {
			response.setEntity(
				new StringEntity(
					collectionRequestHandler.handleRequest(uri, query, context),
					ResourceHelper.CONTENT_TYPE_JSON)
			);

			response.setStatusCode(HttpStatus.SC_OK);
		} catch (PanlNotFoundException e) {
			set404ResponseMessage(response);
		} catch (Exception e) {
			set500ResponseMessage(response, e);
		}
	}

	@Override protected Logger getLogger() {
		return (LOGGER);
	}
}
