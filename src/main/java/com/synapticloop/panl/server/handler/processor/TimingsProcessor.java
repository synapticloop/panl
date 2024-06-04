package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.server.handler.CollectionRequestHandler;
import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

	long parseRequestNanos = (Long)params[0];
	long buildRequestNanos = (Long)params[1];
	long sendAndReceiveNanos = (Long)params[2];
	long buildResponse = (Long)params[3];

	JSONObject timingsObject = new JSONObject();

	// add in some statistics
		timingsObject.put(PANL_PARSE_REQUEST_TIME,TimeUnit.NANOSECONDS.toMillis(parseRequestNanos));
		timingsObject.put(PANL_BUILD_REQUEST_TIME,TimeUnit.NANOSECONDS.toMillis(buildRequestNanos));
		timingsObject.put(PANL_SEND_REQUEST_TIME,TimeUnit.NANOSECONDS.toMillis(sendAndReceiveNanos));

		timingsObject.put(PANL_BUILD_RESPONSE_TIME,TimeUnit.NANOSECONDS.toMillis(buildResponse));
		timingsObject.put(PANL_TOTAL_TIME,TimeUnit.NANOSECONDS.toMillis(
	parseRequestNanos +
	buildRequestNanos +
	sendAndReceiveNanos +
	buildResponse
		));
		return(timingsObject);
}
}
