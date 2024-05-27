package com.synapticloop.panl.server.properties;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.field.BaseField;
import com.synapticloop.panl.server.handler.field.FacetField;
import com.synapticloop.panl.server.handler.field.MetaDataField;
import com.synapticloop.panl.util.PropertyHelper;
import org.apache.solr.client.solrj.SolrQuery;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CollectionProperties {
	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionProperties.class);
	public static final String PROPERTY_KEY_PANL_FACET = "panl.facet.";
	public static final String PROPERTY_KEY_PANL_NAME = "panl.name.";
	public static final String PROPERTY_KEY_PANL_RESULTS_FIELDS = "panl.results.fields.";

	private final String collectionName;

	private boolean facetEnabled;
	private int facetMinCount;
	private int resultRows;

	private int panlLpseNum;
	private String panlParamQuery;
	private String panlParamSort;
	private String panlParamPage;
	private String panlParamNumRows;
	
	private String solrModifierAnd;
	private String solrModifierOr;
	private String solrDefaultModifier;
	private String solrSortAsc;
	private String solrSortDesc;

	private SolrQuery.ORDER defaultOrder = SolrQuery.ORDER.asc;

	private List<String> lpseOrder = new ArrayList<>();
	private List<BaseField> lpseFields = new ArrayList<>();

	private Set<String> metadataMap = new HashSet<>();
	private Map<String, String> facetFieldMap = new HashMap<>();
	private Map<String, String> facetNameMap = new HashMap<>();
	private Map<String, String> solrFacetNameToPanlCodeMap = new HashMap<>();
	private Map<String, String> solrFacetNameToPanlName = new HashMap<>();
	private final Map<String, List<String>> resultFieldsMap = new HashMap<>();
	private String[] facetFields;

	public CollectionProperties(String collectionName, Properties properties) throws PanlServerException {
		this.collectionName = collectionName;

		parseBaseProperties(properties);
		parseDefaultProperties(properties);
		parseFacetFields(properties);
		parseLpseOrder(properties);
		parseResultFields(properties);
		parseDefaultSortOrder(properties);
	}

	private void parseDefaultSortOrder(Properties properties) {
		String property = properties.getProperty("solr.default.modifier");
		switch(property) {
			case "+":
				this.defaultOrder = SolrQuery.ORDER.asc;
				break;
			case "-":
				this.defaultOrder = SolrQuery.ORDER.desc;
				break;
			default:
				LOGGER.warn("Unknown solr.default.modifier of '{}', using default sort order of SolrQuery.ORDER.asc.", property);
				this.defaultOrder = SolrQuery.ORDER.asc;
		}
	}

	private void parseBaseProperties(Properties properties) {
		this.facetEnabled = properties.getProperty("solr.facet.enabled", "true").equals("true");
		this.facetMinCount = PropertyHelper.getIntProperty(properties, "solr.facet.min.count", 1);
		this.resultRows = PropertyHelper.getIntProperty(properties, "solr.numrows", 10);
	}

	private void parseDefaultProperties(Properties properties) {
		this.panlLpseNum = PropertyHelper.getIntProperty(properties, "panl.lpse.num", 1);

		this.panlParamQuery = properties.getProperty("panl.param.query", "q");
		metadataMap.add(this.panlParamQuery);
		this.panlParamSort = properties.getProperty("panl.param.sort", "s");
		metadataMap.add(this.panlParamSort);
		this.panlParamPage = properties.getProperty("panl.param.page", "p");
		metadataMap.add(this.panlParamPage);
		this.panlParamNumRows = properties.getProperty("panl.param.numrows", "n");
		metadataMap.add(this.panlParamNumRows);

		this.solrModifierAnd = properties.getProperty("solr.modifier.AND", "+");
		this.solrModifierOr = properties.getProperty("solr.modifier.OR", "-");
		this.solrDefaultModifier = properties.getProperty("solr.default.modifier", "+");
		this.solrSortAsc = properties.getProperty("solr.sort.ASC", "U");
		this.solrSortDesc = properties.getProperty("solr.sort.DESC", "D");
	}

	private void parseFacetFields(Properties properties) throws PanlServerException {
		List<String> facetFieldList = new ArrayList<>();

		// now parse the fields
		for (String panlFieldKey : PropertyHelper.getPropertiesByPrefix(properties, PROPERTY_KEY_PANL_FACET)) {
			String panlFieldValue = properties.getProperty(panlFieldKey);
			String panlFacetCode = panlFieldKey.substring(PROPERTY_KEY_PANL_FACET.length());
			if(panlFacetCode.length() != panlLpseNum) {
				throw new PanlServerException(PROPERTY_KEY_PANL_FACET + panlFacetCode + " property key is of invalid length - should be " + panlLpseNum);
			}
			facetFieldMap.put(panlFacetCode, panlFieldValue);
			facetFieldList.add(panlFieldValue);
			LOGGER.info("[{}] Mapping facet '{}' to panl key '{}'", collectionName, panlFieldValue, panlFacetCode);
			String panlFacetName = properties.getProperty(PROPERTY_KEY_PANL_NAME + panlFacetCode, null);
			if(null == panlFacetName) {
				LOGGER.warn("[{}] Could not find a name for panl facet code '{}', using field name '{}'", collectionName, panlFacetCode, panlFieldValue);
				panlFacetName = panlFieldValue;
			} else {
				LOGGER.info("[{}] Found a name for panl facet code '{}', using '{}'", collectionName, panlFacetCode, panlFacetName);
			}
			facetNameMap.put(panlFacetCode, panlFacetName);
			solrFacetNameToPanlCodeMap.put(panlFieldValue, panlFacetCode);
			solrFacetNameToPanlName.put(panlFieldValue, panlFacetName);
		}
		this.facetFields = facetFieldList.toArray(new String[0]);
	}

	private void parseLpseOrder(Properties properties) throws PanlServerException {
		String panlLpseOrder = properties.getProperty("panl.lpse.order", null);
		if(null == panlLpseOrder) {
			throw new PanlServerException("Could not find the panl.lpse.order");
		}

		for (String lpseCode : panlLpseOrder.split(",")) {
			if(facetFieldMap.containsKey(lpseCode)) {
				lpseOrder.add(lpseCode);
				lpseFields.add(new FacetField(lpseCode));
			} else if(metadataMap.contains(lpseCode)) {
				lpseOrder.add(lpseCode);
				lpseFields.add(new MetaDataField(lpseCode));
			} else {
				throw new PanlServerException("Could not find the panl code '" + lpseCode + "' in the panl.lpse.order property.");
			}
		}
	}

	private void parseResultFields(Properties properties) throws PanlServerException {
		List<String> resultFieldProperties = PropertyHelper.getPropertiesByPrefix(properties, PROPERTY_KEY_PANL_RESULTS_FIELDS);
		for(String resultFieldProperty: resultFieldProperties) {
			addResultsFields(resultFieldProperty.substring(PROPERTY_KEY_PANL_RESULTS_FIELDS.length()), properties.getProperty(resultFieldProperty));
		}
	}

	private void addResultsFields(String resultFieldsName, String resultFields) throws PanlServerException {
		if(resultFieldsMap.containsKey(resultFieldsName)) {
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

	public String getPanlParamSort() {
		return (panlParamSort);
	}

	public String getPanlParamPage() {
		return (panlParamPage);
	}

	public String getPanlParamNumRows() {
		return (panlParamNumRows);
	}

	public String getSolrModifierAnd() {
		return (solrModifierAnd);
	}

	public String getSolrModifierOr() {
		return (solrModifierOr);
	}

	public String getSolrDefaultModifier() {
		return (solrDefaultModifier);
	}

	public String getSolrSortAsc() {
		return (solrSortAsc);
	}

	public String getSolrSortDesc() {
		return (solrSortDesc);
	}

	public List<String> getLpseOrder() {
		return (lpseOrder);
	}

	public List<String> getResultFieldsNames() {
		return(new ArrayList<>(resultFieldsMap.keySet()));
	}

	public List<String> getResultFieldsForName(String name) {
		return(resultFieldsMap.get(name));
	}

	public Map<String, List<String>> getResultFieldsMap() {
		return (resultFieldsMap);
	}

	public boolean isValidResultFieldsName(String name) {
		return(resultFieldsMap.containsKey(name));
	}

	public String getValidUrlsJson() {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		for (String resultFieldsName : getResultFieldsNames()) {
			jsonArray.put("/" + collectionName + "/" + resultFieldsName + "/");
		}

		jsonObject.put("valid_urls", jsonArray);

		return(jsonObject.toString());
	}

	public boolean getFacetEnabled() {
		return facetEnabled;
	}

	public int getFacetMinCount() {
		return facetMinCount;
	}

	public int getResultRows() {
		return resultRows;
	}

	public String[] getFacetFields() {
		return (facetFields);
	}

	public List<BaseField> getLpseFields() {
		return (lpseFields);
	}

	public boolean isMetadataToken(String token) {
		return(metadataMap.contains(token));
	}

	public SolrQuery.ORDER getDefaultOrder() {
		return (defaultOrder);
	}

	public boolean hasSortField(String panlCode) {
		return(facetFieldMap.containsKey(panlCode));
	}

	public String getNameFromCode(String panlCode) {
		return(facetFieldMap.get(panlCode));
	}

	public boolean hasFacetCode(String panlfacet)   {
		return(facetFieldMap.containsKey(panlfacet));
	}

	public String getPanlCodeFromSolrFacetName(String name) {
		return(solrFacetNameToPanlCodeMap.get(name));
	}

	public String getPanlNameFromSolrFacetName(String name) {
		return(solrFacetNameToPanlName.get(name));
	}
}
