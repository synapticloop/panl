package com.synapticloop.panl.server;

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
import com.synapticloop.panl.server.handler.*;
import com.synapticloop.panl.server.handler.webapp.singlepagesearch.PanlSinglePageSearchHandler;
import com.synapticloop.panl.server.handler.webapp.explainer.PanlResultsExplainerExplainHandler;
import com.synapticloop.panl.server.handler.webapp.explainer.PanlResultsExplainerHandler;
import com.synapticloop.panl.server.handler.webapp.viewer.PanlResultsViewerScriptHandler;
import com.synapticloop.panl.server.handler.webapp.PanlResultsStaticHandler;
import com.synapticloop.panl.server.handler.webapp.viewer.PanlResultsViewerHandler;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.util.Constants;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>This is the PANL server which parses the <code>panl.properties</code> file,
 * loads all the <code>&lt;panl_collection_url>.panl.properties</code> files.  If
 * there are any errors with either of the files, a PanlServerException will be
 * thrown and the server will refuse to start.</p>
 *
 * @author synapticloop
 */
public class PanlServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlServer.class);

	/**
	 * <p>The look up set so that duplicate collections are not registered</p>
	 */
	private final Set<String> registeredCollections = new HashSet<>();

	/**
	 * <p>The location of the <code>panl.properties file</code>.
	 * <strong>Note:</strong> that the file can be named anything, this is just
	 * the default name if not passed in as command line parameter.</p>
	 */
	private final String propertiesFileLocation;

	/**
	 * <p>The port number that this server will listen on.</p>
	 */
	private final int portNumber;

	/**
	 * <p>The Panl properties file.</p>
	 */
	private PanlProperties panlProperties;

	/**
	 * <p>All the Panl collection handlers.</p>
	 */
	private final List<CollectionRequestHandler> collectionRequestHandlers = new ArrayList<>();

	/**
	 * <p>The list of Collection Properties.</p>
	 */
	private final List<CollectionProperties> collectionPropertiesList = new ArrayList<>();

	/**
	 * <p>The HTTP Server instance</p>
	 */
	private HttpServer httpServer;

	/**
	 * <p>Instantiate a new PanlServer instance.  This will parse the
	 * <code>properties.properties</code> and any linked
	 * <code>collection.panl.properties</code> files which are referenced by the
	 * list of properties with the property key starting with
	 * <code>panl.collection.*</code>.</p>
	 *
	 * @param propertiesFileLocation The location of the <code>panl.properties</code>
	 * 		file to load
	 * @param portNumber The port number from the command line option (or
	 * 		default of 8181)
	 *
	 * @throws PanlServerException If there was an error parsing the properties
	 */
	public PanlServer(String propertiesFileLocation, int portNumber) throws PanlServerException {
		this.propertiesFileLocation = propertiesFileLocation;
		this.portNumber = portNumber;

		parsePropertiesFile();
	}


	/**
	 * <p>Parse the <code>panl.properties</code> file parsing, validating, and
	 * setting the fields This also instantiates new CollectionRequestHandlers</p>
	 *
	 * @throws PanlServerException If there was an error parsing the properties
	 * 		file.
	 * @see CollectionRequestHandler#CollectionRequestHandler(String, String, PanlProperties, CollectionProperties)
	 */
	private void parsePropertiesFile() throws PanlServerException {
		Properties properties = new Properties();
		try {
			properties.load(new FileReader(propertiesFileLocation));
		} catch (IOException e) {
			throw new PanlServerException(e.getMessage());
		}

		panlProperties = new PanlProperties(properties);

		File panlPropertiesFile = new File(propertiesFileLocation);
		File propertiesFileDirectory = panlPropertiesFile.getAbsoluteFile().getParentFile();

		Enumeration<Object> keys = properties.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if (key.startsWith(Constants.Property.Panl.PANL_COLLECTION)) {
				// we have found a new collection
				String solrCollection = key.substring(Constants.Property.Panl.PANL_COLLECTION.length());

				for (String propertyFileName : properties.getProperty(key).split(",")) {
					Properties propertiesCollectionProperties = new Properties();

					CollectionProperties collectionProperties;
					String panlCollectionUri;
					try {
						File collectionPropertiesFile = new File(propertiesFileDirectory + File.separator + propertyFileName.trim());

						String fileName = collectionPropertiesFile.getName();
						// TODO - need a set to lookup so that there aren't multiple panl
						//   collection names bound

						panlCollectionUri = fileName.substring(0, fileName.indexOf("."));
						if(registeredCollections.contains(panlCollectionUri)) {
							throw new PanlServerException("Collection '" + panlCollectionUri + "' is already registered.");
						}
						registeredCollections.add(panlCollectionUri);

						if(panlCollectionUri.toLowerCase().startsWith("panl-")) {
							throw new PanlServerException("Collection '" + panlCollectionUri + "' starts with the " +
									"case-insensitive text 'panl-' which is disallowed.");
						}

						LOGGER.info("Found Solr collection named '{}' with properties file named '{}'.", solrCollection, fileName);

						propertiesCollectionProperties.load(new FileReader(collectionPropertiesFile));

						collectionProperties = new CollectionProperties(
								solrCollection,
								panlCollectionUri,
								propertiesCollectionProperties,
								panlProperties.getExtraJsonObject());


						collectionPropertiesList.add(collectionProperties);
					} catch (IOException e) {
						throw new PanlServerException(e.getMessage());
					}


					collectionRequestHandlers.add(new CollectionRequestHandler(
							solrCollection,
							panlCollectionUri,
							panlProperties,
							collectionProperties));
				}
			}
		}
	}

	/**
	 * <p>Start the PANL server, initialising the required Servlets and handlers
	 * for URLs.</p>
	 *
	 * <p>There are three types of request handlers.</p>
	 *
	 * <ol>
	 *   <li>The Default Handler - the default handler for requests that do not
	 *   match any other request.</li>
	 *   <li>(Optional) The Panl Results View Handler - the in-built test webapp
	 *   to view the results and the LPSE URIs</li>
	 *   <li>The Panl Request Handler - one for each of the collections.</li>
	 * </ol>
	 *
	 * <p><strong>The Default Handler:</strong> Bound to the root context (i.e.
	 * <code>/*</code>) which will always return a HTTP status code of 404 and
	 * the body content will be of the format:</p>
	 *
	 * <pre>
	 * {
	 *   "error":404,
	 *   "message":"Could not find a PANL request url, see 'valid_urls' array.",
	 *   "valid_urls":[
	 *     "/example/*"
	 *   ]
	 * }
	 * </pre>
	 * <p><strong>The Panl Results View Handler:</strong> <em>(Optional)</em>
	 * Bound to the context <code>/panl-results-viewer/*</code>, it provides a
	 * way to look at all panl contexts and see the results, with search and
	 * faceting.</p>
	 *
	 * <p>Note that this will only be available if the property
	 * <code>panl.results.testing.urls=true</code> in the
	 * <code>panl.properties</code> file.</p>
	 *
	 * <p><strong>The Panl Request Handler:</strong> Will bind itself to the
	 * context <code>/&lt;collection_uri&gt;/&lt;field_set&gt;/*</code> for
	 * each collection and fieldset that is configured for the Panl Server.</p>
	 *
	 * <p>Finally, it starts the server on the passed in port number (or default
	 * port of 8181) and waits for incoming requests.</p>
	 *
	 * @throws PanlServerException If there was an error starting the server
	 * @see PanlDefaultHandler PanlDefaultHandler - the default handler
	 * @see PanlResultsViewerHandler PanlResultsViewerHandler - the results
	 * 		viewer handler
	 * @see PanlRequestHandler PanlRequestHandler - the Panl request handler for each of the
	 * 		collections
	 */
	public void start() throws PanlServerException {

		ServerBootstrap bootstrap = ServerBootstrap
				.bootstrap()
				.setListenerPort(portNumber);

		// register the default Panl handler which returns a 404
		bootstrap.registerHandler("/*", new PanlDefaultHandler(panlProperties, collectionRequestHandlers));

		// register the panl results viewer - if it enabled

		if (panlProperties.getHasPanlResultsTestingUrls()) {
			LOGGER.info("Panl testing URLs are active, binding the following:");

			bootstrap.registerHandler("/webapp/static/*", new PanlResultsStaticHandler());
			LOGGER.info("Binding testing URL: /webapp/static/*");

			// the simple search and querying webappp
			bootstrap.registerHandler("/panl-results-viewer/*", new PanlResultsViewerHandler(collectionRequestHandlers));
			LOGGER.info("Binding testing URL: /panl-results-viewer/*");
			bootstrap.registerHandler("/panl-results-viewer/script/", new PanlResultsViewerScriptHandler(collectionRequestHandlers));
			LOGGER.info("Binding testing URL: /panl-results-viewer/script/");

			bootstrap.registerHandler("/panl-results-explainer/*", new PanlResultsExplainerHandler(collectionPropertiesList, collectionRequestHandlers));
			LOGGER.info("Binding testing URL: /panl-results-explainer/*");
			bootstrap.registerHandler("/panl-results-explainer/explain/*", new PanlResultsExplainerExplainHandler(collectionPropertiesList, collectionRequestHandlers));
			LOGGER.info("Binding testing URL: /panl-results-explainer/explain/*");

			bootstrap.registerHandler("/panl-single-page-search/*", new PanlSinglePageSearchHandler(collectionRequestHandlers));
			LOGGER.info("Binding testing URL: /panl-single-page-search/*");

		}

		// register the single page search handlers

		bootstrap.registerHandler(
			PanlSinglePageHandler.PANL_URL_BINDING_SINGLE_PAGE + "*",
			new PanlSinglePageHandler(
				panlProperties,
				collectionRequestHandlers));

		LOGGER.info("Binding more facets handler to URI path {}*", PanlMoreFacetsHandler.PANL_URL_BINDING_MORE_FACETS);
		bootstrap.registerHandler(
			PanlMoreFacetsHandler.PANL_URL_BINDING_MORE_FACETS + "*",
			new PanlMoreFacetsHandler(
				panlProperties,
				collectionRequestHandlers));

		LOGGER.info("Binding lookahead handler to URI path {}*", PanlLookaheadHandler.PANL_URL_BINDING_LOOKAHEAD);
		bootstrap.registerHandler(
			PanlLookaheadHandler.PANL_URL_BINDING_LOOKAHEAD + "*",
			new PanlLookaheadHandler(
				panlProperties,
				collectionRequestHandlers));

		LOGGER.info("Binding More Like This (MLT) handler to URI path {}*", PanlMoreLikeThisHandler.PANL_URL_BINDING_MORE_LIKE_THIS);
		bootstrap.registerHandler(
				PanlMoreLikeThisHandler.PANL_URL_BINDING_MORE_LIKE_THIS + "*",
				new PanlMoreLikeThisHandler(
						panlProperties,
						collectionRequestHandlers));


		// finally register the collection and singlepagesearch handlers
		for (CollectionRequestHandler collectionRequestHandler : collectionRequestHandlers) {
			String solrCollection = collectionRequestHandler.getSolrCollection();
			String panlCollectionUri = collectionRequestHandler.getPanlCollectionUri();
			bootstrap.registerHandler("/" + panlCollectionUri + "/*", new PanlRequestHandler(panlProperties, collectionRequestHandler));
			LOGGER.info("Binding Solr collection of '{}' to Panl collection URI path /{}/*", solrCollection, panlCollectionUri);
			for (String resultFieldsName : collectionRequestHandler.getResultFieldsNames()) {
				LOGGER.info("Results will be available on /{}/{}/*", panlCollectionUri, resultFieldsName);
			}

			LOGGER.info(
				"Binding Solr collection of '{}' to Panl Single Page configuration URI path {}{}/*",
				solrCollection,
				PanlSinglePageHandler.PANL_URL_BINDING_SINGLE_PAGE,
				panlCollectionUri);
		}

		// create the server
		httpServer = bootstrap.create();

		// Attempt to start the server
		try {
			httpServer.start();
			LOGGER.info("Server started on port {}", httpServer.getLocalPort());
			Runtime.getRuntime().addShutdownHook(new Thread(httpServer::stop));
			httpServer.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (Exception e) {
			throw new PanlServerException("Could not start the server.", e);
		}
	}

	/**
	 * <p>Stop the server.</p>
	 */
	public void stop() {
		httpServer.stop();
	}
}
