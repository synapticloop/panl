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
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.util.List;

/**
 * <p>The Panl Results Viewer is the in-built Panl web app that provides a
 * fully functional search UI to test out configurations and should be used for
 * testing purposes.</p>
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
public class PanlResultsViewerHandler implements HttpRequestHandler {
	public static final String WEBAPP_VIEWER_INDEX_HTML = "/webapp/viewer/index.html";

	public PanlResultsViewerHandler(List<CollectionRequestHandler> collectionRequestHandlers) {
	}

	/**
	 * <p>Handle the request.</p>
	 *
	 * @param request the HTTP request.
	 * @param response the HTTP response.
	 * @param context the HTTP execution context.
	 */
	@Override public void handle(HttpRequest request, HttpResponse response, HttpContext context) {
		ResourceHelper.serveResource(WEBAPP_VIEWER_INDEX_HTML, response);
	}
}
