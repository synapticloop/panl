package com.synapticloop.panl.server.handler.properties;

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

import com.formdev.flatlaf.util.StringUtils;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.util.Constants;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>This class contains all properties defined in the
 * <code>panl.properties</code> file.</p>
 *
 * @author synapticloop
 */
public class PanlProperties {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlProperties.class);

	/**
	 * <p>The SolrJ client to use.</p>
	 */
	private final String solrjClient;

	/**
	 * <p>The Solr search server URL(s) to use, which may include a zookeeper
	 * prfix</p>
	 */
	private final String solrSearchServerUrl;

	/**
	 * <p>Whether the testing URLs are enabled.</p>
	 */
	private final boolean hasPanlResultsTestingUrls;

	/**
	 * <p>Whether the testing URLs are enabled.</p>
	 */
	private final boolean panlStatus404Verbose;

	/**
	 * <p>Whether the testing URLs are enabled.</p>
	 */
	private final boolean panlStatus500Verbose;

	/**
	 * <p>Whether to use a decimal point as the delimiter between the integer and
	 * fractional parts.  (Used for internationalisation)</p>
	 */
	private static boolean isDecimalPoint = true;

	/**
	 * <p>Remove the Solr response keys that are duplicated in the panl response.</p>
	 */
	private final boolean removeSolrJsonKeys;

	/**
	 * <p>A map of the </p>
	 */
	private final Map<String, List<String>> panlCollections = new HashMap<>();

	/**
	 * <p>The extra information as a JSON Object</p>
	 */
	private JSONObject jsonExtraObject = null;

	/**
	 * <p>Instantiate the Panl properties which defines what Solr server to
	 * connect to, the SolrJ client, whether to use verbose messaging for 404
	 * and/or 500 error messages, and whether to serve the Panl results testing
	 * URL handlers.</p>
	 *
	 * @param properties The properties file
	 */
	public PanlProperties(Properties properties) throws PanlServerException {
		this.hasPanlResultsTestingUrls =
			properties
				.getProperty(Constants.Property.Panl.PANL_RESULTS_TESTING_URLS, Constants.BOOLEAN_FALSE_VALUE)
				.equals(Constants.BOOLEAN_TRUE_VALUE);

		PanlProperties.isDecimalPoint = properties
			.getProperty(Constants.Property.Panl.PANL_DECIMAL_POINT, Constants.BOOLEAN_FALSE_VALUE)
			.equals(Constants.BOOLEAN_TRUE_VALUE);

		String solrjClientTemp;
		solrjClientTemp = properties.getProperty(Constants.Property.Panl.SOLRJ_CLIENT, null);
		if (solrjClientTemp == null) {
			LOGGER.warn(
				"Property '{}' could not be found, defaulting to '{}'",
				Constants.Property.Panl.SOLRJ_CLIENT,
				Constants.Property.Panl.DEFAULT_CLOUD_SOLR_CLIENT);
			solrjClientTemp = Constants.Property.Panl.DEFAULT_CLOUD_SOLR_CLIENT;
		}

		this.solrjClient = solrjClientTemp;

		String solrSearchServerUrlTemp;
		solrSearchServerUrlTemp = properties.getProperty(Constants.Property.Panl.SOLR_SEARCH_SERVER_URL, null);
		if (solrSearchServerUrlTemp == null) {
			LOGGER.warn("Property '{}' could not be found, defaulting to '{}'", Constants.Property.Panl.SOLR_SEARCH_SERVER_URL,
				Constants.Property.Panl.DEFAULT_SOLR_URL);
			solrSearchServerUrlTemp = Constants.Property.Panl.DEFAULT_SOLR_URL;
		}

		this.solrSearchServerUrl = solrSearchServerUrlTemp;

		this.panlStatus404Verbose = properties
			.getProperty(Constants.Property.Panl.PANL_STATUS_404_VERBOSE, Constants.BOOLEAN_FALSE_VALUE)
			.equals(Constants.BOOLEAN_TRUE_VALUE);
		this.panlStatus500Verbose = properties
			.getProperty(Constants.Property.Panl.PANL_STATUS_500_VERBOSE, Constants.BOOLEAN_FALSE_VALUE)
			.equals(Constants.BOOLEAN_TRUE_VALUE);

		this.removeSolrJsonKeys = properties
				.getProperty(Constants.Property.Panl.PANL_REMOVE_SOLR_JSON_KEYS, Constants.BOOLEAN_FALSE_VALUE)
				.equals(Constants.BOOLEAN_TRUE_VALUE);

		String jsonTemp = properties.getProperty(Constants.Property.Panl.PANL_SERVER_EXTRA, "");
		if(!jsonTemp.trim().isEmpty()) {
			try {
				jsonExtraObject = new JSONObject(jsonTemp);
			} catch(JSONException ex) {
				throw new PanlServerException("Could not parse the property '" +
						Constants.Property.Panl.PANL_SERVER_EXTRA +
						"' which __MUST__ be a valid JSON Object.  value was '" +
						jsonTemp +
						"'.");
			}
		}

		for (String stringPropertyName : properties.stringPropertyNames()) {
			if (stringPropertyName.startsWith(Constants.Property.Panl.PANL_COLLECTION)) {
				String panlCollection = stringPropertyName.substring(
						Constants.Property.Panl.PANL_COLLECTION.length());
				String collectionPropertyFiles = properties.getProperty(stringPropertyName);
				List<String> tempList = StringUtils.split(collectionPropertyFiles, ',');
				List<String> finalList = new ArrayList<>();
				for (String propertyFile : tempList) {
					String propertyFileValue = propertyFile.trim();
					if (!propertyFileValue.isBlank()) {
						finalList.add(propertyFile);
					}
				}
				this.panlCollections.put(panlCollection, finalList);
			}
		}
	}

	/**
	 * <p>Return whether the Panl results testing URLs are available to service
	 * requests.  This should probably not be set 'true' for production deployments, or at the very least, not allowed to
	 * be accessed externally.</p>
	 *
	 * @return Whether the Panl results testing URL(s) are available
	 */
	public boolean getHasPanlResultsTestingUrls() {
		return hasPanlResultsTestingUrls;
	}

	/**
	 * <p>Return the configured SolrJ client.</p>
	 *
	 * @return The SolrJ client to use
	 */
	public String getSolrjClient() {
		return (solrjClient);
	}

	/**
	 * <p>Return the Solr server URL(s).  If there is more than one URL, this
	 * will be a comma separated string of URLs</p>
	 *
	 * @return The Solr server URL(s)
	 */
	public String getSolrSearchServerUrl() {
		return (solrSearchServerUrl);
	}

	/**
	 * <p>Whether to use verbose 404 messages.</p>
	 *
	 * @return Whether to return verbose 404 messages
	 */
	public boolean getUseVerbose404Messages() {
		return (panlStatus404Verbose);
	}

	/**
	 * <p>Whether to use verbose 500 messages.</p>
	 *
	 * @return Whether to return verbose 500 messages
	 */
	public boolean getUseVerbose500Messages() {
		return (panlStatus500Verbose);
	}

	/**
	 * <p>Return whether this Panl properties is using a decimal point as the
	 * separator for fractional parts.</p>
	 *
	 * <ul>
	 *   <li>If <code>true</code> then the decimal format will be of the form
	 *   <code>9,123,456.78</code></li>
	 *   <li>If <code>false</code> then the decimal format will be of the form
	 *   <code>9.123.456,78</code></li>
	 * </ul>
	 *
	 * @return Whether decimal points are used as the separator for the fractional
	 * part.
	 */
	public static boolean getIsDecimalPoint() {
		return isDecimalPoint;
	}

	/**
	 * <p>Set whether the decimal format uses a decimal point '<code>.</code>' to
	 * separate the integer part from the fractional part.  If set to <code>false</code>
	 * then separator is set to a comma '<code>,</code>'.</p>
	 *
	 * @param isDecimalPoint Whether to set the fractional separator to the decimal point
	 */
	public static void setIsDecimalPoint(boolean isDecimalPoint) {
		PanlProperties.isDecimalPoint = isDecimalPoint;
	}

	/**
	 * <p>Get the collections map for this panl properties file which is a map keyed
	 * on the Solr collection name with the value a list of properties files</p>
	 *
	 * @return The collections map for this
	 */
	public Map<String, List<String>> getPanlCollectionsMap() {
		return panlCollections;
	}

	/**
	 * <p>Return whether to remove the Solr JSON keys that are duplicated by the
	 * Panl response.</p>
	 *
	 * @return whether to remove the Solr JSON keys that are duplicated.
	 */
	public boolean getRemoveSolrJsonKeys() {
		return(removeSolrJsonKeys);
	}

	/**
	 * <p>Get the 'extra' information JSON Object (if set) - will return null if
	 * it is not set.</p>
	 *
	 * @return The 'extra' JSON Object - will return null if not set.
	 */
	public JSONObject getExtraJsonObject() {
		return(jsonExtraObject);
	}
}

