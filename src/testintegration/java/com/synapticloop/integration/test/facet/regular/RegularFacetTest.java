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
import com.synapticloop.integration.test.facet.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import panl.Root;
import panl.response.panl.Sort;
import panl.response.panl.active.Facet;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({BeforeAllExtension.class})
public class RegularFacetTest extends TestBase {

	@Test public void testDefault() throws Exception {

	}

	@Test public void testSortDefault() throws Exception {

	}

	@Test public void testSortHierarchy() throws Exception {

	}

	@Test public void testPagination() throws Exception {
		Root root = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils/empty" +
				"/Manufactured%20by%20Koh-i-Noor%20Company/page-2/3-per-page/bpn/"), Root.class);
		assertFalse(root.error);
		// ensure that page number is reset
		assertEquals("/3-per-page/bNn/",
				root.panl.available.facets[0].uris.after);

		assertEquals("/Manufactured%20by%20Koh-i-Noor%20Company/page-3/3-per-page/bpn/",
				root.panl.pagination.page_uris.next);

		assertEquals("/Manufactured%20by%20Koh-i-Noor%20Company/page-1/3-per-page/bpn/",
				root.panl.pagination.page_uris.previous);

		assertEquals("/Manufactured%20by%20Koh-i-Noor%20Company/page-",
				root.panl.pagination.page_uris.before);

		assertEquals("/3-per-page/bpn/",
				root.panl.pagination.page_uris.after);
	}
}
