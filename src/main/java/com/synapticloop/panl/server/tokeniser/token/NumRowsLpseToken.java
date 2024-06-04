package com.synapticloop.panl.server.tokeniser.token;

import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.properties.field.BaseField;
import org.apache.solr.client.solrj.SolrQuery;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public class NumRowsLpseToken extends LpseToken {
	private CollectionProperties collectionProperties;

	private int numRows;

	public NumRowsLpseToken(String panlLpseCode) {
		super(panlLpseCode);
	}

	public NumRowsLpseToken(CollectionProperties collectionProperties, String panlLpseCode, StringTokenizer valueTokenizer) {
		super(panlLpseCode);
		this.collectionProperties = collectionProperties;

		int numRowsTemp;

		if (valueTokenizer.hasMoreTokens()) {
			String numRowsTempString = "";
			BaseField lpseField = collectionProperties.getLpseField(panlLpseCode);
			if(null != lpseField) {
				numRowsTempString = lpseField.getDecodedValue(valueTokenizer.nextToken());
			} else {
				this.isValid = false;
			}

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
		this.value = Integer.toString(numRowsTemp);
		this.numRows = numRowsTemp;
	}

	@Override public String getUriPathComponent() {
		if(isValid) {
			return (collectionProperties.getLpseField(lpseCode).getEncodedPanlValue(Integer.toString(numRows)) + "/");
		} else {
			return("");
		}
	}

	@Override public String getLpseComponent() {
		return(lpseCode);
	}

	@Override public String explain() {
		// TODO - suffix and prefix
		return ("PANL " +
				(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				" <rows>          LPSE code '" +
				this.lpseCode +
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
		return("numrows");
	}

	public int getNumRows() {
		return(this.numRows);
	}
}
