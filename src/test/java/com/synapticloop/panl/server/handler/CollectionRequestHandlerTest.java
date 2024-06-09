package com.synapticloop.panl.server.handler;
import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.QueryLpseToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class CollectionRequestHandlerTest {
	public static CollectionRequestHandler defaultCollectionRequestHandler;

	@BeforeAll public static void beforeAll() throws PanlServerException, IOException {
		defaultCollectionRequestHandler = TestHelper.getCollectionRequestHandler("/default.properties");
	}

	@Test public void testLpseParsing() throws PanlServerException, IOException {
		for (LpseToken lpseToken : defaultCollectionRequestHandler.parseLpse(
				"/test/default/brand-name/11/model-name/bwm/",
				"q=limited")) {
			assertTrue(lpseToken.getIsValid());
		}
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *
	 *                         QUERY PARAMETERS
	 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	@Test public void testQueryParameterOverride() throws PanlServerException, IOException {
		List<LpseToken> lpseTokens = defaultCollectionRequestHandler.parseLpse(
				"/test/default/uri_parameter/q/",
				"q=query_parameter");
		assertEquals(1, lpseTokens.size());


		LpseToken lpseToken = lpseTokens.get(0);
		assertTrue(lpseToken.getIsValid());
		assertInstanceOf(QueryLpseToken.class, lpseToken);
		assertEquals("q", lpseToken.getLpseCode());
		assertEquals("query_parameter", lpseToken.getValue());
	}

	@Test public void testQueryParameter() throws PanlServerException, IOException {
		List<LpseToken> lpseTokens = defaultCollectionRequestHandler.parseLpse(
				"/test/default/",
				"q=query_parameter");
		assertEquals(1, lpseTokens.size());


		LpseToken lpseToken = lpseTokens.get(0);
		assertTrue(lpseToken.getIsValid());
		assertInstanceOf(QueryLpseToken.class, lpseToken);
		assertEquals("q", lpseToken.getLpseCode());
		assertEquals("query_parameter", lpseToken.getValue());
	}
	@Test public void testQueryURIPath() throws PanlServerException, IOException {
		List<LpseToken> lpseTokens = defaultCollectionRequestHandler.parseLpse(
				"/test/default/uri_parameter/q/",
				"");
		assertEquals(1, lpseTokens.size());


		LpseToken lpseToken = lpseTokens.get(0);
		assertTrue(lpseToken.getIsValid());
		assertInstanceOf(QueryLpseToken.class, lpseToken);
		assertEquals("q", lpseToken.getLpseCode());
		assertEquals("uri_parameter", lpseToken.getValue());
	}


}
