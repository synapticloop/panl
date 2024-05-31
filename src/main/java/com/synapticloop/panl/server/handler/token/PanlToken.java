package com.synapticloop.panl.server.handler.token;

import org.apache.solr.client.solrj.SolrQuery;

/**
 * <p>The LPSE token encapsulates the URI part and the encoding part.</p>
 * <pre>
 *   /something/else/a value/another/2/dfgtn/
 *   --------------------------------- -----
 *         This is the URI part          |
 *                                       |
 *                                       |
 *                   This is the LPSE code
 * </pre>
 *
 * <p>For the above, the URI part would be mapped to individual LPSE codes
 * thusly:</p>
 *
 * <ul>
 *   <li><code>something</code> maps to <code>d</code></li>
 *   <li><code>else</code> maps to <code>f</code></li>
 *   <li><code>a value</code> maps to <code>g</code></li>
 *   <li><code>another</code> maps to <code>t</code></li>
 *   <li><code>2</code> maps to <code>n</code></li>
 * </ul>
 *
 * <p>Additionally</p>

 * <ul>
 *   <li>There is not always a mapping LPSE code to URI part to LPSE code</li>
 *   <li>The LPSE code may have modifiers applied to it</li>
 * </ul>
 *
 * @author synapticloop
 */
public abstract class PanlToken {
	protected final String panlLpseCode;
	protected String value;

	/**
	 * <p>Instantiate a PanlToken which responds to a specific LPSE code.</p>
	 *
	 * @param panlLpseCode The LPSE code to assign this facet or query operand to
	 */
	public PanlToken(String panlLpseCode) {
		this.panlLpseCode = panlLpseCode;
	}

	/**
	 * <p>Return the LPSE code for this token.</p>
	 *
	 * @return The LPSE code for this token
	 */
	public String getPanlLpseCode() {
		return(panlLpseCode);
	}

	/**
	 * <p>Get the LPSE value for this code.  The value comes from the URI.</p>
	 *
	 * @return The LPSE value
	 */
	public String getPanlLpseValue() {
		return(value);
	}

	/**
	 * <p>Get the URI component for the LPSE encoding - this may return an empty
	 * string if there is no component to render.</p>
	 *
	 * @return The URI component
	 */
	public abstract String getUriComponent();

	public String getResetUriComponent() {
		return(getUriComponent());
	}

	/**
	 * <p>Get the LPSE component, which may have additional operands associated
	 * with it.</p>
	 *
	 * @return The LPSE component
	 */
	public abstract String getLpseComponent();

	/**
	 * <p>Return a human readable explanation of what the URI has been parsed to
	 * be.</p>
	 *
	 * <p>
	 *   An example of multiple tokens and their explanations:
	 * </p>
	 *
	 * <pre>
	 *  PANL [  VALID  ] &lt;facet&gt; LPSE code 'm' (solr field 'manu') with value 'Belkin'.
	 *  PANL [  VALID  ] &lt;facet&gt; LPSE code 'c' (solr field 'cat') with value 'connector'.
	 *  PANL [  VALID  ] &lt;query&gt; LPSE code 'q' with value 'solr'.
	 *  PANL [  VALID  ] &lt;rows&gt;  LPSE code 'n' using parsed value of '2'.
	 *  PANL [  VALID  ] &lt;sort&gt;  LPSE code 'm' (solr field 'manu'), sorted ASCending
	 * </pre>
	 *
	 *
	 * @return A human-readable explanation of what this token has parsed
	 */
	public abstract String explain();

	/**
	 * <p>Apply this token to the solr query.  Depending on the type of the token
	 * this will alter the query in different ways.</p>
	 *
	 * @param solrQuery The Solr Query to apply the token to
	 */
	public abstract void applyToQuery(SolrQuery solrQuery);

	public abstract String getType();
}
