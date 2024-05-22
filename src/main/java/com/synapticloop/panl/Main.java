package com.synapticloop.panl;

import com.synapticloop.panl.exception.CommandLineOptionException;
import com.synapticloop.panl.exception.PanlGenerateException;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.generator.PanlGenerator;
import com.synapticloop.panl.server.PanlServer;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is the main starting point for the panl server/generator
 */
public class Main {
	private static final Set<String> ALLOWABLE_COMMANDS = new HashSet<>();
	private static final Set<String> ALLOWABLE_OPTIONS = new HashSet<>();
	public static final String DEFAULT_PANL_PROPERTIES = "panl.properties";
	public static final String DEFAULT_PORT_NUMBER = "8181";

	static {
		ALLOWABLE_COMMANDS.add("server");
		ALLOWABLE_COMMANDS.add("generate");

		ALLOWABLE_OPTIONS.add("-properties");
		ALLOWABLE_OPTIONS.add("-port");
		ALLOWABLE_OPTIONS.add("-schema");
	}
	private final Map<String, String> OPTIONS_MAP = new HashMap<>();

	private int portNumber = 8181;
	private String propertiesFileLocation = DEFAULT_PANL_PROPERTIES;
	private String schemaFileLocations = null;
	private boolean shouldOverwrite = false;

	private final String[] args;

	public Main(String[] args) {
		this.args = args;
	}

	private void parseCommandLine() throws CommandLineOptionException, PanlGenerateException, PanlServerException {
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
			parseServerCommands();
		} else {
			parseGenerateCommands();
		}
	}

	private void parseServerCommands() throws CommandLineOptionException, PanlServerException {
		this.propertiesFileLocation = OPTIONS_MAP.getOrDefault("-properties", DEFAULT_PANL_PROPERTIES);
		String portNumberString = OPTIONS_MAP.getOrDefault("-port", DEFAULT_PORT_NUMBER);
		try {
			this.portNumber = Integer.parseInt(portNumberString);
		} catch(NumberFormatException e) {
			usage(String.format("Could not parse port number of '%s'", portNumberString));
		}

		System.out.println("Starting server with properties:");
		System.out.printf("  -properties %s\n", this.propertiesFileLocation);
		System.out.printf("        -port %s\n", this.portNumber);

		// at this point we are ready to go
		PanlServer panlServer = new PanlServer(this.propertiesFileLocation, this.portNumber);
		panlServer.start();
	}

	private void parseGenerateCommands() throws CommandLineOptionException, PanlGenerateException {
		this.propertiesFileLocation = OPTIONS_MAP.getOrDefault("-properties", DEFAULT_PANL_PROPERTIES);
		this.schemaFileLocations = OPTIONS_MAP.getOrDefault("-schema", null);
		String shouldOverwriteString = OPTIONS_MAP.getOrDefault("-overwrite", "false").toLowerCase();

		if(shouldOverwriteString.compareTo("true") == 0) {
			this.shouldOverwrite = true;
		}

		if(null == schemaFileLocations) {
			usage("Mandatory command line option of '-schema' missing.");
		}

		System.out.println("Starting generation with properties:");
		System.out.printf("  -properties %s\n", this.propertiesFileLocation);
		System.out.printf("      -schema %s\n", this.schemaFileLocations);
		System.out.printf("   -overwrite %s\n", this.shouldOverwrite);

		PanlGenerator panlGenerator = new PanlGenerator(
				this.propertiesFileLocation,
				this.schemaFileLocations,
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

	public static void main(String[] args) throws CommandLineOptionException, PanlGenerateException, PanlServerException {
		Main main = new Main(args);
		main.parseCommandLine();
	}

}
