package com.synapticloop.panl.server.handler.token;

import org.apache.solr.client.solrj.SolrQuery;

import java.util.StringTokenizer;

public abstract class PanlToken {
	protected final String panlLpseCode;
	protected String value;

	public PanlToken(String panlLpseCode) {
		this.panlLpseCode = panlLpseCode;
	}

	public String getPanlLpseCode() {
		return(panlLpseCode);
	}

	public String getPanlLpseValue() {
		return(value);
	}

	public abstract String getUriComponent();
	public abstract String getLpseComponent();

	public abstract String explain();

	public abstract void applyToQuery(SolrQuery solrQuery);
}
