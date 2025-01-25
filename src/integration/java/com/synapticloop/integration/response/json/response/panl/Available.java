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

package com.synapticloop.integration.response.json.response.panl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.synapticloop.integration.response.json.response.panl.available.Facet;
import com.synapticloop.integration.response.json.response.panl.available.RangeFacet;

public class Available {
	public RangeFacet[] range_facets;
	public Facet[] facets;
	@JsonIgnore public Object[] date_range_facets;
	/**
	 * <p>Return the range_facets.</p>
	 *
	 * @return the range_facets
	 */
	public RangeFacet[] getRange_facets() {
		return range_facets;
	}
	/**
	 * <p>Set the range_facets</p>
	 *
	 * @param range_facets the range_facets to set
	 */
	public void setRange_facets(
			RangeFacet[] range_facets) {
		this.range_facets = range_facets;
	}
	/**
	 * <p>Return the facets.</p>
	 *
	 * @return the facets
	 */
	public Facet[] getFacets() {
		return facets;
	}
	/**
	 * <p>Set the facets</p>
	 *
	 * @param facets the facets to set
	 */
	public void setFacets(
			Facet[] facets) {
		this.facets = facets;
	}
	/**
	 * <p>Return the date_range_facets.</p>
	 *
	 * @return the date_range_facets
	 */
	public Object[] getDate_range_facets() {
		return date_range_facets;
	}
	/**
	 * <p>Set the date_range_facets</p>
	 *
	 * @param date_range_facets the date_range_facets to set
	 */
	public void setDate_range_facets(
			Object[] date_range_facets) {
		this.date_range_facets = date_range_facets;
	}
	
	
}
