package com.synapticloop.integration.test.facet.regular;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapticloop.integration.BeforeAllExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import panl.Root;
import panl.response.panl.Sort;
import panl.response.panl.active.Facet;

import static com.synapticloop.integration.test.util.Helper.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>This test class tests REGULAR facets that are marked as multivalued, i.e.
 * The Solr schema has the multiValued="true"</p>
 *
 * <pre>
 *   <field "indexed"="true" "stored"="true" "name"="colours" "type"="string" ↩
 *          "multiValued"="true" />
 * </pre>
 *
 * <p><strong>AND</strong></p>
 *
 * <p>The collection url panl configuration file also has the multivalue property
 * set: </p>
 *
 * <pre>
 *   panl.multivalue.separator.W=,
 * </pre>
 *
 * <p>The multivalued facets may also have a value separator:</p>
 * <pre>
 *   panl.multivalue.separator.W=,
 * </pre>
 */
@ExtendWith({BeforeAllExtension.class})
public class RegularMultiValuedTest {
	ObjectMapper mapper = new ObjectMapper();

	/**
	 * This tests to ensure that added facets which are REGULAR multivalued
	 * facets have the correct removal URL
	 *
	 * @throws IOException
	 */
	@Test public void testMultiRemove() throws IOException {
		Root root = mapper.readValue(new URL("http://localhost:8282/" +
				"mechanical-pencils/brandandname/" +
				"Black/Blue/Red/WWW/"), Root.class);
		assertFalse(root.error);
		Facet[] facets = root.panl.active.facet;
		assertEquals(3, facets.length);
		for(Facet facet : facets) {
			failOnIncorrectUrlEnding(facet.remove_uri, "/WW/");
		}
	}

	/**
	 * This tests to ensure that added facets which are REGULAR multivalued
	 * with an or separator have the correct removal url
	 *
	 * @throws IOException
	 */
	@Test public void testMultiRemoveOrSeparator() throws IOException {
		Root root = mapper.readValue(new URL("http://localhost:8282/" +
				"mechanical-pencils-multi-separator/brandandname/" +
				"Colours:Black,Blue,Red/W/"), Root.class);

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
	@Test public void testMultiValueOneSort() throws IOException {
		Root root = mapper.readValue(new URL("http://localhost:8282/" +
				"mechanical-pencils-multi-separator/brandandname/" +
				"Colours:Black,Blue,Red/Wsb-/"), Root.class);

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

	/**
	 * Test the following
	 * <ul>
	 *   <li>Multivalued REGULAR facet</li>
	 *   <li><strong>WITHOUT</strong> multivalued separator</li>
	 *   <li>With <strong>TWO</strong> sorting options</li>
	 * </ul>
	 *
	 * @throws IOException if something went wrong
	 */
	@Test public void testMultiWithTwoSort() throws IOException {
		Root root = mapper.readValue(new URL("http://localhost:8282/" +
				"mechanical-pencils-or/empty/" +
				"Black/Blue/Red/" +
				"WWWsb-sN+/"), Root.class);

		assertFalse(root.error);
		Facet[] facets = root.panl.active.facet;
		assertEquals(3, facets.length);

		Sort[] sorts = root.panl.active.sort;
		// sorting is always in order... of the selected

		failOnIncorrectUrlEnding(sorts[0].inverse_uri, "/WWWsb+sN+/");
		failOnIncorrectUrlEnding(sorts[0].remove_uri, "/WWWsN+/");
		failOnIncorrectUrlEnding(sorts[1].inverse_uri, "/WWWsb-sN-/");
		failOnIncorrectUrlEnding(sorts[1].remove_uri, "/WWWsb-/");
	}

	/**
	 * Test the following
	 * <ul>
	 *   <li>Multivalued REGULAR facet</li>
	 *   <li><strong>WITHOUT</strong> multivalued separator</li>
	 *   <li>With <strong>TWO</strong> sorting options</li>
	 * </ul>
	 *
	 * @throws IOException if something went wrong
	 */
	@Test public void testMultiWithTwoSortAndOperand() throws IOException {
		Root root = mapper.readValue(new URL("http://localhost:8282/" +
				"mechanical-pencils-or/empty/" +
				"Black/Blue/Red/" +
				"WWWsb-sN+o+/"), Root.class);

		assertFalse(root.error);
		Facet[] facets = root.panl.active.facet;
		assertEquals(3, facets.length);

		Sort[] sorts = root.panl.active.sort;
		// sorting is always in order... of the selected

		failOnIncorrectUrlEnding(root.panl.active.query_operand.remove_uri, "/WWWsb-sN+/");

		failOnIncorrectUrlEnding(sorts[0].inverse_uri, "/WWWsb+sN+o+/");
		failOnIncorrectUrlEnding(sorts[0].remove_uri, "/WWWsN+o+/");
		failOnIncorrectUrlEnding(sorts[1].inverse_uri, "/WWWsb-sN-o+/");
		failOnIncorrectUrlEnding(sorts[1].remove_uri, "/WWWsb-o+/");
	}

	@Test public void testMultiWithBoolean() throws IOException {
		Root root = mapper.readValue(new URL("http://localhost:8282/" +
				"mechanical-pencils-or/empty/" +
				"Black/Blue/Red/false/" +
				"WWWI/"), Root.class);

		assertFalse(root.error);
		Facet[] facets = root.panl.active.facet;
		assertEquals(4, facets.length);
		for(Facet facet : facets) {
			if (facet.panl_code.equals("W")) {
				failOnIncorrectUrlEnding(facet.remove_uri, "/WWI/");
			}
			if (facet.panl_code.equals("I")) {
				failOnIncorrectUrlEnding(facet.remove_uri, "/WWW/");
				failOnIncorrectUrlEnding(facet.inverse_uri, "/true/WWWI/");
			}
		}
	}

	@Test public void testMultiSeparatorWithBoolean() throws IOException {
		Root root = mapper.readValue(new URL("http://localhost:8282/" +
				"mechanical-pencils-multi-separator/empty/" +
				"Colours:Black,Blue,Red/false/" +
				"WI/"), Root.class);

		assertFalse(root.error);
		Facet[] facets = root.panl.active.facet;
		assertEquals(4, facets.length);
		for(Facet facet : facets) {
			if (facet.panl_code.equals("W")) {
				failOnIncorrectUrlEnding(facet.remove_uri, "/WI/");
			}
			if (facet.panl_code.equals("I")) {
				failOnIncorrectUrlEnding(facet.remove_uri, "/W/");
				failOnIncorrectUrlEnding(facet.inverse_uri, "/true/WI/");
			}
		}
	}

	@Test public void testMultiSeparatorWithAddition() throws Exception {
		Root root = mapper.readValue(new URL("http://localhost:8282/" +
				"mechanical-pencils-multi-separator/empty" +
				"/Colours:Black,Blue,Red" +
				"/W/"), Root.class);

		assertFalse(root.error);

		Facet[] facets = root.panl.active.facet;
		assertEquals(3, facets.length);

		// add a boolean facet
		panl.response.panl.available.Facet colours = findAvailableFacetByFieldName(root.panl.available.facets, "colours");
		String encodedMulti = colours.values[0].encoded_multi;
		String uri = colours.uris.before + encodedMulti + colours.uris.after;
		assertTrue(uri.contains(encodedMulti));

		failOnIncorrectUrlEnding(uri, "/W/");

		root = mapper.readValue(new URL("http://localhost:8282/" +
				"mechanical-pencils-multi-separator/empty" + uri), Root.class);

		assertFalse(root.error);

		assertEquals(4, root.panl.active.facet.length);
		failOnIncorrectUrlEnding(uri, "/W/");
	}

	@Test public void testMultiSeparatorRemoveAll() throws Exception {
		Root root = mapper.readValue(new URL("http://localhost:8282/" +
				"mechanical-pencils-multi-separator/empty" +
				"/Colours:Black,Blue,Red,Green" +
				"/W/"), Root.class);

		assertFalse(root.error);

		while(root.panl.active.facet.length > 2) {
			String checkUri = root.panl.active.facet[0].remove_uri;

			root = mapper.readValue(new URL("http://localhost:8282/" +
					"mechanical-pencils-multi-separator/empty" +
					checkUri), Root.class);

			assertFalse(root.error);

			for(Facet facet: root.panl.active.facet) {
				failOnIncorrectUrlStarting(facet.remove_uri, "/Colours:");
				failOnIncorrectUrlEnding(facet.remove_uri, "/W/");
			}
		}

	}

}
