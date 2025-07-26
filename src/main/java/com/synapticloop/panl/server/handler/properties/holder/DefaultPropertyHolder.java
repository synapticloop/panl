package com.synapticloop.panl.server.handler.properties.holder;

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
import com.synapticloop.panl.server.handler.fielderiser.field.param.*;
import com.synapticloop.panl.server.handler.helper.PropertyHelper;
import com.synapticloop.panl.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DefaultPropertyHolder {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPropertyHolder.class);

	private final String solrCollection;
	private final String panlCollectionUri;
	private final Properties properties;

	private final Set<String> LPSE_METADATA = new HashSet<>();
	private final Map<String, BaseField> lpseFieldLookup = new HashMap<>();
	/**
	 * <p>Used as a holder for panl LPSE codes which are mandatory to have in the
	 * panl.lpse.order property.  Mandatory properties are added, then removed when the property is set.</p>
	 */
	private final Map<String, String> MANDATORY_LPSE_ORDER_FIELDS = new HashMap<>();


	/**
	 * <p>Whether to include single facets in  the results</p>
	 */
	protected boolean panlIncludeSingleFacets;

	/**
	 * <p>Whether to include the same number of facets in the results as the number
	 * of documents that are returned.</p>
	 */
	protected boolean panlIncludeSameNumberFacets;
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
	 * <p>Whether to highlight the results</p>
	 */
	private boolean highlight;

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
	public DefaultPropertyHolder(
			String solrCollection,
			String panlCollectionUri,
			Properties properties) throws PanlServerException {

		this.solrCollection = solrCollection;
		this.panlCollectionUri = panlCollectionUri;

		this.panlIncludeSingleFacets = properties
				.getProperty(Constants.Property.Panl.PANL_INCLUDE_SINGLE_FACETS, Constants.BOOLEAN_FALSE_VALUE)
				.equals(Constants.BOOLEAN_TRUE_VALUE);

		this.panlIncludeSameNumberFacets = properties
				.getProperty(Constants.Property.Panl.PANL_INCLUDE_SAME_NUMBER_FACETS, Constants.BOOLEAN_FALSE_VALUE)
				.equals(Constants.BOOLEAN_TRUE_VALUE);

		this.formQueryRespondTo = properties
				.getProperty(
						Constants.Property.Panl.PANL_FORM_QUERY_RESPONDTO,
						Constants.DEFAULT_VALUE_QUERY_RESPOND_TO);

		this.facetMinCount =
				PropertyHelper.getIntProperty(
						LOGGER,
						properties,
						Constants.Property.Solr.SOLR_FACET_MIN_COUNT,
						Constants.DEFAULT_VALUE_FACET_MIN_COUNT);

		this.highlight = properties
				.getProperty(Constants.Property.Solr.SOLR_HIGHLIGHT, Constants.BOOLEAN_FALSE_VALUE)
				.equals(Constants.BOOLEAN_TRUE_VALUE);

		this.numResultsPerPage =
				PropertyHelper.getIntProperty(
						LOGGER,
						properties,
						Constants.Property.Solr.SOLR_NUMROWS_DEFAULT,
						Constants.DEFAULT_VALUE_NUM_RESULTS_PER_PAGE);

		// we are setting the maximum number of results to default to the same
		// number for the results per page if it is not set
		this.maxNumResultsPerPage =
				PropertyHelper.getIntProperty(
						LOGGER,
						properties,
						Constants.Property.Solr.SOLR_NUMROWS_MAXIMUM,
						this.numResultsPerPage);

		this.numResultsLookahead =
				PropertyHelper.getIntProperty(
						LOGGER,
						properties,
						Constants.Property.Solr.SOLR_NUMROWS_LOOKAHEAD,
						Constants.DEFAULT_VALUE_NUM_RESULTS_LOOKAHEAD);

		this.solrFacetLimit =
				PropertyHelper.getIntProperty(
						LOGGER,
						properties,
						Constants.Property.Solr.SOLR_FACET_LIMIT,
						Constants.DEFAULT_VALUE_SOLR_FACET_LIMIT);

		this.lpseLength =
				PropertyHelper.getIntProperty(
						LOGGER,
						properties,
						Constants.Property.Panl.PANL_LPSE_LENGTH,
						null);
		this.properties = properties;
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

}
