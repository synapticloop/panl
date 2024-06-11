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
	public static CollectionRequestHandler defaultHandler;
	public static CollectionRequestHandler defaultOrHandler;
	public static CollectionRequestHandler defaultLpseLengthTwo;

	@BeforeAll public static void beforeAll() throws PanlServerException, IOException {
		defaultHandler = TestHelper.getCollectionRequestHandler("/default.properties");
		defaultOrHandler = TestHelper.getCollectionRequestHandler("/default.or.properties");
		defaultLpseLengthTwo = TestHelper.getCollectionRequestHandler("/default.lpse.length.2.properties");
	}

	@Test public void testLpseParsingLength2() throws PanlServerException, IOException {
		for (LpseToken lpseToken : defaultLpseLengthTwo.parseLpse(
				"/test/default/brand-name/11/model-name/bbwwmm/",
				"q=limited")) {
			assertTrue(lpseToken.getIsValid());
		}

		for (LpseToken lpseToken : defaultLpseLengthTwo.parseLpse(
				"/test/default/brand-name/11/model-name/bbwwmmsbb+/",
				"q=limited")) {
			assertTrue(lpseToken.getIsValid());
		}

		for (LpseToken lpseToken : defaultLpseLengthTwo.parseLpse(
				"/test/default/brand-name/11/model-name/bbwwmmsbb+smm-/",
				"q=limited")) {
			assertTrue(lpseToken.getIsValid());
		}

	}

	@Test public void testLpseParsing() throws PanlServerException, IOException {
		for (LpseToken lpseToken : defaultHandler.parseLpse(
				"/test/default/brand-name/11/model-name/bwm/",
				"q=limited")) {
			assertTrue(lpseToken.getIsValid());
		}
	}

	/**
	 * <p>If the</p>
	 * @throws PanlServerException
	 * @throws IOException
	 */
	@Test public void testParseLpseNoOperands() throws PanlServerException, IOException {
		testInvalidLpseURIs("/default/test/o/");
		testInvalidLpseURIs("/default/test/s/");
		testInvalidLpseURIs("/default/test/w-/");
		testInvalidLpseURIs("/default/test/w+/");
	}

	private void testInvalidLpseURIs(String uriPath) {
		for (LpseToken lpseToken : defaultOrHandler.parseLpse(uriPath, "")) {
			assertFalse(lpseToken.getIsValid());
		}
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *
	 *                         QUERY PARAMETERS
	 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	@Test public void testQueryParameterOverride() throws PanlServerException, IOException {
		List<LpseToken> lpseTokens = defaultHandler.parseLpse(
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
		List<LpseToken> lpseTokens = defaultHandler.parseLpse(
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
		List<LpseToken> lpseTokens = defaultHandler.parseLpse(
				"/test/default/uri_parameter/q/",
				"");
		assertEquals(1, lpseTokens.size());


		LpseToken lpseToken = lpseTokens.get(0);
		assertTrue(lpseToken.getIsValid());
		assertInstanceOf(QueryLpseToken.class, lpseToken);
		assertEquals("q", lpseToken.getLpseCode());
		assertEquals("uri_parameter", lpseToken.getValue());
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *
	 *                                ODD URIs
	 *                       (picked up whilst testing...)
	 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	/**
	 * <p>Bug where if only one token was left for lpse number 2 and there were
	 * no more tokens </p>
	 */
	@Test public void testTripleM() {
		List<LpseToken> lpseTokens = defaultLpseLengthTwo.parseLpse(
				"/test/default/Black/mmm/",
				"");
	}

}
