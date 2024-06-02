package com.synapticloop.panl.server.properties;

import com.synapticloop.panl.exception.PanlServerException;
//import com.synapticloop.panl.server.handler.field.BaseField;
//import com.synapticloop.panl.server.handler.field.FacetField;
//import com.synapticloop.panl.server.handler.field.MetaDataField;
import com.synapticloop.panl.server.properties.field.FacetField;
import com.synapticloop.panl.server.properties.util.PropertyHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CollectionProperties {
	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionProperties.class);

	// STATIC strings
	public static final String PROPERTY_KEY_PANL_FACET = "panl.facet.";
	public static final String PROPERTY_KEY_PANL_NAME = "panl.name.";
	public static final String PROPERTY_KEY_PANL_RESULTS_FIELDS = "panl.results.fields.";

	/**
	 * <p>The name of this collection</p>
	 */
	private final String collectionName;
	/**
	 * <p>The collection.panl.properties that drive this collection</p>
	 */
	private final Properties properties;

	/**
	 * <p>The minimum number of results that have this facet value to include</p>
	 */
	private int facetMinCount;
	/**
	 * <p>The number of results returned per search/page</p>
	 */
	private int numResultsPerPage;

	/**
	 * <p>The number of characters that make up the LPSE code </p>
	 */
	private Integer panlLpseNum;

	private List<FacetField> FACET_FIELDS = new ArrayList<>();
	private boolean panlIncludeSingleFacets;
	private boolean panlIncludeSameNumberFacets;

	private final String validUrls; // the

	private String panlParamQuery;
	private String panlParamSort;
	private String panlParamPage;
	private String panlParamNumRows;
	private String panlParamQueryOperand;
	private String panlParamPassthrough;

	private String solrDefaultQueryOperand;
	private int solrFacetLimit;

	private final List<String> lpseOrder = new ArrayList<>();
//	private final List<BaseField> lpseFields = new ArrayList<>();

	private final Set<String> metadataMap = new HashSet<>();

	private final Map<String, String> panlCodeToSolrFieldNameMap = new HashMap<>();
	private final Map<String, String> panlCodeToPanlNameMap = new HashMap<>();
	private final Map<String, String> solrFacetNameToPanlCodeMap = new HashMap<>();
	private final Map<String, String> solrFacetNameToPanlName = new HashMap<>();

	/**
	 * <p>The Set of all boolean facets that are available within this
	 * collection.  If it is a boolean facet, then it this facet may have
	 * replacements for the true/false values.</p>
	 */
	private final Set<String> panlBooleanFacets = new HashSet<>();
	/**
	 * <p>The hashmap of true String values for a boolean panl facet - if they
	 * are defined for this facet. This map is keyed on
	 * <code>&lt;lpseCode&gt</code> with the replacement value as the
	 * stored value.</p>
	 */
	private final Map<String, String> panlBooleanFacetTrueValues = new HashMap<>();
	/**
	 * <p>The hashmap of false String values for a boolean panl facet - if they
	 * are defined for this facet. This map is keyed on
	 * <code>&lt;lpseCode&gt;</code> with the replacement value as the
	 * stored value.</p>
	 */
	private final Map<String, String> panlBooleanFacetFalseValues = new HashMap<>();

	private final Map<String, List<String>> resultFieldsMap = new HashMap<>();
	/**
	 * <p>The list of all the named Solr facet fields - Note that this is not
	 * used as a FieldSet - it is all of the Solr fields that will be
	 * faceted on.</p>
	 */
	private String[] solrFacetFields;

	/**
	 * <p>This is the prefix map for each panl code, it is keyed on
	 * <code>&lt;lpseCode&gt;</code> with the value as the String prefix for this
	 * facet.</p>
	 */
	private final Map<String, String> panlFacetPrefixMap = new HashMap<>();
	/**
	 * <p>This is the suffix map for each panl code, it is keyed on
	 * <code>&lt;lpseCode&gt;</code> with the value as the String suffix for this
	 * facet.</p>
	 */
	private final Map<String, String> panlFacetSuffixMap = new HashMap<>();

	private final List<String> panlLpseCodeSortFields = new ArrayList<>();

	public CollectionProperties(String collectionName, Properties properties) throws PanlServerException {
		this.collectionName = collectionName;
		this.properties = properties;

		parseDefaultProperties();
		parseFacetFields();
		parseLpseOrder();
		parseResultFields();
		parseSortFields();


		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		for (String resultFieldsName : getResultFieldsNames()) {
			jsonArray.put("/" + collectionName + "/" + resultFieldsName + "/");
		}

		jsonObject.put("valid_urls", jsonArray);

		this.validUrls = jsonObject.toString();
	}

	private void parseSortFields() {
		String sortFieldsTemp = properties.getProperty("panl.sort.fields", "");
		for (String sortField : sortFieldsTemp.split(",")) {
			String panlLpseCode = solrFacetNameToPanlCodeMap.getOrDefault(sortField, null);
			if(null == panlLpseCode) {
				LOGGER.warn("[{}] '{}' Could not look up the Panl LPSE code for Solr field name '{}', ignoring...", collectionName, "panl.sort.fields", sortField);
			} else {
				LOGGER.info("[{}] Adding Panl LPSE code '{}' for Solr field name '{}'.", collectionName, panlLpseCode, sortField);
				panlLpseCodeSortFields.add(panlLpseCode);
			}
		}
	}

	/**
	 * <p>Parse the default properties for a collection.</p>
	 *
	 * @throws PanlServerException If a mandatory property was not found, or
	 *                             could not be adequately parsed
	 */
	private void parseDefaultProperties() throws PanlServerException {
		this.panlIncludeSingleFacets = properties.getProperty("panl.include.single.facets", "false").equals("true");
		this.panlIncludeSameNumberFacets = properties.getProperty("panl.include.same.number.facets", "false").equals("true");

		this.facetMinCount = PropertyHelper.getIntProperty(properties, "solr.facet.min.count", 1);
		this.numResultsPerPage = PropertyHelper.getIntProperty(properties, "solr.numrows.default", 10);
		this.solrFacetLimit = PropertyHelper.getIntProperty(properties, "solr.facet.limit", 100);


		this.panlLpseNum = PropertyHelper.getIntProperty(properties, "panl.lpse.num", null);
		if (null == panlLpseNum) {
			throw new PanlServerException("MANDATORY PROPERTY MISSING: Could not find the 'panl.lpse.num' property in the '" + this.collectionName + "' Panl properties file.'");
		}
		LOGGER.info("[{}] LPSE number set to '{}'", collectionName, panlLpseNum);

		this.solrDefaultQueryOperand = properties.getProperty("solr.default.query.operand", "+");
		if(!(this.solrDefaultQueryOperand.equals("+") || this.solrDefaultQueryOperand.equals("-"))) {
			throw new PanlServerException("Property solr.default.query.operand __MUST__ be one of '+', or '-'.");
		} else {
			LOGGER.info("[{}] default query operand set to '{}'", collectionName, solrDefaultQueryOperand);
		}

		this.panlParamQuery = initialiseStringProperty("panl.param.query", true, false);
		this.panlParamSort = initialiseStringProperty("panl.param.sort", true, false);
		this.panlParamPage = initialiseStringProperty("panl.param.page", true, true);
		this.panlParamNumRows = initialiseStringProperty("panl.param.numrows", true, true);
		this.panlParamQueryOperand = initialiseStringProperty("panl.param.operand", true, false);
		this.panlParamPassthrough = initialiseStringProperty("panl.param.passthrough", false, false);
	}

	/**
	 * <p>Initialise a string property from the properties file.</p>
	 *
	 * <p>This will look up the property from the properties.  If it doesn't
	 * exist and is a mandatory property, it will throw an exception.  If the
	 * property isn't mandatory, then it will return null,</p>
	 *
	 * <p>If the hasPrefixSuffix parameter is set, then it will also look for a
	 * property with the key of <code>propertyName + ".prefix"</code> and if it
	 * exists, it will add it to the prefix map.  If there is a property with the
	 * key of <code>propertyName + ".suffix</code>, then it will be added to the
	 * suffix map.</p>
	 *
	 * <p>Finally, if the property is found it will be added to the metadatMap.</p>
	 *
	 * @param propertyName    The property name to look up
	 * @param isMandatory     Whether this is a mandatory property - if it is, and
	 *                        it doesn't exist, then this will throw a PanlServerException
	 * @param hasPrefixSuffix Whether this property can also
	 * @return the initialised property, or null if it doesn't exist
	 * @throws PanlServerException If a mandatory property was not found
	 */
	private String initialiseStringProperty(String propertyName, boolean isMandatory, boolean hasPrefixSuffix) throws PanlServerException {
		String panlPropertyValue = properties.getProperty(propertyName, null);
		if (null == panlPropertyValue) {
			if (isMandatory) {
				throw new PanlServerException(
						"MANDATORY PROPERTY MISSING: Could not find the '" +
								propertyName +
								"' property in the '" +
								this.collectionName +
								"' Panl properties file.'");
			} else {
				return (null);
			}
		}

		LOGGER.info("[{}] {} set to '{}'", collectionName, propertyName, panlPropertyValue);
		metadataMap.add(panlPropertyValue);

		if (hasPrefixSuffix) {
			// now for the suffix and prefix
			String paramPrefix = properties.getProperty(propertyName + ".prefix", null);
			if (null != paramPrefix && !paramPrefix.isEmpty()) {
				LOGGER.info("[{}] {}.prefix set to '{}'", collectionName, propertyName, panlPropertyValue);
				panlFacetPrefixMap.put(panlPropertyValue, paramPrefix);
			}

			String paramSuffix = properties.getProperty(propertyName + ".suffix", null);
			if (null != paramSuffix && !paramSuffix.isEmpty()) {
				LOGGER.info("[{}] {}.suffix set to '{}'", collectionName, propertyName, panlPropertyValue);
				panlFacetSuffixMap.put(panlPropertyValue, paramSuffix);
			}
		}
		return (panlPropertyValue);
	}

	/**
	 * <p>Parse the properties files and extract all properties that begin with
	 * the panl facet property key</p>
	 *
	 * <p> See the
	 * {@link  CollectionProperties#PROPERTY_KEY_PANL_FACET PROPERTY_KEY_PANL_FACET}
	 * static String for the panl prefix property</p>
	 *
	 * @throws PanlServerException If there was an error looking up the properties,
	 *                             or with the found property and its associated values
	 */
	private void parseFacetFields() throws PanlServerException {
		List<String> facetFieldList = new ArrayList<>();
		for (String panlFieldKey : PropertyHelper.getPropertiesByPrefix(properties, PROPERTY_KEY_PANL_FACET)) {
			FACET_FIELDS.add(new FacetField(panlFieldKey, properties, collectionName, panlLpseNum));
		}

		// now parse the fields
		for (String panlFieldKey : PropertyHelper.getPropertiesByPrefix(properties, PROPERTY_KEY_PANL_FACET)) {
			String panlFieldValue = properties.getProperty(panlFieldKey);
			String panlFacetCode = panlFieldKey.substring(PROPERTY_KEY_PANL_FACET.length());

			if (panlFacetCode.length() != panlLpseNum) {
				throw new PanlServerException(PROPERTY_KEY_PANL_FACET + panlFacetCode + " property key is of invalid length - should be " + panlLpseNum);
			}

			panlCodeToSolrFieldNameMap.put(panlFacetCode, panlFieldValue);
			facetFieldList.add(panlFieldValue);
			LOGGER.info("[{}] Mapping facet '{}' to panl key '{}'", collectionName, panlFieldValue, panlFacetCode);
			String panlFacetName = properties.getProperty(PROPERTY_KEY_PANL_NAME + panlFacetCode, null);
			if (null == panlFacetName) {
				LOGGER.warn("[{}] Could not find a name for panl facet code '{}', using field name '{}'", collectionName, panlFacetCode, panlFieldValue);
				panlFacetName = panlFieldValue;
			} else {
				LOGGER.info("[{}] Found a name for panl facet code '{}', using '{}'", collectionName, panlFacetCode, panlFacetName);
			}
			panlCodeToPanlNameMap.put(panlFacetCode, panlFacetName);

			// now we need to look at the suffixes and prefixes
			String facetPrefix = properties.getProperty("panl.prefix." + panlFacetCode);
			if (null != facetPrefix) {
				panlFacetPrefixMap.put(panlFacetCode, facetPrefix);
			}
			String facetSuffix = properties.getProperty("panl.suffix." + panlFacetCode);
			if (null != facetSuffix) {
				panlFacetSuffixMap.put(panlFacetCode, facetSuffix);
			}

			// finally - we are going to look at the replacement - but only if there
			// is a type of solr.BoolField and values are actually assigned

			String facetClassName = properties.getProperty("panl.type." + panlFacetCode);
			if (null != facetClassName && facetClassName.equals("solr.BoolField")) {
				// see if we have a true, or false value for it
				panlBooleanFacets.add(panlFacetCode);
				panlBooleanFacetTrueValues.put(panlFacetCode, properties.getProperty("panl.bool." + panlFacetCode + ".true", "true"));
				panlBooleanFacetFalseValues.put(panlFacetCode, properties.getProperty("panl.bool." + panlFacetCode + ".false", "false"));
			}

			solrFacetNameToPanlCodeMap.put(panlFieldValue, panlFacetCode);
			solrFacetNameToPanlName.put(panlFieldValue, panlFacetName);
		}

		this.solrFacetFields = facetFieldList.toArray(new String[0]);
	}

	/**
	 * <p></p>
	 *
	 * @throws PanlServerException if the panl.lpse.order does not exist
	 */
	private void parseLpseOrder() throws PanlServerException {
		String panlLpseOrder = properties.getProperty("panl.lpse.order", null);
		if (null == panlLpseOrder) {
			throw new PanlServerException("Could not find the panl.lpse.order");
		}

		for (String lpseCode : panlLpseOrder.split(",")) {
			lpseCode = lpseCode.trim();
			if (panlCodeToSolrFieldNameMap.containsKey(lpseCode)) {
				lpseOrder.add(lpseCode);
//				lpseFields.add(new FacetField(lpseCode));
			} else if (metadataMap.contains(lpseCode)) {
				lpseOrder.add(lpseCode);
//				lpseFields.add(new MetaDataField(lpseCode));
			} else {
				LOGGER.warn("Found a panl code of '{}' in the panl.lpse.order property, yet it is not a defined field.  This will be ignored...", lpseCode);
			}
		}
	}

	private void parseResultFields() throws PanlServerException {
		List<String> resultFieldProperties = PropertyHelper.getPropertiesByPrefix(properties, PROPERTY_KEY_PANL_RESULTS_FIELDS);
		for (String resultFieldProperty : resultFieldProperties) {
			addResultsFields(resultFieldProperty.substring(PROPERTY_KEY_PANL_RESULTS_FIELDS.length()), properties.getProperty(resultFieldProperty));
		}
	}

	private void addResultsFields(String resultFieldsName, String resultFields) throws PanlServerException {
		if (resultFieldsMap.containsKey(resultFieldsName)) {
			throw new PanlServerException("panl.results.fields.'" + resultFieldsName + "' is already defined.");
		}

		LOGGER.info("[{}] Adding result fields with key '{}', and fields '{}'.", collectionName, resultFieldsName, resultFields);
		List<String> fields = new ArrayList<>(Arrays.asList(resultFields.split(",")));
		resultFieldsMap.put(resultFieldsName, fields);
	}


	public String getCollectionName() {
		return (collectionName);
	}

	public int getPanlLpseNum() {
		return (panlLpseNum);
	}

	public String getPanlParamQuery() {
		return (panlParamQuery);
	}

	public String getPanlParamQueryOperand() {
		return (panlParamQueryOperand);
	}

	public String getPanlParamSort() {
		return (panlParamSort);
	}

	public String getPanlParamPassthrough() {
		return (panlParamPassthrough);
	}

	public String getPanlParamPage() {
		return (panlParamPage);
	}

	public String getPanlParamNumRows() {
		return (panlParamNumRows);
	}

	public String getSolrDefaultQueryOperand() {
		if(solrDefaultQueryOperand.equals("-")) {
			return("OR");
		} else {
			return("AND");
		}
	}

	public List<String> getLpseOrder() {
		return (lpseOrder);
	}

	public List<String> getResultFieldsNames() {
		return (new ArrayList<>(resultFieldsMap.keySet()));
	}

	public List<String> getResultFieldsForName(String name) {
		return (resultFieldsMap.get(name));
	}

	public Map<String, List<String>> getResultFieldsMap() {
		return (resultFieldsMap);
	}

	public boolean isValidResultFieldsName(String name) {
		return (resultFieldsMap.containsKey(name));
	}

	public String getValidUrlsJson() {
		return (this.validUrls);
	}

	public int getFacetMinCount() {
		return facetMinCount;
	}

	public int getNumResultsPerPage() {
		return numResultsPerPage;
	}

	public String[] getSolrFacetFields() {
		return (solrFacetFields);
	}

//	public List<BaseField> getLpseFields() {
//		return (lpseFields);
//	}

	public boolean isMetadataToken(String token) {
		return (metadataMap.contains(token));
	}

	public boolean hasSortField(String panlCode) {
		return (panlCodeToSolrFieldNameMap.containsKey(panlCode));
	}

	public String getSolrFacetNameFromPanlLpseCode(String name) {
		return(panlCodeToSolrFieldNameMap.get(name));
	}

	public String getNameFromCode(String panlCode) {
		return (panlCodeToSolrFieldNameMap.get(panlCode));
	}

	public boolean hasFacetCode(String panlfacet) {
		return (panlCodeToSolrFieldNameMap.containsKey(panlfacet));
	}

	public String getPanlCodeFromSolrFacetName(String name) {
		return (solrFacetNameToPanlCodeMap.get(name));
	}

	public String getPanlNameFromSolrFacetName(String name) {
		return (solrFacetNameToPanlName.get(name));
	}

	public String getPanlNameFromPanlCode(String name) {
		return (panlCodeToPanlNameMap.get(name));
	}

	public boolean getPanlIncludeSingleFacets() {
		return (panlIncludeSingleFacets);
	}

	public boolean getPanlIncludeSameNumberFacets() {
		return (panlIncludeSameNumberFacets);
	}

	/**
	 * <p>The panl value (from the URI) can have a prefix or suffix, or both
	 * applied to it.</p>
	 *
	 * <p>Remove any suffixes, or prefixes from a URI parameter, should they
	 * be defined for the LPSE code.</p>
	 *
	 * <p>Additionally, if this is a boolean field, it may be that there also is
	 * a replacement for true/false for it.</p>
	 *
	 * @param panlFacetCode The panl LPSE code to lookup
	 * @param value         the value to convert if any conversions are required
	 * @return the de-suffixed, de-prefixed, and de-replaced value.
	 */
	public String getConvertedFromPanlValue(String panlFacetCode, String value) {
		String temp = value;

		if (panlFacetPrefixMap.containsKey(panlFacetCode)) {
			String prefix = panlFacetPrefixMap.get(panlFacetCode);
			if (temp.startsWith(prefix)) {
				temp = temp.substring(prefix.length());
			}
		}

		if (panlFacetSuffixMap.containsKey(panlFacetCode)) {
			String suffix = panlFacetSuffixMap.get(panlFacetCode);
			if (temp.endsWith(suffix)) {
				temp = temp.substring(0, temp.length() - panlFacetSuffixMap.get(panlFacetCode).length());
			}
		}

		if (panlBooleanFacets.contains(panlFacetCode)) {
			// we might have a boolean replacement
			if (panlBooleanFacetFalseValues.containsKey(panlFacetCode) &&
					panlBooleanFacetFalseValues.getOrDefault(panlFacetCode, "").equals(value)) {
				return ("false");
			}
			// we might have a boolean replacement
			if (panlBooleanFacetTrueValues.containsKey(panlFacetCode) &&
					panlBooleanFacetTrueValues.getOrDefault(panlFacetCode, "").equals(value)) {
				return ("true");
			}
		}

		return (temp);
	}

	public String getConvertedToPanlValue(String panlFacetCode, String value) {
		StringBuilder sb = new StringBuilder();

		if (panlFacetPrefixMap.containsKey(panlFacetCode)) {
			sb.append(panlFacetPrefixMap.get(panlFacetCode));
		}

		if (panlBooleanFacets.contains(panlFacetCode)) {
			// we might have a boolean replacement
			if (panlBooleanFacetFalseValues.containsKey(panlFacetCode) && value.equalsIgnoreCase("false")) {
				sb.append(panlBooleanFacetFalseValues.getOrDefault(panlFacetCode, "false"));
			} else if (panlBooleanFacetTrueValues.containsKey(panlFacetCode) && value.equalsIgnoreCase("true")) {
				sb.append(panlBooleanFacetTrueValues.getOrDefault(panlFacetCode, "true"));
			} else {
				// we don;t have a boolean replacement
				sb.append(value);
			}
		} else {
			sb.append(value);
		}

		if (panlFacetSuffixMap.containsKey(panlFacetCode)) {
			sb.append(panlFacetSuffixMap.get(panlFacetCode));
		}

		return (sb.toString());
	}

	public String getSuffixForLpseCode(String code) {
		return (panlFacetSuffixMap.getOrDefault(code, ""));
	}

	public String getPrefixForLpseCode(String code) {
		return (panlFacetPrefixMap.getOrDefault(code, ""));
	}

	public List<String> getSortFields() {
		return(panlLpseCodeSortFields);
	}

	public int getSolrFacetLimit() {
		return(solrFacetLimit);
	}
}
