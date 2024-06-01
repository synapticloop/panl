package com.synapticloop.panl.server.handler.token;

import com.synapticloop.panl.server.properties.CollectionProperties;
import org.apache.solr.client.solrj.SolrQuery;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public class PassthroughLpseToken extends LpseToken {
	private CollectionProperties collectionProperties;

	/**
	 * <p>Create a new Panl Passthrough Token for use with generating URI paths.</p>
	 *
	 * @param panlLpseCode The code to create for the lpse part of the URL
	 */
	public PassthroughLpseToken(String panlLpseCode) {
		super(panlLpseCode);
	}

	public PassthroughLpseToken(
			CollectionProperties collectionProperties,
			String panlLpseCode,
			StringTokenizer valueTokeniser) {
		super(panlLpseCode);

		this.collectionProperties = collectionProperties;

		this.value = URLDecoder.decode(
				valueTokeniser.nextToken(),
				StandardCharsets.UTF_8);
	}

	@Override public String getUriComponent() {
		return (
				URLEncoder.encode(
						collectionProperties.getConvertedToPanlValue(
								panlLpseCode,
								this.value),
						StandardCharsets.UTF_8) +
						"/");
	}

	@Override public String getLpseComponent() {
		return (panlLpseCode);
	}

	@Override public String explain() {
		return ("PANL [  VALID  ] <passthrough> LPSE code '" +
				this.panlLpseCode +
				"' with value '" +
				value +
				"'.");
	}

	@Override public void applyToQuery(SolrQuery solrQuery) {
		// do nothing
	}

	@Override public String getType() {
		return ("Pass-through LPSE code");
	}
}
