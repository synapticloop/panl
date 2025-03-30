package com.synapticloop.panl.server.handler.tokeniser.token;

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

import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <p>The LPSE token encapsulates the URI part and the encoding part and
 * validates whether this token is valid.</p>
 *
 * <pre>
 *   /something/else/a+value/another/2/dfgtn/
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
 *   <li><code>a+value</code> maps to <code>g</code></li>
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
	/**
	 * <p>The LPSE code that was found in the last path encoding</p>
	 */
	protected String lpseCode;
	/**
	 * <p>The original value </p>
	 */
	protected String originalValue;
	/**
	 * <p>The parsed value</p>
	 */
	protected String value;
	/**
	 * <p>Whether this token is valid</p>
	 */
	protected boolean isValid = true;
	/**
	 * <p>The collection properties for lookup</p>
	 */
	protected CollectionProperties collectionProperties;

	/**
	 * <p>Factory method for getting the correct LPSE token for a particular
	 * code.</p>
	 *
	 * <p>Any unknown tokens become a <code>FacetLpseToken</code>, whether it is
	 * valid or not.</p>
	 *
	 * @param collectionProperties The collection properties
	 * @param lpseCode The lpseCode
	 * @param query The query string (this may be blank or null)
	 * @param valueTokeniser The LPSE URI tokeniser
	 * @param lpseTokeniser The LPSE code tokeniser
	 *
	 * @return The list of LpseTokens for this LPSE code.
	 */
	public static List<LpseToken> getLpseTokens(
			CollectionProperties collectionProperties,
			String lpseCode,
			String query,
			StringTokenizer valueTokeniser,
			LpseTokeniser lpseTokeniser) {


		BaseField lpseField = collectionProperties.getLpseField(lpseCode);
		if (null == lpseField) {
			// it may be that it is more than a single code
			StringBuilder lpseCodeBuilder = new StringBuilder(lpseCode);
			// it is a lpseCodeBuilder field - unlike parameters and operands, the token
			// must be the length LPSE length
			while (lpseCodeBuilder.length() < collectionProperties.getLpseLength()) {
				if (lpseTokeniser.hasMoreTokens()) {
					lpseCodeBuilder.append(lpseTokeniser.nextToken());
				} else {
					break;
				}
			}

			lpseField = collectionProperties.getLpseField(lpseCodeBuilder.toString());
			if (null == lpseField) {
				// still null
				return(List.of(new FacetLpseToken(
						collectionProperties,
						lpseCodeBuilder.toString(),
						lpseTokeniser,
						valueTokeniser)));

			}
		}

		return (lpseField.instantiateTokens(collectionProperties, lpseCode, query, valueTokeniser, lpseTokeniser));
	}

	/**
	 * <p>Instantiate a LpseToken which responds to a specific LPSE code.  This
	 * code may be updated by the child classes. as there may be different
	 * lengths of LPSE codes.</p>
	 *
	 * @param lpseCode The LPSE code to assign this facet or query operand to
	 * @param collectionProperties The collection properties
	 */
	public LpseToken(String lpseCode, CollectionProperties collectionProperties) {
		this.lpseCode = lpseCode;
		this.collectionProperties = collectionProperties;
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
	 * <p>Get the value for this token.</p>
	 *
	 * @return The LPSE value
	 */
	public String getValue() {
		return (value);
	}

	/**
	 * <p>Return a human-readable explanation of what the URI has been parsed to
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
	 * <p>Return the human-readable type for this token.  This type is also used
	 * for the active filter json keys in the response JSON object.</p>
	 *
	 * @return The human-readable type for this token.
	 */
	public abstract String getType();

	/**
	 * <p>Return whether this token is valid - i.e. it passed all of the parsing
	 * and validation.  Valid tokens will be applied to the Solr query, invalid
	 * tokens will be silently ignored.</p>
	 *
	 * @return Whether this is a valid incoming token
	 */
	public boolean getIsValid() {
		return (isValid);
	}


	/**
	 * <p>Return the equivalence value for this token - this is used to
	 * de-duplicate incoming tokens so that the same token will not be applied to
	 * the Solr query more than once.</p>
	 *
	 * <p>The equivalence token is generally of the form
	 * <code>&lt;lpse_code&gt;/&lt;value&gt;</code>.  This differs for some
	 * tokens:</p>
	 *
	 * <ul>
	 *   <li>Sort Tokens - the order of the sort (+/-) is ignored</li>
	 *   <li>Operand Tokens - only one operand is allowed.</li>
	 *   <li>Page Number Tokens - the page number is ignored.</li>
	 *   <li>Number Per Page Tokens - The number per page value is ignored.</li>
	 *   <li>BOOLEAN Facet Tokens - you may only have one boolean value and the
	 *       value is ignored.</li>
	 * </ul>
	 *
	 * <p>In the above cases, the equivalence value will be:
	 * <code>&lt;lpse_code&gt;/</code>.  You will need to reference the over-riding
	 * methods in the subclasses to see the return value.</p>
	 *
	 * @return The equivalence token
	 */
	public String getEquivalenceValue() {
		if(getCanHaveMultiple()) {
			return (lpseCode + "/" + this.value);
		} else {
			// as there cannot be multiple, the equivalence value is just the lpse
			// code with a forward slash
			return(lpseCode + "/");
		}
	}


	/**
	 * <p>Set whether this token is valid.  If the token is valid then it will be
	 * passed through to the SOlr server.</p>
	 *
	 * @param isValid Whether this token is valid
	 */
	public void setIsValid(boolean isValid) {
		this.isValid = isValid;
	}

	/**
	 * <p>Get the original value that came through in the URI path</p>
	 *
	 * @return The original value
	 */
	public String getOriginalURIValue() {
		return (this.originalValue);
	}

	/**
	 * <p>Return whether there can be multiple tokens for this request, in
	 * general you may only have one token per request, however facets can have
	 * multiple.</p>
	 *
	 * @return Whether there can be multiple tokens for this URI
	 */
	public boolean getCanHaveMultiple() {
		return (false);
	}
}
