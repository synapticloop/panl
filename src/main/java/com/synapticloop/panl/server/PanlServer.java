package com.synapticloop.panl.server;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.CollectionRequestHandler;
import com.synapticloop.panl.server.handler.PanlRequestHandler;
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
 * This is the runner for the PANL server
 * @author synapticloop
 */
public class PanlServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlServer.class);
	public static final String PROPERTY_KEY_PANL_COLLECTION = "panl.collection.";

	private final String propertiesFileLocation;
	private final int portNumber;

	private final List<CollectionRequestHandler> collectionRequestHandlers = new ArrayList<>();

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

		BaseProperties baseProperties = new BaseProperties(properties);

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
	 * Start the PANL server, which relies on the Apache HttpComponents HttpSever
	 *
	 * @throws PanlServerException I
	 */
	public void start() throws PanlServerException {

		ServerBootstrap bootstrap = ServerBootstrap
				.bootstrap()
				.setListenerPort(portNumber);

		for(CollectionRequestHandler collectionRequestHandler : collectionRequestHandlers) {
			String collectionName = collectionRequestHandler.getCollectionName();
			bootstrap.registerHandler("/" + collectionName + "/*", new PanlRequestHandler(collectionRequestHandler));
			LOGGER.info("Binding collection of '{}' to /{}/*", collectionName, collectionName);
			for (String resultFieldsName : collectionRequestHandler.getResultFieldsNames()) {
				LOGGER.info("Results will be available on /{}/{}/*", collectionName, resultFieldsName);
			}
		}

		HttpServer httpServer = bootstrap.create();

		try {
			httpServer.start();
			LOGGER.info("Server started on port {}", httpServer.getLocalPort());
			Runtime.getRuntime().addShutdownHook(new Thread(httpServer::stop));
			httpServer.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (IOException | InterruptedException e) {
			throw new PanlServerException("could not start the server.", e);
		}
	}
}
