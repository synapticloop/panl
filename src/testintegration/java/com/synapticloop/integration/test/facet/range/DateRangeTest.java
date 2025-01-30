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

import com.synapticloop.integration.test.facet.TestBase;
import org.junit.jupiter.api.Test;
import panl.Root;
import panl.response.panl.available.DateRangeFacet;
import panl.response.panl.available.RangeFacet;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DateRangeTest extends TestBase {
	public static final String BASE_URL = "http://localhost:8282/simple-date/empty";

	@Test public void testDefault() throws Exception {
		Root root = mapper.readValue(new URL(BASE_URL), Root.class);
		assertFalse(root.error);
		assertEquals(55L, root.response.numFound);

		// now test the adding and removing - there is only one
		DateRangeFacet dateRangeFacet = root.panl.available.date_range_facets[0];

	}


	@Test public void testSortDefault() throws Exception {
		Root root = mapper.readValue(new URL("http://localhost:8282/simple-date/empty/next+10+months/SsS-/"), Root.class);
		assertFalse(root.error);
	}

	@Test public void testSortHierarchy() throws Exception {
		Root root = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils-multi-separator/empty/" +
				"weighing+from+15+grams+to+42+grams/w-sb+sN+/"), Root.class);
		assertFalse(root.error);

		assertEquals("/weighing+from+15+grams+to+42+grams/w-/", root.panl.sorting.remove_uri);
		assertEquals("/weighing+from+15+grams+to+42+grams/w-sN+/", root.panl.active.sort[0].remove_uri);
		assertEquals("/weighing+from+15+grams+to+42+grams/w-sb-sN+/", root.panl.active.sort[0].inverse_uri);
		assertEquals("/weighing+from+15+grams+to+42+grams/w-sb+/", root.panl.active.sort[1].remove_uri);
		assertEquals("/weighing+from+15+grams+to+42+grams/w-sb+sN-/", root.panl.active.sort[1].inverse_uri);
	}

}
