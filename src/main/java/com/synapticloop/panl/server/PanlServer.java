package com.synapticloop.panl.server;

import com.synapticloop.panl.exception.PanlGenerateException;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.bean.Collection;
import org.rapidoid.http.Req;
import org.rapidoid.http.ReqRespHandler;
import org.rapidoid.http.Resp;
import org.rapidoid.http.customize.ErrorHandler;
import org.rapidoid.setup.App;
import org.rapidoid.setup.My;
import org.rapidoid.setup.On;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class PanlServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlServer.class);
	private final String propertiesFileLocation;
	private final int portNumber;

	public static String solrSearchIndexerUrl;
	public static String solrFacetEnabled;
	public static String solrFacetMinCount;
	public static String solrRows;

	public static String solrSearchServerUrl;
	private final List<Collection> collections = new ArrayList<>();

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

		solrSearchServerUrl = properties.getProperty("solr.search.server.url");
		solrSearchIndexerUrl = properties.getProperty("solr.search.indexer.url");
		solrFacetEnabled = properties.getProperty("solr.facet.enabled");
		solrFacetMinCount = properties.getProperty("solr.facet.min.count");
		solrRows = properties.getProperty("solr.rows");

		Enumeration<Object> keys = properties.keys();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			if(key.startsWith("panl.collection.")) {
				// we have found a new collection
				String collectionName = key.substring("panl.collection.".length());
				LOGGER.info("Found collection of '{}', binding to '/{}'", collectionName, collectionName);
				collections.add(new Collection(collectionName, properties.getProperty(key)));
			}
		}

	}

	public void start() {
		// start up the server to listen
		// TODO - server port number
		App.profiles("production");
		On.port(portNumber);

		for(Collection collection: collections) {
			On.get("/" + collection.getCollectionName() + "/*").json(new PanlReqRespHandler(collection));
		}
	}
}
