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

import java.util.StringTokenizer;

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
 *
 * <ul>
 *   <li>There is not always a mapping LPSE code to URI part to LPSE code</li>
 *   <li>The LPSE code may have modifiers applied to it</li>
 * </ul>
 *
 * @author synapticloop
 */
public abstract class LpseToken {
	protected String lpseCode;
	protected String originalValue;
	protected String value;
	protected boolean isValid = true;

	/**
	 * <p>Factory method for getting the correct LPSE token for a particular
	 * code.</p>
	 *
	 * <p>Any unknown tokens become a <code>FacetLpseToken</code>, whether it is
	 * valid or not.</p>
	 *
	 * @param collectionProperties The collection properties
	 * @param token The token
	 * @param query The query string (this may be blank or null)
	 * @param valueTokeniser The LPSE URI tokeniser
	 * @param lpseTokeniser The LPSE code tokeniser
	 *
	 * @return The LpseToken for this LPSE code.
	 */
	public static LpseToken getLpseToken(
			CollectionProperties collectionProperties,
			String token,
			String query,
			StringTokenizer valueTokeniser,
			LpseTokeniser lpseTokeniser) {

		if (token.equals(collectionProperties.getPanlParamQuery())) {
			// having a query on the URL always trumps whether we have a query
			// parameter in the URI path
			return (new QueryLpseToken(
					query,
					token,
					valueTokeniser));
		} else if (token.equals(collectionProperties.getPanlParamSort())) {
			return (new SortLpseToken(
					collectionProperties,
					token,
					lpseTokeniser));
		} else if (token.equals(collectionProperties.getPanlParamQueryOperand())) {
			return (new QueryOperandLpseToken(
					collectionProperties,
					token,
					lpseTokeniser));
		} else if (token.equals(collectionProperties.getPanlParamNumRows())) {
			return (new NumRowsLpseToken(
					collectionProperties,
					token,
					valueTokeniser));
		} else if (token.equals(collectionProperties.getPanlParamPage())) {
			return (new PageLpseToken(
					collectionProperties,
					token,
					valueTokeniser));

		} else if (token.equals(collectionProperties.getPanlParamPassThrough())) {
			return (new PassThroughLpseToken(
					collectionProperties,
					token,
					valueTokeniser));
		} else {
			StringBuilder facet = new StringBuilder(token);
			// it is a facet field
			while (token.length() < collectionProperties.getLpseLength()) {
				facet.append(lpseTokeniser.nextToken());
			}

			// now we have the facetField
			return (new FacetLpseToken(
					collectionProperties,
					facet.toString(),
					lpseTokeniser,
					valueTokeniser));
		}
	}

	/**
	 * <p>Instantiate a LpseToken which responds to a specific LPSE code.  This
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
		return (lpseCode);
	}

	/**
	 * <p>Get the UIR path value for this token.</p>
	 *
	 * @return The LPSE value
	 */
	public String getValue() {
		return (value);
	}

	/**
	 * <p>Return a human readable explanation of what the URI has been parsed to
	 * be.</p>
	 *
	 * <p>
	 * An example of multiple tokens and their explanations:
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
	 * @return A human-readable explanation of what this token has parsed
	 */
	public abstract String explain();

	/**
	 * <p>Return the human-readable type for this token.</p>
	 *
	 * @return The human-readable type for this token.
	 */
	public abstract String getType();

	public boolean getIsValid() {
		return (isValid);
	}


	public String getEquivalenceValue() {
		return(lpseCode + "/" + this.value);
	}

	public void setIsValid(boolean isValid) {
		this.isValid = isValid;
	}
}
