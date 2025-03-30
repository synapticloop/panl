package com.synapticloop.panl.server.handler.tokeniser.token.facet;

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

import com.synapticloop.panl.server.handler.fielderiser.field.facet.PanlBooleanFacetField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.util.Constants;

import java.util.StringTokenizer;

/**
 * <p>As the name would suggest, BOOLEAN facet can have only one of two values</p>
 *
 * <ul>
 *   <li><code>true</code>, or</li>
 *   <li><code>false</code></li>
 * </ul>
 *
 * <p>BOOLEAN facets can have a prefix, a suffix, neither, or both, as well as a
 * true/false value replacement.</p>
 *
 * @author syanapticloop
 */
public class BooleanFacetLpseToken extends LpseToken {
	public static final String TOKEN_TYPE = "facet";

	/**
	 * <p>Instantiate a BOOLEAN Facet LPSE Token.</p>
	 *
	 * @param collectionProperties The Collection that this belongs to
	 * @param lpseCode The LPSE code
	 * @param lpseTokeniser The LPSE tokeniser
	 * @param valueTokeniser The value tokeniser
	 */
	public BooleanFacetLpseToken(
			CollectionProperties collectionProperties,
			String lpseCode,
			LpseTokeniser lpseTokeniser,
			StringTokenizer valueTokeniser) {

		super(lpseCode, collectionProperties);

		StringBuilder sb = new StringBuilder(lpseCode);
		int i = sb.length();
		while (i < collectionProperties.getLpseLength()) {
			if (lpseTokeniser.hasMoreTokens()) {
				sb.append(lpseTokeniser.nextToken());
			}
			i++;
		}

		this.lpseCode = sb.toString();

		if(!valueTokeniser.hasMoreTokens()) {
			this.isValid = false;
			return;
		}

		// at this point we are good to attempt to validate
		this.originalValue = valueTokeniser.nextToken();

		if (collectionProperties.hasFacetCode(lpseCode)) {
			this.solrField = collectionProperties.getSolrFieldNameFromLpseCode(lpseCode);

			PanlBooleanFacetField lpseField = (PanlBooleanFacetField) collectionProperties.getLpseField(this.lpseCode);

			this.value = lpseField.getDecodedValue(this.originalValue);

			if(null == this.value) {
				this.isValid = false;
			}
		} else {
			this.isValid = false;
		}
	}

	@Override public String explain() {
		return ("PANL " +
				(this.isValid ? "[  VALID  ]" : "[ INVALID ]") +
				" <facet (BOOLEAN)>    LPSE code '" +
				this.lpseCode +
				"' (solr field '" +
				this.solrField +
				"') with parsed value '" +
				value +
				"', incoming value '" +
				this.originalValue +
				"'.");
	}


	/**
	 * <p>Return the inverse BOOLEAN value for the token, which will be encoded
	 * if there is a true/false/value replacement.</p>
	 *
	 * @return The inverse (and encoded) BOOLEAN value.
	 */
	public String getInverseBooleanValue() {
		PanlBooleanFacetField lpseField = (PanlBooleanFacetField) collectionProperties.getLpseField(this.lpseCode);
		if(value.equals(Constants.BOOLEAN_TRUE_VALUE)) {
			return(lpseField.getEncodedPanlValue(Constants.BOOLEAN_FALSE_VALUE));
		} else {
			return(lpseField.getEncodedPanlValue(Constants.BOOLEAN_TRUE_VALUE));
		}

	}

	@Override public String getType() {
		return TOKEN_TYPE;
	}

	/**
	 * <p>Return the Solr field name for this token</p>
	 *
	 * @return The Solr field name for this token.
	 */
	public String getSolrField() {
		return solrField;
	}

	/**
	 * <p>A BOOLEAN facet cannot have multiple, it will either be a true or false
	 * value, however, physically you can have more than one value passed through,
	 * it is the further values that will be marked as invalid.</p>
	 *
	 * <p>I.e. logically, you cannot have two BOOLEAN values passed through,
	 * in reality you could pass through as many as you want.</p>
	 *
	 * @return ALWAYS 'true', although any further values will be ignored by Panl
	 *    and marked as 'INVALID'
	 */
	@Override public boolean getCanHaveMultiple() {
		return (true);
	}

	/**
	 * <p>Get the equivalence value, which will only return the LPSE code with a
	 * forward slash, as it doesn't matter whether it is true or false, you may
	 * only have one LPSE token value for this token.</p>
	 *
	 * @return The equivalence value
	 */
	public String getEquivalenceValue() {
		// as there cannot be multiple, the equivalence value is just the lpse
		// code with a forward slash
		return(lpseCode + "/");
	}

}
