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
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Settings {
	public static final String JSON_KEY_X_POSITION = "x_position";
	public static final String JSON_KEY_Y_POSITION = "y_position";
	public static final String JSON_KEY_RECENT_FILES = "recent_files";
	public static final String JSON_KEY_FILENAME = "filename";
	public static String APP_DATA_DIRECTORY = AppDirsFactory.getInstance().getUserDataDir("Panl", null, "Synapticloop");
	public static String APP_PROPERTIES_LOCATION = APP_DATA_DIRECTORY + File.separator + "panl.json";

	public static final String SETTING_IS_DARK_MODE = "isDarkMode";

	private static boolean isInitialised = false;
	private static JSONObject settingsJson;
	private static final Map<String, Point> RECENT_FILES = new LinkedHashMap<>();

	public synchronized static void loadSettings() {
		if (isInitialised) {
			return;
		}

		File file = new File(APP_PROPERTIES_LOCATION);
		try (FileInputStream fileInputStream = new FileInputStream(file)) {
			StringBuilder stringBuilder = new StringBuilder();
			for (String readLine : IOUtils.readLines(fileInputStream, StandardCharsets.UTF_8)) {
				if (!readLine.trim().isEmpty()) {
					stringBuilder.append(readLine.trim());
				}
			}
			settingsJson = new JSONObject(stringBuilder.toString());
		} catch (Exception ex) {
			settingsJson = new JSONObject();
		}
		isInitialised = true;

		// load up the recent files
		for (Object recentFile : settingsJson.optJSONArray(JSON_KEY_RECENT_FILES, new JSONArray())) {
			JSONObject recentFileObject = (JSONObject) recentFile;
			String filename = recentFileObject.optString(JSON_KEY_FILENAME, null);
			int x = recentFileObject.optInt(JSON_KEY_X_POSITION, 5);
			int y = recentFileObject.optInt(JSON_KEY_Y_POSITION, 5);

			if(null != filename) {
				RECENT_FILES.put(filename, new Point(x, y));
			}
		}


	}

	public synchronized static void saveSettings() {
		File file = new File(APP_PROPERTIES_LOCATION);

		JSONArray recentFilesArray = new JSONArray();
		int i = 0;
		for (String recentFile : RECENT_FILES.keySet()) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(JSON_KEY_FILENAME, recentFile);
			Point point = RECENT_FILES.get(recentFile);
			jsonObject.put(JSON_KEY_X_POSITION, point.x);
			jsonObject.put(JSON_KEY_Y_POSITION, point.y);
			recentFilesArray.put(jsonObject);

			i++;
			// we are only going to keep the last 40 files
			if(i > 40) {
				break;
			}
		}

		settingsJson.put(JSON_KEY_RECENT_FILES, recentFilesArray);

		try {
			FileUtils.write(file, settingsJson.toString(2), StandardCharsets.UTF_8);
		} catch (IOException ignored) {
		}
	}

	public static void setIsDarkMode(boolean isDarkMode) {
		settingsJson.put(SETTING_IS_DARK_MODE, isDarkMode);
	}

	public static boolean getIsDarkMode() {
		return (settingsJson.optBoolean(SETTING_IS_DARK_MODE, false));
	}

	public static String[] getRecentFiles() {
		String[] array = RECENT_FILES.keySet().toArray(new String[0]);
		ArrayUtils.reverse(array);
		return (array);
	}

	public static void setMainPosition(int x, int y) {
		settingsJson.put(JSON_KEY_X_POSITION, x);
		settingsJson.put(JSON_KEY_Y_POSITION, y);
	}

	public static void setSubPosition(String filename, int x, int y) {
		RECENT_FILES.put(filename, new Point(x, y));
	}

	public static Point getSubPosition(String filename) {
		if(RECENT_FILES.containsKey(filename)) {
			return(RECENT_FILES.get(filename));
		} else {
			return(new Point(5, 5));
		}
	}

	public static Point getMainPosition() {
		return (
			new Point(
				settingsJson.optInt(JSON_KEY_X_POSITION, 0),
				settingsJson.optInt(JSON_KEY_Y_POSITION, 0)));
	}

	public static void addRecentFile(File file) {
		String absolutePath = file.getAbsolutePath();
		Point position = new Point(5,5);
		if(RECENT_FILES.containsKey(absolutePath)) {
			position = RECENT_FILES.get(absolutePath);
		}

		RECENT_FILES.remove(absolutePath);
		RECENT_FILES.put(absolutePath, position);
	}

	public static void removeRecentFile(File file) {
		String absolutePath = file.getAbsolutePath();
		RECENT_FILES.remove(absolutePath);
	}

}