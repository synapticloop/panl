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

package com.synapticloop.integration.test.facet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapticloop.integration.BeforeAllExtension;
import com.synapticloop.integration.response.json.Root;
import com.synapticloop.integration.response.json.response.panl.active.Facet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({BeforeAllExtension.class})
public class ActiveFacetTest {
	ObjectMapper mapper = new ObjectMapper();
	@Test public void testSingleFacet() throws IOException {
		Root root = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils/brandandname/Black/W/"), Root.class);
		assertFalse(root.error);
		Facet[] facets = root.panl.active.facet;
		assertEquals(1, facets.length);
	}

	@Test public void testAvailableActiveFacets() throws IOException {
		Root root = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils/brandandname/"), Root.class);
		assertFalse(root.error);
		for (com.synapticloop.integration.response.json.response.panl.available.Facet facet : root.panl.available.facets) {
			String addFacet = facet.uris.before + facet.values[0].encoded + facet.uris.after;
			Root activeRoot = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils/brandandname" + addFacet), Root.class);
			assertFalse(root.error);
			assertEquals(activeRoot.panl.active.facet[0].encoded, facet.values[0].encoded);
			assertEquals(activeRoot.panl.active.facet[0].remove_uri, "/");
		}
	}
}
