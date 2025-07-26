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
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 * <p>The base response handler for all incoming requests to Panl</p>
 *
 * @author Synapticloop
 */
public abstract class BaseResponseHandler {

	/**
	 * <p>The Panl properties driving the responses</p>
	 */
	protected final PanlProperties panlProperties;
	/**
	 * <p>Pre-built valid URLs object</p>
	 */
	protected JSONArray validUrls = new JSONArray();

	/**
	 * <p>Instantiate the base response handler which has default methods for 404
	 * and 500 error responses.</p>
	 *
	 * @param panlProperties The panl properties to determine whether to use verbose messaging
	 */
	protected BaseResponseHandler(PanlProperties panlProperties) {
		this.panlProperties = panlProperties;
	}

	/**
	 * <p>Get the logger for this handler.</p>
	 *
	 * @return The logger for this handler
	 */
	protected abstract Logger getLogger();

	/**
	 * <p>Set the response as a 500 status code, also checking to see whether
	 * verbose messaging is set.  If so, it will add in the exception class and the
	 * message of the exception.</p>
	 *
	 * <p><strong>NOTE:</strong> This will set the response code and body, but not
	 * return the actual response.</p>
	 *
	 * @param response The response object
	 * @param exception The exception that was thrown
	 */
	protected void set500ResponseMessage(HttpResponse response, Exception exception) {
		getLogger().error("Internal server error, message was '{}'", exception.getMessage(), exception);
		response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(Constants.Json.Response.ERROR, true);
		jsonObject.put(Constants.Json.Response.STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
		if (panlProperties.getUseVerbose500Messages()) {
			jsonObject.put(Constants.Json.Response.MESSAGE,
				String.format("Class: %s, message: %s.",
					exception.getClass().getCanonicalName(),
					exception.getMessage()));

			response.setEntity(new StringEntity(jsonObject.toString(), ResourceHelper.CONTENT_TYPE_JSON));
		} else {
			jsonObject.put(Constants.Json.Response.MESSAGE, Constants.Json.Response.JSON_VALUE_MESSAGE_500);
		}
	}

	/**
	 * <p>Set the response as a 404 status code, also checking to see whether
	 * verbose messaging is set.  If so, it will add in the <code>valid_urls</code>
	 * JSON object to the response as well.</p>
	 *
	 * <p><strong>NOTE:</strong> This will set the response code and body, but not
	 * return it.</p>
	 *
	 * @param response The response object
	 */
	protected void set404ResponseMessage(HttpResponse response) {
		response.setStatusCode(HttpStatus.SC_NOT_FOUND);

		JSONObject jsonObject = new JSONObject();

		jsonObject.put(Constants.Json.Response.ERROR, true);
		jsonObject.put(Constants.Json.Response.STATUS, HttpStatus.SC_NOT_FOUND);
		if (panlProperties.getUseVerbose404Messages()) {
			jsonObject.put(Constants.Json.Response.MESSAGE, PanlDefaultHandler.JSON_VALUE_MESSAGE);
			jsonObject.put(Constants.Json.Response.VALID_URLS, validUrls);
		} else {
			jsonObject.put(Constants.Json.Response.MESSAGE, Constants.Json.Response.JSON_VALUE_MESSAGE_404);
		}

		response.setEntity(
			new StringEntity(jsonObject.toString(),
				ResourceHelper.CONTENT_TYPE_JSON));
	}

	/**
	 * <p>Set the response as a 503 status code, there is no verbose messaging
	 * flag for this as the Solr server has gone away :(</p>
	 *
	 * <p><strong>NOTE:</strong> This will set the response code and body, but not
	 * return it.</p>
	 *
	 * @param response The response object
	 */
	protected void set503ResponseMessage(HttpResponse response) {
		response.setStatusCode(HttpStatus.SC_SERVICE_UNAVAILABLE);

		JSONObject jsonObject = new JSONObject();

		jsonObject.put(Constants.Json.Response.ERROR, true);
		jsonObject.put(Constants.Json.Response.STATUS, HttpStatus.SC_SERVICE_UNAVAILABLE);

		response.setEntity(
				new StringEntity(jsonObject.toString(),
						ResourceHelper.CONTENT_TYPE_JSON));
	}
}
