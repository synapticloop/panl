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
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import static com.synapticloop.panl.server.handler.webapp.util.ResourceHelper.*;

/**
 * <p>This is the default handler for any URLs that are not bound to a
 * collection.  It returns a 404 error code with a JSON object as the
 * response.</p>
 *
 * <p>The response will be of the following form:</p>
 *
 * <pre>
 * {
 *   "error": 404,
 *   "message": "Could not find a PANL request url, see 'valid_urls' array.",
 *   "valid_urls": [
 *     "/example/*"
 *   ]
 * }
 * </pre>
 *
 * @author synapticloop
 */
public class PanlDefaultHandler implements HttpRequestHandler {
	public static final String JSON_VALUE_MESSAGE = "Could not find a PANL request url, see 'valid_urls' array.";

	// this error string is cached upon instantiation.
	private static String json404ErrorString;

	/**
	 * <p>Instantiate the default request handler.</p>
	 *
	 * <p>In effect, this will create the JSON response at instantiation time,
	 * which will then be statically served for every request.</p>
	 *
	 * @param panlProperties The panl properties
	 * @param collectionRequestHandlers The collection request handlers to iterate
	 * 		through to build the static response.
	 */
	public PanlDefaultHandler(PanlProperties panlProperties, List<CollectionRequestHandler> collectionRequestHandlers) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(JSON_KEY_ERROR, true);
		jsonObject.put(JSON_KEY_STATUS, HttpStatus.SC_NOT_FOUND);

		// do we want verbose messaging for 404 http status codes
		if (panlProperties.getUseVerbose404Messages()) {
			jsonObject.put(JSON_KEY_MESSAGE, JSON_VALUE_MESSAGE);
			JSONArray validUrls = new JSONArray();
			for (CollectionRequestHandler collectionRequestHandler : collectionRequestHandlers) {
				validUrls.put("/" + collectionRequestHandler.getPanlCollectionUri() + "/*");
			}
			jsonObject.put(JSON_KEY_VALID_URLS, validUrls);
		} else {
			jsonObject.put(JSON_KEY_MESSAGE, JSON_VALUE_MESSAGE_404);
		}

		json404ErrorString = jsonObject.toString();
	}

	/**
	 * <p>As the default handler for the PANL server - this will
	 * <strong>ALWAYS</strong> return a 404 response with JSON body.</p>
	 *
	 * @param request the HTTP request.
	 * @param response the HTTP response.
	 * @param context the HTTP execution context.
	 */
	@Override public void handle(HttpRequest request, HttpResponse response, HttpContext context) {
		response.setStatusCode(HttpStatus.SC_NOT_FOUND);
		response.setEntity(new StringEntity(json404ErrorString, CONTENT_TYPE_JSON));
	}
}
