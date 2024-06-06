package com.synapticloop.panl.server.handler.processor;

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
 *  IN THE SOFTWARE.
 */

import com.synapticloop.panl.server.handler.fielderiser.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>Process the timings object.</p>
 *
 * @author synapticloop
 */
public class TimingsProcessor extends Processor {
	public static final String PANL_PARSE_REQUEST_TIME = "panl_parse_request_time";
	public static final String PANL_BUILD_REQUEST_TIME = "panl_build_request_time";
	public static final String PANL_SEND_REQUEST_TIME = "panl_send_request_time";
	public static final String PANL_BUILD_RESPONSE_TIME = "panl_build_response_time";
	public static final String PANL_TOTAL_TIME = "panl_total_time";

	public TimingsProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, Object... params) {
		long parseRequestNanos = (Long) params[0];
		long buildRequestNanos = (Long) params[1];
		long sendAndReceiveNanos = (Long) params[2];
		long buildResponse = (Long) params[3];

		JSONObject timingsObject = new JSONObject();

		// add in some statistics
		timingsObject.put(PANL_PARSE_REQUEST_TIME, TimeUnit.NANOSECONDS.toMillis(parseRequestNanos));
		timingsObject.put(PANL_BUILD_REQUEST_TIME, TimeUnit.NANOSECONDS.toMillis(buildRequestNanos));
		timingsObject.put(PANL_SEND_REQUEST_TIME, TimeUnit.NANOSECONDS.toMillis(sendAndReceiveNanos));

		timingsObject.put(PANL_BUILD_RESPONSE_TIME, TimeUnit.NANOSECONDS.toMillis(buildResponse));
		timingsObject.put(PANL_TOTAL_TIME, TimeUnit.NANOSECONDS.toMillis(
				parseRequestNanos +
						buildRequestNanos +
						sendAndReceiveNanos +
						buildResponse
		));

		return (timingsObject);
	}
}
