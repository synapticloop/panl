package com.synapticloop.panl.server.tokeniser.token;

import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.properties.field.BaseField;
import com.synapticloop.panl.server.tokeniser.PanlTokeniser;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.StringTokenizer;

public class FacetLpseToken extends LpseToken {
	private String solrField = null;
	private CollectionProperties collectionProperties;

	public FacetLpseToken(
			CollectionProperties collectionProperties,
			String panlLpseCode,
			PanlTokeniser lpseTokeniser,
			StringTokenizer valueTokeniser) {
		super(panlLpseCode);
		this.collectionProperties = collectionProperties;

		StringBuilder sb = new StringBuilder(panlLpseCode);
		int i = 1;
		while (i < collectionProperties.getPanlLpseNum()) {
			if (lpseTokeniser.hasMoreTokens()) {
				sb.append(lpseTokeniser.nextToken());
			}
			i++;
		}

		this.lpseCode = sb.toString();

		BaseField lpseField = collectionProperties.getLpseField(this.lpseCode);
		if (null != lpseField) {
			this.originalValue = valueTokeniser.nextToken();
			this.value = lpseField.getDecodedValue(this.originalValue);

			if (null == this.value) {
				isValid = false;
			}
		} else {
			this.isValid = false;
		}

		if (collectionProperties.hasFacetCode(panlLpseCode)) {
			this.solrField = collectionProperties.getSolrFieldNameFromPanlLpseCode(panlLpseCode);
		} else {
			this.isValid = false;
		}
	}

	@Override public String getUriPathComponent() {
		if (isValid) {
			BaseField lpseField = collectionProperties.getLpseField(this.lpseCode);
			return(lpseField.getEncodedPanlValue(this.value) + "/");
		} else {
			return ("");
		}
	}

	@Override public String getLpseComponent() {
		if (isValid) {
			return (this.lpseCode);
		} else {
			return ("");
		}
	}

	@Override public String explain() {
		return ("PANL " +
				(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				" <facet>         LPSE code '" +
				this.lpseCode +
				"' (solr field '" +
				this.solrField +
				"') with parsed value '" +
				value +
				"', incoming value '" +
				this.originalValue +
				"'.");
	}

	@Override public void applyToQuery(SolrQuery solrQuery) {
		if (isValid) {
			solrQuery.addFilterQuery(this.solrField + ":\"" + value + "\"");
		}
	}

	@Override public String getType() {
		return ("facet");
	}

	public String getSolrField() {
		return solrField;
	}
}
