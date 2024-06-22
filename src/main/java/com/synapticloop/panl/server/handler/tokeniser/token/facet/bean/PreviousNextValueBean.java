package com.synapticloop.panl.server.handler.tokeniser.token.facet.bean;

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

/**
 * <p>A simple bean to encapsulate the information required for the DATE facet.
 * No validation is performed on the variables, that should be done prior to
 * instantiation.</p>
 *
 * <ul>
 *   <li><code>previousNext</code> The configured word/phrase for either the
 *   previous or next prefix</li>
 *   <li><code>designator</code> The text designator for the time period</li>
 *   <li><code>solrRangeDesignator</code> The text designator for the Solr time
 *   period - which is used when generating the Solr Query</li>
 *   <li><code>value</code> The integer convertible value for the time period</li>
 * </ul>
 *
 * @author synapticloop
 */
public class PreviousNextValueBean {
	private final String previousNext;
	private final String designator;
	private final String solrRangeDesignator;
	private final String value;

	/**
	 * <p>Instantiate the bean.</p>
	 *
	 * @param previousNext The configured word/phrase for either the previous
	 * 		or next prefix
	 * @param designator The text designator for the time period
	 * @param solrRangeDesignator The text designator for the Solr time period
	 * 		- which is used when generating the Solr Query
	 * @param value The integer convertible value for the time period
	 */
	public PreviousNextValueBean(String previousNext, String designator, String solrRangeDesignator, String value) {
		this.previousNext = previousNext;
		this.designator = designator;
		this.solrRangeDesignator = solrRangeDesignator;
		this.value = value;
	}

	/**
	 * <p>Get the configured word/phrase for either the previous or next prefix</p>
	 *
	 * @return the configured word/phrase for either the previous or next prefix
	 */
	public String getPreviousNext() {
		return previousNext;
	}

	/**
	 * <p>Get the text designator for the time period</p>
	 *
	 * @return The text designator for the time period
	 */
	public String getDesignator() {
		return designator;
	}

	/**
	 * <p>Get the text designator for the Solr time period - which is used when
	 * generating the Solr Query</p>
	 *
	 * @return The text designator for the Solr time period -  which is used when
	 * 		generating the Solr Query
	 */
	public String getSolrRangeDesignator() {
		return (solrRangeDesignator);
	}

	/**
	 * <p>Get the integer convertible value for the time period</p>
	 *
	 * @return The integer convertible value for the time period
	 */
	public String getValue() {
		return value;
	}
}
