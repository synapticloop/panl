package com.synapticloop.panl.server.handler.tokeniser.token;
import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.StringTokenizer;

import static com.synapticloop.panl.TestHelper.getLpseTokeniser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FacetLpseRangeTokenTest {
	private void testDefaults(String propertiesLocation, String uri) {
		CollectionProperties collectionProperties = TestHelper.getCollectionProperties(propertiesLocation);
		StringTokenizer stringTokenizer = new StringTokenizer(uri, "/", false);

		FacetLpseToken lpseToken = (FacetLpseToken)LpseToken.getLpseToken(collectionProperties, "w", "", stringTokenizer, getLpseTokeniser("-w"));
		assertTrue(lpseToken.getIsValid());
		assertEquals("10", lpseToken.getValue());
		assertEquals("50", lpseToken.getToValue());
	}

	@Test public void testPreMidSufRangeDecoding() {
		testDefaults("/range/prefix-midfix-suffix.properties", "/weighing+from+10+to+50+grams/");
		testDefaults("/range/prefix-midfix-suffix.properties", "/from+light+to+heavy+pencils/");
		testDefaults("/range/prefix-midfix-suffix.properties", "/10+grams+to+heavy+pencils/");
		testDefaults("/range/prefix-midfix-suffix.properties", "/from+light+to+50+grams/");
	}

	@Test public void testPreSufRangeDecoding() {
		testDefaults("/range/prefix-suffix.properties", "/weighing+from+10~50+grams/");
		testDefaults("/range/prefix-suffix.properties", "/from+light~heavy+pencils/");
		testDefaults("/range/prefix-suffix.properties", "/10+grams~heavy+pencils/");
		testDefaults("/range/prefix-suffix.properties", "/from+light~50+grams/");
	}

	@Test public void testSufRangeDecoding() throws IOException, PanlServerException {
		testDefaults("/range/suffix.properties", "/10+grams~50+grams/");
		testDefaults("/range/suffix.properties", "/from+light~heavy+pencils/");
		testDefaults("/range/suffix.properties", "/10+grams~heavy+pencils/");
		testDefaults("/range/suffix.properties", "/from+light~50+grams/");
	}

	@Test public void testPreRangeDecoding() throws IOException, PanlServerException {
		testDefaults("/range/prefix.properties", "/this+is+the+prefix10~this+is+the+prefix50/");
		testDefaults("/range/prefix.properties", "/from+light~heavy+pencils/");
		testDefaults("/range/prefix.properties", "/this+is+the+prefix10~heavy+pencils/");
		testDefaults("/range/prefix.properties", "/from+light~this+is+the+prefix50/");
	}

	@Test public void testPreMidRangeDecoding() throws IOException, PanlServerException {
		testDefaults("/range/prefix-midfix.properties", "/weighing+10+to+weighing+50/");
		testDefaults("/range/prefix-midfix.properties", "/from+light+to+heavy+pencils/");
		testDefaults("/range/prefix-midfix.properties", "/weighing+10+to+heavy+pencils/");
		testDefaults("/range/prefix-midfix.properties", "/from+light+to+weighing+50/");
	}

}
