package com.synapticloop.panl;

/*
 * Copyright (c) 2008-2024 synapticloop.
 *
 * https://github.com/synapticloop/panl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 *  IN THE SOFTWARE.
 */

import com.synapticloop.panl.exception.CommandLineOptionException;
import com.synapticloop.panl.exception.PanlGenerateException;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.generator.PanlGenerator;
import com.synapticloop.panl.server.PanlServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is the main starting point for the PANL server/generator
 */
public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private static final Set<String> ALLOWABLE_COMMANDS = new HashSet<>();
	static {
		ALLOWABLE_COMMANDS.add("server");
		ALLOWABLE_COMMANDS.add("generate");
	}

	public static final String DEFAULT_PANL_PROPERTIES = "panl.properties";
	public static final String DEFAULT_PORT_NUMBER = "8181";

	private final Map<String, String> OPTIONS_MAP = new HashMap<>();

	private int portNumber = 8181;
	private String propertiesFileLocation = DEFAULT_PANL_PROPERTIES;
	private boolean shouldOverwrite = false;

	private final String[] args;

	public Main(String[] args) {
		this.args = args;
	}

	private void parseCommandLine() throws PanlServerException, CommandLineOptionException, PanlGenerateException {
		if(args.length < 1) {
			usage("Could not determine command, should be one of 'server' or 'generate'");
		}

		String command = args[0];
		if(!ALLOWABLE_COMMANDS.contains(command)) {
			usage(String.format("Unknown command of '%s', expecting 'server' or 'generate'", command));
		}

		// now go through the rest of the command line arguments
		for(int i = 1; i < args.length; i++) {
			String optionKey = args[i];
			try {
				String optionValue = args[i + 1];
				OPTIONS_MAP.put(optionKey, optionValue);
				i++;
			} catch (ArrayIndexOutOfBoundsException e) {
				usage(String.format("Found a command line option of '%s' without a value", optionKey));
			}
		}

		// now parse the rest of the commands
		if(command.equals("server")) {
			parseAndExecuteServerCommands();
		} else {
			parseAndExecuteGenerateCommands();
		}
	}

	private void parseAndExecuteServerCommands() throws PanlServerException, CommandLineOptionException {
		this.propertiesFileLocation = OPTIONS_MAP.getOrDefault("-properties", DEFAULT_PANL_PROPERTIES);
		String portNumberString = OPTIONS_MAP.getOrDefault("-port", DEFAULT_PORT_NUMBER);
		try {
			this.portNumber = Integer.parseInt(portNumberString);
		} catch(NumberFormatException e) {
			usage(String.format("Could not parse port number of '%s'", portNumberString));
		}

		LOGGER.info("Starting server with properties:");
		LOGGER.info("  -properties {}", this.propertiesFileLocation);
		LOGGER.info("        -port {}", this.portNumber);

		// at this point we are ready to go
		PanlServer panlServer = new PanlServer(this.propertiesFileLocation, this.portNumber);
		panlServer.start();
	}

	/**
	 * <p>Parse the command line options, and then execute the generation.</p>
	 *
	 * @throws CommandLineOptionException If there was an invalid command line
	 *         option
	 * @throws PanlGenerateException If there was an error with the generation of
	 *         the panl.properties file, or the &lt;collection&gt;.panl.properties
	 *         file
	 */
	private void parseAndExecuteGenerateCommands() throws CommandLineOptionException, PanlGenerateException {
		this.propertiesFileLocation = OPTIONS_MAP.getOrDefault("-properties", DEFAULT_PANL_PROPERTIES);
		String schemaFileLocations = OPTIONS_MAP.getOrDefault("-schema", null);
		String shouldOverwriteString = OPTIONS_MAP.getOrDefault("-overwrite", "false").toLowerCase();

		if(shouldOverwriteString.compareTo("true") == 0) {
			this.shouldOverwrite = true;
		}

		if(null == schemaFileLocations) {
			usage("Mandatory command line option of '-schema' missing.");
		}

		LOGGER.info("Starting generation with properties:");
		LOGGER.info("  -properties {}", this.propertiesFileLocation);
		LOGGER.info("      -schema {}", schemaFileLocations);
		LOGGER.info("   -overwrite {}", this.shouldOverwrite);

		PanlGenerator panlGenerator = new PanlGenerator(
				this.propertiesFileLocation,
				schemaFileLocations,
				this.shouldOverwrite);

		panlGenerator.generate();
	}

	private void usage(String message) throws CommandLineOptionException {
		System.out.printf("[ERROR]: %s\n", message);
		if(args.length == 0) {
			System.out.println("[ERROR]: No arguments provided.");
		} else {
			System.out.println("[ERROR]: arguments were:");
			for (String arg : args) {
				System.out.printf("[ERROR]:  %s\n", arg);
			}
		}

		// now print the usage
		try (InputStream inputStream = Main.class.getResourceAsStream("/usage.txt")) {
			if(null == inputStream) {
				System.out.println("[FATAL]: Could not read the usage.txt file.");
			} else {
				System.out.println(
						new BufferedReader(
								new InputStreamReader(inputStream))
								.lines()
								.collect(Collectors.joining("\n")));
			}
		} catch (IOException ignored) {
		}
		throw new CommandLineOptionException("Invalid command line options.");
	}

	public static void main(String[] args) throws PanlServerException, CommandLineOptionException, PanlGenerateException {
		Main main = new Main(args);
		main.parseCommandLine();
	}
}
