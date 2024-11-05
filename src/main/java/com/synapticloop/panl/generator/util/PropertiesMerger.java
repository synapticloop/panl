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

package com.synapticloop.panl.generator.util;

import com.synapticloop.panl.generator.PanlGenerator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class PropertiesMerger {
	public static String mergeProperties(String templateLocation, Map<String, ?> values, boolean keepComments) {
		StringBuilder outputString = new StringBuilder();

		try (InputStream inputStream = PanlGenerator.class.getResourceAsStream(templateLocation)) {
			assert inputStream != null;

			// determine the output directory for the panl.properties and the
			// associated directory
			try (InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
			     BufferedReader reader = new BufferedReader(streamReader)) {

				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("#")) {
						if (keepComments) {
							outputString
								.append(line)
								.append("\n");
						}
					} else if(line.startsWith("$ ") || line.trim().equals("$")) {
						// ignore this line - don't output it - these are just notes in the
						// file explaining the '$' character and what it is used for
					} else if (line.startsWith("$")) {
						if (line.startsWith("$panl.collections")) {
							outputString
								.append(values.get("panl.collections").toString())
								.append("\n");
						} else {
							String propertyKey = line.substring(1);
							if (values.containsKey(propertyKey)) {
								outputString
									.append(propertyKey)
									.append("=")
									.append(values.get(propertyKey).toString())
									.append("\n");
							} else {
								outputString
									.append("#")
									.append("ERROR - Expecting key of '")
									.append(propertyKey)
									.append("' but was not found.")
									.append("\n");
							}
						}
					} else {
						if (line.isBlank()) {
							if (keepComments) {
								outputString
									.append(line)
									.append("\n");
							}
						} else {
							outputString
								.append(line)
								.append("\n");
						}
					}
				}

			}
		} catch (IOException e) {
			outputString
				.append("# Could not generate the file from the template\n")
				.append("# Template file: \n")
				.append("# ")
				.append(templateLocation)
				.append("\n\n")
				.append("# Key/Value pairs:\n");

			for (String key : values.keySet()) {
				outputString
					.append("#   ")
					.append(key)
					.append(" : ")
					.append(values.get(key))
					.append("\n");
			}

			outputString
				.append("\n\n")
				.append("# Error was:\n")
				.append("# ")
				.append(e.getMessage());
		}

		return (outputString.toString());
	}
}
