package com.synapticloop.panl.server.handler.webapp.viewer;

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

import com.synapticloop.panl.server.handler.CollectionRequestHandler;
import com.synapticloop.panl.server.handler.webapp.util.ResourceHelper;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>The Panl Results Viewer Script Handler is used by the in-built Panl web
 * apps for generating the Panl collection URLs as JavaScript variables.  This
 * should only be used for testing purposes.</p>
 *
 * <p><strong>THIS HANDLER AND REGISTERED URL(s) ARE DESIGNED FOR TESTING
 * PURPOSES AND NOT RECOMMENDED FOR PRODUCTION USES.</strong></p>
 *
 * <p>It is recommended to disable this handler by setting the property
 * <code>panl.results.testing.urls</code> to <code>false</code> in the
 * <code>panl.properties</code> file.</p>
 *
 * @author synapticloop
 */
public class PanlResultsViewerScriptHandler implements HttpRequestHandler {
	private final List<String> panlCollectionUris = new ArrayList<>();

	/**
	 * <p>Instantiate the Script handler which builds the CaFUPs that are
	 * available for this handler.</p>
	 *
	 * <p>In effect this builds a list of CaFUPs so that they can be served as a
	 * JSON script.</p>
	 *
	 * @param collectionRequestHandlers The list of collection request handlers
	 */
	public PanlResultsViewerScriptHandler(List<CollectionRequestHandler> collectionRequestHandlers) {
		for (CollectionRequestHandler collectionRequestHandler : collectionRequestHandlers) {
			String panlCollectionUri = collectionRequestHandler.getPanlCollectionUri();
			for (String resultFieldsName : collectionRequestHandler.getResultFieldsNames()) {
				panlCollectionUris.add("/" + panlCollectionUri + "/" + resultFieldsName);
			}
		}
	}

	@Override public void handle(HttpRequest request, HttpResponse response, HttpContext context) {
		StringBuilder sb = new StringBuilder("var panlResultsViewerUrl=\"")
				.append(ResourceHelper.URL_PANL_RESULTS_VIEWER_SUBSET)
				.append("\";\n")
				.append("var collections = [");

		int i = 0;
		for (String panlCollectionUri : panlCollectionUris) {

			if(i != 0) {
				sb.append(",");
			}

			sb.append("\"")
					.append(panlCollectionUri)
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
