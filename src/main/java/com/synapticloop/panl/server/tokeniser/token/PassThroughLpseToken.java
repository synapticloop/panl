package com.synapticloop.panl.server.tokeniser.token;

import com.synapticloop.panl.server.properties.CollectionProperties;
import org.apache.solr.client.solrj.SolrQuery;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public class PassThroughLpseToken extends LpseToken {
	private CollectionProperties collectionProperties;

	/**
	 * <p>Create a new Panl Passthrough Token for use with generating URI paths.</p>
	 *
	 * @param panlLpseCode The code to create for the lpse part of the URL
	 */
	public PassThroughLpseToken(String panlLpseCode) {
		super(panlLpseCode);
	}

	public PassThroughLpseToken(
			CollectionProperties collectionProperties,
			String panlLpseCode,
			StringTokenizer valueTokeniser) {
		super(panlLpseCode);

		this.collectionProperties = collectionProperties;

		this.value = URLDecoder.decode(
				valueTokeniser.nextToken(),
				StandardCharsets.UTF_8);
	}

	@Override public String getUriPathComponent() {
		if(isValid) {
			return (collectionProperties.getLpseField(lpseCode).getEncodedPanlValue(this.value) + "/");
		} else {
			return("");
		}
	}

	@Override public String getLpseComponent() {
		return (lpseCode);
	}

	@Override public String explain() {
		return ("PANL [  VALID  ] <passthrough>   LPSE code '" +
				this.lpseCode +
				"' with value '" +
				value +
				"'.");
	}

	@Override public void applyToQuery(SolrQuery solrQuery) {
		// do nothing
	}

	@Override public String getType() {
		return ("passthrough");
	}
}
