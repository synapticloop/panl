package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public abstract class Processor {
	protected final CollectionProperties collectionProperties;

	public Processor(CollectionProperties collectionProperties) {
		this.collectionProperties = collectionProperties;
	}

	public abstract JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, Object... params);

	public JSONArray processToArray(Map<String, List<LpseToken>> panlTokenMap, Object... params) {
		return(new JSONArray());
	}

	public String processToString(Map<String, List<LpseToken>> panlTokenMap, Object... params) {
		return("");
	}

}