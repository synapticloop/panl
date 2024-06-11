package com.synapticloop.panl.server;

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
import com.synapticloop.panl.server.handler.*;
import com.synapticloop.panl.server.handler.results.explainer.PanlResultsExplainerExplainHandler;
import com.synapticloop.panl.server.handler.results.explainer.PanlResultsExplainerHandler;
import com.synapticloop.panl.server.handler.results.viewer.PanlResultsViewerScriptHandler;
import com.synapticloop.panl.server.handler.results.PanlResultsStaticHandler;
import com.synapticloop.panl.server.handler.results.viewer.PanlResultsViewerHandler;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * <p>This is the PANL server which parses the <code>panl.properties</code> file, loads all
 * the <code>collection.panl.properties</code> files.  If there are any errors with either
 * of the files, a PanlServerException will be thrown and the server will refuse to start.</p>
 *
 * @author synapticloop
 */
public class PanlServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlServer.class);

	public static final String PROPERTY_KEY_PANL_COLLECTION = "panl.collection.";

	private final String propertiesFileLocation;
	private final int portNumber;
	private PanlProperties panlProperties;

	private final List<CollectionRequestHandler> collectionRequestHandlers = new ArrayList<>();
	private final List<CollectionProperties> collectionPropertiesList = new ArrayList<>();

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
	 * @see CollectionRequestHandler#CollectionRequestHandler(String, PanlProperties, CollectionProperties)
	 */
	private void parsePropertiesFile() throws PanlServerException {
		Properties properties = new Properties();
		try {
			properties.load(new FileReader(propertiesFileLocation));
		} catch (IOException e) {
			throw new PanlServerException(e.getMessage());
		}

		panlProperties = new PanlProperties(properties);

		File file = new File(propertiesFileLocation);
		File propertiesFileDirectory = file.getAbsoluteFile().getParentFile();

		Enumeration<Object> keys = properties.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if (key.startsWith(PROPERTY_KEY_PANL_COLLECTION)) {
				// we have found a new collection
				String collectionName = key.substring(PROPERTY_KEY_PANL_COLLECTION.length());
				Properties fileCollectionProperties = new Properties();
				String fileName = propertiesFileDirectory + File.separator + properties.getProperty(key);
				LOGGER.info("Found collection named '{}' with file location '{}'.", collectionName, fileName);

				CollectionProperties collectionProperties;
				try {
					fileCollectionProperties.load(new FileReader(fileName));
					collectionProperties = new CollectionProperties(
							collectionName,
							fileCollectionProperties);

					collectionPropertiesList.add(collectionProperties);
				} catch (IOException e) {
					throw new PanlServerException(e.getMessage());
				}

				collectionRequestHandlers.add(new CollectionRequestHandler(
						collectionName,
						panlProperties,
						collectionProperties));

				// AT this point we want
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
			bootstrap.registerHandler("/panl-results/static/*", new PanlResultsStaticHandler());

			// the simple search and querying webappp
			bootstrap.registerHandler("/panl-results-viewer/*", new PanlResultsViewerHandler(collectionRequestHandlers));
			bootstrap.registerHandler("/panl-results-viewer/script/", new PanlResultsViewerScriptHandler(collectionRequestHandlers));

			bootstrap.registerHandler("/panl-results-explainer/*", new PanlResultsExplainerHandler(collectionPropertiesList, collectionRequestHandlers));
			bootstrap.registerHandler("/panl-results-explainer/explain/*", new PanlResultsExplainerExplainHandler(collectionPropertiesList, collectionRequestHandlers));
		}

		// finally register the collection handlers
		for (CollectionRequestHandler collectionRequestHandler : collectionRequestHandlers) {
			String collectionName = collectionRequestHandler.getCollectionName();
			bootstrap.registerHandler("/" + collectionName + "/*", new PanlRequestHandler(panlProperties, collectionRequestHandler));
			LOGGER.info("Binding collection of '{}' to /{}/*", collectionName, collectionName);
			for (String resultFieldsName : collectionRequestHandler.getResultFieldsNames()) {
				LOGGER.info("Results will be available on /{}/{}/*", collectionName, resultFieldsName);
			}
		}

		// create the server
		HttpServer httpServer = bootstrap.create();

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
}
