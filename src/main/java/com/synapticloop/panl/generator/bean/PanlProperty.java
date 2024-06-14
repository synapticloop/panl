package com.synapticloop.panl.generator.bean;

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

public class PanlProperty {
	private final String panlPropertyName;
	private final String panlPropertyValue;
	private final boolean hideProperty;
	private String solrClassName;

	public PanlProperty(String panlPropertyName, String panlPropertyValue, int lpseNum) {
		this.panlPropertyName = panlPropertyName;

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < lpseNum; i ++) {
			sb.append(panlPropertyValue);
		}
		this.panlPropertyValue = sb.toString();
		this.hideProperty = false;
	}

	public PanlProperty(String panlPropertyName, String panlPropertyValue) {
		this.panlPropertyName = panlPropertyName;
		this.panlPropertyValue = panlPropertyValue;
		this.hideProperty = false;
	}

	public PanlProperty(String panlPropertyName, String panlPropertyValue, boolean hideProperty) {
		this.panlPropertyName = panlPropertyName;
		this.panlPropertyValue = panlPropertyValue;
		this.hideProperty = hideProperty;
	}

	public String getPanlPropertyValue() {
		return(panlPropertyValue);
	}

	public String toProperties() {
		if(hideProperty) {
			return(panlPropertyValue);
		} else {
			return (panlPropertyName + "=" + panlPropertyValue);
		}
	}

	public void setSolrClassName(String solrClassName) {
		this.solrClassName = solrClassName;
	}
}
