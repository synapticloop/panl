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

import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;

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
	private String queryOperand;

	public QueryOperandLpseToken(
			CollectionProperties collectionProperties,
			String lpseCode,
			LpseTokeniser lpseTokeniser) {
		super(lpseCode);
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

	public String getQOpValue() {
		if (this.queryOperand.equals("+")) {
			return(SOLR_QUERY_OPERAND_AND);
		} else {
			return(SOLR_QUERY_OPERAND_OR);
		}
	}

	@Override
	public String getType() {
		return ("sort");
	}

	/**
	 *
	 * @return
	 */
	public String getQueryOperand() {
		return (this.queryOperand);
	}

}
