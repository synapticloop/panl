package com.synapticloop.panl.server.handler.fielderiser.field.facet;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.processor.Processor;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.FacetLpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.synapticloop.panl.server.handler.processor.Processor.*;

public class PanlRangeFacetField extends PanlFacetField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlRangeFacetField.class);

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	//                            OR Facet properties                          //
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	//                          RANGE Facet properties                         //
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	protected boolean isRangeFacet = false;
	private boolean hasMinRange = false;
	private String rangeMinValue;
	private boolean hasMaxRange = false;
	private String rangeMaxValue;
	private boolean hasRangeInfix = false;

	private String rangeValueInfix;
	private String rangeMinValueReplacement;
	private String rangeMaxValueReplacement;

	private boolean hasRangePrefix;
	private String rangePrefix;
	private boolean hasRangeSuffix;
	private String rangeSuffix;

	protected boolean hasMinRangeWildcard;
	protected boolean hasMaxRangeWildcard;

	public PanlRangeFacetField(String lpseCode, String propertyKey, Properties properties, String solrCollection, int lpseLength) throws PanlServerException {
		super(lpseCode, propertyKey, properties, solrCollection, lpseLength);
		populateRangeProperties();
	}

	/**
	 * <p>Populate the range properties, if this is a range facet</p>
	 */
	protected void populateRangeProperties() {
		this.isRangeFacet = properties.getProperty(PROPERTY_KEY_PANL_RANGE_FACET + lpseCode, "false").equals("true");

		if (this.isRangeFacet) {
			// get the other properties, if they exist...
			this.rangeMinValue = properties.getProperty(PROPERTY_KEY_PANL_RANGE_MIN + lpseCode, null);
			if (null != this.rangeMinValue) {
				hasMinRange = true;
			}

			this.rangeMaxValue = properties.getProperty(PROPERTY_KEY_PANL_RANGE_MAX + lpseCode, null);
			if (null != this.rangeMaxValue) {
				hasMaxRange = true;
			}

			this.rangePrefix = properties.getProperty(PROPERTY_KEY_PANL_RANGE_PREFIX + lpseCode, null);
			if (null != this.rangePrefix) {
				hasRangePrefix = true;
			}

			this.rangeSuffix = properties.getProperty(PROPERTY_KEY_PANL_RANGE_SUFFIX + lpseCode, null);
			if (null != this.rangeSuffix) {
				hasRangeSuffix = true;
			}

			this.rangeValueInfix = properties.getProperty(PROPERTY_KEY_PANL_RANGE_INFIX + lpseCode, null);
			if (null != this.rangeValueInfix) {
				hasRangeInfix = true;
			}

			this.rangeMinValueReplacement = properties.getProperty(PROPERTY_KEY_PANL_RANGE_MIN_VALUE + lpseCode, null);
			this.rangeMaxValueReplacement = properties.getProperty(PROPERTY_KEY_PANL_RANGE_MAX_VALUE + lpseCode, null);
			this.hasMinRangeWildcard = properties.getProperty(PROPERTY_KEY_PANL_RANGE_MIN_WILDCARD + lpseCode, "false").equals("true");
			this.hasMaxRangeWildcard = properties.getProperty(PROPERTY_KEY_PANL_RANGE_MAX_WILDCARD + lpseCode, "false").equals("true");
		}
	}

	@Override public List<String> explainAdditional() {
		return List.of();
	}


	@Override public Logger getLogger() {
		return (LOGGER);
	}

	@Override protected void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokens) {
		for (LpseToken lpseToken : lpseTokens) {
			FacetLpseToken facetLpseToken = (FacetLpseToken) lpseToken;

			// even though this field is set to be a range facet, we still allow
			// single values

			if (facetLpseToken.getIsRangeToken()) {
				String value = (hasMinRangeWildcard && facetLpseToken.getValue().equals(getMinRange())) ? "*" : facetLpseToken.getValue();
				String toValue = (hasMaxRangeWildcard && facetLpseToken.getToValue().equals(getMaxRange())) ? "*" : facetLpseToken.getToValue();
				solrQuery.addFilterQuery(
						String.format("%s:[%s TO %s]",
								facetLpseToken.getSolrField(),
								value,
								toValue));
			} else {
				solrQuery.addFilterQuery(String.format("%s:\"%s\"",
						facetLpseToken.getSolrField(),
						facetLpseToken.getValue()));
			}
		}
	}

	@Override public String getLpseCode(LpseToken token, CollectionProperties collectionProperties) {
		FacetLpseToken facetLpseToken = (FacetLpseToken) token;
		return (facetLpseToken.getLpseCode() +
				(facetLpseToken.getHasMidfix() ? "-" : "+") +
				facetLpseToken.getLpseCode());
	}

	public static final String JSON_KEY_IS_RANGE_FACET = "is_range_facet";

	@Override public void appendAvailableObjectInternal(JSONObject jsonObject) {
		jsonObject.put(JSON_KEY_IS_RANGE_FACET, true);
	}

	/**
	 * <p>OR facets are different, in that they will return a currently selected
	 * facet and more</p>
	 *
	 * @param facetObject
	 * @param collectionProperties
	 * @param panlTokenMap
	 * @param values
	 * @param numFound
	 * @param numFoundExact
	 *
	 * @return whether things were appended
	 */
	public boolean appendAvailableValues(
			JSONObject facetObject,
			CollectionProperties collectionProperties,
			Map<String, List<LpseToken>> panlTokenMap,
			List<FacetField.Count> values,
			long numFound,
			boolean numFoundExact) {

		JSONArray facetValueArrays = new JSONArray();

		// if we currently have the facet value, don't do anything
		Set<String> currentValueSet = new HashSet<>();
		for (LpseToken lpseToken : panlTokenMap.getOrDefault(this.lpseCode, new ArrayList<>())) {
			currentValueSet.add(lpseToken.getValue());
		}


		for (FacetField.Count value : values) {
			boolean shouldAdd = true;
			// at this point - we need to see whether we already have the 'value'
			// as a facet - as there is no need to have it again

			String valueName = value.getName();

			if (currentValueSet.contains(valueName)) {
				continue;
			}

			// also, if the count of the number of found results is the same as
			// the number of the count of the facet - then we may not need to
			// include it
			if (!panlIncludeSameNumberFacets &&
					numFound == value.getCount() &&
					numFoundExact) {
				shouldAdd = false;
			}

			if (shouldAdd) {
				JSONObject facetValueObject = new JSONObject();
				facetValueObject.put(JSON_KEY_VALUE, valueName);
				facetValueObject.put(JSON_KEY_COUNT, value.getCount());
				facetValueObject.put(JSON_KEY_ENCODED, getEncodedPanlValue(valueName));
				facetValueArrays.put(facetValueObject);
			}
		}

		int length = facetValueArrays.length();
		boolean shouldIncludeFacet = true;
		switch (length) {
			case 0:
				shouldIncludeFacet = false;
				break;
			case 1:
				shouldIncludeFacet = panlIncludeSingleFacets;
				break;
		}

		// if we don't have any values for this facet, don't put it in

		if (shouldIncludeFacet) {
			facetObject.put(JSON_KEY_VALUES, facetValueArrays);
			if (null != lpseCode) {
				facetObject.put(JSON_KEY_URIS,
						getAdditionURIObject(
								collectionProperties,
								this,
								panlTokenMap));
				return (true);
			}
		}
		return (false);
	}

	public boolean appendAvailableRangeValues(
			JSONObject rangeFacetObject,
			CollectionProperties collectionProperties,
			Map<String, List<LpseToken>> panlTokenMap) {

		// put this in the array please
		String lpseCode = this.lpseCode;
		rangeFacetObject.put(JSON_KEY_FACET_NAME, collectionProperties.getSolrFieldNameFromLpseCode(lpseCode));
		rangeFacetObject.put(JSON_KEY_NAME, collectionProperties.getPanlNameFromPanlCode(lpseCode));
		rangeFacetObject.put(JSON_KEY_PANL_CODE, lpseCode);
		rangeFacetObject.put(JSON_KEY_MIN, getMinRange());
		rangeFacetObject.put(JSON_KEY_MAX, getMaxRange());
		rangeFacetObject.put(JSON_KEY_PREFIX, URLEncoder.encode(getValuePrefix(), StandardCharsets.UTF_8));
		rangeFacetObject.put(JSON_KEY_SUFFIX, URLEncoder.encode(getValueSuffix(), StandardCharsets.UTF_8));

		// range min and max values
		String rangeMaxValue = getRangeMaxValueReplacement();
		if (null != rangeMaxValue) {
			rangeFacetObject.put(Processor.JSON_KEY_RANGE_MAX_VALUE, URLEncoder.encode(rangeMaxValue, StandardCharsets.UTF_8));
		}

		String rangeMinValue = getRangeMinValueReplacement();
		if (null != rangeMinValue) {
			rangeFacetObject.put(Processor.JSON_KEY_RANGE_MIN_VALUE, URLEncoder.encode(rangeMinValue, StandardCharsets.UTF_8));
		}

		// if we already have this facet selected - add in the to and from
		// values - only allowed one facet code per range
		if (panlTokenMap.containsKey(lpseCode)) {
			FacetLpseToken facetLpseToken = (FacetLpseToken) panlTokenMap.get(lpseCode).get(0);
			if (null != facetLpseToken.getToValue()) {
				rangeFacetObject.put(JSON_KEY_VALUE, facetLpseToken.getValue());
				rangeFacetObject.put(JSON_KEY_VALUE_TO, facetLpseToken.getToValue());
			}
		}

		// addition URIs are a little bit different...
		JSONObject additionURIObject = getAdditionURIObject(collectionProperties, panlTokenMap);
		rangeFacetObject.put(JSON_KEY_URIS, additionURIObject);

		return (true);
	}

	/**
	 * <p>This is an OR facet, so we can additional </p>
	 *
	 * @param collectionProperties
	 * @param panlTokenMap
	 *
	 * @return
	 */
	protected JSONObject getAdditionURIObject(CollectionProperties collectionProperties, Map<String, List<LpseToken>> panlTokenMap) {
		JSONObject additionObject = new JSONObject();

		StringBuilder lpseUri = new StringBuilder(FORWARD_SLASH);
		StringBuilder lpseUriBefore = new StringBuilder();
		StringBuilder lpseUriCode = new StringBuilder();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			// we need to add in any other token values in the correct order
			String orderedLpseCode = baseField.getLpseCode();

			// if we don't have a current token, just carry on
			if (!panlTokenMap.containsKey(orderedLpseCode)) {
				continue;
			}

			if (orderedLpseCode.equals(this.lpseCode)) {
				// we have found the current LPSE code, so reset the URI and add it to
				// the after
				lpseUri.append(baseField.getURIPath(panlTokenMap, collectionProperties));
				lpseUriBefore.append(lpseUri);
				lpseUri.setLength(0);
				lpseUriCode.append(this.lpseCode);

				// we add an additional LPSE code for the additional value that we are
				// going to put in
				lpseUriCode.append(this.lpseCode);
			} else {
				lpseUri.append(baseField.getURIPath(panlTokenMap, collectionProperties));
				lpseUriCode.append(baseField.getLpseCode());
			}
		}

		additionObject.put(JSON_KEY_BEFORE, lpseUriBefore.toString());

		additionObject.put(JSON_KEY_AFTER, FORWARD_SLASH + lpseUri.toString() + lpseUriCode.toString() + FORWARD_SLASH);
		return (additionObject);
	}
}
