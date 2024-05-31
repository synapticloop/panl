package com.synapticloop.panl.server.properties.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * <pSimple utility class to help with properties.</p>
 *
 * @author synapticloop
 */
public class PropertyHelper {
	/**
	 * <p>Get a list of all of the properties that start with the prefix.</p>
	 *
	 * @param properties The properties file to look through
	 * @param prefix The prefix to search for
	 *
	 * @return The list of matching property keys that start with the prefix
	 */
	public static List<String> getPropertiesByPrefix(Properties properties, String prefix) {
		List<String> list = new ArrayList<>();
		Enumeration<Object> keys = properties.keys();
		while(keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			if(key.startsWith(prefix)) {
				list.add(key);
			}
		}
		return(list);
	}

	/**
	 * <p>Return an integer value for the property.  If the property does not
	 * exist or cannot be parsed to an int value, then the default is returned.</p>
	 *
	 * @param properties The properties file to look up the key in
	 * @param key The key to look up
	 * @param defaultValue The default value
	 *
	 * @return The parsed property (if it exists), or the default value if it
	 *    does not exist, or it cannot be parsed.
	 */
	public static Integer getIntProperty(Properties properties, String key, Integer defaultValue) {
		try {
			String property = properties.getProperty(key, null);
			if(null == property) {
				return(defaultValue);
			}

			return(Integer.parseInt(properties.getProperty(key)));
		} catch(NumberFormatException e) {
			return(defaultValue);
		}
	}
}
