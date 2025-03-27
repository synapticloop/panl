package com.synapticloop.panl.server.handler.fielderiser.field.facet;

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
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.RangeFacetLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.bean.FromToBean;
import com.synapticloop.panl.util.Constants;
import com.synapticloop.panl.util.PanlLPSEHelper;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PanlRangeFacetField extends PanlFacetField {
	// LOGGER
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlRangeFacetField.class);

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


	public PanlRangeFacetField(String lpseCode, String propertyKey, Properties properties, String solrCollection,
				String panlCollectionUri, int lpseLength) throws PanlServerException {
		super(lpseCode, propertyKey, properties, solrCollection, panlCollectionUri, lpseLength);

		// get the other properties, if they exist...
		this.rangeMinValue = properties.getProperty(Constants.Property.Panl.PANL_RANGE_MIN + lpseCode, null);
		if (null != this.rangeMinValue) {
			hasMinRange = true;
		}

		this.rangeMaxValue = properties.getProperty(Constants.Property.Panl.PANL_RANGE_MAX + lpseCode, null);
		if (null != this.rangeMaxValue) {
			hasMaxRange = true;
		}

		this.rangePrefix = properties.getProperty(Constants.Property.Panl.PANL_RANGE_PREFIX + lpseCode, null);
		if (null != this.rangePrefix) {
			hasRangePrefix = true;
		}

		this.rangeSuffix = properties.getProperty(Constants.Property.Panl.PANL_RANGE_SUFFIX + lpseCode, null);
		if (null != this.rangeSuffix) {
			hasRangeSuffix = true;
		}

		this.rangeValueInfix = properties.getProperty(Constants.Property.Panl.PANL_RANGE_INFIX + lpseCode, null);
		if (null != this.rangeValueInfix) {
			hasRangeInfix = true;
		}

		this.rangeMinValueReplacement = properties.getProperty(Constants.Property.Panl.PANL_RANGE_MIN_VALUE + lpseCode,
				null);
		this.rangeMaxValueReplacement = properties.getProperty(Constants.Property.Panl.PANL_RANGE_MAX_VALUE + lpseCode, null);
		this.hasMinRangeWildcard = properties.getProperty(Constants.Property.Panl.PANL_RANGE_MIN_WILDCARD + lpseCode, "false")
		                                     .equals("true");
		this.hasMaxRangeWildcard = properties.getProperty(Constants.Property.Panl.PANL_RANGE_MAX_WILDCARD + lpseCode, "false")
		                                     .equals("true");
		this.rangeSuppress = properties.getProperty(Constants.Property.Panl.PANL_RANGE_SUPPRESS + lpseCode, "false").equals("true");
	}

	@Override
	public List<LpseToken> instantiateTokens(CollectionProperties collectionProperties, String lpseCode, String query,
				StringTokenizer valueTokeniser, LpseTokeniser lpseTokeniser) {
		return (List.of(new RangeFacetLpseToken(collectionProperties, this.lpseCode, lpseTokeniser, valueTokeniser)));
	}

	@Override public List<String> explainAdditional() {
		List<String> explanations = new ArrayList<>(super.explainAdditional());
		explanations.add(
					"Is a RANGE facet which will allow either a single value to be selected, or values within a range.");

		if (hasRangePrefix) {
			explanations.add("Has a range prefix of '" + rangePrefix + "'.");
		} else {
			explanations.add("No range prefix");
		}

		if (hasRangeInfix) {
			explanations.add("Has a range infix of '" + rangeValueInfix + "'.");
		} else {
			explanations.add("No range infix, and is set to the default '" + Constants.JSON_VALUE_NO_INFIX_REPLACEMENT + "'.");
		}

		if (hasRangeSuffix) {
			explanations.add("Has a range suffix of '" + rangeSuffix + "'.");
		} else {
			explanations.add("No range suffix");
		}

		if (hasMinRange) {
			explanations.add("Has the minimum range value set at '" + rangeMinValue + "'.");
			if (null != rangeMinValueReplacement) {
				explanations.add(
							"Additionally, if the value is the minimum range, the replacement for this value is set to  '" + rangeMinValueReplacement + "'.");
			}
		} else {
			explanations.add("Does __NOT__ have a minimum range value set.");
		}

		if (hasMaxRange) {
			explanations.add("Has the maximum range value set at '" + rangeMaxValue + "'.");
			if (null != rangeMinValueReplacement) {
				explanations.add(
							"Additionally, if the value is the maximum range, the replacement for this value is set to  '" + rangeMaxValueReplacement + "'.");
			}
		} else {
			explanations.add("Does __NOT__ have a maximum range value set.");
		}

		if (hasMinRangeWildcard) {
			explanations.add(
						"When the range value is the minimum configured value, the Solr query will include any results less than the minimum value (wildcard '*' search).");
		} else {
			explanations.add(
						"The Solr query will include any results equal to or greater to the minimum value (inclusive search).");
		}

		if (hasMaxRangeWildcard) {
			explanations.add(
						"When the range value is the maximum configured value, the Solr query will include any results greater than the maximum value (wildcard '*' search).");
		} else {
			explanations.add(
						"The Solr query will include any results equal to or less than the maximum value (inclusive search).");
		}


		return (explanations);
	}


	@Override public Logger getLogger() {
		return (LOGGER);
	}

	@Override protected void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokens, CollectionProperties collectionProperties) {
		for(LpseToken lpseToken : lpseTokens) {
			RangeFacetLpseToken rangeFacetLpseToken = (RangeFacetLpseToken) lpseToken;

			// even though this field is set to be a range facet, we still allow
			// single values

			if (rangeFacetLpseToken.getIsRangeToken()) {
				String value = (hasMinRangeWildcard && rangeFacetLpseToken.getValue().equals(
							getMinRange())) ? "*" : rangeFacetLpseToken.getValue();
				String toValue = (hasMaxRangeWildcard && rangeFacetLpseToken.getToValue().equals(
							getMaxRange())) ? "*" : rangeFacetLpseToken.getToValue();
				solrQuery.addFilterQuery(
							rangeFacetLpseToken.getSolrField() +
										":[" +
										value +
										" TO " +
										toValue +
										"]");

			} else {
				solrQuery.addFilterQuery(
							rangeFacetLpseToken.getSolrField() +
										":\"" +
										rangeFacetLpseToken.getValue() +
										"\"");
			}
		}
	}

	@Override public String getLpseCode(LpseToken token, CollectionProperties collectionProperties) {
		RangeFacetLpseToken rangeFacetLpseToken = (RangeFacetLpseToken) token;

		// whilst this may be a range token, it may just be a single value
		if(null != rangeFacetLpseToken.getToValue()) {
			return (rangeFacetLpseToken.getLpseCode() + (this.hasRangeInfix ? "-" : "+"));
		} else {
			return (rangeFacetLpseToken.getLpseCode());
		}

	}

	@Override public void appendToAvailableObjectInternal(JSONObject jsonObject) {
		jsonObject.put(Constants.Json.Panl.IS_RANGE_FACET, true);
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
	 * @param numFoundExact Whether the number of results found is an exact number
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
		for(LpseToken lpseToken : panlTokenMap.getOrDefault(this.lpseCode, new ArrayList<>())) {
			currentValueSet.add(lpseToken.getValue());
		}


		for(FacetField.Count value : facetCountValues) {
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
				facetValueObject.put(Constants.Json.Panl.VALUE, valueName);
				facetValueObject.put(Constants.Json.Panl.COUNT, value.getCount());
				facetValueObject.put(Constants.Json.Panl.ENCODED, getEncodedPanlValue(valueName));
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
			facetObject.put(Constants.Json.Panl.VALUES, facetValueArrays);
			if (null != lpseCode) {
				facetObject.put(Constants.Json.Panl.URIS,
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
		rangeFacetObject.put(Constants.Json.Panl.FACET_NAME, collectionProperties.getSolrFieldNameFromLpseCode(lpseCode));
		rangeFacetObject.put(Constants.Json.Panl.NAME, collectionProperties.getPanlNameFromPanlCode(lpseCode));
		rangeFacetObject.put(Constants.Json.Panl.PANL_CODE, lpseCode);
		rangeFacetObject.put(Constants.Json.Panl.MIN, getMinRange());
		rangeFacetObject.put(Constants.Json.Panl.MAX, getMaxRange());
		rangeFacetObject.put(Constants.Json.Panl.PREFIX, PanlLPSEHelper.encodeURIPath(getValuePrefix()));
		rangeFacetObject.put(Constants.Json.Panl.SUFFIX, PanlLPSEHelper.encodeURIPath(getValueSuffix()));

		// range min and max values
		if (null != rangeMaxValueReplacement) {
			rangeFacetObject.put(Constants.Json.Panl.RANGE_MAX_VALUE,
					PanlLPSEHelper.encodeURIPath(rangeMaxValueReplacement));
		}

		if (null != rangeMinValueReplacement) {
			rangeFacetObject.put(Constants.Json.Panl.RANGE_MIN_VALUE,
					PanlLPSEHelper.encodeURIPath(rangeMinValueReplacement));
		}

		// if we already have this facet selected - add in the to and from
		// values - only allowed one facet code per range
		if (panlTokenMap.containsKey(lpseCode)) {
			RangeFacetLpseToken rangeFacetLpseToken = (RangeFacetLpseToken) panlTokenMap.get(lpseCode).get(0);
			if (null != rangeFacetLpseToken.getToValue()) {
				rangeFacetObject.put(Constants.Json.Panl.VALUE, rangeFacetLpseToken.getValue());
				rangeFacetObject.put(Constants.Json.Panl.VALUE_TO, rangeFacetLpseToken.getToValue());
			}
		}

		// addition URIs are a little bit different...
		JSONObject additionURIObject = getRangeAdditionURIObject(collectionProperties, panlTokenMap);
		rangeFacetObject.put(Constants.Json.Panl.URIS, additionURIObject);


		return (true);
	}

	private JSONObject getRangeAdditionURIObject(
				CollectionProperties collectionProperties,
				Map<String, List<LpseToken>> panlTokenMap) {

		String additionLpseCode = lpseCode;
		JSONObject additionObject = new JSONObject();
		StringBuilder lpseUri = new StringBuilder(Constants.FORWARD_SLASH);
		StringBuilder lpseUriAfterMax = new StringBuilder();
		StringBuilder lpseCodeUri = new StringBuilder();

		for(BaseField baseField : collectionProperties.getLpseFields()) {
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
					additionObject.put(Constants.Json.Panl.BEFORE_MIN_VALUE,
								lpseUri + PanlLPSEHelper.encodeURIPath(rangeMinValueReplacement));
				}

				if (hasRangeInfix) {
					// we have an infix - we will be using the range value prefix/suffix
					lpseUri.append(PanlLPSEHelper.encodeURIPath(getRangePrefix()));
				} else {
					// we don't have an infix - we will be using the value prefix/suffix
					lpseUri.append(PanlLPSEHelper.encodeURIPath(getValuePrefix()));
				}

				lpseCodeUri.append(lpseCode);
				lpseCodeUri.append((hasRangeInfix ? "-" : "+"));

				if (hasRangeInfix) {
					// we have the infix
					additionObject.put(Constants.Json.Panl.HAS_INFIX, true);
					additionObject.put(Constants.Json.Panl.DURING, PanlLPSEHelper.encodeURIPath(rangeValueInfix));
				} else {
					// we shall use the value suffix and prefix;
					additionObject.put(Constants.Json.Panl.HAS_INFIX, false);
					additionObject.put(
								Constants.Json.Panl.DURING,
							PanlLPSEHelper.encodeURIPath(getValueSuffix()) +
											Constants.JSON_VALUE_NO_INFIX_REPLACEMENT +
									PanlLPSEHelper.encodeURIPath(getValuePrefix()));
				}


				additionObject.put(Constants.Json.Panl.BEFORE, lpseUri.toString());
				lpseUri.setLength(0);

				if (hasRangeInfix) {
					lpseUri.append(PanlLPSEHelper.encodeURIPath(getRangeSuffix()));
				} else {
					lpseUri.append(PanlLPSEHelper.encodeURIPath(getValueSuffix()));
				}

				if (null != rangeMaxValueReplacement) {
					lpseUriAfterMax.append(PanlLPSEHelper.encodeURIPath(rangeMaxValueReplacement))
					               .append(Constants.FORWARD_SLASH);
				}

				lpseUri.append(Constants.FORWARD_SLASH);
			}
		}

		additionObject.put(Constants.Json.Panl.AFTER, lpseUri.toString() + lpseCodeUri.toString() + Constants.FORWARD_SLASH);

		if (null != rangeMaxValueReplacement) {
			additionObject.put(Constants.Json.Panl.AFTER_MAX_VALUE, lpseUriAfterMax.toString() + lpseCodeUri.toString() + Constants.FORWARD_SLASH);
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
			String[] fromToSplit = value.split(Constants.JSON_VALUE_NO_INFIX_REPLACEMENT);
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
		for(LpseToken lpseToken : panlTokenMap.getOrDefault(lpseCode, new ArrayList<>())) {
			if (lpseToken.getIsValid()) {
				// this facet could be either a range facet, or just a simple facet
				// value
				RangeFacetLpseToken rangeFacetLpseToken = (RangeFacetLpseToken) lpseToken;
				if (rangeFacetLpseToken.getIsRangeToken() && !hasRange) {
					sb.append(lpseCode);

					// this may not be a range facet, it may just be a single value....
					// so check to see whether it has a toValue
					if(null != rangeFacetLpseToken.getToValue()) {
						sb.append((rangeFacetLpseToken.getHasInfix() ? "-" : "+"));
					}

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
	 * value.  This will take care of prefixes, suffixes, min and max range values, and range prefix/suffixes, and infix
	 * if available</p>
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

			return (PanlLPSEHelper.encodeURIPath(sb.toString()));
		} else {
			// we will have a two part URI path, split by a '~' and both values need
			// to be URLEncoded before.
			if (rangeFacetLpseToken.getValue().equals(rangeMinValue) && null != rangeMinValueReplacement) {
				sb.append(PanlLPSEHelper.encodeURIPath(rangeMinValueReplacement));
			} else {
				if (hasValuePrefix) {
					sb.append(PanlLPSEHelper.encodeURIPath(valuePrefix));
				}
				sb.append(PanlLPSEHelper.encodeURIPath(rangeFacetLpseToken.getValue()));
				if (hasValueSuffix) {
					sb.append(PanlLPSEHelper.encodeURIPath(valueSuffix));
				}

			}

			sb.append(Constants.JSON_VALUE_NO_INFIX_REPLACEMENT);

			if (rangeFacetLpseToken.getToValue().equals(rangeMaxValue) && null != rangeMaxValueReplacement) {
				sb.append(PanlLPSEHelper.encodeURIPath(rangeMaxValueReplacement));
			} else {
				if (hasValuePrefix) {
					sb.append(PanlLPSEHelper.encodeURIPath(valuePrefix));
				}
				sb.append(PanlLPSEHelper.encodeURIPath(rangeFacetLpseToken.getToValue()));
				if (hasValueSuffix) {
					sb.append(PanlLPSEHelper.encodeURIPath(valueSuffix));
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
		StringBuilder lpseUri = new StringBuilder(Constants.FORWARD_SLASH);
		StringBuilder lpseUriAfterMax = new StringBuilder();
		StringBuilder lpseCode = new StringBuilder();

		for(BaseField baseField : collectionProperties.getLpseFields()) {

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

				additionObject.put(Constants.Json.Panl.BEFORE, lpseUri.toString());
				lpseUri.setLength(0);
				lpseCode.append(baseField.getLpseCode());

				lpseUri.append(Constants.FORWARD_SLASH);
			}
		}

		additionObject.put(Constants.Json.Panl.AFTER, lpseUri.toString() + lpseCode.toString() + Constants.FORWARD_SLASH);
		additionObject.put(Constants.Json.Panl.AFTER_MAX_VALUE, lpseUriAfterMax.toString() + lpseCode.toString() + Constants.FORWARD_SLASH);
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
		removeObject.put(Constants.Json.Panl.IS_RANGE_FACET, true);

		RangeFacetLpseToken rangeFacetLpseToken = (RangeFacetLpseToken) lpseToken;
		if (rangeFacetLpseToken.getIsRangeToken()) {
			removeObject.put(Constants.Json.Panl.HAS_INFIX, hasRangeInfix);

			String toValue = rangeFacetLpseToken.getToValue();

			if (null == toValue) {
				removeObject.remove(Constants.Json.Panl.VALUE_TO);
			} else {
				removeObject.put(Constants.Json.Panl.VALUE_TO, toValue);
			}
		}
	}

	public boolean getRangeSuppress() {
		return rangeSuppress;
	}
}
