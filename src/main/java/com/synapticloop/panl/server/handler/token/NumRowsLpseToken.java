package com.synapticloop.panl.server.handler.token;

import com.synapticloop.panl.server.properties.CollectionProperties;
import org.apache.solr.client.solrj.SolrQuery;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public class NumRowsLpseToken extends LpseToken {
	private int numRows;
	private boolean isValid = true;
	private CollectionProperties collectionProperties;

	public NumRowsLpseToken(String panlLpseCode) {
		super(panlLpseCode);
	}

	public NumRowsLpseToken(CollectionProperties collectionProperties, String panlLpseCode, StringTokenizer valueTokenizer) {
		super(panlLpseCode);
		this.collectionProperties = collectionProperties;

		int numRowsTemp;

		if (valueTokenizer.hasMoreTokens()) {
			String numRowsTempString = collectionProperties
				.getConvertedFromPanlValue(
						panlLpseCode,
						URLDecoder.decode(
								valueTokenizer.nextToken(),
								StandardCharsets.UTF_8));

			try {
				numRowsTemp = Integer.parseInt(numRowsTempString);
			} catch (NumberFormatException e) {
				isValid = false;
				numRowsTemp = collectionProperties.getNumResultsPerPage();
			}
		} else {
			isValid = false;
			numRowsTemp = collectionProperties.getNumResultsPerPage();
		}
		this.numRows = numRowsTemp;
	}

	@Override public String getUriComponent() {
		return (
				URLEncoder.encode(
						collectionProperties.getConvertedToPanlValue(
								panlLpseCode,
								Integer.toString(numRows)),
						StandardCharsets.UTF_8) +
						"/");
	}

	@Override public String getLpseComponent() {
		return(panlLpseCode);
	}

	@Override public String explain() {
		// TODO - suffix and prefix
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
		return("Number of results per page LPSE code");
	}

	public int getNumRows() {
		return(this.numRows);
	}
}
