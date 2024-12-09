package com.synapticloop.panl.server.handler.helper;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;

public class PropertyHelperTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyHelperTest.class);
	@Test public void testDefault() {
		PropertyHelper propertyHelper = new PropertyHelper();

		Properties properties = new Properties();
		int defaultValue = 100;
		assertEquals(defaultValue, PropertyHelper.getIntProperty(LOGGER, properties, "missing.key", defaultValue));

		properties.put("key", "100");
		assertEquals(defaultValue, PropertyHelper.getIntProperty(LOGGER, properties, "key", defaultValue));

		properties.put("key", "askdkjhsadfkjh");
		assertEquals(defaultValue, PropertyHelper.getIntProperty(LOGGER, properties, "key", defaultValue));

		properties.put("key", "100L");
		assertEquals(defaultValue, PropertyHelper.getIntProperty(LOGGER, properties, "key", defaultValue));

	}
}
