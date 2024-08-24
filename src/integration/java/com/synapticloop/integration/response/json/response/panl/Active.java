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

package com.synapticloop.integration.response.json.response.panl;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.synapticloop.integration.response.json.response.panl.active.Facet;

public class Active {
	public Facet[] facet;
	@JsonIgnore public Object page;
	@JsonIgnore public Object numrows;
	@JsonIgnore public Object sort_fields;
	@JsonIgnore public Object sort;

	/**
	 * <p>Return the facet.</p>
	 *
	 * @return the facet
	 */
	public Facet[] getFacet() {
		return facet;
	}

	/**
	 * <p>Set the facet</p>
	 *
	 * @param facet the facet to set
	 */
	public void setFacet(
			Facet[] facet) {
		this.facet = facet;
	}
	
	
}
