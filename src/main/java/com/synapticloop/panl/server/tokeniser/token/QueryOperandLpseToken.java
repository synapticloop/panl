package com.synapticloop.panl.server.tokeniser.token;

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

import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.tokeniser.PanlTokeniser;
import org.apache.solr.client.solrj.SolrQuery;

/**
 * <p>The query operand </p>
 *
 * <p>There may be multiple sort tokens, and they are add the the solr query
 * in order of appearance.</p>
 *
 * @author synapticloop
 */
public class QueryOperandLpseToken extends LpseToken {
	private String queryOperand;

	public QueryOperandLpseToken(
			CollectionProperties collectionProperties,
			String panlLpseCode,
			PanlTokeniser lpseTokeniser) {
		super(panlLpseCode);
		if (lpseTokeniser.hasMoreTokens()) {
			queryOperand = lpseTokeniser.nextToken();
			if (!(queryOperand.equals("+") || queryOperand.equals("-"))) {
				isValid = false;
			}
		}
	}

	@Override public String explain() {
		return ("PANL " +
				(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				" <query_operand> LPSE code '" +
				this.lpseCode +
				"' operand '" +
				this.queryOperand +
				"' (q.op=" + getQOpValue() + ")");
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
			solrQuery.setParam("q.op", getQOpValue());
		}
	}

	public String getQOpValue() {
		if (this.queryOperand.equals("+")) {
			return ("AND");
		} else {
			return ("OR");
		}
	}

	@Override
	public String getType() {
		return ("sort");
	}

	public String getQueryOperand() {
		return (this.queryOperand);
	}

}
