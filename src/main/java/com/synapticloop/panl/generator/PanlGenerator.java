package com.synapticloop.panl.generator;

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

import com.synapticloop.panl.exception.PanlGenerateException;
import com.synapticloop.panl.generator.bean.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * <p>This is the generator for both the panl.properties configuration file
 * and the collection.panl.properties file. It will prompt for </p>
 *
 * @author synapticloop
 */
public class PanlGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlGenerator.class);

	private static final String TEMPLATE_LOCATION_COLLECTION_PANL_PROPERTIES = "/panl_collection_uri.panl.properties.template";
	private static final String TEMPLATE_LOCATION_PANL_PROPERTIES = "/panl.properties.template";

	private static final String PANL_PARAM_QUERY = "panl.param.query";
	private static final String PANL_PARAM_SORT = "panl.param.sort";
	private static final String PANL_PARAM_PAGE = "panl.param.page";
	private static final String PANL_PARAM_NUMROWS = "panl.param.numrows";
	private static final String PANL_PARAM_QUERY_OPERAND = "panl.param.query.operand";
	public static final String PANL_PARAM_PASSTHROUGH = "panl.param.passthrough";

	private final String propertiesFileLocation;
	private final String schemaFileLocations;

	/**
	 * <p>The list of schemas to parse and add to the panl.properties file which
	 * will be output to the '$panl.collections' property.</p>
	 */
	private final List<File> schemasToParse = new ArrayList<>();
	/**
	 * <p>The list of collections (parsed from the Solr schemas) to convert.</p>
	 */
	private final List<Collection> collections = new ArrayList<>();
	/**
	 * <p>A map of Panl params, keyed on param code:param property.</p>
	 */
	private final Map<String, String> panlParamMap = new HashMap<>();
	/**
	 * <p>A map of Panl params, keyed on param property:param code.</p>
	 */
	private final Map<String, String> panlReplacementPropertyMap = new LinkedHashMap<>();

	/**
	 * <p>Instantiate the Panl generator.</p>
	 *
	 * @param propertiesFileLocation The location of the output for the
	 *         properties file
	 * @param schemaFileLocations The comma separated list of Solr schema file
	 *         locations
	 * @param shouldOverwrite If true, this will overwrite the panl.properties
	 *         file and the collection.panl.properties file
	 * @throws PanlGenerateException If there was a problem finding the files
	 *         to parse, generating the files
	 */
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

	/**
	 * <p>Prompt for the default required parameters, and then generate the
	 * <code>panl.properties</code> and <code>collection.panl.properties</code>
	 * files.</p>
	 *
	 * <p>The required parameters are:</p>
	 *
	 * <ul>
	 *   <li><code>panl.param.query</code> - The search query parameter</li>
	 *   <li><code>panl.param.sort</code> - The results sorting parameter</li>
	 *   <li><code>panl.param.page</code> - The page number</li>
	 *   <li><code>panl.param.numrows</code> - The number of results to return per page/li>
	 *   <li><code>panl.param.query.operand</code> - The default query operand (q.op)</li>
	 *   <li><code>panl.param.passthrough</code> - The URI path passthrough</li>
	 * </ul>
	 *
	 * @throws PanlGenerateException If there was an error generating the
	 *         properties files.
	 */
	public void generate() throws PanlGenerateException {
		// we need to parse each of the schema files before writing out the top
		// level panl.properties file

		// now ask the questions:

		//		$panl.param.query
		getParameterInput("The search query parameter", PANL_PARAM_QUERY, "q", null);

		//		$panl.param.page
		getParameterInput("The page number", PANL_PARAM_PAGE, "p", null);

		//		$panl.param.numrows
		getParameterInput("The number of results to return per page", PANL_PARAM_NUMROWS, "n", null);

		//		$panl.param.sort
		getParameterInput("The results sorting parameter", PANL_PARAM_SORT, "s", null);

		//		$panl.param.query.operand
		getParameterInput("The default query operand (q.op)", PANL_PARAM_QUERY_OPERAND, "o", null);

		//		$panl.param.passthrough
		getParameterInput("The URI path passthrough", PANL_PARAM_PASSTHROUGH, "z", null);


		for (File schema : schemasToParse) {
			collections.add(new Collection(schema, panlReplacementPropertyMap));
		}

		// now we have all collections parsed
		// time to go through them and generate the panl.properties file

		generatePanlDotProperties();
		for (Collection collection : collections) {
			generateCollectionDotPanlDotProperties(collection);
		}

	}

	private String getParameterInput(String description, String panlParamProperty, String defaultValue, String errorPrompt) {
		if (null != errorPrompt) {
			System.out.printf("Invalid value. %s, please try again.\n", errorPrompt);
		}
		System.out.printf("Enter the 1 character property value for '%s' (%s), default [%s]: ", panlParamProperty, description, defaultValue);
		Scanner in = new Scanner(System.in);
		String temp = in.nextLine();
		if (temp.isBlank()) {
			System.out.printf("Property '%s' set to default value of '%s'\n", panlParamProperty, defaultValue);
			panlParamMap.put(defaultValue, panlParamProperty);
			panlReplacementPropertyMap.put("$" + panlParamProperty, defaultValue);
			return (defaultValue);
		}

		if (temp.length() != 1) {
			return (getParameterInput(panlParamProperty, description, defaultValue, "Value must be exactly 1 character."));
		} else {
			// the value must be one of the available codes
			if(!Collection.CODES.contains(temp)) {
				return (getParameterInput(
								panlParamProperty,
								description,
								defaultValue,
								String.format("Value '%s' __MUST__ be one of '%s'.", temp, Collection.CODES)));
			}
			// It cannot be already in use
			if (panlParamMap.containsKey(temp)) {
				return (getParameterInput(
								panlParamProperty,
								description,
								defaultValue,
								String.format("Value '%s' already assigned to property '%s'.", temp, panlParamMap.get(temp))));

			}
			System.out.printf("Property '%s' set to value '%s'.\n", panlParamProperty, temp);
			panlParamMap.put(temp, panlParamProperty);
			panlReplacementPropertyMap.put("$" + panlParamProperty, temp);
			return (temp);
		}
	}

	private void generateCollectionDotPanlDotProperties(Collection collection) {
		StringBuilder outputString = new StringBuilder();

		try (InputStream inputStream = PanlGenerator.class.getResourceAsStream(TEMPLATE_LOCATION_COLLECTION_PANL_PROPERTIES)) {
			assert inputStream != null;
			try (InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
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

			}
		} catch (IOException e) {
			LOGGER.error("IOException with writing <collection>.panl.properties file", e);
		}
	}

	/**
	 * <p>Generate the panl.properties file.</p>
	 */
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

		try (InputStream inputStream = PanlGenerator.class.getResourceAsStream(TEMPLATE_LOCATION_PANL_PROPERTIES)) {
			assert inputStream != null;

			try (InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
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

			}
		} catch (IOException e) {
			LOGGER.error("IOException with writing panl.properties file", e);
		}
	}
}
