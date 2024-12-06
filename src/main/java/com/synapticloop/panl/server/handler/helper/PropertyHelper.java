package com.synapticloop.panl.server.handler.helper;

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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * <p>Simple utility class to help with properties.</p>
 *
 * @author synapticloop
 */
public class PropertyHelper {

	/**
	 * <p>Get a list of all properties that start with the passed in prefix.</p>
	 *
	 * @param properties The properties file to look through
	 * @param prefix The prefix to search for
	 *
	 * @return The list of matching property keys that start with the prefix
	 */
	public static List<String> getPropertiesByPrefix(Properties properties, String prefix) {
		List<String> list = new ArrayList<>();
		Enumeration<Object> keys = properties.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if (key.startsWith(prefix)) {
				list.add(key);
			}
		}
		return (list);
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
	 * 		does not exist, or it cannot be parsed.
	 */
	public static Integer getIntProperty(Properties properties, String key, Integer defaultValue) {
		try {
			String property = properties.getProperty(key, null);
			if (null == property) {
				return (defaultValue);
			}

			return (Integer.parseInt(properties.getProperty(key)));
		} catch (NumberFormatException e) {
			return (defaultValue);
		}
	}
}
