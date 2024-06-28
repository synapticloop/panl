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

package pagination;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import response.json.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class PaginationTest {
	@Test public void testDefaultPagination() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Response response = mapper.readValue(new URL("http://localhost:8181/mechanical-pencils/brandandname/"), Response.class);
		assertFalse(response.error);

		assertEquals("/page-1/10-per-page/pn/", response.panl.canonical_uri);
		assertEquals("q", response.panl.query_respond_to);
		assertEquals(6, response.panl.pagination.num_pages);
		assertEquals(55, response.panl.pagination.num_results);
		assertEquals(10, response.panl.pagination.num_per_page);
		assertEquals(1, response.panl.pagination.page_num);
		assertNull(response.panl.pagination.page_uris.previous);
		assertEquals("/page-2/p/", response.panl.pagination.page_uris.next);
		assertEquals("/page-", response.panl.pagination.page_uris.before);
		assertEquals("/p/", response.panl.pagination.page_uris.after);
		assertTrue(response.panl.pagination.num_results_exact);
	}

	@Test public void testPage2Pagination() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Response response = mapper.readValue(new URL("http://localhost:8181/mechanical-pencils/brandandname/page-2/p/"), Response.class);
		assertFalse(response.error);

		assertEquals("/page-2/10-per-page/pn/", response.panl.canonical_uri);
		assertEquals("q", response.panl.query_respond_to);
		assertEquals(6, response.panl.pagination.num_pages);
		assertEquals(55, response.panl.pagination.num_results);
		assertEquals(10, response.panl.pagination.num_per_page);
		assertEquals(2, response.panl.pagination.page_num);

		assertEquals("/page-3/p/", response.panl.pagination.page_uris.next);
		assertEquals("/page-1/p/", response.panl.pagination.page_uris.previous);
		assertEquals("/page-", response.panl.pagination.page_uris.before);
		assertEquals("/p/", response.panl.pagination.page_uris.after);
		assertTrue(response.panl.pagination.num_results_exact);
	}

	@Test public void testLastPagePagination() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Response response = mapper.readValue(new URL("http://localhost:8181/mechanical-pencils/brandandname/page-6/p/"), Response.class);
		assertFalse(response.error);

		assertEquals("/page-6/10-per-page/pn/", response.panl.canonical_uri);
		assertEquals("q", response.panl.query_respond_to);
		assertEquals(6, response.panl.pagination.num_pages);
		assertEquals(55, response.panl.pagination.num_results);
		assertEquals(10, response.panl.pagination.num_per_page);
		assertEquals(6, response.panl.pagination.page_num);

		assertNull(response.panl.pagination.page_uris.next);
		assertEquals("/page-5/p/", response.panl.pagination.page_uris.previous);
		assertEquals("/page-", response.panl.pagination.page_uris.before);
		assertEquals("/p/", response.panl.pagination.page_uris.after);
		assertTrue(response.panl.pagination.num_results_exact);
	}

	@Test public void testResetPageNumberOnAddFacet() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Response response = mapper.readValue(new URL("http://localhost:8181/mechanical-pencils/brandandname/Clutch/m/"), Response.class);
		assertFalse(response.error);

		assertEquals("/Clutch/page-1/10-per-page/mpn/", response.panl.canonical_uri);
		assertEquals("q", response.panl.query_respond_to);
		assertEquals(3, response.panl.pagination.num_pages);
		assertEquals(30, response.panl.pagination.num_results);
		assertEquals(10, response.panl.pagination.num_per_page);
		assertEquals(1, response.panl.pagination.page_num);
	}
}
