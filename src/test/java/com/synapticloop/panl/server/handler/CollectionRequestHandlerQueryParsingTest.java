package com.synapticloop.panl.server.handler;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.param.QueryLpseToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.synapticloop.panl.TestHelper.DEFAULT_HANDLER;
import static org.junit.jupiter.api.Assertions.*;


public class CollectionRequestHandlerQueryParsingTest {
	@BeforeAll public static void beforeAll() throws PanlServerException, IOException {
		TestHelper.beforeAll();
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *
	 *                         QUERY PARAMETERS
	 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	@Test public void testQueryParameterOverride() throws PanlServerException, IOException {
		List<LpseToken> lpseTokens = DEFAULT_HANDLER.parseLpse(
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
		List<LpseToken> lpseTokens = DEFAULT_HANDLER.parseLpse(
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
		List<LpseToken> lpseTokens = DEFAULT_HANDLER.parseLpse(
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
