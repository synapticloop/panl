package com.synapticloop.panl.server.handler.fielderiser.field.facet;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.processor.Processor;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.FacetLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.RangeFacetLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.bean.FromToBean;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
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
	 * <p>Populate the range properties</p>
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
			RangeFacetLpseToken rangeFacetLpseToken = (RangeFacetLpseToken) lpseToken;

			// even though this field is set to be a range facet, we still allow
			// single values

			if (rangeFacetLpseToken.getIsRangeToken()) {
				String value = (hasMinRangeWildcard && rangeFacetLpseToken.getValue().equals(getMinRange())) ? "*" : rangeFacetLpseToken.getValue();
				String toValue = (hasMaxRangeWildcard && rangeFacetLpseToken.getToValue().equals(getMaxRange())) ? "*" : rangeFacetLpseToken.getToValue();
				solrQuery.addFilterQuery(
						String.format("%s:[%s TO %s]",
								rangeFacetLpseToken.getSolrField(),
								value,
								toValue));
			} else {
				solrQuery.addFilterQuery(String.format("%s:\"%s\"",
						rangeFacetLpseToken.getSolrField(),
						rangeFacetLpseToken.getValue()));
			}
		}
	}

	@Override public String getLpseCode(LpseToken token, CollectionProperties collectionProperties) {
		RangeFacetLpseToken rangeFacetLpseToken = (RangeFacetLpseToken) token;
		return (rangeFacetLpseToken.getLpseCode() +
				(this.hasRangeInfix ? "-" : "+") +
				this.lpseCode);
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
		if (null != rangeMaxValueReplacement) {
			rangeFacetObject.put(Processor.JSON_KEY_RANGE_MAX_VALUE, URLEncoder.encode(rangeMaxValue, StandardCharsets.UTF_8));
		}

		if (null != rangeMinValueReplacement) {
			rangeFacetObject.put(Processor.JSON_KEY_RANGE_MIN_VALUE, URLEncoder.encode(rangeMinValue, StandardCharsets.UTF_8));
		}

		// if we already have this facet selected - add in the to and from
		// values - only allowed one facet code per range
		if (panlTokenMap.containsKey(lpseCode)) {
			RangeFacetLpseToken rangeFacetLpseToken = (RangeFacetLpseToken) panlTokenMap.get(lpseCode).get(0);
			if (null != rangeFacetLpseToken.getToValue()) {
				rangeFacetObject.put(JSON_KEY_VALUE, rangeFacetLpseToken.getValue());
				rangeFacetObject.put(JSON_KEY_VALUE_TO, rangeFacetLpseToken.getToValue());
			}
		}
		// addition URIs are a little bit different...
		JSONObject additionURIObject = getRangeAdditionURIObject(collectionProperties, panlTokenMap, true);
		rangeFacetObject.put(JSON_KEY_URIS, additionURIObject);


		return (true);
	}

	protected JSONObject getRangeAdditionURIObject(CollectionProperties collectionProperties, Map<String, List<LpseToken>> panlTokenMap, boolean shouldRange) {
		String additionLpseCode = lpseCode;
		JSONObject additionObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder(FORWARD_SLASH);
		StringBuilder lpseUriAfterMax = new StringBuilder();
		StringBuilder lpseCodeUri = new StringBuilder();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			if (panlTokenMap.containsKey(baseField.getLpseCode()) &&
					!(shouldRange &&
							baseField.getLpseCode().equals(additionLpseCode))) {

				String resetUriPath = baseField.getResetUriPath(panlTokenMap, collectionProperties);
				lpseUri.append(resetUriPath);

				if(lpseUriAfterMax.length() != 0) {
					lpseUriAfterMax.append(resetUriPath);
				}

				lpseCodeUri.append(baseField.getResetLpseCode(panlTokenMap, collectionProperties));
			}

			if (baseField.getLpseCode().equals(additionLpseCode)) {
				if (shouldRange) {
					// depends on whether there is an infix
					// at this point we want to also do the min value replacement, if it
					// exists
					if(null != rangeMaxValueReplacement) {
						additionObject.put(JSON_KEY_BEFORE_MIN_VALUE, lpseUri.toString() + URLEncoder.encode(rangeMinValueReplacement, StandardCharsets.UTF_8));
					}

					if (hasRangeInfix) {
						// we have an infix - we will be using the range value prefix/suffix
						lpseUri.append(URLEncoder.encode(getRangePrefix(), StandardCharsets.UTF_8));
					} else {
						// we don't have an infix - we will be using the value prefix/suffix
						lpseUri.append(URLEncoder.encode(getValuePrefix(), StandardCharsets.UTF_8));
					}

					lpseCodeUri.append(lpseCode);
					lpseCodeUri.append((hasRangeInfix ? "-" : "+"));

					if (getHasRangeInfix()) {
						// we have the infix
						additionObject.put(JSON_KEY_HAS_INFIX, true);
						additionObject.put(JSON_KEY_DURING, URLEncoder.encode(getRangeValueInfix(), StandardCharsets.UTF_8));
					} else {
						// we shall use the value suffix and prefix;
						additionObject.put(JSON_KEY_HAS_INFIX, false);
						additionObject.put(
								JSON_KEY_DURING,
								URLEncoder.encode(getValueSuffix(), StandardCharsets.UTF_8) +
										JSON_VALUE_NO_INFIX_REPLACEMENT +
										URLEncoder.encode(getValuePrefix(), StandardCharsets.UTF_8));
					}
				}

				additionObject.put(JSON_KEY_BEFORE, lpseUri.toString());
				lpseUri.setLength(0);
				lpseCodeUri.append(baseField.getLpseCode());

				if(shouldRange) {
					if(getHasRangeInfix()) {
						lpseUri.append(URLEncoder.encode(getRangeSuffix(), StandardCharsets.UTF_8));
					} else {
						lpseUri.append(URLEncoder.encode(getValueSuffix(), StandardCharsets.UTF_8));
					}

					if(null != rangeMaxValueReplacement) {
						lpseUriAfterMax.append(URLEncoder.encode(rangeMaxValueReplacement, StandardCharsets.UTF_8))
								.append(FORWARD_SLASH);
					}
				}
				lpseUri.append(FORWARD_SLASH);
			}
		}

		additionObject.put(JSON_KEY_AFTER, lpseUri.toString() + lpseCodeUri.toString() + FORWARD_SLASH);
		additionObject.put(JSON_KEY_AFTER_MAX_VALUE, lpseUriAfterMax.toString() + lpseCodeUri.toString() + FORWARD_SLASH);
		return (additionObject);
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


	/**
	 * <p>Decode a range facet values which are in one of two formats, which
	 * depends on whether the RANGE facet has <code>hasRangeInfix</code> set.</p>
	 *
	 * <p><strong><code>hasRangeInfix == true</code></strong></p>
	 *
	 * <ul>
	 *   <li>The value will be URL decoded, then</li>
	 *   <li>The value will be split on the <code>rangeInfix</code></li>
	 *   <li>If the split is exactly two Strings, carry on else return null.</li>
	 * </ul>
	 *
	 * <p><strong><code>hasRangeInfix == false</code></strong></p>
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

		if (hasRangeInfix) {
			// It is OK to decode the value as it is all in one
			String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);
			// then we need to split by the infix
			String[] fromToSplit = decodedValue.split(rangeValueInfix);
			if (fromToSplit.length != 2) {
				return (null);
			} else {
				fromString = fromToSplit[0];
				toString = fromToSplit[1];
			}
		} else {
			String[] fromToSplit = value.split(Processor.JSON_VALUE_NO_INFIX_REPLACEMENT);
			if (fromToSplit.length != 2) {
				return (null);
			} else {
				fromString = URLDecoder.decode(fromToSplit[0], StandardCharsets.UTF_8);
				toString = URLDecoder.decode(fromToSplit[1], StandardCharsets.UTF_8);
			}
		}

		// at this point we have two values, the from and to - although they may
		// have a min or max value replacement


		if (hasRangeInfix) {
			if (null != rangeMinValueReplacement) {
				if (fromString.equals(rangeMinValueReplacement)) {
					fromString = getMinRange();
				}
			} else if (hasRangePrefix) {
				if (fromString.startsWith(rangePrefix)) {
					fromString = fromString.substring(rangePrefix.length());
				} else {
					return (null);
				}
			}

			if (null != rangeMaxValueReplacement) {
				if (toString.equals(rangeMaxValueReplacement)) {
					toString = getMaxRange();
				} else if (hasRangeSuffix) {
					if (toString.endsWith(rangeSuffix)) {
						toString = toString.substring(0, toString.length() - rangeSuffix.length());
					} else {
						return (null);
					}
				}
			}
		} else {
			if (null != rangeMinValueReplacement) {
				if (fromString.equals(rangeMinValueReplacement)) {
					fromString = getMinRange();
				}
			} else if (hasValuePrefix) {
				if (fromString.startsWith(valuePrefix)) {
					fromString = fromString.substring(valuePrefix.length());
				} else {
					return (null);
				}
			}

			if (null != rangeMaxValueReplacement) {
				if (toString.equals(rangeMaxValueReplacement)) {
					toString = getMaxRange();
				}
			} else if (hasValueSuffix) {
				if (toString.endsWith(valueSuffix)) {
					toString = toString.substring(0, toString.length() - valueSuffix.length());
				} else {
					return (null);
				}
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

	public String getLpseCode(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder();
		boolean hasRange = false;
		for (LpseToken lpseToken : panlTokenMap.getOrDefault(lpseCode, new ArrayList<>())) {
			if (lpseToken.getIsValid()) {
				// this facet could be either a range facet, or just a simple facet
				// value
				RangeFacetLpseToken rangeFacetLpseToken = (RangeFacetLpseToken) lpseToken;
				if (rangeFacetLpseToken.getIsRangeToken() && !hasRange) {
					sb.append(lpseCode);
					sb.append((rangeFacetLpseToken.getHasInfix() ? "-" : "+"));
					sb.append(lpseCode);

					// there may be only one range
					hasRange = true;
				} else {
					// we may have multiple values for the non range token
					sb.append(lpseCode);
				}
			}
		}
		return (sb.toString());
	}

	public String getEncodedPanlValue(LpseToken token) {
		if (null == token.getValue()) {
			return ("");
		}

		if (isRangeFacet) {
			return (getEncodedRangeFacetValueUriPart((RangeFacetLpseToken) token));
		} else {
			return (getEncodedPanlValue(token.getValue()));
		}
	}

	/**
	 * <p>For a specific Token which is a range value, get the encoded URI path
	 * value.  This will take care of prefixes, suffixes, min and max range
	 * values, and range prefix/suffixes, and infix if available</p>
	 *
	 * @param facetLpseToken The FacetLpseToken to interrogate
	 *
	 * @return The encoded URI path part for a range token
	 */
	private String getEncodedRangeFacetValueUriPart(RangeFacetLpseToken facetLpseToken) {

		// we can still have a single facet value which is not a range facet
		if (null == facetLpseToken.getToValue()) {
			return (getEncodedPanlValue(facetLpseToken.getValue()));
		}

		// at this point it is a range facet
		StringBuilder sb = new StringBuilder();

		if (hasRangeInfix) {
			if (facetLpseToken.getValue().equals(rangeMinValue) && rangeMinValueReplacement != null) {
				sb.append(rangeMinValueReplacement);
			} else {
				if (hasRangePrefix) {
					sb.append(rangePrefix);
				} else if (hasValuePrefix) {
					sb.append(valuePrefix);
				}

				sb.append(facetLpseToken.getValue());

				if (hasValueSuffix) {
					sb.append(valueSuffix);
				}
			}

			sb.append(rangeValueInfix);

			if (facetLpseToken.getToValue().equals(rangeMaxValue) && rangeMaxValueReplacement != null) {
				sb.append(rangeMaxValueReplacement);
			} else {
				if (hasValuePrefix) {
					sb.append(valuePrefix);
				}

				sb.append(facetLpseToken.getToValue());

				if (hasRangeSuffix) {
					sb.append(rangeSuffix);
				} else if (hasValueSuffix) {
					sb.append(valueSuffix);
				}
			}

			return (URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8));
		} else {
			// we will have a two part URI path, split by a '~' and both values need
			// to be URLEncoded before.
			if (facetLpseToken.getValue().equals(rangeMinValue) && null != rangeMinValueReplacement) {
				sb.append(URLEncoder.encode(rangeMinValueReplacement, StandardCharsets.UTF_8));
			} else {
				if (hasValuePrefix) {
					sb.append(URLEncoder.encode(valuePrefix, StandardCharsets.UTF_8));
				}
				sb.append(URLEncoder.encode(facetLpseToken.getValue(), StandardCharsets.UTF_8));
				if (hasValueSuffix) {
					sb.append(URLEncoder.encode(valueSuffix, StandardCharsets.UTF_8));
				}

			}

			sb.append(Processor.JSON_VALUE_NO_INFIX_REPLACEMENT);

			if (facetLpseToken.getToValue().equals(rangeMaxValue) && null != rangeMaxValueReplacement) {
				sb.append(URLEncoder.encode(rangeMaxValueReplacement, StandardCharsets.UTF_8));
			} else {
				if (hasValuePrefix) {
					sb.append(URLEncoder.encode(valuePrefix, StandardCharsets.UTF_8));
				}
				sb.append(URLEncoder.encode(facetLpseToken.getToValue(), StandardCharsets.UTF_8));
				if (hasValueSuffix) {
					sb.append(URLEncoder.encode(valueSuffix, StandardCharsets.UTF_8));
				}
			}

			return (sb.toString());
		}
	}

	@Override protected JSONObject getAdditionURIObject(CollectionProperties collectionProperties, BaseField lpseField, Map<String, List<LpseToken>> panlTokenMap) {
		String additionLpseCode = lpseField.getLpseCode();
		JSONObject additionObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder(FORWARD_SLASH);
		StringBuilder lpseUriAfterMax = new StringBuilder();
		StringBuilder lpseCode = new StringBuilder();

		for (BaseField baseField : collectionProperties.getLpseFields()) {

			if (panlTokenMap.containsKey(baseField.getLpseCode()) &&
					!(baseField.getLpseCode().equals(additionLpseCode))) {

				String resetUriPath = baseField.getResetUriPath(panlTokenMap, collectionProperties);
				lpseUri.append(resetUriPath);

				if (lpseUriAfterMax.length() != 0) {
					lpseUriAfterMax.append(resetUriPath);
				}

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
		additionObject.put(JSON_KEY_AFTER_MAX_VALUE, lpseUriAfterMax.toString() + lpseCode.toString() + FORWARD_SLASH);
		return (additionObject);
	}

	public String getMinRange() {
		if (hasMinRange) {
			return (rangeMinValue);
		} else {
			// TODO - needs to be based on the field type
			return (Integer.toString(Integer.MIN_VALUE));
		}
	}

	public String getMaxRange() {
		if (hasMaxRange) {
			return (rangeMaxValue);
		} else {
			// TODO - needs to be based on the field type
			return (Integer.toString(Integer.MAX_VALUE));
		}
	}

	public boolean getHasRangeInfix() {
		return (hasRangeInfix);
	}

	public String getRangeValueInfix() {
		return (rangeValueInfix);
	}

	public String getRangePrefix() {
		if (hasRangePrefix) {
			return (rangePrefix);
		} else {
			return ("");
		}
	}

	public String getRangeSuffix() {
		if (hasRangeSuffix) {
			return (rangeSuffix);
		} else {
			return ("");
		}
	}


	public String getPrefix() {
		if (hasRangeInfix) {
			return (getRangePrefix());
		} else {
			return (getValuePrefix());
		}
	}

	public LpseToken instantiateToken(CollectionProperties collectionProperties, String lpseCode, String query, StringTokenizer valueTokeniser, LpseTokeniser lpseTokeniser) {
		return(new RangeFacetLpseToken(collectionProperties, this.lpseCode, lpseTokeniser, valueTokeniser));
	}

}
