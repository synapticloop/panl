package com.synapticloop.panl.server.handler.tokeniser.token;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.StringTokenizer;

import static com.synapticloop.panl.TestHelper.getLpseTokeniser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LpseTokenEquivalenceValueTest {
	private List<LpseToken> getLpseTokens(String propertiesLocation, String uri, String lpseUri) {
		CollectionProperties collectionProperties = TestHelper.getCollectionProperties(propertiesLocation);
		StringTokenizer stringTokenizer = new StringTokenizer(uri, "/", false);
		LpseTokeniser lpseTokeniser = getLpseTokeniser(lpseUri);
		String lpseCode = lpseTokeniser.nextToken();
		return(LpseToken.getLpseTokens(collectionProperties, lpseCode, "", stringTokenizer, lpseTokeniser));
	}

	@Test public void testQueryOperandEquivalence() {
		LpseToken lpseToken = getLpseTokens("/default.properties", "//", "o-").get(0);
		assertEquals("o/", lpseToken.getEquivalenceValue());

		lpseToken = getLpseTokens("/default.properties", "//", "o+").get(0);
		assertEquals("o/", lpseToken.getEquivalenceValue());

	}

	@Test public void testPageNumberEquivalence() {
		LpseToken lpseToken = getLpseTokens("/default.properties", "/1/", "p").get(0);
		assertEquals("p/", lpseToken.getEquivalenceValue());

		lpseToken = getLpseTokens("/default.properties", "/2/", "p").get(0);
		assertEquals("p/", lpseToken.getEquivalenceValue());
	}

	@Test public void testNumPerPageEquivalence() {
		LpseToken lpseToken = getLpseTokens("/default.properties", "/1/", "n").get(0);
		assertEquals("n/", lpseToken.getEquivalenceValue());

		lpseToken = getLpseTokens("/default.properties", "/2/", "n").get(0);
		assertEquals("n/", lpseToken.getEquivalenceValue());
	}

	@Test public void testFacetEquivalence() {
		LpseToken lpseToken = getLpseTokens("/default.properties", "/the-value-of-a-facet/", "b").get(0);
		assertEquals("b/the-value-of-a-facet", lpseToken.getEquivalenceValue());

		// URI decoding
		lpseToken = getLpseTokens("/default.properties", "/the+value+of+a+facet/", "b").get(0);
		assertEquals("b/the value of a facet", lpseToken.getEquivalenceValue());

		lpseToken = getLpseTokens("/default.properties", "/the value of a facet/", "b").get(0);
		assertEquals("b/the value of a facet", lpseToken.getEquivalenceValue());
	}

	@Test public void testSortEquivalence() {
		LpseToken lpseToken = getLpseTokens("/default.properties", "//", "sN-").get(0);
		assertEquals("s/N", lpseToken.getEquivalenceValue());

		lpseToken = getLpseTokens("/default.properties", "//", "sN+").get(0);
		assertEquals("s/N", lpseToken.getEquivalenceValue());
	}

	@Test public void testPassThroughEquivalence() {
		LpseToken lpseToken = getLpseTokens("/default.properties", "/something here/", "z").get(0);
		assertEquals("", lpseToken.getEquivalenceValue());

		lpseToken = getLpseTokens("/default.properties", "/something else goes here/", "z").get(0);
		assertEquals("", lpseToken.getEquivalenceValue());
	}

}
