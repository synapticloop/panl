package com.synapticloop.panl.server.handler;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.synapticloop.panl.TestHelper.DEFAULT_HANDLER;
import static org.junit.jupiter.api.Assertions.*;

public class CollectionRequestHandlerBooleanFacetTest {
	@BeforeAll public static void beforeAll() throws PanlServerException, IOException {
		TestHelper.beforeAll();
	}
	@Test public void parseTrueFalse() {
		for (LpseToken lpseToken : DEFAULT_HANDLER.parseLpse(
				"/test/default/able+to+be+disassembled/D/",
				"")) {
			assertTrue(lpseToken.getIsValid());
			assertEquals("true", lpseToken.getValue());
		}

		for (LpseToken lpseToken : DEFAULT_HANDLER.parseLpse(
				"/test/default/cannot+be+disassembled/D/",
				"")) {
			assertTrue(lpseToken.getIsValid());
			assertEquals("false", lpseToken.getValue());
		}

		for (LpseToken lpseToken : DEFAULT_HANDLER.parseLpse(
				"/test/default/this-is-not-valid/D/",
				"")) {
			assertFalse(lpseToken.getIsValid());
			assertNull(lpseToken.getValue());
		}

		for (LpseToken lpseToken : DEFAULT_HANDLER.parseLpse(
				"/test/default/true/D/",
				"")) {
			assertFalse(lpseToken.getIsValid());
			assertNull(lpseToken.getValue());
		}

		for (LpseToken lpseToken : DEFAULT_HANDLER.parseLpse(
				"/test/default/false/D/",
				"")) {
			assertFalse(lpseToken.getIsValid());
			assertNull(lpseToken.getValue());
		}

	}
}
