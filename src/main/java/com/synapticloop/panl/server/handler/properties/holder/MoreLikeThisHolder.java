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
import com.synapticloop.panl.server.handler.helper.PropertyHelper;
import com.synapticloop.panl.util.Constants;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.MoreLikeThisParams;
import org.apache.solr.common.params.ShardParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>This holds all 'More Like This' properties and can be used to apply the
 * parameters to the query.</p>
 *
 * <p>To enable the MLT in Panl to use the Solr <strong>MoreLikeThis Query Parser</strong>
 * you will need to configure the following properties which will enable More Like This queries.</p>
 *
 * <ul>
 *   <li><code>panl.mlt.enable=true</code></li>
 *   <li><code>panl.mlt.handler=/select</code></li>
 *   <li><code>panl.mlt.type=select</code></li>
 * </ul>
 *
 * <p>To enable the MLT in Panl to use the <strong>MoreLikeThis Request Handler</strong>
 * (assuming that it is also configured in the Solr server), you will need to
 * configure the following properties.
 * </p>
 *
 * <ul>
 *   <li><code>panl.mlt.enable=true</code></li>
 *   <li><code>panl.mlt.handler=/mlt</code></li>
 *   <li><code>panl.mlt.type=mlt</code></li>
 *   <li><code>panl.mlt.numretries=6</code></li>
 * </ul>
 *
 * <p>The following table summarises what properties are available for each of
 * the handlers (either the 'select' or the 'mlt' handler).</p>
 *
 * <table>
 *   <thead>
 *     <tr><th>Property:</th><th>/select</th><th>/mlt</th></tr>
 *  	</thead>
 * 		<tbody>
 * 			<tr><td><code>mlt.fl</code></td><td>NO (use <code>mlt.qf</code>)</td><td>YES</td></tr>
 * 			<tr><td colspan="3">If you are using the '/select' handler, then you should set
 * 		  the fields with the <code>mlt.qf</code></td></tr>
 * 			<tr><td><code>mlt.mintf</code></td><td>YES</td><td>YES</td></tr>
 * 			<tr><td><code>mlt.mindf</code></td><td>YES</td><td>YES</td></tr>
 * 			<tr><td><code>mlt.maxdf</code></td><td>YES</td><td>YES</td></tr>
 * 			<tr><td><code>mlt.maxdfpct</code></td><td>NO</td><td>YES</td></tr>
 * 			<tr><td><code>mlt.minwl</code></td><td>YES</td><td>YES</td></tr>
 * 			<tr><td><code>mlt.maxwl</code></td><td>YES</td><td>YES</td></tr>
 * 			<tr><td><code>mlt.maxqt</code></td><td>YES</td><td>YES</td></tr>
 * 			<tr><td><code>mlt.maxntp</code></td><td>YES</td><td>YES</td></tr>
 * 			<tr><td><code>mlt.boost</code></td><td>YES</td><td>YES</td></tr>
 * 			<tr><td><code>mlt.qf</code></td><td>YES</td><td>NO (use <code>mlt.fl</code>)</td></tr>
 * 			<tr><td colspan="3">If you are using the '/mlt' handler, then you should set
 * 		  the fields with the <code>mlt.fl</code>.  Whilst this is supported for '/mlt'
 * 		  in Solr, it is unsupported in the Panl server.</td></tr>
 * 			<tr><td><code>mlt.interestingTerms</code></td><td>NO</td><td>YES</td></tr>
 * 			<tr><td><code>mlt.match.include</code></td><td>NO</td><td>YES</td></tr>
 * 			<tr><td><code>mlt.match.offset</code></td><td>NO</td><td>YES</td></tr>
 *   </tbody>
 * </table>
 *
 * @author Synapticloop
 * @see <a href="https://solr.apache.org/guide/solr/latest/query-guide/morelikethis.html">Apache Solr More Like This</a>
 */

public class MoreLikeThisHolder {
	private static final Logger LOGGER = LoggerFactory.getLogger(MoreLikeThisHolder.class);

	public static final String SHARD_1 = "shard1";
	public static final String REPLICA_LEADER_TRUE = ShardParams.SHARDS_PREFERENCE_REPLICA_LEADER + ":true";
	public static final String REPLICA_LOCATION_LOCAL = "replica.location:local";

	public static final String REQUEST_PROPERTY_MINTF = "mintf";
	public static final String REQUEST_PROPERTY_MINDF = "mindf";
	public static final String REQUEST_PROPERTY_MAXDF = "maxdf";
	public static final String REQUEST_PROPERTY_MINWL = "minwl";
	public static final String REQUEST_PROPERTY_MAXWL = "maxwl";
	public static final String REQUEST_PROPERTY_MAXQT = "maxqt";
	public static final String REQUEST_PROPERTY_MAXNTP = "maxntp";
	public static final String REQUEST_PROPERTY_BOOST = "boost";

	/**
	 * <p>The number of results returned for the more like this query</p>
	 */
	private int numResultsMoreLikeThis;

	/**
	 * <p>Whether the More Like This functionality is enabled for this collection</p>
	 */
	private boolean mltEnabled = false;

	/**
	 * <p>The Solr More Like This handler URL.</p>
	 */
	private String mltHandler = Constants.DEFAULT_MLT_HANDLER;

	/**
	 * <p>The type of the MLT that we are using - it must be either
	 * 'select', or 'mlt' (without the quotes).</p>
	 */
	private String mltType = Constants.DEFAULT_MLT_TYPE_SELECT;

	// (Required - Default: false) Specifies the fields to use for similarity. A
	// list of fields can be provided separated by commas. If possible, the fields
	// should have stored termVectors.
	private String mltFl;  // maps to mlt.fl

	// This is the parsed values of the mltFl member
	private String[] mltFieldArray;

	// this is a quick lookup for the field list which is then validated against
	// the query fields
	private Set<String> mltFieldSet = new HashSet<>();

	// (Optional - Default: 2) Specifies the minimum frequency below which terms
	// will be ignored in the source document.
	private Integer mltMinTermFrequency = null;

	// (Optional - Default: 5) Specifies the maximum frequency above which terms
	// will be ignored which occur in more than this many documents.
	private Integer mltMinDocFrequency = null;

	// (Optional - Default: none) Specifies the maximum frequency above which
	// terms will be ignored which occur in more than this many documents.
	private Integer mltMaxDocFrequency = null;

	// (Optional - Default: none) Specifies the maximum document frequency using a
	// ratio relative to the number of documents in the index. The value provided
	// must be an integer between 0 and 100. For example, mlt.maxdfpct=75 means
	// the word will be ignored if it occurs in more than 75 percent of the
	// documents in the index.
	private Integer mltMaxDocFreqPercentage = null;

	// (Optional - Default: none) Sets the minimum word length below which words
	// will be ignored.
	private Integer mltMinWordLength = null;

	// (Optional - Default: none) Sets the maximum word length above which words
	// will be ignored.
	private Integer mltMaxWordLength = null;

	// (Optional - Default: 25) Sets the maximum number of query terms that will
	// be included in any generated query.
	private Integer mltMaxQueryTerms = null;

	// (Optional - Default: 5000) Sets the maximum number of tokens to parse in
	// each example document field that is not stored with TermVector support.
	private Integer mltMaxNumTokensParse = null;

	// (Optional - Default: false) Specifies if the query will be boosted by the
	// interesting term relevance. Possible values are true or false.
	private Boolean mltBoost = null;

	// (Optional - Default: false) Query fields and their boosts using the same
	// format used by the DisMax Query Parser. These fields must also be specified
	// in mlt.fl.
	private String mltQueryFields = null;

	// the query field array for adding to the solr query
	private String[] mltQueryFieldArray = null;

	// (Optional - Default: none) Adds a section in the response that shows the
	// top terms (based on TF/IDF) used for the MoreLikeThis query. It supports
	// three possible values:
	//
	//  - list lists the terms.
	//  - none lists no terms (the default).
	//  - details lists the terms along with the boost value used for each term.
	//    Unless mlt.boost=true, all terms will have boost=1.0.
	private String mltInterestingTerms = null;

	// (Optional - Default: true) Specifies if the response should include the
	// matched document. If set to false, the response will look like a normal
	// select response.
	private Boolean mltMatchInclude = null;

	// (Optional - Default: none) Specifies an offset into the main query search
	// results to locate the document on which the MoreLikeThis query should
	// operate. By default, the query operates on the first result for the q
	// parameter.
	private Integer mltMatchOffset = null;

	/**
	 * <p>The Solr Field Name that is the unique key</p>
	 */
	private String uniqueKeySolrFieldName = null;

	/**
	 * <p>The string builder for the cache-able part of the select query</p>
	 */
	private StringBuilder selectQueryStringBuilder = new StringBuilder();

	/**
	 * <p>The number of retries to the Solr server.</p>
	 */
	private int numMltRetries = Constants.DEFAULT_MLT_NUM_RETRIES;

	/**
	 * <p>Instantiate an MLT Holder and parse and process the properties to set
	 * them.  This will only parse the property file if the property
	 * <code>panl.mlt.enable</code> is set to <code>true</code> (case-sensitive)</p>
	 *
	 * @param properties The properties to use as a lookup
	 * @param solrFieldHolder The field holder to interrogate to ensure that the field is valid when using MLT fields
	 * 		and MLT query fields
	 *
	 * @throws PanlServerException If there was an error with one of the properties
	 */
	public MoreLikeThisHolder(
			Properties properties,
			SolrFieldHolder solrFieldHolder) throws PanlServerException {


		this.mltEnabled = properties.getProperty(
				Constants.Property.Panl.PANL_MLT_ENABLE,
				Constants.BOOLEAN_FALSE_VALUE).equals(Constants.BOOLEAN_TRUE_VALUE);

		if (!this.mltEnabled) {
			return;
		}

		this.mltHandler = properties.getProperty(
				Constants.Property.Panl.PANL_MLT_HANDLER,
				Constants.DEFAULT_MLT_HANDLER);

		this.mltType = properties.getProperty(
				Constants.Property.Panl.PANL_MLT_TYPE,
				Constants.DEFAULT_MLT_TYPE_SELECT);

		if (!(this.mltType.equals(Constants.DEFAULT_MLT_TYPE_SELECT) ||
				this.mltType.equals(Constants.DEFAULT_MLT_TYPE_MLT))) {
			throw new PanlServerException("[ Solr/Panl '" +
					solrFieldHolder.getSolrCollection() +
					"/" +
					solrFieldHolder.getPanlCollectionUri() +
					"' ] The property '" +
					Constants.Property.Panl.PANL_MLT_TYPE +
					"' MUST be one of '" +
					Constants.DEFAULT_MLT_TYPE_SELECT + "' or '" +
					Constants.DEFAULT_MLT_TYPE_MLT + "'.");
		}

		// at this point MLT is enabled so we should load the additional properties

		this.numResultsMoreLikeThis =
				PropertyHelper.getIntProperty(
						LOGGER,
						properties,
						Constants.Property.Solr.SOLR_NUMROWS_MORELIKETHIS,
						Constants.DEFAULT_VALUE_NUM_RESULTS_MORELIKETHIS);

		if (this.numResultsMoreLikeThis < 1) {
			LOGGER.warn("[ Solr/Panl '{}/{}' ] The property '{}' was set to '{}', this" +
							" must be an integer greater than 0, setting the value to '1'.",
					solrFieldHolder.getSolrCollection(),
					solrFieldHolder.getPanlCollectionUri(),
					Constants.Property.Solr.SOLR_NUMROWS_MORELIKETHIS,
					this.numResultsMoreLikeThis);

			this.numResultsMoreLikeThis = 1;
		}

		this.numMltRetries =
				PropertyHelper.getIntProperty(
						LOGGER,
						properties,
						Constants.Property.Panl.PANL_MLT_NUM_RETRIES,
						Constants.DEFAULT_MLT_NUM_RETRIES);

		if (this.numMltRetries < 1) {
			LOGGER.warn("[ Solr/Panl '{}/{}' ] The property '{}' was set to '{}', this" +
							" must be an integer greater than 0, setting the value to '1'.",
					solrFieldHolder.getSolrCollection(),
					solrFieldHolder.getPanlCollectionUri(),
					Constants.Property.Panl.PANL_MLT_NUM_RETRIES,
					this.numMltRetries);

			this.numMltRetries = 1;
		}

		SolrPanlField uniqueKeySolrPanlField = solrFieldHolder.getUniqueKeySolrField();
		if (null == uniqueKeySolrPanlField) {
			throw new PanlServerException("[ Solr/Panl '" +
					solrFieldHolder.getSolrCollection() +
					"/" +
					solrFieldHolder.getPanlCollectionUri() +
					"' ] Solr More Like This query handler requires a query of the " +
					"Solr uniqueKey field, which has not been defined in the " +
					"<panl_collection_url>.panl.properties file.  You must add ONE and " +
					"ONLY one panl.uniquekey.<lpse_code>=true property to the file.");
		} else {
			this.uniqueKeySolrFieldName = uniqueKeySolrPanlField.getSolrFieldName();
		}

		// IMPORTANT - we need to know the mltType to determine which of the
		// properties we will use for the field query - whilst in some instances it
		// is valid to use both for the Solr query, The Panl server is going to
		// ignore it.
		int i = 0;

		switch (this.mltType) {
			case Constants.DEFAULT_MLT_TYPE_SELECT:
				this.mltQueryFields = PropertyHelper.getProperty(
						properties,
						Constants.Property.Panl.PANL_MLT_QF,
						"");

				this.mltQueryFieldArray = this.mltQueryFields.split(",");

				for (String solrFieldName : this.mltQueryFieldArray) {
					String trimmed = solrFieldName.trim();
					if (trimmed.isEmpty()) {
						continue;
					}

					if (!solrFieldHolder.getIsFieldOrFacet(trimmed)) {
						throw new PanlServerException("[ Solr/Panl '" +
								solrFieldHolder.getSolrCollection() +
								"/" +
								solrFieldHolder.getPanlCollectionUri() +
								"' ] Attempting to define property '" +
								Constants.Property.Panl.PANL_MLT_QF +
								"' with a field named '" +
								trimmed +
								"' which __MUST__ be defined in the property '" +
								Constants.Property.Panl.PANL_MLT_FL +
								"'.");
					}

					this.mltQueryFieldArray[i] = trimmed;
					i++;
				}
				break;
			case Constants.DEFAULT_MLT_TYPE_MLT:
				this.mltFl = PropertyHelper.getProperty(
						properties,
						Constants.Property.Panl.PANL_MLT_FL,
						null);

				// if the above is null - error
				if (null == mltFl) {
					throw new PanlServerException("The property '" +
							Constants.Property.Panl.PANL_MLT_FL +
							"' MUST be set to enable the Panl MLT.");
				}

				// now go through each of the fields and ensure that is it a valid field
				this.mltFieldArray = this.mltFl.split(",");

				for (String solrFieldName : this.mltFieldArray) {
					String trimmed = solrFieldName.trim();
					if (trimmed.isEmpty()) {
						continue;
					}
					if (!solrFieldHolder.getIsFieldOrFacet(trimmed)) {
						throw new PanlServerException("[ Solr/Panl '" +
								solrFieldHolder.getSolrCollection() +
								"/" +
								solrFieldHolder.getPanlCollectionUri() +
								"' ] Attempting to define property '" +
								Constants.Property.Panl.PANL_MLT_FL +
								"' with a field value of '" +
								trimmed +
								"' which is not defined in the <panl_collection_url>.panl.properties file.");
					}

					this.mltFieldSet.add(trimmed);
					this.mltFieldArray[i] = trimmed;
					i++;
				}

				if(this.mltFieldSet.isEmpty()) {
					throw new PanlServerException("[ Solr/Panl '" +
							solrFieldHolder.getSolrCollection() +
							"/" +
							solrFieldHolder.getPanlCollectionUri() +
							"' ] Attempting to define property '" +
							Constants.Property.Panl.PANL_MLT_FL +
							"' with no valid field values");
				}
				break;
		}


		this.mltMinTermFrequency = PropertyHelper.getIntProperty(
				properties,
				Constants.Property.Panl.PANL_MLT_MINTF,
				null);

		this.mltMinDocFrequency = PropertyHelper.getIntProperty(
				properties,
				Constants.Property.Panl.PANL_MLT_MINDF,
				null);

		this.mltMaxDocFrequency = PropertyHelper.getIntProperty(
				properties,
				Constants.Property.Panl.PANL_MLT_MAXDF,
				null);

		this.mltMaxDocFreqPercentage = PropertyHelper.getIntProperty(
				properties,
				Constants.Property.Panl.PANL_MLT_MAXDFPCT,
				null);

		if (null != this.mltMaxDocFreqPercentage) {
			if (this.mltMaxDocFreqPercentage < 0 || this.mltMaxDocFreqPercentage > 100) {
				throw new PanlServerException("The property '" +
						Constants.Property.Panl.PANL_MLT_MAXDFPCT +
						"' MUST be between 0 and 100 (inclusive)");
			}
		}

		this.mltMinWordLength = PropertyHelper.getIntProperty(
				properties,
				Constants.Property.Panl.PANL_MLT_MINWL,
				null);

		this.mltMaxWordLength = PropertyHelper.getIntProperty(
				properties,
				Constants.Property.Panl.PANL_MLT_MAXWL,
				null);

		this.mltMaxQueryTerms = PropertyHelper.getIntProperty(
				properties,
				Constants.Property.Panl.PANL_MLT_MAXQT,
				null);

		this.mltMaxNumTokensParse = PropertyHelper.getIntProperty(
				properties,
				Constants.Property.Panl.PANL_MLT_MAXNTP,
				null);

		// we do not test the boolean value - as if it is null, we will leave it
		// as such so that it doesn't get added to the Solr query
		String mltBoostTemp = PropertyHelper.getProperty(
				properties,
				Constants.Property.Panl.PANL_MLT_BOOST,
				null);

		if (null != mltBoostTemp) {
			this.mltBoost = Boolean.parseBoolean(mltBoostTemp);
		}

		this.mltQueryFields = PropertyHelper.getProperty(
				properties,
				Constants.Property.Panl.PANL_MLT_QF,
				null);

		this.mltInterestingTerms = PropertyHelper.getProperty(
				properties,
				Constants.Property.Panl.PANL_MLT_INTERESTINGTERMS,
				null);

		if (null != this.mltInterestingTerms) {
			switch (this.mltInterestingTerms) {
				case "list":
				case "details":
				case "none":
					break;
				case "":
					this.mltInterestingTerms = null;
					break;
				default:
					throw new PanlServerException("The property '" +
							Constants.Property.Panl.PANL_MLT_INTERESTINGTERMS +
							"' __MUST__ be one of 'none', 'list', or 'details' " +
							"(case-sensitive and without the single quotes).  " +
							"Or you can leave it blank/empty string to default to 'none'");
			}
		}

		String mltMatchInclude = PropertyHelper.getProperty(
				properties,
				Constants.Property.Panl.PANL_MLT_MATCH_INCLUDE,
				null);

		if (null != mltMatchInclude) {
			this.mltMatchInclude = Boolean.parseBoolean(mltMatchInclude);
		}

		this.mltMatchOffset = PropertyHelper.getIntProperty(
				properties,
				Constants.Property.Panl.PANL_MLT_MATCH_OFFSET,
				null);

		// now we are going to add to the stringBuilder for caching
		// purposes (if the type of the MLT is select
		if (this.mltType.equals(Constants.DEFAULT_MLT_TYPE_SELECT)) {
			this.selectQueryStringBuilder = cacheSelectStringQuery();
		}
	}

	/**
	 * <p>Apply the More Like This (MLT) parameters to the passed in Solr query.
	 * This should only be done if this is an MLT enabled for this collection, otherwise an exception will be thrown.</p>
	 *
	 * @param solrQuery The Solr query to apply the MLT parameters to
	 * @param returnFieldSet The list of field to return for the FieldSet
	 * @param uniqueKeyValue The value for the unique key to base the More Like This search on.
	 *
	 * @throws PanlServerException if the Collection has not enabled the MLT processor.
	 */
	public void applyMltToQuery(SolrQuery solrQuery, List<String> returnFieldSet,
			String uniqueKeyValue) throws PanlServerException {
		if (!this.mltEnabled) {
			throw new PanlServerException("Attempting to retrieve an MLT query on a collection that hasn't enabled it.");
		}

		switch (this.mltType) {
			case Constants.DEFAULT_MLT_TYPE_SELECT:
				applyMltSelectQuery(solrQuery, returnFieldSet, uniqueKeyValue);
				break;
			case Constants.DEFAULT_MLT_TYPE_MLT:
				applyMltHandlerQuery(solrQuery, returnFieldSet, uniqueKeyValue);
				break;
		}
	}

	/**
	 * <p>cache the /select query string - which is then re-used to build the Solr
	 * query string.</p>
	 *
	 * @return the cache-able part of the /select query string
	 */
	private StringBuilder cacheSelectStringQuery() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder
				.append("{!mlt qf=")
				.append(String.join(",", this.mltQueryFieldArray));

		appendSelectParameterMltQuery(stringBuilder, REQUEST_PROPERTY_MINTF, this.mltMinTermFrequency);
		appendSelectParameterMltQuery(stringBuilder, REQUEST_PROPERTY_MINDF, this.mltMinDocFrequency);
		appendSelectParameterMltQuery(stringBuilder, REQUEST_PROPERTY_MAXDF, this.mltMaxDocFrequency);
		appendSelectParameterMltQuery(stringBuilder, REQUEST_PROPERTY_MINWL, this.mltMinWordLength);
		appendSelectParameterMltQuery(stringBuilder, REQUEST_PROPERTY_MAXWL, this.mltMaxWordLength);
		appendSelectParameterMltQuery(stringBuilder, REQUEST_PROPERTY_MAXQT, this.mltMaxQueryTerms);
		appendSelectParameterMltQuery(stringBuilder, REQUEST_PROPERTY_MAXNTP, this.mltMaxNumTokensParse);
		appendSelectParameterMltQuery(stringBuilder, REQUEST_PROPERTY_BOOST, this.mltBoost);

		stringBuilder.append("}");

		return (stringBuilder);
	}

	/**
	 * <p>Helper method to append non-null queries to the query string.  This will
	 * only append the <code> key=value</code> to the string buffer if the value part is not null.</p>
	 *
	 * @param stringBuilder The string builder to append the key value pair to
	 * @param key The key to use
	 * @param value The value to use
	 */
	private void appendSelectParameterMltQuery(StringBuilder stringBuilder, String key, Object value) {
		if (null != value) {
			stringBuilder
					.append(" ")
					.append(key)
					.append("=")
					.append(value);
		}
	}


	/**
	 * <p>Apply the MLT /select query to the Solr query.</p>
	 *
	 * @param solrQuery The Solr Query to use
	 * @param returnFieldSet The list of field to return for the FieldSet
	 * @param uniqueKeyValue The unique key value to perform the MLT on
	 *
	 * @throws PanlServerException if the Collection has not enabled the MLT processor.
	 */
	private void applyMltSelectQuery(SolrQuery solrQuery, List<String> returnFieldSet,
			String uniqueKeyValue) throws PanlServerException {
		if (!this.mltEnabled) {
			throw new PanlServerException("Attempting to retrieve an MLT query on a collection that hasn't enabled it.");
		}

		String stringBuilder = selectQueryStringBuilder + uniqueKeyValue;

		solrQuery.setRequestHandler(this.mltHandler);
		solrQuery.setQuery(stringBuilder);
		solrQuery.setRows(this.numResultsMoreLikeThis);

		solrQuery.setFields(returnFieldSet.toArray(new String[]{}));
	}

	/**
	 * <p>Apply the MLT to the /mlt Solr query handler.</p>
	 *
	 * @param solrQuery The Solr Query to use
	 * @param returnFieldSet The list of field to return for the FieldSet
	 * @param uniqueKeyValue The unique key value to perform the MLT on
	 */
	private void applyMltHandlerQuery(SolrQuery solrQuery, List<String> returnFieldSet, String uniqueKeyValue) {
		solrQuery.setMoreLikeThis(true);
		solrQuery.setRequestHandler(this.mltHandler);
		solrQuery.setMoreLikeThisFields(this.mltFieldArray);

		if (null != this.mltMinTermFrequency) {
			solrQuery.setMoreLikeThisMinTermFreq(this.mltMinTermFrequency);
		}

		if (null != this.mltMinDocFrequency) {
			solrQuery.setMoreLikeThisMinDocFreq(this.mltMinDocFrequency);
		}

		solrQuery.setQuery(uniqueKeySolrFieldName + ":" + uniqueKeyValue);

		if (null != this.mltMaxDocFrequency) {
			// for some unknown reason there is not a setter for this on the Solr
			// query - very odd
			solrQuery.set(MoreLikeThisParams.MAX_DOC_FREQ, this.mltMaxDocFrequency);
		}

		if (null != this.mltMaxDocFreqPercentage) {
			// for some unknown reason there is not a setter for this on the Solr
			// query - very odd
			solrQuery.set(MoreLikeThisParams.MAX_DOC_FREQ_PCT, this.mltMaxDocFreqPercentage);
		}

		if (null != this.mltMinWordLength) {
			solrQuery.setMoreLikeThisMinWordLen(this.mltMinWordLength);
		}

		if (null != this.mltMaxWordLength) {
			solrQuery.setMoreLikeThisMaxWordLen(this.mltMaxWordLength);
		}

		if (null != this.mltBoost) {
			solrQuery.setMoreLikeThisBoost(this.mltBoost);
		}

		if (null != this.mltMaxQueryTerms) {
			solrQuery.setMoreLikeThisMaxQueryTerms(this.mltMaxQueryTerms);
		}

		if (null != this.mltMaxNumTokensParse) {
			solrQuery.set(MoreLikeThisParams.MAX_NUM_TOKENS_PARSED, this.mltMaxNumTokensParse);
		}

		if (null != this.mltInterestingTerms) {
			// for some unknown reason there is not a setter for this on the Solr
			// query - very odd
			solrQuery.set(MoreLikeThisParams.INTERESTING_TERMS, this.mltInterestingTerms);
		}

		// for some unknown reason there are no setters for this on the Solr query
		// for the following - very odd
		if (null != this.mltMatchInclude) {
			solrQuery.set(MoreLikeThisParams.MATCH_INCLUDE, this.mltMatchInclude);
		}

		if (null != this.mltMatchOffset) {
			solrQuery.set(MoreLikeThisParams.MATCH_OFFSET, this.mltMatchOffset);
		}


		// This __MAY__ help getting the correct results...
		// always attempt to hit shard 1
		solrQuery.set(ShardParams.SHARDS, SHARD_1);
		solrQuery.set(ShardParams.SHARDS_PREFERENCE, REPLICA_LEADER_TRUE);
		solrQuery.set(ShardParams.SHARDS_PREFERENCE, REPLICA_LOCATION_LOCAL);

		solrQuery.setRows(this.numResultsMoreLikeThis);

		// This __probably__ has no effect...
		// solrQuery.set(MoreLikeThisParams.QF, this.mltQueryFieldArray);
		solrQuery.setFields(returnFieldSet.toArray(new String[]{}));
	}

	/**
	 * <p>Return whether MLT is enabled for this Panl collection.</p>
	 *
	 * @return Whether MLT is enabled for this collection.
	 */
	public boolean getIsMltEnabled() {
		return (this.mltEnabled);
	}

	/**
	 * <p>Return the number of retries for the MLT request to the Solr server.
	 * This is only used where the MLT type is <code>mlt</code> and is not used
	 * by the MLT <code>select</code> type.</p>
	 *
	 * @return The number of retries to the MLT Request handler for Solr
	 */
	public int getNumMltRetries() {
		return (this.numMltRetries);
	}
}
