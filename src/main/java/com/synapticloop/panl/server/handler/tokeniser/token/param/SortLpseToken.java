package com.synapticloop.panl.server.handler.tokeniser.token.param;

/*
 * Copyright (c) 2008-2024 synapticloop.
 *
 * https://github.com/synapticloop/panl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 *  IN THE SOFTWARE.
 */

import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.SolrQuery;

/**
 * <p>The sort token defines the sort order for the returned results.  By
 * default the results are already sorted by relevance DESCending.</p>
 *
 * <p>Apart from the default sort order of relevance, other sort fields are
 * defined by the <code>panl.sort.fields</code> property in the
 * <code>collection.panl.properties</code> file.</p>
 *
 * @author synapticloop
 */
public class SortLpseToken extends LpseToken {
	public static final String SORT_ORDER_URI_KEY_ASCENDING = "+";
	public static final String SORT_ORDER_URI_KEY_DESCENDING = "-";

	private final String lpseSortCode;
	private final String solrFacetField;
	private SolrQuery.ORDER sortOrder = SolrQuery.ORDER.desc;
	private String sortOrderUriKey = SORT_ORDER_URI_KEY_DESCENDING;

	public SortLpseToken(
			CollectionProperties collectionProperties,
			String lpseCode,
			LpseTokeniser lpseTokeniser) {

		super(lpseCode, collectionProperties);

		// Sort URI path will either be sorted on relevance and will be blank, or
		// it will be sorted on a facet code /sb+/ or /sb-/

		// consume all tokens until we find a + or a -
		StringBuilder sb = new StringBuilder();

		boolean hasFound = false;
		while (!hasFound && lpseTokeniser.hasMoreTokens()) {
			String sortLpseToken = lpseTokeniser.nextToken();
			switch (sortLpseToken) {
				case SORT_ORDER_URI_KEY_ASCENDING:
					this.sortOrderUriKey = SORT_ORDER_URI_KEY_ASCENDING;
					this.sortOrder = SolrQuery.ORDER.asc;
					hasFound = true;
					break;
				case SORT_ORDER_URI_KEY_DESCENDING:
					this.sortOrderUriKey = SORT_ORDER_URI_KEY_DESCENDING;
					this.sortOrder = SolrQuery.ORDER.desc;
					hasFound = true;
					break;
				default:
					sb.append(sortLpseToken);
			}
		}


		// at this point, the string builder will either be length 0 - i.e. which
		// means that they haven't passed through a facet field LPSE code to
		// order by

		this.lpseSortCode = sb.toString();

		if (sb.length() == 0) {
			this.isValid = false;
		} else {
			if (!collectionProperties.hasSortField(this.lpseSortCode)) {
				this.isValid = false;
			}
		}

		if(!hasFound) {
			this.isValid = false;
		}

		this.solrFacetField = collectionProperties.getSolrFieldNameFromLpseCode(this.lpseSortCode);
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
		// TODO - there shouldn't be a relevance
		return ("PANL " +
				(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				" <sort>          LPSE code '" +
				this.lpseCode +
				"' sort code '" +
				this.lpseSortCode +
				"' (solr field '" +
				(this.solrFacetField == null ? " defaulting to <relevance>" : this.solrFacetField) +
				"'), sorted " +
				(this.sortOrder == SolrQuery.ORDER.asc ? "ASCending" : "DESCending"));
	}

	@Override public String getType() {
		return ("sort");
	}

	/**
	 * <p>Get the LPSE code for the sorting order</p>
	 *
	 * @return The LPSE code for the sorting order
	 */
	public String getLpseSortCode() {
		return (lpseSortCode);
	}

	public String getSortOrderUriKey() {
		return (sortOrderUriKey);
	}

	public String getInverseSortOrderUriKey() {
		if (sortOrderUriKey.equals("+")) {
			return ("-");
		}
		return ("+");
	}

	public String getSolrFacetField() {
		return solrFacetField;
	}

	public SolrQuery.ORDER getSortOrder() {
		return sortOrder;
	}

	public String getEquivalenceValue() {
		return(lpseCode + "/" + this.lpseSortCode);
	}

}
