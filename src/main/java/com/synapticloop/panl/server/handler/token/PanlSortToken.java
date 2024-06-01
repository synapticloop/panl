package com.synapticloop.panl.server.handler.token;

import com.synapticloop.panl.server.properties.CollectionProperties;
import org.apache.solr.client.solrj.SolrQuery;

/**
 * <p>The sort token defines the sort order for the returned results.  By
 * default the results are already sorted in relevance order in descending
 * order</p>
 *
 * <p>There may be multiple sort tokens, and they are add the the solr query
 * in order of appearance.</p>
 *
 * @author synapticloop
 */
public class PanlSortToken extends PanlToken {
	private String panlFacetCode;
	private String solrFacetField;
	private SolrQuery.ORDER sortOrder;
	private boolean isValid = true;

	public PanlSortToken(String panlLpseCode) {
		super(panlLpseCode);
	}

	public PanlSortToken(
			CollectionProperties collectionProperties,
			String panlLpseCode,
			PanlStringTokeniser lpseTokeniser) {

		super(panlLpseCode);

		// this is going to be either a +, -, or a facet field
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
					// this is a facet field, so collect all characters after this
					this.sortOrder = collectionProperties.getDefaultOrder();
					StringBuilder sb = new StringBuilder(sortCode);
					int i = 1;
					while (i < collectionProperties.getPanlLpseNum()) {
						if (lpseTokeniser.hasMoreTokens()) {
							sb.append(lpseTokeniser.nextToken());
						}
						i++;
					}
					// at this point we should have a +, or a -
					lpseTokeniser.decrementCurrentPosition();
			}
		} else {
			this.sortOrder = collectionProperties.getDefaultOrder();
		}

		// at this point - we are going to sort by the facetField
		StringBuilder sb = new StringBuilder(panlLpseCode);
		int i = 1;
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
	}

	/**
	 * <p>The Sort Order Panl Token does not have a URI Part</p>
	 *
	 * @return ALWAYS returns an empty string
	 */
	@Override public String getUriComponent() {
		return ("");
	}

	/**
	 * <p>Get the LPSE component.  This will only return a value if there is a
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

	/**
	 * <p>This will return a human readable string of the format:</p>
	 *
	 * <pre>
	 *   PANL [  VALID  ] &lt;sort&gt;  LPSE code 'm' (solr field 'manu'), sorted ASCending
	 * </pre>
	 *
	 * <p>or, for an invalid LPSE code:</p>
	 *
	 * <pre>
	 *   PANL [ INVALID ] &lt;sort&gt;  LPSE code 'I' (solr field 'null'), sorted ASCending
	 * </pre>
	 *
	 * @return The human-readable explanation
	 */
	@Override public String explain() {
		return ("PANL " +
				(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				" <sort>        LPSE code '" +
				this.panlFacetCode +
				"' (solr field '" +
				this.solrFacetField +
				"'), sorted " +
				(this.sortOrder == SolrQuery.ORDER.asc ? "ASCending" : "DESCending"));
	}

	/**
	 * <p>If the token is valid (i.e. it is a valid solr field to sort on), add
	 * a sort order to the solrQuery.</p>
	 *
	 * <p><code>solrQuery.addSort(String field, ORDER order)</code></p>
	 *
	 * <p>If the token is invalid - nothing is performed.</p>
	 *
	 * @param solrQuery The Solr Query to apply the sort order to
	 */
	@Override public void applyToQuery(SolrQuery solrQuery) {
		if (isValid) {
			solrQuery.addSort(this.solrFacetField, sortOrder);
		}
	}
	@Override public String getType() {
		return("sort");
	}

}
