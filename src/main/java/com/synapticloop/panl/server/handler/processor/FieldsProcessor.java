package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class FieldsProcessor extends Processor {
	public FieldsProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	@Override public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, Object... params) {
		return(collectionProperties.getSolrFieldToPanlNameLookup());
	}
}
