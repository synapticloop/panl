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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SolrFieldHolder {
	private static final Logger LOGGER = LoggerFactory.getLogger(SolrFieldHolder.class);

	private final String solrCollection;
	private final String panlCollectionUri;

	private Map<String, SolrPanlField> ALL_FIELDS = new HashMap<>();;
	private Map<String, SolrPanlField> FACET_FIELDS = new HashMap<>();;
	private Map<String, SolrPanlField> REGULAR_FIELDS = new HashMap<>();;

	private SolrPanlField uniqueKeySolrPanlField = null;

	/**
	 *
	 * @param solrCollection The Solr collection that this connects to, used for
	 *    debugging and logging.
	 * @param panlCollectionUri The Panl collection URI that this is bound to,
	 *    used for debugging and logging.
	 * @param properties
	 * @throws PanlServerException
	 */
	public SolrFieldHolder(
			String solrCollection,
			String panlCollectionUri,
			Properties properties) throws PanlServerException {

		this.solrCollection = solrCollection;
		this.panlCollectionUri = panlCollectionUri;

		for (String propertiesByPrefix : PropertyHelper.getPropertiesByPrefix(
				properties, Constants.Property.Panl.PANL_FACET)) {

			String lpseCode = propertiesByPrefix.substring(propertiesByPrefix.lastIndexOf('.') + 1);

			String solrFieldName = properties.getProperty(Constants.Property.Panl.PANL_FACET + lpseCode, null);

			boolean isMultivalue = properties.getProperty(Constants.Property.Panl.PANL_MULTIVALUE + lpseCode,
					Constants.BOOLEAN_FALSE_VALUE).equals(
					Constants.BOOLEAN_TRUE_VALUE);

			boolean isUniqueKey = properties.getProperty(Constants.Property.Panl.PANL_UNIQUEKEY + lpseCode,
					Constants.BOOLEAN_FALSE_VALUE).equals(
					Constants.BOOLEAN_TRUE_VALUE);

			SolrPanlField solrPanlField = new SolrPanlField(lpseCode, solrFieldName, true, isUniqueKey, isMultivalue);
			addToDataStructures(solrPanlField);
		}

		for (String propertiesByPrefix : PropertyHelper.getPropertiesByPrefix(
				properties, Constants.Property.Panl.PANL_FIELD)) {

			String lpseCode = propertiesByPrefix.substring(propertiesByPrefix.lastIndexOf('.') + 1);

			String solrFieldName = properties.getProperty(Constants.Property.Panl.PANL_FIELD + lpseCode, null);

			boolean isMultivalue = properties.getProperty(Constants.Property.Panl.PANL_MULTIVALUE + lpseCode,
					Constants.BOOLEAN_FALSE_VALUE).equals(
					Constants.BOOLEAN_TRUE_VALUE);

			boolean isUniqueKey = properties.getProperty(Constants.Property.Panl.PANL_UNIQUEKEY + lpseCode,
					Constants.BOOLEAN_FALSE_VALUE).equals(
					Constants.BOOLEAN_TRUE_VALUE);

			SolrPanlField solrPanlField = new SolrPanlField(lpseCode, solrFieldName, false, isUniqueKey, isMultivalue);
			addToDataStructures(solrPanlField);
		}
	}

	private void addToDataStructures(SolrPanlField solrPanlField) throws PanlServerException {
		if(solrPanlField.getIsUniqueKey()) {
			if(null != this.uniqueKeySolrPanlField) {
				throw new PanlServerException("Attempting to redefine the unique key for a Solr/Panl collection, is: " +
						uniqueKeySolrPanlField +
						", attempting to reassign to: " +
						solrPanlField +
						".");
			} else {
				this.uniqueKeySolrPanlField = solrPanlField;
			}
		}

		ALL_FIELDS.put(solrPanlField.getSolrFieldName(), solrPanlField);

		if(solrPanlField.getIsFacet()) {
			FACET_FIELDS.put(solrPanlField.getSolrFieldName(), solrPanlField);
		} else {
			REGULAR_FIELDS.put(solrPanlField.getSolrFieldName(), solrPanlField);
		}
	}

	/**
	 * <p>Get the Solr collection that these fields connect to</p>
	 *
	 * @return The Solr collection that these fields connect to
	 */
	public String getSolrCollection() {
		return solrCollection;
	}

	/**
	 * <p>Get the Panl collection URI that this is bound to.</p>
	 *
	 * @return The panl collection URI that this is bound to
	 */
	public String getPanlCollectionUri() {
		return panlCollectionUri;
	}

	/**
	 * <p>Return whether this is a defined field or facet.</p>
	 *
	 * @param solrFieldName The Solr field name to look up
	 *
	 * @return Whether this is a defined field or facet
	 */
	public boolean getIsFieldOrFacet(String solrFieldName) {
		return(ALL_FIELDS.containsKey(solrFieldName));
	}

	/**
	 * <p>Return whether this is a defined field.</p>
	 *
	 * @param solrFieldName The Solr field name to look up
	 *
	 * @return Whether this is a defined field
	 */
	public boolean getIsField(String solrFieldName) {
		return(REGULAR_FIELDS.containsKey(solrFieldName));
	}

	/**
	 * <p>Return whether this is a defined facet.</p>
	 *
	 * @param solrFieldName The Solr field name to look up
	 *
	 * @return Whether this is a defined facet
	 */
	public boolean getIsFacet(String solrFieldName) {
		return(FACET_FIELDS.containsKey(solrFieldName));
	}

	/**
	 * <p>Return the unique key for this collection.</p>
	 *
	 * @return The unique key for this collection.
	 */
	public SolrPanlField getUniqueKeySolrField() {
		return(uniqueKeySolrPanlField);
	}
}
