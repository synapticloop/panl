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

package com.synapticloop.integration.test.facet.regular;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapticloop.integration.BeforeAllExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import panl.Root;
import panl.response.panl.Sort;
import panl.response.panl.Sorting;
import panl.response.panl.active.Facet;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({BeforeAllExtension.class})
public class RegularFacetTest {
	ObjectMapper mapper = new ObjectMapper();

	/**
	 * This tests to ensure that added facets which are REGULAR multivalued
	 * facets have the correct removal URL
	 *
	 * @throws IOException
	 */
	@Test public void testMultipleRegularMultiValuedFacetRemoval() throws IOException {
		Root root = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils/brandandname/Black/Blue/Red/WWW/"), Root.class);
		assertFalse(root.error);
		Facet[] facets = root.panl.active.facet;
		assertEquals(3, facets.length);
		for(Facet facet : facets) {
			if(!facet.remove_uri.endsWith("/WW/")) {
				fail("Expecting URL of '" + facet.remove_uri + "' to end with /WW/");
			}
		}
	}

	/**
	 * This tests to ensure that added facets which are REGULAR multivalued
	 * with an or separator have the correct removal url
	 *
	 * @throws IOException
	 */
	@Test public void testMultipleRegularMultiValuedFacetRemovalOrSeparator() throws IOException {
		Root root = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils-multi-separator/brandandname/Colours:Black,Blue,Red/W/"), Root.class);
		assertFalse(root.error);
		Facet[] facets = root.panl.active.facet;
		assertEquals(3, facets.length);
		for(Facet facet : facets) {
			if(!facet.remove_uri.endsWith("/W/")) {
				fail("Expecting URL of '" + facet.remove_uri + "' to end with /W/");
			}
		}
	}

	/**
	 * This tests to ensure that added facets which are REGULAR multivalued
	 * with an or separator have the correct removal url
	 *
	 * @throws IOException
	 */
	@Test public void testMultipleRegularMultiValuedFacetRemovalOrSeparatorWithSort() throws IOException {
		Root root = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils-multi-separator/brandandname/Colours:Black,Blue,Red/Wsb-/"), Root.class);
		assertFalse(root.error);
		Facet[] facets = root.panl.active.facet;
		assertEquals(3, facets.length);

		for (Sort sort : root.panl.active.sort) {
			if(!sort.remove_uri.endsWith("/W/")) {
				fail("Expecting URL of '" + sort.remove_uri + "' to end with /W/");
			}

			if(!sort.inverse_uri.endsWith("/Wsb+/")) {
				fail("Expecting URL of '" + sort.inverse_uri + "' to end with /Wsb+/");
			}
		}
	}

	@Test public void testMultiWithSort() throws IOException {
		Root root = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils-or/empty/Black/Blue/Red/WWWsb-sN+/"), Root.class);
		assertFalse(root.error);
		Facet[] facets = root.panl.active.facet;
		assertEquals(3, facets.length);

		Sort[] sorts = root.panl.active.sort;
		// sorting is always in order...

		if(!sorts[0].inverse_uri.endsWith("/WWWsb+sN+/")) {
			fail("Expecting URL of '" + sorts[0].inverse_uri + "' to end with /WWWsb+sN+/");
		}

		if(!sorts[0].remove_uri.endsWith("/WWWsN+/")) {
			fail("Expecting URL of '" + sorts[0].remove_uri + "' to end with /WWWsN+/");
		}


		if(!sorts[1].inverse_uri.endsWith("/Wsb-/")) {
			fail("Expecting URL of '" + sorts[1].inverse_uri + "' to end with /Wsb-sN-/");
		}
	}

}
