package com.synapticloop.panl.server.handler.tokeniser.token;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.generator.bean.PanlCollection;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.FacetLpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.RangeFacetLpseToken;
import org.junit.jupiter.api.Test;

import java.util.StringTokenizer;

import static com.synapticloop.panl.TestHelper.getLpseTokeniser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RangeFacetLpseTokenTest {
	private void testDefaults(String propertiesLocation, String uri) {
		CollectionProperties collectionProperties = TestHelper.getCollectionProperties(propertiesLocation);
		StringTokenizer stringTokenizer = new StringTokenizer(uri, "/", false);

		RangeFacetLpseToken rangeFacetLpseToken = (RangeFacetLpseToken) LpseToken.getLpseToken(collectionProperties, "w", "", stringTokenizer, getLpseTokeniser("-w"));
		assertTrue(rangeFacetLpseToken.getIsValid());
		assertEquals("10", rangeFacetLpseToken.getValue());
		assertEquals("50", rangeFacetLpseToken.getToValue());
	}

	@Test public void testPreMidSufRangeDecoding() {
		testDefaults("/range/prefix-infix-suffix.properties", "/weighing+from+10+to+50+grams/");
		testDefaults("/range/prefix-infix-suffix.properties", "/from+light+to+heavy+pencils/");
		testDefaults("/range/prefix-infix-suffix.properties", "/10+grams+to+heavy+pencils/");
		testDefaults("/range/prefix-infix-suffix.properties", "/from+light+to+50+grams/");
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
		testDefaults("/range/prefix-infix.properties", "/weighing+10+to+weighing+50/");
		testDefaults("/range/prefix-infix.properties", "/from+light+to+heavy+pencils/");
		testDefaults("/range/prefix-infix.properties", "/weighing+10+to+heavy+pencils/");
		testDefaults("/range/prefix-infix.properties", "/from+light+to+weighing+50/");
	}

	private void getLpseToken(String propertiesLocation, String uriPath, String lpsePath, int from, int to) {
		CollectionProperties collectionProperties = TestHelper.getCollectionProperties(propertiesLocation);
		StringTokenizer stringTokenizer = new StringTokenizer(uriPath, "/", false);
		LpseTokeniser lpseTokeniser = new LpseTokeniser(lpsePath, PanlCollection.CODES_AND_METADATA, true);
		String lpseToken = lpseTokeniser.nextToken();
		RangeFacetLpseToken rangeFacetLpseToken = (RangeFacetLpseToken) LpseToken.getLpseToken(
				collectionProperties,
				lpseToken,
				"",
				stringTokenizer,
				lpseTokeniser);
		System.out.println(rangeFacetLpseToken.explain());

		assertTrue(rangeFacetLpseToken.getIsValid());
		assertEquals(from, Integer.parseInt(rangeFacetLpseToken.getValue()));
		assertEquals(to, Integer.parseInt(rangeFacetLpseToken.getToValue()));
	}

	@Test public void testAppendageVariations() {
		getLpseToken("/range/appendages/no-infix/no-infix.properties", "/10~50/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix.properties", "/10~50/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix.properties", "10~50/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vp.properties", "weight+10~weight+50/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vs.properties", "10+grams~50+grams/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vs-vp.properties", "weight+10+grams~weight+50+grams/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-min.properties", "from+light~50/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-max.properties", "10~heavy+pencils/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-min-max.properties", "from+light~heavy+pencils/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vp-min.properties", "from+light~weight+50/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vp-min.properties", "weight+11~weight+50/", "w+w", 11, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vp-max.properties", "weight+10~heavy+pencils/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vp-max.properties", "weight+10~weight+49/", "w+w", 10, 49);
		getLpseToken("/range/appendages/no-infix/no-infix-vp-min-max.properties", "from+light~heavy+pencils/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vp-min-max.properties", "weight+11~heavy+pencils/", "w+w", 11, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vp-min-max.properties", "from+light~weight+49/", "w+w", 10, 49);
		getLpseToken("/range/appendages/no-infix/no-infix-vp-min-max.properties", "weight+11~weight+49/", "w+w", 11, 49);
		getLpseToken("/range/appendages/no-infix/no-infix-vs-min.properties", "from+light~50+grams/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vs-min.properties", "11+grams~50+grams/", "w+w", 11, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vs-max.properties", "10+grams~heavy+pencils/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vs-max.properties", "10+grams~49+grams/", "w+w", 10, 49);
		getLpseToken("/range/appendages/no-infix/no-infix-vs-min-max.properties", "from+light~heavy+pencils/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vs-min-max.properties", "11+grams~heavy+pencils/", "w+w", 11, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vs-min-max.properties", "from+light~49+grams/", "w+w", 10, 49);
		getLpseToken("/range/appendages/no-infix/no-infix-vs-min-max.properties", "11+grams~49+grams/", "w+w", 11, 49);
		getLpseToken("/range/appendages/no-infix/no-infix-vs-vp-min.properties", "from+light~weight+50+grams/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vs-vp-min.properties", "weight+11+grams~weight+50+grams/", "w+w", 11, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vs-vp-max.properties", "weight+10+grams~heavy+pencils/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vs-vp-max.properties", "weight+10+grams~weight+49+grams/", "w+w", 10, 49);
		getLpseToken("/range/appendages/no-infix/no-infix-vs-vp-min-max.properties", "from+light~heavy+pencils/", "w+w", 10, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vs-vp-min-max.properties", "weight+11+grams~heavy+pencils/", "w+w", 11, 50);
		getLpseToken("/range/appendages/no-infix/no-infix-vs-vp-min-max.properties", "from+light~weight+49+grams/", "w+w", 10, 49);

		getLpseToken("/range/appendages/infix/infix.properties", "10+to+50/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-min.properties", "from+light+to+50/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-min.properties", "11+to+50/", "w-w", 11, 50);
		getLpseToken("/range/appendages/infix/infix-min.properties", "11+to+49/", "w-w", 11, 49);
		getLpseToken("/range/appendages/infix/infix-max.properties", "10+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-max.properties", "10+to+49/", "w-w", 10, 49);
		getLpseToken("/range/appendages/infix/infix-max.properties", "11+to+49/", "w-w", 11, 49);
		getLpseToken("/range/appendages/infix/infix-min-max.properties", "from+light+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-min-max.properties", "11+to+heavy+pencils/", "w-w", 11, 50);
		getLpseToken("/range/appendages/infix/infix-min-max.properties", "from+light+to+49/", "w-w", 10, 49);
		getLpseToken("/range/appendages/infix/infix-min-max.properties", "11+to+49/", "w-w", 11, 49);
		getLpseToken("/range/appendages/infix/infix-rp.properties", "weighing+10+to+50/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-rp-min.properties", "from+light+to+50/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-rp-min.properties", "weighing+11+to+50/", "w-w", 11, 50);
		getLpseToken("/range/appendages/infix/infix-rp-max.properties", "weighing+10+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-rp-max.properties", "weighing+10+to+49/", "w-w", 10, 49);
		getLpseToken("/range/appendages/infix/infix-rp-min-max.properties", "from+light+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-rp-min-max.properties", "weighing+11+to+heavy+pencils/", "w-w", 11, 50);
		getLpseToken("/range/appendages/infix/infix-rp-min-max.properties", "from+light+to+49/", "w-w", 10, 49);
		getLpseToken("/range/appendages/infix/infix-rp-min-max.properties", "weighing+11+to+49/", "w-w", 11, 49);
		getLpseToken("/range/appendages/infix/infix-rs.properties", "10+to+50+grams/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-rs-min.properties", "from+light+to+50+grams/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-rs-min.properties", "11+to+50+grams/", "w-w", 11, 50);
		getLpseToken("/range/appendages/infix/infix-rs-max.properties", "10+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-rs-max.properties", "10+to+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/infix/infix-rs-min-max.properties", "from+light+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-rs-min-max.properties", "11+to+heavy+pencils/", "w-w", 11, 50);
		getLpseToken("/range/appendages/infix/infix-rs-min-max.properties", "from+light+to+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/infix/infix-rs-min-max.properties", "11+to+49+grams/", "w-w", 11, 49);
		getLpseToken("/range/appendages/infix/infix-rp-rs.properties", "weighing+10+to+50+grams/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-rp-rs-min.properties", "from+light+to+50+grams/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-rp-rs-min.properties", "from+light+to+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/infix/infix-rp-rs-min.properties", "weighing+11+to+50+grams/", "w-w", 11, 50);
		getLpseToken("/range/appendages/infix/infix-rp-rs-min.properties", "weighing+11+to+49+grams/", "w-w", 11, 49);
		getLpseToken("/range/appendages/infix/infix-rp-rs-max.properties", "weighing+10+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-rp-rs-max.properties", "weighing+11+to+heavy+pencils/", "w-w", 11, 50);
		getLpseToken("/range/appendages/infix/infix-rp-rs-max.properties", "weighing+11+to+49+grams/", "w-w", 11, 49);
		getLpseToken("/range/appendages/infix/infix-rp-rs-max.properties", "weighing+10+to+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/infix/infix-rp-rs-min-max.properties", "from+light+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-rp-rs-min-max.properties", "weighing+11+to+heavy+pencils/", "w-w", 11, 50);
		getLpseToken("/range/appendages/infix/infix-rp-rs-min-max.properties", "weighing+11+to+49+grams/", "w-w", 11, 49);
		getLpseToken("/range/appendages/infix/infix-rp-rs-min-max.properties", "from+light+to+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/infix/infix-vp.properties", "weight+10+to+weight+50/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-vp-min.properties", "from+light+to+weight+50/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-vp-min.properties", "weight+11+to+weight+50/", "w-w", 11, 50);
		getLpseToken("/range/appendages/infix/infix-vp-max.properties", "weight+10+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-vp-max.properties", "weight+10+to+weight+49/", "w-w", 10, 49);
		getLpseToken("/range/appendages/infix/infix-vp-min-max.properties", "from+light+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-vp-min-max.properties", "weight+11+to+heavy+pencils/", "w-w", 11, 50);
		getLpseToken("/range/appendages/infix/infix-vp-min-max.properties", "from+light+to+weight+49/", "w-w", 10, 49);
		getLpseToken("/range/appendages/infix/infix-vp-min-max.properties", "weight+11+to+weight+49/", "w-w", 11, 49);
		getLpseToken("/range/appendages/infix/infix-vs.properties", "10+grams+to+50+grams/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-vs-min.properties", "from+light+to+50+grams/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-vs-min.properties", "11+grams+to+50+grams/", "w-w", 11, 50);
		getLpseToken("/range/appendages/infix/infix-vs-max.properties", "10+grams+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-vs-max.properties", "10+grams+to+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/infix/infix-vs-min-max.properties", "from+light+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-vs-min-max.properties", "11+grams+to+heavy+pencils/", "w-w", 11, 50);
		getLpseToken("/range/appendages/infix/infix-vs-min-max.properties", "from+light+to+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/infix/infix-vs-min-max.properties", "11+grams+to+49+grams/", "w-w", 11, 49);
		getLpseToken("/range/appendages/infix/infix-vp-vs.properties", "weight+10+grams+to+weight+50+grams/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-vp-vs.properties", "weight+11+grams+to+weight+49+grams/", "w-w", 11, 49);
		getLpseToken("/range/appendages/infix/infix-vp-vs-min.properties", "from+light+to+weight+50+grams/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-vp-vs-min.properties", "from+light+to+weight+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/infix/infix-vp-vs-max.properties", "weight+10+grams+to+weight+49+grams/", "w-w", 10, 49);
		getLpseToken("/range/appendages/infix/infix-vp-vs-max.properties", "weight+10+grams+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-vp-vs-min-max.properties", "from+light+to+heavy+pencils/", "w-w", 10, 50);
		getLpseToken("/range/appendages/infix/infix-vp-vs-min-max.properties", "weight+11+grams+to+heavy+pencils/", "w-w", 11, 50);
		getLpseToken("/range/appendages/infix/infix-vp-vs-min-max.properties", "weight+11+grams+to+weight+49+grams/", "w-w", 11, 49);
		getLpseToken("/range/appendages/infix/infix-vp-vs-min-max.properties", "from+light+to+weight+49+grams/", "w-w", 10, 49);


	}


}
