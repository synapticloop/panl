package com.synapticloop.panl.server.handler.webapp;

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

import com.synapticloop.panl.server.handler.webapp.util.ResourceHelper;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

/**
 * <p>The Static handler passes any requests to the resource helper to serve
 * static resources from the classpath.</p>
 *
 * @author synapticloop
 * @see ResourceHelper
 */
public class PanlResultsStaticHandler implements HttpRequestHandler {

	/**
	 * <p>Instantiate the PanlResultsStaticHandler and do nothing</p>
	 */
	public PanlResultsStaticHandler() {
	}

	/**
	 * <p>Handle a request for a static resource loading it from the classpath.
	 * In effect this passes this through to the ResourceHelper.</p>
	 *
	 * @param request The request
	 * @param response The response
	 * @param context The context (unused)
	 *
	 * @see ResourceHelper
	 */
	@Override public void handle(HttpRequest request, HttpResponse response, HttpContext context) {
		ResourceHelper.serveResource(request.getRequestLine().getUri(), response);
	}
}
