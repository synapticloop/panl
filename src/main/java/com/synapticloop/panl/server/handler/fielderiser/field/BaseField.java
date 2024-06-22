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
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.FacetLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.synapticloop.panl.server.handler.processor.Processor.*;
import static com.synapticloop.panl.server.handler.properties.CollectionProperties.PROPERTY_KEY_PANL_SORT_FIELDS;

public abstract class BaseField {

	public static final String JSON_KEY_FACET_NAME = "facet_name";
	public static final String JSON_KEY_NAME = "name";
	public static final String JSON_KEY_PANL_CODE = "panl_code";
	public static final String JSON_KEY_VALUE = "value";
	public static final String JSON_KEY_COUNT = "count";
	public static final String JSON_KEY_ENCODED = "encoded";
	public static final String JSON_KEY_VALUES = "values";
	public static final String JSON_KEY_URIS = "uris";


	public static final String PROPERTY_KEY_PANL_FIELD = "panl.field.";
	public static final String PROPERTY_KEY_PANL_NAME = "panl.name.";
	public static final String PROPERTY_KEY_PANL_FACET = "panl.facet.";
	public static final String PROPERTY_KEY_PANL_OR_FACET = "panl.or.facet.";
	public static final String PROPERTY_KEY_PANL_TYPE = "panl.type.";
	public static final String PROPERTY_KEY_PANL_PREFIX = "panl.prefix.";
	public static final String PROPERTY_KEY_PANL_SUFFIX = "panl.suffix.";
	public static final String PROPERTY_KEY_SOLR_FACET_MIN_COUNT = "solr.facet.min.count";

	public static final String PROPERTY_KEY_PANL_INCLUDE_SAME_NUMBER_FACETS = "panl.include.same.number.facets";
	public static final String PROPERTY_KEY_PANL_INCLUDE_SINGLE_FACETS = "panl.include.single.facets";

	public static final String PROPERTY_KEY_PANL_RANGE_FACET = "panl.range.facet.";
	public static final String PROPERTY_KEY_PANL_RANGE_INFIX = "panl.range.infix.";

	public static final String TYPE_SOLR_BOOL_FIELD = "solr.BoolField";
	public static final String TYPE_SOLR_INT_POINT_FIELD = "solr.IntPointField";
	public static final String TYPE_SOLR_LONG_POINT_FIELD = "solr.LongPointField";
	public static final String TYPE_SOLR_DOUBLE_POINT_FIELD = "solr.DoublePointField";
	public static final String TYPE_SOLR_FLOAT_POINT_FIELD = "solr.FloatPointField";
	public static final String TYPE_SOLR_DATE_POINT_FIELD = "solr.DatePointField";

	protected String lpseCode;
	protected String panlFieldName;
	protected String solrFieldName;
	private String solrFieldType;

	protected final Properties properties;
	protected final String solrCollection;
	private final String propertyKey;

	protected final String panlCollectionUri;
	protected final int lpseLength;

	private static final int VALIDATION_TYPE_NONE = 0;
	private static final int VALIDATION_TYPE_NUMBER = 1;
	private static final int VALIDATION_TYPE_DECIMAL = 2;
	private static final int VALIDATION_TYPE_DATE = 3;

	protected final boolean panlIncludeSingleFacets;
	protected final boolean panlIncludeSameNumberFacets;

	private int validationType;

	private final List<String> WARNING_MESSAGES = new ArrayList<>();

	public BaseField(
			String lpseCode,
			Properties properties,
			String propertyKey,
			String solrCollection,
			String panlCollectionUri) throws PanlServerException {
		this(lpseCode, propertyKey, properties, solrCollection, panlCollectionUri, 1);
	}

	public BaseField(
			String lpseCode,
			String propertyKey,
			Properties properties,
			String solrCollection,
			String panlCollectionUri,
			int lpseLength) throws PanlServerException {

		this.panlIncludeSingleFacets = properties.getProperty(PROPERTY_KEY_PANL_INCLUDE_SINGLE_FACETS, "false").equals("true");
		this.panlIncludeSameNumberFacets = properties.getProperty(PROPERTY_KEY_PANL_INCLUDE_SAME_NUMBER_FACETS, "false").equals("true");

		this.lpseCode = lpseCode;
		this.properties = properties;
		this.propertyKey = propertyKey;
		this.solrCollection = solrCollection;
		this.panlCollectionUri = panlCollectionUri;
		this.lpseLength = lpseLength;

		if (!propertyKey.equals(PROPERTY_KEY_PANL_SORT_FIELDS)) {
			// sort keys can be longer than the panlParamSort property code

			if (this.lpseCode.length() != lpseLength) {
				throw new PanlServerException(propertyKey + " LPSE code of '" + this.lpseCode + "' has invalid lpse length of " + lpseCode.length() + " is of invalid length - should be " + lpseLength);
			}
		}
	}

	/**
	 * <p>Default validation of properties - this does not set any properties.</p>
	 *
	 * @throws PanlServerException If there was an error parsing the properties
	 */
	protected void validateProperties() throws PanlServerException {
		boolean hasErrors = false;
		// check for or facet and range facet
		boolean orFacet = properties.getProperty(PROPERTY_KEY_PANL_OR_FACET + this.lpseCode, "false").equals("true");
		boolean rangeFacet = properties.getProperty(PROPERTY_KEY_PANL_RANGE_FACET + this.lpseCode, "false").equals("true");
		if (orFacet && rangeFacet) {
			hasErrors = true;
			getLogger().error("You __MAY_NOT__ set a facet to both OR and RANGE.  Properties: '{}{}' and '{}{}'.",
					PROPERTY_KEY_PANL_OR_FACET,
					this.lpseCode,
					PROPERTY_KEY_PANL_RANGE_FACET,
					this.lpseCode);
		}

		if (rangeFacet) {
			String infix = properties.getProperty(PROPERTY_KEY_PANL_RANGE_INFIX + this.lpseCode, null);
			if (null != infix) {
				if (infix.equals("-")) {
					hasErrors = true;
					getLogger().error("You __MAY_NOT__ set an infix value to the minus character '-'.  Property: '{}{}'.",
							PROPERTY_KEY_PANL_RANGE_INFIX,
							this.lpseCode);
				} else if (infix.contains("-")) {
					getLogger().warn("Setting an infix value that contains the minus character '-' __MAY__ cause parsing errors.  Property: '{}{}'.",
							PROPERTY_KEY_PANL_RANGE_INFIX,
							this.lpseCode);
				}
			}
		}

		if (hasErrors) {
			throw new PanlServerException("FATAL Property validation errors, not continuing.");
		}
	}

	protected void populateSolrFieldTypeValidation() {
		if (null == this.solrFieldType) {
			this.solrFieldType = properties.getProperty(PROPERTY_KEY_PANL_TYPE + lpseCode);
			switch (this.solrFieldType) {
				case TYPE_SOLR_INT_POINT_FIELD:
				case TYPE_SOLR_LONG_POINT_FIELD:
					this.validationType = VALIDATION_TYPE_NUMBER;
					break;
				case TYPE_SOLR_DOUBLE_POINT_FIELD:
				case TYPE_SOLR_FLOAT_POINT_FIELD:
					this.validationType = VALIDATION_TYPE_DECIMAL;
					break;
				case TYPE_SOLR_DATE_POINT_FIELD:
					this.validationType = VALIDATION_TYPE_DATE;
					break;
				default:
					this.validationType = VALIDATION_TYPE_NONE;
			}
		}
	}

	/**
	 * <p>Populate the names for both Solr and Panl. THe Solr name is the field
	 * name.  The Panl name is either set, or will default to the Solr field
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

	/**
	 * <p>Get the logger for the child object.</p>
	 *
	 * @return The logger for the object
	 */
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
	 * <p>URL Decodes the URI Path value and validates it.</p>
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
	 * @return The URL decoded value This will return <code>null</code> if it is
	 * 		invalid.
	 */
	public String getDecodedValue(String value) {
		String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);

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
	protected String getValidatedValue(String temp) {
		// TODO - should change this to objects...
		String replaced;
		switch (this.validationType) {
			case VALIDATION_TYPE_DATE:
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
		return (URLEncoder.encode(value, StandardCharsets.UTF_8));
	}

	public String getEncodedPanlValue(LpseToken token) {
		if (null == token.getValue()) {
			return ("");
		}
		return (getEncodedPanlValue(token.getValue()));
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

					// we only every have one
					return(sb.toString());
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
	 *   include whether it has an infix or not.
	 *   <ul>
	 *     <li>If it has an infix, the returned string will be
	 *     <code>&lt;lpse_code&gt;-&lt;lpse_code&gt;</code></li>
	 *     <li>If it does not have an infix, then the returned string will be of
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
							sb.append((facetLpseToken.getHasMidfix() ? "-" : "+"));
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

	/**
	 * <p>The reset LPSE code, this will reset the LPSE code where adding a new
	 * filter to the query will want the user to go back to page 1.</p>
	 *
	 * @param panlTokenMap The panlToken map to see if a list of tokens is
	 * 		available to generate the resetLpseCode for
	 * @param collectionProperties The collection properties to look up defaults
	 * 		if required
	 *
	 * @return The reset LPSE code, or an empty string if there are no tokens in
	 * 		the query for this
	 */
	public String getResetLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		return (getLpseCode(panlTokenMap, collectionProperties));
	}

	/**
	 * <p>A human readable list of explanations for debugging purposes</p>
	 *
	 * @return A list of human-readable strings.
	 */
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
				"', Panl name '" +
				panlFieldName +
				"'.");

		temp.addAll(explainAdditional());

		temp.addAll(WARNING_MESSAGES);

		return (temp);
	}

	public abstract List<String> explainAdditional();

	/**
	 * <p>Apply the token to the Solr Query </p>
	 *
	 * @param solrQuery The Solr Query to apply to field to
	 * @param panlTokenMap The token map to get the parameters from
	 */
	public void applyToQuery(SolrQuery solrQuery, Map<String, List<LpseToken>> panlTokenMap) {
		// no facets, no query, all is good :)
		if (panlTokenMap.containsKey(getLpseCode())) {
			applyToQueryInternal(solrQuery, panlTokenMap.get(getLpseCode()));
		}
	}

	protected void logDetails() {
		getLogger().info("[ Solr/Panl '{}/{}' ] Mapping Solr facet field name '{}' of type '{}' to panl key '{}', LPSE length {}",
				solrCollection,
				panlCollectionUri,
				solrFieldName,
				solrFieldType,
				lpseCode,
				lpseLength);
	}

	public String getValuePrefix() {
		return ("");
	}

	public String getValueSuffix() {
		return ("");
	}

	public void addToAdditionObject(JSONObject additionObject, Map<String, List<LpseToken>> panlTokenMap) {
		// do nothing
	}

	/**
	 * <p>The internal implementation for applying the tokens to the SolrQuery</p>
	 *
	 * @param solrQuery The SolrQuery to apply the tokens to
	 * @param lpseTokenList The list of tokens to apply to the Solr query
	 */
	protected abstract void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokenList);

	protected void logWarnProperties(String lpseCode, String propertyKey) {
		if (properties.containsKey(propertyKey)) {
			String message = String.format("LPSE code '%s' has a property of '%s' which is invalid and should be removed.  (It has been ignored...)", lpseCode, propertyKey);
			getLogger().warn(message);
			WARNING_MESSAGES.add("[ CONFIGURATION WARNING ] " + message);
		}
	}

	/**
	 * <p>Append information to the available facet object.</p>
	 *
	 * <p>This will add the base information to the facet object, and then calls
	 * the internal method <code>appendAvailableObjectInternal</code> which sub
	 * classes can override and append additional information to the JSON
	 * object.</p>
	 *
	 * @param jsonObject The JSON object to append values to
	 */
	public void appendToAvailableFacetObject(JSONObject jsonObject) {
		jsonObject.put(JSON_KEY_FACET_NAME, this.solrFieldName);
		jsonObject.put(JSON_KEY_NAME, this.panlFieldName);
		jsonObject.put(JSON_KEY_PANL_CODE, this.lpseCode);

		appendToAvailableObjectInternal(jsonObject);
	}

	public boolean appendAvailableRangeValues(
			JSONObject facetObject,
			CollectionProperties collectionProperties,
			Map<String, List<LpseToken>> panlTokenMap) {
		return (false);
	}

	/**
	 * <p>Append to the facet object any available facet values (not including
	 * the currently selected facet value.</p>
	 *
	 * @param facetObject The facet object to append to
	 * @param collectionProperties The colleciton properties
	 * @param panlTokenMap The incoming Panl tokens
	 * @param existingLpseValues The existing LPSE values
	 * @param facetCountValues The facet count values
	 * @param numFound Number of results found
	 * @param numFoundExact Whether the number of results were exact
	 *
	 * @return Whether any values were appended
	 */
	public boolean appendAvailableValues(
			JSONObject facetObject,
			CollectionProperties collectionProperties,
			Map<String, List<LpseToken>> panlTokenMap,
			Set<String> existingLpseValues,
			List<FacetField.Count> facetCountValues,
			long numFound,
			boolean numFoundExact) {

		JSONArray facetValueArrays = new JSONArray();

		for (FacetField.Count value : facetCountValues) {
			// at this point - we need to see whether we already have the 'value'
			// as a facet - as there is no need to have it again
			boolean shouldAdd = true;

			String valueName = value.getName();

			// if we already have this value, then skip it
			if (existingLpseValues.contains(valueName)) {
				continue;
			}

			// if we have an or Facet and this is an or facet, then we keep all
			// values, otherwise we strip out the xero values
			if (value.getCount() == 0) {
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

	/**
	 * <p>Get the JSON object for the URIs that add this field (And consequently
	 * lengthen the URI).</p>
	 *
	 * @param collectionProperties The collection properties
	 * @param lpseField The LPSE field that this applies to
	 * @param panlTokenMap The inbound Panl tokens
	 *
	 * @return The JSON object with the URIs for adding this field to the
	 * 		existing search URI.
	 */
	protected JSONObject getAdditionURIObject(CollectionProperties collectionProperties, BaseField lpseField, Map<String, List<LpseToken>> panlTokenMap) {
		String additionLpseCode = lpseField.getLpseCode();
		JSONObject additionObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder(FORWARD_SLASH);
		StringBuilder lpseCode = new StringBuilder();

		for (BaseField baseField : collectionProperties.getLpseFields()) {

			if (panlTokenMap.containsKey(baseField.getLpseCode()) &&
					!(baseField.getLpseCode().equals(additionLpseCode))) {

				String resetUriPath = baseField.getResetUriPath(panlTokenMap, collectionProperties);
				lpseUri.append(resetUriPath);

				lpseCode.append(baseField.getResetLpseCode(panlTokenMap, collectionProperties));
			}

			if (baseField.getLpseCode().equals(additionLpseCode)) {

				additionObject.put(JSON_KEY_BEFORE, lpseUri.toString());
				lpseUri.setLength(0);
				lpseCode.append(baseField.getLpseCode());

				lpseUri.append(FORWARD_SLASH);
			}
		}

		additionObject.put(JSON_KEY_AFTER, lpseUri.toString() + lpseCode.toString() + FORWARD_SLASH);
		return (additionObject);
	}


	protected abstract void appendToAvailableObjectInternal(JSONObject jsonObject);

	/**
	 * <p>Instantiate a token for this field type.</p>
	 *
	 * @param collectionProperties The collection properties
	 * @param lpseCode The lpseCode for this field
	 * @param query The query parameter
	 * @param valueTokeniser The value tokeniser
	 * @param lpseTokeniser The lpse tokeniser
	 *
	 * @return The correct LPSE token for this lpseCode
	 */
	public abstract LpseToken instantiateToken(
			CollectionProperties collectionProperties,
			String lpseCode,
			String query,
			StringTokenizer valueTokeniser,
			LpseTokeniser lpseTokeniser);

	public void addToRemoveObject(JSONObject removeObject, LpseToken lpseToken) {

	}

	public boolean appendAvailableDateRangeValues(JSONObject dateRangeFacetObject, CollectionProperties collectionProperties, Map<String, List<LpseToken>> panlTokenMap) {
		return(false);
	}
}