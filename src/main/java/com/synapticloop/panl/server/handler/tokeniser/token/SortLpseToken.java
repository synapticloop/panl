package com.synapticloop.panl.server.handler.tokeniser.token;

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
	public static final String SORT_CODE_ASCENDING = "+";
	public static final String SORT_CODE_DESCENDING = "-";

	private final String panlFacetCode;
	private final String solrFacetField;
	private SolrQuery.ORDER sortOrder = SolrQuery.ORDER.desc;
	private String sortCode = SORT_CODE_DESCENDING;

	public SortLpseToken(
			CollectionProperties collectionProperties,
			String lpseCode,
			LpseTokeniser lpseTokeniser) {

		super(lpseCode);

		// Sort URI path will either be sorted on relevance and will look
		// like /s-/ or /s+/
		// or will be sorted on a facet code /sb+/ or /sb-/

		// consume all tokens until we find a + or a -
		StringBuilder sb = new StringBuilder();

		boolean hasFound = false;
		while (!hasFound && lpseTokeniser.hasMoreTokens()) {
			String sortLpseToken = lpseTokeniser.nextToken();
			switch (sortLpseToken) {
				case SORT_CODE_ASCENDING:
					this.sortCode = SORT_CODE_ASCENDING;
					this.sortOrder = SolrQuery.ORDER.asc;
					hasFound = true;
					break;
				case SORT_CODE_DESCENDING:
					this.sortCode = SORT_CODE_DESCENDING;
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

		if (sb.length() == 0) {
			// this is a relevance search
		} else {
			if (!collectionProperties.hasSortField(this.panlFacetCode)) {
				this.isValid = false;
			}
		}

		this.solrFacetField = collectionProperties.getSolrFieldNameFromLpseCode(this.panlFacetCode);
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
				this.lpseCode +
				"' sort code '" +
				this.panlFacetCode +
				"' (solr field '" +
				(this.solrFacetField == null ? "<relevance>" : this.solrFacetField) +
				"'), sorted " +
				(this.sortOrder == SolrQuery.ORDER.asc ? "ASCending" : "DESCending"));
	}

	@Override
	public String getType() {
		return ("sort");
	}

	public String getPanlFacetCode() {
		return (panlFacetCode);
	}

	public String getSortCode() {
		return (sortCode);
	}

	public String getSolrFacetField() {
		return solrFacetField;
	}

	public SolrQuery.ORDER getSortOrder() {
		return sortOrder;
	}
}
