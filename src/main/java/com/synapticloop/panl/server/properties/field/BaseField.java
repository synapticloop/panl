package com.synapticloop.panl.server.properties.field;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.properties.CollectionProperties;
import com.synapticloop.panl.server.tokeniser.token.LpseToken;
import org.slf4j.Logger;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public abstract class BaseField {
	protected static final String PROPERTY_KEY_PANL_FIELD = "panl.field.";
	protected static final String PROPERTY_KEY_PANL_NAME = "panl.name.";
	protected static final String PROPERTY_KEY_PANL_FACET = "panl.facet.";
	protected static final String PROPERTY_KEY_PANL_TYPE = "panl.type.";
	protected static final String PROPERTY_KEY_PANL_PREFIX = "panl.prefix.";
	protected static final String PROPERTY_KEY_PANL_SUFFIX = "panl.suffix.";

	protected static final String BOOLEAN_TRUE_VALUE = "true";
	protected static final String BOOLEAN_FALSE_VALUE = "false";

	private boolean hasPrefix = false;
	private boolean hasSuffix = false;
	private String panlPrefix;
	private String panlSuffix;

	private boolean isBooleanSolrFieldType;
	private String booleanTrueReplacement;
	private String booleanFalseReplacement;

	protected String panlLpseCode;
	private String panlFieldName;
	private String solrFieldName;
	private String solrFieldType;

	private final String propertyKey;
	private final String collectionName;

	public BaseField(
					String lpseCode,
					String propertyKey,
					String collectionName) throws PanlServerException {
		this(lpseCode, propertyKey, collectionName, 1);
	}

	public BaseField(
					String lpseCode,
					String propertyKey,
					String collectionName,
					int panlLpseNum) throws PanlServerException {

		this.panlLpseCode = lpseCode;
		this.propertyKey = propertyKey;
		this.collectionName = collectionName;

		if (this.panlLpseCode.length() != panlLpseNum) {
			throw new PanlServerException(propertyKey + " has invalid lpse length of " + lpseCode.length() + " is of invalid length - should be " + panlLpseNum);
		}

		getLogger().info("[{}] [{}] Mapping Solr field name '{}' to panl key '{}', LPSE length {}",
						collectionName,
						this.getClass().getSimpleName(),
						solrFieldName,
						panlLpseCode,
						panlLpseNum);
	}

	protected void populateBooleanReplacements(Properties properties, String lpseCode) {
		// finally - we are going to look at the replacement - but only if there
		// is a type of solr.BoolField and values are actually assigned

		populateSolrFieldType(properties, lpseCode);

		if (null != solrFieldType && solrFieldType.equals("solr.BoolField")) {
			this.booleanTrueReplacement = properties.getProperty("panl.bool." + panlLpseCode + ".true", BOOLEAN_TRUE_VALUE);
			this.booleanFalseReplacement = properties.getProperty("panl.bool." + panlLpseCode + ".false", BOOLEAN_FALSE_VALUE);
			this.isBooleanSolrFieldType = true;
		} else {
			this.booleanTrueReplacement = null;
			this.booleanFalseReplacement = null;
			this.isBooleanSolrFieldType = false;
		}
	}

	protected void populateSolrFieldType(Properties properties, String lpseCode) {
		if(null == this.solrFieldType) {
			this.solrFieldType = properties.getProperty(PROPERTY_KEY_PANL_TYPE + panlLpseCode);
		}
	}

	protected void populatePanlAndSolrFieldNames(Properties properties, String lpseCode) {
		this.solrFieldName = properties.getProperty(PROPERTY_KEY_PANL_FACET + lpseCode);

		if(null == this.solrFieldName) {
			this.solrFieldName = properties.getProperty(PROPERTY_KEY_PANL_FIELD + lpseCode);
		}

		String panlFieldNameTemp = properties.getProperty(PROPERTY_KEY_PANL_NAME + panlLpseCode, null);
		if (null == panlFieldNameTemp) {
			this.panlFieldName = solrFieldName;
		} else {
			this.panlFieldName = panlFieldNameTemp;
		}
	}

	protected void populateParamSuffixAndPrefix(Properties properties, String propertyKey) {
		this.panlPrefix = properties.getProperty(propertyKey + ".prefix");

		this.panlSuffix = properties.getProperty(propertyKey + ".suffix");

		checkPrefixSuffix();
	}

	protected void populateSuffixAndPrefix(Properties properties, String panlLpseCode) {
		this.panlPrefix = properties.getProperty(PROPERTY_KEY_PANL_PREFIX + panlLpseCode);

		this.panlSuffix = properties.getProperty(PROPERTY_KEY_PANL_SUFFIX + panlLpseCode);

		checkPrefixSuffix();
	}

	private void checkPrefixSuffix() {
		if(this.panlPrefix != null && !this.panlPrefix.isEmpty()) {
			hasPrefix = true;
		}

		if(this.panlSuffix != null && !this.panlSuffix.isEmpty()) {
			hasSuffix = true;
		}
	}

	public String getPanlPrefix() {
		if(hasPrefix) {
			return(panlPrefix);
		} else {
			return("");
		}
	}

	public String getPanlSuffix() {
		if(hasSuffix) {
			return(panlSuffix);
		} else {
			return("");
		}
	}

	public abstract Logger getLogger();

	public String getPanlLpseCode() {
		return panlLpseCode;
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
	 * @param value the value to convert if any conversions are required
	 * @return the de-suffixed, de-prefixed, and de-replaced value.
	 */
	public String getDecodedValue(String value) {
		String temp = value;

		if (hasPrefix) {
			if (temp.startsWith(panlPrefix)) {
				temp = temp.substring(panlPrefix.length());
			} else {
				return(null);
			}
		}

		if (hasSuffix) {
			if (temp.endsWith(panlSuffix)) {
				temp = temp.substring(0, temp.length() - panlSuffix.length());
			} else {
				return(null);
			}
		}

		if (isBooleanSolrFieldType) {
			if (hasBooleanTrueReplacement() && booleanTrueReplacement.equals(value)) {
				return BOOLEAN_TRUE_VALUE;
			}

			if (hasBooleanFalseReplacement() && booleanFalseReplacement.equals(value)) {
				return BOOLEAN_FALSE_VALUE;
			}

			// if we get to this point, and we cannot determine whether it is true or false
			if (BOOLEAN_TRUE_VALUE.equalsIgnoreCase(value)) {
				return(BOOLEAN_TRUE_VALUE);
			} else if (BOOLEAN_FALSE_VALUE.equalsIgnoreCase(value)) {
				return(BOOLEAN_FALSE_VALUE);
			} else {
				return(null);
			}
		}

		return (URLDecoder.decode(temp, StandardCharsets.UTF_8));
	}

public String getEncodedPanlValue(String value) {
		if(null == value) {
			return("");
		}

		StringBuilder sb = new StringBuilder();

		if(hasPrefix) {
			sb.append(panlPrefix);
		}

		if(isBooleanSolrFieldType) {
			if(hasBooleanTrueReplacement() && value.equalsIgnoreCase(BOOLEAN_TRUE_VALUE)) {
				sb.append(booleanTrueReplacement);
			} else if(hasBooleanFalseReplacement() && value.equalsIgnoreCase(BOOLEAN_FALSE_VALUE)) {
				sb.append(booleanFalseReplacement);
			} else {
				if (BOOLEAN_TRUE_VALUE.equalsIgnoreCase(value)) {
					sb.append (BOOLEAN_TRUE_VALUE);
				} else {
					sb.append (BOOLEAN_FALSE_VALUE);
				}
			}
		} else {
			sb.append(value);
		}

		if(hasSuffix) {
			sb.append(panlSuffix);
		}

		return (URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8));
	}

	private boolean hasBooleanTrueReplacement() {
		return (null != this.booleanTrueReplacement);
	}

	private boolean hasBooleanFalseReplacement() {
		return (null != this.booleanFalseReplacement);
	}

	public String getSolrFieldType() {
		return solrFieldType;
	}

	public String getURIPath(LpseToken token, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder();
		sb.append(getEncodedPanlValue(token.getValue()));
		sb.append("/");
		return(sb.toString());
	}

	public String getLpseCode(LpseToken token, CollectionProperties collectionProperties) {
		return(token.getLpseCode());
	}

	public String getURIPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder();
		if(panlTokenMap.containsKey(panlLpseCode)) {
			for(LpseToken lpseToken : panlTokenMap.get(panlLpseCode)) {
				if(lpseToken.getIsValid()) {
					sb.append(getEncodedPanlValue(lpseToken.getValue()));
					sb.append("/");
				}
			}
		}
		return(sb.toString());
	}

	public String getLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder();
		if(panlTokenMap.containsKey(panlLpseCode)) {
			for(LpseToken lpseToken : panlTokenMap.get(panlLpseCode)) {
				if(lpseToken.getIsValid()) {
					sb.append(lpseToken.getLpseCode());
				}
			}
		}
		return(sb.toString());
	}

	public String getCanonicalUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return(getURIPath(panlTokenMap, collectionProperties));
	}

	public String getCanonicalLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return(getLpseCode(panlTokenMap, collectionProperties));
	}

	public String getResetUriPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return(getURIPath(panlTokenMap, collectionProperties));
	}

	public String getResetLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return(getLpseCode(panlTokenMap, collectionProperties));
	}

}