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

package com.synapticloop.panl.editor.util;

import net.harawata.appdirs.AppDirsFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Settings {
	public static String APP_DATA_DIRECTORY = AppDirsFactory.getInstance().getUserDataDir("Panl", null, "Synapticloop");
	public static String APP_PROPERTIES_LOCATION = APP_DATA_DIRECTORY + File.separator + "panl.json";

	public static final String SETTING_IS_DARK_MODE = "isDarkMode";

	private static boolean isInitialised = false;
	private static final List<String> RECENT_FILES = new ArrayList<>();
	private static JSONObject settingsJson;

	public synchronized static void loadSettings() {
		if (isInitialised) {
			return;
		}

		File file = new File(APP_PROPERTIES_LOCATION);
		try (FileInputStream fileInputStream = new FileInputStream(file)) {
			StringBuilder stringBuilder = new StringBuilder();
			for (String readLine : IOUtils.readLines(fileInputStream, StandardCharsets.UTF_8)) {
				if(!readLine.trim().isEmpty()) {
					stringBuilder.append(readLine.trim());
				}
			}
			settingsJson = new JSONObject(stringBuilder.toString());
		} catch (Exception ex) {
			settingsJson = new JSONObject();
		}
		isInitialised = true;
	}

	public synchronized static void saveSettings() {
		File file = new File(APP_PROPERTIES_LOCATION);
		try {
			FileUtils.write(file, settingsJson.toString(), StandardCharsets.UTF_8);
		} catch (IOException ignored) {
		}
	}

	public static void setIsDarkMode(boolean isDarkMode) {
		settingsJson.put(SETTING_IS_DARK_MODE, isDarkMode);
	}

	public static boolean getIsDarkMode() {
		return(settingsJson.optBoolean(SETTING_IS_DARK_MODE, false));
	}

	public static List<String> getRecentFiles() {
		return(RECENT_FILES);
	}
}