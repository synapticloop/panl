package com.synapticloop.panl.server;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.CollectionRequestHandler;
import com.synapticloop.panl.server.handler.PanlDefaultHandler;
import com.synapticloop.panl.server.handler.PanlRequestHandler;
import com.synapticloop.panl.server.handler.PanlResultsViewerHandler;
import com.synapticloop.panl.server.properties.BaseProperties;
import com.synapticloop.panl.server.properties.CollectionProperties;
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
	private BaseProperties baseProperties;

	private final List<CollectionRequestHandler> collectionRequestHandlers = new ArrayList<>();

	/**
	 * <p>Instantiate a new PanlServ er instance.</p>
	 *
	 * @param propertiesFileLocation The location of the panl.properties to load
	 * @param portNumber The port number from the command line option (or
	 *                   default of 8181)
	 * @throws PanlServerException If there was an error parsing the properties
	 */
	public PanlServer(String propertiesFileLocation, int portNumber) throws PanlServerException {
		this.propertiesFileLocation = propertiesFileLocation;
		this.portNumber = portNumber;

		parsePropertiesFile();
	}


	private void parsePropertiesFile() throws PanlServerException {
		Properties properties = new Properties();
		try {
			properties.load(new FileReader(propertiesFileLocation));
		} catch (IOException e) {
			throw new PanlServerException(e.getMessage());
		}

		baseProperties = new BaseProperties(properties);

		File file = new File(propertiesFileLocation);
		File propertiesFileDirectory = file.getAbsoluteFile().getParentFile();

		Enumeration<Object> keys = properties.keys();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			if(key.startsWith(PROPERTY_KEY_PANL_COLLECTION)) {
				// we have found a new collection
				String collectionName = key.substring(PROPERTY_KEY_PANL_COLLECTION.length());
				Properties collectionProperties = new Properties();
				String fileName = propertiesFileDirectory + File.separator + properties.getProperty(key);
				LOGGER.info("Found collection named '{}' with file location '{}'.", collectionName, fileName);

				try {
					collectionProperties.load(new FileReader(fileName));
					collectionRequestHandlers.add(new CollectionRequestHandler(
							collectionName,
							baseProperties,
							new CollectionProperties(
									collectionName,
									collectionProperties)));
				} catch (IOException e) {
					throw new PanlServerException(e.getMessage());
				}
			}
		}
	}

	/**
	 * <p>Start the PANL server, which relies on the Apache HttpComponents HttpSever.</p>
	 *
	 * <p>This sets up a servlet for each of the collections and binds it to the
	 * correct URL.</p>
	 *
	 * <p>It then sets up the panl results viewer servlet</p>
	 *
	 * <p>Finally, it starts the server on the passed in port number (of default
	 * port of 8181).</p>
	 *
	 * @throws PanlServerException If there was an error starting the server
	 */
	public void start() throws PanlServerException {

		ServerBootstrap bootstrap = ServerBootstrap
				.bootstrap()
				.setListenerPort(portNumber);

		// register the default Panl handler which returns a 404
		bootstrap.registerHandler("/*", new PanlDefaultHandler(collectionRequestHandlers));

		// register the panl results viewer - if one is available
		String panlResultsViewerUrl = baseProperties.getPanlResultsViewerUrl();
		if(null != panlResultsViewerUrl) {
			bootstrap.registerHandler(panlResultsViewerUrl, new PanlResultsViewerHandler(collectionRequestHandlers));
		}

		// finally register the collection handlers
		for(CollectionRequestHandler collectionRequestHandler : collectionRequestHandlers) {
			String collectionName = collectionRequestHandler.getCollectionName();
			bootstrap.registerHandler("/" + collectionName + "/*", new PanlRequestHandler(collectionRequestHandler));
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
		} catch (IOException | InterruptedException e) {
			throw new PanlServerException("Could not start the server.", e);
		}
	}
}
