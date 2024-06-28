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

package sorting;

import com.fasterxml.jackson.databind.ObjectMapper;
import response.json.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class SortOrderSelectedTest {
	@Test public void testMultiSort() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Response response = mapper.readValue(new URL("http://localhost:8181/mechanical-pencils/brandandname/sb-/"), Response.class);
		assertFalse(response.error);
		assertNull(response.panl.sorting.fields[0].add_uri_asc);
		assertNull(response.panl.sorting.fields[0].add_uri_desc);
		assertEquals("/sb+/", response.panl.sorting.fields[0].set_uri_asc);
		assertEquals("/sb-/", response.panl.sorting.fields[0].set_uri_desc);

		// now for the name sorting
		assertEquals("/sb-sN+/", response.panl.sorting.fields[1].add_uri_asc);
		assertEquals("/sb-sN-/", response.panl.sorting.fields[1].add_uri_desc);
		assertEquals("/sN+/", response.panl.sorting.fields[1].set_uri_asc);
		assertEquals("/sN-/", response.panl.sorting.fields[1].set_uri_desc);
	}

	@Test public void testRangeFacetSelected() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Response response = mapper.readValue(new URL("http://localhost:8181/mechanical-pencils/brandandname/weighing+from+17+grams+to+39+grams/w-w/"), Response.class);
		assertFalse(response.error);
		assertEquals("/weighing+from+17+grams+to+39+grams/w-wsb+/", response.panl.sorting.fields[0].add_uri_asc);
		assertEquals("/weighing+from+17+grams+to+39+grams/w-wsb-/", response.panl.sorting.fields[0].add_uri_desc);
		assertEquals("/weighing+from+17+grams+to+39+grams/w-wsb+/", response.panl.sorting.fields[0].set_uri_asc);
		assertEquals("/weighing+from+17+grams+to+39+grams/w-wsb+/", response.panl.sorting.fields[0].set_uri_desc);
	}

	@Test public void testRegularFacetSelected() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Response response = mapper.readValue(new URL("http://localhost:8181/mechanical-pencils/brandandname/Manufactured+by+Pacific+Arc+Company/b/"), Response.class);
		assertFalse(response.error);
		assertEquals("/Manufactured+by+Pacific+Arc+Company/bsb+/", response.panl.sorting.fields[0].add_uri_asc);
		assertEquals("/Manufactured+by+Pacific+Arc+Company/bsb-/", response.panl.sorting.fields[0].add_uri_desc);
		assertEquals("/Manufactured+by+Pacific+Arc+Company/bsb+/", response.panl.sorting.fields[0].set_uri_asc);
		assertEquals("/Manufactured+by+Pacific+Arc+Company/bsb-/", response.panl.sorting.fields[0].set_uri_desc);
	}
}
