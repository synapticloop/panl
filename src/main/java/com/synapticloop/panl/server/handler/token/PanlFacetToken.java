package com.synapticloop.panl.server.handler.token;

import com.synapticloop.panl.server.properties.CollectionProperties;
import org.apache.solr.client.solrj.SolrQuery;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public class PanlFacetToken extends PanlToken {
	private String solrField = null;
	private boolean isValid = true;
	private CollectionProperties collectionProperties;

	/**
	 * <p>Create a new Panl Facet Token for use with generating URLs.</p>
	 *
	 * @param panlLpseCode The code to create for the lpse part of the URL
	 */
	public PanlFacetToken(String panlLpseCode) {
		super(panlLpseCode);
	}

	public PanlFacetToken(
			CollectionProperties collectionProperties,
			String panlLpseCode,
			PanlStringTokeniser lpseTokeniser,
			StringTokenizer valueTokeniser) {
		super(panlLpseCode);
		this.collectionProperties = collectionProperties;

		this.value = collectionProperties
				.getDePrefixSuffixForValue(
						panlLpseCode,
						URLDecoder.decode(
								valueTokeniser.nextToken(),
								StandardCharsets.UTF_8));


		if(collectionProperties.hasFacetCode(panlLpseCode)) {
			this.solrField = collectionProperties.getNameFromCode(panlLpseCode);
		} else {
			this.isValid = false;
		}
	}

	@Override public String getUriComponent() {
		if(isValid) {
			return (
					URLEncoder.encode(
							collectionProperties.getPrefixSuffixForValue(
									panlLpseCode,
									this.value),
							StandardCharsets.UTF_8) +
							"/");
		} else {
			return("");
		}
	}

	@Override public String getLpseComponent() {
		if(isValid) {
			return(panlLpseCode);
		} else {
			return("");
		}
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
			solrQuery.addFilterQuery(this.solrField + ":\"" + value + "\"");
		}
	}

	@Override public String getType() {
		return("facet");
	}
}
