package com.synapticloop.panl.server;

import com.synapticloop.panl.exception.PanlGenerateException;

public class PanlServer {
	private final String propertiesFileLocation;
	private final int portNumber;

	public PanlServer(String propertiesFileLocation, int portNumber) throws PanlGenerateException {
		this.propertiesFileLocation = propertiesFileLocation;
		this.portNumber = portNumber;

		parsePropertiesFile();
	}

	private void parsePropertiesFile() throws PanlGenerateException {
	}

	public void start() {
		// start up the server to listen
	}
}
