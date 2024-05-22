package com.synapticloop.panl.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class PropertyHelper {
	public static List<String> getPropertiesStartsWith(Properties properties, String startsWith) {
		List<String> list = new ArrayList<>();
		Enumeration<Object> keys = properties.keys();
		while(keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			if(key.startsWith(startsWith)) {
				list.add(key);
			}
		}
		return(list);
	}

	public static int getIntProperty(Properties properties, String key, int defaultValue) {
		try {
			return(Integer.parseInt(properties.getProperty(key, defaultValue + "")));
		} catch(NumberFormatException e) {
			return(defaultValue);
		}
	}
}
