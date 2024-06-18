package com.synapticloop.panl.server.handler.fielderiser.field;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.fielderiser.field.param.PanlPassThroughField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.synapticloop.panl.TestHelper.getPanlTokenMap;
import static com.synapticloop.panl.TestHelper.testApplyToQueryEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PanlPassThroughFieldTest {
	@BeforeAll public static void beforeAll() throws PanlServerException, IOException {
		TestHelper.beforeAll();
	}

	@Test public void testPassThroughNoCanonical() throws PanlServerException, IOException {
		testApplyToQueryEquals("/passthrough/default.properties", "/test/default/something+goes+here/z/", "q=");
	}

	@Test public void testPassThroughCanonical() throws PanlServerException, IOException {
		testApplyToQueryEquals("/passthrough/canonical.properties", "/test/default/something+goes+here/z/", "q=");
	}

	@Test public void testPassThroughUrisNoCanonical() throws PanlServerException, IOException {
		CollectionProperties collectionProperties = TestHelper.getCollectionProperties("/passthrough/default.properties");

		List<LpseToken> lpseTokens = TestHelper.getLpseTokens("/passthrough/default.properties", "/test/default/something+here/z/", "");
		Map<String, List<LpseToken>> panlTokenMap = getPanlTokenMap(lpseTokens);
		PanlPassThroughField panlPassThroughField = (PanlPassThroughField) collectionProperties.getLpseField("z");
		assertEquals("", panlPassThroughField.getCanonicalUriPath(panlTokenMap, collectionProperties));
		assertEquals("", panlPassThroughField.getCanonicalLpseCode(panlTokenMap, collectionProperties));

		assertEquals("something+here/", panlPassThroughField.getURIPath(panlTokenMap, collectionProperties));
		assertEquals("z", panlPassThroughField.getLpseCode(panlTokenMap, collectionProperties));

		assertEquals(1, lpseTokens.size());
		for (LpseToken lpseToken : lpseTokens) {
			assertEquals("something+here/", panlPassThroughField.getURIPath(lpseToken, collectionProperties));
			assertEquals("z", panlPassThroughField.getLpseCode(lpseToken, collectionProperties));
		}
	}

	@Test public void testPassThroughUrisCanonical() throws PanlServerException, IOException {
		CollectionProperties collectionProperties = TestHelper.getCollectionProperties("/passthrough/canonical.properties");

		List<LpseToken> lpseTokens = TestHelper.getLpseTokens("/passthrough/canonical.properties", "/test/default/something+here/z/", "");
		Map<String, List<LpseToken>> panlTokenMap = getPanlTokenMap(lpseTokens);
		PanlPassThroughField panlPassThroughField = (PanlPassThroughField) collectionProperties.getLpseField("z");
		assertEquals("something+here/", panlPassThroughField.getCanonicalUriPath(panlTokenMap, collectionProperties));
		assertEquals("z", panlPassThroughField.getCanonicalLpseCode(panlTokenMap, collectionProperties));

		assertEquals("something+here/", panlPassThroughField.getURIPath(panlTokenMap, collectionProperties));
		assertEquals("z", panlPassThroughField.getLpseCode(panlTokenMap, collectionProperties));

		assertEquals(1, lpseTokens.size());
		for (LpseToken lpseToken : lpseTokens) {
			assertEquals("something+here/", panlPassThroughField.getURIPath(lpseToken, collectionProperties));
			assertEquals("z", panlPassThroughField.getLpseCode(lpseToken, collectionProperties));
		}
	}

}
