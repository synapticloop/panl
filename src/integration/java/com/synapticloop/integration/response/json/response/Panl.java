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

package com.synapticloop.integration.response.json.response;

import com.synapticloop.integration.response.json.response.panl.Sorting;
import com.synapticloop.integration.response.json.response.panl.timings.Timings;
import com.synapticloop.integration.response.json.response.panl.Active;
import com.synapticloop.integration.response.json.response.panl.Available;
import com.synapticloop.integration.response.json.response.panl.Pagination;
import com.synapticloop.integration.response.json.response.panl.QueryOperand;

import java.util.Map;

public class Panl {
	public Timings timings;
	public Pagination pagination;
	public QueryOperand query_operand;
	public Sorting sorting;
	public String canonical_uri;
	public String query_respond_to;

	public Available available;
	public Map<String, String> fields;

	public Active active;

	/**
	 * <p>Return the timings.</p>
	 *
	 * @return the timings
	 */
	public Timings getTimings() {
		return timings;
	}

	/**
	 * <p>Set the timings</p>
	 *
	 * @param timings the timings to set
	 */
	public void setTimings(
			Timings timings) {
		this.timings = timings;
	}

	/**
	 * <p>Return the pagination.</p>
	 *
	 * @return the pagination
	 */
	public Pagination getPagination() {
		return pagination;
	}

	/**
	 * <p>Set the pagination</p>
	 *
	 * @param pagination the pagination to set
	 */
	public void setPagination(
			Pagination pagination) {
		this.pagination = pagination;
	}

	/**
	 * <p>Return the query_operand.</p>
	 *
	 * @return the query_operand
	 */
	public QueryOperand getQuery_operand() {
		return query_operand;
	}

	/**
	 * <p>Set the query_operand</p>
	 *
	 * @param query_operand the query_operand to set
	 */
	public void setQuery_operand(
			QueryOperand query_operand) {
		this.query_operand = query_operand;
	}

	/**
	 * <p>Return the sorting.</p>
	 *
	 * @return the sorting
	 */
	public Sorting getSorting() {
		return sorting;
	}

	/**
	 * <p>Set the sorting</p>
	 *
	 * @param sorting the sorting to set
	 */
	public void setSorting(
			Sorting sorting) {
		this.sorting = sorting;
	}

	/**
	 * <p>Return the canonical_uri.</p>
	 *
	 * @return the canonical_uri
	 */
	public String getCanonical_uri() {
		return canonical_uri;
	}

	/**
	 * <p>Set the canonical_uri</p>
	 *
	 * @param canonical_uri the canonical_uri to set
	 */
	public void setCanonical_uri(
			String canonical_uri) {
		this.canonical_uri = canonical_uri;
	}

	/**
	 * <p>Return the query_respond_to.</p>
	 *
	 * @return the query_respond_to
	 */
	public String getQuery_respond_to() {
		return query_respond_to;
	}

	/**
	 * <p>Set the query_respond_to</p>
	 *
	 * @param query_respond_to the query_respond_to to set
	 */
	public void setQuery_respond_to(
			String query_respond_to) {
		this.query_respond_to = query_respond_to;
	}

	/**
	 * <p>Return the available.</p>
	 *
	 * @return the available
	 */
	public Available getAvailable() {
		return available;
	}

	/**
	 * <p>Set the available</p>
	 *
	 * @param available the available to set
	 */
	public void setAvailable(
			Available available) {
		this.available = available;
	}

	/**
	 * <p>Return the fields.</p>
	 *
	 * @return the fields
	 */
	public Map<String, String> getFields() {
		return fields;
	}

	/**
	 * <p>Set the fields</p>
	 *
	 * @param fields the fields to set
	 */
	public void setFields(
			Map<String, String> fields) {
		this.fields = fields;
	}

	/**
	 * <p>Return the active.</p>
	 *
	 * @return the active
	 */
	public Active getActive() {
		return active;
	}

	/**
	 * <p>Set the active</p>
	 *
	 * @param active the active to set
	 */
	public void setActive(Active active) {
		this.active = active;
	}
	
}
