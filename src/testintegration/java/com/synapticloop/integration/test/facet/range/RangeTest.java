package com.synapticloop.integration.test.facet.range;

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
import com.synapticloop.integration.test.facet.TestBase;
import org.junit.jupiter.api.extension.ExtendWith;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapticloop.integration.BeforeAllExtension;
import panl.Root;
import panl.response.panl.active.Facet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import panl.response.panl.available.RangeFacet;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class RangeTest extends TestBase {
	public static final String BASE_URL = "http://localhost:8282/mechanical-pencils/empty";

	@Test public void testDefault() throws Exception {
		Root root = mapper.readValue(new URL(BASE_URL), Root.class);
		assertFalse(root.error);
		assertEquals(55L, root.response.numFound);

		// now test the adding and removing - there is only one
		RangeFacet rangeFacet = root.panl.available.range_facets[0];
		String before = rangeFacet.uris.before;
		String after = rangeFacet.uris.after;
		String beforeMinValue = rangeFacet.uris.before_min_value;
		String afterMaxValue = rangeFacet.uris.after_max_value;
		String during = rangeFacet.uris.during;

		String rangeMaxValue = rangeFacet.range_max_value;
		String rangeMinValue = rangeFacet.range_min_value;
		int min = Integer.parseInt(rangeFacet.min);
		int max = Integer.parseInt(rangeFacet.max);

		// first up test the min and max value replacement
		root = mapper.readValue(new URL(BASE_URL + beforeMinValue + during + afterMaxValue), Root.class);
		assertFalse(root.error);
		assertEquals(55L, root.response.numFound);
		assertEquals(1, root.panl.active.facet.length);

		assertEquals("from%20light%20to%20heavy%20pencils", root.panl.active.facet[0].encoded);
		assertEquals(min, Integer.parseInt(root.panl.active.facet[0].value));
		assertEquals(max, Integer.parseInt(root.panl.active.facet[0].value_to));

		// Now test to ensure that min and max value replacements are made
		root = mapper.readValue(new URL(BASE_URL + before + min + during + max + after), Root.class);
		assertFalse(root.error);
		assertEquals(55L, root.response.numFound);
		assertEquals(1, root.panl.active.facet.length);

		assertEquals("from%20light%20to%20heavy%20pencils", root.panl.active.facet[0].encoded);
		assertEquals(min, Integer.parseInt(root.panl.active.facet[0].value));
		assertEquals(max, Integer.parseInt(root.panl.active.facet[0].value_to));

		root = mapper.readValue(new URL(BASE_URL + before + (min + 1) + during + (max - 1) + after), Root.class);
		assertFalse(root.error);
		assertEquals(48L, root.response.numFound);
		assertEquals(1, root.panl.active.facet.length);

		assertEquals("weighing%20from%20" + (min + 1) + "%20grams%20to%20" + (max-1) +"%20grams", root.panl.active.facet[0].encoded);
		assertEquals(min + 1, Integer.parseInt(root.panl.active.facet[0].value));
		assertEquals(max -1, Integer.parseInt(root.panl.active.facet[0].value_to));
	}

	/**
	 * <p>Test for a multivalued query that multiple facets selected and has a
	 * range query, removing those multivalued separator facets does not generate
	 * a valid URL</p>
	 * <pre>
	 *   http://localhost:8181/panl-results-viewer/mechanical-pencils-multi-separator/empty/
	 *   Colours%3ABlack%2CSilver/weighing%20from%2015%20grams%20to%2042%20grams/Ww-/
	 * </pre>
	 *
	 *
	 * <p>The test URL:
	 *  <a href="http://localhost:8181/panl-results-viewer/mechanical-pencils-multi-separator/empty/Colours%3ABlack%2CSilver/weighing%20from%2015%20grams%20to%2042%20grams/Ww-/">TEST URL</a>
	 * </p>
	 *
	 * <p>Remove a colour facet - the remove URL is not correct - it adds an extra <code>%20grams</code> to it.</p>
	 *
	 * @throws Exception if something goes wrong
	 */
	@Test public void testRangeMultiValueRemoval() throws Exception {
		Root root = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils-multi-separator/empty/" +
				"Colours:Black,Silver/weighing%20from%2015%20grams%20to%2042%20grams/Ww-/"), Root.class);
		assertFalse(root.error);

		assertEquals("/Colours:Silver/weighing%20from%2015%20grams%20to%2042%20grams/Ww-/",
				root.panl.active.facet[0].remove_uri);
	}

	@Test public void testSortDefault() throws Exception {
		Root root = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils-multi-separator/empty/" +
				"weighing%20from%2015%20grams%20to%2042%20grams/w-sb+/"), Root.class);
		assertFalse(root.error);

		assertEquals("/weighing%20from%2015%20grams%20to%2042%20grams/w-/", root.panl.sorting.remove_uri);
		assertEquals("/weighing%20from%2015%20grams%20to%2042%20grams/w-/", root.panl.active.sort[0].remove_uri);
		assertEquals("/weighing%20from%2015%20grams%20to%2042%20grams/w-sb-/", root.panl.active.sort[0].inverse_uri);

		root = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils-multi-separator/empty/" +
				"Colours%3ABlack%2CSilver/weighing%20from%2015%20grams%20to%2042%20grams/Ww-sb+/"), Root.class);
		assertFalse(root.error);

		assertEquals("/Colours:Black,Silver/weighing%20from%2015%20grams%20to%2042%20grams/Ww-/", root.panl.sorting.remove_uri);
		assertEquals("/Colours:Black,Silver/weighing%20from%2015%20grams%20to%2042%20grams/Ww-/", root.panl.active.sort[0].remove_uri);
		assertEquals("/Colours:Black,Silver/weighing%20from%2015%20grams%20to%2042%20grams/Ww-sb-/", root.panl.active.sort[0].inverse_uri);
	}

	@Test public void testSortHierarchy() throws Exception {
		Root root = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils-multi-separator/empty/" +
				"weighing%20from%2015%20grams%20to%2042%20grams/w-sb+sN+/"), Root.class);
		assertFalse(root.error);

		assertEquals("/weighing%20from%2015%20grams%20to%2042%20grams/w-/", root.panl.sorting.remove_uri);
		assertEquals("/weighing%20from%2015%20grams%20to%2042%20grams/w-sN+/", root.panl.active.sort[0].remove_uri);
		assertEquals("/weighing%20from%2015%20grams%20to%2042%20grams/w-sb-sN+/", root.panl.active.sort[0].inverse_uri);
		assertEquals("/weighing%20from%2015%20grams%20to%2042%20grams/w-sb+/", root.panl.active.sort[1].remove_uri);
		assertEquals("/weighing%20from%2015%20grams%20to%2042%20grams/w-sb+sN-/", root.panl.active.sort[1].inverse_uri);
	}

}
