package com.synapticloop.panl.server.handler.fielderiser.field;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.synapticloop.panl.TestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PanlSortFieldTest {
	@BeforeAll public static void beforeAll() throws PanlServerException, IOException {
		TestHelper.beforeAll();
	}

	@Test public void testSortOrder() throws PanlServerException, IOException {
		testApplyToQueryContains("/sort/default.properties", "/test/default/sb-/", "&sort=brand+desc");
		testApplyToQueryContains("/sort/default.properties", "/test/default/sm+/", "&sort=name+asc");
		testApplyToQueryContains("/sort/default.properties", "/test/default/sm+sb-/", "&sort=name+asc,brand+desc");
		testApplyToQueryContains("/sort/default.properties", "/test/default/sb-sm+/", "&sort=brand+desc,name+asc");

		// blank LPSE sort codes do nothing
		testApplyToQueryEquals("/sort/default.properties", "/test/default/", "q=");

		// relevance desc is the default and ignored
		testApplyToQueryEquals("/sort/default.properties", "/test/default/s-/", "q=");
		// there is no relevance ascending
		testApplyToQueryEquals("/sort/default.properties", "/test/default/s+/", "q=");
	}

	@Test public void testLpseTokens()  throws PanlServerException, IOException {

		List<LpseToken> lpseTokens = TestHelper.getLpseTokens("/sort/default.properties", "/test/default/sb-/", "");
		for(LpseToken lpseToken : lpseTokens) {
			assertTrue(lpseToken.getIsValid());
			assertEquals("s/b", lpseToken.getEquivalenceValue());
		}
	}

	@Test public void testSortFieldUris() throws PanlServerException, IOException {
		CollectionProperties collectionProperties = TestHelper.getCollectionProperties("/sort/default.properties");

		List<LpseToken> lpseTokens = TestHelper.getLpseTokens("/sort/default.properties", "/test/default/sb-/", "");
		Map<String, List<LpseToken>> panlTokenMap = getPanlTokenMap(lpseTokens);
		PanlSortField panlSortField = (PanlSortField) collectionProperties.getLpseField("s");
		assertEquals("", panlSortField.getCanonicalUriPath(panlTokenMap, collectionProperties));
		assertEquals("sb-", panlSortField.getCanonicalLpseCode(panlTokenMap, collectionProperties));

		assertEquals("", panlSortField.getURIPath(panlTokenMap, collectionProperties));
		assertEquals("sb-", panlSortField.getLpseCode(panlTokenMap, collectionProperties));

		assertEquals(1, lpseTokens.size());
		for (LpseToken lpseToken : lpseTokens) {
			assertEquals("", panlSortField.getURIPath(lpseToken, collectionProperties));
			assertEquals("sb-", panlSortField.getLpseCode(lpseToken, collectionProperties));
		}
	}
}
