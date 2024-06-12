package com.synapticloop.panl.server.handler.tokeniser.token;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.generator.bean.Collection;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
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

		FacetLpseToken lpseToken = (FacetLpseToken) LpseToken.getLpseToken(collectionProperties, "w", "", stringTokenizer, getLpseTokeniser("-w"));
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

	@Test public void testSufRangeDecoding() {
		testDefaults("/range/suffix.properties", "/10+grams~50+grams/");
		testDefaults("/range/suffix.properties", "/from+light~heavy+pencils/");
		testDefaults("/range/suffix.properties", "/10+grams~heavy+pencils/");
		testDefaults("/range/suffix.properties", "/from+light~50+grams/");
	}

	@Test public void testPreRangeDecoding() {
		testDefaults("/range/prefix.properties", "/this+is+the+prefix10~this+is+the+prefix50/");
		testDefaults("/range/prefix.properties", "/from+light~heavy+pencils/");
		testDefaults("/range/prefix.properties", "/this+is+the+prefix10~heavy+pencils/");
		testDefaults("/range/prefix.properties", "/from+light~this+is+the+prefix50/");
	}

	@Test public void testPreMidRangeDecoding() {
		testDefaults("/range/prefix-midfix.properties", "/weighing+10+to+weighing+50/");
		testDefaults("/range/prefix-midfix.properties", "/from+light+to+heavy+pencils/");
		testDefaults("/range/prefix-midfix.properties", "/weighing+10+to+heavy+pencils/");
		testDefaults("/range/prefix-midfix.properties", "/from+light+to+weighing+50/");
	}

	private void getLpseToken(String propertiesLocation, String uriPath, String lpsePath, int from, int to) {
		CollectionProperties collectionProperties = TestHelper.getCollectionProperties(propertiesLocation);
		StringTokenizer stringTokenizer = new StringTokenizer(uriPath, "/", false);
		LpseTokeniser lpseTokeniser = new LpseTokeniser(lpsePath, Collection.CODES_AND_METADATA, true);
		String lpseToken = lpseTokeniser.nextToken();
		FacetLpseToken facetLpseToken = (FacetLpseToken) LpseToken.getLpseToken(
				collectionProperties,
				lpseToken,
				"",
				stringTokenizer,
				lpseTokeniser);
		System.out.println(facetLpseToken.explain());

		assertTrue(facetLpseToken.getIsValid());
		assertEquals(from, Integer.parseInt(facetLpseToken.getValue()));
		assertEquals(to, Integer.parseInt(facetLpseToken.getToValue()));
	}

	@Test public void testAppendageVariations() {
		getLpseToken("/range/appendages/no-midfix/no-midfix.properties", "/10~50/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix.properties", "/10~50/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix.properties", "10~50/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vp.properties", "weight+10~weight+50/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vs.properties", "10+grams~50+grams/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vs-vp.properties", "weight+10+grams~weight+50+grams/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-min.properties", "from+light~50/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-max.properties", "10~heavy+pencils/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-min-max.properties", "from+light~heavy+pencils/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vp-min.properties", "from+light~weight+50/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vp-min.properties", "weight+11~weight+50/", "w+w", 11, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vp-max.properties", "weight+10~heavy+pencils/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vp-max.properties", "weight+10~weight+49/", "w+w", 10, 49);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vp-min-max.properties", "from+light~heavy+pencils/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vp-min-max.properties", "weight+11~heavy+pencils/", "w+w", 11, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vp-min-max.properties", "from+light~weight+49/", "w+w", 10, 49);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vp-min-max.properties", "weight+11~weight+49/", "w+w", 11, 49);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vs-min.properties", "from+light~50+grams/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vs-min.properties", "11+grams~50+grams/", "w+w", 11, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vs-max.properties", "10+grams~heavy+pencils/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vs-max.properties", "10+grams~49+grams/", "w+w", 10, 49);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vs-min-max.properties", "from+light~heavy+pencils/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vs-min-max.properties", "11+grams~heavy+pencils/", "w+w", 11, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vs-min-max.properties", "from+light~49+grams/", "w+w", 10, 49);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vs-min-max.properties", "11+grams~49+grams/", "w+w", 11, 49);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vs-vp-min.properties", "from+light~weight+50+grams/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vs-vp-min.properties", "weight+11+grams~weight+50+grams/", "w+w", 11, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vs-vp-max.properties", "weight+10+grams~heavy+pencils/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vs-vp-max.properties", "weight+10+grams~weight+49+grams/", "w+w", 10, 49);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vs-vp-min-max.properties", "from+light~heavy+pencils/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vs-vp-min-max.properties", "weight+11+grams~heavy+pencils/", "w+w", 11, 50);
		getLpseToken("/range/appendages/no-midfix/no-midfix-vs-vp-min-max.properties", "from+light~weight+49+grams/", "w+w", 10, 49);

		getLpseToken("/range/appendages/midfix/midfix.properties", "10+to+50/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-min.properties", "from+light+to+50/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-min.properties", "11+to+50/", "w-w", 11, 50);
		getLpseToken("/range/appendages/midfix/midfix-min.properties", "11+to+49/", "w-w", 11, 49);
		getLpseToken("/range/appendages/midfix/midfix-max.properties", "10+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-max.properties", "10+to+49/", "w-w", 10, 49);
		getLpseToken("/range/appendages/midfix/midfix-max.properties", "11+to+49/", "w-w", 11, 49);
		getLpseToken("/range/appendages/midfix/midfix-min-max.properties", "from+light+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-min-max.properties", "11+to+heavy+pencils/", "w-w", 11, 50);
		getLpseToken("/range/appendages/midfix/midfix-min-max.properties", "from+light+to+49/", "w-w", 10, 49);
		getLpseToken("/range/appendages/midfix/midfix-min-max.properties", "11+to+49/", "w-w", 11, 49);
		getLpseToken("/range/appendages/midfix/midfix-rp.properties", "weighing+10+to+50/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-rp-min.properties", "from+light+to+50/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-rp-min.properties", "weighing+11+to+50/", "w-w", 11, 50);
		getLpseToken("/range/appendages/midfix/midfix-rp-max.properties", "weighing+10+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-rp-max.properties", "weighing+10+to+49/", "w-w", 10, 49);
		getLpseToken("/range/appendages/midfix/midfix-rp-min-max.properties", "from+light+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-rp-min-max.properties", "weighing+11+to+heavy+pencils/", "w-w", 11, 50);
		getLpseToken("/range/appendages/midfix/midfix-rp-min-max.properties", "from+light+to+49/", "w-w", 10, 49);
		getLpseToken("/range/appendages/midfix/midfix-rp-min-max.properties", "weighing+11+to+49/", "w-w", 11, 49);
		getLpseToken("/range/appendages/midfix/midfix-rs.properties", "10+to+50+grams/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-rs-min.properties", "from+light+to+50+grams/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-rs-min.properties", "11+to+50+grams/", "w-w", 11, 50);
		getLpseToken("/range/appendages/midfix/midfix-rs-max.properties", "10+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-rs-max.properties", "10+to+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/midfix/midfix-rs-min-max.properties", "from+light+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-rs-min-max.properties", "11+to+heavy+pencils/", "w-w", 11, 50);
		getLpseToken("/range/appendages/midfix/midfix-rs-min-max.properties", "from+light+to+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/midfix/midfix-rs-min-max.properties", "11+to+49+grams/", "w-w", 11, 49);
		getLpseToken("/range/appendages/midfix/midfix-rp-rs.properties", "weighing+10+to+50+grams/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-rp-rs-min.properties", "from+light+to+50+grams/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-rp-rs-min.properties", "from+light+to+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/midfix/midfix-rp-rs-min.properties", "weighing+11+to+50+grams/", "w-w", 11, 50);
		getLpseToken("/range/appendages/midfix/midfix-rp-rs-min.properties", "weighing+11+to+49+grams/", "w-w", 11, 49);
		getLpseToken("/range/appendages/midfix/midfix-rp-rs-max.properties", "weighing+10+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-rp-rs-max.properties", "weighing+11+to+heavy+pencils/", "w-w", 11, 50);
		getLpseToken("/range/appendages/midfix/midfix-rp-rs-max.properties", "weighing+11+to+49+grams/", "w-w", 11, 49);
		getLpseToken("/range/appendages/midfix/midfix-rp-rs-max.properties", "weighing+10+to+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/midfix/midfix-rp-rs-min-max.properties", "from+light+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-rp-rs-min-max.properties", "weighing+11+to+heavy+pencils/", "w-w", 11, 50);
		getLpseToken("/range/appendages/midfix/midfix-rp-rs-min-max.properties", "weighing+11+to+49+grams/", "w-w", 11, 49);
		getLpseToken("/range/appendages/midfix/midfix-rp-rs-min-max.properties", "from+light+to+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/midfix/midfix-vp.properties", "weight+10+to+weight+50/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-vp-min.properties", "from+light+to+weight+50/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-vp-min.properties", "weight+11+to+weight+50/", "w-w", 11, 50);
		getLpseToken("/range/appendages/midfix/midfix-vp-max.properties", "weight+10+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-vp-max.properties", "weight+10+to+weight+49/", "w-w", 10, 49);
		getLpseToken("/range/appendages/midfix/midfix-vp-min-max.properties", "from+light+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-vp-min-max.properties", "weight+11+to+heavy+pencils/", "w-w", 11, 50);
		getLpseToken("/range/appendages/midfix/midfix-vp-min-max.properties", "from+light+to+weight+49/", "w-w", 10, 49);
		getLpseToken("/range/appendages/midfix/midfix-vp-min-max.properties", "weight+11+to+weight+49/", "w-w", 11, 49);
		getLpseToken("/range/appendages/midfix/midfix-vs.properties", "10+grams+to+50+grams/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-vs-min.properties", "from+light+to+50+grams/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-vs-min.properties", "11+grams+to+50+grams/", "w-w", 11, 50);
		getLpseToken("/range/appendages/midfix/midfix-vs-max.properties", "10+grams+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-vs-max.properties", "10+grams+to+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/midfix/midfix-vs-min-max.properties", "from+light+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-vs-min-max.properties", "11+grams+to+heavy+pencils/", "w-w", 11, 50);
		getLpseToken("/range/appendages/midfix/midfix-vs-min-max.properties", "from+light+to+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/midfix/midfix-vs-min-max.properties", "11+grams+to+49+grams/", "w-w", 11, 49);
		getLpseToken("/range/appendages/midfix/midfix-vp-vs.properties", "weight+10+grams+to+weight+50+grams/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-vp-vs.properties", "weight+11+grams+to+weight+49+grams/", "w-w", 11, 49);
		getLpseToken("/range/appendages/midfix/midfix-vp-vs-min.properties", "from+light+to+weight+50+grams/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-vp-vs-min.properties", "from+light+to+weight+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/midfix/midfix-vp-vs-max.properties", "weight+10+grams+to+weight+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/midfix/midfix-vp-vs-max.properties", "weight+10+grams+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-vp-vs-min-max.properties", "from+light+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/midfix/midfix-vp-vs-min-max.properties", "weight+11+grams+to+heavy+pencils/", "w-w", 11, 50);
		getLpseToken("/range/appendages/midfix/midfix-vp-vs-min-max.properties", "weight+11+grams+to+weight+49+grams/", "w-w", 11, 49);
		getLpseToken("/range/appendages/midfix/midfix-vp-vs-min-max.properties", "from+light+to+weight+49+grams/", "w-w", 10, 49);


	}


}
