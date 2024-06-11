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


public class CollectionRequestHandlerResponseParsingTest {

	@BeforeAll public static void beforeAll() throws PanlServerException, IOException {
		TestHelper.beforeAll();
	}

}
