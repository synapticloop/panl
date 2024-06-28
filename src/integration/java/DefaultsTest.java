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

import com.fasterxml.jackson.databind.ObjectMapper;
import json.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultsTest {
	@Test public void testDefaultMechanicalPencils() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Response response = mapper.readValue(new URL("http://localhost:8181/mechanical-pencils/brandandname/"), Response.class);
		assertFalse(response.error);
		assertEquals("/page-1/10-per-page/pn/", response.panl.canonical_uri);
		assertEquals("q", response.panl.query_respond_to);
		assertEquals(6, response.panl.pagination.num_pages);
		assertEquals(55, response.panl.pagination.num_results);
		assertEquals(10, response.panl.pagination.num_per_page);
		assertEquals(1, response.panl.pagination.page_num);
		assertEquals(true, response.panl.pagination.num_results_exact);

		// now for the uris
		assertEquals("/p/", response.panl.pagination.page_uris.after);
		assertEquals("/page-", response.panl.pagination.page_uris.before);
		assertEquals("/page-2/p/", response.panl.pagination.page_uris.next);

		assertEquals("/", response.panl.pagination.num_per_page_uris.before);
		assertEquals("-per-page/n/", response.panl.pagination.num_per_page_uris.after);


		// timings
		assertTrue(response.panl.timings.panl_total_time >= 0);
		assertTrue(response.panl.timings.panl_parse_request_time >= 0);
		assertTrue(response.panl.timings.panl_build_request_time >= 0);
		assertTrue(response.panl.timings.panl_send_request_time >= 0);
		assertTrue(response.panl.timings.panl_build_response_time >= 0);

		// query_operand
		assertEquals("/o+/", response.panl.query_operand.AND);
		assertEquals("/o-/", response.panl.query_operand.OR);

		// sorting
		assertEquals("/", response.panl.sorting.remove_uri);
		assertEquals("/sb-/", response.panl.sorting.fields[0].add_uri_desc);
		assertEquals("Brand", response.panl.sorting.fields[0].name);
		assertEquals("brand", response.panl.sorting.fields[0].facet_name);
		assertEquals("/sb+/", response.panl.sorting.fields[0].add_uri_asc);
		assertEquals("/sb-/", response.panl.sorting.fields[0].set_uri_desc);
		assertEquals("/sb+/", response.panl.sorting.fields[0].set_uri_asc);

		assertEquals("/sN-/", response.panl.sorting.fields[1].add_uri_desc);
		assertEquals("Name", response.panl.sorting.fields[1].name);
		assertEquals("name", response.panl.sorting.fields[1].facet_name);
		assertEquals("/sN+/", response.panl.sorting.fields[1].add_uri_asc);
		assertEquals("/sN-/", response.panl.sorting.fields[1].set_uri_desc);
		assertEquals("/sN+/", response.panl.sorting.fields[1].set_uri_asc);

		// ranges
		assertEquals("10", response.panl.available.range_facets[0].min);
		assertEquals("50", response.panl.available.range_facets[0].max);
		assertEquals("", response.panl.available.range_facets[0].prefix);
		assertEquals("from+light", response.panl.available.range_facets[0].range_min_value);
		assertEquals("weight", response.panl.available.range_facets[0].facet_name);
		assertEquals("Weight", response.panl.available.range_facets[0].name);
		assertEquals("w", response.panl.available.range_facets[0].panl_code);
		assertEquals("+grams", response.panl.available.range_facets[0].suffix);
		assertEquals("heavy+pencils", response.panl.available.range_facets[0].range_max_value);

		assertEquals("/weighing+from+", response.panl.available.range_facets[0].uris.before);
		assertEquals("/from+light", response.panl.available.range_facets[0].uris.before_min_value);
		assertEquals(true, response.panl.available.range_facets[0].uris.has_infix);
		assertEquals("heavy+pencils/w-w/", response.panl.available.range_facets[0].uris.after_max_value);
		assertEquals("+to+", response.panl.available.range_facets[0].uris.during);
		assertEquals("+grams/w-w/", response.panl.available.range_facets[0].uris.after);

	}
}
