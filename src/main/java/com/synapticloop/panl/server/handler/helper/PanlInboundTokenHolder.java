package com.synapticloop.panl.server.handler.helper;

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

import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.NumRowsLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.PageNumLpseToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>The PanlInboundTokenHolder builds data structures and contains all
 * information that is required to process the Panl response object.</p>
 */
public class PanlInboundTokenHolder {
	private Map<String, List<LpseToken>> panlTokenMap = new HashMap<>();
	private final boolean numFoundExact;
	private final long numFound;
	private final List<BaseField> lpseFields;

	long numRows = 10;
	long pageNum = 1;

	public PanlInboundTokenHolder(
			CollectionProperties collectionProperties,
			List<LpseToken> lpseTokens,
			long defaultNumPerPage,
			long numFound,
			boolean numFoundExact) {

		this.lpseFields = collectionProperties.getLpseFields();
		this.pageNum = defaultNumPerPage;
		this.numFound = numFound;
		this.numFoundExact = numFoundExact;

		// now build the internal datastructures
		for (LpseToken lpseToken : lpseTokens) {
			String lpseCode = lpseToken.getLpseCode();

			List<LpseToken> lpseTokenList = panlTokenMap.get(lpseCode);
			if (null == lpseTokenList) {
				lpseTokenList = new ArrayList<>();
			}

			// only adding valid tokens
			if (lpseToken.getIsValid()) {
				lpseTokenList.add(lpseToken);
				panlTokenMap.put(lpseCode, lpseTokenList);
			}

			if (lpseToken instanceof NumRowsLpseToken) {
				numRows = ((NumRowsLpseToken) lpseToken).getNumRows();
			} else if (lpseToken instanceof PageNumLpseToken) {
				pageNum = ((PageNumLpseToken) lpseToken).getPageNum();
			}
		}
	}

	public long getNumRows() {
		return numRows;
	}

	public long getPageNum() {
		return pageNum;
	}
}
