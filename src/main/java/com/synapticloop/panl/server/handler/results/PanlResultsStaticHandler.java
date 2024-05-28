package com.synapticloop.panl.server.handler.results;

import com.synapticloop.panl.server.handler.util.ResourceHelper;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;


public class PanlResultsStaticHandler implements HttpRequestHandler {

	public PanlResultsStaticHandler() {
	}

	@Override public void handle(HttpRequest request, HttpResponse response, HttpContext context) {
		ResourceHelper.serveResource(request.getRequestLine().getUri(), response);
	}
}
