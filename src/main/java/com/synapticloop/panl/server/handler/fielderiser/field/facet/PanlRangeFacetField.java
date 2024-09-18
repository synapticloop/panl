package com.synapticloop.panl.server.handler.fielderiser.field.facet;

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
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.processor.Processor;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
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
	// LOGGER
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlRangeFacetField.class);

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	//                        RANGE Facet property keys                        //
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	public static final String PROPERTY_KEY_PANL_RANGE_MIN = "panl.range.min.";
	public static final String PROPERTY_KEY_PANL_RANGE_MAX = "panl.range.max.";
	public static final String PROPERTY_KEY_PANL_RANGE_MIN_WILDCARD = "panl.range.min.wildcard.";
	public static final String PROPERTY_KEY_PANL_RANGE_MAX_WILDCARD = "panl.range.max.wildcard.";
	public static final String PROPERTY_KEY_PANL_RANGE_SUPPRESS = "panl.range.suppress.";
	public static final String PROPERTY_KEY_PANL_RANGE_PREFIX = "panl.range.prefix.";
	public static final String PROPERTY_KEY_PANL_RANGE_SUFFIX = "panl.range.suffix.";
	public static final String PROPERTY_KEY_PANL_RANGE_MIN_VALUE = "panl.range.min.value.";
	public static final String PROPERTY_KEY_PANL_RANGE_MAX_VALUE = "panl.range.max.value.";

	public static final String JSON_KEY_IS_RANGE_FACET = "is_range_facet";
	public static final String JSON_KEY_BEFORE_MIN_VALUE = "before_min_value";
	public static final String JSON_KEY_RANGE_MAX_VALUE = "range_max_value";
	public static final String JSON_KEY_RANGE_MIN_VALUE = "range_min_value";

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	//                          RANGE Facet properties                         //
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	private boolean hasMinRange = false; // whether a minimum range is set
	private final String rangeMinValue; // the minimum range value

	private boolean hasMaxRange = false; // whether a maximum range has been set
	private final String rangeMaxValue; // the maximum range value

	private boolean hasRangeInfix = false; // whether this has a range infix
	private final String rangeValueInfix;

	private final String rangeMinValueReplacement;
	private final String rangeMaxValueReplacement;

	private boolean hasRangePrefix = false;
	private final String rangePrefix;
	private boolean hasRangeSuffix = false;
	private final String rangeSuffix;

	private final boolean hasMinRangeWildcard;
	private final boolean hasMaxRangeWildcard;

	private boolean rangeSuppress = false;


	public PanlRangeFacetField(String lpseCode, String propertyKey, Properties properties, String solrCollection, String panlCollectionUri, int lpseLength) throws PanlServerException {
		super(lpseCode, propertyKey, properties, solrCollection, panlCollectionUri, lpseLength);

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
		this.rangeSuppress = properties.getProperty(PROPERTY_KEY_PANL_RANGE_SUPPRESS + lpseCode, "false").equals("true");
	}

	@Override
	public LpseToken instantiateToken(CollectionProperties collectionProperties, String lpseCode, String query, StringTokenizer valueTokeniser, LpseTokeniser lpseTokeniser) {
		return (new RangeFacetLpseToken(collectionProperties, this.lpseCode, lpseTokeniser, valueTokeniser));
	}

	@Override public List<String> explainAdditional() {
		List<String> explanations = new ArrayList<>(super.explainAdditional());
		explanations.add("Is a RANGE facet which will allow either a single value to be selected, or values within a range.");

		if(hasRangePrefix) {
			explanations.add("Has a range prefix of '" + rangePrefix + "'.");
		} else {
			explanations.add("No range prefix");
		}

		if(hasRangeInfix) {
			explanations.add("Has a range infix of '" + rangeValueInfix + "'.");
		} else {
			explanations.add("No range infix, and is set to the default '" + JSON_VALUE_NO_INFIX_REPLACEMENT + "'.");
		}

		if(hasRangeSuffix) {
			explanations.add("Has a range suffix of '" + rangeSuffix + "'.");
		} else {
			explanations.add("No range suffix");
		}

		if(hasMinRange) {
			explanations.add("Has the minimum range value set at '" + rangeMinValue + "'.");
			if(null != rangeMinValueReplacement) {
				explanations.add("Additionally, if the value is the minimum range, the replacement for this value is set to  '" + rangeMinValueReplacement + "'.");
			}
		} else {
			explanations.add("Does __NOT__ have a minimum range value set.");
		}

		if(hasMaxRange) {
			explanations.add("Has the maximum range value set at '" + rangeMaxValue + "'.");
			if(null != rangeMinValueReplacement) {
				explanations.add("Additionally, if the value is the maximum range, the replacement for this value is set to  '" + rangeMaxValueReplacement + "'.");
			}
		} else {
			explanations.add("Does __NOT__ have a maximum range value set.");
		}

		if(hasMinRangeWildcard) {
			explanations.add("When the range value is the minimum configured value, the Solr query will include any results less than the minimum value (wildcard '*' search).");
		} else {
			explanations.add("The Solr query will include any results equal to or greater to the minimum value (inclusive search).");
		}

		if(hasMaxRangeWildcard) {
			explanations.add("When the range value is the maximum configured value, the Solr query will include any results greater than the maximum value (wildcard '*' search).");
		} else {
			explanations.add("The Solr query will include any results equal to or less than the maximum value (inclusive search).");
		}


		return (explanations);
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

	@Override public void appendToAvailableObjectInternal(JSONObject jsonObject) {
		jsonObject.put(JSON_KEY_IS_RANGE_FACET, true);
	}

	/**
	 * <p>Append available Solr facet values (including URIs for addition) to the
	 * passed in JSON Object.</p>
	 *
	 * <p>OR facets are different, in that they allow multiple selection facet
	 * values for an otherwise single selection</p>
	 *
	 * @param facetObject The facet object to append the values to
	 * @param collectionProperties The collection properties
	 * @param panlTokenMap The panl token map
	 * @param existingLpseValues The existing LPSE values for this lpseCode
	 * @param facetCountValues The Facet values from Solr
	 * @param numFound The number of results found
	 * @param numFoundExact Whether the number of results found is an exact
	 * 		number
	 *
	 * @return Whether any values were appended to the available facet object
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

		// if we currently have the facet value, don't do anything
		Set<String> currentValueSet = new HashSet<>();
		for (LpseToken lpseToken : panlTokenMap.getOrDefault(this.lpseCode, new ArrayList<>())) {
			currentValueSet.add(lpseToken.getValue());
		}


		for (FacetField.Count value : facetCountValues) {
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
		rangeFacetObject.put(JSON_KEY_FACET_NAME, collectionProperties.getSolrFieldNameFromLpseCode(lpseCode));
		rangeFacetObject.put(JSON_KEY_NAME, collectionProperties.getPanlNameFromPanlCode(lpseCode));
		rangeFacetObject.put(JSON_KEY_PANL_CODE, lpseCode);
		rangeFacetObject.put(JSON_KEY_MIN, getMinRange());
		rangeFacetObject.put(JSON_KEY_MAX, getMaxRange());
		rangeFacetObject.put(JSON_KEY_PREFIX, URLEncoder.encode(getValuePrefix(), StandardCharsets.UTF_8));
		rangeFacetObject.put(JSON_KEY_SUFFIX, URLEncoder.encode(getValueSuffix(), StandardCharsets.UTF_8));

		// range min and max values
		if (null != rangeMaxValueReplacement) {
			rangeFacetObject.put(JSON_KEY_RANGE_MAX_VALUE, URLEncoder.encode(rangeMaxValueReplacement, StandardCharsets.UTF_8));
		}

		if (null != rangeMinValueReplacement) {
			rangeFacetObject.put(JSON_KEY_RANGE_MIN_VALUE, URLEncoder.encode(rangeMinValueReplacement, StandardCharsets.UTF_8));
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
		JSONObject additionURIObject = getRangeAdditionURIObject(collectionProperties, panlTokenMap);
		rangeFacetObject.put(JSON_KEY_URIS, additionURIObject);


		return (true);
	}

	private JSONObject getRangeAdditionURIObject(
			CollectionProperties collectionProperties,
			Map<String, List<LpseToken>> panlTokenMap) {

		String additionLpseCode = lpseCode;
		JSONObject additionObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder(FORWARD_SLASH);
		StringBuilder lpseUriAfterMax = new StringBuilder();
		StringBuilder lpseCodeUri = new StringBuilder();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			if (panlTokenMap.containsKey(baseField.getLpseCode()) &&
					!(baseField.getLpseCode().equals(additionLpseCode))) {

				String resetUriPath = baseField.getResetUriPath(panlTokenMap, collectionProperties);
				lpseUri.append(resetUriPath);

				if (lpseUriAfterMax.length() != 0) {
					lpseUriAfterMax.append(resetUriPath);
				}

				lpseCodeUri.append(baseField.getResetLpseCode(panlTokenMap, collectionProperties));
			}

			if (baseField.getLpseCode().equals(additionLpseCode)) {
					// depends on whether there is an infix
					// at this point we want to also do the min value replacement, if it
					// exists
					if (null != rangeMinValueReplacement) {
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

					if (hasRangeInfix) {
						// we have the infix
						additionObject.put(JSON_KEY_HAS_INFIX, true);
						additionObject.put(JSON_KEY_DURING, URLEncoder.encode(rangeValueInfix, StandardCharsets.UTF_8));
					} else {
						// we shall use the value suffix and prefix;
						additionObject.put(JSON_KEY_HAS_INFIX, false);
						additionObject.put(
								JSON_KEY_DURING,
								URLEncoder.encode(getValueSuffix(), StandardCharsets.UTF_8) +
										JSON_VALUE_NO_INFIX_REPLACEMENT +
										URLEncoder.encode(getValuePrefix(), StandardCharsets.UTF_8));
					}


				additionObject.put(JSON_KEY_BEFORE, lpseUri.toString());
				lpseUri.setLength(0);
				lpseCodeUri.append(baseField.getLpseCode());

					if (hasRangeInfix) {
						lpseUri.append(URLEncoder.encode(getRangeSuffix(), StandardCharsets.UTF_8));
					} else {
						lpseUri.append(URLEncoder.encode(getValueSuffix(), StandardCharsets.UTF_8));
					}

					if (null != rangeMaxValueReplacement) {
						lpseUriAfterMax.append(URLEncoder.encode(rangeMaxValueReplacement, StandardCharsets.UTF_8))
								.append(FORWARD_SLASH);
					}
				lpseUri.append(FORWARD_SLASH);
			}
		}

		additionObject.put(JSON_KEY_AFTER, lpseUri.toString() + lpseCodeUri.toString() + FORWARD_SLASH);

		if (null != rangeMaxValueReplacement) {
			additionObject.put(JSON_KEY_AFTER_MAX_VALUE, lpseUriAfterMax.toString() + lpseCodeUri.toString() + FORWARD_SLASH);
		}
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

		boolean hasFrom = false;
		boolean hasTo = false;

		// if we have a range infix - then there is either a min/max value
		// replacement, or there is a range prefix/suffix

		if (hasRangeInfix) {
			// It is OK to decode the value as it is all in one
			String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);
			// then we need to split by the infix
			String[] fromToSplit = decodedValue.split(rangeValueInfix);

			// at this point, with a range infix, determine whether we have a min
			// /max value replacement
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
		// have a min or max value replacement, and we need to remove the ranges


		if (hasRangeInfix) {
			// test the range min value replacement
			if (null != rangeMinValueReplacement) {
				if (fromString.equals(rangeMinValueReplacement)) {
					fromString = getMinRange();
				} else if (hasRangePrefix) {
					if (fromString.startsWith(rangePrefix)) {
						fromString = fromString.substring(rangePrefix.length());
					} else {
						return (null);
					}
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

	public String getEncodedPanlValue(LpseToken lpseToken) {
		if (null == lpseToken.getValue()) {
			return ("");
		}
		return (getEncodedRangeFacetValueUriPart((RangeFacetLpseToken) lpseToken));
	}

	/**
	 * <p>For a specific Token which is a range value, get the encoded URI path
	 * value.  This will take care of prefixes, suffixes, min and max range
	 * values, and range prefix/suffixes, and infix if available</p>
	 *
	 * @param rangeFacetLpseToken The FacetLpseToken to interrogate
	 *
	 * @return The encoded URI path part for a range token
	 */
	private String getEncodedRangeFacetValueUriPart(RangeFacetLpseToken rangeFacetLpseToken) {

		// we can still have a single facet value which is not a range facet, if
		// this is the case, just return the normal encoded, non-range facet
		// value
		if (null == rangeFacetLpseToken.getToValue()) {
			return (getEncodedPanlValue(rangeFacetLpseToken.getValue()));
		}

		// at this point it is a range facet
		StringBuilder sb = new StringBuilder();

		if (hasRangeInfix) {
			if (rangeFacetLpseToken.getValue().equals(rangeMinValue) && rangeMinValueReplacement != null) {
				sb.append(rangeMinValueReplacement);
			} else {
				if (hasRangePrefix) {
					sb.append(rangePrefix);
				} else if (hasValuePrefix) {
					sb.append(valuePrefix);
				}

				sb.append(rangeFacetLpseToken.getValue());

				if (hasValueSuffix) {
					sb.append(valueSuffix);
				}
			}

			sb.append(rangeValueInfix);

			if (rangeFacetLpseToken.getToValue().equals(rangeMaxValue) && rangeMaxValueReplacement != null) {
				sb.append(rangeMaxValueReplacement);
			} else {
				if (hasValuePrefix) {
					sb.append(valuePrefix);
				}

				sb.append(rangeFacetLpseToken.getToValue());

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
			if (rangeFacetLpseToken.getValue().equals(rangeMinValue) && null != rangeMinValueReplacement) {
				sb.append(URLEncoder.encode(rangeMinValueReplacement, StandardCharsets.UTF_8));
			} else {
				if (hasValuePrefix) {
					sb.append(URLEncoder.encode(valuePrefix, StandardCharsets.UTF_8));
				}
				sb.append(URLEncoder.encode(rangeFacetLpseToken.getValue(), StandardCharsets.UTF_8));
				if (hasValueSuffix) {
					sb.append(URLEncoder.encode(valueSuffix, StandardCharsets.UTF_8));
				}

			}

			sb.append(Processor.JSON_VALUE_NO_INFIX_REPLACEMENT);

			if (rangeFacetLpseToken.getToValue().equals(rangeMaxValue) && null != rangeMaxValueReplacement) {
				sb.append(URLEncoder.encode(rangeMaxValueReplacement, StandardCharsets.UTF_8));
			} else {
				if (hasValuePrefix) {
					sb.append(URLEncoder.encode(valuePrefix, StandardCharsets.UTF_8));
				}
				sb.append(URLEncoder.encode(rangeFacetLpseToken.getToValue(), StandardCharsets.UTF_8));
				if (hasValueSuffix) {
					sb.append(URLEncoder.encode(valueSuffix, StandardCharsets.UTF_8));
				}
			}

			return (sb.toString());
		}
	}

	@Override protected JSONObject getAdditionURIObject(
			CollectionProperties collectionProperties,
			BaseField lpseField,
			Map<String, List<LpseToken>> panlTokenMap) {

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

	public void addToRemoveObject(JSONObject removeObject, LpseToken lpseToken) {
		removeObject.put(JSON_KEY_IS_RANGE_FACET, true);

		RangeFacetLpseToken rangeFacetLpseToken = (RangeFacetLpseToken) lpseToken;
		if (rangeFacetLpseToken.getIsRangeToken()) {
			removeObject.put(JSON_KEY_HAS_INFIX, hasRangeInfix);

			String toValue = rangeFacetLpseToken.getToValue();

			if(null == toValue) {
				removeObject.remove(JSON_KEY_VALUE_TO);
			} else {
				removeObject.put(JSON_KEY_VALUE_TO, toValue);
			}
		}
	}

	public boolean getRangeSuppress() {
		return rangeSuppress;
	}
}
