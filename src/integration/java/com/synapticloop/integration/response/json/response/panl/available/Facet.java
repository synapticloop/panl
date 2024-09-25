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

package com.synapticloop.integration.response.json.response.panl.available;

public class Facet {
	public Uri uris;
	public Value[] values;
	public String facet_name;
	public String name;
	public String panl_code;
	public boolean is_boolean_facet;
	public boolean is_range_facet;
	public boolean is_date_facet;
	public boolean is_multivalue;
	public boolean facet_limit;

	/**
	 * <p>Return the uris.</p>
	 *
	 * @return the uris
	 */
	public Uri getUris() {
		return uris;
	}
	/**
	 * <p>Set the uris</p>
	 *
	 * @param uris the uris to set
	 */
	public void setUris(
			Uri uris) {
		this.uris = uris;
	}
	/**
	 * <p>Return the values.</p>
	 *
	 * @return the values
	 */
	public Value[] getValues() {
		return values;
	}
	/**
	 * <p>Set the values</p>
	 *
	 * @param values the values to set
	 */
	public void setValues(
			Value[] values) {
		this.values = values;
	}
	/**
	 * <p>Return the facet_name.</p>
	 *
	 * @return the facet_name
	 */
	public String getFacet_name() {
		return facet_name;
	}
	/**
	 * <p>Set the facet_name</p>
	 *
	 * @param facet_name the facet_name to set
	 */
	public void setFacet_name(
			String facet_name) {
		this.facet_name = facet_name;
	}
	/**
	 * <p>Return the name.</p>
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * <p>Set the name</p>
	 *
	 * @param name the name to set
	 */
	public void setName(
			String name) {
		this.name = name;
	}
	/**
	 * <p>Return the panl_code.</p>
	 *
	 * @return the panl_code
	 */
	public String getPanl_code() {
		return panl_code;
	}
	/**
	 * <p>Set the panl_code</p>
	 *
	 * @param panl_code the panl_code to set
	 */
	public void setPanl_code(
			String panl_code) {
		this.panl_code = panl_code;
	}
	/**
	 * <p>Return the is_boolean_facet.</p>
	 *
	 * @return the is_boolean_facet
	 */
	public boolean isIs_boolean_facet() {
		return is_boolean_facet;
	}
	/**
	 * <p>Set the is_boolean_facet</p>
	 *
	 * @param is_boolean_facet the is_boolean_facet to set
	 */
	public void setIs_boolean_facet(
			boolean is_boolean_facet) {
		this.is_boolean_facet = is_boolean_facet;
	}
	/**
	 * <p>Return the is_range_facet.</p>
	 *
	 * @return the is_range_facet
	 */
	public boolean isIs_range_facet() {
		return is_range_facet;
	}
	/**
	 * <p>Set the is_range_facet</p>
	 *
	 * @param is_range_facet the is_range_facet to set
	 */
	public void setIs_range_facet(
			boolean is_range_facet) {
		this.is_range_facet = is_range_facet;
	}
	/**
	 * <p>Return the is_date_facet.</p>
	 *
	 * @return the is_date_facet
	 */
	public boolean isIs_date_facet() {
		return is_date_facet;
	}
	/**
	 * <p>Set the is_date_facet</p>
	 *
	 * @param is_date_facet the is_date_facet to set
	 */
	public void setIs_date_facet(
			boolean is_date_facet) {
		this.is_date_facet = is_date_facet;
	}
	
	
}
