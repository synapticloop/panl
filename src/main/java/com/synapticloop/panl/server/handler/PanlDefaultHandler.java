package com.synapticloop.panl.server.handler;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * <p>This is the default handler for any URLs that are not bound to a
 * collection.  It returns a 404 error code with a JSON object as the response.</p>
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
 */
public class PanlDefaultHandler implements HttpRequestHandler {
	public static final ContentType CONTENT_TYPE_JSON = ContentType.create("application/json", "UTF-8");
	private static String json404ErrorString;

	/**
	 * <p>Instantiate the default request handler.</p>
	 *
	 * <p>In effect, this will create the JSON response at instantiation time,
	 * which will then be statically served for every request.</p>
	 *
	 * @param collectionRequestHandlers The collection request handlers to iterate
	 *        through to build the static response.
	 */
	public PanlDefaultHandler(List<CollectionRequestHandler> collectionRequestHandlers) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("error", 404);
		jsonObject.put("message", "Could not find a PANL request url, see 'valid_urls' array.");
		JSONArray validUrls = new JSONArray();
		for (CollectionRequestHandler collectionRequestHandler: collectionRequestHandlers) {
			validUrls.put("/" +collectionRequestHandler.getCollectionName() + "/*");
		}
		jsonObject.put("valid_urls", validUrls);

		json404ErrorString = jsonObject.toString();
	}

	/**
	 * <p>As the default handler for the PANL server - this will always return a
	 * 404 response with JSON body.</p>
	 *
	 * @param request the HTTP request.
	 * @param response the HTTP response.
	 * @param context the HTTP execution context.
	 */
	@Override public void handle(HttpRequest request, HttpResponse response, HttpContext context) {
		response.setStatusCode(HttpStatus.SC_NOT_FOUND);
		response.setEntity(
				new StringEntity(json404ErrorString,
						CONTENT_TYPE_JSON));
	}
}
