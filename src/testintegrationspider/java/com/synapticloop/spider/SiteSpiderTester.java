package com.synapticloop.spider;

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
import panl.Root;
import panl.response.panl.available.Facet;
import panl.response.panl.available.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@ExtendWith({BeforeAllExtension.class})
public class SiteSpiderTester {
	ObjectMapper mapper = new ObjectMapper();
	private static final String[] URLS = {
			"http://localhost:8282/mechanical-pencils-or-separator/empty",
			"http://localhost:8282/mechanical-pencils-or/empty",
			"http://localhost:8282/mechanical-pencils/empty",
			"http://localhost:8282/mechanical-pencils-more/empty",
			"http://localhost:8282/mechanical-pencils-multi-separator/empty"
	};

	private Map<String, String> firstLevelLinks = new HashMap<>();
	private Map<String, String> secondLevelLinks = new HashMap<>();

	@Test public void generateLinks() throws Exception {
		for (String url : URLS) {
			Root root = mapper.readValue(
					new URL(url),
					Root.class);

			getLinks(url, root);
		}

		for (String firstLevelLink : firstLevelLinks.keySet()) {
			testIndividualLink(firstLevelLink, firstLevelLinks.get(firstLevelLink), true);
		}

		for (String secondLevelLink : secondLevelLinks.keySet()) {
			testIndividualLink(secondLevelLink, secondLevelLinks.get(secondLevelLink), false);
		}

	}

	private void getLinks(String url, Root root) {
		for (Facet facet : root.panl.available.facets) {
			String before = facet.uris.before;
			String after = facet.uris.after;
			for (Value value : facet.values) {
				if (null != value.getEncoded_multi()) {
					firstLevelLinks.put(url + before + value.getEncoded_multi() + after, url);
				} else {
					firstLevelLinks.put(url + before + value.getEncoded() + after, url);
				}
			}
		}
	}

	private void getSecondLevelLinks(String parent, String url, Root root) {
		for (Facet facet : root.panl.available.facets) {
			String before = facet.uris.before;
			String after = facet.uris.after;
			for (Value value : facet.values) {
				if (null != value.getEncoded_multi()) {
					secondLevelLinks.put(url + before + value.getEncoded_multi() + after, parent);
				} else {
					secondLevelLinks.put(url + before + value.getEncoded() + after, parent);
				}
			}
		}
	}

	private void testIndividualLink(String link, String parentLink, boolean recurse) throws IOException {
		Root root = mapper.readValue(
				new URL(link),
				Root.class);

		if (recurse) {
			getSecondLevelLinks(link, link.substring(0, link.indexOf("empty/") + 5), root);
		}

		// now test for invalid links...

		String replace = link.replace(":8282/", ":8282/panl-results-explainer/explain/");

		root = mapper.readValue(new URL(replace), Root.class);
		for (String explanation : root.explanation) {
			if (explanation.contains("INVALID")) {
				fail(
						"LINK\n\t" +
								link +
								"\nfailed with invalid explanation:\n\t" +
								explanation +
								"\nLINK came from parent link:\n\t" +
								parentLink +
								"\n"
				);
			}

		}

	}

}
