package com.synapticloop.panl.server.tokeniser.token;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.solr.client.solrj.SolrQuery;

import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public class QueryLpseToken extends LpseToken {
	private boolean isOverride;

	public QueryLpseToken(String panlLpseCode) {
		super(panlLpseCode);
	}

	public QueryLpseToken(
			String queryFromUri,
			String panlLpseCode) {
		this(queryFromUri, panlLpseCode, null);
	}

	public QueryLpseToken(
			String queryFromUri,
			String panlLpseCode,
			StringTokenizer valueTokeniser) {
		super(panlLpseCode);

		if(null != valueTokeniser && valueTokeniser.hasMoreTokens()) {
			this.value = valueTokeniser.nextToken();
		}

		for (NameValuePair nameValuePair : URLEncodedUtils.parse(queryFromUri, StandardCharsets.UTF_8)) {
			// TODO - do we want to allow people to change this???
			if(nameValuePair.getName().equals("q")) {
				this.value = nameValuePair.getValue();
				isOverride = true;
				break;
			}
		}
	}

	@Override public String getUriPathComponent() {
		if(null != value) {
			return (value + "/");
		} else {
			return("");
		}
	}

	@Override public String getLpseComponent() {
		if(null != value) {
			return (lpseCode);
		} else {
			return("");
		}
	}

	@Override public String explain() {
		return ("PANL [  VALID  ] <query>       LPSE code '" +
				this.lpseCode +
				"' with value '" +
				value +
				"'" +
				(isOverride ? " (Overridden by query parameter).": ".")
		);
	}

	@Override public void applyToQuery(SolrQuery solrQuery) {
		solrQuery.setQuery(value);
	}

	@Override public String getType() {
		return("query");
	}

}