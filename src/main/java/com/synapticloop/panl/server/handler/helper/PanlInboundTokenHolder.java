package com.synapticloop.panl.server.handler.helper;

import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.NumRowsLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.PageNumLpseToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PanlInboundTokenHolder {
	private Map<String, List<LpseToken>> panlTokenMap = new HashMap<>();
	private final boolean numFoundExact;
	private final long numFound;

	long numRows = 10;
	long pageNum = 1;

	public PanlInboundTokenHolder(
			List<LpseToken> lpseTokens,
			long defaultNumPerPage,
			long numFound,
			boolean numFoundExact) {

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
