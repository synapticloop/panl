package com.synapticloop.panl.server.handler.helper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;

public class PropertyHelperTest {
	@Test public void testDefault() {
		PropertyHelper propertyHelper = new PropertyHelper();

		Properties properties = new Properties();
		int defaultValue = 100;
		assertEquals(defaultValue, PropertyHelper.getIntProperty(properties, "missing.key", defaultValue));

		properties.put("key", "100");
		assertEquals(defaultValue, PropertyHelper.getIntProperty(properties, "key", defaultValue));

		properties.put("key", "askdkjhsadfkjh");
		assertEquals(defaultValue, PropertyHelper.getIntProperty(properties, "key", defaultValue));

		properties.put("key", "100L");
		assertEquals(defaultValue, PropertyHelper.getIntProperty(properties, "key", defaultValue));

	}
}
