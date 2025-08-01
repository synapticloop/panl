package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.fielderiser.field.facet.PanlRangeFacetField;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.FacetLpseToken;
import com.synapticloop.panl.util.Constants;
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
		assertTrue(facetObject.getBoolean(Constants.Json.Panl.IS_RANGE_FACET));

		assertEquals("weight", facetObject.getString(Constants.Json.Panl.FACET_NAME));
		assertEquals("Weight", facetObject.getString(Constants.Json.Panl.NAME));
		assertEquals("w", facetObject.getString(Constants.Json.Panl.PANL_CODE));
		assertEquals("18", facetObject.getString(Constants.Json.Panl.VALUE));
		assertEquals("40", facetObject.getString(Constants.Json.Panl.VALUE_TO));
		assertEquals("/", facetObject.getString(Constants.Json.Panl.REMOVE_URI));
		assertEquals("18~40", facetObject.getString(Constants.Json.Panl.ENCODED));

	}
}
