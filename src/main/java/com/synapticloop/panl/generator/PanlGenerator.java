package com.synapticloop.panl.generator;

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

import com.synapticloop.panl.exception.PanlGenerateException;
import com.synapticloop.panl.generator.bean.PanlCollection;
import com.synapticloop.panl.generator.bean.field.BasePanlField;
import com.synapticloop.panl.generator.util.PropertiesMerger;
import com.synapticloop.panl.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;

import static com.synapticloop.panl.util.Constants.Property.Panl.*;

/**
 * <p>This is the generator for both the panl.properties configuration file
 * and the collection.panl.properties file. It will prompt for the various parameters that need to be set (including the
 * one optional one).</p>
 *
 * @author synapticloop
 */
public class PanlGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlGenerator.class);

	public static final String TEMPLATE_LOCATION_COLLECTION_PANL_PROPERTIES = "/panl_collection_url.panl.properties.template";
	public static final String TEMPLATE_LOCATION_PANL_PROPERTIES = "/panl.properties.template";

	private final String schemaFileLocation;
	private final String propertiesFileLocation;
	private final String collectionPropertiesOutputDirectory;
	private final boolean noPrompt;

	/**
	 * <p>The list of schemas to parse and add to the panl.properties file which
	 * will be output to the '$panl.panlCollections' property.</p>
	 */
	private File schemaToParse;

	/**
	 * <p>The list of panlCollections (parsed from the Solr schemas) to convert.</p>
	 */
	private final List<PanlCollection> panlCollections = new ArrayList<>();

	/**
	 * <p>A map of Panl params, keyed on param code:param property.</p>
	 */
	private final Map<String, String> panlParamMap = new HashMap<>();

	/**
	 * <p>A map of Panl params, keyed on param property:param code.</p>
	 */
	private final Map<String, String> panlReplacementPropertyMap = new LinkedHashMap<>();

	/**
	 * <p>A map of currently registered solr fields names and their Panl LPSE codes
	 * (if a previous file was found)  key:value is solr_field_name:base_panl_field</p>
	 */
	private final Map<String, BasePanlField> existingSolrMappings = new HashMap<>();
	/**
	 * <p>Instantiate the Panl generator.</p>
	 *
	 * @param propertiesFileLocation The location of the output for the properties
	 * 		file
	 * @param schemaFileLocation The comma separated list of Solr schema file
	 * 		locations
	 * @param shouldOverwrite If true, this will overwrite the panl.properties
	 * 		file and the collection.panl.properties 	file
	 * @param noPrompt
	 *
	 * @throws PanlGenerateException If there was a problem finding the files to
	 * 		parse, generating the files
	 */
	public PanlGenerator(
			String propertiesFileLocation,
			String schemaFileLocation,
			boolean shouldOverwrite,
			boolean noPrompt) throws PanlGenerateException {

		this.propertiesFileLocation = propertiesFileLocation;
		this.schemaFileLocation = schemaFileLocation;
		this.noPrompt = noPrompt;


		// load up the defaults
		panlReplacementPropertyMap.put(SOLRJ_CLIENT, "CloudSolrClient");
		panlReplacementPropertyMap.put(SOLR_SEARCH_SERVER_URL, "http://localhost:8983/solr,http://localhost:7574/solr");
		panlReplacementPropertyMap.put(PANL_RESULTS_TESTING_URLS, "true");
		panlReplacementPropertyMap.put(PANL_STATUS_404_VERBOSE, "true");
		panlReplacementPropertyMap.put(PANL_STATUS_500_VERBOSE, "true");
		panlReplacementPropertyMap.put(PANL_DECIMAL_POINT, "true");
		panlReplacementPropertyMap.put(PANL_PARAM_PASSTHROUGH_CANONICAL, "false");
		panlReplacementPropertyMap.put(PANL_REMOVE_SOLR_JSON_KEYS, "false");
		panlReplacementPropertyMap.put(PANL_SERVER_EXTRA, "{}");


		File propertiesFile = new File(propertiesFileLocation);
		if (propertiesFile.exists()) {
			if(!shouldOverwrite) {
				throw new PanlGenerateException(
						"Properties file '" +
								this.propertiesFileLocation +
								"' exists, and we are not overwriting.  " +
								"Use the '-overwrite true' command line option to overwrite this file.");
			}
		}

		// load up the properties file - override any default properties
		Properties properties = new Properties();
		try {
			properties.load(new BufferedReader(new FileReader(propertiesFile)));
		} catch(IOException ignored) {
		}


		// now override the properties that have been already found
		overrideDefaultProperty(properties, SOLRJ_CLIENT, "CloudSolrClient");
		overrideDefaultProperty(properties, SOLR_SEARCH_SERVER_URL, "http://localhost:8983/solr,http://localhost:7574/solr");
		overrideDefaultProperty(properties, PANL_RESULTS_TESTING_URLS, "true");
		overrideDefaultProperty(properties, PANL_STATUS_404_VERBOSE, "true");
		overrideDefaultProperty(properties, PANL_STATUS_500_VERBOSE, "true");
		overrideDefaultProperty(properties, PANL_DECIMAL_POINT, "true");
		overrideDefaultProperty(properties, PANL_PARAM_PASSTHROUGH_CANONICAL, "false");
		overrideDefaultProperty(properties, PANL_REMOVE_SOLR_JSON_KEYS, "false");
		overrideDefaultProperty(properties, PANL_SERVER_EXTRA, "{}");

		File file = new File(propertiesFileLocation);
		this.collectionPropertiesOutputDirectory = file.getParentFile().getAbsolutePath();
		checkSchemaFileLocations();
	}

	private void overrideDefaultProperty(Properties properties, String key, String defaultValue) {
		String property = properties.getProperty(key, null);
		if(null != property) {
			LOGGER.info(
					"Found an existing property for key '" +
							key +
							"' with value '" +
							property +
							"', overriding" +
							" default of '" +
							defaultValue +
							"'."
					);
			panlReplacementPropertyMap.put(key, properties.getProperty(key, defaultValue));
		} else {
			LOGGER.info("Could not find default property for key '" + key + "', using '" + defaultValue +"'.");
		}
	}
	/**
	 * <p>Check the location of the schema file.</p>
	 *
	 * @throws PanlGenerateException If the schema file does not exist, or cannot be read
	 */
	private void checkSchemaFileLocations() throws PanlGenerateException {
		File schemaFile = new File(schemaFileLocation);
		if (!schemaFile.exists() & !schemaFile.canRead()) {
			throw new PanlGenerateException(
				"Could not find or read the '" +
					schemaFile.getAbsolutePath() +
					"' file, exiting...");
		} else {
			schemaToParse = schemaFile;
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
	 *   <li><code>panl.param.numrows</code> - The number of results to return per page</li>
	 *   <li><code>panl.param.query.operand</code> - The default query operand (q.op)</li>
	 *   <li><code>panl.param.passthrough</code> - The URI path passthrough -
	 *   whilst this is an optional parameter, it is included and can then be
	 *   removed later on from the generated properties file.</li>
	 * </ul>
	 *
	 * @throws PanlGenerateException If there was an error generating the properties files.
	 */
	public void generate() throws PanlGenerateException {
		// we need to parse each of the schema files before writing out the top
		// level panl.properties file

		// now ask the questions:

		//		$panl.param.query
		getAndValidateParameterInput("The search query parameter", PANL_PARAM_QUERY, "q", null);

		//		$panl.param.page
		getAndValidateParameterInput("The page number", PANL_PARAM_PAGE, "p", null);

		//		$panl.param.numrows
		getAndValidateParameterInput("The number of results to return per page", PANL_PARAM_NUMROWS, "n", null);

		//		$panl.param.sort
		getAndValidateParameterInput("The results sorting parameter", PANL_PARAM_SORT, "s", null);

		//		$panl.param.query.operand
		getAndValidateParameterInput("The default query operand (q.op)", PANL_PARAM_QUERY_OPERAND, "o", null);

		//		$panl.param.passthrough
		getAndValidateParameterInput("The URI path passthrough", PANL_PARAM_PASSTHROUGH, "z", null);


		panlCollections.add(new PanlCollection(schemaToParse, panlReplacementPropertyMap, this.collectionPropertiesOutputDirectory));

		// now we have all panlCollections parsed
		// time to go through them and generate the panl.properties file

		generatePanlDotProperties();

		for (PanlCollection panlCollection : panlCollections) {
			generateCollectionDotPanlDotProperties(panlCollection);
		}
	}

	/**
	 * <p>Get and validate that the input parameter is correct, it will do the following
	 * validations:</p>
	 *
	 * <ul>
	 *   <li>If the character is not __EXACTLY__ 1 character</li>
	 *   <li>It is an alphanumeric character (lower or uppercased)</li>
	 *   <li>That it has not been used before</li>
	 * </ul>
	 *
	 * <p>This will be recursively called until a correct parameter is input.</p>
	 *
	 * @param description The description to output to the prompt
	 * @param panlParamProperty The property that this will replace in the properties file
	 * @param defaultValue The default value - which will be set if an empty string is sent through
	 * @param errorPrompt The error prompt
	 *
	 * @return The inputted parameter
	 */
	private String getAndValidateParameterInput(
			String description,
			String panlParamProperty,
			String defaultValue,
			String errorPrompt) {

		if(this.noPrompt) {
			LOGGER.info("Property '{}' set to default value of '{}'", panlParamProperty, defaultValue);
			panlParamMap.put(defaultValue, panlParamProperty);
			panlReplacementPropertyMap.put(panlParamProperty, defaultValue);
			return(defaultValue);
		}

		if (null != errorPrompt) {
			System.out.printf("Invalid value. %s Please try again.\n", errorPrompt);
		}

		System.out.printf(
				"Enter the 1 character property value for '%s' (%s), default [%s]: ",
				panlParamProperty,
				description,
				defaultValue);

		Scanner in = getSystemInput();
		String temp = in.nextLine();
		if (temp.isBlank()) {
			System.out.printf("Property '%s' set to default value of '%s'\n", panlParamProperty, defaultValue);
			panlParamMap.put(defaultValue, panlParamProperty);
			panlReplacementPropertyMap.put(panlParamProperty, defaultValue);
			return (defaultValue);
		}

		if (temp.length() != 1) {
			return (getAndValidateParameterInput(
				panlParamProperty,
				description,
				defaultValue,
				"Value must be exactly 1 character."));
		} else {
			// the value must be one of the available codes
			if (!PanlCollection.CODES.contains(temp)) {
				return (getAndValidateParameterInput(
					panlParamProperty,
					description,
					defaultValue,
					String.format("Value '%s' __MUST__ be one of '%s'.", temp, PanlCollection.CODES)));
			}

			// It cannot be already in use
			if (panlParamMap.containsKey(temp)) {
				return (getAndValidateParameterInput(
					panlParamProperty,
					description,
					defaultValue,
					String.format("Value '%s' already assigned to property '%s'.", temp, panlParamMap.get(temp))));

			}
			System.out.printf("Property '%s' set to value '%s'.\n", panlParamProperty, temp);
			panlParamMap.put(temp, panlParamProperty);
			panlReplacementPropertyMap.put(panlParamProperty, temp);
			return (temp);
		}
	}

	/**
	 * <p>Generate the panl.properties file.</p>
	 */
	private void generatePanlDotProperties() {

		StringBuilder collectionPropertyFiles = new StringBuilder();
		for (PanlCollection panlCollection : panlCollections) {
			String niceCollectionName = panlCollection.getCollectionName().toLowerCase().replaceAll("[^a-z0-9]", "-");
			collectionPropertyFiles
				.append(Constants.Property.Panl.PANL_COLLECTION)
				.append(niceCollectionName)
				.append("=")
				.append(niceCollectionName)
				.append(".panl.properties\n");
		}

		// determine the output directory for the panl.properties and the
		// associated directory
		try (OutputStream outputStream = Files.newOutputStream(new File(this.propertiesFileLocation).toPath());
		     OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
		     BufferedWriter writer = new BufferedWriter(outputStreamWriter)) {

			LOGGER.info("Writing out file panl.properties");

			panlReplacementPropertyMap.put("panl.collections", collectionPropertyFiles.toString());

			writer.write(
				PropertiesMerger.mergeProperties(
					TEMPLATE_LOCATION_PANL_PROPERTIES,
					panlReplacementPropertyMap,
					true));

			writer.flush();

			LOGGER.info("Done writing out file panl.properties");

		} catch (IOException e) {
			LOGGER.error("IOException with writing panl.properties file", e);
		}
	}

	/**
	 * <p>Generate the <code>&lt;panl_collection_url&gt;.panl.properties</code>
	 * file.</p>
	 *
	 * @param panlCollection The panl collection object to generate the file with
	 */
	private void generateCollectionDotPanlDotProperties(PanlCollection panlCollection) {
		try (
			OutputStream outputStream = Files.newOutputStream(new File(
				this.collectionPropertiesOutputDirectory +
					FileSystems
						.getDefault()
						.getSeparator() + panlCollection.getCollectionName() + ".panl.properties").toPath());

			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
			BufferedWriter writer = new BufferedWriter(outputStreamWriter)) {

			panlReplacementPropertyMap.put("panl.lpse.length", panlCollection.getLpseLength() + "");
			panlReplacementPropertyMap.put("panl.lpse.fields", panlCollection.getPanlProperty("panl.lpse.fields"));

			panlReplacementPropertyMap.put("panl.lpse.order", panlCollection.getPanlProperty("panl.lpse.order"));
			panlReplacementPropertyMap.put("panl.lpse.facetorder", panlCollection.getPanlProperty("panl.lpse.facetorder"));

			panlReplacementPropertyMap.put("panl.results.fields.default", panlCollection.getPanlProperty("panl.results.fields.default"));
			panlReplacementPropertyMap.put("panl.results.fields.firstfive", panlCollection.getPanlProperty("panl.results.fields.firstfive"));
			panlReplacementPropertyMap.put("panl.collections", panlCollection.getPanlProperty("panl.collections"));

			LOGGER.info("Writing out file {}.panl.properties", panlCollection.getCollectionName());

			writer.write(
				PropertiesMerger.mergeProperties(
					TEMPLATE_LOCATION_COLLECTION_PANL_PROPERTIES,
					panlReplacementPropertyMap,
					true));

			writer.flush();
			LOGGER.info("Done writing out file {}.panl.properties", panlCollection.getCollectionName());

		} catch (IOException e) {
			LOGGER.error("IOException with writing <panlCollection>.panl.properties file", e);
		}
	}

	public Scanner getSystemInput() {
		return (new Scanner(System.in));
	}
}
