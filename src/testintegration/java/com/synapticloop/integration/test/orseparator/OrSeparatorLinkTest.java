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

package com.synapticloop.integration.test.orseparator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapticloop.integration.BeforeAllExtension;
import com.synapticloop.integration.interactor.InteractorException;
import com.synapticloop.integration.interactor.PanlInteractor;
import com.synapticloop.integration.test.util.Helper;
import panl.Root;
import panl.response.panl.available.Facet;
import panl.response.panl.available.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({BeforeAllExtension.class})
public class OrSeparatorLinkTest {
	ObjectMapper mapper = new ObjectMapper();
	private static final String BASE_URL = "http://localhost:8282/mechanical-pencils-or-separator/brandandname";

	@Test public void testAdditionOfLInks() throws IOException, InteractorException {
		PanlInteractor panlInteractor = new PanlInteractor(BASE_URL);
		panlInteractor.addFirstFacetValueByName("brand");
		panlInteractor.addFirstFacetValueByName("brand");
		panlInteractor.addFirstFacetValueByName("brand");
	}

	@Test public void testLinks() throws Exception {
		Root root = mapper.readValue(
				new URL(BASE_URL),
				Root.class);

		List<String> urlsToTest = new ArrayList<>();

		// now we are going to go through all  add urls test can be found and test them out
		for (Facet facet : root.panl.available.facets) {
			String before = facet.uris.before;
			String after = facet.uris.after;
			for (Value value : facet.values) {
				if(null != value.encoded_multi && facet.value_separator != null) {
					urlsToTest.add(BASE_URL + before + value.encoded_multi + after);
				} else {
					urlsToTest.add(BASE_URL + before + value.encoded + after);
				}
			}
		}

		// now go through the links and test them
		for (String url : urlsToTest) {
			testIndividualLink(url);
		}
	}

	private void testIndividualLink(String link) throws IOException {
		Root root = mapper.readValue(
				new URL(link),
				Root.class);
	}
}
