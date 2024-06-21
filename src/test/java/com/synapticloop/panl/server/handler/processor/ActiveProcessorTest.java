package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.FacetLpseToken;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.synapticloop.panl.server.handler.processor.AvailableProcessorTest.WEIGHT_FACETS;
import static org.junit.jupiter.api.Assertions.*;

public class ActiveProcessorTest {
	@Test public void testRangeFacet() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeActiveProcessor(
				TestHelper.DEFAULT_PROPERTIES,
				"/test/default/18~40/w+w/",
				"");

		System.out.println(jsonObject.toString(2));
		JSONArray facetArray = jsonObject.getJSONArray(FacetLpseToken.TOKEN_TYPE);
		assertEquals(1, facetArray.length());

		JSONObject facetObject = facetArray.getJSONObject(0);
		assertTrue(facetObject.getBoolean(Processor.JSON_KEY_IS_RANGE_FACET));
		assertFalse(facetObject.getBoolean(Processor.JSON_KEY_IS_OR_FACET));

		assertEquals("weight", facetObject.getString(Processor.JSON_KEY_FACET_NAME));
		assertEquals("Weight", facetObject.getString(Processor.JSON_KEY_NAME));
		assertEquals("w", facetObject.getString(Processor.JSON_KEY_PANL_CODE));
		assertEquals("18", facetObject.getString(Processor.JSON_KEY_VALUE));
		assertEquals("40", facetObject.getString(Processor.JSON_KEY_VALUE_TO));
		assertEquals("/", facetObject.getString(Processor.JSON_KEY_REMOVE_URI));
		assertEquals("18~40", facetObject.getString(Processor.JSON_KEY_ENCODED));

	}
}
