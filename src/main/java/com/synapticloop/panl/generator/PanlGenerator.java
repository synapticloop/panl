package com.synapticloop.panl.generator;

import com.synapticloop.panl.exception.PanlGenerateException;
import com.synapticloop.panl.generator.bean.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class PanlGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlGenerator.class);
	private final String propertiesFileLocation;
	private final String schemaFileLocations;
	private final List<File> schemasToParse = new ArrayList<>();
	private final List<Collection> collections = new ArrayList<>();

	public PanlGenerator(
			String propertiesFileLocation,
			String schemaFileLocations,
			boolean shouldOverwrite) throws PanlGenerateException {
		this.propertiesFileLocation = propertiesFileLocation;
		this.schemaFileLocations = schemaFileLocations;

		if (!shouldOverwrite) {
			checkPropertiesFileLocation();
		}

		checkSchemaFileLocations();
	}

	private void checkSchemaFileLocations() throws PanlGenerateException {
		for (String schemaFileLocation : this.schemaFileLocations.split(",")) {
			File schemaFile = new File(schemaFileLocation);
			if (!schemaFile.exists() & !schemaFile.canRead()) {
				throw new PanlGenerateException("Could not find or read the '" +
						schemaFile.getAbsolutePath() +
						"' file, exiting...");
			} else {
				schemasToParse.add(schemaFile);
			}
		}
	}

	private void checkPropertiesFileLocation() throws PanlGenerateException {
		if (new File(propertiesFileLocation).exists()) {
			throw new PanlGenerateException("Properties file '" +
					this.propertiesFileLocation +
					"' exists, and we are not overwriting.  " +
					"Use the '-overwrite true' command line option to overwrite this file.");
		}
	}

	private Map<String, String> panlParamMap = new HashMap<>();

	public void generate() throws PanlGenerateException {
		// we need to parse each of the schema files before writing out the top
		// level panl.properties file

		// now ask the questions:

		//		$panl.param.query
		String panlParamQuery = getParameterInput("The search query parameter", "panl.param.query", "q", null);
		panlParamMap.put(panlParamQuery, "panl.param.query");

		//		$panl.param.sort
		String panlParamSort = getParameterInput("The results sorting parameter", "panl.param.sort", "s", null);
		panlParamMap.put(panlParamSort, "panl.param.sort");

		//		$panl.param.page
		String panlParamPage = getParameterInput("The page number", "panl.param.page", "p", null);
		panlParamMap.put(panlParamPage, "panl.param.page");

		//		$panl.param.numrows
		String panlParamNumRows = getParameterInput("The number of results to return per page", "panl.param.numrows", "n", null);
		panlParamMap.put(panlParamNumRows, "panl.param.numrows");

		//		$panl.param.operand
		String panlParamOperand = getParameterInput("The default query operand (q.op)", "panl.param.operand", "o", null);
		panlParamMap.put(panlParamOperand, "panl.param.operand");

		//		$panl.param.passthrough
		String panlParamPassthrough = getParameterInput("The URI path passthrough", "panl.param.passthrough", "z", null);
		panlParamMap.put(panlParamPassthrough, "panl.param.passthrough");


		for (File schema : schemasToParse) {
			collections.add(new Collection(schema));
		}

		// now we have all collections parsed
		// time to go through them and generate the panl.properties file

		generatePanlDotProperties();
		for (Collection collection : collections) {
			generateCollectionDotPanlDotProperties(collection);
		}

	}

	private String getParameterInput(String description, String paramName, String defaultValue, String errorPrompt) {
		if (null != errorPrompt) {
			System.out.printf("Invalid value. %s, please try again.\n", errorPrompt);
		}
		System.out.printf("Enter the 1 character property value for '%s' (%s), default [%s]: ", paramName, description, defaultValue);
		Scanner in = new Scanner(System.in);
		String temp = in.nextLine();
		if (temp.isBlank()) {
			System.out.printf("Property '%s' set to default value of '%s'\n", paramName, defaultValue);
			return (defaultValue);
		}

		if (temp.length() != 1) {
			return(getParameterInput(paramName, description, defaultValue, "Value must be exactly 1 character."));
		} else {
			if(panlParamMap.containsKey(temp)) {
				return(getParameterInput(
						paramName,
						description,
						defaultValue,
						String.format("Value '%s' already assigned to property '%s'.", temp, panlParamMap.get(temp))));

			}
			System.out.printf("Property '%s' set to value '%s'.\n", paramName, temp);
			return (temp);
		}
	}

	private void generateCollectionDotPanlDotProperties(Collection collection) {
		StringBuilder outputString = new StringBuilder();

		try (
				InputStream inputStream = PanlGenerator.class.getResourceAsStream("/collection.panl.template.properties");
				InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
				BufferedReader reader = new BufferedReader(streamReader);
				OutputStream outputStream = Files.newOutputStream(new File(collection.getCollectionName() + ".panl.properties").toPath());
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
				BufferedWriter writer = new BufferedWriter(outputStreamWriter)) {

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("$")) {
					outputString.append(collection.getPanlProperty(line))
							.append("\n");
				} else {
					outputString.append(line)
							.append("\n");
				}
			}

			LOGGER.info("Writing out file {}.panl.properties", collection.getCollectionName());
			writer.write(outputString.toString());
			writer.flush();
			LOGGER.info("Done writing out file {}.panl.properties", collection.getCollectionName());

		} catch (IOException e) {
			LOGGER.error("IOException with writing <collection>.panl.properties file", e);
		}
	}

	private void generatePanlDotProperties() {
		StringBuilder outputString = new StringBuilder();

		StringBuilder collectionPropertyFiles = new StringBuilder();
		for (Collection collection : collections) {
			String niceCollectionName = collection.getCollectionName().toLowerCase().replaceAll("[^a-z0-9]", "-");
			collectionPropertyFiles.append("panl.collection.")
					.append(niceCollectionName)
					.append("=")
					.append(niceCollectionName)
					.append(".panl.properties\n");
		}

		try (
				InputStream inputStream = PanlGenerator.class.getResourceAsStream("/panl.template.properties");
				InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
				BufferedReader reader = new BufferedReader(streamReader);
				OutputStream outputStream = Files.newOutputStream(new File("panl.properties").toPath());
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
				BufferedWriter writer = new BufferedWriter(outputStreamWriter)) {

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("$panl.collections")) {
					outputString.append(collectionPropertyFiles)
							.append("\n");
				} else {
					outputString.append(line)
							.append("\n");

				}
			}

			LOGGER.info("Writing out file panl.properties");
			writer.write(outputString.toString());
			writer.flush();
			LOGGER.info("Done writing out file panl.properties");

		} catch (IOException e) {
			LOGGER.error("IOException with writing panl.properties file", e);
		}
	}
}
