package com.synapticloop.panl.server.handler;

import com.synapticloop.panl.server.properties.PanlProperties;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.json.JSONObject;

/**
 * <p>This is the default handler for all requests and simply passes the
 * processing to the appropriate collection for execution.</p>
 *
 * @author synapticloop
 */
public class PanlRequestHandler implements HttpRequestHandler {
	public static final ContentType CONTENT_TYPE_JSON = ContentType.create("application/json", "UTF-8");
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
	 * @param request  the HTTP request.
	 * @param response the HTTP response.
	 * @param context  the HTTP execution context. (which is ignored by this processor)
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
							collectionRequestHandler.getValidUrlsJSON(),
							CONTENT_TYPE_JSON));
			return;
		}

		try {
			response.setStatusCode(HttpStatus.SC_OK);
			response.setEntity(
					new StringEntity(
					collectionRequestHandler.handleRequest(uri, query),
					CONTENT_TYPE_JSON)
			);
		} catch (Exception e) {
			response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("error", true);
			jsonObject.put("status", 500);
			if(panlProperties.getPanlStatus500Verbose()) {
				jsonObject.put("message", e.getMessage());
				response.setEntity(new StringEntity(jsonObject.toString(), CONTENT_TYPE_JSON));
			} else {
				jsonObject.put("message", "Internal Server Error");
			}
		}
	}
}
