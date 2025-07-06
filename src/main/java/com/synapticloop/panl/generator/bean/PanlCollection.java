package com.synapticloop.panl.generator.bean;

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
import com.synapticloop.panl.generator.PanlGenerator;
import com.synapticloop.panl.generator.bean.field.BasePanlField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

import static com.synapticloop.panl.util.Constants.Property.Panl.*;

/**
 * <p></p>
 *
 * @author synapticloop
 */
public class PanlCollection {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlCollection.class);

	private String collectionName;
	private final List<SolrField> solrFields = new ArrayList<>();
	private final List<String> resultFieldNames = new ArrayList<>();
	private final List<BasePanlField> basePanlFields = new ArrayList<>();
	private final Map<String, String> fieldXmlMap = new HashMap<>();
	private int lpseLength = 1;
	public static String CODES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234567890";

	private static final Set<String> CODES_AVAILABLE = new HashSet<>();
	private static final Map<String, String> PANL_PROPERTIES = new HashMap<>();
	private static final Map<String, String> SOLR_FIELD_TYPE_NAME_TO_SOLR_CLASS = new HashMap<>();
	private static final Map<String, String> SOLR_FIELD_NAME_TO_SOLR_FIELD_TYPE = new HashMap<>();

	private static final Set<String> SUPPORTED_SOLR_FIELD_TYPES = new HashSet<>();

	private static final String[] LPSE_ORDER_PARAMS = {
			PANL_PARAM_QUERY,
			PANL_PARAM_PAGE,
			PANL_PARAM_NUMROWS,
			PANL_PARAM_SORT,
			PANL_PARAM_QUERY_OPERAND
	};

	static {
		SUPPORTED_SOLR_FIELD_TYPES.add("solr.BoolField");
		SUPPORTED_SOLR_FIELD_TYPES.add("solr.StrField");
		SUPPORTED_SOLR_FIELD_TYPES.add("solr.TextField");
		SUPPORTED_SOLR_FIELD_TYPES.add("solr.UUIDField");
		SUPPORTED_SOLR_FIELD_TYPES.add("solr.IntPointField");
		SUPPORTED_SOLR_FIELD_TYPES.add("solr.DatePointField");
		SUPPORTED_SOLR_FIELD_TYPES.add("solr.FloatPointField");
		SUPPORTED_SOLR_FIELD_TYPES.add("solr.LongPointField");
		SUPPORTED_SOLR_FIELD_TYPES.add("solr.DoublePointField");
	}

	public PanlCollection(File schema, Map<String, String> panlReplacementPropertyMap) throws PanlGenerateException {
		parseSchemaFile(schema);

		int numSupported = 0;
		// now that we have parsed the Solr fields, go through and mark the fields
		// as either supported or not supported
		for (SolrField solrField : solrFields) {
			String fieldName = solrField.getName();
			String solrClass = SOLR_FIELD_TYPE_NAME_TO_SOLR_CLASS.get(SOLR_FIELD_NAME_TO_SOLR_FIELD_TYPE.get(fieldName));
			if (SUPPORTED_SOLR_FIELD_TYPES.contains(solrClass)) {
				solrField.setIsSupported(true);
				numSupported++;
			} else {
				LOGGER.warn("Unsupported Solr field for Panl '{}' of type '{}', ignoring.", fieldName, solrClass);
			}
		}


		// each character can be one of the letters and numbers
		this.lpseLength = numSupported / 62;

		// don't forget that we have pre-defined 'params'
		if ((numSupported % 62 - panlReplacementPropertyMap.size()) > 0) {
			this.lpseLength++;
		}

		if (this.lpseLength == 0) {
			this.lpseLength = 1;
		}

		LOGGER.info("PanlCollection: {}", this.collectionName);
		LOGGER.info("Have {} panlFields, LPSE length is set to {}", numSupported, this.lpseLength);

		// now we are going to remove all codes that are in use by the panl replacement map
		for (String code : panlReplacementPropertyMap.values()) {
			CODES = CODES.replace(code, "");
		}

		generateAvailableCodesForFields();

		PANL_PROPERTIES.put("panl.lpse.length", "" + this.lpseLength);

		for (String property : panlReplacementPropertyMap.keySet()) {
			PANL_PROPERTIES.put(property, panlReplacementPropertyMap.get(property));
		}

		// now go through to panlFields and assign a code which is close to what they want...
		List<SolrField> unassignedSolrFields = new ArrayList<>();
		for (SolrField solrField : solrFields) {
			if (!solrField.getIsSupported()) {
				continue;
			}

			String fieldName = solrField.getName();
			String cleanedName = fieldName.replaceAll("[^A-Za-z0-9]", "");
			String possibleCode = cleanedName.substring(0, this.lpseLength);
			if (CODES_AVAILABLE.contains(possibleCode)) {

				basePanlFields.add(BasePanlField.getPanlField(
						possibleCode,
						fieldName,
						SOLR_FIELD_TYPE_NAME_TO_SOLR_CLASS.get(SOLR_FIELD_NAME_TO_SOLR_FIELD_TYPE.get(fieldName)),
						fieldXmlMap.get(fieldName),
						solrField.getIsFacetable(),
						solrField.getIsMultiValued()));

				LOGGER.info("Assigned field '{}' to panl code '{}'", fieldName, possibleCode);

				CODES_AVAILABLE.remove(possibleCode);
			} else if (CODES_AVAILABLE.contains(possibleCode.toUpperCase())) {
				String nextPossibleCode = possibleCode.toUpperCase();
				basePanlFields.add(BasePanlField.getPanlField(
						nextPossibleCode,
						fieldName,
						SOLR_FIELD_TYPE_NAME_TO_SOLR_CLASS.get(SOLR_FIELD_NAME_TO_SOLR_FIELD_TYPE.get(fieldName)),
						fieldXmlMap.get(fieldName),
						solrField.getIsFacetable(),
						solrField.getIsMultiValued()));
				LOGGER.info("Assigned field '{}' to panl code '{}'", fieldName, nextPossibleCode);
				CODES_AVAILABLE.remove(nextPossibleCode);
			} else {
				LOGGER.warn("No nice panl code for field '{}', '{}' and '{}' already taken", fieldName, possibleCode,
						possibleCode.toUpperCase());
				unassignedSolrFields.add(solrField);
			}
		}

		// at this point, we are going to go through all unassigned field names and
		// try and determine what we should mark them as
		for (SolrField solrField : unassignedSolrFields) {
			int size = CODES_AVAILABLE.size();
			int item = new Random().nextInt(size); // In real life, the Random object should be rather more shared than this
			int i = 0;
			String assignedCode = null;
			for (String code : CODES_AVAILABLE) {
				if (i == item) {
					assignedCode = code;

					String fieldName = solrField.getName();
					basePanlFields.add(BasePanlField.getPanlField(
							assignedCode,
							fieldName,
							SOLR_FIELD_TYPE_NAME_TO_SOLR_CLASS.get(SOLR_FIELD_NAME_TO_SOLR_FIELD_TYPE.get(fieldName)),
							fieldXmlMap.get(fieldName),
							solrField.getIsFacetable(),
							solrField.getIsMultiValued()));

					LOGGER.info("Assigned field '{}' to RANDOM panl code '{}'", fieldName, assignedCode);
					break;
				}
				i++;
			}
			CODES_AVAILABLE.remove(assignedCode);
		}


		StringBuilder panlLpseOrder = new StringBuilder();
		StringBuilder panlLpseFacetOrder = new StringBuilder();
		// we are going to put the passthrough parameter first
		String panlParamPassthrough = panlReplacementPropertyMap.get(PANL_PARAM_PASSTHROUGH);
		panlLpseOrder.append(
				             panlParamPassthrough)
		             .append(",\\\n");

		panlReplacementPropertyMap.remove(PANL_PARAM_PASSTHROUGH);

		// last but not least, we need to put the lpse order
		StringBuilder panlLpseFields = new StringBuilder();
		for (BasePanlField basePanlField : basePanlFields) {
			panlLpseOrder.append(basePanlField.getLpseCode());
			panlLpseOrder.append(",\\\n");
			panlLpseFields.append(basePanlField.toProperties());

			panlLpseFacetOrder.append(basePanlField.getLpseCode())
			                  .append(",\\\n");
		}

		// there will be a trailing comma and newline on the panlLpseFacetOrder, remove it

		panlLpseFacetOrder.delete(panlLpseFacetOrder.length() -3, panlLpseFacetOrder.length());

		// put in the other parameters (query etc)

		for (String key : LPSE_ORDER_PARAMS) {
			panlLpseOrder.append(panlReplacementPropertyMap.get(key))
			             .append(",\\\n");
		}

		// remove the trailing comma
		panlLpseOrder.setLength(panlLpseOrder.length() - 3);

		// put the passthrough back
		panlReplacementPropertyMap.put(PANL_PARAM_PASSTHROUGH, panlParamPassthrough);

		PANL_PROPERTIES.put("panl.lpse.order", panlLpseOrder.toString());
		PANL_PROPERTIES.put("panl.lpse.facetorder", panlLpseFacetOrder.toString());
		PANL_PROPERTIES.put("panl.lpse.fields", panlLpseFields.toString());

		StringBuilder panlResultsFieldsDefault = new StringBuilder();
		StringBuilder panlResultsFieldsFirstFive = new StringBuilder();
		int i = 0;
		for (String resultsFieldName : resultFieldNames) {
			if (i != 0) {
				panlResultsFieldsDefault.append(",\\\n");
			}
			if (i < 5) {
				if (i != 0) {
					panlResultsFieldsFirstFive.append(",\\\n");
				}
				panlResultsFieldsFirstFive.append(resultsFieldName);
			}
			i++;
			panlResultsFieldsDefault.append(resultsFieldName);
		}
		panlResultsFieldsDefault.append("\n");

		PANL_PROPERTIES.put("panl.results.fields.default", panlResultsFieldsDefault.toString());
		PANL_PROPERTIES.put("panl.results.fields.firstfive", panlResultsFieldsFirstFive.toString());
	}

	private void generateAvailableCodesForFields() {
		// generate the codes
		for (int i = 0; i < lpseLength; i++) {
			for (char c : CODES.toCharArray()) {
				generateCode(String.valueOf(c), i + 1);
			}
		}
	}

	private void generateCode(String s, int i) {
		if (i < lpseLength) {
			for (char c : CODES.toCharArray()) {
				generateCode(s + c, i + 1);
			}
		} else {
			if (s.length() == lpseLength) {
				CODES_AVAILABLE.add(s);
			}
		}
	}

	private void parseSchemaFile(File schema) throws PanlGenerateException {
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(schema));
			while (reader.hasNext()) {
				XMLEvent nextEvent = reader.nextEvent();
				if (nextEvent.isStartElement()) {
					StartElement startElement = nextEvent.asStartElement();
					String element = startElement.getName().getLocalPart();
					switch (element) {
						case "schema":
							this.collectionName = startElement.getAttributeByName(new QName("name")).getValue();
							LOGGER.info("Found collection name of {}", this.collectionName);
							if (this.collectionName.startsWith("panl-")) {
								throw new PanlGenerateException("You CANNOT have a collection that starts with 'panl-'");
							}
							break;
						case "fieldType":
							String fieldTypeName = startElement.getAttributeByName(new QName("name")).getValue();
							String fieldClass = startElement.getAttributeByName(new QName("class")).getValue();
							SOLR_FIELD_TYPE_NAME_TO_SOLR_CLASS.put(fieldTypeName, fieldClass);
							LOGGER.info("Mapping solr field type '{}' to solr class '{}'.", fieldTypeName, fieldClass);
							break;
						case "field":
							StringBuilder sb = new StringBuilder("<field ");

							String name = startElement.getAttributeByName(new QName("name")).getValue();
							Attribute indexedAttribute = startElement.getAttributeByName(new QName("indexed"));
							String indexed = "false";
							if (null != indexedAttribute) {
								indexed = indexedAttribute.getValue();
							}

							Attribute multiValuedAttribute = startElement.getAttributeByName(new QName("multiValued"));
							boolean isMultiValued = false;
							if (null != multiValuedAttribute) {
								isMultiValued = (multiValuedAttribute.getValue().compareToIgnoreCase("true") == 0);
							}

							String stored = startElement.getAttributeByName(new QName("stored")).getValue();
							String type = startElement.getAttributeByName(new QName("type")).getValue();

							SOLR_FIELD_NAME_TO_SOLR_FIELD_TYPE.put(name, type);

							Iterator<Attribute> attributes = startElement.getAttributes();
							while (attributes.hasNext()) {
								Attribute attribute = attributes.next();
								String attributeName = attribute.getName().toString();
								String attributeValue = attribute.getValue();
								sb.append("\"")
								  .append(attributeName)
								  .append("\"=\"")
								  .append(attributeValue)
								  .append("\" ");
							}
							sb.append("/>");
							fieldXmlMap.put(name, sb.toString());
							boolean isIndexedOrStored = false;

							if (indexed.equals("true")) {
								LOGGER.info("Adding facet field names '{}' as it is indexed.", name);
								isIndexedOrStored = true;
								SolrField solrField = new SolrField(name, true);
								solrField.setIsMultiValued(isMultiValued);
								this.solrFields.add(solrField);
								this.resultFieldNames.add(name);
							} else {
								if (stored.equals("true")) {
									// then this can be returned as a field in the results
									LOGGER.info("Adding field names '{}' as it is stored, but not indexed.", name);
									isIndexedOrStored = true;
									SolrField solrField = new SolrField(name, false);
									solrField.setIsMultiValued(isMultiValued);
									this.solrFields.add(solrField);
									this.resultFieldNames.add(name);
								}
							}

							if (!isIndexedOrStored) {
								LOGGER.info("NOT Adding field name '{}' as it is neither indexed nor stored.", name);
							}
							break;
					}
				}
			}
		} catch (XMLStreamException | FileNotFoundException e) {
			throw new PanlGenerateException(
					"Could not adequately parse the '" + schema.getAbsolutePath() + "' solr schema file.");
		}
	}

	/**
	 * <p>Get the panl property value (if it exists), otherwise return an empty
	 * string.</p>
	 *
	 * @param key The key to look for
	 *
	 * @return The replacement property, or an empty string if one could not be found
	 */
	public String getPanlProperty(String key) {
		return (PANL_PROPERTIES.getOrDefault(key, ""));
	}

	/**
	 * <p>Get the collection name from the Solr managed-schema.xml file.</p>
	 *
	 * @return The collection name
	 */
	public String getCollectionName() {
		return (collectionName);
	}

	public List<BasePanlField> getFields() {
		return (basePanlFields);
	}

	public int getLpseLength() {
		return lpseLength;
	}
}
