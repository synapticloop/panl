package com.synapticloop.panl.server.properties.field;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PanlFacetField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlFacetField.class);


	public PanlFacetField(String lpseCode, String propertyKey, Properties properties, String collectionName, int panlLpseNum) throws PanlServerException {
		super(lpseCode, propertyKey, collectionName, panlLpseNum);

		populateSuffixAndPrefix(properties, lpseCode);
		populateBooleanReplacements(properties, lpseCode);
		populateSolrFieldType(properties, lpseCode);
		populatePanlAndSolrFieldNames(properties, lpseCode);
	}

	@Override
	public Logger getLogger() {
		return(LOGGER);
	}

//	@Override
//	public void applyToSolrQuery(SolrQuery solrQuery, Map<String, List<LpseToken>> panlTokenMap) {
//		// TODO - need to do the OR filtering....
//		if (panlTokenMap.containsKey(getPanlLpseCode())) {
//			List<LpseToken> lpseTokens = panlTokenMap.get(getPanlLpseCode());
//			if(lpseTokens.size() == 1) {
//			} else {
//				// this is where we check for the OR fields
//			}
//		}
//	}
}
