package com.synapticloop.panl.generator.bean;

import com.synapticloop.panl.exception.PanlGenerateException;
import com.synapticloop.panl.generator.PanlGenerator;
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

	public static String CODES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234567890";
	public static String CODES_AND_METADATA = CODES + "[].+-";
	private static final Set<String> CODES_AVAILABLE = new HashSet<>();
	private static final Map<String, PanlProperty> PANL_PROPERTIES = new HashMap<>();
	private static final Map<String, String> SOLR_FIELD_TYPE_NAME_TO_SOLR_CLASS = new HashMap<>();
	private static final Map<String, String> SOLR_FIELD_NAME_TO_SOLR_FIELD_TYPE = new HashMap<>();

	public Collection(File schema, Map<String, String> panlReplacementPropertyMap) throws PanlGenerateException {
		parseSchemaFile(schema);

		// each character can be one of the letters and numbers
		this.lpseNumber = facetFieldNames.size() / 62;

		// don't forget that we have pre-defined 'params'
		if ((facetFieldNames.size() % 62 - panlReplacementPropertyMap.size()) > 0) {
			this.lpseNumber++;
		}

		LOGGER.info("Collection: {}", this.collectionName);
		LOGGER.info("Have {} fields, lpseNum is set to {}", facetFieldNames.size(), this.lpseNumber);

		// now we are going to remove all codes that are in use by the panl replacement map
		for (String code : panlReplacementPropertyMap.values()) {
			CODES = CODES.replace(code, "");
		}

		generateAvailableCodesForFields();

		PanlProperty panlLpseNum = new PanlProperty("panl.lpse.num", "" + lpseNumber);
		PANL_PROPERTIES.put("$panl.lpse.num", panlLpseNum);

		for (String property : panlReplacementPropertyMap.keySet()) {
			PanlProperty temp = new PanlProperty(property.substring(1), panlReplacementPropertyMap.get(property));
			PANL_PROPERTIES.put(property, temp);
		}

		// now go through to fields and assign a code which is close to what they want...
		for (String fieldName : facetFieldNames) {
			String cleanedName = fieldName.replaceAll("[^A-Za-z0-9]", "");
			String possibleCode = cleanedName.substring(0, lpseNumber);
			if (CODES_AVAILABLE.contains(possibleCode)) {
				fields.add(new Field(
						possibleCode,
						fieldName,
						fieldXmlMap.get(fieldName),
						SOLR_FIELD_TYPE_NAME_TO_SOLR_CLASS.get(SOLR_FIELD_NAME_TO_SOLR_FIELD_TYPE.get(fieldName))));
				LOGGER.info("Assigned field '{}' to panl code '{}'", fieldName, possibleCode);
				CODES_AVAILABLE.remove(possibleCode);
			} else if (CODES_AVAILABLE.contains(possibleCode.toUpperCase())) {
				String nextPossibleCode = possibleCode.toUpperCase();
				fields.add(new Field(
						nextPossibleCode,
						fieldName,
						fieldXmlMap.get(fieldName),
						SOLR_FIELD_TYPE_NAME_TO_SOLR_CLASS.get(SOLR_FIELD_NAME_TO_SOLR_FIELD_TYPE.get(fieldName))));
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
					fields.add(new Field(
							assignedCode,
							unassignedFieldName,
							fieldXmlMap.get(unassignedFieldName),
							SOLR_FIELD_TYPE_NAME_TO_SOLR_CLASS.get(SOLR_FIELD_NAME_TO_SOLR_FIELD_TYPE.get(unassignedFieldName))));

					LOGGER.info("Assigned field '{}' to RANDOM panl code '{}'", unassignedFieldName, assignedCode);
					break;
				}
				i++;
			}
			CODES_AVAILABLE.remove(assignedCode);
		}


		StringBuilder panlLpseOrder = new StringBuilder();
		// we are going to put the passthrough parameter first
		panlLpseOrder.append(panlReplacementPropertyMap.get("$" + PanlGenerator.PANL_PARAM_PASSTHROUGH))
						.append(",\\\n");

		panlReplacementPropertyMap.remove("$" + PanlGenerator.PANL_PARAM_PASSTHROUGH);

		// last but not least, we need to put the lpse order
		StringBuilder panlLpseFields = new StringBuilder();
		for (Field field : fields) {
			panlLpseOrder.append(field.getCode());
			panlLpseOrder.append(",\\\n");
			panlLpseFields.append(field.toProperties());
		}

		// put in the other parameters (query etc)
		// we are doing this as it is a linked hashmap on the order in which it was inserted
		for (String key : panlReplacementPropertyMap.keySet()) {
			panlLpseOrder.append(panlReplacementPropertyMap.get(key))
							.append(",\\\n");
		}

		// remove the trailing comma
		panlLpseOrder.setLength(panlLpseOrder.length() - 3);


		PANL_PROPERTIES.put("$panl.lpse.order", new PanlProperty("panl.lpse.order", panlLpseOrder.toString()));
		PANL_PROPERTIES.put("$panl.lpse.fields", new PanlProperty("panl.lpse.fields", panlLpseFields.toString(), true));

		boolean isFirst = true;
		StringBuilder panlResultsFieldsAll = new StringBuilder();
		StringBuilder panlResultsFieldsFirstFive = new StringBuilder();
		int i = 0;
		for (String resultsFieldName: resultFieldNames) {
			if(i != 0) {
				panlResultsFieldsAll.append(",\\\n");
			}
			if(i < 5) {
				if(i != 0) {
					panlResultsFieldsFirstFive.append(",\\\n");
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

	private void generateAvailableCodesForFields() {
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
						case "fieldType":
							String fieldTypeName = startElement.getAttributeByName(new QName("name")).getValue();
							String fieldClass = startElement.getAttributeByName(new QName("class")).getValue();
							SOLR_FIELD_TYPE_NAME_TO_SOLR_CLASS.put(fieldTypeName, fieldClass);
							LOGGER.info("Mapping solr field type '{}' to solr class '{}'.", fieldTypeName, fieldClass);
							break;
						case "field":
							StringBuilder sb = new StringBuilder("<field ");

							String name = startElement.getAttributeByName(new QName("name")).getValue();
							String indexed = startElement.getAttributeByName(new QName("indexed")).getValue();
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

							if(indexed.equals("true")) {
								LOGGER.info("Adding facet field name '{}' as it is indexed.", name);
								isIndexedOrStored = true;
								this.facetFieldNames.add(name);
							}

							if (stored.equals("true")) {
								// then this can be returned as a field in the results
								LOGGER.info("Adding result field name '{}' as it is stored.", name);
								isIndexedOrStored = true;
								this.resultFieldNames.add(name);
							}

							if(!isIndexedOrStored){
								LOGGER.info("NOT Adding field name '{}' as it is neither indexed nor stored.", name);
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
