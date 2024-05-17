package com.synapticloop.panl.generator.bean;

import com.synapticloop.panl.Main;
import com.synapticloop.panl.exception.PanlGenerateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class Collection {
	private static final Logger LOGGER = LoggerFactory.getLogger(Collection.class);
	private String collectionName;
	private List<String> fieldNames = new ArrayList<>();
	private List<Field> fields = new ArrayList<>();
	private List<String> unassignedFieldNames = new ArrayList<>();

	private int lpseNumber = 1;

	private static final String CODES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234567890";
	private static final Set<String> CODES_USED = new HashSet<>();
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
		this.lpseNumber = fieldNames.size() / 62;

		// don't forget that we have 4 pre-defined 'params'
		if ((fieldNames.size() % 62 - 4) > 0) {
			this.lpseNumber++;
		}

		LOGGER.info("Collection: {}", this.collectionName);
		LOGGER.info("Have {} fields, lpseNum is set to {}", fieldNames.size(), this.lpseNumber);

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

		CODES_AVAILABLE.remove(panlParamNumrows.getPanlCode());
		CODES_AVAILABLE.remove(panlParamPage.getPanlCode());
		CODES_AVAILABLE.remove(panlParamSort.getPanlCode());
		CODES_AVAILABLE.remove(panlParamQuery.getPanlCode());

		// now go through to fields and assign a code which is close to what they want...
		for(String fieldName: fieldNames) {
			String cleanedName = fieldName.replaceAll("[^A-Za-z0-9]", "");
			String possibleCode = cleanedName.substring(0, lpseNumber);
			if(CODES_AVAILABLE.contains(possibleCode)) {
				fields.add(new Field(possibleCode, fieldName));
				LOGGER.info("Assigned field '{}' to panl code '{}'", fieldName, possibleCode);
				CODES_AVAILABLE.remove(possibleCode);
			} else if(CODES_AVAILABLE.contains(possibleCode.toUpperCase())){
				String nextPossibleCode = possibleCode.toUpperCase();
				fields.add(new Field(nextPossibleCode, fieldName));
				LOGGER.info("Assigned field '{}' to panl code '{}'", fieldName, nextPossibleCode);
				CODES_AVAILABLE.remove(nextPossibleCode);
			} else {
				LOGGER.warn("No nice panl code for field '{}', '{}' and '{}' already taken", fieldName, possibleCode, possibleCode.toUpperCase());
				unassignedFieldNames.add(fieldName);
			}
		}

		// at this point, we are going to go through all unassigned field names and
		// try and determine what we should mark them as
		for(String unassignedFieldName: unassignedFieldNames) {
			int size = CODES_AVAILABLE.size();
			int item = new Random().nextInt(size); // In real life, the Random object should be rather more shared than this
			int i = 0;
			String assignedCode = null;
			for(String code : CODES_AVAILABLE) {
				if (i == item) {
					assignedCode = code;
					fields.add(new Field(assignedCode, unassignedFieldName));
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
		for(Field field: fields) {
			panlLpseOrder.append(field.getCode());
			panlLpseOrder.append(",");
			panlLpseFields.append(field.toProperties());
		}

		panlLpseOrder.append(panlParamPage.getPanlCode());
		panlLpseOrder.append(",");
		panlLpseOrder.append(panlParamNumrows.getPanlCode());
		panlLpseOrder.append(",");
		panlLpseOrder.append(panlParamSort.getPanlCode());
		panlLpseOrder.append(",");
		panlLpseOrder.append(panlParamQuery.getPanlCode());

		// put in the other parameters (query etc)

		PANL_PROPERTIES.put("$panl.lpse.order", new PanlProperty("panl.lpse.order", panlLpseOrder.toString()));
		PANL_PROPERTIES.put("$panl.lpse.fields", new PanlProperty("panl.lpse.fields", panlLpseFields.toString()));

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
							String indexed = startElement.getAttributeByName(new QName("indexed")).getValue();
							String name = startElement.getAttributeByName(new QName("name")).getValue();
							// TODO - may not neet to do this
							if (indexed.equals("true")) {
								LOGGER.info("Adding field name '{}' as it is indexed", name);
								this.fieldNames.add(name);
							} else {
								LOGGER.info("NOT Adding field name '{}' as it is not indexed", name);
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
		if(null == panlProperty) {
			return("\n");
		} else {
			return(panlProperty.toProperties());
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
