package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.TestHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CanonicalURIProcessorRangeTest {
	@Test public void testURIPaths() {
		TestHelper.assertCanonicalURI("/test/default/", "/1/10/pn/");
		TestHelper.assertCanonicalURI("/test/default/brand-name/b/", "/1/10/brand-name/pnb/");

		TestHelper.assertCanonicalURI("/test/default/11/brand-name/wb/", "/11/1/10/brand-name/wpnb/");
		TestHelper.assertCanonicalURI("/test/default/brand-name/11/bw/", "/11/1/10/brand-name/wpnb/");

		TestHelper.assertCanonicalURI("/test/default/11/brand-name/ws+o-b/", "/11/1/10/brand-name/wpno-b/");

		TestHelper.assertCanonicalURI("/test/default/11/brand-name/3/7/wbpn/", "/11/3/7/brand-name/wpnb/");
	}

	@Test public void testRangeReplacements() {
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix.properties", "10~50");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp.properties", "weight+10~weight+50");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs.properties", "10+grams~50+grams");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-vp.properties", "weight+10+grams~weight+50+grams");

		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-min.properties", "from+light~50");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-max.properties", "10~heavy+pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-min-max.properties", "from+light~heavy+pencils");

		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp-min.properties", "from+light~weight+50");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp-min.properties", "weight+11~weight+50");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp-max.properties", "weight+10~heavy+pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp-max.properties", "weight+10~weight+49");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp-min-max.properties", "from+light~heavy+pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp-min-max.properties", "weight+11~heavy+pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp-min-max.properties", "from+light~weight+49");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp-min-max.properties", "weight+11~weight+49");

		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-min.properties", "from+light~50+grams");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-min.properties", "11+grams~50+grams");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-max.properties", "10+grams~heavy+pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-max.properties", "10+grams~49+grams");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-min-max.properties", "from+light~heavy+pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-min-max.properties", "11+grams~heavy+pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-min-max.properties", "from+light~49+grams");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-min-max.properties", "11+grams~49+grams");

		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-vp-min.properties", "from+light~weight+50+grams");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-vp-min.properties", "weight+11+grams~weight+50+grams");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-vp-max.properties", "weight+10+grams~heavy+pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-vp-max.properties", "weight+10+grams~weight+49+grams");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-vp-min-max.properties", "from+light~heavy+pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-vp-min-max.properties", "weight+11+grams~heavy+pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-vp-min-max.properties", "from+light~weight+49+grams");

		assertCanonicalWithInfix("/range/appendages/infix/infix.properties", "10+to+50");
		assertCanonicalWithInfix("/range/appendages/infix/infix-min.properties", "from+light+to+50");
		assertCanonicalWithInfix("/range/appendages/infix/infix-min.properties", "11+to+50");
		assertCanonicalWithInfix("/range/appendages/infix/infix-min.properties", "11+to+49");
		assertCanonicalWithInfix("/range/appendages/infix/infix-max.properties", "10+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-max.properties", "10+to+49");
		assertCanonicalWithInfix("/range/appendages/infix/infix-max.properties", "11+to+49");
		assertCanonicalWithInfix("/range/appendages/infix/infix-min-max.properties", "from+light+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-min-max.properties", "11+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-min-max.properties", "from+light+to+49");
		assertCanonicalWithInfix("/range/appendages/infix/infix-min-max.properties", "11+to+49");


		assertCanonicalWithInfix("/range/appendages/infix/infix-rp.properties", "weighing+10+to+50");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-min.properties", "from+light+to+50");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-min.properties", "weighing+11+to+50");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-max.properties", "weighing+10+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-max.properties", "weighing+10+to+49");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-min-max.properties", "from+light+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-min-max.properties", "weighing+11+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-min-max.properties", "from+light+to+49");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-min-max.properties", "weighing+11+to+49");

		assertCanonicalWithInfix("/range/appendages/infix/infix-rs.properties", "10+to+50+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rs-min.properties", "from+light+to+50+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rs-min.properties", "11+to+50+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rs-max.properties", "10+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rs-max.properties", "10+to+49+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rs-min-max.properties", "from+light+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rs-min-max.properties", "11+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rs-min-max.properties", "from+light+to+49+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rs-min-max.properties", "11+to+49+grams");

		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs.properties", "weighing+10+to+50+grams");

		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-min.properties", "from+light+to+50+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-min.properties", "from+light+to+49+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-min.properties", "weighing+11+to+50+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-min.properties", "weighing+11+to+49+grams");

		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-max.properties", "weighing+10+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-max.properties", "weighing+11+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-max.properties", "weighing+11+to+49+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-max.properties", "weighing+10+to+49+grams");

		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-min-max.properties", "from+light+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-min-max.properties", "weighing+11+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-min-max.properties", "weighing+11+to+49+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-min-max.properties", "from+light+to+49+grams");

		assertCanonicalWithInfix("/range/appendages/infix/infix-vp.properties", "weight+10+to+weight+50");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-min.properties", "from+light+to+weight+50");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-min.properties", "weight+11+to+weight+50");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-max.properties", "weight+10+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-max.properties", "weight+10+to+weight+49");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-min-max.properties", "from+light+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-min-max.properties", "weight+11+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-min-max.properties", "from+light+to+weight+49");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-min-max.properties", "weight+11+to+weight+49");

		assertCanonicalWithInfix("/range/appendages/infix/infix-vs.properties", "10+grams+to+50+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vs-min.properties", "from+light+to+50+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vs-min.properties", "11+grams+to+50+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vs-max.properties", "10+grams+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vs-max.properties", "10+grams+to+49+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vs-min-max.properties", "from+light+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vs-min-max.properties", "11+grams+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vs-min-max.properties", "from+light+to+49+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vs-min-max.properties", "11+grams+to+49+grams");

		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs.properties", "weight+10+grams+to+weight+50+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs.properties", "weight+11+grams+to+weight+50+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs-min.properties", "from+light+to+weight+50+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs-min.properties", "from+light+to+weight+49+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs-max.properties", "weight+10+grams+to+weight+49+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs-max.properties", "weight+10+grams+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs-min-max.properties", "from+light+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs-min-max.properties", "weight+11+grams+to+heavy+pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs-min-max.properties", "weight+11+grams+to+weight+49+grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs-min-max.properties", "from+light+to+weight+49+grams");
	}

	private void assertCanonicalNoInfix(String propertiesLocation, String uriPath) {
		TestHelper.assertCanonicalURI(
				propertiesLocation,
				"/test/default/" + uriPath + "/w+/",
				"/" + uriPath + "/1/10/w+pn/");
	}

	private void assertCanonicalWithInfix(String propertiesLocation, String URIpath) {
		TestHelper.assertCanonicalURI(
				propertiesLocation,
				"/test/default/" + URIpath + "/w-/",
				"/" + URIpath + "/1/10/w-pn/");
	}

}
