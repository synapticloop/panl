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

import com.synapticloop.integration.response.json.response.panl.response.Doc;

public class Response {
	public Doc[] docs;
	public long numFound;
	public long start;
	public long maxScore;
	public boolean numFoundExact;
	/**
	 * <p>Return the numFound.</p>
	 *
	 * @return the numFound
	 */
	public long getNumFound() {
		return numFound;
	}
	/**
	 * <p>Set the numFound</p>
	 *
	 * @param numFound the numFound to set
	 */
	public void setNumFound(
			long numFound) {
		this.numFound = numFound;
	}
	/**
	 * <p>Return the start.</p>
	 *
	 * @return the start
	 */
	public long getStart() {
		return start;
	}
	/**
	 * <p>Set the start</p>
	 *
	 * @param start the start to set
	 */
	public void setStart(
			long start) {
		this.start = start;
	}
	/**
	 * <p>Return the maxScore.</p>
	 *
	 * @return the maxScore
	 */
	public long getMaxScore() {
		return maxScore;
	}
	/**
	 * <p>Set the maxScore</p>
	 *
	 * @param maxScore the maxScore to set
	 */
	public void setMaxScore(
			long maxScore) {
		this.maxScore = maxScore;
	}
	/**
	 * <p>Return the numFoundExact.</p>
	 *
	 * @return the numFoundExact
	 */
	public boolean isNumFoundExact() {
		return numFoundExact;
	}
	/**
	 * <p>Set the numFoundExact</p>
	 *
	 * @param numFoundExact the numFoundExact to set
	 */
	public void setNumFoundExact(
			boolean numFoundExact) {
		this.numFoundExact = numFoundExact;
	}
	/**
	 * <p>Return the docs.</p>
	 *
	 * @return the docs
	 */
	public Doc[] getDocs() {
		return docs;
	}
	/**
	 * <p>Set the docs</p>
	 *
	 * @param docs the docs to set
	 */
	public void setDocs(
			Doc[] docs) {
		this.docs = docs;
	}
	
	
}
