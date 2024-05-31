package com.synapticloop.panl.server.properties.field;

import com.synapticloop.panl.exception.PanlServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class FacetField {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacetField.class);

	private static final String PROPERTY_KEY_PANL_FACET = "panl.facet.";
	private static final String PROPERTY_KEY_PANL_NAME = "panl.name.";
	private static final String PROPERTY_KEY_PANL_TYPE = "panl.type.";
	private static final String PROPERTY_KEY_PANL_PREFIX = "panl.prefix.";
	private static final String PROPERTY_KEY_PANL_SUFFIX = "panl.suffix.";

	public static final String BOOLEAN_TRUE_VALUE = "true";
	public static final String BOOLEAN_FALSE_VALUE = "false";

	private final String panlLpseCode;
	private final String panlFacetName;
	private final String solrFieldName;
	private final String panlPrefix;
	private final String panlSuffix;
	private final String solrFieldType;

	private final boolean isBooleanSolrFieldType;
	private final String booleanTrueReplacement;
	private final String booleanFalseReplacement;

	public FacetField(String panlFacetProperty, Properties properties, String collectionName, int panlLpseNum) throws PanlServerException {
		this.solrFieldName = properties.getProperty(panlFacetProperty);
		this.panlLpseCode = panlFacetProperty.substring(PROPERTY_KEY_PANL_FACET.length());

		if (panlLpseCode.length() != panlLpseNum) {
			throw new PanlServerException(PROPERTY_KEY_PANL_FACET + panlLpseCode + " property key is of invalid length - should be " + panlLpseNum);
		}

		LOGGER.info("[{}] Mapping Solr facet named '{}' to panl key '{}'", collectionName, solrFieldName, panlLpseCode);

		String panlFacetNameTemp = properties.getProperty(PROPERTY_KEY_PANL_NAME + panlLpseCode, null);
		if (null == panlFacetNameTemp) {
			LOGGER.warn("[{}] Could not find a name for Panl facet LPSE code '{}', using Solr field name '{}'", collectionName, panlLpseCode, solrFieldName);
			this.panlFacetName = solrFieldName;
		} else {
			this.panlFacetName = panlFacetNameTemp;
			LOGGER.info("[{}] Found a name for Panl facet LPSE code '{}', using '{}'", collectionName, panlLpseCode, panlFacetName);
		}

		// now we need to look at the suffixes and prefixes
		String facetPrefix = properties.getProperty(PROPERTY_KEY_PANL_PREFIX + panlLpseCode);
		if (null != facetPrefix) {
			this.panlPrefix = facetPrefix;
		} else {
			this.panlPrefix = null;
		}

		String facetSuffix = properties.getProperty(PROPERTY_KEY_PANL_SUFFIX + panlLpseCode);
		if (null != facetSuffix) {
			this.panlSuffix = facetSuffix;
		} else {
			this.panlSuffix = null;
		}

		// finally - we are going to look at the replacement - but only if there
		// is a type of solr.BoolField and values are actually assigned

		this.solrFieldType = properties.getProperty(PROPERTY_KEY_PANL_TYPE + panlLpseCode);
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

	public String getConvertedFromPanlValue(String value) {
		String temp = value;

		if (hasPrefix()) {
			if (temp.startsWith(panlPrefix)) {
				temp = temp.substring(panlPrefix.length());
			}
		}

		if (hasSuffix()) {
			if (temp.endsWith(panlSuffix)) {
				temp = temp.substring(0, temp.length() - panlSuffix.length());
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
				return BOOLEAN_TRUE_VALUE;
			} else {
				return BOOLEAN_FALSE_VALUE;
			}
		}

		return (temp);
	}

	public String getConvertedToPanlValue(String value) {
		StringBuilder sb = new StringBuilder();

		if(hasPrefix()) {
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

		if(hasSuffix()) {
			sb.append(panlSuffix);
		}

		return (sb.toString());
	}

	private boolean hasPrefix() {
		return (null != panlPrefix);
	}

	private boolean hasSuffix() {
		return (null != panlSuffix);
	}

	private boolean hasBooleanTrueReplacement() {
		return (null != this.booleanTrueReplacement);
	}

	private boolean hasBooleanFalseReplacement() {
		return (null != this.booleanFalseReplacement);
	}

	public String getPanlLpseCode() {
		return panlLpseCode;
	}

	public String getPanlFacetName() {
		return panlFacetName;
	}

	public String getSolrFieldName() {
		return solrFieldName;
	}

	public String getSolrFieldType() {
		return solrFieldType;
	}
}
