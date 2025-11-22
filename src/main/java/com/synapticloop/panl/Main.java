package com.synapticloop.panl;

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

import com.synapticloop.panl.exception.CommandLineOptionException;
import com.synapticloop.panl.exception.PanlGenerateException;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.generator.PanlGenerator;
import com.synapticloop.panl.server.PanlServer;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>This is the main class for the PANL server/generator.</p>
 *
 * @author synapticloop
 */
public class Main {
	public static final String UNKNOWN_SHRUG = "Unknown - ¯\\_(ツ)_/¯";

	// We want to find the log4j.xml file, so we are doing it first.  If it cannot
	// be found, then we hope that it will be on the classpath
	static {
		String[] locations = {
			"./log4j2.xml",
			"./lib/log4j2.xml"
		};

		boolean hasFoundLog4j = false;
		for(String location : locations) {
			File log4jFile = new File(location);

			try {
				InputStream inputStream = new FileInputStream(log4jFile);
				ConfigurationSource source = new ConfigurationSource(inputStream);
				Configurator.initialize(null, source);
				System.out.println("[LOGGING SETUP] Found log4j2 configuration file '" + log4jFile.getAbsolutePath() + "'.");
				hasFoundLog4j = true;
				break;
			} catch (Exception ex) {
				System.out.println("[LOGGING SETUP] Could not find the file located '" + log4jFile.getAbsolutePath() + "'.");
				System.out.println("[LOGGING SETUP] message was: " + ex.getMessage());
			}
		}

		if(!hasFoundLog4j) {
			System.out.println("[LOGGING SETUP] Could not find a log4j2 configuration file.  See messages above...");
			System.out.println("[LOGGING SETUP] If available, log4j2 will use the configuration file from the classpath.");
		}

		System.out.println("\n");
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static final String CMD_OPTION_OVERWRITE = "-overwrite";
	public static final String CMD_OPTION_PORT = "-port";
	public static final String CMD_OPTION_PROPERTIES = "-properties";
	public static final String CMD_OPTION_SCHEMA = "-schema";
	public static final String CMD_OPTION_NO_PROMPT = "--no-prompt";

	public static final String CMD_VALUE_SERVER = "server";
	public static final String CMD_VALUE_GENERATE = "generate";
	public static final String CMD_VALUE_HELP = "help";

	private static final Set<String> ALLOWABLE_COMMANDS = new HashSet<>();

	static {
		ALLOWABLE_COMMANDS.add(CMD_VALUE_SERVER);
		ALLOWABLE_COMMANDS.add(CMD_VALUE_GENERATE);
		ALLOWABLE_COMMANDS.add(CMD_VALUE_HELP);
	}

	public static String panlVersion = UNKNOWN_SHRUG;
	public static String solrVersion = UNKNOWN_SHRUG;

	public static final String DEFAULT_PANL_PROPERTIES = "panl.properties";
	public static final String DEFAULT_PORT_NUMBER = "8181";

	private final Map<String, String> OPTIONS_MAP = new HashMap<>();

	private int portNumber = 8181;
	private String propertiesFileLocation = DEFAULT_PANL_PROPERTIES;
	private boolean shouldOverwrite = false;
	private boolean noPrompt = false;

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
				if(optionKey.startsWith("--")) {
					// there is no second argument for double hyphens
					OPTIONS_MAP.put(optionKey, "");
				} else {
					String optionValue = args[i + 1];
					OPTIONS_MAP.put(optionKey, optionValue);
					i++;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				usageAndException(String.format("Found a command line option of '%s' without a value", optionKey));
			}
		}

		// now parse the rest of the commands
		switch (command) {
			case CMD_VALUE_SERVER:
				try {
					parseAndExecuteServerCommands();
				} catch(PanlServerException pex) {
					usageAndException(pex.getMessage());
				}
				break;
			case CMD_VALUE_GENERATE:
				parseAndExecuteGenerateCommands();
				break;
			case CMD_VALUE_HELP:
				outputUsageText("/usage.txt");
				outputUsageText("/usage-server.txt");
				outputUsageText("/usage-generate.txt");
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

		LOGGER.info("      Starting Panl server with properties:");
		LOGGER.info("             -properties {}", this.propertiesFileLocation);
		LOGGER.info("                   -port {}", this.portNumber);
		LOGGER.info("");
		LOGGER.info("                  ~ ~ ~ * ~ ~ ~");
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
		this.noPrompt = OPTIONS_MAP.containsKey(CMD_OPTION_NO_PROMPT);

		if (shouldOverwriteString.compareTo("true") == 0) {
			this.shouldOverwrite = true;
		}

		if (null == schemaFileLocations) {
			usageAndException("Mandatory command line option of '-schema' missing.");
		}

		LOGGER.info("     Starting Panl generation with properties:");
		LOGGER.info("             -properties {}", this.propertiesFileLocation);
		LOGGER.info("                 -schema {}", schemaFileLocations);
		LOGGER.info("              -overwrite {}", this.shouldOverwrite);
		LOGGER.info("             --no-prompt {}", this.noPrompt);
		LOGGER.info("");
		LOGGER.info("                  ~ ~ ~ * ~ ~ ~");
		LOGGER.info("");

		PanlGenerator panlGenerator = new PanlGenerator(
				this.propertiesFileLocation,
				schemaFileLocations,
				this.shouldOverwrite,
				this.noPrompt);

		panlGenerator.generate();
	}

	/**
	 * <p>Print a simple usage message depending on what command the Panl server
	 * thinks what is meant, and then throw an exception.</p>
	 *
	 * @param message The message to prepend to the usage instructions
	 *
	 * @throws CommandLineOptionException Always throws this exception
	 */
	private void usageAndException(String message) throws CommandLineOptionException {
		printIndentedError(message);

		if (args.length == 0) {
			System.out.println("[ERROR]: No arguments provided.");
			outputUsageText("/usage.txt");
			outputUsageText("/usage-server.txt");
			outputUsageText("/usage-generate.txt");
		} else {
			boolean foundArgument = false;
			boolean foundServer = false;
			boolean foundGenerate = false;
			System.out.println("[ERROR]:   Command line argument" + (args.length > 1 ? "s were" : " was") + ":");
			for (String arg : args) {
				System.out.printf("[ERROR]:      %s\n", arg);
				if(arg.equalsIgnoreCase("server")) {
					foundServer = true;
					foundArgument = true;
				} else if (arg.equalsIgnoreCase("generate")) {
					foundGenerate = true;
					foundArgument = true;
				}
			}
			
			if(!foundArgument) {
				System.out.println("[ERROR]: Couldn't determine what you were trying to do...");
			}

			System.out.println("[ERROR]:");
			System.out.println("[ERROR]: +-------------------------------------------------------------------+");
			System.out.println("[ERROR]:");

			if(foundServer || !foundArgument) {
				if(foundServer) {
					printErrorLines("Looks like you were trying to start the server, consequently printing out the Panl server " +
							"usage instructions. (If 'server' was not what you meant to do, try the 'help' command line argument " +
							"for a complete list of options.)");
				}
				outputUsageText("/usage-server.txt");
			}

			if(foundGenerate || !foundArgument) {
				if(foundGenerate) {
					printErrorLines("Looks like you were trying to generate configuration, consequently printing out the " +
							"Panl generation usage  instructions. (If 'generate' was not what you meant to do, try the 'help' " +
							"command line argument for a complete list of options.)");
				}

				outputUsageText("/usage-generate.txt");
			}

		}

		System.out.println("[ERROR]:");
		System.out.println("[ERROR]: +====================================================================+");
		System.out.println("[ERROR]:");

		throw new CommandLineOptionException("Invalid command line options.");
	}

	/**
	 * <p>Print out the error lines, prefixed with <code>[ERROR]: </code> prefix,
	 * splitting the error message between words so that the complete line will
	 * not go over 80 characters.</p>
	 *
	 * @param errorMessage The message to print to standard out
	 */
	private void printErrorLines(String errorMessage) {
		final String PREFIX = "[ERROR]: ";
		final int MAX_WIDTH = 79 - PREFIX.length();   // max characters of actual text per line (less the prefix)

		if (errorMessage == null || errorMessage.isEmpty()) {
			System.out.println(PREFIX + "(empty message)");
			return;
		}

		String[] words = errorMessage.split("\\s+");
		StringBuilder line = new StringBuilder();

		System.out.print(PREFIX);

		for (String word : words) {

			// If adding this word would exceed line width
			if (line.length() + word.length() > MAX_WIDTH) {
				// Print current line
				System.out.println(line.toString());

				// Start new line
				System.out.print(PREFIX);
				line.setLength(0); // clear builder
			}

			// Add word to line with space if needed
			if (line.length() > 0) {
				line.append(" ");
			}
			line.append(word);
		}

		// Print any remaining text
		if (line.length() > 0) {
			System.out.println(line.toString());
		}
		System.out.println("[ERROR]:");
	}

	/**
	 * <p>Print out an error message which is indented by two spaces (and a new
	 * line) for every colon ':' character found.</p>
	 *
	 * @param errorMessage The error message to print out.
	 */
	private void printIndentedError(String errorMessage) {
		System.out.println("[ERROR]: +===================================================================+");
		System.out.println("[ERROR]:");
		if (errorMessage == null || errorMessage.isEmpty()) {
			System.out.println("[ERROR]: No errorMessage message provided.");
		} else {

			// Split the errorMessage message on ':'
			String[] parts = errorMessage.split(":");

			// Print each part with increasing indentation
			for (int i = 0; i < parts.length; i++) {
				String indent = " ".repeat(i * 4);
				System.out.println("[ERROR]:   " + indent + parts[i].trim());
			}
		}
		System.out.println("[ERROR]:");
		System.out.println("[ERROR]: +-------------------------------------------------------------------+");
		System.out.println("[ERROR]:");
	}

	/**
	 * <p>Output the specific usage text to the standard out.  This will load the
	 * passed in location from the classpath and print out the file line by line,
	 * indented with two spaces.</p>
	 *
	 * <p>If the text file cannot be loaded from the classpath, then it will print
	 * out a <code>FATAL</code> error message and continue.</p>
	 *
	 * @param textLocation The location in the classpath of the file to print out.
	 */
	private void outputUsageText(String textLocation) {
		// now print the usage
		try (InputStream inputStream = Main.class.getResourceAsStream(textLocation)) {
			if (null == inputStream) {
				System.out.println("[FATAL]: Could not read the " + textLocation + " file.");
			} else {
				System.out.println(
						new BufferedReader(
								new InputStreamReader(inputStream))
								.lines()
								.collect(Collectors.joining("\n  ")));
				System.out.println();
			}
		} catch (IOException ignored) {
		}
	}

	/**
	 * <p>Main starting point for the application, parsing the command line
	 * options and executing the required component.  If there was an error when
	 * parsing the options then it will print out an error message and exit.</p>
	 *
	 * @param args The command line arguments to parse
	 */
	public static void main(String[] args) {
		Main main = new Main(args);

		Properties gradleProperties = new Properties();
		try {
			gradleProperties.load(Main.class.getResourceAsStream("/gradle.properties"));
			Main.panlVersion = gradleProperties.getProperty("panl.version", panlVersion);
			Main.solrVersion = gradleProperties.getProperty("panl.solr.version", solrVersion);
		} catch (IOException ignored) {
		}

		LOGGER.info("                  ~ ~ ~ * ~ ~ ~");
		LOGGER.info("");
		LOGGER.info("                                 __ ");
		LOGGER.info("             .-----.---.-.-----.|  |");
		LOGGER.info("             |  _  |  _  |     ||  |");
		LOGGER.info("             |   __|___._|__|__||__|");
		LOGGER.info("             |__|     ... .-..      ");
		LOGGER.info("");
		LOGGER.info("                  ~ ~ ~ * ~ ~ ~");
		LOGGER.info("");

		LOGGER.info("               Panl version: {}", Main.panlVersion);
		LOGGER.info("");
		LOGGER.info("          Designed for integration with");
		LOGGER.info("                 Solr version: {}", Main.solrVersion);
		LOGGER.info("");
		LOGGER.info("                  ~ ~ ~ * ~ ~ ~");
		LOGGER.info("");

		try {
			main.parseAndExecuteCommandLine();
		} catch (PanlServerException | CommandLineOptionException | PanlGenerateException e) {
			LOGGER.error("FAILURE to start, message was: {}", e.getMessage());
		}
	}
}
