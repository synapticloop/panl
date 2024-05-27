package com.synapticloop.panl.server.handler.token;

import com.synapticloop.panl.server.properties.CollectionProperties;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.solr.client.solrj.SolrQuery;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public class PanlFacetToken extends PanlToken {
	private String solrField = null;
	private final String value;
	private boolean isValid = true;
	public PanlFacetToken(
			CollectionProperties collectionProperties,
			String panlLpseCode,
			PanlStringTokeniser lpseTokeniser,
			StringTokenizer valueTokeniser) {
		super(panlLpseCode);

		this.value = valueTokeniser.nextToken();
		if(collectionProperties.hasFacetCode(panlLpseCode)) {
			this.solrField = collectionProperties.getNameFromCode(panlLpseCode);
		} else {
			this.isValid = false;
		}
	}

	@Override public String getUriComponent() {
		return null;
	}

	@Override public String getLpseComponent() {
		return null;
	}

	@Override public String explain() {
			return ("PANL " +
					(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
					" <facet> LPSE code '" +
					this.panlLpseCode +
					"' (solr field '" +
					this.solrField +
					"') with value '" +
					value +
					"'.");
	}

	@Override public void applyToQuery(SolrQuery solrQuery) {
		if(isValid) {
			solrQuery.addFilterQuery(this.solrField + ":" + URLEncoder.encode(value, StandardCharsets.UTF_8));
		}
	}
}
