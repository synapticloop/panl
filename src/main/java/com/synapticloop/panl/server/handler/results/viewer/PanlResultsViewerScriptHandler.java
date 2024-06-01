package com.synapticloop.panl.server.handler.results.viewer;

import com.synapticloop.panl.server.handler.CollectionRequestHandler;
import com.synapticloop.panl.server.handler.results.util.ResourceHelper;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.util.ArrayList;
import java.util.List;


public class PanlResultsViewerScriptHandler implements HttpRequestHandler {

	private final List<String> collectionUrls = new ArrayList<>();

	public PanlResultsViewerScriptHandler(List<CollectionRequestHandler> collectionRequestHandlers) {
		for (CollectionRequestHandler collectionRequestHandler : collectionRequestHandlers) {
			String collectionName = collectionRequestHandler.getCollectionName();
			for (String resultFieldsName : collectionRequestHandler.getResultFieldsNames()) {
				collectionUrls.add("/" + collectionName + "/" + resultFieldsName);
			}
		}
	}

	@Override public void handle(HttpRequest request, HttpResponse response, HttpContext context) {
		StringBuilder sb = new StringBuilder("var panlResultsViewerUrl=\"")
				.append(ResourceHelper.URL_PANL_RESULTS_VIEWER_SUBSET)
				.append("\";\n")
				.append("var collections = [");

		int i = 0;
		for (String collectionUrl : collectionUrls) {

			if(i != 0) {
				sb.append(",");
			}

			sb.append("\"")
					.append(collectionUrl)
					.append("\"");

			i++;
		}

		sb.append("];\n");

		response.setStatusCode(HttpStatus.SC_OK);
		response.setEntity(
				new StringEntity(sb.toString(),
						ResourceHelper.CONTENT_TYPE_JS));
	}
}
