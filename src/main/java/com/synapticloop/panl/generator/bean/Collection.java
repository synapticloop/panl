package com.synapticloop.panl.generator.bean;

import com.synapticloop.panl.exception.PanlGenerateException;
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

public class Collection {
	private static final Logger LOGGER = LoggerFactory.getLogger(Collection.class);

	private String collectionName;
	private final List<String> facetFieldNames = new ArrayList<>();
	private final List<String> resultFieldNames = new ArrayList<>();
	private final List<Field> fields = new ArrayList<>();
	private final List<String> unassignedFieldNames = new ArrayList<>();
	private final Map<String, String> fieldXmlMap = new HashMap<>();

	private int lpseNumber = 1;

	public static final String CODES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234567890";
	public static final String CODES_AND_METADATA = CODES + "[].+-";
	private static final Set<String> CODES_AVAILABLE = new HashSet<>();
	private static final Map<String, PanlProperty> PANL_PROPERTIES = new HashMap<>();

	public Collection(File schema) throws PanlGenerateException {
		parseSchemaFile(schema);

		// at this point, all going well, we have the collection name and the
		//		panl.param.query=q
		//		panl.param.sort=s
		//		panl.param.page=p
		//		panl.param.numrows=n

		// each character can be one of the letters and numbers
		this.lpseNumber = facetFieldNames.size() / 62;

		// don't forget that we have 4 pre-defined 'params'
		if ((facetFieldNames.size() % 62 - 4) > 0) {
			this.lpseNumber++;
		}

		LOGGER.info("Collection: {}", this.collectionName);
		LOGGER.info("Have {} fields, lpseNum is set to {}", facetFieldNames.size(), this.lpseNumber);

		generateCodesForFields();

		// now we are going to add the in-built fields that we require to run panl
		PanlProperty panlParamQuery = new PanlProperty("panl.param.query", "q", lpseNumber);
		PANL_PROPERTIES.put("$panl.param.query", panlParamQuery);

		PanlProperty panlParamPage = new PanlProperty("panl.param.page", "p", lpseNumber);
		PANL_PROPERTIES.put("$panl.param.page", panlParamPage);

		PanlProperty panlParamSort = new PanlProperty("panl.param.sort", "s", lpseNumber);
		PANL_PROPERTIES.put("$panl.param.sort", panlParamSort);

		PanlProperty panlParamNumrows = new PanlProperty("panl.param.numrows", "n", lpseNumber);
		PANL_PROPERTIES.put("$panl.param.numrows", panlParamNumrows);

		PanlProperty panlLpseNum = new PanlProperty("panl.lpse.num", "" + lpseNumber);
		PANL_PROPERTIES.put("$panl.lpse.num", panlLpseNum);

		CODES_AVAILABLE.remove(panlParamNumrows.getPanlValue());
		CODES_AVAILABLE.remove(panlParamPage.getPanlValue());
		CODES_AVAILABLE.remove(panlParamSort.getPanlValue());
		CODES_AVAILABLE.remove(panlParamQuery.getPanlValue());

		// now go through to fields and assign a code which is close to what they want...
		for (String fieldName : facetFieldNames) {
			String cleanedName = fieldName.replaceAll("[^A-Za-z0-9]", "");
			String possibleCode = cleanedName.substring(0, lpseNumber);
			if (CODES_AVAILABLE.contains(possibleCode)) {
				fields.add(new Field(possibleCode, fieldName, fieldXmlMap.get(fieldName)));
				LOGGER.info("Assigned field '{}' to panl code '{}'", fieldName, possibleCode);
				CODES_AVAILABLE.remove(possibleCode);
			} else if (CODES_AVAILABLE.contains(possibleCode.toUpperCase())) {
				String nextPossibleCode = possibleCode.toUpperCase();
				fields.add(new Field(nextPossibleCode, fieldName, fieldXmlMap.get(fieldName)));
				LOGGER.info("Assigned field '{}' to panl code '{}'", fieldName, nextPossibleCode);
				CODES_AVAILABLE.remove(nextPossibleCode);
			} else {
				LOGGER.warn("No nice panl code for field '{}', '{}' and '{}' already taken", fieldName, possibleCode, possibleCode.toUpperCase());
				unassignedFieldNames.add(fieldName);
			}
		}

		// at this point, we are going to go through all unassigned field names and
		// try and determine what we should mark them as
		for (String unassignedFieldName : unassignedFieldNames) {
			int size = CODES_AVAILABLE.size();
			int item = new Random().nextInt(size); // In real life, the Random object should be rather more shared than this
			int i = 0;
			String assignedCode = null;
			for (String code : CODES_AVAILABLE) {
				if (i == item) {
					assignedCode = code;
					fields.add(new Field(assignedCode, unassignedFieldName, fieldXmlMap.get(unassignedFieldName)));
					LOGGER.info("Assigned field '{}' to RANDOM panl code '{}'", unassignedFieldName, assignedCode);
					break;
				}
				i++;
			}
			CODES_AVAILABLE.remove(assignedCode);
		}

		// last but not least, we need to put the lpse order
		StringBuilder panlLpseOrder = new StringBuilder();
		StringBuilder panlLpseFields = new StringBuilder();
		for (Field field : fields) {
			panlLpseOrder.append(field.getCode());
			panlLpseOrder.append(",");
			panlLpseFields.append(field.toProperties());
		}

		panlLpseOrder.append(panlParamPage.getPanlValue());
		panlLpseOrder.append(",");
		panlLpseOrder.append(panlParamNumrows.getPanlValue());
		panlLpseOrder.append(",");
		panlLpseOrder.append(panlParamSort.getPanlValue());
		panlLpseOrder.append(",");
		panlLpseOrder.append(panlParamQuery.getPanlValue());

		// we do not put in the collection facet - as this is done automatically by the server

		// put in the other parameters (query etc)

		PANL_PROPERTIES.put("$panl.lpse.order", new PanlProperty("panl.lpse.order", panlLpseOrder.toString()));
		PANL_PROPERTIES.put("$panl.lpse.fields", new PanlProperty("panl.lpse.fields", panlLpseFields.toString(), true));

		boolean isFirst = true;
		StringBuilder panlResultsFieldsAll = new StringBuilder();
		StringBuilder panlResultsFieldsFirstFive = new StringBuilder();
		int i = 0;
		for (String resultsFieldName: resultFieldNames) {
			if(i != 0) {
				panlResultsFieldsAll.append(",");
			}
			if(i < 5) {
				if(i != 0) {
					panlResultsFieldsFirstFive.append(",");
				}
				panlResultsFieldsFirstFive.append(resultsFieldName);
			}
			i++;
			panlResultsFieldsAll.append(resultsFieldName);
		}
		panlResultsFieldsAll.append("\n");

		PANL_PROPERTIES.put("$panl.results.fields.all", new PanlProperty("panl.results.fields.all", panlResultsFieldsAll.toString()));
		PANL_PROPERTIES.put("$panl.results.fields.firstfive", new PanlProperty("panl.results.fields.firstfive", panlResultsFieldsFirstFive.toString()));
	}

	private void generateCodesForFields() {
		// generate the codes
		for (int i = 0; i < lpseNumber; i++) {
			for (char c : CODES.toCharArray()) {
				generateCode(String.valueOf(c), i + 1);
			}
		}
	}

	private void generateCode(String s, int i) {
		if (i < lpseNumber) {
			for (char c : CODES.toCharArray()) {
				generateCode(s + c, i + 1);
			}
		} else {
			if (s.length() == lpseNumber) {
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
							break;
						case "field":
							StringBuilder sb = new StringBuilder("<field ");

							String name = startElement.getAttributeByName(new QName("name")).getValue();
							String indexed = startElement.getAttributeByName(new QName("indexed")).getValue();
							String stored = startElement.getAttributeByName(new QName("stored")).getValue();

							Iterator<Attribute> attributes = startElement.getAttributes();
							while (attributes.hasNext()) {
								Attribute attribute = attributes.next();
								String attributeName = attribute.getName().toString();
								String attributeValue = attribute.getValue().toString();
								sb.append("\"")
										.append(attributeName)
										.append("\"=\"")
										.append(attributeValue)
										.append("\" ");
							}
							sb.append("/>");
							fieldXmlMap.put(name, sb.toString());

							if (stored.equals("true")) {
								// then this can be returned as a field in the results
								this.resultFieldNames.add(name);
								if(indexed.equals("true")) {
									LOGGER.info("Adding field name '{}' as it is indexed and stored", name);
									this.facetFieldNames.add(name);
								}
							} else {
								LOGGER.info("NOT Adding field name '{}' as it is not indexed and stored", name);
							}
							break;
					}
				}
			}
		} catch (XMLStreamException | FileNotFoundException e) {
			throw new PanlGenerateException("Could not adequately parse the '" + schema.getAbsolutePath() + "' solr schema file.");
		}
	}

	public String getPanlProperty(String key) {
		PanlProperty panlProperty = PANL_PROPERTIES.get(key);
		if (null == panlProperty) {
			return ("\n");
		} else {
			return (panlProperty.toProperties());
		}
	}

	public String getCollectionName() {
		return (collectionName);
	}

	public List<Field> getFields() {
		return (fields);
	}

	public int getLpseNumber() {
		return (lpseNumber);
	}
}
