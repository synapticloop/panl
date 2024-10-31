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

package com.synapticloop.panl.server.handler;

import com.synapticloop.panl.server.handler.properties.PanlProperties;
import com.synapticloop.panl.server.handler.webapp.util.ResourceHelper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import static com.synapticloop.panl.server.handler.webapp.util.ResourceHelper.*;

public abstract class BaseResponseHandler {

	protected final PanlProperties panlProperties;
	protected JSONArray validUrls = new JSONArray();

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
	 * verbose messaging is set.  If so, it will add in the exception class and
	 * the message of the exception.</p>
	 *
	 * <p><strong>NOTE:</strong> This will set the response code and body, but not
	 * return it.</p>
	 *
	 * @param response  The response object
	 * @param exception The exception that was thrown
	 */
	protected void set500ResponseMessage(HttpResponse response, Exception exception) {
		getLogger().error("Internal server error, message was '{}'", exception.getMessage(), exception);
		response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(JSON_KEY_ERROR, true);
		jsonObject.put(JSON_KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
		if (panlProperties.getUseVerbose500Messages()) {
			jsonObject.put(JSON_KEY_MESSAGE,
				String.format("Class: %s, message: %s.",
					exception.getClass().getCanonicalName(),
					exception.getMessage()));

			response.setEntity(new StringEntity(jsonObject.toString(), ResourceHelper.CONTENT_TYPE_JSON));
		} else {
			jsonObject.put(JSON_KEY_MESSAGE, JSON_VALUE_MESSAGE_500);
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
	 * @param response  The response object
	 */
	protected void set404ResponseMessage(HttpResponse response) {
		response.setStatusCode(HttpStatus.SC_NOT_FOUND);

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
