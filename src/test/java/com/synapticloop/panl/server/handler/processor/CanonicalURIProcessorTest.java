package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

public class CanonicalURIProcessorTest {
	@Test public void testURIPaths() throws PanlServerException, IOException {
		TestHelper.assertCanonicalURI("/test/default/","/1/10/pn/");
		TestHelper.assertCanonicalURI("/test/default/brand-name/b/","/1/10/brand-name/pnb/");

		TestHelper.assertCanonicalURI("/test/default/11/brand-name/wb/","/11/1/10/brand-name/wpnb/");
		TestHelper.assertCanonicalURI("/test/default/brand-name/11/bw/","/11/1/10/brand-name/wpnb/");

		TestHelper.assertCanonicalURI("/test/default/11/brand-name/ws+o-b/","/11/1/10/brand-name/wpno-b/");

		TestHelper.assertCanonicalURI("/test/default/11/brand-name/3/7/wbpn/","/11/3/7/brand-name/wpnb/");
	}

	@Test public void testRangeReplacements() throws PanlServerException, IOException {
//		assertCanonicalNoMidfix("/range/appendages/no-midfix.properties","10~50");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vp.properties","weight+10~weight+50");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vs.properties","10+grams~50+grams");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vs-vp.properties","weight+10+grams~weight+50+grams");

//		assertCanonicalNoMidfix("/range/appendages/no-midfix-min.properties","from+light~50");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-max.properties","10~heavy+pencils");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-min-max.properties","from+light~heavy+pencils");

//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vp-min.properties","from+light~weight+50");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vp-min.properties","weight+11~weight+50");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vp-max.properties","weight+10~heavy+pencils");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vp-max.properties","weight+10~weight+49");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vp-min-max.properties","from+light~heavy+pencils");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vp-min-max.properties","weight+11~heavy+pencils");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vp-min-max.properties","from+light~weight+49");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vp-min-max.properties","weight+11~weight+49");

//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vs-min.properties","from+light~50+grams");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vs-min.properties","11+grams~50+grams");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vs-max.properties","10+grams~heavy+pencils");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vs-max.properties","10+grams~49+grams");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vs-min-max.properties","from+light~heavy+pencils");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vs-min-max.properties","11+grams~heavy+pencils");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vs-min-max.properties","from+light~49+grams");
//		assertCanonicalNoMidfix("/range/appendages/no-midfix-vs-min-max.properties","11+grams~49+grams");


//		assertCanonicalMidfix("/range/appendages/midfix.properties","10+to+50");
//		assertCanonicalMidfix("/range/appendages/midfix-rp.properties","weighing+10+to+50");
		assertCanonicalMidfix("/range/appendages/midfix-rs.properties","10+to+50+grams");
//		assertCanonicalMidfix("/range/appendages/midfix-rp-rs.properties","weighing+10+to+50+grams");
//		assertCanonicalMidfix("/range/appendages/midfix-vp.properties","weight+10+to+weight+50");
//		assertCanonicalMidfix("/range/appendages/midfix-vs.properties","10+grams+to+50+grams");
//		assertCanonicalMidfix("/range/appendages/midfix-vp-vs.properties","weight+10+grams+to+weight+50+grams");
	}

	private void assertCanonicalNoMidfix(String propertiesLocation, String uriPath) {
		TestHelper.assertCanonicalURI(
				propertiesLocation,
				"/test/default/" + uriPath + "/w+w/",
				"/" + uriPath + "/1/10/w+wpn/");

	}
	private void assertCanonicalMidfix(String propertiesLocation, String URIpath) {
		TestHelper.assertCanonicalURI(
				propertiesLocation,
				"/test/default/" + URIpath + "/w-w/",
				"/" + URIpath + "/1/10/w-wpn/");

	}

}
