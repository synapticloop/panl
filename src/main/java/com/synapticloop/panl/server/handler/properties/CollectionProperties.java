package com.synapticloop.panl.server.handler.properties;

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
//import com.synapticloop.panl.server.handler.field.BaseField;
//import com.synapticloop.panl.server.handler.field.FacetField;
//import com.synapticloop.panl.server.handler.field.MetaDataField;
import com.synapticloop.panl.server.handler.fielderiser.field.*;
import com.synapticloop.panl.server.handler.fielderiser.field.facet.*;
import com.synapticloop.panl.server.handler.fielderiser.field.param.*;
import com.synapticloop.panl.server.handler.helper.PropertyHelper;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.synapticloop.panl.server.handler.fielderiser.field.BaseField.*;

/**
 * <p>This object contains all information required for the Panl server bound
 * URI path with assigned field sets.</p>
 *
 * <p>The held information:</p>
 *
 * <ul>
 *   <li>Panl parameter queries</li>
 * </ul>
 *
 * @author synapticloop
 */
public class CollectionProperties {
	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionProperties.class);

	// STATIC strings
	public static final String PROPERTY_KEY_PANL_FACET = "panl.facet.";
	public static final String PROPERTY_KEY_PANL_FIELD = "panl.field.";
	public static final String PROPERTY_KEY_PANL_FORM_QUERY_RESPONDTO = "panl.form.query.respondto";
	public static final String PROPERTY_KEY_PANL_INCLUDE_SAME_NUMBER_FACETS = "panl.include.same.number.facets";
	public static final String PROPERTY_KEY_PANL_INCLUDE_SINGLE_FACETS = "panl.include.single.facets";
	public static final String PROPERTY_KEY_SOLR_HIGHLIGHT = "solr.highlight";
	public static final String PROPERTY_KEY_PANL_LPSE_LENGTH = "panl.lpse.length";
	public static final String PROPERTY_KEY_PANL_LPSE_ORDER = "panl.lpse.order";
	public static final String PROPERTY_KEY_PANL_LPSE_IGNORE = "panl.lpse.ignore";
	public static final String PROPERTY_KEY_PANL_PARAM_NUMROWS = "panl.param.numrows";
	public static final String PROPERTY_KEY_PANL_PARAM_PAGE = "panl.param.page";
	public static final String PROPERTY_KEY_PANL_PARAM_PASSTHROUGH = "panl.param.passthrough";
	public static final String PROPERTY_KEY_PANL_PARAM_QUERY = "panl.param.query";
	public static final String PROPERTY_KEY_PANL_PARAM_QUERY_OPERAND = "panl.param.query.operand";
	public static final String PROPERTY_KEY_PANL_PARAM_SORT = "panl.param.sort";
	public static final String PROPERTY_KEY_PANL_RESULTS_FIELDS = "panl.results.fields.";
	public static final String PROPERTY_KEY_PANL_SORT_FIELDS = "panl.sort.fields";
	public static final String PROPERTY_KEY_SOLR_DEFAULT_QUERY_OPERAND = "solr.default.query.operand";
	public static final String PROPERTY_KEY_SOLR_FACET_LIMIT = "solr.facet.limit";
	public static final String PROPERTY_KEY_SOLR_FACET_MIN_COUNT = "solr.facet.min.count";
	public static final String PROPERTY_KEY_SOLR_NUMROWS_DEFAULT = "solr.numrows.default";
	public static final String PROPERTY_KEY_SOLR_NUMROWS_LOOKAHEAD = "solr.numrows.lookahead";

	public static final String FIELDSETS_DEFAULT = "default";
	public static final String FIELDSETS_EMPTY = "empty";

	public static final String SOLR_DEFAULT_QUERY_OPERAND_OR = "OR";
	public static final String SOLR_DEFAULT_QUERY_OPERAND_AND = "AND";
	public static final String JSON_KEY_VALID_URLS = "valid_urls";


	/**
	 * <p>The name of this collection</p>
	 */
	private final String solrCollection;

	/**
	 * <p>The URI that this collection is bound to</p>
	 */
	private final String panlCollectionUri;

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
	 * <p>The number of results returned per search/page</p>
	 */
	private int numResultsLookahead;

	/**
	 * <p>The number of characters that make up the LPSE code </p>
	 */
	private Integer lpseLength;

	/**
	 * <p>This is the list of all facet fields that are registered with panl.
	 * These fields may be used as facets.</p>
	 */
	private final List<PanlFacetField> FACET_FIELDS = new ArrayList<>();
	private final List<PanlFacetField> FACET_INDEX_SORT_FIELDS = new ArrayList<>();

	/**
	 * <p>This is the list of all fields that are registered with panl. Unlike
	 * faceted fields, these may not be used as a facet, but will be able to be sorted.</p>
	 */
	private final List<PanlField> NON_FACET_FIELDS = new ArrayList<>();

	private final Set<String> LPSE_FACET_FIELDS = new HashSet<>();
	private final Set<String> LPSE_FIELDS = new HashSet<>();

	private final Map<String, PanlFacetField> LPSE_CODE_TO_FACET_FIELD_MAP = new HashMap<>();
	private final Map<String, PanlFacetField> SOLR_NAME_TO_FACET_FIELD_MAP = new HashMap<>();
	private final Map<String, PanlField> LPSE_CODE_TO_FIELD_MAP = new HashMap<>();
	private final Map<String, PanlField> SOLR_NAME_TO_FIELD_MAP = new HashMap<>();
	private final Map<String, PanlSortField> LPSE_CODE_TO_SORT_FIELD_MAP = new HashMap<>();
	private final Map<String, PanlSortField> SOLR_NAME_TO_SORT_FIELD_MAP = new HashMap<>();
	private final Map<String, PanlDateRangeFacetField> LPSE_CODE_DATE_RANGE_FACET_MAP = new HashMap<>();
	private final Map<String, PanlRangeFacetField> LPSE_CODE_RANGE_FACET_MAP = new HashMap<>();
	private final Map<String, PanlBooleanFacetField> LPSE_CODE_BOOLEAN_FACET_MAP = new HashMap<>();

	private final Map<String, Set<String>> LPSE_CODE_WHEN_MAP = new HashMap<>();
	private final Map<String, String> SOLR_NAME_TO_LPSE_CODE_MAP = new HashMap<>();

	private final Set<String> PANL_CODE_OR_FIELDS = new HashSet<>();
	private final Set<String> PANL_CODE_OR_FIELDS_ALWAYS = new HashSet<>();
	private final Set<String> PANL_CODE_OR_SEPARATOR_FIELDS = new HashSet<>();
	private final Set<String> PANL_CODE_RANGE_FIELDS = new HashSet<>();


	private final JSONArray validUrls;

	private String panlParamQuery;
	private String panlParamSort;
	private String panlParamPage;
	private String panlParamNumRows;
	private String panlParamQueryOperand;
	private String panlParamPassThrough;

	private String facetSortFieldsIndex;

	private String formQueryRespondTo;

	private String solrDefaultQueryOperand;
	private int solrFacetLimit;
	private String panlLpseOrder;
	private List<String> panlLpseOrderList = new ArrayList<>();

	private final Set<String> LPSE_URI_CODES = new HashSet<>();
	private final Set<String> LPSE_IGNORED_URI_CODES = new HashSet<>();
	private final List<BaseField> lpseFields = new ArrayList<>();
	private final List<PanlRangeFacetField> rangeFields = new ArrayList<>();
	private final Set<String> LPSE_METADATA = new HashSet<>();

	private final Map<String, List<String>> resultFieldsMap = new HashMap<>();

	/**
	 * <p>The list of all the named Solr facet fields - Note that this is not
	 * used as a FieldSet - it is all of the Solr fields that will be faceted on.</p>
	 */
	private String[] solrFacetFields;

	private final List<String> lpseCodeSortFields = new ArrayList<>();

	private final JSONObject solrFieldToPanlNameLookup = new JSONObject();

	private final Map<String, BaseField> lpseFieldLookup = new HashMap<>();
	/**
	 * <p>Are there any OR facet fields registered for this collection</p>
	 */
	private boolean hasOrFacetFields = false;
	protected boolean panlIncludeSingleFacets;
	protected boolean panlIncludeSameNumberFacets;

	/**
	 * <p>Used as a holder for panl LPSE codes which are mandatory to have in the
	 * panl.lpse.order property.  Mandatory properties are added, then removed when the property is set.</p>
	 */
	private final Map<String, String> MANDATORY_LPSE_ORDER_FIELDS = new HashMap<>();
	private boolean highlight;

	/**
	 * <p>Instantiate the Collection properties which maps to one Panl collection
	 * URI path and multiple fieldsets.</p>
	 *
	 * @param solrCollection The Solr collection to connect to - this is used for debugging and logging purposes
	 * @param panlCollectionUri The Panl collection URI that this collection is registered to - this is used for
	 * 			debugging and logging purposes
	 * @param properties The panl_collection_uri.properties object to generate the configuration from
	 *
	 * @throws PanlServerException If there was an error in parsing, there are missing, or there was an invalid
	 * 			property.
	 */
	public CollectionProperties(String solrCollection, String panlCollectionUri,
				Properties properties) throws PanlServerException {
		this.solrCollection = solrCollection;
		this.panlCollectionUri = panlCollectionUri;
		this.properties = properties;

		parseDefaultProperties();

		parseFacetFields();
		parseFields();
		parseResultFields();
		parseSortFields();
		parseLpseOrder();
		parseLpseIgnore();
		parseFacetSortFields();


		// Generate some static information
		JSONArray jsonArray = new JSONArray();
		for(String resultFieldsName : getResultFieldsNames()) {
			jsonArray.put("/" + solrCollection + "/" + resultFieldsName + "/");
		}

		this.validUrls = jsonArray;

		// now for the solr field to panl name lookup
		for(PanlFacetField facetField : FACET_FIELDS) {
			solrFieldToPanlNameLookup.put(facetField.getSolrFieldName(), facetField.getPanlFieldName());
		}

		for(PanlField field : NON_FACET_FIELDS) {
			solrFieldToPanlNameLookup.put(field.getSolrFieldName(), field.getPanlFieldName());
		}

		// finally - do we have any or fields
		for(String key : lpseFieldLookup.keySet()) {
			BaseField baseField = lpseFieldLookup.get(key);
			if (baseField instanceof PanlOrFacetField) {
				this.hasOrFacetFields = true;
				break;
			}
		}
	}

	private void parseSortFields() throws PanlServerException {
		String sortFieldsTemp = properties.getProperty(PROPERTY_KEY_PANL_SORT_FIELDS, "");
		//		int sortOrder = 0;
		for(String sortField : sortFieldsTemp.split(",")) {
			// A sort field can either be a field, or a facet field
			String lpseCode = null;
			if (SOLR_NAME_TO_FIELD_MAP.containsKey(sortField)) {
				lpseCode = SOLR_NAME_TO_FIELD_MAP.get(sortField).getLpseCode();
			} else if (SOLR_NAME_TO_FACET_FIELD_MAP.containsKey(sortField)) {
				lpseCode = SOLR_NAME_TO_FACET_FIELD_MAP.get(sortField).getLpseCode();
			}

			if (null == lpseCode) {
				LOGGER.warn(
							"[ Solr/Panl '{}/{}' ] Sort Fields - '{}' Could not look up the Panl LPSE code for Solr field name '{}', ignoring...",
							solrCollection, panlCollectionUri, PROPERTY_KEY_PANL_SORT_FIELDS, sortField);
			} else {
				LOGGER.info("[ Solr/Panl '{}/{}' ] Sort Fields - adding Panl LPSE code '{}' for Solr field name '{}'.",
							solrCollection, panlCollectionUri, lpseCode, sortField);
				lpseCodeSortFields.add(lpseCode);
				PanlSortField panlSortField = new PanlSortField(
							lpseCode,
							PROPERTY_KEY_PANL_SORT_FIELDS,
							properties,
							solrCollection,
							panlCollectionUri);

				LPSE_CODE_TO_SORT_FIELD_MAP.put(lpseCode, panlSortField);
				SOLR_NAME_TO_SORT_FIELD_MAP.put(sortField, panlSortField);
			}
		}
	}

	/**
	 * <p>Parse the default properties for a collection, which affect all search
	 * queries.</p>
	 *
	 * <p>Some parameters have default values: </p>
	 *
	 * <ul>
	 *   <li><code>panl.include.single.facets</code> - default value <code>false</code></li>
	 *   <li><code>panl.include.same.number.facets</code> - default value <code>false</code></li>
	 *   <li><code>solr.facet.min.count</code> - default value <code>1</code></li>
	 *   <li><code>solr.numrows.default</code> - default value <code>10</code></li>
	 *   <li><code>solr.facet.limit</code> - default value <code>100</code></li>
	 * </ul>
	 *
	 * @throws PanlServerException If a mandatory property was not found, or could not be adequately parsed
	 */
	private void parseDefaultProperties() throws PanlServerException {
		this.panlIncludeSingleFacets = properties
			.getProperty(PROPERTY_KEY_PANL_INCLUDE_SINGLE_FACETS, "false")
			.equals("true");

		this.panlIncludeSameNumberFacets = properties
			.getProperty(PROPERTY_KEY_PANL_INCLUDE_SAME_NUMBER_FACETS, "false")
			.equals("true");

		this.formQueryRespondTo = properties.getProperty(PROPERTY_KEY_PANL_FORM_QUERY_RESPONDTO, "q");

		this.facetMinCount = PropertyHelper.getIntProperty(properties, PROPERTY_KEY_SOLR_FACET_MIN_COUNT, 1);
		this.highlight = properties.getProperty(PROPERTY_KEY_SOLR_HIGHLIGHT, "false").equals("true");
		this.numResultsPerPage = PropertyHelper.getIntProperty(properties, PROPERTY_KEY_SOLR_NUMROWS_DEFAULT, 10);
		this.numResultsLookahead = PropertyHelper.getIntProperty(properties, PROPERTY_KEY_SOLR_NUMROWS_LOOKAHEAD, 5);
		this.solrFacetLimit = PropertyHelper.getIntProperty(properties, PROPERTY_KEY_SOLR_FACET_LIMIT, 100);


		this.lpseLength = PropertyHelper.getIntProperty(properties, PROPERTY_KEY_PANL_LPSE_LENGTH, null);
		if (null == lpseLength) {
			throw new PanlServerException(
						"MANDATORY PROPERTY MISSING: Could not find the 'panl.lpse.length' property in the '" + this.solrCollection + "'.panl.properties file.'");
		}

		// TODO - check whether this is the best possible default to get the most amount of results...
		this.solrDefaultQueryOperand = properties.getProperty(PROPERTY_KEY_SOLR_DEFAULT_QUERY_OPERAND, "+");
		if (!(this.solrDefaultQueryOperand.equals("+") || this.solrDefaultQueryOperand.equals("-"))) {
			throw new PanlServerException("Property solr.default.query.operand __MUST__ be one of '+', or '-'.");
		}

		LPSE_METADATA.add(this.solrDefaultQueryOperand);

		this.panlParamQuery = initialiseStringProperty(PROPERTY_KEY_PANL_PARAM_QUERY, true);
		lpseFieldLookup.put(this.panlParamQuery,
					new PanlQueryField(
						panlParamQuery,
						PROPERTY_KEY_PANL_PARAM_QUERY,
						properties,
						solrCollection,
						panlCollectionUri));

		this.panlParamSort = initialiseStringProperty(PROPERTY_KEY_PANL_PARAM_SORT, true);
		lpseFieldLookup.put(this.panlParamSort,
					new PanlSortField(
						panlParamSort,
						PROPERTY_KEY_PANL_PARAM_SORT,
						properties,
						solrCollection,
						panlCollectionUri));

		this.panlParamPage = initialiseStringProperty(PROPERTY_KEY_PANL_PARAM_PAGE, true);
		lpseFieldLookup.put(this.panlParamPage,
					new PanlPageNumField(
						panlParamPage,
						PROPERTY_KEY_PANL_PARAM_PAGE,
						properties,
						solrCollection,
						panlCollectionUri));

		this.panlParamNumRows = initialiseStringProperty(PROPERTY_KEY_PANL_PARAM_NUMROWS, true);
		lpseFieldLookup.put(this.panlParamNumRows,
					new PanlNumRowsField(
						panlParamNumRows,
						PROPERTY_KEY_PANL_PARAM_NUMROWS,
						properties,
						solrCollection,
						panlCollectionUri));

		this.panlParamQueryOperand = initialiseStringProperty(PROPERTY_KEY_PANL_PARAM_QUERY_OPERAND, true);
		lpseFieldLookup.put(this.panlParamQueryOperand,
					new PanlQueryOperandField(
						panlParamQueryOperand,
						PROPERTY_KEY_PANL_PARAM_QUERY_OPERAND,
						properties,
						solrCollection,
						panlCollectionUri));

		this.panlParamPassThrough = initialiseStringProperty(PROPERTY_KEY_PANL_PARAM_PASSTHROUGH, false);
		if (null != panlParamPassThrough) {
			lpseFieldLookup.put(this.panlParamPassThrough,
						new PanlPassThroughField(
							panlParamPassThrough,
							PROPERTY_KEY_PANL_PARAM_PASSTHROUGH,
							properties,
							solrCollection,
							panlCollectionUri));
		}
	}

	/**
	 * <p>Initialise a string property from the properties file.</p>
	 *
	 * <p>This will look up the property from the properties.  If it doesn't
	 * exist and is a mandatory property, it will throw an exception.  If the property isn't mandatory, then it will
	 * return null,</p>
	 *
	 * <p>If the hasPrefixSuffix parameter is set, then it will also look for a
	 * property with the key of <code>propertyName + ".prefix"</code> and if it exists, it will add it to the prefix map.
	 * If there is a property with the key of <code>propertyName + ".suffix</code>, then it will be added to the suffix
	 * map.</p>
	 *
	 * <p>Finally, if the property is found it will be added to the metadatMap.</p>
	 *
	 * @param propertyName The property name to look up
	 * @param isMandatory Whether this is a mandatory property - if it is, and it doesn't exist, then this will throw a
	 * 			PanlServerException
	 *
	 * @return the initialised property, or null if it doesn't exist
	 *
	 * @throws PanlServerException If a mandatory property was not found
	 */
	private String initialiseStringProperty(String propertyName, boolean isMandatory) throws PanlServerException {
		String panlPropertyValue = properties.getProperty(propertyName, null);
		if (null == panlPropertyValue) {
			if (isMandatory) {
				throw new PanlServerException(
							"MANDATORY PROPERTY MISSING: Could not find the '" +
										propertyName +
										"' property in the '" +
										this.solrCollection +
										"' Panl properties file.'");
			} else {
				return (null);
			}
		}

		MANDATORY_LPSE_ORDER_FIELDS.put(panlPropertyValue, propertyName);

		LPSE_METADATA.add(panlPropertyValue);
		return (panlPropertyValue);
	}

	/**
	 * <p>Parse the properties files and extract all properties that begin with
	 * the panl facet property key.</p>
	 *
	 * <p> See the
	 * {@link  CollectionProperties#PROPERTY_KEY_PANL_FACET PROPERTY_KEY_PANL_FACET} static String for the panl prefix
	 * property</p>
	 *
	 * @throws PanlServerException If there was an error looking up the properties, or with the found property and its
	 * 			associated values
	 */
	private void parseFacetFields() throws PanlServerException {
		for(String panlFieldKey : PropertyHelper.getPropertiesByPrefix(properties, PROPERTY_KEY_PANL_FACET)) {
			String lpseCode = panlFieldKey.substring(panlFieldKey.lastIndexOf(".") + 1);
			// now we need to know the type of the facetField
			String solrFieldType = properties.getProperty(PROPERTY_KEY_PANL_TYPE + lpseCode, null);
			boolean isOrFacet = properties.getProperty(PROPERTY_KEY_PANL_OR_FACET + lpseCode, "false").equals("true");
			boolean isRangeFacet = properties.getProperty(PROPERTY_KEY_PANL_RANGE_FACET + lpseCode, "false").equals("true");

			PanlFacetField facetField;
			if (TYPE_SOLR_DATE_POINT_FIELD.equals(solrFieldType)) {
				facetField = new PanlDateRangeFacetField(lpseCode, panlFieldKey, properties, solrCollection, panlCollectionUri,
							lpseLength);
				LPSE_CODE_DATE_RANGE_FACET_MAP.put(lpseCode, (PanlDateRangeFacetField) facetField);
			} else if (TYPE_SOLR_BOOL_FIELD.equals(solrFieldType)) {
				facetField = new PanlBooleanFacetField(lpseCode, panlFieldKey, properties, solrCollection, panlCollectionUri,
							lpseLength);
				LPSE_CODE_BOOLEAN_FACET_MAP.put(lpseCode, (PanlBooleanFacetField) facetField);
			} else if (isOrFacet) {
				facetField = new PanlOrFacetField(lpseCode, panlFieldKey, properties, solrCollection, panlCollectionUri, lpseLength);
				PANL_CODE_OR_FIELDS.add(lpseCode);
				PanlOrFacetField panlOrFacetField = (PanlOrFacetField) facetField;
				if (panlOrFacetField.getIsAlwaysOr()) {
					PANL_CODE_OR_FIELDS_ALWAYS.add(lpseCode);
				}

				if(panlOrFacetField.getOrSeparator() != null) {
					PANL_CODE_OR_SEPARATOR_FIELDS.add(lpseCode);
				}
			} else if (isRangeFacet) {
				facetField = new PanlRangeFacetField(lpseCode, panlFieldKey, properties, solrCollection, panlCollectionUri,
							lpseLength);
				LPSE_CODE_RANGE_FACET_MAP.put(lpseCode, (PanlRangeFacetField) facetField);
				PANL_CODE_RANGE_FIELDS.add(lpseCode);
			} else {
				facetField = new PanlFacetField(lpseCode, panlFieldKey, properties, solrCollection, panlCollectionUri,
							lpseLength);
			}

			String lpseWhen = properties.getProperty(PROPERTY_KEY_PANL_WHEN + lpseCode);
			if (null != lpseWhen) {
				String[] splits = lpseWhen.split(",");
				for(String split : splits) {
					String trim = split.trim();

					if (!trim.isEmpty()) {
						if (!LPSE_CODE_WHEN_MAP.containsKey(lpseCode)) {
							LPSE_CODE_WHEN_MAP.put(lpseCode, new HashSet<>());
						}

						LPSE_CODE_WHEN_MAP.get(lpseCode).add(trim);
					}
				}
			}
			// we need to determine whether it is an OR facet

			FACET_FIELDS.add(facetField);
			LPSE_FACET_FIELDS.add(facetField.getLpseCode());

			lpseFieldLookup.put(lpseCode, facetField);

			LPSE_CODE_TO_FACET_FIELD_MAP.put(facetField.getLpseCode(), facetField);
			SOLR_NAME_TO_FACET_FIELD_MAP.put(facetField.getSolrFieldName(), facetField);
			SOLR_NAME_TO_LPSE_CODE_MAP.put(facetField.getSolrFieldName(), facetField.getLpseCode());
		}

		List<String> temp = new ArrayList<>();
		for(PanlFacetField facetField : FACET_FIELDS) {
			// we do not ever return the Date facet - no point in enlarging the
			// response object for no purpose
			if (!(facetField instanceof PanlDateRangeFacetField)) {
				temp.add(facetField.getSolrFieldName());
			}
		}

		this.solrFacetFields = temp.toArray(new String[0]);
	}

	/**
	 * <p>Parse the fields - these are not able to be be used as a facet, however
	 * it will allow sort ordering.</p>
	 *
	 * @throws PanlServerException If there was an error parsing the field
	 */
	private void parseFields() throws PanlServerException {
		for(String panlFieldKey : PropertyHelper.getPropertiesByPrefix(properties, PROPERTY_KEY_PANL_FIELD)) {
			String lpseCode = panlFieldKey.substring(panlFieldKey.lastIndexOf(".") + 1);
			PanlField field = new PanlField(lpseCode, panlFieldKey, properties, solrCollection, panlCollectionUri,
						lpseLength);
			NON_FACET_FIELDS.add(field);
			LPSE_FIELDS.add(field.getLpseCode());
			lpseFieldLookup.put(lpseCode, field);

			LPSE_CODE_TO_FIELD_MAP.put(field.getLpseCode(), field);
			SOLR_NAME_TO_FIELD_MAP.put(field.getSolrFieldName(), field);
		}
	}

	/**
	 * <p>Parse the LPSE order</p>
	 *
	 * @throws PanlServerException if the panl.lpse.order does not exist
	 */
	private void parseLpseOrder() throws PanlServerException {
		panlLpseOrder = properties.getProperty(PROPERTY_KEY_PANL_LPSE_ORDER, null);
		if (null == panlLpseOrder) {
			throw new PanlServerException("Could not find the MANDATORY property " + PROPERTY_KEY_PANL_LPSE_ORDER);
		}

		for(String lpseCode : panlLpseOrder.split(",")) {
			lpseCode = lpseCode.trim();
			if (lpseFieldLookup.containsKey(lpseCode)) {
				lpseFields.add(lpseFieldLookup.get(lpseCode));
				LPSE_URI_CODES.add(lpseCode);
			}

			panlLpseOrderList.add(lpseCode);

			if (!LPSE_FACET_FIELDS.contains(lpseCode) &&
						    !LPSE_FIELDS.contains(lpseCode) &&
						    !LPSE_METADATA.contains(lpseCode)) {
				LOGGER.warn(
							"Found a panl code of '{}' in the " + PROPERTY_KEY_PANL_LPSE_ORDER + " property, yet it is not a defined field.  This will be ignored...",
							lpseCode);
			}

			MANDATORY_LPSE_ORDER_FIELDS.remove(lpseCode);
		}

		boolean missingMandatoryLpseCode = false;
		// we also need to ensure that the default parameters are in the lpse order
		for(String key : MANDATORY_LPSE_ORDER_FIELDS.keySet()) {
			LOGGER.error("__MUST__ have key of '{}' in property '{}', this is set by the property '{}'.",
						key,
						PROPERTY_KEY_PANL_LPSE_ORDER,
						MANDATORY_LPSE_ORDER_FIELDS.get(key));
			missingMandatoryLpseCode = true;
		}

		if (missingMandatoryLpseCode) {
			throw new PanlServerException(
						"Missing mandatory LPSE codes in the " + PROPERTY_KEY_PANL_LPSE_ORDER + " property.");
		}

	}

	/**
	 * <p>Parse the LPSE ignore comma separated list.  LPSE ignore codes will
	 * still be able to be searched on, however they won't be returned in the available </p>
	 *
	 * <p>If the LPSE code to be ignored is not in the LPSE order, it will
	 * generate a warning message.</p>
	 */
	private void parseLpseIgnore() {
		String panlLpseIgnore = properties.getProperty(PROPERTY_KEY_PANL_LPSE_IGNORE, "");
		for(String ignore : panlLpseIgnore.split(",")) {
			String trimmed = ignore.trim();
			if (trimmed.isBlank()) {
				continue;
			}

			if (LPSE_URI_CODES.contains(trimmed)) {
				LPSE_IGNORED_URI_CODES.add(trimmed);
			} else {
				LOGGER.warn("Attempting to ignore a facet with code '{}' which was not defined by the lpse order property '{}'",
							trimmed, PROPERTY_KEY_PANL_LPSE_ORDER);
			}
		}
	}

	private void parseResultFields() throws PanlServerException {
		List<String> resultFieldProperties = PropertyHelper.getPropertiesByPrefix(properties,
					PROPERTY_KEY_PANL_RESULTS_FIELDS);
		for(String resultFieldProperty : resultFieldProperties) {
			addResultsFields(resultFieldProperty.substring(PROPERTY_KEY_PANL_RESULTS_FIELDS.length()),
						properties.getProperty(resultFieldProperty));
		}

		// there must always be a default field
		if (!resultFieldsMap.containsKey(FIELDSETS_DEFAULT)) {
			LOGGER.warn("[ Solr/Panl '{}/{}' ] Missing default field set, adding one which will return all fields.",
						solrCollection, panlCollectionUri);
			resultFieldsMap.put(FIELDSETS_DEFAULT, new ArrayList<>());
		}

		if (resultFieldsMap.containsKey(FIELDSETS_EMPTY)) {
			LOGGER.warn(
						"[ Solr/Panl '{}/{}' ] 'empty' fieldset defined.  This will be ignored, and empty fieldset __ALWAYS__ returns no fields.",
						solrCollection, panlCollectionUri);
		}
		resultFieldsMap.put(FIELDSETS_EMPTY, null);
	}

	private void addResultsFields(String resultFieldsName, String resultFields) throws PanlServerException {
		if (resultFieldsMap.containsKey(resultFieldsName)) {
			throw new PanlServerException("panl.results.fields.'" + resultFieldsName + "' is already defined.");
		}

		LOGGER.info("[ Solr/Panl '{}/{}' ] Adding result fields with key '{}', and fields '{}'.", solrCollection,
					panlCollectionUri, resultFieldsName, resultFields);
		List<String> fields = new ArrayList<>();
		for(String resultField : resultFields.split(",")) {
			fields.add(resultField.trim());
		}

		resultFieldsMap.put(resultFieldsName, fields);
	}


	public String getSolrCollection() {
		return (solrCollection);
	}

	public int getLpseLength() {
		return (lpseLength);
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

	public String getPanlParamPassThrough() {
		return (panlParamPassThrough);
	}

	public String getPanlParamPage() {
		return (panlParamPage);
	}

	public String getPanlParamNumRows() {
		return (panlParamNumRows);
	}

	public String getSolrDefaultQueryOperand() {
		if (solrDefaultQueryOperand.equals("-")) {
			return (SOLR_DEFAULT_QUERY_OPERAND_OR);
		} else {
			return (SOLR_DEFAULT_QUERY_OPERAND_AND);
		}
	}

	public String getDefaultQueryOperand() {
		return (solrDefaultQueryOperand);
	}

	public List<String> getResultFieldsNames() {
		return (new ArrayList<>(resultFieldsMap.keySet()));
	}

	public List<String> getResultFieldsForName(String name) {
		return (resultFieldsMap.get(name));
	}

	public boolean isValidResultFieldsName(String name) {
		return (resultFieldsMap.containsKey(name));
	}

	/**
	 * <p>Get the valid URLs as a JSON array.</p>
	 *
	 * @return The valid URLs as a JSON array
	 */
	public JSONArray getValidUrls() {
		return (validUrls);
	}

	/**
	 * <p>Set the minimum count of facets that need to be returned.  If there are
	 * any OR facets, <strong>__AND__</strong> no other lpseCodes are passed through, then return 0 for this field only -
	 * i.e. return all of the rows. If there is a LPSE code passed through then set value the value to the variable
	 * facetMinCount (which comes from the properties file.</p>
	 *
	 * @param solrQuery The Solr query object to set the facet mincounts on
	 * @param panlTokenMap The passed in tokens
	 */
	public void setFacetMinCounts(SolrQuery solrQuery, Map<String, List<LpseToken>> panlTokenMap) {
		// always set the facet min count to the property

		solrQuery.setFacetMinCount(facetMinCount);

		if (panlTokenMap.isEmpty() || PANL_CODE_OR_FIELDS.isEmpty()) {
			// if we haven't done any queries or we don't have any or facets
			return;
		}

		// At this point we have some tokens in the panlTokenMap, we need to go
		// through the OR fields and set specific facet mincounts.
		//
		// If and ONLY IF the OR facet exists in the panlToken map, then continue 
		// and there are no other facets selected, then set this OR facet to 0  

		for(String panlCodeOrField : PANL_CODE_OR_FIELDS) {
			// if there is
			if (
						(panlTokenMap.containsKey(panlCodeOrField) && panlTokenMap.size() == 1) ||
									(PANL_CODE_OR_FIELDS_ALWAYS.contains(panlCodeOrField) && panlTokenMap.containsKey(panlCodeOrField))) {
				// we are faceting on this 
				solrQuery.add(
							"f." +
										LPSE_CODE_TO_FACET_FIELD_MAP.get(panlCodeOrField).getSolrFieldName() +
										".facet.mincount",
							"0");
			}
		}
	}

	/**
	 * @return The minimum value for the facet count
	 */
	public int getFacetMinCount() {
		return (facetMinCount);
	}

	public int getNumResultsPerPage() {
		return numResultsPerPage;
	}

	public int getNumResultsLookahead() {
		return numResultsLookahead;
	}

	/**
	 * <p>Get the facet fields that should be passed through to Solr, ensuring
	 * that any hierarchical lpse codes filter out those that are not supposed to be retrieved.</p>
	 *
	 * @param lpseTokens The current active LPSE tokens
	 *
	 * @return The array of solr fields to facet on
	 */
	public String[] getWhenSolrFacetFields(List<LpseToken> lpseTokens) {
		if (LPSE_CODE_WHEN_MAP.isEmpty()) {
			return (solrFacetFields);
		}

		Set<String> activeLpseCodes = new HashSet<>();
		for(LpseToken lpseToken : lpseTokens) {
			activeLpseCodes.add(lpseToken.getLpseCode());
		}

		List<String> returnedFacetFields = new ArrayList<>();
		for(String solrFacetFieldName : solrFacetFields) {
			String lpseCode = SOLR_NAME_TO_LPSE_CODE_MAP.get(solrFacetFieldName);
			if (null == lpseCode) {
				// shouldn't happen, but doesn't matter
				returnedFacetFields.add(solrFacetFieldName);
			} else {
				// now we need to lookup the lpseCode in the WHEN map
				if (LPSE_CODE_WHEN_MAP.containsKey(lpseCode)) {
					// do we have the 'when' code in the token map?
					for(String s : LPSE_CODE_WHEN_MAP.get(lpseCode)) {
						if (activeLpseCodes.contains(s)) {
							returnedFacetFields.add(solrFacetFieldName);
							break;
						}
					}
				} else {
					returnedFacetFields.add(solrFacetFieldName);
				}
			}
		}
		return (returnedFacetFields.toArray(new String[0]));
	}

	/**
	 * <p>Return whether this is a valid sort field.</p>
	 *
	 * @param lpseCode The lpse code to look up
	 *
	 * @return whether this is a valid sort code
	 */
	public boolean hasSortField(String lpseCode) {
		return (LPSE_CODE_TO_SORT_FIELD_MAP.containsKey(lpseCode));
	}

	public String getSolrFieldNameFromLpseCode(String lpseCode) {
		if (LPSE_CODE_TO_FACET_FIELD_MAP.containsKey(lpseCode)) {
			return (LPSE_CODE_TO_FACET_FIELD_MAP.get(lpseCode).getSolrFieldName());
		} else if (LPSE_CODE_TO_FIELD_MAP.containsKey(lpseCode)) {
			return (LPSE_CODE_TO_FIELD_MAP.get(lpseCode).getSolrFieldName());
		} else if (LPSE_CODE_TO_SORT_FIELD_MAP.containsKey(lpseCode)) {
			return (LPSE_CODE_TO_SORT_FIELD_MAP.get(lpseCode).getSolrFieldName());
		}
		return (null);
	}

	public boolean hasFacetCode(String lpseCode) {
		return (LPSE_CODE_TO_FACET_FIELD_MAP.containsKey(lpseCode));
	}

	public String getPanlCodeFromSolrFacetFieldName(String name) {
		if (SOLR_NAME_TO_FACET_FIELD_MAP.containsKey(name)) {
			return (SOLR_NAME_TO_FACET_FIELD_MAP.get(name).getLpseCode());
		}

		return (null);
	}

	/**
	 * <p>Return he Panl name from the Solr field name.  This will look at both
	 * the facets and fields.</p>
	 *
	 * @param solrFieldName The Solr field name to lookup
	 *
	 * @return The Panl name that matches the Solr field
	 */
	public String getPanlNameFromSolrFieldName(String solrFieldName) {
		if (SOLR_NAME_TO_FACET_FIELD_MAP.containsKey(solrFieldName)) {
			return (SOLR_NAME_TO_FACET_FIELD_MAP.get(solrFieldName).getPanlFieldName());
		} else if (SOLR_NAME_TO_FIELD_MAP.containsKey(solrFieldName)) {
			return (SOLR_NAME_TO_FIELD_MAP.get(solrFieldName).getPanlFieldName());
		}

		return (null);
	}

	/**
	 * <p>Return whether the passed in LPSE code is an OR facet field.</p>
	 *
	 * @param lpseCode The LPSE code to check
	 * @return Whether the LPSE code field is an OR facet field
	 */
	public boolean getIsOrFacetField(String lpseCode) {
		return (PANL_CODE_OR_FIELDS.contains(lpseCode));
	}

	/**
	 * <p>Return whether the passed in LPSE code has an OR separator string.</p>
	 *
	 * @param lpseCode The LPSE code to check
	 * @return Whether the LPSE code field has an  OR separator
	 */
	public boolean getIsOrSeparatorFacetField(String lpseCode) {
		return (PANL_CODE_OR_SEPARATOR_FIELDS.contains(lpseCode));
	}

	/**
	 * <p>Return whether the passed in LPSE code is a RANGE facet field.</p>
	 *
	 * @param lpseCode The LPSE code to check
	 * @return Whether the LPSE code field is a RANGE facet field
	 */
	public boolean getIsRangeFacetField(String lpseCode) {
		return (PANL_CODE_RANGE_FIELDS.contains(lpseCode));
	}

	/**
	 * <p>Return the Panl field name (the human-readable name) from the Panl LPSE
	 * code, else return null if the LPSE code does not exist.</p>
	 *
	 * @param lpseCode The LPSE code to look up
	 *
	 * @return The Panl human-readable name for the LPSE code, or null if it does
	 * not exist.
	 */
	public String getPanlNameFromPanlCode(String lpseCode) {
		if (LPSE_CODE_TO_FACET_FIELD_MAP.containsKey(lpseCode)) {
			return (LPSE_CODE_TO_FACET_FIELD_MAP.get(lpseCode).getPanlFieldName());
		} else if (LPSE_CODE_TO_FIELD_MAP.containsKey(lpseCode)) {
			return (LPSE_CODE_TO_FIELD_MAP.get(lpseCode).getPanlFieldName());
		}
		return (null);
	}

	/**
	 * <p>Return the Panl LPSE codes for the fields or facets which are available
	 * to sort the results with.</p>
	 *
	 * @return The list of Panl LPSE codes that are available for sorting
	 */
	public List<String> getSortFieldLpseCodes() {
		return (lpseCodeSortFields);
	}

	/**
	 * <p>Return the Solr facet limit - i.e. the maximum number of facets for a
	 * field - this maps to the <code>solr.facet.limit</code> in the
	 * <code>collection.panl.properties</code> file and the <code>facet.limit</code>
	 * Solr query parameter.</p>
	 *
	 * <p><strong>NOTE:</strong> This defaults to 100 if not set.</p>
	 *
	 * @return The Solr facet limit.
	 */
	public int getSolrFacetLimit() {
		return (solrFacetLimit);
	}

	public JSONObject getSolrFieldToPanlNameLookup() {
		return (solrFieldToPanlNameLookup);
	}

	public List<BaseField> getLpseFields() {
		return (lpseFields);
	}

	public BaseField getLpseField(String lpseCode) {
		return (lpseFieldLookup.get(lpseCode));
	}

	public boolean getHasOrFacetFields() {
		return hasOrFacetFields;
	}

	/**
	 * <p>The URL parameter key to respond to for a text search term.</p>
	 *
	 * <p>In the below form, the URL parameter is <code>q</code> (i.e. the form
	 * field name.)</p>
	 *
	 * <pre>
	 * &lt;form method="GET"&gt;
	 *   &lt;labe&gt;&lt;input type="text" name="q" /&gt;&lt;/label&gt;
	 *   &lt;button type="submit"&gt;Search&lt;/button&gt;
	 * &lt;/form&gt;
	 * </pre>
	 *
	 * <p>This parameter will change the name of the input that is picked up, e.g.
	 * <code>panl.form.query.respondto=search</code> will require a named input
	 * as:</p>
	 *
	 * <pre>
	 * &lt;form method="GET"&gt;
	 *   &lt;label&gt;&lt;input type="text" name="search" /&gt;&lt;/label&gt;
	 *   &lt;button type="submit"&gt;Search&lt;/button&gt;
	 * &lt;/form&gt;
	 * </pre>
	 *
	 * @return The URL parameter key to respond to for a text search term
	 */
	public String getFormQueryRespondTo() {
		return (formQueryRespondTo);
	}

	/**
	 * <p>Get the Panl collection URI for this collection</p>
	 *
	 * @return The Panl collection URI for this collection
	 */
	public String getPanlCollectionUri() {
		return panlCollectionUri;
	}

	/**
	 * <p>Return whether the LPSE code should be ignored in the active and
	 * available facets.</p>
	 *
	 * @param lpseCode The LPSE code to lookup
	 *
	 * @return Whether this LPSE code should be ignored
	 */
	public boolean getIsIgnoredLpseCode(String lpseCode) {
		return (LPSE_IGNORED_URI_CODES.contains(lpseCode));
	}

	public boolean getPanlIncludeSingleFacets() {
		return (panlIncludeSingleFacets);
	}

	public boolean getPanlIncludeSameNumberFacets() {
		return (panlIncludeSameNumberFacets);
	}

	public String getPanlLpseOrder() {
		return (panlLpseOrder);
	}

	public boolean getHighlight() {
		return (highlight);
	}

	public List<PanlDateRangeFacetField> getDateRangeFacetFields() {
		return (List.of(LPSE_CODE_DATE_RANGE_FACET_MAP.values().toArray(new PanlDateRangeFacetField[0])));
	}

	public boolean getIsSuppressedRangeFacet(String lpseCode) {
		if (LPSE_CODE_RANGE_FACET_MAP.containsKey(lpseCode)) {
			return (LPSE_CODE_RANGE_FACET_MAP.get(lpseCode).getRangeSuppress());
		}
		return (false);
	}

	public Set<String> getLpseWhenCode(String lpseCode) {
		return (LPSE_CODE_WHEN_MAP.get(lpseCode));
	}

	public List<PanlFacetField> getFacetIndexSortFields() {
		return (FACET_INDEX_SORT_FIELDS);
	}

	private void parseFacetSortFields() {
		for(PanlFacetField facetField : FACET_FIELDS) {
			if (facetField.getIsFacetSortByIndex()) {
				FACET_INDEX_SORT_FIELDS.add(facetField);
			}
		}
	}

	/**
	 * <p>Get the list of the LPSE codes</p>
	 *
	 * @return The list of LPSE codes
	 */
	public List<String> getPanlLpseOrderList() {
		return (panlLpseOrderList);
	}
}

