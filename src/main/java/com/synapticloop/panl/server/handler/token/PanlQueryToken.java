package com.synapticloop.panl.server.handler.token;

import com.synapticloop.panl.server.properties.CollectionProperties;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.params.HttpParams;
import org.apache.solr.client.solrj.SolrQuery;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringTokenizer;

public class PanlQueryToken extends PanlToken {
	private String value;
	private boolean isOverride;

	public PanlQueryToken(String panlLpseCode) {
		super(panlLpseCode);
	}

	public PanlQueryToken(
			String queryFromUri,
			String panlLpseCode,
			StringTokenizer valueTokeniser) {
		super(panlLpseCode);
		this.value = valueTokeniser.nextToken();

		for (NameValuePair nameValuePair : URLEncodedUtils.parse(queryFromUri, StandardCharsets.UTF_8)) {
			// TODO - do we want to allow people to change this???
			if(nameValuePair.getName().equals("q")) {
				this.value = nameValuePair.getValue();
				isOverride = true;
				break;
			}
		}
	}

	@Override public String getUriComponent() {
		return(value + "/");
	}

	@Override public String getLpseComponent() {
		return(panlLpseCode);
	}

	@Override public String explain() {
		return ("PANL [  VALID  ] <query> LPSE code '" +
				this.panlLpseCode +
				"' with value '" +
				value +
				"'" +
				(isOverride ? " (Overridden by query parameter).": ".")
		);
	}

	@Override public void applyToQuery(SolrQuery solrQuery) {
		solrQuery.setQuery(value);
	}
}
