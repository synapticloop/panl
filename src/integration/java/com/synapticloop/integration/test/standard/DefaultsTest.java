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

package com.synapticloop.integration.test.standard;import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapticloop.integration.BeforeAllExtension;
import org.junit.jupiter.api.Test;
import com.synapticloop.integration.response.json.Root;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({BeforeAllExtension.class})
public class DefaultsTest {
	@Test public void testDefaultMechanicalPencils() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Root root = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils/brandandname/"), Root.class);
		assertFalse(root.error);
		assertEquals("/page-1/10-per-page/pn/", root.panl.canonical_uri);
		assertEquals("search", root.panl.query_respond_to);
		assertEquals(6, root.panl.pagination.num_pages);
		assertEquals(55, root.panl.pagination.num_results);
		assertEquals(10, root.panl.pagination.num_per_page);
		assertEquals(1, root.panl.pagination.page_num);
		assertTrue(root.panl.pagination.num_results_exact);

		// now for the uris
		assertEquals("/p/", root.panl.pagination.page_uris.after);
		assertEquals("/page-", root.panl.pagination.page_uris.before);
		assertEquals("/page-2/p/", root.panl.pagination.page_uris.next);

		assertEquals("/", root.panl.pagination.num_per_page_uris.before);
		assertEquals("-per-page/n/", root.panl.pagination.num_per_page_uris.after);


		// timings
		assertTrue(root.panl.timings.panl_total_time >= 0);
		assertTrue(root.panl.timings.panl_parse_request_time >= 0);
		assertTrue(root.panl.timings.panl_build_request_time >= 0);
		assertTrue(root.panl.timings.panl_send_request_time >= 0);
		assertTrue(root.panl.timings.panl_build_response_time >= 0);

		// query_operand
		assertEquals("/o+/", root.panl.query_operand.AND);
		assertEquals("/o-/", root.panl.query_operand.OR);

		// sorting
		assertEquals("/", root.panl.sorting.remove_uri);
		assertEquals("/sb-/", root.panl.sorting.fields[0].add_uri_desc);
		assertEquals("Brand", root.panl.sorting.fields[0].name);
		assertEquals("brand", root.panl.sorting.fields[0].facet_name);
		assertEquals("/sb+/", root.panl.sorting.fields[0].add_uri_asc);
		assertEquals("/sb-/", root.panl.sorting.fields[0].set_uri_desc);
		assertEquals("/sb+/", root.panl.sorting.fields[0].set_uri_asc);

		assertEquals("/sN-/", root.panl.sorting.fields[1].add_uri_desc);
		assertEquals("Pencil Model", root.panl.sorting.fields[1].name);
		assertEquals("name", root.panl.sorting.fields[1].facet_name);
		assertEquals("/sN+/", root.panl.sorting.fields[1].add_uri_asc);
		assertEquals("/sN-/", root.panl.sorting.fields[1].set_uri_desc);
		assertEquals("/sN+/", root.panl.sorting.fields[1].set_uri_asc);

		// ranges
		assertEquals("10", root.panl.available.range_facets[0].min);
		assertEquals("50", root.panl.available.range_facets[0].max);
		assertEquals("", root.panl.available.range_facets[0].prefix);
		assertEquals("from+light", root.panl.available.range_facets[0].range_min_value);
		assertEquals("weight", root.panl.available.range_facets[0].facet_name);
		assertEquals("Weight", root.panl.available.range_facets[0].name);
		assertEquals("w", root.panl.available.range_facets[0].panl_code);
		assertEquals("+grams", root.panl.available.range_facets[0].suffix);
		assertEquals("heavy+pencils", root.panl.available.range_facets[0].range_max_value);

		assertEquals("/weighing+from+", root.panl.available.range_facets[0].uris.before);
		assertEquals("/from+light", root.panl.available.range_facets[0].uris.before_min_value);
		assertTrue(root.panl.available.range_facets[0].uris.has_infix);
		assertEquals("heavy+pencils/w-/", root.panl.available.range_facets[0].uris.after_max_value);
		assertEquals("+to+", root.panl.available.range_facets[0].uris.during);
		assertEquals("+grams/w-/", root.panl.available.range_facets[0].uris.after);
	}
}
