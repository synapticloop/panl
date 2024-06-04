package com.synapticloop.panl.server.tokeniser.token;

import org.apache.solr.client.solrj.SolrQuery;

/**
 * <p>The LPSE token encapsulates the URI part and the encoding part and
 * validates whether this token is valid.</p>
 *
 * <pre>
 *   /something/else/a value/another/2/dfgtn/
 *   --------------------------------- -----
 *         This is the URI part          |
 *                                       |
 *                                       |
 *                   This is the LPSE code
 * </pre>
 *
 * <p>If this token is marked as invalid, it will not generate any URI paths or
 * of LPSE codes.</p>
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
public abstract class LpseToken {
	protected String lpseCode;
	protected String value;
	protected boolean isValid = true;

	/**
	 * <p>Instantiate a PanlToken which responds to a specific LPSE code.  This
	 * code may be updated by the child classes. as there may be different
	 * lengths of LPSE codes.</p>
	 *
	 * @param lpseCode The LPSE code to assign this facet or query operand to
	 */
	public LpseToken(String lpseCode) {
		this.lpseCode = lpseCode;
	}

	/**
	 * <p>Return the LPSE code for this token.</p>
	 *
	 * @return The LPSE code for this token
	 */
	public String getLpseCode() {
		return(lpseCode);
	}

	/**
	 * <p>Get the UIR path value for this token.</p>
	 *
	 * @return The LPSE value
	 */
	public String getValue() {
		return(value);
	}

	/**
	 * <p>Get the URI component for the LPSE encoding - this may return an empty
	 * string if there is no component to render.</p>
	 *
	 * @return The URI component
	 */
	@Deprecated
	public abstract String getUriPathComponent();

	/**
	 * <p>Return the URI Path component if this is to be reset, for example the
	 * page number if any other num per page or sort order changes.</p>
	 *
	 * @return The URI path component for a reset
	 */
	@Deprecated
	public String getResetUriPathComponent() {
		return(getUriPathComponent());
	}

	/**
	 * <p>Get the LPSE component, which may have additional operands associated
	 * with it.</p>
	 *
	 * @return The LPSE component
	 */
	@Deprecated
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
	@Deprecated
	public abstract void applyToQuery(SolrQuery solrQuery);

	/**
	 * <p>Return the human-readable type for this token.</p>
	 *
	 * @return The human-readable type for this token.
	 */
	public abstract String getType();

	public boolean getIsValid() {
		return(isValid);
	}
}
