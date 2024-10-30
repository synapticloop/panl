package com.synapticloop.panl.server.handler.fielderiser.field.facet;

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
 * IN THE SOFTWARE.
 */

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.BooleanFacetLpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.synapticloop.panl.server.handler.processor.Processor.*;

public class PanlBooleanFacetField extends PanlFacetField {
	public static final String BOOLEAN_TRUE_VALUE = "true";
	public static final String BOOLEAN_FALSE_VALUE = "false";
	public static final String JSON_KEY_IS_BOOLEAN_FACET = "is_boolean_facet";

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	//                         BOOLEAN Facet properties                        //
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	private boolean hasBooleanTrueReplacement;
	private boolean hasBooleanFalseReplacement;
	private String booleanTrueReplacement;
	private String booleanFalseReplacement;

	public PanlBooleanFacetField(String lpseCode, String propertyKey, Properties properties, String solrCollection, String panlCollectionUri, int lpseLength) throws PanlServerException {
		super(lpseCode, propertyKey, properties, solrCollection, panlCollectionUri, lpseLength);
		validateProperties();

		populateBooleanReplacements();
		populateSuffixAndPrefix();
		populateSolrFieldTypeValidation();
		populatePanlAndSolrFieldNames();

		logWarnProperties(this.lpseCode, PROPERTY_KEY_PANL_OR_FACET + this.lpseCode);
		logWarnProperties(this.lpseCode, PROPERTY_KEY_PANL_RANGE_FACET + this.lpseCode);
		logDetails();
	}

	/**
	 * <p>Populate any replacements for boolean field types which map to the Solr
	 * field type of <code>solr.BoolField</code></p>
	 *
	 * <p>This will only have an effect if it is a boolean field type</p>
	 */
	private void populateBooleanReplacements() {
		// finally - we are going to look at the replacement - but only if there
		// is a type of solr.BoolField and values are actually assigned

		populateSolrFieldTypeValidation();

		if (null != solrFieldType && solrFieldType.equals(TYPE_SOLR_BOOL_FIELD)) {
			this.booleanTrueReplacement = properties.getProperty("panl.bool." + this.lpseCode + ".true", null);
			if (null != this.booleanTrueReplacement) {
				hasBooleanTrueReplacement = true;
			} else {
				this.booleanTrueReplacement = BOOLEAN_TRUE_VALUE;
			}

			this.booleanFalseReplacement = properties.getProperty("panl.bool." + this.lpseCode + ".false", null);
			if (null != this.booleanFalseReplacement) {
				hasBooleanFalseReplacement = true;
			} else {
				this.booleanFalseReplacement = BOOLEAN_FALSE_VALUE;
			}
		} else {
			this.booleanTrueReplacement = null;
			this.booleanFalseReplacement = null;
		}
	}

	public String getEncodedPanlValue(LpseToken lpseToken) {
		return (getEncodedPanlValue(lpseToken.getValue()));
	}

	public String getEncodedPanlValue(String value) {
		StringBuilder sb = new StringBuilder();

		if (hasValuePrefix) {
			sb.append(valuePrefix);
		}

		if ("true".equals(value) && hasBooleanTrueReplacement) {
			sb.append(booleanTrueReplacement);
		} else if (hasBooleanFalseReplacement) {
			sb.append(booleanFalseReplacement);
		} else {
			sb.append(value);
		}

		if (hasValueSuffix) {
			sb.append(valueSuffix);
		}

		return (URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8));
	}

	public String getDecodedValue(String value) {
		String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);

		if (hasValuePrefix) {
			if (decodedValue.startsWith(valuePrefix)) {
				decodedValue = decodedValue.substring(valuePrefix.length());
			} else {
				return (null);
			}
		}

		if (hasValueSuffix) {
			if (decodedValue.endsWith(valueSuffix)) {
				decodedValue = decodedValue.substring(0, decodedValue.length() - valueSuffix.length());
			} else {
				return (null);
			}
		}

		if (hasBooleanTrueReplacement && booleanTrueReplacement.equals(decodedValue)) {
			return BOOLEAN_TRUE_VALUE;
		}

		if (hasBooleanFalseReplacement && booleanFalseReplacement.equals(decodedValue)) {
			return BOOLEAN_FALSE_VALUE;
		}

		// if we get to this point, and we cannot determine whether it is true or false
		if (BOOLEAN_TRUE_VALUE.equalsIgnoreCase(value)) {
			return (BOOLEAN_TRUE_VALUE);
		} else if (BOOLEAN_FALSE_VALUE.equalsIgnoreCase(value)) {
			return (BOOLEAN_FALSE_VALUE);
		} else {
			return (null);
		}
	}

	/**
	 * <p>This may have a true/false value replacement</p>
	 *
	 * @param panlTokenMap The panl token map to look up
	 * @param collectionProperties The collection properties
	 *
	 * @return The URI path for these tokens
	 */
	@Override
	public String getURIPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		if (!panlTokenMap.containsKey(lpseCode) || panlTokenMap.get(lpseCode).isEmpty()) {
			return ("");
		}

		for (LpseToken lpseToken : panlTokenMap.get(lpseCode)) {
			if (!lpseToken.getIsValid()) {
				// not a valid token - keep going
				continue;
			}

			// we are going to only do the first token (true/false only has two
			// values, and they are mutually exclusive)
			return (getURIPath(lpseToken, collectionProperties));
		}
		return ("");
	}

	public String getURIPath(LpseToken lpseToken, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder();
		String decoded = lpseToken.getValue();
		if (hasValuePrefix) {
			sb.append(valuePrefix);
		}

		if (decoded.equals(BOOLEAN_TRUE_VALUE)) {
			if (hasBooleanTrueReplacement) {
				sb.append(booleanTrueReplacement);
			} else {
				sb.append(BOOLEAN_TRUE_VALUE);
			}
		} else {
			if (hasBooleanFalseReplacement) {
				sb.append(booleanFalseReplacement);
			} else {
				sb.append(BOOLEAN_FALSE_VALUE);
			}
		}

		if (hasValueSuffix) {
			sb.append(valueSuffix);
		}

		return (URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8) + "/");
	}

	protected void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokenList) {
		// we are only going to do the first one
		for (LpseToken lpseToken : lpseTokenList) {
			BooleanFacetLpseToken booleanFacetLpseToken = (BooleanFacetLpseToken) lpseToken;
			solrQuery.addFilterQuery(String.format("%s:\"%s\"",
					booleanFacetLpseToken.getSolrField(),
					booleanFacetLpseToken.getValue()));
			return;
		}
	}

	public List<LpseToken> instantiateTokens(CollectionProperties collectionProperties, String lpseCode, String query, StringTokenizer valueTokeniser, LpseTokeniser lpseTokeniser) {
		return(List.of(new BooleanFacetLpseToken(collectionProperties, this.lpseCode, lpseTokeniser, valueTokeniser)));
	}

	@Override public void appendToAvailableObjectInternal(JSONObject jsonObject) {
		jsonObject.put(JSON_KEY_IS_BOOLEAN_FACET, true);
	}

	@Override public void addToRemoveObject(JSONObject removeObject, LpseToken lpseToken) {
		removeObject.put(JSON_KEY_IS_BOOLEAN_FACET, true);
		BooleanFacetLpseToken booleanFacetLpseToken = (BooleanFacetLpseToken)lpseToken;
		// now we need to put in the inverse URI
		if(lpseToken.getIsValid()) {
			removeObject.put(JSON_KEY_INVERSE_ENCODED, booleanFacetLpseToken.getInverseBooleanValue(lpseToken));
		}
	}

	@Override public List<String> explainAdditional() {
		List<String> explanations = new ArrayList<>(super.explainAdditional());
		explanations.add("Is a BOOLEAN facet which will allow a selection of either 'true' or 'false'.");

		if(hasBooleanTrueReplacement) {
			explanations.add("Will replace boolean 'true' values with '" + booleanTrueReplacement + "'.");
		} else {
			explanations.add("Will not replace boolean 'true' values.");
		}

		if(hasBooleanFalseReplacement) {
			explanations.add("Will replace boolean 'false' values with '" + booleanFalseReplacement + "'.");
		} else {
			explanations.add("Will not replace boolean 'false' values.");
		}

		return (explanations);
	}
}
