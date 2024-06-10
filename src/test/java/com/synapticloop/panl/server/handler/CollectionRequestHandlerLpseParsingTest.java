package com.synapticloop.panl.server.handler;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.synapticloop.panl.TestHelper.DEFAULT_HANDLER;
import static com.synapticloop.panl.TestHelper.DEFAULT_OR_HANDLER;
import static com.synapticloop.panl.TestHelper.DEFAULT_LPSE_LENGTH_TWO_HANDLER;
import static org.junit.jupiter.api.Assertions.*;


public class CollectionRequestHandlerLpseParsingTest {

	@BeforeAll public static void beforeAll() throws PanlServerException, IOException {
		TestHelper.beforeAll();
	}

	@Test public void testLpseParsingLength2() throws PanlServerException, IOException {
		for (LpseToken lpseToken : DEFAULT_LPSE_LENGTH_TWO_HANDLER.parseLpse(
				"/test/default/brand-name/11/model-name/bbwwmm/",
				"q=limited")) {
			assertTrue(lpseToken.getIsValid());
		}

		for (LpseToken lpseToken : DEFAULT_LPSE_LENGTH_TWO_HANDLER.parseLpse(
				"/test/default/brand-name/11/model-name/bbwwmmsbb+/",
				"q=limited")) {
			assertTrue(lpseToken.getIsValid());
		}

		for (LpseToken lpseToken : DEFAULT_LPSE_LENGTH_TWO_HANDLER.parseLpse(
				"/test/default/brand-name/11/model-name/bbwwmmsbb+smm-/",
				"q=limited")) {
			assertTrue(lpseToken.getIsValid());
		}

	}

	@Test public void testLpseParsing() {
		for (LpseToken lpseToken : DEFAULT_HANDLER.parseLpse(
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
		for (LpseToken lpseToken : DEFAULT_OR_HANDLER.parseLpse(uriPath, "")) {
			assertFalse(lpseToken.getIsValid());
		}
	}

	/**
	 * <p>Bug where if only one token was left for lpse number 2 and there were
	 * no more tokens </p>
	 */
	@Test public void testTripleM() {
		List<LpseToken> lpseTokens = DEFAULT_LPSE_LENGTH_TWO_HANDLER.parseLpse(
				"/test/default/Black/mmm/",
				"");
	}

}
