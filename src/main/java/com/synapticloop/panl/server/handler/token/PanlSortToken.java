package com.synapticloop.panl.server.handler.token;

import com.synapticloop.panl.server.properties.CollectionProperties;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.List;
import java.util.StringTokenizer;

/**
 * <p>The sort token defines the sort order for the returned results.</p>
 *
 * <p>There may be multiple sort tokens, and they are add the the solr query
 * in order of appearance.</p>
 *
 * @author synapticloop
 */
public class PanlSortToken extends PanlToken {
	private final String panlFacetCode;
	private final String solrFacetField;
	private final SolrQuery.ORDER sortOrder;
	private boolean isValid = true;

	public PanlSortToken(
			CollectionProperties collectionProperties,
			String panlLpseCode,
			PanlStringTokeniser lpseTokeniser) {

		super(panlLpseCode);

		// at this point - we are going to sort by the facetField
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (i < collectionProperties.getPanlLpseNum()) {
			if (lpseTokeniser.hasMoreTokens()) {
				sb.append(lpseTokeniser.nextToken());
			}
			i++;
		}

		this.panlFacetCode = sb.toString();
		if (!collectionProperties.hasSortField(this.panlFacetCode)) {
			this.isValid = false;
		}

		this.solrFacetField = collectionProperties.getNameFromCode(this.panlFacetCode);


		// lastly - we have a sort order (either ascending, or descending) - although
		// this may not exist
		if (lpseTokeniser.hasMoreTokens()) {
			String sortCode = lpseTokeniser.nextToken();
			switch (sortCode) {
				case "+":
					this.sortOrder = SolrQuery.ORDER.asc;
					break;
				case "-":
					this.sortOrder = SolrQuery.ORDER.desc;
					break;
				default:
					this.sortOrder = collectionProperties.getDefaultOrder();
					lpseTokeniser.decrementCurrentPosition();
			}
		} else {
			this.sortOrder = collectionProperties.getDefaultOrder();
		}
	}

	/**
	 * <p>The Sort Order Panl Token does not have a Uri Part</p>
	 *
	 * @return ALWAYS returns an empty string
	 */
	@Override public String getUriComponent() {
		return ("");
	}

	/**
	 * <p>Get the lpse component.  This will only return a value if there is a
	 * valid sort field available.</p>
	 *
	 * @return The LPSE component, or an empty string if not valid
	 */
	@Override public String getLpseComponent() {
		if (isValid) {
			return (this.panlLpseCode +
					this.panlFacetCode +
					this.sortOrder);
		} else {
			return ("");
		}
	}

	@Override public String explain() {
		return ("PANL " +
				(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				" <sort>  LPSE code '" +
				this.panlFacetCode +
				"' (solr field '" +
				this.solrFacetField +
				"'), sorted " +
				(this.sortOrder == SolrQuery.ORDER.asc ? "ASCending" : "DESCending"));
	}

	@Override public void applyToQuery(SolrQuery solrQuery) {
		if (isValid) {
			solrQuery.addSort(this.solrFacetField, sortOrder);
		}
	}
}
