package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryOperandProcessorTest {

	@Test public void testQueryOperand() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeQueryOperandProcessor(
				"/default.properties",
				"/test/default/",
				"");

		assertEquals("/o-/", jsonObject.getString("OR"));
		assertEquals("/o+/", jsonObject.getString("AND"));
	}

	@Test public void testQueryOperandWeight() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeQueryOperandProcessor(
				"/default.properties",
				"/test/default/11/w/",
				"");

		assertEquals("/11/wo-/", jsonObject.getString("OR"));
		assertEquals("/11/wo+/", jsonObject.getString("AND"));
	}

	@Test public void testQueryOperandBrand() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeQueryOperandProcessor(
				"/default.properties",
				"/test/default/brand-name/b/",
				"");

		assertEquals("/brand-name/o-b/", jsonObject.getString("OR"));
		assertEquals("/brand-name/o+b/", jsonObject.getString("AND"));
	}

	@Test public void testResetPagination() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeQueryOperandProcessor(
				"/default.properties",
				"/test/default/2/p/",
				"");

		assertEquals("/o-/", jsonObject.getString("OR"));
		assertEquals("/o+/", jsonObject.getString("AND"));
	}

	@Test public void testResetPaginationWithNumPerPage() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeQueryOperandProcessor(
				"/default.properties",
				"/test/default/2/3/pn/",
				"");

		assertEquals("/3/no-/", jsonObject.getString("OR"));
		assertEquals("/3/no+/", jsonObject.getString("AND"));
	}

	@Test public void testReplacementAnd() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeQueryOperandProcessor(
				"/default.properties",
				"/test/default/o+/",
				"");

		assertEquals("/o-/", jsonObject.getString("OR"));
		assertEquals("/o+/", jsonObject.getString("AND"));
	}

	@Test public void testReplacementOr() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeQueryOperandProcessor(
				"/default.properties",
				"/test/default/o-/",
				"");

		assertEquals("/o-/", jsonObject.getString("OR"));
		assertEquals("/o+/", jsonObject.getString("AND"));
	}

}
