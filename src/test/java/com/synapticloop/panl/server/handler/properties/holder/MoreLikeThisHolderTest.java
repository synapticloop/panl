package com.synapticloop.panl.server.handler.properties.holder;

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

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.util.Constants;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MoreLikeThisHolderTest {
	private static final Properties PROPERTIES = new Properties();
	private static final SolrFieldHolder solrFieldHolder;
	static {
		PROPERTIES.put("panl.facet.f", "field");
		PROPERTIES.put("panl.facet.a", "another");
		PROPERTIES.put("panl.facet.o", "one");
		PROPERTIES.put("panl.uniquekey.o", "true");

		try {
			solrFieldHolder = new SolrFieldHolder("solr", "panl", PROPERTIES);
		} catch (PanlServerException e) {
			throw new RuntimeException(e);
		}
	}


	@Test public void testNoFields() throws Exception {
		Properties properties = new Properties();
		properties.put(Constants.Property.Panl.PANL_MLT_ENABLE, "true");
		assertThrows(PanlServerException.class, () -> {
			new MoreLikeThisHolder(properties, solrFieldHolder);
		});

		properties.put(Constants.Property.Panl.PANL_MLT_FL, "field,another,one");
		assertDoesNotThrow(() -> {
			new MoreLikeThisHolder(properties, solrFieldHolder);
		});
	}

	@Test public void testMltEnabledProperty() throws PanlServerException {
		Properties properties = new Properties();
		properties.put(Constants.Property.Panl.PANL_MLT_ENABLE, "true");
		properties.put(Constants.Property.Panl.PANL_MLT_FL, "field,another,one");

		MoreLikeThisHolder moreLikeThisHolder = new MoreLikeThisHolder(properties, solrFieldHolder);
		assertTrue(moreLikeThisHolder.getIsMltEnabled());

		properties.put(Constants.Property.Panl.PANL_MLT_ENABLE, "false");

		moreLikeThisHolder = new MoreLikeThisHolder(properties, solrFieldHolder);
		assertFalse(moreLikeThisHolder.getIsMltEnabled());

		properties.put(Constants.Property.Panl.PANL_MLT_ENABLE, "TRUE");

		moreLikeThisHolder = new MoreLikeThisHolder(properties, solrFieldHolder);
		assertFalse(moreLikeThisHolder.getIsMltEnabled());
	}

	@Test public void testDefaults() throws PanlServerException {
		Properties properties = new Properties();
		properties.put(Constants.Property.Panl.PANL_MLT_ENABLE, "true");
		properties.put(Constants.Property.Panl.PANL_MLT_FL, "field,another,one");
		MoreLikeThisHolder moreLikeThisHolder = new MoreLikeThisHolder(properties, solrFieldHolder);

		SolrQuery solrQuery = new SolrQuery();
		moreLikeThisHolder.applyMltToQuery(solrQuery, new ArrayList<String>(), "hello");
		Set<String> fields = new HashSet<>(Arrays.asList(solrQuery.getMoreLikeThisFields()));
		assertTrue(fields.contains("field"));
		assertTrue(fields.contains("another"));
		assertTrue(fields.contains("one"));

		assertEquals(2, solrQuery.getMoreLikeThisMinTermFreq());
		assertEquals(5, solrQuery.getMoreLikeThisMinDocFreq());
		assertEquals("one:hello", solrQuery.getQuery());
	}
}
