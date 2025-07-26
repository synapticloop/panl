package com.synapticloop.panl.server.handler.fielderiser.field.param;

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

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.fielderiser.field.BasePrefixSuffixField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.PageNumLpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PanlPageNumField extends BasePrefixSuffixField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlPageNumField.class);

	public PanlPageNumField(String lpseCode, String propertyKey, Properties properties, String solrCollection, String panlCollectionUri) throws PanlServerException {
		super(lpseCode, propertyKey, properties, solrCollection, panlCollectionUri);
	}


	@Override public String getURIPath(LpseToken token, CollectionProperties collectionProperties) {
		PageNumLpseToken pageNumLpseToken = (PageNumLpseToken) token;
		if(pageNumLpseToken.getPageNum() == 1) {
			return("");
		} else {
			return(getEncodedPanlValue(token.getValue()) + "/");
		}
	}

	@Override
	public String getURIPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		for (LpseToken lpseToken : panlTokenMap.getOrDefault(this.lpseCode, new ArrayList<>())) {
			return(getURIPath(lpseToken, collectionProperties));
		}

		return("");
	}

	@Override public String getLpseCode(LpseToken token, CollectionProperties collectionProperties) {
		PageNumLpseToken pageNumLpseToken = (PageNumLpseToken) token;
		if(pageNumLpseToken.getPageNum() == 1) {
			return("");
		} else {
			return(this.lpseCode);
		}
	}

	/**
	 * <p>Get the LPSE code for the LPSE URI path from the field.</p>
	 *
	 * <p>This will loop through all LpseTokens for this code outputting the
	 * correct code.  The following rules apply:</p>
	 *
	 * <ul>
	 *   <li>If the <code>panlTokenMap</code> does not contain a key of this
	 *   field's LPSE code, then a blank string will be returned.</li>
	 *   <li>If the token is not valid, then an empty string will be returned.</li>
	 *   <li>If the token is a RANGE facet token, then the lpse code will
	 *   include whether it has an infix or not.
	 *   <ul>
	 *     <li>If it has an infix, the returned string will be
	 *     <code>&lt;lpse_code&gt;-&lt;lpse_code&gt;</code></li>
	 *     <li>If it does not have an infix, then the returned string will be of
	 *     the format <code>&lt;lpse_code&gt;+&lt;lpse_code&gt;</code></li>
	 *   </ul>
	 *   </li>
	 * </ul>
	 *
	 * @param panlTokenMap The map of LPSE codes to the list of tokens for that
	 * 		LPSE code
	 * @param collectionProperties The collection properties for this collection
	 * 		this is unused in this implementation
	 *
	 * @return The LPSE URI path for this field
	 */
	@Override
	public String getLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		for (LpseToken lpseToken : panlTokenMap.getOrDefault(this.lpseCode, new ArrayList<>())) {
			return(getLpseCode(lpseToken, collectionProperties));
		}

		return("");
	}

	/**
	 * <p>Get the canonical LPSE token - this will <strong>ALWAYS</strong> be the
	 * LPSE code</p>
	 *
	 * @param panlTokenMap The panl token map to look at
	 * @param collectionProperties The collection properties
	 *
	 * @return This will <strong>ALWAYS</strong> be the LPSE code
	 */
	@Override
	public String getCanonicalLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return (lpseCode);
	}

	/**
	 * <p>Get the canonical URI path for the page number.  This __ALWAYS__
	 * returns a URI path. The path will be encoded with a prefix and/or
	 * suffix if they have been set.</p>
	 *
	 * @param panlTokenMap The token map with all fields and a list of their
	 * 		values
	 * @param collectionProperties The collection properties
	 *
	 * @return The URI path, never an empty string
	 */
	@Override
	public String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder();
		if (panlTokenMap.containsKey(lpseCode) && !panlTokenMap.get(lpseCode).isEmpty()) {

			// get the first token out of the list - there can be only one, so we
			// choose the first one.

			PageNumLpseToken pageNumLpseToken = (PageNumLpseToken) panlTokenMap.get(lpseCode).get(0);
			sb.append(getEncodedPanlValue(Integer.toString(pageNumLpseToken.getPageNum())));
		} else {
			sb.append(getEncodedPanlValue("1"));
		}

		sb.append("/");
		return (sb.toString());
	}

	/**
	 * <p>When resetting the page number, it is always blank, whether or not it
	 * exists in the panl token map.  This is because when we add/remove etc, we
	 * go back to the first page always</p>
	 *
	 * @param panlTokenMap The panl token map
	 * @param collectionProperties The collection properties
	 *
	 * @return The reset URI path
	 */
	public String getResetUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return("");
	}

	public String getResetUriPath(LpseToken lpseToken, CollectionProperties collectionProperties) {
		return("");
	}

	public String getResetLpseCode(LpseToken lpseToken, CollectionProperties collectionProperties) {
		return ("");
	}


	/**
	 * <p>Get the reset LPSE code for the page number.  This will always return
	 * an empty string as a rest will always return to the first page of results.</p>
	 *
	 * @param panlTokenMap The panlToken map to see if a list of tokens is
	 * 		available to generate the resetLpseCode for
	 * @param collectionProperties The collection properties to look up defaults
	 * 		if required
	 *
	 * @return The reset LPSE code - always an empty string
	 */
	public String getResetLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return ("");
	}

	/**
	 * <p>Get the logger</p>
	 *
	 * @return The logger for the object
	 */
	@Override public Logger getLogger() {
		return (LOGGER);
	}

	/**
	 * <p>Return the additional explanation for debugging/information used in the
	 * Panl results explainer web app.</p>
	 *
	 * @return The additional explanation for debugging/information
	 */
	@Override public List<String> explainAdditional() {
		List<String> explanations = new ArrayList<>();
		explanations.add("The page number of the results (works in conjunction with the number of results).");
		return(explanations);
	}

	/**
	 * <p>Apply the token to the Solr Query - this does do anything as Solr only
	 * has a concept of a <code>start</code> parameter, which is worked out in
	 * conjunction with the number of results per page.</p>
	 *
	 * @param solrQuery The SolrQuery to apply the tokens to
	 * @param lpseTokenList The list of tokens to apply to the Solr query
	 */
	public void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokenList, CollectionProperties collectionProperties) {
		// do nothing - this relies on other data and is set by the handler
	}

	/**
	 * <p>Instantiate the token for this field, which is a PageNumLpseToken</p>
	 *
	 * @param collectionProperties The collection properties
	 * @param lpseCode The lpseCode for this field
	 * @param query The query parameter
	 * @param valueTokeniser The value tokeniser
	 * @param lpseTokeniser The lpse tokeniser
	 *
	 * @return the token for this field, which is a PageNumLpseToken
	 *
	 * @see PageNumLpseToken
	 */
	@Override public List<LpseToken> instantiateTokens(CollectionProperties collectionProperties, String lpseCode, String query, StringTokenizer valueTokeniser, LpseTokeniser lpseTokeniser) {
		return(List.of(new PageNumLpseToken(collectionProperties, this.lpseCode, valueTokeniser)));
	}

	/**
	 * <p>Log the details upon instantiation.</p>
	 */
	@Override protected void logDetails() {
		getLogger().info("[ Solr/Panl '{}/{}' ] Page number parameter mapped to '{}'.",
				solrCollection,
				panlCollectionUri,
				lpseCode);
	}

	@Override public String getPanlFieldType() {
		return("PAGE NUM");
	}

}
