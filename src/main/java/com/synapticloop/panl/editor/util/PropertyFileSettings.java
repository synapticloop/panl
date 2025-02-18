/*
 * Copyright (c) 2008-2025 synapticloop.
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

import org.json.JSONObject;

import java.awt.*;
import java.io.File;

import static com.synapticloop.panl.editor.util.Settings.*;

public class PropertyFileSettings {
	private final File panlPropertisFile;
	private Point location = new Point(5, 5);
	private boolean includeComments = true;

	public PropertyFileSettings(JSONObject jsonObject) {
		this.includeComments = jsonObject.optBoolean(JSON_KEY_INCLUDE_COMMENTS, true);
		this.panlPropertisFile = new File(JSON_KEY_FILEPATH);
		this.location = new Point(
				jsonObject.optInt(JSON_KEY_X_POSITION, 5),
				jsonObject.optInt(JSON_KEY_Y_POSITION, 5)
		);
	}

	public PropertyFileSettings(File panlPropertisFile) {
		this.panlPropertisFile = panlPropertisFile;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public boolean getIncludeComments() {
		return includeComments;
	}

	public void setIncludeComments(boolean includeComments) {
		this.includeComments = includeComments;
	}

	public JSONObject toJsonObject() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(JSON_KEY_X_POSITION, location.x);
		jsonObject.put(JSON_KEY_Y_POSITION, location.y);
		jsonObject.put(JSON_KEY_FILEPATH, panlPropertisFile.getAbsoluteFile());
		jsonObject.put(JSON_KEY_INCLUDE_COMMENTS, includeComments);
		return(jsonObject);
	}
}
