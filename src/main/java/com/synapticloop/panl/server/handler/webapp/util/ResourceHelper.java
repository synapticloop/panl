package com.synapticloop.panl.server.handler.webapp.util;

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

import com.synapticloop.panl.server.handler.webapp.PanlResultsStaticHandler;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.*;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>A helper class for serving up assets from the classpath.  Used by the
 * panl result viewer / explainer (and helpers) in-built handler.</p>
 *
 * @author synapticloop
 */
public class ResourceHelper {
	public static final String URL_PANL_RESULTS_VIEWER_SUBSET = "/panl-results-viewer";

	public static final String JSON_KEY_ERROR = "error";
	public static final String JSON_KEY_STATUS = "status";
	public static final String JSON_KEY_MESSAGE = "message";
	public static final String JSON_KEY_VALID_URLS = "valid_urls";

	public static final String JSON_VALUE_MESSAGE_404 = "Not found";
	public static final String JSON_VALUE_MESSAGE_500 = "internal server error";

	public static final ContentType CONTENT_TYPE_JSON = ContentType.create("application/json", StandardCharsets.UTF_8);
	public static final ContentType CONTENT_TYPE_TEXT = ContentType.create("text/plain", StandardCharsets.UTF_8);
	public static final ContentType CONTENT_TYPE_CSS = ContentType.create("text/css", StandardCharsets.UTF_8);
	public static final ContentType CONTENT_TYPE_HTML = ContentType.create("text/html", StandardCharsets.UTF_8);
	public static final ContentType CONTENT_TYPE_JS = ContentType.create("text/javascript ", StandardCharsets.UTF_8);
	public static final ContentType CONTENT_TYPE_PNG = ContentType.create("image/png");

	private static final Map<String, ContentType> CONTENT_TYPE_MAP = new HashMap<>();
	static {
		CONTENT_TYPE_MAP.put(".json", CONTENT_TYPE_JSON);
		CONTENT_TYPE_MAP.put(".css", CONTENT_TYPE_CSS);
		CONTENT_TYPE_MAP.put(".js", CONTENT_TYPE_JS);
		CONTENT_TYPE_MAP.put(".png", CONTENT_TYPE_PNG);
		CONTENT_TYPE_MAP.put(".html", CONTENT_TYPE_HTML);
	}

	private static final Map<String, byte[]> CONTENT_CACHE = new HashMap<>();
	private static final Map<String, ContentType> CONTENT_TYPE_CACHE = new HashMap<>();
	private static final Map<String, Integer> CONTENT_RESPONSE_CODE_CACHE = new HashMap<>();

	/**
	 * <p>Serve a resourcePath from the class loader and cache the response.</p>
	 *
	 * <p><strong> NOTE:</strong> that this is not supposed to be performant.</p>
	 *
	 * @param resourcePath the path to the resource
	 * @param response The response object to write to
	 */
	public static void serveResource(String resourcePath, HttpResponse response) {
		if(CONTENT_CACHE.containsKey(resourcePath)) {
			response.setStatusCode(CONTENT_RESPONSE_CODE_CACHE.get(resourcePath));
			response.setEntity(new ByteArrayEntity(CONTENT_CACHE.get(resourcePath), CONTENT_TYPE_CACHE.get(resourcePath)));
			return;
		}

		try (InputStream resourceAsStream = PanlResultsStaticHandler.class.getResourceAsStream(resourcePath)) {
			if (null != resourceAsStream) {
				byte[] content = resourceAsStream.readAllBytes();
				ContentType contentType = getContentType(resourcePath);

				CONTENT_CACHE.put(resourcePath, content);
				CONTENT_TYPE_CACHE.put(resourcePath, contentType);
				CONTENT_RESPONSE_CODE_CACHE.put(resourcePath, HttpStatus.SC_OK);
			} else {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(JSON_KEY_ERROR, true);
				jsonObject.put(JSON_KEY_MESSAGE, "Could not find the resourcePath '" + resourcePath + "'");
				CONTENT_CACHE.put(resourcePath, jsonObject.toString().getBytes());
				CONTENT_TYPE_CACHE.put(resourcePath, CONTENT_TYPE_JSON);
				CONTENT_RESPONSE_CODE_CACHE.put(resourcePath, HttpStatus.SC_NOT_FOUND);
			}
		} catch (IOException ioex) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(JSON_KEY_ERROR, true);
			jsonObject.put(JSON_KEY_MESSAGE, "Could not serve the resourcePath '" + resourcePath + "'");
			CONTENT_CACHE.put(resourcePath, jsonObject.toString().getBytes());
			CONTENT_TYPE_CACHE.put(resourcePath, CONTENT_TYPE_JSON);
			CONTENT_RESPONSE_CODE_CACHE.put(resourcePath, HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}

		response.setStatusCode(CONTENT_RESPONSE_CODE_CACHE.get(resourcePath));
		response.setEntity(new ByteArrayEntity(CONTENT_CACHE.get(resourcePath), CONTENT_TYPE_CACHE.get(resourcePath)));
	}

	/**
	 * <p>Simple get the content type from the extension helper method.  Just a
	 * quick lookup on the extension of the file.</p>
	 *
	 * <p>There is a limited number of content types that this will work on, with
	 * the default for unknown extensions being 'text/plain'.</p>
	 *
	 * @param resource The resource to look up the extension for
	 *
	 * @return The content type for the resource
	 */
	private static ContentType getContentType(String resource) {
		int lastIndexOf = resource.lastIndexOf('.');
		if(lastIndexOf != -1) {
			String extension = resource.substring(lastIndexOf);
			return(CONTENT_TYPE_MAP.getOrDefault(extension, CONTENT_TYPE_TEXT));
		}
		return(CONTENT_TYPE_TEXT);
	}
}
