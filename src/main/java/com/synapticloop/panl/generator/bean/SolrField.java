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

package com.synapticloop.panl.generator.bean;

public class SolrField {
	private final String name;
	private final boolean isFacet;
	private boolean isSupported = false;
	private boolean isMultiValued = false;

	public SolrField(String name, boolean isFacet) {
		this.name = name;
		this.isFacet = isFacet;
	}

	public String getName() {
		return name;
	}

	public boolean getIsFacet() {
		return isFacet;
	}

	public boolean getIsSupported() {
		return isSupported;
	}

	public void setIsSupported(boolean supported) {
		isSupported = supported;
	}

	public boolean getIsMultiValued() {
		return isMultiValued;
	}

	public void setIsMultiValued(boolean multiValued) {
		isMultiValued = multiValued;
	}
}
