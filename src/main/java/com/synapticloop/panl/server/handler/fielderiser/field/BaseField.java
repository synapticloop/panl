package com.synapticloop.panl.server.handler.fielderiser.field;

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

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.token.FacetLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.bean.FromToBean;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public abstract class BaseField {
	protected static final String PROPERTY_KEY_PANL_FIELD = "panl.field.";
	protected static final String PROPERTY_KEY_PANL_NAME = "panl.name.";
	protected static final String PROPERTY_KEY_PANL_FACET = "panl.facet.";
	protected static final String PROPERTY_KEY_PANL_OR_FACET = "panl.or.facet.";
	protected static final String PROPERTY_KEY_PANL_TYPE = "panl.type.";
	protected static final String PROPERTY_KEY_PANL_PREFIX = "panl.prefix.";
	protected static final String PROPERTY_KEY_PANL_SUFFIX = "panl.suffix.";
	public static final String PROPERTY_KEY_SOLR_FACET_MIN_COUNT = "solr.facet.min.count";

	protected static final String BOOLEAN_TRUE_VALUE = "true";
	protected static final String BOOLEAN_FALSE_VALUE = "false";

	private boolean hasValuePrefix = false;
	private boolean hasValueSuffix = false;
	private String valuePrefix;
	private String getValueSuffix;

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	//                            OR Facet properties                          //
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	protected boolean isOrFacet = false;

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	//                          RANGE Facet properties                         //
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	protected boolean isRangeFacet = false;
	private boolean hasMinRange = false;
	private String rangeMinRange;
	private boolean hasMaxRange = false;
	private String rangeMaxRange;
	private boolean hasRangeMidfix = false;
	private String rangeValueMidfix;

	private boolean hasRangePrefix;
	private String rangeValuePrefix;
	private boolean hasRangeSuffix;
	private String rangeValueSuffix;

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	//                         BOOLEAN Facet properties                        //
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	private boolean isBooleanSolrFieldType;
	private boolean hasBooleanTrueReplacement;
	private boolean hasBooleanFalseReplacement;
	private String booleanTrueReplacement;
	private String booleanFalseReplacement;

	protected String lpseCode;
	private String panlFieldName;
	private String solrFieldName;
	private String solrFieldType;

	private final Properties properties;
	private final String collectionName;
	private final String propertyKey;

	private final int lpseLength;

	private static final int VALIDATION_TYPE_NONE = 0;
	private static final int VALIDATION_TYPE_NUMBER = 1;
	private static final int VALIDATION_TYPE_DECIMAL = 2;

	private int validationType;

	public BaseField(
			String lpseCode,
			Properties properties,
			String propertyKey,
			String collectionName) throws PanlServerException {
		this(lpseCode, properties, propertyKey, collectionName, 1);
	}

	public BaseField(
			String lpseCode,
			Properties properties,
			String propertyKey,
			String collectionName,
			int lpseLength) throws PanlServerException {

		this.lpseCode = lpseCode;
		this.properties = properties;
		this.propertyKey = propertyKey;
		this.collectionName = collectionName;
		this.lpseLength = lpseLength;

		if (this.lpseCode.length() != lpseLength) {
			throw new PanlServerException(propertyKey + " has invalid lpse length of " + lpseCode.length() + " is of invalid length - should be " + lpseLength);
		}
	}

	public void logDetails() {
		getLogger().info("[{}] Mapping Solr field name '{}' to panl key '{}', LPSE length {}, isOrFacet: {}, isRangeFacet: {}",
				collectionName,
				solrFieldName,
				lpseCode,
				lpseLength,
				isOrFacet,
				isRangeFacet);
	}

	protected void populateFacetOr() {
		this.isOrFacet = properties.getProperty(PROPERTY_KEY_PANL_OR_FACET + lpseCode, "false").equalsIgnoreCase("true");
		if (this.isOrFacet) {
			String propertyFacetMinCount = properties.getProperty(PROPERTY_KEY_SOLR_FACET_MIN_COUNT, null);
			if (null != propertyFacetMinCount) {
				try {
					int minCount = Integer.parseInt(propertyFacetMinCount);
					if (minCount != 0) {
						getLogger().warn("Property '{}' __MUST__ be set to zero for '{}{}' to be enabled.", PROPERTY_KEY_SOLR_FACET_MIN_COUNT, PROPERTY_KEY_PANL_OR_FACET, lpseCode);
					}
				} catch (NumberFormatException e) {
					// TODO - should we throw an exception???
					getLogger().error("Property '{}' must be set", PROPERTY_KEY_SOLR_FACET_MIN_COUNT);
				}
			}
		}
	}

	protected void populateBooleanReplacements() {
		// finally - we are going to look at the replacement - but only if there
		// is a type of solr.BoolField and values are actually assigned

		populateSolrFieldType();

		if (null != solrFieldType && solrFieldType.equals("solr.BoolField")) {
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

			this.isBooleanSolrFieldType = true;
		} else {
			this.booleanTrueReplacement = null;
			this.booleanFalseReplacement = null;
			this.isBooleanSolrFieldType = false;
		}
	}

	protected void populateSolrFieldType() {
		if (null == this.solrFieldType) {
			this.solrFieldType = properties.getProperty(PROPERTY_KEY_PANL_TYPE + lpseCode);
			switch (this.solrFieldType) {
				case "solr.IntPointField":
				case "solr.LongPointField":
					this.validationType = VALIDATION_TYPE_NUMBER;
					break;
				case "solr.DoublePointField":
				case "solr.FloatPointField":
					this.validationType = VALIDATION_TYPE_DECIMAL;
					break;
				default:
					this.validationType = VALIDATION_TYPE_NONE;
			}
		}
	}

	protected void populateRanges() {
		this.isRangeFacet = properties.getProperty("panl.range.facet." + lpseCode, "false").equals("true");
		if (this.isRangeFacet) {
			// get the other properties, if they exist...
			this.rangeMinRange = properties.getProperty("panl.range.min." + lpseCode, null);
			if (null != this.rangeMinRange) {
				hasMinRange = true;
			}

			this.rangeMaxRange = properties.getProperty("panl.range.max." + lpseCode, null);
			if (null != this.rangeMaxRange) {
				hasMaxRange = true;
			}

			this.rangeValuePrefix = properties.getProperty("panl.range.prefix." + lpseCode, null);
			if (null != this.rangeValuePrefix) {
				hasRangePrefix = true;
			}

			this.rangeValueSuffix = properties.getProperty("panl.range.suffix." + lpseCode, null);
			if (null != this.rangeValueSuffix) {
				hasRangeSuffix = true;
			}

			this.rangeValueMidfix = properties.getProperty("panl.range.midfix." + lpseCode, null);
			if (null != this.rangeValueMidfix) {
				hasRangeMidfix = true;
			}
		}
	}

	/**
	 * <p>Populate the names for both Solr and Panl. THe Solr name is the field
	 * name.  THe Panl name is either set, or will default to the Solr field
	 * name.</p>
	 *
	 * <p>The Panl name can be set to any string and can be little nicer than the
	 * Solr field name.</p>
	 */
	protected void populatePanlAndSolrFieldNames() {
		this.solrFieldName = properties.getProperty(PROPERTY_KEY_PANL_FACET + lpseCode);

		if (null == this.solrFieldName) {
			this.solrFieldName = properties.getProperty(PROPERTY_KEY_PANL_FIELD + lpseCode);
		}

		String panlFieldNameTemp = properties.getProperty(PROPERTY_KEY_PANL_NAME + this.lpseCode, null);
		if (null == panlFieldNameTemp) {
			this.panlFieldName = solrFieldName;
		} else {
			this.panlFieldName = panlFieldNameTemp;
		}
	}

	protected void populateParamSuffixAndPrefix() {
		this.valuePrefix = properties.getProperty(propertyKey + ".prefix");

		this.getValueSuffix = properties.getProperty(propertyKey + ".suffix");

		checkPrefixSuffix();
	}

	protected void populateSuffixAndPrefix() {
		this.valuePrefix = properties.getProperty(PROPERTY_KEY_PANL_PREFIX + lpseCode);

		this.getValueSuffix = properties.getProperty(PROPERTY_KEY_PANL_SUFFIX + lpseCode);

		checkPrefixSuffix();
	}

	private void checkPrefixSuffix() {
		if (this.valuePrefix != null && !this.valuePrefix.isEmpty()) {
			hasValuePrefix = true;
		}

		if (this.getValueSuffix != null && !this.getValueSuffix.isEmpty()) {
			hasValueSuffix = true;
		}
	}

	public String getValuePrefix() {
		if (hasValuePrefix) {
			return (valuePrefix);
		} else {
			return ("");
		}
	}

	public String getValueSuffix() {
		if (hasValueSuffix) {
			return (getValueSuffix);
		} else {
			return ("");
		}
	}

	public abstract Logger getLogger();

	public String getLpseCode() {
		return lpseCode;
	}

	public String getPanlFieldName() {
		return panlFieldName;
	}

	public String getSolrFieldName() {
		return solrFieldName;
	}

	/**
	 * <p>The panl value (from the URI) can have a prefix or suffix, or both
	 * applied to it.</p>
	 *
	 * <p>Remove any suffixes, or prefixes from a URI parameter, should they be
	 * defined for the LPSE code.</p>
	 *
	 * <p>Additionally, if this is a boolean field, it may be that there also is
	 * a replacement for true/false for it.</p>
	 *
	 * <p>This will also validate the value by the Solr field type, at the
	 * moment, the only fields that have additional validation are:</p>
	 *
	 * <ul>
	 *   <li>"solr.IntPointField"</li>
	 *   <li>"solr.LongPointField"</li>
	 *   <li>"solr.DoublePointField"</li>
	 *   <li>"solr.FloatPointField"</li>
	 * </ul>
	 *
	 * @param value the value to convert if any conversions are required
	 *
	 * @return the de-suffixed, de-prefixed, and de-replaced value.  This will
	 * 		return <code>null</code> if it is invalid.
	 */
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
			if (decodedValue.endsWith(getValueSuffix)) {
				decodedValue = decodedValue.substring(0, decodedValue.length() - getValueSuffix.length());
			} else {
				return (null);
			}
		}

		if (isBooleanSolrFieldType) {
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

		// now we are going to validate the fields, boolean and string fields have
		// their own validation
		String validatedValue = getValidatedValue(decodedValue);
		if (null != validatedValue && validatedValue.isBlank()) {
			return (null);
		} else {
			return validatedValue;
		}
	}

	/**
	 * <p>Decode a range facet values which are in one of two formats, which
	 * depends on whether the RANGE facet has <code>hasRangeMidfix</code> set.</p>
	 *
	 * <p><strong><code>hasRangeMidfix == true</code></strong></p>
	 *
	 * <ul>
	 *   <li>The value will be URL decoded, then</li>
	 *   <li>The value will be split on the <code>rangeMidfix</code></li>
	 *   <li>If the split is exactly two Strings, carry on else return null.</li>
	 * </ul>
	 *
	 * <p><strong><code>hasRangeMidfix == false</code></strong></p>
	 *
	 * <ul>
	 *   <li>The value will be split on the <code>/</code> character</li>
	 *   <li>If the split is exactly two Strings, carry on else return null.</li>
	 *   <li>The two values will be s URL decoded.</li>
	 * </ul>
	 *
	 * <p>The <code>fromValue</code> will have it's prefix removed.</p>
	 *
	 * <p>The <code>toValue</code> will have it's suffix removed.</p>
	 *
	 * <p>If all validation tests are passed, then return the bean.</p>
	 *
	 * @param value The value to decode for a range
	 *
	 * @return The FromToBean with the from and to values set.
	 */
	public FromToBean getDecodedRangeValues(String value) {

		String fromString = "";
		String toString = "";

		if (hasRangeMidfix) {
			// It is OK to decode the value as it is all in one
			String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);
			// then we need to split by the midfix
			String[] fromToSplit = decodedValue.split(rangeValueMidfix);
			if (fromToSplit.length != 2) {
				return (null);
			} else {
				fromString = fromToSplit[0];
				toString = fromToSplit[1];
			}
		} else {
			String[] fromToSplit = value.split("/");
			if (fromToSplit.length != 2) {
				return (null);
			} else {
				fromString = URLDecoder.decode(fromToSplit[0], StandardCharsets.UTF_8);
				toString = URLDecoder.decode(fromToSplit[1], StandardCharsets.UTF_8);
			}
		}

		// at this point we have two values, the from and to

		if (hasRangePrefix) {
			if (fromString.startsWith(rangeValuePrefix)) {
				fromString = fromString.substring(rangeValuePrefix.length());
			} else {
				return (null);
			}
		}

		if (hasRangeSuffix) {
			if (toString.endsWith(rangeValueSuffix)) {
				toString = toString.substring(0, toString.length() - rangeValueSuffix.length());
			} else {
				return (null);
			}
		}

		// lastly we are going to validate the values

		String validatedFromString = getValidatedValue(fromString);
		String validatedToString = getValidatedValue(toString);

		if (null == validatedFromString || null == validatedToString || validatedFromString.isBlank() || validatedToString.isBlank()) {
			return (null);
		}
		// now we have all that we need
		return (new FromToBean(validatedFromString, validatedToString));
	}


	/**
	 * <p>Get the validated value.  This will ensure that an incoming token value
	 * matches the Solr field type, and if it doesn't, then it will attempt to
	 * clean it up.</p>
	 *
	 * <p>At the moment, the only fields that are validated are:</p>
	 *
	 * <ul>
	 *   <li>"solr.IntPointField"</li>
	 *   <li>"solr.LongPointField"</li>
	 *   <li>"solr.DoublePointField"</li>
	 *   <li>"solr.FloatPointField"</li>
	 * </ul>
	 *
	 * <p>String/Text Solr fields are not validated, but are validated elsewhere.</p>
	 *
	 * @param temp The value to attempt to validate
	 *
	 * @return The validated value
	 */
	private String getValidatedValue(String temp) {
		String replaced;
		switch (this.validationType) {
			case VALIDATION_TYPE_NUMBER:
				replaced = temp.replaceAll("[^0-9]", "");
				if (replaced.isBlank()) {
					return (null);
				} else {
					return replaced;
				}
			case VALIDATION_TYPE_DECIMAL:
				replaced = temp.replaceAll("[^0-9.]", "");
				if (replaced.isBlank()) {
					return (null);
				} else {
					return replaced;
				}
		}
		return (temp);
	}

	public String getEncodedPanlValue(String value) {
		return (getEncodedRegularFacetValue(value));
	}

	public String getEncodedPanlValue(LpseToken token) {
		if (null == token.getValue()) {
			return ("");
		}

		if (isRangeFacet) {
			return (getEncodedRangeFacetValue(token));
		} else {
			return (getEncodedRegularFacetValue(token.getValue()));
		}
	}

	private String getEncodedRangeFacetValue(LpseToken token) {
		FacetLpseToken facetLpseToken = (FacetLpseToken) token;

		// we can still have a single facet value which is not a range facet
		if (null == facetLpseToken.getToValue()) {
			return (getEncodedRegularFacetValue(facetLpseToken.getValue()));
		}

		// at this point it is a range facet
		StringBuilder sb = new StringBuilder();

		if (hasRangeMidfix) {
			// just add it all together
			if (hasRangePrefix) {
				sb.append(rangeValuePrefix);
			}
			sb.append(facetLpseToken.getValue());

			sb.append(rangeValueMidfix);

			sb.append(facetLpseToken.getToValue());

			if (hasRangeSuffix) {
				sb.append(rangeValueSuffix);
			}
			return (URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8));
		} else {
			// we will have a two part URI path, split by a '/' and both values need
			// to be URLEncoded before.
			if (hasRangePrefix) {
				sb.append(URLEncoder.encode(rangeValuePrefix, StandardCharsets.UTF_8));
			}

			sb.append(URLEncoder.encode(facetLpseToken.getValue(), StandardCharsets.UTF_8));

			sb.append("/");

			sb.append(URLEncoder.encode(facetLpseToken.getToValue(), StandardCharsets.UTF_8));

			if (hasRangeSuffix) {
				sb.append(URLEncoder.encode(rangeValueSuffix, StandardCharsets.UTF_8));
			}
			return (sb.toString());
		}
	}

	private String getEncodedRegularFacetValue(String value) {
		StringBuilder sb = new StringBuilder();

		if (hasValuePrefix) {
			sb.append(valuePrefix);
		}

		if (isBooleanSolrFieldType) {
			if (hasBooleanTrueReplacement && value.equalsIgnoreCase(BOOLEAN_TRUE_VALUE)) {
				sb.append(booleanTrueReplacement);
			} else if (hasBooleanFalseReplacement && value.equalsIgnoreCase(BOOLEAN_FALSE_VALUE)) {
				sb.append(booleanFalseReplacement);
			} else {
				if (BOOLEAN_TRUE_VALUE.equalsIgnoreCase(value)) {
					sb.append(BOOLEAN_TRUE_VALUE);
				} else {
					sb.append(BOOLEAN_FALSE_VALUE);
				}
			}
		} else {
			sb.append(value);
		}

		if (hasValueSuffix) {
			sb.append(getValueSuffix);
		}

		return (URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8));
	}

	public String getSolrFieldType() {
		return solrFieldType;
	}

	public String getURIPath(LpseToken token, CollectionProperties collectionProperties) {
		return (getEncodedPanlValue(token) + "/");
	}

	public String getLpseCode(LpseToken token, CollectionProperties collectionProperties) {
		return (token.getLpseCode());
	}

	public String getURIPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder();
		if (panlTokenMap.containsKey(lpseCode)) {
			for (LpseToken lpseToken : panlTokenMap.get(lpseCode)) {
				if (lpseToken.getIsValid()) {
					sb.append(getEncodedPanlValue(lpseToken));
					sb.append("/");
				}
			}
		}
		return (sb.toString());
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
	 *   include whether it has a midfix or not.
	 *   <ul>
	 *     <li>If it has a midfix, the returned string will be
	 *     <code>&lt;lpse_code&gt;-&lt;lpse_code&gt;</code></li>
	 *     <li>If it does not have a midfix, then the returned string will be of
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
	public String getLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder();
		if (panlTokenMap.containsKey(lpseCode)) {
			for (LpseToken lpseToken : panlTokenMap.get(lpseCode)) {
				if (lpseToken.getIsValid()) {
					if (lpseToken instanceof FacetLpseToken) {
						FacetLpseToken facetLpseToken = (FacetLpseToken) lpseToken;
						if (facetLpseToken.getIsRangeToken()) {
							sb.append(lpseToken.getLpseCode());
							sb.append((facetLpseToken.getHasMidFix() ? "-" : "+"));
							sb.append(lpseToken.getLpseCode());
						} else {
							sb.append(lpseToken.getLpseCode());
						}
					} else {
						sb.append(lpseToken.getLpseCode());
					}
				}
			}
		}
		return (sb.toString());
	}

	/**
	 * <p>Get the canonical URI path for this field, if the field has any values.</p>
	 *
	 * @param panlTokenMap The token map with all fields and a list of their
	 * 		values
	 * @param collectionProperties THe collection properties
	 *
	 * @return The URI path for this field, or an empty string if the field has
	 * 		no values
	 */
	public String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return (getURIPath(panlTokenMap, collectionProperties));
	}

	public String getCanonicalLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return (getLpseCode(panlTokenMap, collectionProperties));
	}

	public String getResetUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return (getURIPath(panlTokenMap, collectionProperties));
	}

	public String getResetLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return (getLpseCode(panlTokenMap, collectionProperties));
	}

	@Deprecated public abstract String getExplainDescription();

	public List<String> explain() {
		List<String> temp = new ArrayList<>();
		temp.add("FIELD CONFIG [ " +
				this.getClass().getSimpleName() +
				" ] LPSE code '" +
				lpseCode +
				"' Solr field name '" +
				solrFieldName +
				"' of type '" +
				solrFieldType +
				"'.");

		if (hasValuePrefix) {
			temp.add("             Prefix: '" + valuePrefix + "'.");
		}

		if (hasValueSuffix) {
			temp.add("             Suffix: '" + getValueSuffix + "'.");
		}

		if (hasBooleanTrueReplacement) {
			temp.add("             '" + booleanTrueReplacement + "' maps to 'true'.");
		}

		if (hasBooleanFalseReplacement) {
			temp.add("             '" + booleanFalseReplacement + "' maps to 'false'.");
		}

		if (isOrFacet) {
			temp.add("             Is an OR facet, allowing multiple selections of this facet.");
		}

		temp.add("DESCRIPTION: " + getExplainDescription());
		return (temp);
	}

	public boolean getIsOrFacet() {
		return isOrFacet;
	}

	public boolean getIsRangeFacet() {
		return isRangeFacet;
	}

	public void applyToQuery(SolrQuery solrQuery, Map<String, List<LpseToken>> panlTokenMap) {
		// no facets, no query, all is good :)
		if (panlTokenMap.containsKey(getLpseCode())) {
			applyToQueryInternal(solrQuery, panlTokenMap);
		}
	}

	public String getMinRange() {
		if (hasMinRange) {
			return (rangeMinRange);
		} else {
			// TODO - needs to be based on the field type
			return (Integer.toString(Integer.MIN_VALUE));
		}
	}

	public String getMaxRange() {
		if (hasMaxRange) {
			return (rangeMaxRange);
		} else {
			// TODO - needs to be based on the field type
			return (Integer.toString(Integer.MAX_VALUE));
		}
	}

	public boolean getHasRangeMidfix() {
		return (hasRangeMidfix);
	}

	public String getRangeValueMidfix() {
		return (rangeValueMidfix);
	}

	public String getRangeValuePrefix() {
		if(hasRangePrefix) {
			return (rangeValuePrefix);
		} else {
			return("");
		}
	}

	public String getRangeValueSuffix() {
		if(hasRangeSuffix) {
			return (rangeValueSuffix);
		} else {
			return("");
		}
	}


	public String getPrefix() {
		if(hasRangeMidfix) {
			return(getRangeValuePrefix());
		} else {
			return(getValuePrefix());
		}
	}

	protected abstract void applyToQueryInternal(SolrQuery solrQuery, Map<String, List<LpseToken>> panlTokenMap);
}