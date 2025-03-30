package com.synapticloop.panl.server.handler.properties;

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
//import com.synapticloop.panl.server.handler.field.BaseField;
//import com.synapticloop.panl.server.handler.field.FacetField;
//import com.synapticloop.panl.server.handler.field.MetaDataField;
import com.synapticloop.panl.server.handler.fielderiser.field.*;
import com.synapticloop.panl.server.handler.fielderiser.field.facet.*;
import com.synapticloop.panl.server.handler.fielderiser.field.param.*;
import com.synapticloop.panl.server.handler.helper.PropertyHelper;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.util.Constants;
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

	/**
	 * <p>The name of this Solr collection</p>
	 */
	private final String solrCollection;

	/**
	 * <p>The URI that this collection is bound to</p>
	 */
	private final String panlCollectionUri;

	/**
	 * <p>The <code>&lt;panl.collection.url&gt;.properties</code> properties that
	 * drive this Panl collection</p>
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
	 * <p>The maximum number of results returned per search/page</p>
	 */
	private int maxNumResultsPerPage;

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

	/**
	 * <p>This is the list of all facet fields that are registered with as sort
	 * fields panl to be sorted in the index (i.e. the value), rather than the count of the facet</p>
	 */
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
	/**
	 * <p>This is the unless map, which is keyed on the unless LPSE code, the values
	 * are the LPSE codes which will not appear if the unless LPSE code appears</p>
	 */
	private final Map<String, Set<String>> LPSE_CODE_UNLESS_MAP = new HashMap<>();
	private final Map<String, String> SOLR_NAME_TO_LPSE_CODE_MAP = new HashMap<>();

	private final Set<String> PANL_CODE_OR_FIELDS = new HashSet<>();
	private final Set<String> PANL_CODE_OR_FIELDS_ALWAYS = new HashSet<>();
	private final Set<String> PANL_CODE_MULTIVALUED_SEPARATOR_FIELDS = new HashSet<>();
	private final Set<String> PANL_CODE_RANGE_FIELDS = new HashSet<>();

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	//
	// QUERY/SEARCH KEYWORDS RELATED DATASTRUCTURES
	//
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	private final Map<String, String> SEARCH_LPSE_CODE_TO_SOLR_FIELD_MAP = new HashMap<>();
	private final Map<String, String> SEARCH_SOLR_FIELD_TO_LPSE_CODE_MAP = new HashMap<>();
	private final Map<String, String> SEARCH_LPSE_CODE_TO_PANL_NAME_MAP = new HashMap<>();
	private final Map<String, String> SEARCH_FIELDS_MAP = new LinkedHashMap<>();
	private final Map<String, String> SEARCH_CODES_MAP = new LinkedHashMap<>();
	private final Map<String, Integer> SEARCH_CODES_BOOST = new HashMap<>();

	/**
	 * <p>The valid URLs for this Panl collection - this is only used for
	 * returning within the 404 JSON response object.</p>
	 */
	private final JSONArray validUrls;

	/**
	 * <p>The LPSE query parameter that is used</p>
	 */
	private String panlParamQuery;

	/**
	 * <p>The LPSE sort parameter that is used</p>
	 */
	private String panlParamSort;

	/**
	 * <p>The LPSE page number parameter that is used</p>
	 */
	private String panlParamPage;

	/**
	 * <p>The LPSE number of rows to return parameter that is used</p>
	 */
	private String panlParamNumRows;

	/**
	 * <p>The LPSE parameter for the Solr query operand that is used</p>
	 */
	private String panlParamQueryOperand;

	/**
	 * <p>The LPSE passthrough parameter that is used</p>
	 */
	private String panlParamPassThrough;

	/**
	 * <p>The URL Parameter key that the Panl server will use for a keyword search
	 * on the indexed data.</p>
	 *
	 * <p><strong>Note:</strong> the following HTML form uses the query
	 * parameter name of <code>search</code>.</p>
	 *
	 * <pre>
	 * &lt;form method=&quot;GET&quot;&gt;
	 *   &lt;label&gt;&lt;input type=&quot;text&quot; name=&quot;search&quot; /&gt;&lt;/label&gt;
	 *   &lt;button type=&quot;submit&quot;&gt;Search&lt;/button&gt;
	 * &lt;/form&gt;
	 * </pre>
	 */
	private String formQueryRespondTo;

	/**
	 * <p>The default Solr query operand to be set</p>
	 */
	private String solrDefaultQueryOperand;

	/**
	 * <p>The default limit to set for the number of returned facets.</p>
	 */
	private int solrFacetLimit;

	/**
	 * <p>The LPSE order set as a property - this should be a comma separated list
	 * of the LPSE codes, in order.</p>
	 */
	private String panlLpseOrder;

	/**
	 * <p>The LPSE order as a List of LPSE codes.</p>
	 */
	private final List<String> panlLpseOrderList = new ArrayList<>();

	/**
	 * <p>The LPSE facet order set as a property - this should be a comma
	 * separated list of the LPSE codes, in order.</p>
	 */
	private String panlLpseFacetOrder;

	/**
	 * <p>The LPSE facet order as a List of LPSE codes.</p>
	 */
	private final JSONArray panlLpseFacetOrderJsonArray = new JSONArray();

	/**
	 * <p>A set of LPSE codes that have been found - used to ensure that there
	 * aren't duplicate LPSE codes registered.</p>
	 */
	private final Set<String> LPSE_URI_CODES = new HashSet<>();

	/**
	 * <p>The set of all LPSE codes that are ignored.</p>
	 */
	private final Set<String> LPSE_IGNORED_URI_CODES = new HashSet<>();
	private final List<BaseField> lpseFields = new ArrayList<>();
	private final List<PanlRangeFacetField> rangeFields = new ArrayList<>();
	private final Set<String> LPSE_METADATA = new HashSet<>();

	private final Map<String, List<String>> resultFieldsMap = new HashMap<>();

	/**
	 * <p>The list of all the named Solr facet fields - Note that this is not
	 * used as a FieldSet - it is all Solr fields that will be faceted on.</p>
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
	 * 		debugging and logging purposes
	 * @param properties The panl_collection_uri.properties object to generate the configuration from
	 *
	 * @throws PanlServerException If there was an error in parsing, there are missing, or there was an invalid
	 * 		property.
	 */
	public CollectionProperties(String solrCollection, String panlCollectionUri,
			Properties properties) throws PanlServerException {
		this.solrCollection = solrCollection;
		this.panlCollectionUri = panlCollectionUri;
		this.properties = properties;

		parseDefaultProperties();

		parseFacetFields();
		parseFields();
		parseSearchFields();


		parseResultFields();
		parseSortFields();
		parseLpseOrder();
		parseLpseFacetOrder();
		parseLpseIgnore();
		parseFacetSortFields();


		// Generate some static information
		JSONArray jsonArray = new JSONArray();
		for (String resultFieldsName : getResultFieldsNames()) {
			jsonArray.put("/" + solrCollection + "/" + resultFieldsName + "/");
		}

		this.validUrls = jsonArray;

		// now for the solr field to panl name lookup
		for (PanlFacetField facetField : FACET_FIELDS) {
			solrFieldToPanlNameLookup.put(facetField.getSolrFieldName(), facetField.getPanlFieldName());
		}

		for (PanlField field : NON_FACET_FIELDS) {
			solrFieldToPanlNameLookup.put(field.getSolrFieldName(), field.getPanlFieldName());
		}

		// finally - do we have any or fields
		for (String key : lpseFieldLookup.keySet()) {
			BaseField baseField = lpseFieldLookup.get(key);
			if (baseField instanceof PanlOrFacetField) {
				this.hasOrFacetFields = true;
				break;
			}
		}
	}

	/**
	 * <p>Parse and validate the sort fields, ensuring that they exist and are
	 * able to be sorted on.</p>
	 *
	 * @throws PanlServerException if there was an error parsing the sort fields
	 */
	private void parseSortFields() throws PanlServerException {
		String sortFieldsTemp = properties.getProperty(Constants.Property.Panl.PANL_SORT_FIELDS, "");

		for (String sortField : sortFieldsTemp.split(",")) {
			// trim any empty sort fields
			sortField = sortField.trim();

			if (sortField.isEmpty()) {
				continue;
			}
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
						solrCollection, panlCollectionUri, Constants.Property.Panl.PANL_SORT_FIELDS, sortField);
			} else {
				LOGGER.info("[ Solr/Panl '{}/{}' ] Sort Fields - adding Panl LPSE code '{}' for Solr field name '{}'.",
						solrCollection, panlCollectionUri, lpseCode, sortField);
				lpseCodeSortFields.add(lpseCode);
				PanlSortField panlSortField = new PanlSortField(
						lpseCode,
						Constants.Property.Panl.PANL_SORT_FIELDS,
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
				.getProperty(Constants.Property.Panl.PANL_INCLUDE_SINGLE_FACETS, Constants.BOOLEAN_FALSE_VALUE)
				.equals(Constants.BOOLEAN_TRUE_VALUE);

		this.panlIncludeSameNumberFacets = properties
				.getProperty(Constants.Property.Panl.PANL_INCLUDE_SAME_NUMBER_FACETS, Constants.BOOLEAN_FALSE_VALUE)
				.equals(Constants.BOOLEAN_TRUE_VALUE);

		this.formQueryRespondTo = properties.getProperty(Constants.Property.Panl.PANL_FORM_QUERY_RESPONDTO, "q");

		this.facetMinCount = PropertyHelper.getIntProperty(LOGGER, properties, Constants.Property.Solr.SOLR_FACET_MIN_COUNT, 1);
		this.highlight =
				properties.getProperty(Constants.Property.Solr.SOLR_HIGHLIGHT, Constants.BOOLEAN_FALSE_VALUE)
				          .equals(Constants.BOOLEAN_TRUE_VALUE);
		this.numResultsPerPage = PropertyHelper.getIntProperty(LOGGER, properties, Constants.Property.Solr.SOLR_NUMROWS_DEFAULT, 10);

		// we are setting the maximum number of results to default to the same
		// number for the results per page if it is not set
		this.maxNumResultsPerPage = PropertyHelper.getIntProperty(LOGGER, properties, Constants.Property.Solr.SOLR_NUMROWS_MAXIMUM,
				this.numResultsPerPage);

		this.numResultsLookahead = PropertyHelper.getIntProperty(LOGGER, properties, Constants.Property.Solr.SOLR_NUMROWS_LOOKAHEAD,
				5);
		this.solrFacetLimit = PropertyHelper.getIntProperty(LOGGER, properties, Constants.Property.Solr.SOLR_FACET_LIMIT, 100);


		this.lpseLength = PropertyHelper.getIntProperty(LOGGER, properties, Constants.Property.Panl.PANL_LPSE_LENGTH, null);
		if (null == lpseLength) {
			throw new PanlServerException(
					"MANDATORY PROPERTY MISSING: Could not find the 'panl.lpse.length' property in the '" + this.solrCollection + "'.panl.properties file.'");
		}

		// TODO - check whether this is the best possible default to get the most amount of results...
		this.solrDefaultQueryOperand = properties.getProperty(Constants.Property.Solr.SOLR_DEFAULT_QUERY_OPERAND, "+");
		if (!(this.solrDefaultQueryOperand.equals("+") || this.solrDefaultQueryOperand.equals("-"))) {
			throw new PanlServerException("Property solr.default.query.operand __MUST__ be one of '+', or '-'.");
		}

		LPSE_METADATA.add(this.solrDefaultQueryOperand);

		this.panlParamQuery = initialiseStringProperty(Constants.Property.Panl.PANL_PARAM_QUERY, true);
		lpseFieldLookup.put(this.panlParamQuery,
				new PanlQueryField(
						panlParamQuery,
						Constants.Property.Panl.PANL_PARAM_QUERY,
						properties,
						solrCollection,
						panlCollectionUri));

		this.panlParamSort = initialiseStringProperty(Constants.Property.Panl.PANL_PARAM_SORT, true);
		lpseFieldLookup.put(this.panlParamSort,
				new PanlSortField(
						panlParamSort,
						Constants.Property.Panl.PANL_PARAM_SORT,
						properties,
						solrCollection,
						panlCollectionUri));

		this.panlParamPage = initialiseStringProperty(Constants.Property.Panl.PANL_PARAM_PAGE, true);
		lpseFieldLookup.put(this.panlParamPage,
				new PanlPageNumField(
						panlParamPage,
						Constants.Property.Panl.PANL_PARAM_PAGE,
						properties,
						solrCollection,
						panlCollectionUri));

		this.panlParamNumRows = initialiseStringProperty(Constants.Property.Panl.PANL_PARAM_NUMROWS, true);
		lpseFieldLookup.put(this.panlParamNumRows,
				new PanlNumRowsField(
						panlParamNumRows,
						Constants.Property.Panl.PANL_PARAM_NUMROWS,
						properties,
						solrCollection,
						panlCollectionUri));

		this.panlParamQueryOperand = initialiseStringProperty(Constants.Property.Panl.PANL_PARAM_QUERY_OPERAND, true);
		lpseFieldLookup.put(this.panlParamQueryOperand,
				new PanlQueryOperandField(
						panlParamQueryOperand,
						Constants.Property.Panl.PANL_PARAM_QUERY_OPERAND,
						properties,
						solrCollection,
						panlCollectionUri));

		this.panlParamPassThrough = initialiseStringProperty(Constants.Property.Panl.PANL_PARAM_PASSTHROUGH, false);
		if (null != panlParamPassThrough) {
			lpseFieldLookup.put(this.panlParamPassThrough,
					new PanlPassThroughField(
							panlParamPassThrough,
							Constants.Property.Panl.PANL_PARAM_PASSTHROUGH,
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
	 * 		PanlServerException
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
	 * {@link  Constants.Property.Panl#PANL_FACET} static String for the panl prefix
	 * property</p>
	 *
	 * @throws PanlServerException If there was an error looking up the properties, or with the found property and its
	 * 		associated values
	 */
	private void parseFacetFields() throws PanlServerException {
		for (String panlFieldKey : PropertyHelper.getPropertiesByPrefix(properties, Constants.Property.Panl.PANL_FACET)) {
			String lpseCode = panlFieldKey.substring(panlFieldKey.lastIndexOf(".") + 1);
			// now we need to know the type of the facetField
			String solrFieldType = properties.getProperty(Constants.Property.Panl.PANL_TYPE + lpseCode, null);
			boolean isOrFacet = properties.getProperty(
					Constants.Property.Panl.PANL_OR_FACET + lpseCode,
					Constants.BOOLEAN_FALSE_VALUE).equals(Constants.BOOLEAN_TRUE_VALUE);
			boolean isRangeFacet = properties.getProperty(Constants.Property.Panl.PANL_RANGE_FACET + lpseCode,
					Constants.BOOLEAN_FALSE_VALUE).equals(Constants.BOOLEAN_TRUE_VALUE);

			PanlFacetField facetField;
			if (TYPE_SOLR_DATE_POINT_FIELD.equals(solrFieldType)) {
				facetField = new PanlDateRangeFacetField(
						lpseCode,
						panlFieldKey,
						properties,
						solrCollection,
						panlCollectionUri,
						lpseLength);

				LPSE_CODE_DATE_RANGE_FACET_MAP.put(lpseCode, (PanlDateRangeFacetField) facetField);

			} else if (TYPE_SOLR_BOOL_FIELD.equals(solrFieldType)) {
				facetField = new PanlBooleanFacetField(
						lpseCode,
						panlFieldKey,
						properties,
						solrCollection,
						panlCollectionUri,
						lpseLength);

				LPSE_CODE_BOOLEAN_FACET_MAP.put(lpseCode, (PanlBooleanFacetField) facetField);

			} else if (isOrFacet) {
				facetField = new PanlOrFacetField(
						lpseCode,
						panlFieldKey,
						properties,
						solrCollection,
						panlCollectionUri,
						lpseLength);

				PANL_CODE_OR_FIELDS.add(lpseCode);
				PanlOrFacetField panlOrFacetField = (PanlOrFacetField) facetField;

				if (panlOrFacetField.getIsAlwaysOr()) {
					PANL_CODE_OR_FIELDS_ALWAYS.add(lpseCode);
				}
			} else if (isRangeFacet) {
				facetField = new PanlRangeFacetField(
						lpseCode,
						panlFieldKey,
						properties,
						solrCollection,
						panlCollectionUri,
						lpseLength);

				LPSE_CODE_RANGE_FACET_MAP.put(lpseCode, (PanlRangeFacetField) facetField);
				PANL_CODE_RANGE_FIELDS.add(lpseCode);
			} else {
				facetField = new PanlFacetField(
						lpseCode,
						panlFieldKey,
						properties,
						solrCollection,
						panlCollectionUri,
						lpseLength);
			}

			// the following occurs on OR facets with a separator, and with regular
			// facets that are configured to be multivalued and have a multivalue
			// separator
			if (facetField.getValueSeparator() != null) {
				PANL_CODE_MULTIVALUED_SEPARATOR_FIELDS.add(lpseCode);
			}

			String lpseWhen = properties.getProperty(Constants.Property.Panl.PANL_WHEN + lpseCode);
			if (null != lpseWhen) {
				String[] splits = lpseWhen.split(",");
				for (String split : splits) {
					String trim = split.trim();

					if (!trim.isEmpty()) {
						if (!LPSE_CODE_WHEN_MAP.containsKey(lpseCode)) {
							LPSE_CODE_WHEN_MAP.put(lpseCode, new HashSet<>());
						}

						LPSE_CODE_WHEN_MAP.get(lpseCode).add(trim);
					}
				}
			}

			String lpseUnless = properties.getProperty(Constants.Property.Panl.PANL_UNLESS + lpseCode);
			if (null != lpseUnless) {
				String[] splits = lpseUnless.split(",");
				for (String split : splits) {
					String trim = split.trim();

					if (!trim.isEmpty()) {
						if (!LPSE_CODE_UNLESS_MAP.containsKey(lpseCode)) {
							LPSE_CODE_UNLESS_MAP.put(lpseCode, new HashSet<>());
						}

						LPSE_CODE_UNLESS_MAP.get(lpseCode).add(trim);
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
		for (PanlFacetField facetField : FACET_FIELDS) {
			// we do not ever return the Date facet - no point in enlarging the
			// response object for no purpose
			if (!(facetField instanceof PanlDateRangeFacetField)) {
				temp.add(facetField.getSolrFieldName());
			}
		}

		this.solrFacetFields = temp.toArray(new String[0]);
	}

	/**
	 * <p>Parse the properties files and extract all properties that begin with
	 * the panl search property key.</p>
	 *
	 * <p>This property is used as a lookup for specific search field lookups.</p>
	 *
	 * <p> See the
	 * {@link Constants.Property.Panl#PANL_SEARCH} static String for the panl prefix
	 * property</p>
	 *
	 * @throws PanlServerException If there was an error looking up the properties, or with the found property and its
	 * 		associated values
	 */
	private void parseSearchFields() throws PanlServerException {
		for (String panlFieldKey : PropertyHelper.getPropertiesByPrefix(properties, Constants.Property.Panl.PANL_SEARCH)) {
			String lpseCode = panlFieldKey.substring(panlFieldKey.lastIndexOf(".") + 1);
			// we don't care about the type, only the name of the Solr field and if
			// there is a nice human-readable name
			String solrFieldName = properties.getProperty(Constants.Property.Panl.PANL_SEARCH + lpseCode);
			String panlName = properties.getProperty(Constants.Property.Panl.PANL_NAME + lpseCode, null);
			if (null == panlName) {
				panlName = solrFieldName;
			}

			// now add them to the data structures
			SEARCH_LPSE_CODE_TO_PANL_NAME_MAP.put(lpseCode, panlName);
			SEARCH_LPSE_CODE_TO_SOLR_FIELD_MAP.put(lpseCode, solrFieldName);
			SEARCH_SOLR_FIELD_TO_LPSE_CODE_MAP.put(solrFieldName, lpseCode);
		}

		// now we need to add the order in panl.search.fields
		String property = properties.getProperty(Constants.Property.Panl.PANL_SEARCH_FIELDS, "");
		for (String solrSearchField : property.split(",")) {
			String solrSearchFieldTrim = solrSearchField.trim();

			if (solrSearchFieldTrim.isEmpty()) {
				break;
			} else {
				// look up the search field

				Integer fieldBoost = null;
				// if we have a caret character...
				if (solrSearchFieldTrim.contains("^")) {
					int caretIndex = solrSearchFieldTrim.lastIndexOf("^");
					try {
						fieldBoost = Integer.parseInt(solrSearchFieldTrim.substring(caretIndex + 1));
					} catch (NumberFormatException ignored) {
						LOGGER.warn("Invalid field boost for search field string of '" + solrSearchFieldTrim + "', could not " +
								"parse boost");
					}
					solrSearchFieldTrim = solrSearchFieldTrim.substring(0, caretIndex);
					SEARCH_CODES_BOOST.put(solrSearchFieldTrim, fieldBoost);
				}

				if (SEARCH_SOLR_FIELD_TO_LPSE_CODE_MAP.containsKey(solrSearchFieldTrim)) {
					// we are good to go
					String value = SEARCH_SOLR_FIELD_TO_LPSE_CODE_MAP.get(solrSearchFieldTrim);
					SEARCH_FIELDS_MAP.put(solrSearchFieldTrim, value);
					SEARCH_CODES_MAP.put(value, solrSearchFieldTrim);
				} else {
					LOGGER.warn(
							"Attempted to register a search field of '{}' which does not have " +
									"a corresponding Panl property set of panl.search.<lpse_code>",
							solrSearchFieldTrim);
				}
			}
		}
	}

	/**
	 * <p>Parse the fields - these are not able to be be used as a facet, however
	 * it will allow sort ordering.</p>
	 *
	 * @throws PanlServerException If there was an error parsing the field
	 */
	private void parseFields() throws PanlServerException {
		for (String panlFieldKey : PropertyHelper.getPropertiesByPrefix(properties, Constants.Property.Panl.PANL_FIELD)) {
			String lpseCode = panlFieldKey.substring(panlFieldKey.lastIndexOf(".") + 1);
			PanlField field = new PanlField(
					lpseCode,
					panlFieldKey,
					properties,
					solrCollection,
					panlCollectionUri,
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
		panlLpseOrder = properties.getProperty(Constants.Property.Panl.PANL_LPSE_ORDER, null);
		if (null == panlLpseOrder) {
			throw new PanlServerException("Could not find the MANDATORY property " + Constants.Property.Panl.PANL_LPSE_ORDER);
		}

		for (String lpseCode : panlLpseOrder.split(",")) {
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
						"Found a panl code of '{}' in the " + Constants.Property.Panl.PANL_LPSE_ORDER + " property, yet it is not a defined field.  This will be ignored...",
						lpseCode);
			}

			MANDATORY_LPSE_ORDER_FIELDS.remove(lpseCode);
		}

		boolean missingMandatoryLpseCode = false;
		// we also need to ensure that the default parameters are in the lpse order
		for (String key : MANDATORY_LPSE_ORDER_FIELDS.keySet()) {
			LOGGER.error("__MUST__ have key of '{}' in property '{}', this is set by the property '{}'.",
					key,
					Constants.Property.Panl.PANL_LPSE_ORDER,
					MANDATORY_LPSE_ORDER_FIELDS.get(key));
			missingMandatoryLpseCode = true;
		}

		if (missingMandatoryLpseCode) {
			throw new PanlServerException(
					"Missing mandatory LPSE codes in the " + Constants.Property.Panl.PANL_LPSE_ORDER + " property.");
		}
	}

	/**
	 * <p>Parse the LPSE Facet order</p>
	 *
	 * @throws PanlServerException if the panl.lpse.order does not exist
	 */
	private void parseLpseFacetOrder() throws PanlServerException {
		panlLpseFacetOrder = properties.getProperty(Constants.Property.Panl.PANL_LPSE_FACETORDER, null);

		if (null == panlLpseFacetOrder) {
			LOGGER.warn(
					"[Solr/Panl '{}/{}'] Could not find a property of '{}' which determines the ordering of the response " +
							"facets, defaulting to the facet subset of property '{}'",
					solrCollection,
					panlCollectionUri,
					Constants.Property.Panl.PANL_LPSE_FACETORDER,
					Constants.Property.Panl.PANL_LPSE_ORDER);

			// no go through the LPSE ORDER, removing the META characters, we don't
			// need to check for invalid LPSE codes as they were removed as part of
			// the parseLpseOrder() call
			for (String lpseCode : panlLpseOrderList) {

				if (!LPSE_METADATA.contains(lpseCode)) {
					boolean found = false;
					if(LPSE_CODE_TO_FACET_FIELD_MAP.containsKey(lpseCode)) {
						PanlFacetField panlFacetField = LPSE_CODE_TO_FACET_FIELD_MAP.get(lpseCode);
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("panl_code", lpseCode);
						jsonObject.put("facet_name", panlFacetField.getSolrFieldName());
						jsonObject.put("type", "facets");
						panlLpseFacetOrderJsonArray.put(jsonObject);
						found = true;
					} else if (LPSE_CODE_DATE_RANGE_FACET_MAP.containsKey(lpseCode)) {
						PanlDateRangeFacetField panlDateRangeFacetField = LPSE_CODE_DATE_RANGE_FACET_MAP.get(lpseCode);
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("panl_code", lpseCode);
						jsonObject.put("facet_name", panlDateRangeFacetField.getSolrFieldName());
						jsonObject.put("type", "date_range_facets");
						panlLpseFacetOrderJsonArray.put(jsonObject);
						found = true;
					}

					// it can be a facet and a date range facet
					if(LPSE_CODE_RANGE_FACET_MAP.containsKey(lpseCode)) {
						PanlRangeFacetField panlRangeFacetField = LPSE_CODE_RANGE_FACET_MAP.get(lpseCode);
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("panl_code", lpseCode);
						jsonObject.put("facet_name", panlRangeFacetField.getSolrFieldName());
						jsonObject.put("type", "range_facets");
						panlLpseFacetOrderJsonArray.put(jsonObject);
						found = true;
					}

					if(!found) {
						LOGGER.warn("Could not find the LPSE code '{}' for the facet order.", lpseCode);
					}
				}
			}
		} else {
			// lookup for all facets - in case there is one missing
			Set<String> allFacetLPSECodes = new LinkedHashSet<>();
			for (String lpseCode : panlLpseOrderList) {
				if (!LPSE_METADATA.contains(lpseCode)) {
					allFacetLPSECodes.add(lpseCode);
				}
			}

			for (String lpseCode : panlLpseFacetOrder.split(",")) {
				lpseCode = lpseCode.trim();
				if(allFacetLPSECodes.contains(lpseCode)) {

					if (!LPSE_METADATA.contains(lpseCode)) {
						boolean found = false;
						if(LPSE_CODE_TO_FACET_FIELD_MAP.containsKey(lpseCode)) {
							PanlFacetField panlFacetField = LPSE_CODE_TO_FACET_FIELD_MAP.get(lpseCode);
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("panl_code", lpseCode);
							jsonObject.put("facet_name", panlFacetField.getSolrFieldName());
							jsonObject.put("type", "facets");
							panlLpseFacetOrderJsonArray.put(jsonObject);
							found = true;
						} else if (LPSE_CODE_DATE_RANGE_FACET_MAP.containsKey(lpseCode)) {
							PanlDateRangeFacetField panlDateRangeFacetField = LPSE_CODE_DATE_RANGE_FACET_MAP.get(lpseCode);
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("panl_code", lpseCode);
							jsonObject.put("facet_name", panlDateRangeFacetField.getSolrFieldName());
							jsonObject.put("type", "date_range_facets");
							panlLpseFacetOrderJsonArray.put(jsonObject);
							found = true;
						}

						// it can be a facet and a date range facet
						if(LPSE_CODE_RANGE_FACET_MAP.containsKey(lpseCode)) {
							PanlRangeFacetField panlRangeFacetField = LPSE_CODE_RANGE_FACET_MAP.get(lpseCode);
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("panl_code", lpseCode);
							jsonObject.put("facet_name", panlRangeFacetField.getSolrFieldName());
							jsonObject.put("type", "range_facets");
							panlLpseFacetOrderJsonArray.put(jsonObject);
							found = true;
						}

						if(!found) {
							LOGGER.warn("Could not find the LPSE code '{}' for the facet order.", lpseCode);
						}
					}

					allFacetLPSECodes.remove(lpseCode);
				} else {
					LOGGER.warn(
							"Could not find the facet LPSE code of '{}' in the defined LPSE order.  Please check the '{}' and '{}' " +
									"properties.",
							lpseCode,
							Constants.Property.Panl.PANL_LPSE_FACETORDER,
							Constants.Property.Panl.PANL_LPSE_ORDER);

				}
			}
			// now just add all other LPSE code
			if(!allFacetLPSECodes.isEmpty()) {
				LOGGER.warn("[Solr/Panl '{}/{}'] Remaining LPSE codes not added to the '{}' property, adding them to the end of the order",
						solrCollection,
						panlCollectionUri,
						Constants.Property.Panl.PANL_LPSE_FACETORDER);

				for (String remainingLpseCode : allFacetLPSECodes.toArray(new String[] {})) {
					LOGGER.warn("Remaining LPSE code '{}' added to the '{}' property.",
							remainingLpseCode,
							Constants.Property.Panl.PANL_LPSE_FACETORDER);
				}
			}
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
		String panlLpseIgnore = properties.getProperty(Constants.Property.Panl.PANL_LPSE_IGNORE, "");
		for (String ignore : panlLpseIgnore.split(",")) {
			String trimmed = ignore.trim();
			if (trimmed.isBlank()) {
				continue;
			}

			if (LPSE_URI_CODES.contains(trimmed)) {
				LPSE_IGNORED_URI_CODES.add(trimmed);
			} else {
				LOGGER.warn(
						"Attempting to ignore a facet with code '{}' which was not defined by the lpse order property '{}'",
						trimmed, Constants.Property.Panl.PANL_LPSE_ORDER);
			}
		}
	}

	private void parseResultFields() throws PanlServerException {
		List<String> resultFieldProperties = PropertyHelper.getPropertiesByPrefix(properties,
				Constants.Property.Panl.PANL_RESULTS_FIELDS);
		for (String resultFieldProperty : resultFieldProperties) {
			addResultsFields(resultFieldProperty.substring(Constants.Property.Panl.PANL_RESULTS_FIELDS.length()),
					properties.getProperty(resultFieldProperty));
		}

		// there must always be a default field
		if (!resultFieldsMap.containsKey(Constants.Url.Panl.FIELDSETS_DEFAULT)) {
			LOGGER.warn("[ Solr/Panl '{}/{}' ] Missing default field set, adding one which will return all fields.",
					solrCollection, panlCollectionUri);
			resultFieldsMap.put(Constants.Url.Panl.FIELDSETS_DEFAULT, new ArrayList<>());
		}

		if (resultFieldsMap.containsKey(Constants.Url.Panl.FIELDSETS_EMPTY)) {
			LOGGER.warn(
					"[ Solr/Panl '{}/{}' ] 'empty' fieldset defined.  This will be ignored, and empty fieldset __ALWAYS__ returns no fields.",
					solrCollection, panlCollectionUri);
		}
		resultFieldsMap.put(Constants.Url.Panl.FIELDSETS_EMPTY, null);
	}

	private void addResultsFields(String resultFieldsName, String resultFields) throws PanlServerException {
		if (resultFieldsMap.containsKey(resultFieldsName)) {
			throw new PanlServerException("panl.results.fields.'" + resultFieldsName + "' is already defined.");
		}

		LOGGER.info("[ Solr/Panl '{}/{}' ] Adding result fields with key '{}', and fields '{}'.", solrCollection,
				panlCollectionUri, resultFieldsName, resultFields);

		List<String> fields = new ArrayList<>();
		for (String resultField : resultFields.split(",")) {
			fields.add(resultField.trim());
			//			String resultFieldTrim = resultField.trim();
			//			// now look up to see whether we have this field
			//			// TODO - FIX THIS - need to only add the fields which exist
			//			if(SOLR_NAME_TO_LPSE_CODE_MAP.containsKey(resultFieldTrim)) {
			//				fields.add(resultFieldTrim);
			//			} else {
			//				LOGGER.warn("[ Solr/Panl '{}/{}' ] Cannot find field/facet definition for Solr field '{}'." +
			//								"This will not be added to the fieldSet '{}'.",
			//						solrCollection,
			//						panlCollectionUri,
			//						resultFieldTrim,
			//						resultFieldsName);
			//			}
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
			return (Constants.Parameter.Solr.SOLR_DEFAULT_QUERY_OPERAND_OR);
		} else {
			return (Constants.Parameter.Solr.SOLR_DEFAULT_QUERY_OPERAND_AND);
		}
	}

	public String getDefaultQueryOperand() {
		return (solrDefaultQueryOperand);
	}

	public List<String> getResultFieldsNames() {
		return (new ArrayList<>(resultFieldsMap.keySet()));
	}

	/**
	 * <p>Return the fields for a specific fieldSet, or an empty list if either
	 * the field does not exist, or it is an emp[ty fieldset.</p>
	 *
	 * @param name The name of the fieldSet
	 *
	 * @return The list of fields in this fieldset
	 */
	public List<String> getResultFieldsForFieldSet(String name) {
		List<String> strings = resultFieldsMap.get(name);
		if (null == strings) {
			return (new ArrayList<>());
		} else {
			return (strings);
		}
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

		for (String panlCodeOrField : PANL_CODE_OR_FIELDS) {
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
	 * <p>Get the minimum facet value for facets.</p>
	 *
	 * @return The minimum value for the facet count
	 */
	public int getFacetMinCount() {
		return (facetMinCount);
	}

	/**
	 * <p>Get the number of results per page to return (if not overridden by a
	 * LPSE code)</p>
	 *
	 * @return The number of results to return by page
	 */
	public int getNumResultsPerPage() {
		return numResultsPerPage;
	}

	public int getMaxNumResultsPerPage() {
		return (maxNumResultsPerPage);
	}

	public int getNumResultsLookahead() {
		return numResultsLookahead;
	}

	/**
	 * <p>Get the facet fields that should be passed through to Solr, ensuring
	 * that any hierarchical (i.e. <code>panl.when.&lt;lpse_code&gt;</code>) lpse codes are not returned, and unless lpse
	 * codes (i.e. <code>panl.when.&lt;lpse_code&gt;</code>) are returned unless it shouldn't be.</p>
	 *
	 * @param lpseTokens The current active LPSE tokens
	 *
	 * @return The array of solr fields to facet on including when codes and excluding unless lpse codes
	 */
	public String[] getWhenUnlessSolrFacetFields(List<LpseToken> lpseTokens) {
		// if there are no conditions on the retrieval of facets, then continue
		if (LPSE_CODE_WHEN_MAP.isEmpty() && LPSE_CODE_UNLESS_MAP.isEmpty()) {
			// return all of the facet fields
			return (solrFacetFields);
		}

		Set<String> activeLpseCodes = new HashSet<>();
		for (LpseToken lpseToken : lpseTokens) {
			activeLpseCodes.add(lpseToken.getLpseCode());
		}

		// now go through the facet fields and add them all - in some cases there
		// will be a
		//   - when facet - so we don't add it until another facet in the when list
		//     has been added
		//   - unless facet - we will add it unless a previous facet has been chosen
		//     from the unless list
		List<String> returnedFacetFields = new ArrayList<>();
		for (String solrFacetFieldName : solrFacetFields) {
			String lpseCode = SOLR_NAME_TO_LPSE_CODE_MAP.get(solrFacetFieldName);
			if (null == lpseCode) {
				// shouldn't happen, but doesn't matter - this may error on the Solr
				// side - which is unlikely as you cannot register a field without a
				// LPSE code
				returnedFacetFields.add(solrFacetFieldName);
			} else {
				// now we need to lookup the lpseCode in the WHEN map
				if (LPSE_CODE_WHEN_MAP.containsKey(lpseCode)) {
					// do we have the 'when' code in the token map?
					for (String s : LPSE_CODE_WHEN_MAP.get(lpseCode)) {
						// TODO - union/intersection of sets is probably a better way to go
						if (activeLpseCodes.contains(s)) {
							// now we need to check this lpse code to ensure it isn't in an
							// unless state
							if (LPSE_CODE_UNLESS_MAP.containsKey(lpseCode)) {
								// have a look to see whether this is in the active facets
								Set<String> lookupUnless = new HashSet<>(activeLpseCodes);
								lookupUnless.retainAll(LPSE_CODE_UNLESS_MAP.get(lpseCode));
								if (lookupUnless.isEmpty()) {
									returnedFacetFields.add(solrFacetFieldName);
								}
							} else {
								returnedFacetFields.add(solrFacetFieldName);
							}
							break;
						}
					}
				} else {
					if (LPSE_CODE_UNLESS_MAP.containsKey(lpseCode)) {
						// have a look to see whether this is in the active facets
						Set<String> lookupUnless = new HashSet<>(activeLpseCodes);
						lookupUnless.retainAll(LPSE_CODE_UNLESS_MAP.get(lpseCode));
						if (lookupUnless.isEmpty()) {
							returnedFacetFields.add(solrFacetFieldName);
						}
					} else {
						returnedFacetFields.add(solrFacetFieldName);
					}
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
	 *
	 * @return Whether the LPSE code field is an OR facet field
	 */
	public boolean getIsOrFacetField(String lpseCode) {
		return (PANL_CODE_OR_FIELDS.contains(lpseCode));
	}

	/**
	 * <p>Return whether the passed in LPSE code has an OR separator string.</p>
	 *
	 * @param lpseCode The LPSE code to check
	 *
	 * @return Whether the LPSE code field has an  OR separator
	 */
	public boolean getIsMultiValuedSeparatorFacetField(String lpseCode) {
		return (PANL_CODE_MULTIVALUED_SEPARATOR_FIELDS.contains(lpseCode));
	}

	/**
	 * <p>Return whether the passed in LPSE code is a RANGE facet field.</p>
	 *
	 * @param lpseCode The LPSE code to check
	 *
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
	 * @return The Panl human-readable name for the LPSE code, or null if it does not exist.
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

	/**
	 * <p>Return the BaseField for a specific LPSE code.  This will return null
	 * if the lpseCode is not registered.</p>
	 *
	 * @param lpseCode The LPSE code to look up.
	 *
	 * @return The BaseField for the LPSE code (or null if it doesn't exist)
	 */
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

	/**
	 * <p>Return the list of facet fields that should be sorted by the index,
	 * rather than the count.</p>
	 *
	 * @return The list of facet fields that should be sorted by index rather
	 * than the count.
	 */
	public List<PanlFacetField> getFacetIndexSortFields() {
		return (FACET_INDEX_SORT_FIELDS);
	}

	private void parseFacetSortFields() {
		for (PanlFacetField facetField : FACET_FIELDS) {
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

	/**
	 * <p>Get the list of the facet order LPSE codes</p>
	 *
	 * @return The JSONArray of lpse facet codes
	 */
	public JSONArray getPanlLpseFacetOrderJsonArray() {
		return (panlLpseFacetOrderJsonArray);
	}

	/**
	 * <p>Return the Panl name for a LPSE code that is designated as a searchable
	 * field.</p>
	 *
	 * @param lpseCode The LPSE code to look up
	 *
	 * @return The Solr field name, or null if it doesn't exist
	 */
	public String getPanlNameFromSearchLpseCode(String lpseCode) {
		return (SEARCH_LPSE_CODE_TO_PANL_NAME_MAP.get(lpseCode));
	}

	/**
	 * <p>Return the Solr field name for a LPSE code that is designated as a
	 * searchable field.</p>
	 *
	 * @param lpseCode The LPSE code to look up
	 *
	 * @return The Solr field name, or null if it doesn't exist
	 */
	public String getSolrFieldNameFromSearchLpseCode(String lpseCode) {
		return (SEARCH_LPSE_CODE_TO_SOLR_FIELD_MAP.get(lpseCode));
	}

	public boolean getIsASearchField(String solrFieldName) {
		return (SEARCH_FIELDS_MAP.containsKey(solrFieldName));
	}

	public Map<String, String> getSearchFieldsMap() {
		return (SEARCH_FIELDS_MAP);
	}

	public Map<String, String> getSearchCodesMap() {
		return (SEARCH_CODES_MAP);
	}

	/**
	 * <p>Return the boost for a field in the Solr form of <code>^&lt;number&gt;</code> or
	 * an empty string.</p>
	 *
	 * <p>For example, if the Solr field does not have a boost applied to it, this
	 * method will return <code>""</code>.  If it does have a boost value (e.g. '4')
	 * it will return <code>"^4"</code>.</p>
	 *
	 * @param solrFieldName The Solr field name to look up to determine if there is a boost value.
	 *
	 * @return The Solr query boost in the correct format, or an empty string if no boost is available.
	 */
	public String getSpecificSearchBoost(String solrFieldName) {
		if (SEARCH_CODES_BOOST.containsKey(solrFieldName)) {
			return ("^" + SEARCH_CODES_BOOST.get(solrFieldName));
		} else {
			return ("");
		}
	}
}

