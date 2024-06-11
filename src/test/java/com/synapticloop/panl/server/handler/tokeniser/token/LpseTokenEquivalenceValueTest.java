package com.synapticloop.panl.server.handler.tokeniser.token;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import org.junit.jupiter.api.Test;

import java.util.StringTokenizer;

import static com.synapticloop.panl.TestHelper.getLpseTokeniser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LpseTokenEquivalenceValueTest {
	private LpseToken getLpseToken(String propertiesLocation, String uri, String lpseUri) {
		CollectionProperties collectionProperties = TestHelper.getCollectionProperties(propertiesLocation);
		StringTokenizer stringTokenizer = new StringTokenizer(uri, "/", false);
		LpseTokeniser lpseTokeniser = getLpseTokeniser(lpseUri);
		String lpseCode = lpseTokeniser.nextToken();
		return(LpseToken.getLpseToken(collectionProperties, lpseCode, "", stringTokenizer, lpseTokeniser));
	}

	@Test public void testQueryOperandEquivalence() {
		LpseToken lpseToken = getLpseToken("/default.properties", "//", "o-");
		assertEquals("o/", lpseToken.getEquivalenceValue());

		lpseToken = getLpseToken("/default.properties", "//", "o+");
		assertEquals("o/", lpseToken.getEquivalenceValue());

	}

	@Test public void testPageNumberEquivalence() {
		LpseToken lpseToken = getLpseToken("/default.properties", "/1/", "p");
		assertEquals("p/1", lpseToken.getEquivalenceValue());

		lpseToken = getLpseToken("/default.properties", "/2/", "p");
		assertEquals("p/2", lpseToken.getEquivalenceValue());
	}

	@Test public void testNumPerPageEquivalence() {
		LpseToken lpseToken = getLpseToken("/default.properties", "/1/", "n");
		assertEquals("n/1", lpseToken.getEquivalenceValue());

		lpseToken = getLpseToken("/default.properties", "/2/", "n");
		assertEquals("n/2", lpseToken.getEquivalenceValue());
	}

	@Test public void testFacetEquivalence() {
		LpseToken lpseToken = getLpseToken("/default.properties", "/the-value-of-a-facet/", "b");
		assertEquals("b/the-value-of-a-facet", lpseToken.getEquivalenceValue());

		// URI decoding
		lpseToken = getLpseToken("/default.properties", "/the+value+of+a+facet/", "b");
		assertEquals("b/the value of a facet", lpseToken.getEquivalenceValue());

		lpseToken = getLpseToken("/default.properties", "/the value of a facet/", "b");
		assertEquals("b/the value of a facet", lpseToken.getEquivalenceValue());
	}

	@Test public void testSortEquivalence() {
		LpseToken lpseToken = getLpseToken("/default.properties", "//", "sN-");
		assertEquals("s/N", lpseToken.getEquivalenceValue());

		lpseToken = getLpseToken("/default.properties", "//", "sN+");
		assertEquals("s/N", lpseToken.getEquivalenceValue());
	}

	@Test public void testPassThroughEquivalence() {
		LpseToken lpseToken = getLpseToken("/default.properties", "/something here/", "z");
		assertEquals("", lpseToken.getEquivalenceValue());

		lpseToken = getLpseToken("/default.properties", "/something else goes here/", "z");
		assertEquals("", lpseToken.getEquivalenceValue());
	}

}
