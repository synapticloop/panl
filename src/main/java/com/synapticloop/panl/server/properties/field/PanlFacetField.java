package com.synapticloop.panl.server.properties.field;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.tokeniser.token.FacetLpseToken;
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

		// lastly, we are going to check to see whether this is an 'OR' field
		populateFacetOr(properties, lpseCode);
	}

	@Override
	public Logger getLogger() {
		return (LOGGER);
	}

	@Override public String getExplainDescription() {
		return ("A Solr field that can be used as a facet, returned in the field set, or configured to be sorted by.");
	}

	protected void applyToQueryInternal(SolrQuery solrQuery, Map<String, List<LpseToken>> panlTokenMap) {
		if (!isOrFacet) {
			// just go through and set the filter queries - these will be ANDed together
			for (LpseToken lpseToken : panlTokenMap.get(getPanlLpseCode())) {
				FacetLpseToken facetLpseToken = (FacetLpseToken) lpseToken;
				solrQuery.addFilterQuery(facetLpseToken.getSolrField() + ":\"" + facetLpseToken.getValue() + "\"");
			}
			return;
		}

		StringBuilder stringBuilder = new StringBuilder();
		boolean isFirst = true;
		// at this point, we are going through the or filters
		for (LpseToken lpseToken : panlTokenMap.get(getPanlLpseCode())) {
			FacetLpseToken facetLpseToken = (FacetLpseToken) lpseToken;
			if (isFirst) {
				stringBuilder
						.append(facetLpseToken.getSolrField())
						.append(":(");
			}

			if(!isFirst) {
				stringBuilder.append(" OR ");
			}

			stringBuilder
					.append("\"")
					.append(facetLpseToken.getValue())
					.append("\"");

			isFirst = false;
		}

		stringBuilder.append(")");
		solrQuery.addFilterQuery(stringBuilder.toString());
	}
}