package com.synapticloop.panl.server.tokeniser.token;

import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.tokeniser.PanlTokeniser;
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
public class QueryOperandLpseToken extends LpseToken {
	private String queryOperand;

	public QueryOperandLpseToken(String panlLpseCode) {
		super(panlLpseCode);
	}

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

	/**
	 * <p>The Query Operand Token does not have a URI Part</p>
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
							this.queryOperand);
		} else {
			return ("");
		}
	}

	@Override
	public String explain() {
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
		if(this.queryOperand.equals("+")) {
			return("AND");
		} else {
			return("OR");
		}
	}
	@Override
	public String getType() {
		return ("sort");
	}

	public String getQueryOperand() {
		return(this.queryOperand);
	}

}
