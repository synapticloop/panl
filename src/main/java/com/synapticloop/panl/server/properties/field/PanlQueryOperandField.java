package com.synapticloop.panl.server.properties.field;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.tokeniser.token.QueryOperandLpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PanlQueryOperandField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlQueryOperandField.class);

	public PanlQueryOperandField(String lpseCode, String propertyKey, Properties properties, String collectionName) throws PanlServerException {
		super(lpseCode, propertyKey, collectionName);
	}

	@Override public String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return("");
	}

	@Override public String getCanonicalLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder(panlLpseCode);
		if(panlTokenMap.containsKey(panlLpseCode)) {
			QueryOperandLpseToken lpseToken = (QueryOperandLpseToken) panlTokenMap.get(panlLpseCode).get(0);
			if(lpseToken.getIsValid()) {
				sb.append(lpseToken.getQueryOperand());
			} else {
				sb.append(collectionProperties.getDefaultQueryOperand());
			}
		} else {
			sb.append(collectionProperties.getDefaultQueryOperand());
		}
		return(sb.toString());
	}

	public String getResetUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return("");
	}

	@Override public Logger getLogger() {
		return(LOGGER);
	}

	@Override public String getExplainDescription() {
		return("The query operand which maps to the 'q.op' parameter of Solr");
	}

	public void applyToQueryInternal(SolrQuery solrQuery, Map<String, List<LpseToken>> panlTokenMap) {
		if(panlTokenMap.containsKey(panlLpseCode)) {
			QueryOperandLpseToken lpseToken = (QueryOperandLpseToken)panlTokenMap.get(panlLpseCode).get(0);
			solrQuery.setParam("q.op", lpseToken.getQOpValue());
		}
	}

}