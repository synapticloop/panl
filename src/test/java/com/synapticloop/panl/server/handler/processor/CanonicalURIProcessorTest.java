package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

public class CanonicalURIProcessorTest {
	private void defaultAssertionHelper(String URIPath, String expect) throws PanlServerException, IOException {
		String uriPath = TestHelper.invokeCanonicalURIProcessor(
				"/default.properties",
				URIPath,
				"");
		assertEquals(expect, uriPath);
	}

	@Test public void testURIPaths() throws PanlServerException, IOException {
		defaultAssertionHelper("/test/default/","/1/10/s-pno+/");
		defaultAssertionHelper("/test/default/brand-name/b/","/1/10/brand-name/s-pno+b/");

		defaultAssertionHelper("/test/default/11/brand-name/wb/","/11/1/10/brand-name/ws-pno+b/");
		defaultAssertionHelper("/test/default/brand-name/11/bw/","/11/1/10/brand-name/ws-pno+b/");

		defaultAssertionHelper("/test/default/11/brand-name/ws+o-b/","/11/1/10/brand-name/ws+pno-b/");

		defaultAssertionHelper("/test/default/11/brand-name/3/7/wbpn/","/11/3/7/brand-name/ws-pno+b/");
	}
}
