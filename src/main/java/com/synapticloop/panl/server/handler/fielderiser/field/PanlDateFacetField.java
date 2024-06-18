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
import com.synapticloop.panl.server.handler.tokeniser.token.FacetLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.SolrQuery;
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
 *   <li>A BOOLEAN facet</li>
 *   <li>A DATE facet</li>
 * </ol>
 */
public class PanlDateFacetField extends PanlFacetField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlDateFacetField.class);
	public static final String PROPERTY_KEY_PREFIX_PANL_DATE = "panl.date.";
	public static final String SOLR_DESIGNATOR_YEARS = "YEARS";
	public static final String SOLR_DESIGNATOR_MONTHS = "MONTHS";
	public static final String SOLR_DESIGNATOR_DAYS = "DAYS";
	public static final String SOLR_DESIGNATOR_HOURS = "HOURS";

	private Map<String, String> solrRangeDesignatorLookupMap = new HashMap<>();
	private Map<String, Integer> solrRangeDesignatorLengthLookupMap = new HashMap<>();

	private String nextIndicator;
	private String previousIndicator;
	private String dateYears;
	private String dateMonths;
	private String dateDays;
	private String dateHours;

	private boolean hasNext = false;
	private boolean hasPrevious = false;

	public PanlDateFacetField(String lpseCode, String propertyKey, Properties properties, String solrCollection, int lpseLength) throws PanlServerException {
		super(lpseCode, propertyKey, properties, solrCollection, lpseLength);

		validateProperties();

		populateSuffixAndPrefix();
		populateDateReplacements();
		populateSolrFieldTypeValidation();
		populatePanlAndSolrFieldNames();

		logDetails();
	}

	@Override
	public Logger getLogger() {
		return (LOGGER);
	}

	@Override public List<String> explainAdditional() {
		List<String> explanations = new ArrayList<>();
		explanations.add("A Solr field that can be used as a facet, returned in the field set, or configured to be sorted by.");
		return(explanations);
	}

	@Override public String getLpseCode(LpseToken token, CollectionProperties collectionProperties) {
		return (token.getLpseCode());
	}

	protected void populateDateReplacements() {
		populateSolrFieldTypeValidation();

		// look for the previous and next keys
		nextIndicator = properties.getProperty(PROPERTY_KEY_PREFIX_PANL_DATE + this.lpseCode + ".next", null);
		hasNext = nullCheck(nextIndicator);

		previousIndicator = properties.getProperty(PROPERTY_KEY_PREFIX_PANL_DATE + this.lpseCode + ".previous", null);
		hasPrevious = nullCheck(previousIndicator);

		if(null != nextIndicator || null != previousIndicator) {
			dateYears = properties.getProperty(PROPERTY_KEY_PREFIX_PANL_DATE + this.lpseCode + ".years", null);
			addToSolrLookupMap(dateYears, SOLR_DESIGNATOR_YEARS);
			dateMonths = properties.getProperty(PROPERTY_KEY_PREFIX_PANL_DATE + this.lpseCode + ".months", null);
			addToSolrLookupMap(dateMonths, SOLR_DESIGNATOR_MONTHS);
			dateDays = properties.getProperty(PROPERTY_KEY_PREFIX_PANL_DATE + this.lpseCode + ".days", null);
			addToSolrLookupMap(dateDays, SOLR_DESIGNATOR_DAYS);
			dateHours = properties.getProperty(PROPERTY_KEY_PREFIX_PANL_DATE + this.lpseCode + ".hours", null);
			addToSolrLookupMap(dateHours, SOLR_DESIGNATOR_HOURS);
		}
	}

	private void addToSolrLookupMap(String key, String value) {
		if(null != key) {
			solrRangeDesignatorLookupMap.put(key, value);
			solrRangeDesignatorLengthLookupMap.put(value, value.length());
		}
	}
	private boolean nullCheck(String propertyValue) {
		return(null != propertyValue);
	}

	private String getSolrRangeDesignator(String originalValue) {
		for(String key : solrRangeDesignatorLookupMap.keySet()) {
			if(originalValue.endsWith(key)) {
				return(solrRangeDesignatorLookupMap.get(key));
			}
		}
		return(null);
	}

	private Integer getSolrRangeDesignatorLength(String solrRangeDesignator) {
		return(solrRangeDesignatorLengthLookupMap.getOrDefault(solrRangeDesignator, 0));
	}

	@Override protected void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokenList) {
		// there can be only one date next/previous
		FacetLpseToken lpseToken;
		if(!lpseTokenList.isEmpty()) {
			lpseToken = (FacetLpseToken)lpseTokenList.get(0);
			String originalValue = URLDecoder.decode(lpseToken.getOriginalValue(), StandardCharsets.UTF_8);
			if(hasNext && originalValue.startsWith(nextIndicator)) {
				boolean shouldQuery = true;
				// then we are going to do a range from NOW to x years/months/days
				originalValue = originalValue.substring(nextIndicator.length());
				String solrRangeDesignator = getSolrRangeDesignator(originalValue);
				if(null != solrRangeDesignator) {
					originalValue = originalValue.substring(0, (originalValue.length() - getSolrRangeDesignatorLength(solrRangeDesignator) - 1));
					// now we are ready for the query
					// the original value is the number
					int value = 0;
					try {
						value = Integer.parseInt(originalValue);
					} catch(NumberFormatException e) {
						shouldQuery = false;
					}

					if(shouldQuery) {
						solrQuery.addFilterQuery(String.format("%s:[NOW TO NOW-%d%s]",
								lpseToken.getSolrField(),
								value,
								solrRangeDesignator));
					}
				}
			} else if (hasPrevious && originalValue.startsWith(previousIndicator)) {
				boolean shouldQuery = true;
				// then we are going to do a range from x years/months/days to NOW
				originalValue = originalValue.substring(previousIndicator.length());
				String solrRangeDesignator = getSolrRangeDesignator(originalValue);
				if(null != solrRangeDesignator) {
					originalValue = originalValue.substring(0, (originalValue.length() - getSolrRangeDesignatorLength(solrRangeDesignator) - 1));
					// now we are ready for the query
					// the original value is the number
					int value = 0;
					try {
						value = Integer.parseInt(originalValue);
					} catch(NumberFormatException e) {
						shouldQuery = false;
					}

					if(shouldQuery) {
						solrQuery.addFilterQuery(String.format("%s:[NOW-%d%s TO NOW]",
								lpseToken.getSolrField(),
								value,
								solrRangeDesignator));
					}
				}
			} else {
				// TODO - need to determine OR FACET
				// just a facet selection
				solrQuery.addFilterQuery(String.format("%s:%s",
						lpseToken.getSolrField(),
						lpseToken.getValue()));
			}
		}
	}

	@Override public String getURIPath(Map<String, List<LpseToken>> panlTokenMap, CollectionProperties collectionProperties) {
		StringBuilder sb = new StringBuilder();
		if (panlTokenMap.containsKey(lpseCode)) {

			List<LpseToken> lpseTokens = panlTokenMap.get(lpseCode);

			FacetLpseToken lpseToken;
			if(!lpseTokens.isEmpty()) {
				lpseToken = (FacetLpseToken)lpseTokens.get(0);
				if(!lpseToken.getIsValid()) {
					return("");
				}

				String originalValue = URLDecoder.decode(lpseToken.getOriginalValue(), StandardCharsets.UTF_8);
				boolean isDateRangeDesignator = true;
				if(hasNext && !originalValue.startsWith(nextIndicator)) {
					isDateRangeDesignator = false;
				}

				if(hasPrevious && originalValue.startsWith(previousIndicator)) {
					isDateRangeDesignator = false;
				}

				if(isDateRangeDesignator) {
					return(lpseToken.getOriginalValue() + "/");
				}
			}

//			for (LpseToken lpseToken : lpseTokens) {
//				if (lpseToken.getIsValid()) {
//					sb.append(getEncodedPanlValue(lpseToken));
//					sb.append("/");
//				}
//			}
		}
		return (sb.toString());
	}
}
