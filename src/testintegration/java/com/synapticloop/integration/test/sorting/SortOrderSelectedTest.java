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

package com.synapticloop.integration.test.sorting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapticloop.integration.BeforeAllExtension;
import org.junit.jupiter.api.Test;
import panl.Root;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({BeforeAllExtension.class})
public class SortOrderSelectedTest {
	@Test public void testMultiSort() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Root root = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils/brandandname/sb-/"), Root.class);
		assertFalse(root.error);
		assertNull(root.panl.sorting.fields[0].add_uri_asc);
		assertNull(root.panl.sorting.fields[0].add_uri_desc);
		assertEquals("/sb+/", root.panl.sorting.fields[0].set_uri_asc);
		assertEquals("/sb-/", root.panl.sorting.fields[0].set_uri_desc);

		// now for the name sorting
		assertEquals("/sb-sN+/", root.panl.sorting.fields[1].add_uri_asc);
		assertEquals("/sb-sN-/", root.panl.sorting.fields[1].add_uri_desc);
		assertEquals("/sN+/", root.panl.sorting.fields[1].set_uri_asc);
		assertEquals("/sN-/", root.panl.sorting.fields[1].set_uri_desc);
	}

	@Test public void testRangeFacetSelected() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Root root = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils/brandandname/weighing%20from%2017%20grams%20to%2039%20grams/w-/"), Root.class);
		assertFalse(root.error);
		assertEquals("/weighing%20from%2017%20grams%20to%2039%20grams/w-sb+/", root.panl.sorting.fields[0].add_uri_asc);
		assertEquals("/weighing%20from%2017%20grams%20to%2039%20grams/w-sb-/", root.panl.sorting.fields[0].add_uri_desc);
		assertEquals("/weighing%20from%2017%20grams%20to%2039%20grams/w-sb+/", root.panl.sorting.fields[0].set_uri_asc);
		assertEquals("/weighing%20from%2017%20grams%20to%2039%20grams/w-sb-/", root.panl.sorting.fields[0].set_uri_desc);
	}

	@Test public void testRegularFacetSelected() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Root root = mapper.readValue(new URL("http://localhost:8282/mechanical-pencils/brandandname/Manufactured%20by%20Pacific%20Arc%20Company/b/"), Root.class);
		assertFalse(root.error);
		assertEquals("/Manufactured%20by%20Pacific%20Arc%20Company/bsb+/", root.panl.sorting.fields[0].add_uri_asc);
		assertEquals("/Manufactured%20by%20Pacific%20Arc%20Company/bsb-/", root.panl.sorting.fields[0].add_uri_desc);
		assertEquals("/Manufactured%20by%20Pacific%20Arc%20Company/bsb+/", root.panl.sorting.fields[0].set_uri_asc);
		assertEquals("/Manufactured%20by%20Pacific%20Arc%20Company/bsb-/", root.panl.sorting.fields[0].set_uri_desc);
	}
}
