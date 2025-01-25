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

import com.synapticloop.integration.response.json.response.panl.uris.NumPerPageUris;
import com.synapticloop.integration.response.json.response.panl.uris.PageUris;

public class Pagination {
	public int num_pages;
	public boolean num_results_exact;
	public PageUris page_uris;
	public int page_num;
	public int num_results;
	public int num_per_page;
	public NumPerPageUris num_per_page_uris;
	/**
	 * <p>Return the num_pages.</p>
	 *
	 * @return the num_pages
	 */
	public int getNum_pages() {
		return num_pages;
	}
	/**
	 * <p>Set the num_pages</p>
	 *
	 * @param num_pages the num_pages to set
	 */
	public void setNum_pages(
			int num_pages) {
		this.num_pages = num_pages;
	}
	/**
	 * <p>Return the num_results_exact.</p>
	 *
	 * @return the num_results_exact
	 */
	public boolean isNum_results_exact() {
		return num_results_exact;
	}
	/**
	 * <p>Set the num_results_exact</p>
	 *
	 * @param num_results_exact the num_results_exact to set
	 */
	public void setNum_results_exact(
			boolean num_results_exact) {
		this.num_results_exact = num_results_exact;
	}
	/**
	 * <p>Return the page_uris.</p>
	 *
	 * @return the page_uris
	 */
	public PageUris getPage_uris() {
		return page_uris;
	}
	/**
	 * <p>Set the page_uris</p>
	 *
	 * @param page_uris the page_uris to set
	 */
	public void setPage_uris(
			PageUris page_uris) {
		this.page_uris = page_uris;
	}
	/**
	 * <p>Return the page_num.</p>
	 *
	 * @return the page_num
	 */
	public int getPage_num() {
		return page_num;
	}
	/**
	 * <p>Set the page_num</p>
	 *
	 * @param page_num the page_num to set
	 */
	public void setPage_num(
			int page_num) {
		this.page_num = page_num;
	}
	/**
	 * <p>Return the num_results.</p>
	 *
	 * @return the num_results
	 */
	public int getNum_results() {
		return num_results;
	}
	/**
	 * <p>Set the num_results</p>
	 *
	 * @param num_results the num_results to set
	 */
	public void setNum_results(
			int num_results) {
		this.num_results = num_results;
	}
	/**
	 * <p>Return the num_per_page.</p>
	 *
	 * @return the num_per_page
	 */
	public int getNum_per_page() {
		return num_per_page;
	}
	/**
	 * <p>Set the num_per_page</p>
	 *
	 * @param num_per_page the num_per_page to set
	 */
	public void setNum_per_page(
			int num_per_page) {
		this.num_per_page = num_per_page;
	}
	/**
	 * <p>Return the num_per_page_uris.</p>
	 *
	 * @return the num_per_page_uris
	 */
	public NumPerPageUris getNum_per_page_uris() {
		return num_per_page_uris;
	}
	/**
	 * <p>Set the num_per_page_uris</p>
	 *
	 * @param num_per_page_uris the num_per_page_uris to set
	 */
	public void setNum_per_page_uris(
			NumPerPageUris num_per_page_uris) {
		this.num_per_page_uris = num_per_page_uris;
	}
	
	
}
