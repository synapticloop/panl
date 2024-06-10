package com.synapticloop.panl.server.handler.tokeniser.token;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.StringTokenizer;

import static org.junit.jupiter.api.Assertions.*;
import static com.synapticloop.panl.TestHelper.*;

public class PassThroughLpseTokenTest {
	@Test public void testPassThrough() throws PanlServerException, IOException {
		testEquals("/this-should-be-ignored/", "this-should-be-ignored");
		testEquals("/this+should+be+ignored/", "this should be ignored");
		testEquals("/this should be ignored/", "this should be ignored");
	}

	private void testEquals(String uriPath, String expected) throws PanlServerException, IOException {
		StringTokenizer valueTokeniser = new StringTokenizer(uriPath, "/", false);

		PassThroughLpseToken passThroughLpseToken = new PassThroughLpseToken(
				TestHelper.getCollectionProperties(DEFAULT_PROPERTIES),
				"z",
				valueTokeniser);

		assertTrue(passThroughLpseToken.getIsValid());
		assertEquals(expected, passThroughLpseToken.getValue());
	}

	/**
	 * <p>This is done, because changing the type can cause issues with the other
	 * components.</p>
	 */
	@Test public void testType()  {
		StringTokenizer valueTokeniser = new StringTokenizer("/something/", "/", false);

		PassThroughLpseToken passThroughLpseToken = new PassThroughLpseToken(
				TestHelper.getCollectionProperties(DEFAULT_PROPERTIES),
				"z",
				valueTokeniser);

		assertEquals("passthrough", passThroughLpseToken.getType());
	}

	@Test public void testExplain()  {
		StringTokenizer valueTokeniser = new StringTokenizer("/something/", "/", false);

		PassThroughLpseToken passThroughLpseToken = new PassThroughLpseToken(
				TestHelper.getCollectionProperties(DEFAULT_PROPERTIES),
				"z",
				valueTokeniser);
		assertTrue(passThroughLpseToken.explain().contains("VALID"));
	}
}
