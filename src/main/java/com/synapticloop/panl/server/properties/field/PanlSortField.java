package com.synapticloop.panl.server.properties.field;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.tokeniser.token.SortLpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PanlSortField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlSortField.class);

	public PanlSortField(String lpseCode, String propertyKey, Properties properties, String collectionName) throws PanlServerException {
		super(lpseCode, propertyKey, collectionName);
	}

	@Override public String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return("");
	}

	@Override public String getCanonicalLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder(panlLpseCode);
		if(panlTokenMap.containsKey(panlLpseCode)) {
			SortLpseToken lpseToken = (SortLpseToken) panlTokenMap.get(panlLpseCode).get(0);
			if(lpseToken.getIsValid()) {
				sb.append(lpseToken.getPanlFacetCode());
				sb.append(lpseToken.getSortCode());
			} else {
				sb.append("-");
			}
		} else {
			sb.append("-");
		}
		return(sb.toString());
	}

	@Override public String getURIPath(LpseToken token, CollectionProperties collectionProperties) {
		return("");
	}

	@Override public String getLpseCode(LpseToken token, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder(token.getLpseCode());
		SortLpseToken lpseToken = (SortLpseToken) token;
		if(lpseToken.getIsValid()) {
			sb.append(lpseToken.getPanlFacetCode());
			sb.append(lpseToken.getSortCode());
		} else {
			sb.append("-");
		}
		return(sb.toString());
	}

	@Override
	public String getURIPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return("");
	}

	@Override
	public String getLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		if(panlTokenMap.containsKey(collectionProperties.getPanlParamSort())) {
			return(getCanonicalLpseCode(panlTokenMap, collectionProperties));
		}
		return("");
	}

	@Override public Logger getLogger() {
		return(LOGGER);
	}

	@Override public List<String> explain() {
		List<String> temp = new ArrayList<>();
		temp.add("FIELD CONFIG [ " +
				this.getClass().getSimpleName() +
				" ] LPSE code '" +
				panlLpseCode +
				"'.");

		return(temp);
	}

	@Override public String getExplainDescription() {
		return("The sort order (default is relevance descending), but can be any PanlField, or PanlFacet.");
	}

	public void applyToQueryInternal(SolrQuery solrQuery, Map<String, List<LpseToken>> panlTokenMap) {
		if(panlTokenMap.containsKey(panlLpseCode)) {
			SortLpseToken lpseToken = (SortLpseToken)panlTokenMap.get(panlLpseCode).get(0);
			solrQuery.addSort(lpseToken.getSolrFacetField(), lpseToken.getSortOrder());
		}
	}
}