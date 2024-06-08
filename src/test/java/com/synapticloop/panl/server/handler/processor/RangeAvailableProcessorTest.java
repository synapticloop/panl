package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.CollectionRequestHandler;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RangeAvailableProcessorTest {
	@Test void testRangeAdditionURIPrefixMidfixSuffix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcesser(
				"/range/prefix-midfix-suffix.properties",
				"/",
				"",
				10,
				true);
		JSONObject urisObject = jsonObject.getJSONArray(Processor.JSON_KEY_RANGE_FACETS)
				.getJSONObject(0)
				.getJSONObject(Processor.JSON_KEY_URIS);

		assertFalse(urisObject.toString().contains("PANL_WONT_APPEAR"));

		System.out.println(urisObject.toString(2));

		assertEquals("/weighing+from+", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("+to+", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("+grams/w-w/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testRangeAdditionURIPrefixMidfix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcesser(
				"/range/midfix-prefix.properties",
				"/",
				"",
				10,
				true);

		JSONObject urisObject = jsonObject.getJSONArray(Processor.JSON_KEY_RANGE_FACETS)
				.getJSONObject(0)
				.getJSONObject(Processor.JSON_KEY_URIS);

		assertFalse(urisObject.toString().contains("PANL_WONT_APPEAR"));

		System.out.println(urisObject.toString(2));

		assertEquals("/weighing+from+", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("+to+", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("/w-w/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testRangeAdditionURIPrefix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcesser(
				"/range/prefix.properties",
				"/",
				"",
				10,
				true);		JSONObject urisObject = jsonObject.getJSONArray(Processor.JSON_KEY_RANGE_FACETS)
				.getJSONObject(0)
				.getJSONObject(Processor.JSON_KEY_URIS);

		System.out.println(urisObject.toString(2));
		assertFalse(urisObject.toString().contains("PANL_WONT_APPEAR"));

		assertEquals("/this+is+the+prefix", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("/this+is+the+prefix", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("/w+w/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testRangeAdditionURISuffix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcesser(
				"/range/suffix.properties",
				"/",
				"",
				10,
				true);
		JSONObject urisObject = jsonObject.getJSONArray(Processor.JSON_KEY_RANGE_FACETS)
				.getJSONObject(0)
				.getJSONObject(Processor.JSON_KEY_URIS);

		System.out.println(urisObject.toString(2));
		assertFalse(urisObject.toString().contains("PANL_WONT_APPEAR"));

		assertEquals("/", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("+grams/", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("+grams/w+w/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testRangeAdditionURIPrefixSuffix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcesser(
				"/range/prefix-suffix.properties",
				"/",
				"",
				10,
				true);
		JSONObject urisObject = jsonObject.getJSONArray(Processor.JSON_KEY_RANGE_FACETS)
				.getJSONObject(0)
				.getJSONObject(Processor.JSON_KEY_URIS);

		System.out.println(urisObject.toString(2));
		assertFalse(urisObject.toString().contains("PANL_WONT_APPEAR"));

		assertEquals("/this+is+the+prefix", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("this+is+the+suffix/this+is+the+prefix", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("this+is+the+suffix/w+w/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

}
