package com.synapticloop.panl.server.handler.token;

import org.apache.solr.client.solrj.SolrQuery;

public class PanlPageToken extends PanlToken {
	public PanlPageToken(String panlLpseCode) {
		super(panlLpseCode);
	}

	@Override public String getUriComponent() {
		return null;
	}

	@Override public String getLpseComponent() {
		return null;
	}

	@Override public String explain() {
		return null;
	}

	@Override public void applyToQuery(SolrQuery solrQuery) {
//		solrQuery.setStart()
	}
	@Override public String getType() {
		return("page");
	}

}
