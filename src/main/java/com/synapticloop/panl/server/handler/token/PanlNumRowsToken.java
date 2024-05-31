package com.synapticloop.panl.server.handler.token;

import com.synapticloop.panl.server.properties.CollectionProperties;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.StringTokenizer;

public class PanlNumRowsToken extends PanlToken {
	private int numRows;
	private boolean isValid = true;

	public PanlNumRowsToken(String panlLpseCode) {
		super(panlLpseCode);
	}

	public PanlNumRowsToken(CollectionProperties collectionProperties, String panlLpseCode, StringTokenizer valueTokenizer) {
		super(panlLpseCode);

		int numRowsTemp;
		if (valueTokenizer.hasMoreTokens()) {
			try {
				numRowsTemp = Integer.parseInt(valueTokenizer.nextToken());
			} catch (NumberFormatException e) {
				isValid = false;
				numRowsTemp = collectionProperties.getResultRows();
			}
		} else {
			isValid = false;
			numRowsTemp = collectionProperties.getResultRows();
		}
		this.numRows = numRowsTemp;
	}

	@Override public String getUriComponent() {
		return(this.numRows + "/");
	}

	@Override public String getLpseComponent() {
		return(panlLpseCode);
	}

	@Override public String explain() {
		return ("PANL " +
				(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				" <rows>        LPSE code '" +
				this.panlLpseCode +
				"' using " +
				(this.isValid ? "parsed" : "default") +
				" value of '" +
				this.numRows +
				"'.");
	}

	@Override public void applyToQuery(SolrQuery solrQuery) {
		solrQuery.setRows(this.numRows);
	}

	@Override public String getType() {
		return("rows");
	}

	public int getNumRows() {
		return(this.numRows);
	}

}
