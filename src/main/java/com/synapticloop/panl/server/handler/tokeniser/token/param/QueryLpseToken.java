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
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <p>The query operand </p>
 */
public class QueryLpseToken extends LpseToken {
	private boolean isOverride;
	private List<String> searchableLpseFields = new ArrayList<>();

	public QueryLpseToken(
			CollectionProperties collectionProperties,
			String queryFromUri,
			String lpseCode) {

		this(collectionProperties, lpseCode, queryFromUri, null, null);
	}

	public QueryLpseToken(
			CollectionProperties collectionProperties,
			String lpseCode,
			String queryFromUri,
			StringTokenizer valueTokeniser,
			LpseTokeniser lpseTokeniser) {

		super(lpseCode, collectionProperties);

		if (null != valueTokeniser && valueTokeniser.hasMoreTokens()) {
			this.value = URLDecoder.decode(
					valueTokeniser.nextToken(),
					StandardCharsets.UTF_8);
		}

		StringBuilder queryLpseCodes = new StringBuilder();
		boolean foundCorrectTokens = false;
		// now we need to see if we are going to search on specific fields
		if (null != lpseTokeniser && lpseTokeniser.hasMoreTokens()) {
			String lpseToken = lpseTokeniser.nextToken();
			if (lpseToken.equals("(")) {
				// consume until we get an end ")"
				while (lpseTokeniser.hasMoreTokens()) {
					lpseToken = lpseTokeniser.nextToken();
					if (lpseToken.equals(")")) {
						foundCorrectTokens = true;
						break;
					} else {
						queryLpseCodes.append(lpseToken);
					}
				}
			} else {
				// we don't have a specific field search going on here
				lpseTokeniser.decrementCurrentPosition();
			}
		}

		if (foundCorrectTokens) {
			// we want to split the tokens on the LPSE length

			// \G is a zero-width assertion that matches the position where the previous match ended. If there was no
			// previous match, it matches the beginning of the input, the same as \A. The enclosing lookbehind matches
			// the position that's four characters along from the end of the last match.

			for (String lpseSearchCode :
					queryLpseCodes.toString()
					              .split("(?<=\\G.{" + collectionProperties.getLpseLength() + "})")) {
				// ensure that it is a valid searchable field - else ignore
				if (lpseSearchCode.length() == collectionProperties.getLpseLength()) {
					String solrSearchField = collectionProperties.getSearchCodesMap().get(lpseSearchCode);
					if (null != solrSearchField) {
						searchableLpseFields.add(solrSearchField);
					}
				}
			}
		}

		Map<String, String> nameValuePairMap = new HashMap<>();

		String formQueryRespondTo = collectionProperties.getFormQueryRespondTo();
		for (NameValuePair nameValuePair : URLEncodedUtils.parse(queryFromUri, StandardCharsets.UTF_8)) {
			nameValuePairMap.put(nameValuePair.getName(), nameValuePair.getValue());
		}

		if (nameValuePairMap.containsKey(formQueryRespondTo)) {
			this.value = URLDecoder.decode(
					nameValuePairMap.get(formQueryRespondTo),
					StandardCharsets.UTF_8);
			isOverride = true;

			searchableLpseFields.clear();

			// now go through and find the fields to search in

			Map<String, String> searchFields = collectionProperties.getSearchCodesMap();
			Iterator<String> iterator = searchFields.keySet().iterator();
			while (iterator.hasNext()) {
				String next = iterator.next();
				String key = searchFields.get(next);
				if (nameValuePairMap.containsKey(formQueryRespondTo + "." + next)) {
					searchableLpseFields.add(key);
				}
			}
		}
	}

	@Override public String explain() {
		return ("PANL [  VALID  ]" +
				" <query>             LPSE code '" +
				this.lpseCode +
				"' with value '" +
				value +
				"'" +
				(isOverride ? " (Overridden by query parameter)." : ".") +
				getExplainSearchFields()
		);
	}

	private String getExplainSearchFields() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append(" Searching Solr field(s) ");
		for (String searchableLpseField : searchableLpseFields) {
			if(!first) {
				sb.append(", ");
			}
			first = false;
			sb.append("'")
			  .append(searchableLpseField)
			  .append("'");
		}

		if(searchableLpseFields.isEmpty()) {
			sb.append("'default'");
		}
		sb.append(".");

		return sb.toString();
	}

	@Override public String getType() {
		return ("query");
	}

	@Override public String getValue() {
		return super.getValue();
	}

	/**
	 * <p>Get the list of searchable solr fields that this query string should
	 * operate on.</p>
	 *
	 * @return The list of searchable fields.
	 */
	public List<String> getSearchableLpseFields() {
		return(searchableLpseFields);
	}
}
