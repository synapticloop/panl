package com.synapticloop.integration.test.unless;

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
import com.synapticloop.integration.test.util.Helper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import panl.Root;
import panl.response.panl.available.Facet;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({BeforeAllExtension.class})
public class UnlessTest {
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * <p>Test whether the unless facets do not appear when other facets are
	 * selected.</p>
	 *
	 * <p>In this example, genre should not appear if LPSE code <code>I</code> or
	 * <code>A</code>, Configuration properties below:</p>
	 *
	 * <pre>
	 *   panl.facet.g=genre
	 *   panl.or.facet.g=true
	 *   panl.name.g=Genre
	 *   panl.type.g=solr.StrField
	 *   panl.unless.g=I,A
	 * </pre>
	 *
	 * @throws IOException if something happens
	 */
	@Test public void testUnlessFields() throws IOException {
		Root root = mapper.readValue(new URL("http://localhost:8282/unless/default/"), Root.class);
		assertFalse(root.error);

		Facet genre = Helper.findAvailableFacetByFieldName(root.panl.available.facets, "genre");
		Facet aToZIndex = Helper.findAvailableFacetByFieldName(root.panl.available.facets, "a_to_z_index");
		Facet isPaperback = Helper.findAvailableFacetByFieldName(root.panl.available.facets, "is_paperback");

		// it should appear
		assertNotNull(genre);
		assertNotNull(aToZIndex);
		assertNotNull(isPaperback);

		root = mapper.readValue(new URL("http://localhost:8282/unless/default/Paperback/I/"), Root.class);
		assertFalse(root.error);
		genre = Helper.findAvailableFacetByFieldName(root.panl.available.facets, "genre");
		aToZIndex = Helper.findAvailableFacetByFieldName(root.panl.available.facets, "a_to_z_index");
		isPaperback = Helper.findAvailableFacetByFieldName(root.panl.available.facets, "is_paperback");

		// it should NOT appear
		assertNull(genre);
		assertNotNull(aToZIndex);
		assertNull(isPaperback);
	}
}
