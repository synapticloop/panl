package com.synapticloop.panl.server.properties.field;

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
import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
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

	private boolean hasPrefix = false;
	private boolean hasSuffix = false;
	private String panlPrefix;
	private String panlSuffix;

	protected boolean isOrFacet = false;
	protected boolean isRangeFacet = false;
	private boolean hasMinRange = false;
	private String rangeMinRange;
	private boolean hasMaxRange = false;
	private String rangeMaxRange;
	private boolean hasRangeMidfix = false;
	private String rangeMidfix;

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

	private static final int VALIDATION_TYPE_NONE = 0;
	private static final int VALIDATION_TYPE_NUMBER = 1;
	private static final int VALIDATION_TYPE_DECIMAL = 2;

	private int validationType;
	private int panlLpseLength;

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
			int panlLpseLength) throws PanlServerException {

		this.lpseCode = lpseCode;
		this.properties = properties;
		this.propertyKey = propertyKey;
		this.collectionName = collectionName;
		this.panlLpseLength = panlLpseLength;

		if (this.lpseCode.length() != panlLpseLength) {
			throw new PanlServerException(propertyKey + " has invalid lpse length of " + lpseCode.length() + " is of invalid length - should be " + panlLpseLength);
		}
	}

	public void logDetails() {
		getLogger().info("[{}] [{}] Mapping Solr field name '{}' to panl key '{}', LPSE length {}, isOrFacet: {}, isRangeFacet: {}",
				collectionName,
				this.getClass().getSimpleName(),
				solrFieldName,
				lpseCode,
				panlLpseLength,
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
		this.isRangeFacet =  properties.getProperty("panl.range." + lpseCode, "false").equals("true");
		if(this.isRangeFacet) {
			// get the other properties, if they exist...
			this.rangeMinRange = properties.getProperty("panl.range.min." + lpseCode, null);
			if(null != this.rangeMinRange) {
				hasMinRange = true;
			}

			this.rangeMaxRange = properties.getProperty("panl.range.max." + lpseCode, null);
			if(null != this.rangeMaxRange) {
				hasMaxRange = true;
			}

//			this.rangeMaxRange = properties.getProperty("panl.range.prefix." + lpseCode, null);
//			if(null != this.rangeMaxRange) {
//				hasMaxRange = true;
//			}
//
//			this.rangeMaxRange = properties.getProperty("panl.range.suffix." + lpseCode, null);
//			if(null != this.rangeMaxRange) {
//				hasMaxRange = true;
//			}

			this.rangeMidfix = properties.getProperty("panl.range.midfix." + lpseCode, null);
			if(null != this.rangeMidfix) {
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
	 *
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
		this.panlPrefix = properties.getProperty(propertyKey + ".prefix");

		this.panlSuffix = properties.getProperty(propertyKey + ".suffix");

		checkPrefixSuffix();
	}

	protected void populateSuffixAndPrefix() {
		this.panlPrefix = properties.getProperty(PROPERTY_KEY_PANL_PREFIX + lpseCode);

		this.panlSuffix = properties.getProperty(PROPERTY_KEY_PANL_SUFFIX + lpseCode);

		checkPrefixSuffix();
	}

	private void checkPrefixSuffix() {
		if (this.panlPrefix != null && !this.panlPrefix.isEmpty()) {
			hasPrefix = true;
		}

		if (this.panlSuffix != null && !this.panlSuffix.isEmpty()) {
			hasSuffix = true;
		}
	}

	public String getPanlPrefix() {
		if (hasPrefix) {
			return (panlPrefix);
		} else {
			return ("");
		}
	}

	public String getPanlSuffix() {
		if (hasSuffix) {
			return (panlSuffix);
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
		String temp = URLDecoder.decode(value, StandardCharsets.UTF_8);

		if (hasPrefix) {
			if (temp.startsWith(panlPrefix)) {
				temp = temp.substring(panlPrefix.length());
			} else {
				return (null);
			}
		}

		if (hasSuffix) {
			if (temp.endsWith(panlSuffix)) {
				temp = temp.substring(0, temp.length() - panlSuffix.length());
			} else {
				return (null);
			}
		}

		if (isBooleanSolrFieldType) {
			if (hasBooleanTrueReplacement && booleanTrueReplacement.equals(value)) {
				return BOOLEAN_TRUE_VALUE;
			}

			if (hasBooleanFalseReplacement && booleanFalseReplacement.equals(value)) {
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
		String validatedValue = getValidatedValue(temp);
		if (null != validatedValue && validatedValue.isBlank()) {
			return (null);
		} else {
			return validatedValue;
		}
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
		if (null == value) {
			return ("");
		}

		StringBuilder sb = new StringBuilder();

		if (hasPrefix) {
			sb.append(panlPrefix);
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

		if (hasSuffix) {
			sb.append(panlSuffix);
		}

		return (URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8));
	}

	public String getSolrFieldType() {
		return solrFieldType;
	}

	public String getURIPath(LpseToken token, CollectionProperties collectionProperties) {
		return (getEncodedPanlValue(token.getValue()) + "/");
	}

	public String getLpseCode(LpseToken token, CollectionProperties collectionProperties) {
		return (token.getLpseCode());
	}

	public String getURIPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder();
		if (panlTokenMap.containsKey(lpseCode)) {
			for (LpseToken lpseToken : panlTokenMap.get(lpseCode)) {
				if (lpseToken.getIsValid()) {
					sb.append(getEncodedPanlValue(lpseToken.getValue()));
					sb.append("/");
				}
			}
		}
		return (sb.toString());
	}

	public String getLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder();
		if (panlTokenMap.containsKey(lpseCode)) {
			for (LpseToken lpseToken : panlTokenMap.get(lpseCode)) {
				if (lpseToken.getIsValid()) {
					sb.append(lpseToken.getLpseCode());
				}
			}
		}
		return (sb.toString());
	}

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

		if (hasPrefix) {
			temp.add("             Prefix: '" + panlPrefix + "'.");
		}

		if (hasSuffix) {
			temp.add("             Suffix: '" + panlSuffix + "'.");
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
		if(hasMinRange) {
			return(rangeMinRange);
		} else {
			// TODO - needs to be based on the field type
			return(Integer.toString(Integer.MIN_VALUE));
		}
	}

	public String getMaxRange() {
		if(hasMaxRange) {
			return(rangeMaxRange);
		} else {
			// TODO - needs to be based on the field type
			return(Integer.toString(Integer.MAX_VALUE));
		}
	}

	public boolean getHasRangeMidfix() {
		return(hasRangeMidfix);
	}
	public String getRangeMidfix() {
		return(rangeMidfix);
	}

	protected abstract void applyToQueryInternal(SolrQuery solrQuery, Map<String, List<LpseToken>> panlTokenMap);
}