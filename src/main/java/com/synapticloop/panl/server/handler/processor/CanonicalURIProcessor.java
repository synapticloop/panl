package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.properties.field.BaseField;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
/**
 * <p>Get the canonical URI Path for the query.  This will always return more
 * than an empty path as it includes the following default LPSE path
 * components:</p>
 *
 * <ul>
 *   <li>The Sort order - sorted by relevance by default.</li>
 *   <li>The page number - page 1 by default</li>
 *   <li>The number of results per page - this is defined, or 10 by default</li>
 *   <li>The query operand - this is defined, or 10 by default</li>
 * </ul>
 *
 * <p><strong>NOTE:</strong> The pass-through parameter, if defined, will
 * never be included.</p>
 */
public class CanonicalURIProcessor extends Processor {

	public CanonicalURIProcessor(CollectionProperties collectionProperties) {
		super(collectionProperties);
	}

	@Override public JSONObject processToObject(Map<String, List<LpseToken>> panlTokenMap, Object... params) {
		return(new JSONObject());
	}

	@Override public String processToString(Map<String, List<LpseToken>> panlTokenMap, Object... params) {
		StringBuilder canonicalUri = new StringBuilder("/");
		StringBuilder canonicalLpse = new StringBuilder();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			canonicalUri.append(baseField.getCanonicalUriPath(panlTokenMap, collectionProperties));
			canonicalLpse.append(baseField.getCanonicalLpseCode(panlTokenMap, collectionProperties));
		}

		return (canonicalUri.toString() + canonicalLpse + "/");
	}
}
