package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.synapticloop.panl.server.handler.processor.AvailableProcessorTest.WEIGHT_FACETS;
import static com.synapticloop.panl.server.handler.processor.AvailableProcessorTest.WEIGHT_NAME_FACETS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class AvailableProcessorRangeFacetTest {

	@Test void testRangeAdditionURIPrefixInfixSuffix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(
				WEIGHT_FACETS,
				"/range/prefix-infix-suffix.properties",
				"/",
				"",
				10,
				true);

		JSONObject urisObject = jsonObject.getJSONArray(Processor.JSON_KEY_RANGE_FACETS)
				.getJSONObject(0)
				.getJSONObject(Processor.JSON_KEY_URIS);

		assertFalse(urisObject.toString().contains("PANL_WONT_APPEAR"));

		System.out.println(urisObject.toString(2));

		assertEquals("/weighing%20from%20", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("%20to%20", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("%20grams/w-/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testRangeAdditionURIPrefixInfix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(
				WEIGHT_FACETS,
				"/range/prefix-infix.properties",
				"/",
				"",
				10,
				true);

		JSONObject urisObject = jsonObject.getJSONArray(Processor.JSON_KEY_RANGE_FACETS)
				.getJSONObject(0)
				.getJSONObject(Processor.JSON_KEY_URIS);

		assertFalse(urisObject.toString().contains("PANL_WONT_APPEAR"));

		System.out.println(urisObject.toString(2));

		assertEquals("/weighing%20", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("%20to%20", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("/w-/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testRangeAdditionURIPrefix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(
				WEIGHT_FACETS,
				"/range/prefix.properties",
				"/",
				"",
				10,
				true);
		JSONObject urisObject = jsonObject.getJSONArray(Processor.JSON_KEY_RANGE_FACETS)
				.getJSONObject(0)
				.getJSONObject(Processor.JSON_KEY_URIS);

		System.out.println(urisObject.toString(2));
		assertFalse(urisObject.toString().contains("PANL_WONT_APPEAR"));

		assertEquals("/this%20is%20the%20prefix", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("~this%20is%20the%20prefix", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("/w+/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testRangeAdditionURISuffix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(
				WEIGHT_FACETS,
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
		assertEquals("%20grams~", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("%20grams/w+/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testRangeAdditionURIPrefixSuffix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(
				WEIGHT_FACETS,
				"/range/prefix-suffix.properties",
				"/",
				"",
				10,
				true);
		System.out.println(jsonObject.toString(2));

		JSONObject urisObject = jsonObject.getJSONArray(Processor.JSON_KEY_RANGE_FACETS)
				.getJSONObject(0)
				.getJSONObject(Processor.JSON_KEY_URIS);

		System.out.println(urisObject.toString(2));
		assertFalse(urisObject.toString().contains("PANL_WONT_APPEAR"));

		assertEquals("/this%20is%20the%20prefix", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("this%20is%20the%20suffix~this%20is%20the%20prefix", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("this%20is%20the%20suffix/w+/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

//	@Test

	void testExistingRange() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(
				WEIGHT_FACETS,
				"/range/prefix-infix-suffix.properties",
				"/test/default/weighing%20from%2018%20to%2035%20grams/w-/",
				"",
				10,
				true);

		JSONObject firstRangeFacetObject = jsonObject.getJSONArray(Processor.JSON_KEY_RANGE_FACETS)
				.getJSONObject(0);

		JSONObject urisObject = firstRangeFacetObject
				.getJSONObject(Processor.JSON_KEY_URIS);

		System.out.println(jsonObject.toString(2));

		assertEquals("18", firstRangeFacetObject.getString(Processor.JSON_KEY_VALUE));
		assertEquals("35", firstRangeFacetObject.getString(Processor.JSON_KEY_VALUE_TO));

		System.out.println(urisObject.toString(2));
		assertFalse(urisObject.toString().contains("PANL_WONT_APPEAR"));

		assertEquals("/weighing%20from%20", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("%20to%20", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("%20grams/w-/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test public void testAdditionOfFacetOnPageNumber() throws PanlServerException, IOException {
		// test with the page number - it shouldn't have a 'p' LPSE code
		assertResetOfPageNumbers("/test/default/2/p/", "/w+/");
		// test with the page number - it shouldn't have a 'p' LPSE code but should
		// have the 'n'
		assertResetOfPageNumbers("/test/default/2/3/pn/", "/3/w+n/");
	}

	private void assertResetOfPageNumbers(String uriPath, String after) throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(
				WEIGHT_FACETS,
				"/range/none.properties",
				uriPath,
				"",
				10,
				true);
		JSONObject urisObject = jsonObject.getJSONArray(Processor.JSON_KEY_RANGE_FACETS)
				.getJSONObject(0)
				.getJSONObject(Processor.JSON_KEY_URIS);

		System.out.println(jsonObject.toString(2));
		System.out.println(urisObject.toString(2));
		assertEquals("/", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("~", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals(after, urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test public void testCanonicalURI() throws PanlServerException, IOException {
		TestHelper.assertCanonicalURI("/test/default/11~18/w+/", "/11~18/1/10/w+pn/");
	}

	@Test public void testNoInfixSuffix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(
				WEIGHT_FACETS,
				"/range/suffix.properties",
				"/test/default/10%20grams~20%20grams/w+/",
				"",
				10,
				true);

		System.out.println(jsonObject.toString(2));
		JSONObject firstRangeFacetObject = jsonObject.getJSONArray(Processor.JSON_KEY_RANGE_FACETS)
				.getJSONObject(0);

		JSONObject urisObject = firstRangeFacetObject
				.getJSONObject(Processor.JSON_KEY_URIS);

		assertEquals("/", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("%20grams~", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("%20grams/w+/", urisObject.getString(Processor.JSON_KEY_AFTER));

	}

}
