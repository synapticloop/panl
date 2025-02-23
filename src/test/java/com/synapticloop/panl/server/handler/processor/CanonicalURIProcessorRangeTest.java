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
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp.properties", "weight%2010~weight%2050");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs.properties", "10%20grams~50%20grams");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-vp.properties", "weight%2010%20grams~weight%2050%20grams");

		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-min.properties", "from%20light~50");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-max.properties", "10~heavy%20pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-min-max.properties", "from%20light~heavy%20pencils");

		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp-min.properties", "from%20light~weight%2050");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp-min.properties", "weight%2011~weight%2050");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp-max.properties", "weight%2010~heavy%20pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp-max.properties", "weight%2010~weight%2049");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp-min-max.properties", "from%20light~heavy%20pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp-min-max.properties", "weight%2011~heavy%20pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp-min-max.properties", "from%20light~weight%2049");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vp-min-max.properties", "weight%2011~weight%2049");

		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-min.properties", "from%20light~50%20grams");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-min.properties", "11%20grams~50%20grams");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-max.properties", "10%20grams~heavy%20pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-max.properties", "10%20grams~49%20grams");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-min-max.properties", "from%20light~heavy%20pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-min-max.properties", "11%20grams~heavy%20pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-min-max.properties", "from%20light~49%20grams");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-min-max.properties", "11%20grams~49%20grams");

		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-vp-min.properties", "from%20light~weight%2050%20grams");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-vp-min.properties", "weight%2011%20grams~weight%2050%20grams");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-vp-max.properties", "weight%2010%20grams~heavy%20pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-vp-max.properties", "weight%2010%20grams~weight%2049%20grams");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-vp-min-max.properties", "from%20light~heavy%20pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-vp-min-max.properties", "weight%2011%20grams~heavy%20pencils");
		assertCanonicalNoInfix("/range/appendages/no-infix/no-infix-vs-vp-min-max.properties", "from%20light~weight%2049%20grams");

		assertCanonicalWithInfix("/range/appendages/infix/infix.properties", "10%20to%2050");
		assertCanonicalWithInfix("/range/appendages/infix/infix-min.properties", "from%20light%20to%2050");
		assertCanonicalWithInfix("/range/appendages/infix/infix-min.properties", "11%20to%2050");
		assertCanonicalWithInfix("/range/appendages/infix/infix-min.properties", "11%20to%2049");
		assertCanonicalWithInfix("/range/appendages/infix/infix-max.properties", "10%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-max.properties", "10%20to%2049");
		assertCanonicalWithInfix("/range/appendages/infix/infix-max.properties", "11%20to%2049");
		assertCanonicalWithInfix("/range/appendages/infix/infix-min-max.properties", "from%20light%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-min-max.properties", "11%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-min-max.properties", "from%20light%20to%2049");
		assertCanonicalWithInfix("/range/appendages/infix/infix-min-max.properties", "11%20to%2049");


		assertCanonicalWithInfix("/range/appendages/infix/infix-rp.properties", "weighing%2010%20to%2050");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-min.properties", "from%20light%20to%2050");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-min.properties", "weighing%2011%20to%2050");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-max.properties", "weighing%2010%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-max.properties", "weighing%2010%20to%2049");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-min-max.properties", "from%20light%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-min-max.properties", "weighing%2011%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-min-max.properties", "from%20light%20to%2049");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-min-max.properties", "weighing%2011%20to%2049");

		assertCanonicalWithInfix("/range/appendages/infix/infix-rs.properties", "10%20to%2050%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rs-min.properties", "from%20light%20to%2050%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rs-min.properties", "11%20to%2050%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rs-max.properties", "10%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rs-max.properties", "10%20to%2049%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rs-min-max.properties", "from%20light%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rs-min-max.properties", "11%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rs-min-max.properties", "from%20light%20to%2049%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rs-min-max.properties", "11%20to%2049%20grams");

		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs.properties", "weighing%2010%20to%2050%20grams");

		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-min.properties", "from%20light%20to%2050%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-min.properties", "from%20light%20to%2049%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-min.properties", "weighing%2011%20to%2050%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-min.properties", "weighing%2011%20to%2049%20grams");

		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-max.properties", "weighing%2010%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-max.properties", "weighing%2011%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-max.properties", "weighing%2011%20to%2049%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-max.properties", "weighing%2010%20to%2049%20grams");

		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-min-max.properties", "from%20light%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-min-max.properties", "weighing%2011%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-min-max.properties", "weighing%2011%20to%2049%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-rp-rs-min-max.properties", "from%20light%20to%2049%20grams");

		assertCanonicalWithInfix("/range/appendages/infix/infix-vp.properties", "weight%2010%20to%20weight%2050");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-min.properties", "from%20light%20to%20weight%2050");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-min.properties", "weight%2011%20to%20weight%2050");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-max.properties", "weight%2010%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-max.properties", "weight%2010%20to%20weight%2049");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-min-max.properties", "from%20light%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-min-max.properties", "weight%2011%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-min-max.properties", "from%20light%20to%20weight%2049");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-min-max.properties", "weight%2011%20to%20weight%2049");

		assertCanonicalWithInfix("/range/appendages/infix/infix-vs.properties", "10%20grams%20to%2050%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vs-min.properties", "from%20light%20to%2050%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vs-min.properties", "11%20grams%20to%2050%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vs-max.properties", "10%20grams%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vs-max.properties", "10%20grams%20to%2049%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vs-min-max.properties", "from%20light%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vs-min-max.properties", "11%20grams%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vs-min-max.properties", "from%20light%20to%2049%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vs-min-max.properties", "11%20grams%20to%2049%20grams");

		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs.properties", "weight%2010%20grams%20to%20weight%2050%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs.properties", "weight%2011%20grams%20to%20weight%2050%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs-min.properties", "from%20light%20to%20weight%2050%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs-min.properties", "from%20light%20to%20weight%2049%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs-max.properties", "weight%2010%20grams%20to%20weight%2049%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs-max.properties", "weight%2010%20grams%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs-min-max.properties", "from%20light%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs-min-max.properties", "weight%2011%20grams%20to%20heavy%20pencils");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs-min-max.properties", "weight%2011%20grams%20to%20weight%2049%20grams");
		assertCanonicalWithInfix("/range/appendages/infix/infix-vp-vs-min-max.properties", "from%20light%20to%20weight%2049%20grams");
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
