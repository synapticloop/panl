package com.synapticloop.panl.server.handler.tokeniser.token.param;

/*
 * Copyright (c) 2008-2025 synapticloop.
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
 * IN THE SOFTWARE.
 */

import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;

/**
 * <p>The query operand </p>
 *
 * <p>There may be multiple sort tokens, and they are add the the solr query
 * in order of appearance.</p>
 *
 * @author synapticloop
 */
public class QueryOperandLpseToken extends LpseToken {
	public static final String SOLR_QUERY_OPERAND_AND = "AND";
	public static final String SOLR_QUERY_OPERAND_OR = "OR";
	public static final String QUERY_OPERAND_URI_KEY_AND = "+";
	public static final String QUERY_OPERAND_URI_KEY_OR = "-";

	private final String queryOperand;

	/**
	 * <p>Instantiate a query operand token</p>
	 *
	 * @param collectionProperties The collection properties
	 * @param lpseCode The LPSE code that this query operand is bound to
	 * @param lpseTokeniser The LPSE tokeniser
	 */
	public QueryOperandLpseToken(
			CollectionProperties collectionProperties,
			String lpseCode,
			LpseTokeniser lpseTokeniser) {
		super(lpseCode, collectionProperties);

		if (lpseTokeniser.hasMoreTokens()) {
			queryOperand = lpseTokeniser.nextToken();
			if (!(queryOperand.equals(QUERY_OPERAND_URI_KEY_AND) || queryOperand.equals(QUERY_OPERAND_URI_KEY_OR))) {
				isValid = false;
			}
		} else {
			this.queryOperand = collectionProperties.getDefaultQueryOperand();
			isValid = false;
		}

		if(isValid) {
			this.value = this.queryOperand;
		}
	}

	/**
	 * <p>Explain this token in human-readable format (used for debugging and
	 * informational purposes through the Panl Viewer Explainer web app.</p>
	 *
	 * @return The human-readable explanation
	 */
	@Override public String explain() {
		return ("PANL " +
					(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				" <query_operand>      LPSE code '" +
				this.lpseCode +
				"' operand '" +
				this.queryOperand +
				"' (q.op=" + getQOpValue() + ")");
	}

	/**
	 * <p>Return the Solr <code>q.op</code> query parameter, which will be one
	 * of:</p>
	 *
	 * <ul>
	 *   <li><code>AND</code>, or</li>
	 *   <li><code>OR</code></li>
	 * </ul>
	 *
	 * @return The Solr <code>q.op</code> query parameter
	 */
	public String getQOpValue() {
		if (this.queryOperand.equals(QUERY_OPERAND_URI_KEY_AND)) {
			return(SOLR_QUERY_OPERAND_AND);
		} else {
			return(SOLR_QUERY_OPERAND_OR);
		}
	}

	@Override public String getType() {
		return ("query_operand");
	}

	/**
	 * <p>Return the LPSE query operand - this may only be one of two values,
	 * namely: a '+' or '-' (without the quotes).</p>
	 *
	 * @return The LPSE query operand
	 */
	public String getLpseQueryOperand() {
		return (this.queryOperand);
	}

	/**
	 * <p>Return the equivalence value for this token, which will always be
	 * <code>&lt;lpse_code&gt;/</code> as you may ony ever have one query operand
	 * per LPSE URI path part.</p>
	 *
	 * @return The equivalence values
	 */
	@Override public String getEquivalenceValue() {
		return(this.getLpseCode() + "/");
	}
}
