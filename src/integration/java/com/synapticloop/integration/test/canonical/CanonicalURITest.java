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

package com.synapticloop.integration.test.canonical;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapticloop.integration.BeforeAllExtension;
import com.synapticloop.integration.response.json.Root;
import com.synapticloop.integration.response.json.response.panl.available.Facet;
import com.synapticloop.integration.test.util.Helper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({BeforeAllExtension.class})
public class CanonicalURITest {
	ObjectMapper mapper = new ObjectMapper();

	@Test public void testCanonical() throws IOException {
		String lines = IOUtils.toString(CanonicalURITest.class.getResourceAsStream("/integration.canonical.txt"), StandardCharsets.UTF_8);
		for (String s : lines.split("\n")) {
			if(s.isBlank() || s.startsWith("#")) {
				continue;
			}
			String[] splits = s.split("\t");
			if(splits.length == 2) {
				testCanonical(splits[0], splits[1].trim());
			}
		}
	}

	public void testCanonical(String URL, String expected) throws IOException {
		Root root = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils/brandandname" + URL), Root.class);
		assertFalse(root.error);
		assertEquals(expected, root.panl.canonical_uri);
	}
}
