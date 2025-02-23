package com.synapticloop.integration.interactor;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapticloop.integration.test.util.Helper;
import panl.Root;
import panl.response.panl.available.Facet;

import java.io.IOException;
import java.net.URL;

public class PanlInteractor {
	private final ObjectMapper mapper = new ObjectMapper();
	private final String baseURL;

	private Root root;

	/**
	 * Instantiate a new PANL interactor
	 *
	 * @param baseURL The baseUrl (including CaFUP, no ending slash)
	 */
	public PanlInteractor(String baseURL) throws InteractorException {
		this.baseURL = baseURL;
		try {
			root = mapper.readValue(new URL(baseURL), Root.class);
		} catch (IOException e) {
			throw new InteractorException("Could not open/parse the JSON response from URL '" + baseURL + "'.");
		}
	}

	public void addFirstFacetValueByName(String facetName) throws InteractorException {
		Facet facet = Helper.findAvailableFacetByFieldName(root.panl.available.facets, facetName);
		if(null == facet) {
			throw new InteractorException("Could not find a facet with name '" + facetName + "'.");
		}

		String before = facet.uris.before;
		String after = facet.uris.after;

		String facetValue;
		if(null != facet.value_separator) {
			facetValue = facet.values[0].encoded_multi;
		} else {
			facetValue = facet.values[0].encoded;
		}

		try {
			root = mapper.readValue(
					new URL(baseURL +
							before +
							facetValue +
							after),
					Root.class
			);
		} catch (IOException e) {
			throw new InteractorException(e.getMessage());
		}

		// now verify that we have added this facet value
		boolean foundActiveFacet = false;
		for (panl.response.panl.active.Facet activeFacet : root.panl.active.facet) {
			if(activeFacet.value.equals(facetValue)) {
				foundActiveFacet = true;
				break;
			}
		}

		if(!foundActiveFacet) {
			throw new InteractorException("Could not find the active add facet with name '" + facetName + "' and value '" + facetValue +"'.");
		}

	}
}
