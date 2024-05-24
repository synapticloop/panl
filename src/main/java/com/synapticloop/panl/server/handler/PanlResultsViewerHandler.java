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


public class PanlResultsViewerHandler implements HttpRequestHandler {
	public static final ContentType CONTENT_TYPE_JSON = ContentType.create("application/json", "UTF-8");
	public PanlResultsViewerHandler(List<CollectionRequestHandler> collectionRequestHandlers) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("error", 404);
		jsonObject.put("message", "Could not find a PANL request url, see 'valid_urls' array.");
		JSONArray validUrls = new JSONArray();
		for (CollectionRequestHandler collectionRequestHandler: collectionRequestHandlers) {
			validUrls.put("/" +collectionRequestHandler.getCollectionName() + "/*");
		}
		jsonObject.put("valid_urls", validUrls);
	}

	@Override public void handle(HttpRequest request, HttpResponse response, HttpContext context) {
		response.setStatusCode(HttpStatus.SC_NOT_FOUND);
		response.setEntity(
				new StringEntity("{}",
						CONTENT_TYPE_JSON));
	}
}
