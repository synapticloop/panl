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
 *  IN THE SOFTWARE.
 */

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.DateRangeFacetLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.bean.PreviousNextValueBean;
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

/**
 * <p>A Panl facet field comes in five flavours</p>
 *
 * <ol>
 *   <li>A regular facet,</li>
 *   <li>A RANGE facet,</li>
 *   <li>An OR facet, or</li>
 *   <li>A BOOLEAN facet, or</li>
 *   <li>A DATE facet</li>
 * </ol>
 */
public class PanlDateRangeFacetField extends PanlFacetField {
	public static final String PREVIOUS_NEXT = "previous_next";
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlDateRangeFacetField.class);

	public static final String JSON_KEY_DAYS = "days";
	public static final String JSON_KEY_DESIGNATOR = "designator";
	public static final String JSON_KEY_DESIGNATORS = "designators";
	public static final String JSON_KEY_HOURS = "hours";
	public static final String JSON_KEY_IS_DATE_FACET = "is_date_facet";
	public static final String JSON_KEY_MONTHS = "months";
	public static final String JSON_KEY_NEXT = "next";
	public static final String JSON_KEY_PREVIOUS = "previous";
	public static final String JSON_KEY_YEARS = "years";

	public static final String PROPERTY_KEY_PREFIX_PANL_DATE = "panl.date.";
	public static final String PROPERTY_KEY_SUFFIX_DAYS = ".days";
	public static final String PROPERTY_KEY_SUFFIX_HOURS = ".hours";
	public static final String PROPERTY_KEY_SUFFIX_MONTHS = ".months";
	public static final String PROPERTY_KEY_SUFFIX_NEXT = ".next";
	public static final String PROPERTY_KEY_SUFFIX_PREVIOUS = ".previous";
	public static final String PROPERTY_KEY_SUFFIX_YEARS = ".years";

	public static final String SOLR_DESIGNATOR_DAYS = "DAYS";
	public static final String SOLR_DESIGNATOR_HOURS = "HOURS";
	public static final String SOLR_DESIGNATOR_MONTHS = "MONTHS";
	public static final String SOLR_DESIGNATOR_YEARS = "YEARS";

	private final Map<String, String> solrRangeDesignatorLookupMap = new HashMap<>();
	private final Map<String, String> solrRangeDesignatorEncodedLookupMap = new HashMap<>();
	private final Map<String, Integer> solrRangeDesignatorLengthLookupMap = new HashMap<>();

	private String nextIndicator;
	private String previousIndicator;

	private boolean hasNext = false;
	private boolean hasPrevious = false;

	public PanlDateRangeFacetField(String lpseCode, String propertyKey, Properties properties, String solrCollection, String panlCollectionUri, int lpseLength) throws PanlServerException {
		super(lpseCode, propertyKey, properties, solrCollection, panlCollectionUri, lpseLength);

		validateProperties();

		populateDateReplacements();
		populateSolrFieldTypeValidation();
		populatePanlAndSolrFieldNames();

		logWarnProperties(this.lpseCode, PROPERTY_KEY_PANL_OR_FACET + this.lpseCode);
		logWarnProperties(this.lpseCode, PROPERTY_KEY_PANL_RANGE_FACET + this.lpseCode);
		logWarnProperties(this.lpseCode, PROPERTY_KEY_PANL_PREFIX + this.lpseCode);
		logWarnProperties(this.lpseCode, PROPERTY_KEY_PANL_SUFFIX + this.lpseCode);
		logDetails();
	}

	/**
	 * <p>Instantiate a <code>DateRangeFacetLpseToken</code> for this field.</p>
	 *
	 * @param collectionProperties The collection properties
	 * @param lpseCode The lpseCode for this field
	 * @param query The query parameter
	 * @param valueTokeniser The value tokeniser
	 * @param lpseTokeniser The lpse tokeniser
	 *
	 * @return The newly instantiated DateRangeFacetLpseToken
	 *
	 * @see DateRangeFacetLpseToken
	 */
	public LpseToken instantiateToken(CollectionProperties collectionProperties, String lpseCode, String query, StringTokenizer valueTokeniser, LpseTokeniser lpseTokeniser) {
		return (new DateRangeFacetLpseToken(collectionProperties, this.lpseCode, lpseTokeniser, valueTokeniser));
	}

	@Override public Logger getLogger() {
		return (LOGGER);
	}

	@Override public List<String> explainAdditional() {
		List<String> explanations = new ArrayList<>();
		explanations.add("A Solr field that can be used as a facet, returned in the field set, or configured to be sorted by.");
		return (explanations);
	}

	private void populateDateReplacements() {
		populateSolrFieldTypeValidation();

		// look for the previous and next keys
		nextIndicator = properties.getProperty(PROPERTY_KEY_PREFIX_PANL_DATE + this.lpseCode + PROPERTY_KEY_SUFFIX_NEXT, null);
		hasNext = nullCheck(nextIndicator);

		previousIndicator = properties.getProperty(PROPERTY_KEY_PREFIX_PANL_DATE + this.lpseCode + PROPERTY_KEY_SUFFIX_PREVIOUS, null);
		hasPrevious = nullCheck(previousIndicator);

		if (null != nextIndicator || null != previousIndicator) {
			String yearsSuffix = properties.getProperty(PROPERTY_KEY_PREFIX_PANL_DATE + this.lpseCode + PROPERTY_KEY_SUFFIX_YEARS, null);
			addToSolrLookupMap(yearsSuffix, SOLR_DESIGNATOR_YEARS);
			String monthsSuffix = properties.getProperty(PROPERTY_KEY_PREFIX_PANL_DATE + this.lpseCode + PROPERTY_KEY_SUFFIX_MONTHS, null);
			addToSolrLookupMap(monthsSuffix, SOLR_DESIGNATOR_MONTHS);
			String daysSuffix = properties.getProperty(PROPERTY_KEY_PREFIX_PANL_DATE + this.lpseCode + PROPERTY_KEY_SUFFIX_DAYS, null);
			addToSolrLookupMap(daysSuffix, SOLR_DESIGNATOR_DAYS);
			String hoursSuffix = properties.getProperty(PROPERTY_KEY_PREFIX_PANL_DATE + this.lpseCode + PROPERTY_KEY_SUFFIX_HOURS, null);
			addToSolrLookupMap(hoursSuffix, SOLR_DESIGNATOR_HOURS);
		}
	}

	private void addToSolrLookupMap(String key, String value) {
		if (null != key) {
			solrRangeDesignatorLookupMap.put(key, value);
			solrRangeDesignatorEncodedLookupMap.put(value, URLEncoder.encode(key, StandardCharsets.UTF_8));
			solrRangeDesignatorLengthLookupMap.put(value, value.length());
		}
	}

	private boolean nullCheck(String propertyValue) {
		return (null != propertyValue);
	}

	private String getSolrRangeDesignator(String originalValue) {
		for (String key : solrRangeDesignatorLookupMap.keySet()) {
			if (originalValue.endsWith(key)) {
				return (solrRangeDesignatorLookupMap.get(key));
			}
		}
		return (null);
	}

	private Integer getSolrRangeDesignatorLength(String solrRangeDesignator) {
		return (solrRangeDesignatorLengthLookupMap.getOrDefault(solrRangeDesignator, 0));
	}

	@Override protected void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokenList) {
		// there can be only one date next/previous
		DateRangeFacetLpseToken lpseToken;
		if (!lpseTokenList.isEmpty()) {
			lpseToken = (DateRangeFacetLpseToken) lpseTokenList.get(0);
			String originalValue = URLDecoder.decode(lpseToken.getOriginalValue(), StandardCharsets.UTF_8);
			if (hasNext && originalValue.startsWith(nextIndicator)) {
				// then we are going to do a range from NOW to x years/months/days
				originalValue = originalValue.substring(nextIndicator.length());
				String solrRangeDesignator = getSolrRangeDesignator(originalValue);
				if (null != solrRangeDesignator) {
					originalValue = originalValue.substring(0, (originalValue.length() - getSolrRangeDesignatorLength(solrRangeDesignator) - 1));
					// now we are ready for the query
					// the original value is the number
					try {
						solrQuery.addFilterQuery(String.format("%s:[NOW TO NOW+%d%s]",
								lpseToken.getSolrField(),
								Integer.parseInt(originalValue),
								solrRangeDesignator));
					} catch (NumberFormatException ignored) {
					}
				}
			} else if (hasPrevious && originalValue.startsWith(previousIndicator)) {
				// then we are going to do a range from x years/months/days to NOW
				originalValue = originalValue.substring(previousIndicator.length());
				String solrRangeDesignator = getSolrRangeDesignator(originalValue);
				if (null != solrRangeDesignator) {
					originalValue = originalValue.substring(0, (originalValue.length() - getSolrRangeDesignatorLength(solrRangeDesignator) - 1));
					// now we are ready for the query
					// the original value is the number
					try {
						solrQuery.addFilterQuery(String.format("%s:[NOW-%d%s TO NOW]",
								lpseToken.getSolrField(),
								Integer.parseInt(originalValue),
								solrRangeDesignator));
					} catch (NumberFormatException ignored) {
					}
				}
			}
		}
	}

	/**
	 * <p>This may either have a next or previous designator, with a designator
	 * suffix.</p>
	 *
	 * @param panlTokenMap The panl token map to look up
	 * @param collectionProperties The collection properties
	 *
	 * @return The URI path for these tokens
	 */
	@Override
	public String getURIPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		if (!panlTokenMap.containsKey(lpseCode) || panlTokenMap.get(lpseCode).isEmpty()) {
			return ("");
		}

		for (LpseToken lpseToken : panlTokenMap.get(lpseCode)) {
			if (!lpseToken.getIsValid()) {
				// not a valid token - keep going
				continue;
			}

			// we are going to return the first one
			if (hasNext || hasPrevious) {
				boolean isValidNextPrevious = getIsValidNextPrevious(lpseToken);
				if (isValidNextPrevious) {
					return (lpseToken.getOriginalValue() + "/");
				}
			}
		}


		if (panlTokenMap.containsKey(lpseCode)) {
			// look for the next/previous

			List<LpseToken> lpseTokens = panlTokenMap.get(lpseCode);

			DateRangeFacetLpseToken lpseToken;
			if (!lpseTokens.isEmpty()) {
				lpseToken = (DateRangeFacetLpseToken) lpseTokens.get(0);
				if (!lpseToken.getIsValid()) {
					return ("");
				}

				String originalValue = URLDecoder.decode(lpseToken.getOriginalValue(), StandardCharsets.UTF_8);
				boolean isDateRangeDesignator = true;
				if (hasNext && !originalValue.startsWith(nextIndicator)) {
					isDateRangeDesignator = false;
				}

				if (hasPrevious && originalValue.startsWith(previousIndicator)) {
					isDateRangeDesignator = false;
				}

				if (isDateRangeDesignator) {
					return (lpseToken.getOriginalValue() + "/");
				}
			}
		}
		return ("");
	}

	private boolean getIsValidNextPrevious(LpseToken lpseToken) {
		if (!hasNext && !hasPrevious) {
			return (false);
		}

		String originalValue = lpseToken.getOriginalValue();
		boolean isValidNextPrevious = true;
		if (hasNext && !originalValue.startsWith(nextIndicator)) {
			isValidNextPrevious = false;
		}

		if (hasPrevious && !originalValue.startsWith(previousIndicator)) {
			isValidNextPrevious = false;
		}

		return (isValidNextPrevious);
	}

	/**
	 * <p>Decode the value, or return null if it cannot be decoded.</p>
	 *
	 * @param value the value to convert if any conversions are required
	 *
	 * @return The decoded value, or null if it couldn't be decoded
	 */
	public PreviousNextValueBean getDecodedRangeValue(String value) {
		String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);

		String previousNext = null;
		boolean decodedNextPrevious = false;
		if (hasNext || hasPrevious) {
			if (hasNext) {
				if (decodedValue.startsWith(nextIndicator)) {
					decodedValue = decodedValue.substring(nextIndicator.length());
					decodedNextPrevious = true;
					previousNext = nextIndicator;
				}
			}

			if (hasPrevious) {
				if (decodedValue.startsWith(previousIndicator)) {
					decodedValue = decodedValue.substring(previousIndicator.length());
					decodedNextPrevious = true;
					previousNext = previousIndicator;
				}
			}
		}

		if (decodedNextPrevious) {
			// now we need to determine which of the fields it is (months, years,
			// hours, days, etc)
			// parse the value
			String solrRangeDesignator = getSolrRangeDesignator(decodedValue);
			if (null != solrRangeDesignator) {
				decodedValue = decodedValue.substring(0, (decodedValue.length() - getSolrRangeDesignatorLength(solrRangeDesignator) - 1));
				// now we are ready for the query
				// the original value is the number
				try {
					String parsedInt = Integer.toString(Integer.parseInt(decodedValue));
					return(new PreviousNextValueBean(previousNext, solrRangeDesignator, parsedInt));
				} catch (NumberFormatException e) {
					return (null);
				}
			}
		}

		return(null);
	}

	@Override public void appendToAvailableObjectInternal(JSONObject jsonObject) {
		jsonObject.put(JSON_KEY_IS_DATE_FACET, true);
	}

	@Override public void addToRemoveObject(JSONObject removeObject, LpseToken lpseToken) {
		removeObject.put(JSON_KEY_IS_DATE_FACET, true);
	}

	/**
	 *
	 * @param facetObject The facet object to append to
	 * @param collectionProperties The colleciton properties
	 * @param panlTokenMap The incoming Panl tokens
	 * @param existingLpseValues The existing LPSE values
	 * @param facetCountValues The facet count values
	 * @param numFound Number of results found
	 * @param numFoundExact Whether the number of results were exact
	 *
	 * @return __ALWAYS__ returns false
	 */
	@Override
	public boolean appendAvailableValues(JSONObject facetObject, CollectionProperties collectionProperties, Map<String, List<LpseToken>> panlTokenMap, Set<String> existingLpseValues, List<FacetField.Count> facetCountValues, long numFound, boolean numFoundExact) {
		return(false);
	}

	@Override public boolean appendAvailableDateRangeValues(
			JSONObject additionObject,
			CollectionProperties collectionProperties,
			Map<String, List<LpseToken>> panlTokenMap) {

		additionObject.put(JSON_KEY_FACET_NAME, this.solrFieldName);
		additionObject.put(JSON_KEY_NAME, this.panlFieldName);
		additionObject.put(JSON_KEY_PANL_CODE, this.lpseCode);

		additionObject.put(JSON_KEY_NEXT, URLEncoder.encode(nextIndicator, StandardCharsets.UTF_8));
		additionObject.put(JSON_KEY_PREVIOUS, URLEncoder.encode(previousIndicator, StandardCharsets.UTF_8));

		JSONObject designatorObject = new JSONObject();
		designatorObject.put(JSON_KEY_HOURS, solrRangeDesignatorEncodedLookupMap.get(SOLR_DESIGNATOR_HOURS));
		designatorObject.put(JSON_KEY_DAYS, solrRangeDesignatorEncodedLookupMap.get(SOLR_DESIGNATOR_DAYS));
		designatorObject.put(JSON_KEY_MONTHS, solrRangeDesignatorEncodedLookupMap.get(SOLR_DESIGNATOR_MONTHS));
		designatorObject.put(JSON_KEY_YEARS, solrRangeDesignatorEncodedLookupMap.get(SOLR_DESIGNATOR_YEARS));

		additionObject.put(JSON_KEY_DESIGNATORS, designatorObject);

		boolean shouldBreak = false;
		for (LpseToken lpseToken : panlTokenMap.getOrDefault(this.lpseCode, new ArrayList<>())) {
			if(lpseToken.getIsValid()) {
				DateRangeFacetLpseToken dateRangeFacetLpseToken = (DateRangeFacetLpseToken)lpseToken;
				additionObject.put(JSON_KEY_VALUE, dateRangeFacetLpseToken.getValue());
				additionObject.put(PREVIOUS_NEXT, dateRangeFacetLpseToken.getPreviousNext());
				additionObject.put(JSON_KEY_DESIGNATOR, URLEncoder.encode(dateRangeFacetLpseToken.getDesignator(), StandardCharsets.UTF_8));
				shouldBreak = true;
			}
			// only one date range is allowed - the first valid one
			if(shouldBreak) {
				break;
			}
		}
		return(true);
	}
}
