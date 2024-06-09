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
}
