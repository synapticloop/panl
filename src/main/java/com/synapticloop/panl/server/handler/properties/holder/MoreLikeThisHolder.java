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
import com.synapticloop.panl.util.PanlLPSEHelper;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.MoreLikeThisParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * <p>This holds all 'More Like This' properties and can be used to apply the
 * parameters to the query.</p>
 *
 * @see <a href="https://solr.apache.org/guide/solr/latest/query-guide/morelikethis.html">Apache Solr More Like This</a>
 *
 * @author Synapticloop
 */

public class MoreLikeThisHolder {
	private static final Logger LOGGER = LoggerFactory.getLogger(MoreLikeThisHolder.class);

	private boolean mltEnabled = false;
	private String mltHandler = Constants.DEFAULT_MLT_HANDLER;

	// (Required - Default: false) Specifies the fields to use for similarity. A
	// list of fields can be provided separated by commas. If possible, the fields
	// should have stored termVectors.
	private String mltFl;  // maps to mlt.fl
	private String[] mltFieldList;

	// (Optional - Default: 2) Specifies the minimum frequency below which terms
	// will be ignored in the source document.
	private int mltMinTermFrequency = 2;

	// (Optional - Default: 5) Specifies the maximum frequency above which terms
	// will be ignored which occur in more than this many documents.
	private int mltMinDocFrequency = 5;

	// (Optional - Default: none) Specifies the maximum frequency above which
	// terms will be ignored which occur in more than this many documents.
	private int mltMaxDocFrequency;

	// (Optional - Default: none) Specifies the maximum document frequency using a
	// ratio relative to the number of documents in the index. The value provided
	// must be an integer between 0 and 100. For example, mlt.maxdfpct=75 means
	// the word will be ignored if it occurs in more than 75 percent of the
	// documents in the index.
	private int mltMaxDocFreqPercentage;

	// (Optional - Default: none) Sets the minimum word length below which words
	// will be ignored.
	private int mltMinWordLength;

	// (Optional - Default: none) Sets the maximum word length above which words
	// will be ignored.
	private int mltMaxWordLength;

	// (Optional - Default: 25) Sets the maximum number of query terms that will
	// be included in any generated query.
	private int mltMaxQueryTerms;

	// (Optional - Default: 5000) Sets the maximum number of tokens to parse in
	// each example document field that is not stored with TermVector support.
	private int mltMaxNumTokensParse;

	// (Optional - Default: false) Specifies if the query will be boosted by the
	// interesting term relevance. Possible values are true or false.
	private boolean mltBoost = false;

	// (Optional - Default: false) Query fields and their boosts using the same
	// format used by the DisMax Query Parser. These fields must also be specified
	// in mlt.fl.
	private String mltQueryFields;

	// (Optional - Default: none) Adds a section in the response that shows the
	// top terms (based on TF/IDF) used for the MoreLikeThis query. It supports
	// three possible values:
	//
	//  - list lists the terms.
	//  - none lists no terms (the default).
	//  - details lists the terms along with the boost value used for each term.
	//    Unless mlt.boost=true, all terms will have boost=1.0.
	private String mltInterestingTerms;

	// (Optional - Default: true) Specifies if the response should include the
	// matched document. If set to false, the response will look like a normal
	// select response.
	private boolean mltMatchInclude;

	// (Optional - Default: none) Specifies an offset into the main query search
	// results to locate the document on which the MoreLikeThis query should
	// operate. By default, the query operates on the first result for the q
	// parameter.
	private int mltMatchOffset;

	/**
	 * <p>The Solr Field Name that is the unique key</p>
	 */
	private String uniqueKeySolrFieldName;
	/**
	 * <p>Instantiate an MLT Holder and parse and process the properties to set
	 * them.  This will only parse the property file if the property
	 * <code>panl.mlt.enable</code> is set to <code>true</code> (case-sensitive)</p>
	 *
	 * @param properties The properties to use as a lookup
	 *
	 * @throws PanlServerException If there was an error with one of the
	 * properties
	 */
	public MoreLikeThisHolder(Properties properties, SolrFieldHolder solrFieldHolder) throws PanlServerException {
		this.mltEnabled = properties.getProperty(Constants.Property.Panl.PANL_MLT_ENABLE,
				Constants.BOOLEAN_FALSE_VALUE).equals(Constants.BOOLEAN_TRUE_VALUE);

		if(!this.mltEnabled) {
			return;
		}

		// at this point MLT is enabled so we should load the additional properties

		SolrPanlField uniqueKeySolrPanlField = solrFieldHolder.getUniqueKeySolrField();
		if(null == uniqueKeySolrPanlField) {
			throw new PanlServerException("Solr More Like This query handler requires a query of the Solr uniqueKey field, " +
					"which has not been defined in the <panl_collection_url>.panl.properties file.  You must add ONE and ONLY " +
					"one panl.uniquekey.<lpse_code>=true property to the file.");
		} else {
			this.uniqueKeySolrFieldName = uniqueKeySolrPanlField.getSolrFieldName();
		}

		this.mltFl = properties.getProperty(Constants.Property.Panl.PANL_MLT_FL, null);
		// if the above is null - error
		if(null == mltFl) {
			throw new PanlServerException("The property '" + Constants.Property.Panl.PANL_MLT_FL + "' MUST " +
					"be set to enable the Panl MLT");
		}

		// now go through
		this.mltFieldList = this.mltFl.split(",");
		for (String solrFieldName : this.mltFieldList) {
			if (!solrFieldHolder.getIsFieldOrFacet(solrFieldName)) {
				throw new PanlServerException("Attempting to define property '" +
						Constants.Property.Panl.PANL_MLT_FL +
						"' with a field value of '" +
						solrFieldName +
						"' which is not defined in the <panl_collection_url>.panl.properties file.");
			}
		}

		this.mltMinTermFrequency = PropertyHelper.getIntProperty(
				LOGGER,
				properties,
				Constants.Property.Panl.PANL_MLT_MINTF,
			2);

		this.mltMinDocFrequency = PropertyHelper.getIntProperty(
				LOGGER,
				properties,
				Constants.Property.Panl.PANL_MLT_MINDF,
				5);

		this.mltMaxDocFrequency = PropertyHelper.getIntProperty(
				LOGGER,
				properties,
				Constants.Property.Panl.PANL_MLT_MAXDF,
				-1);

		this.mltMaxDocFreqPercentage = PropertyHelper.getIntProperty(
				LOGGER,
				properties,
				Constants.Property.Panl.PANL_MLT_MAXDFPCT,
				-1);
		if(this.mltMaxDocFreqPercentage != -1) {
			if(this.mltMaxDocFreqPercentage < 0 || this.mltMaxDocFreqPercentage > 100) {
				throw new PanlServerException("The property '" + Constants.Property.Panl.PANL_MLT_MAXDFPCT + "' MUST be " +
						"between 0 and 100 (inclusive)");
			}
		}

		this.mltMinWordLength = PropertyHelper.getIntProperty(
				LOGGER,
				properties,
				Constants.Property.Panl.PANL_MLT_MINWL,
				-1);

		this.mltMaxWordLength = PropertyHelper.getIntProperty(
				LOGGER,
				properties,
				Constants.Property.Panl.PANL_MLT_MAXWL,
				-1);

		this.mltMaxQueryTerms = PropertyHelper.getIntProperty(
				LOGGER,
				properties,
				Constants.Property.Panl.PANL_MLT_MAXQT,
				25);

		this.mltMaxNumTokensParse = PropertyHelper.getIntProperty(
				LOGGER,
				properties,
				Constants.Property.Panl.PANL_MLT_MAXNTP,
				5000);

		this.mltBoost = properties.getProperty(Constants.Property.Panl.PANL_MLT_BOOST, Constants.BOOLEAN_FALSE_VALUE).equals(Constants.BOOLEAN_TRUE_VALUE);

		this.mltQueryFields = properties.getProperty(Constants.Property.Panl.PANL_MLT_QF);

		this.mltInterestingTerms = properties.getProperty(Constants.Property.Panl.PANL_MLT_INTERESTINGTERMS, null);
		if(null != this.mltInterestingTerms) {
			switch(this.mltInterestingTerms) {
				case "list":
				case "details":
					break;
				case "none":
					this.mltInterestingTerms = null;
					break;
				default:
					throw new PanlServerException("The property '" + Constants.Property.Panl.PANL_MLT_INTERESTINGTERMS + "' " +
							"MUST be one of 'none', 'list', or 'details' (case-sensitive and without the single quotes.");
			}
		}

		this.mltMatchInclude = properties.getProperty(Constants.Property.Panl.PANL_MLT_MATCH_INCLUDE,
				Constants.BOOLEAN_TRUE_VALUE).equals(Constants.BOOLEAN_TRUE_VALUE);

		this.mltMatchOffset = PropertyHelper.getIntProperty(
				LOGGER,
				properties,
				Constants.Property.Panl.PANL_MLT_MATCH_OFFSET,
				-1);
	}

	/**
	 * <p>Apply the More Like This (MLT) parameters to the passed in Solr query.
	 * This should only be done if this is an MLT enabled for this collection,
	 * otherwise an exception will be thrown.</p>
	 *
	 * @param solrQuery The Solr query to apply the MLT parameters to
	 * @param uniqueKeyValue The value for the unique key to base the More Like
	 * This search on.
	 *
	 * @throws PanlServerException if the Collection has not enabled the MLT
	 * processor.
	 */
	public void applyMltToQuery(SolrQuery solrQuery, String uniqueKeyValue) throws PanlServerException {
		if(!this.mltEnabled) {
			throw new PanlServerException("Attempting to retrieve an MLT query on a collection that hasn't enabled it.");
		}

		solrQuery.setMoreLikeThis(true);
		solrQuery.setRequestHandler(this.mltHandler);
		solrQuery.setMoreLikeThisFields(this.mltFieldList);
		solrQuery.setMoreLikeThisMinTermFreq(this.mltMinTermFrequency);
		solrQuery.setMoreLikeThisMinDocFreq(this.mltMinDocFrequency);
		// TODO - check that this is correct
		solrQuery.setQuery(uniqueKeySolrFieldName + ":" + PanlLPSEHelper.encodeURIPath(uniqueKeyValue));

		if(this.mltMaxDocFrequency != -1) {
			// for some unknown reason there is not a setter for this on the Solr
			// query - very odd
			solrQuery.set(MoreLikeThisParams.MAX_DOC_FREQ, this.mltMaxDocFrequency);
		}

		if(this.mltMaxDocFreqPercentage != -1) {
			// for some unknown reason there is not a setter for this on the Solr
			// query - very odd
			solrQuery.set(MoreLikeThisParams.MAX_DOC_FREQ_PCT, this.mltMaxDocFreqPercentage);
		}

		if(this.mltMinWordLength != -1) {
			solrQuery.setMoreLikeThisMinWordLen(this.mltMinWordLength);
		}

		if(this.mltMaxWordLength != -1) {
			solrQuery.setMoreLikeThisMaxWordLen(this.mltMaxWordLength);
		}

		solrQuery.setMoreLikeThisBoost(this.mltBoost);

		solrQuery.setMoreLikeThisMaxQueryTerms(this.mltMaxQueryTerms);

		if(null != this.mltInterestingTerms) {
			// for some unknown reason there is not a setter for this on the Solr
			// query - very odd
			solrQuery.set(MoreLikeThisParams.INTERESTING_TERMS, this.mltInterestingTerms);
		}

		// for some unknown reason there are no setters for this on the Solr query
		// for the following - very odd
		solrQuery.set(MoreLikeThisParams.MATCH_INCLUDE, this.mltMatchInclude);
		solrQuery.set(MoreLikeThisParams.MATCH_OFFSET, this.mltMatchOffset);
	}

	/**
	 * <p>Return whether MLT is enabled for this Panl collection.</p>
	 *
	 * @return Whether MLT is enabled for this collection.
	 */
	public boolean getIsMltEnabled() {
		return(this.mltEnabled);
	}
}
