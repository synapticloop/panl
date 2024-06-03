package com.synapticloop.panl.server.tokeniser.token;

import com.synapticloop.panl.server.properties.CollectionProperties;
import org.apache.solr.client.solrj.SolrQuery;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public class PageLpseToken extends LpseToken {
	private int pageNum = 0;
	private CollectionProperties collectionProperties;

	public PageLpseToken(String panlLpseCode) {
		super(panlLpseCode);
	}

	public PageLpseToken(CollectionProperties collectionProperties, String panlLpseCode, StringTokenizer valueTokenizer) {
		super(panlLpseCode);
		this.collectionProperties = collectionProperties;

		int pageNumTemp;

		if (valueTokenizer.hasMoreTokens()) {
			// might have a prefix - or suffix
			String pageNumTempString = collectionProperties
					.getConvertedFromPanlValue(
							panlLpseCode,
							URLDecoder.decode(
									valueTokenizer.nextToken(),
									StandardCharsets.UTF_8));

			try {
				pageNumTemp = Integer.parseInt(pageNumTempString);
			} catch (NumberFormatException e) {
				isValid = false;
				pageNumTemp = 1;
			}
		} else {
			isValid = false;
			pageNumTemp = 1;
		}

		if (pageNumTemp <= 0) {
			pageNumTemp = 1;
		}

		this.pageNum = pageNumTemp;
	}

	@Override public String getUriPathComponent() {
		return (getURIComponentForPageNumber(this.pageNum));
	}

	private String getURIComponentForPageNumber(int pageNum) {
		return (
				URLEncoder.encode(
						collectionProperties.getConvertedToPanlValue(
								lpseCode,
								Integer.toString(pageNum)),
						StandardCharsets.UTF_8) +
						"/");
	}
	public String getResetUriPathComponent() {
		return (getURIComponentForPageNumber(1));
	}

	@Override public String getLpseComponent() {
		return (lpseCode);
	}

	@Override public String explain() {
		return ("PANL " +
				(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				" <page>          LPSE code '" +
				this.lpseCode +
				"' using " +
				(this.isValid ? "parsed" : "default") +
				" value of '" +
				this.pageNum +
				"'.");
	}

	/**
	 * <p>This is not applied to the solr query as it also relies on the number
	 * of results per page.</p>
	 *
	 * @param solrQuery The Solr Query to apply the token to
	 */
	@Override public void applyToQuery(SolrQuery solrQuery) {
	}

	@Override public String getType() {
		return ("page");
	}

	public int getPageNum() {
		return (this.pageNum);
	}
}
