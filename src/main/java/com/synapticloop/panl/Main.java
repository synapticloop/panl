package com.synapticloop.panl;

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
 * IN THE SOFTWARE.
 */

import com.synapticloop.panl.editor.PanlProjectLauncher;
import com.synapticloop.panl.exception.CommandLineOptionException;
import com.synapticloop.panl.exception.PanlGenerateException;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.generator.PanlGenerator;
import com.synapticloop.panl.server.PanlServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>This is the main class for the PANL server/generator.</p>
 *
 * @author synapticloop
 */
public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static final String CMD_OPTION_OVERWRITE = "-overwrite";
	public static final String CMD_OPTION_PORT = "-port";
	public static final String CMD_OPTION_PROPERTIES = "-properties";
	public static final String CMD_OPTION_SCHEMA = "-schema";

	public static final String CMD_VALUE_SERVER = "server";
	public static final String CMD_VALUE_GENERATE = "generate";
	public static final String CMD_VALUE_EDITOR = "editor";

	private static final Set<String> ALLOWABLE_COMMANDS = new HashSet<>();

	static {
		ALLOWABLE_COMMANDS.add(CMD_VALUE_SERVER);
		ALLOWABLE_COMMANDS.add(CMD_VALUE_GENERATE);
		ALLOWABLE_COMMANDS.add(CMD_VALUE_EDITOR);
	}


	public static final String DEFAULT_PANL_PROPERTIES = "panl.properties";
	public static final String DEFAULT_PORT_NUMBER = "8181";

	private final Map<String, String> OPTIONS_MAP = new HashMap<>();

	private int portNumber = 8181;
	private String propertiesFileLocation = DEFAULT_PANL_PROPERTIES;
	private boolean shouldOverwrite = false;

	private final String[] args;

	/**
	 * <p>Instantiate the main class.</p>
	 *
	 * @param args The command line arguments
	 */
	public Main(String[] args) {
		this.args = args;
	}

	/**
	 * <p>Parse the command line options and execute the options.  This will
	 * execute one of:</p>
	 *
	 * <ol>
	 *   <li>Panl Server - start the Panl server</li>
	 *   <li>Panl Generator - start the Panl generator</li>
	 * </ol>
	 *
	 * @throws PanlServerException If the command line options were correctly
	 * 		parsed and the Panl server was executed, however there was an
	  		exception with the server
	 * @throws CommandLineOptionException If the command line options could not
	 * 		be parsed
	 * @throws PanlGenerateException If the command line options were correctly
	 * 		parsed and the Panl generator was executed, however there was an
	 * 		exception with the generations
	 */
	private void parseAndExecuteCommandLine() throws PanlServerException, CommandLineOptionException, PanlGenerateException {
		if (args.length < 1) {
			usageAndException("Could not determine command, should be one of 'server' or 'generate'");
		}

		String command = args[0];
		if (!ALLOWABLE_COMMANDS.contains(command)) {
			usageAndException(String.format("Unknown command of '%s', expecting 'server' or 'generate'", command));
		}

		// now go through the rest of the command line arguments
		for (int i = 1; i < args.length; i++) {
			String optionKey = args[i];
			try {
				String optionValue = args[i + 1];
				OPTIONS_MAP.put(optionKey, optionValue);
				i++;
			} catch (ArrayIndexOutOfBoundsException e) {
				usageAndException(String.format("Found a command line option of '%s' without a value", optionKey));
			}
		}

		// now parse the rest of the commands
		switch (command) {
			case CMD_VALUE_SERVER:
				parseAndExecuteServerCommands();
				break;
			case CMD_VALUE_GENERATE:
				parseAndExecuteGenerateCommands();
				break;
			case CMD_VALUE_EDITOR:
				SwingUtilities.invokeLater(() -> {
					new PanlProjectLauncher().show();
				});
				break;
		}
	}

	/**
	 * <p>Parse and execute the server component</p>
	 *
	 * @throws PanlServerException If there was an error starting the server
	 * @throws CommandLineOptionException If there was an error with the command
	 *  line options
	 */
	private void parseAndExecuteServerCommands() throws PanlServerException, CommandLineOptionException {
		this.propertiesFileLocation = OPTIONS_MAP.getOrDefault(CMD_OPTION_PROPERTIES, DEFAULT_PANL_PROPERTIES);
		String portNumberString = OPTIONS_MAP.getOrDefault(CMD_OPTION_PORT, DEFAULT_PORT_NUMBER);

		try {
			this.portNumber = Integer.parseInt(portNumberString);
		} catch (NumberFormatException e) {
			usageAndException(String.format("Could not parse port number of '%s'", portNumberString));
		}

		LOGGER.info("Starting Panl server with properties:");
		LOGGER.info("  -properties {}", this.propertiesFileLocation);
		LOGGER.info("        -port {}", this.portNumber);
		LOGGER.info("");
		LOGGER.info("            ~ ~ ~ * ~ ~ ~");
		LOGGER.info("");

		// at this point we are ready to go
		PanlServer panlServer = new PanlServer(this.propertiesFileLocation, this.portNumber);
		panlServer.start();
	}

	/**
	 * <p>Parse the command line options, and then execute the generation.</p>
	 *
	 * @throws CommandLineOptionException If there was an invalid command line
	 * 		option
	 * @throws PanlGenerateException If there was an error with the generation of
	 * 		the panl.properties file, or the &lt;collection&gt;.panl.properties
	 * 		file
	 */
	private void parseAndExecuteGenerateCommands() throws CommandLineOptionException, PanlGenerateException {
		this.propertiesFileLocation = OPTIONS_MAP.getOrDefault(CMD_OPTION_PROPERTIES, DEFAULT_PANL_PROPERTIES);
		String schemaFileLocations = OPTIONS_MAP.getOrDefault(CMD_OPTION_SCHEMA, null);
		String shouldOverwriteString = OPTIONS_MAP.getOrDefault(CMD_OPTION_OVERWRITE, "false").toLowerCase();

		if (shouldOverwriteString.compareTo("true") == 0) {
			this.shouldOverwrite = true;
		}

		if (null == schemaFileLocations) {
			usageAndException("Mandatory command line option of '-schema' missing.");
		}

		LOGGER.info("Starting Panl generation with properties:");
		LOGGER.info("  -properties {}", this.propertiesFileLocation);
		LOGGER.info("      -schema {}", schemaFileLocations);
		LOGGER.info("   -overwrite {}", this.shouldOverwrite);
		LOGGER.info("");
		LOGGER.info("            ~ ~ ~ * ~ ~ ~");
		LOGGER.info("");

		PanlGenerator panlGenerator = new PanlGenerator(
				this.propertiesFileLocation,
				schemaFileLocations,
				this.shouldOverwrite);

		panlGenerator.generate();
	}

	/**
	 * <p>Print a simple usage message.</p>
	 *
	 * @param message The message to prepend to the usage instructions
	 *
	 * @throws CommandLineOptionException Always throws this exception
	 */
	private void usageAndException(String message) throws CommandLineOptionException {
		System.out.printf("[ERROR]: %s\n", message);
		if (args.length == 0) {
			System.out.println("[ERROR]: No arguments provided.");
		} else {
			System.out.println("[ERROR]: arguments were:");
			for (String arg : args) {
				System.out.printf("[ERROR]:  %s\n", arg);
			}
		}

		// now print the usage
		try (InputStream inputStream = Main.class.getResourceAsStream("/usage.txt")) {
			if (null == inputStream) {
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

	/**
	 * <p>Main starting point for the application, parsing the command line
	 * options and executing the required component.  If there was an error when
	 * parsing the options then it will print out an error message and exit.</p>
	 *
	 * @param args The arguments to parse
	 */
	public static void main(String[] args) {
		Main main = new Main(args);

		LOGGER.info("                           __ ");
		LOGGER.info("       .-----.---.-.-----.|  |");
		LOGGER.info("       |  _  |  _  |     ||  |");
		LOGGER.info("       |   __|___._|__|__||__|");
		LOGGER.info("       |__|     ... .-..      ");
		LOGGER.info("");
		LOGGER.info("            ~ ~ ~ * ~ ~ ~");
		LOGGER.info("");

		try {
			main.parseAndExecuteCommandLine();
		} catch (PanlServerException | CommandLineOptionException | PanlGenerateException e) {
			LOGGER.error("FAILURE to start, message was: {}", e.getMessage(), e);
		}
	}
}
