package com.synapticloop.panl.server.tokeniser.token;

import com.synapticloop.panl.server.tokeniser.PanlTokeniser;
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
public class SortLpseToken extends LpseToken {
	private String panlFacetCode;
	private String solrFacetField;
	private SolrQuery.ORDER sortOrder = SolrQuery.ORDER.desc;
	private String sortCode = "-";

	public SortLpseToken(String panlLpseCode) {
		super(panlLpseCode);
	}

	public SortLpseToken(
					CollectionProperties collectionProperties,
					String panlLpseCode,
					PanlTokeniser lpseTokeniser) {

		super(panlLpseCode);

		// Sort URI path will either be sorted on relevance and will look
		// like /s-/ or /s+/
		// or will be sorted on a facet code /sb+/ or /sb-/

		// consume all tokens until we find a + or a -
		StringBuilder sb = new StringBuilder();

		boolean hasFound = false;
		while (!hasFound && lpseTokeniser.hasMoreTokens()) {
			String sortLpseToken = lpseTokeniser.nextToken();
			switch (sortLpseToken) {
				case "+":
					this.sortCode = "+";
					this.sortOrder = SolrQuery.ORDER.asc;
					hasFound = true;
					break;
				case "-":
					this.sortCode = "-";
					this.sortOrder = SolrQuery.ORDER.desc;
					hasFound = true;
					break;
				default:
					sb.append(sortLpseToken);
			}
		}

		// at this point, the string builder will either be length 0 - i.e. this
		// is a relevance search, or will be the facet field.

		this.panlFacetCode = sb.toString();
		if (!collectionProperties.hasSortField(this.panlFacetCode)) {
			this.isValid = false;
		}

		this.solrFacetField = collectionProperties.getSolrFieldNameFromPanlLpseCode(this.panlFacetCode);
	}

	/**
	 * <p>The Sort Order Panl Token does not have a URI Part</p>
	 *
	 * @return ALWAYS returns an empty string
	 */
	@Override
	public String getUriPathComponent() {
		return ("");
	}

	/**
	 * <p>Get the LPSE component.  This will only return a value if there is a
	 * valid sort field available.</p>
	 *
	 * @return The LPSE component, or an empty string if not valid
	 */
	@Override
	public String getLpseComponent() {
		if (isValid) {
			return (this.lpseCode +
							this.panlFacetCode +
							(this.sortOrder.equals(SolrQuery.ORDER.asc) ? "+" : "-"));

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
	@Override
	public String explain() {
		return ("PANL " +
						(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
						" <sort>          LPSE code '" +
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
	@Override
	public void applyToQuery(SolrQuery solrQuery) {
		if (isValid) {
			solrQuery.addSort(this.solrFacetField, sortOrder);
		}
	}

	@Override
	public String getType() {
		return ("sort");
	}

	public String getPanlFacetCode() {
		return(panlFacetCode);
	}

	public String getSortCode() {
		return(sortCode);
	}
}
