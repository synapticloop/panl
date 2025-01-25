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

package com.synapticloop.integration.response.json.response.panl.active;

public class Facet {
	public String remove_uri;
	public String facet_name;
	public String name;
	public String panl_code;
	public String value;
	public String encoded;
	public String inverse_uri;
	public String value_to;
	public boolean is_range_facet = false;
	public boolean is_boolean_facet = false;
	public boolean is_or_facet = false;
	public boolean has_infix = false;
	public String inverse_encoded;

	/**
	 * <p>Return the remove_uri.</p>
	 *
	 * @return the remove_uri
	 */
	public String getRemove_uri() {
		return remove_uri;
	}
	/**
	 * <p>Set the remove_uri</p>
	 *
	 * @param remove_uri the remove_uri to set
	 */
	public void setRemove_uri(
			String remove_uri) {
		this.remove_uri = remove_uri;
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
	 * <p>Return the value.</p>
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * <p>Set the value</p>
	 *
	 * @param value the value to set
	 */
	public void setValue(
			String value) {
		this.value = value;
	}
	/**
	 * <p>Return the encoded.</p>
	 *
	 * @return the encoded
	 */
	public String getEncoded() {
		return encoded;
	}
	/**
	 * <p>Set the encoded</p>
	 *
	 * @param encoded the encoded to set
	 */
	public void setEncoded(
			String encoded) {
		this.encoded = encoded;
	}
	/**
	 * <p>Return the inverse_uri.</p>
	 *
	 * @return the inverse_uri
	 */
	public String getInverse_uri() {
		return inverse_uri;
	}
	/**
	 * <p>Set the inverse_uri</p>
	 *
	 * @param inverse_uri the inverse_uri to set
	 */
	public void setInverse_uri(
			String inverse_uri) {
		this.inverse_uri = inverse_uri;
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
	 * <p>Return the inverse_encoded.</p>
	 *
	 * @return the inverse_encoded
	 */
	public String getInverse_encoded() {
		return inverse_encoded;
	}
	/**
	 * <p>Set the inverse_encoded</p>
	 *
	 * @param inverse_encoded the inverse_encoded to set
	 */
	public void setInverse_encoded(
			String inverse_encoded) {
		this.inverse_encoded = inverse_encoded;
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

	public String getValue_to() {
		return value_to;
	}

	public void setValue_to(String value_to) {
		this.value_to = value_to;
	}

	public boolean isIs_or_facet() {
		return is_or_facet;
	}

	public void setIs_or_facet(boolean is_or_facet) {
		this.is_or_facet = is_or_facet;
	}

	public boolean isHas_infix() {
		return has_infix;
	}

	public void setHas_infix(boolean has_infix) {
		this.has_infix = has_infix;
	}
}
