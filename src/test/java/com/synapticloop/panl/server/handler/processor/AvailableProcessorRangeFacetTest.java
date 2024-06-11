package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class AvailableProcessorRangeFacetTest {

	@Test void testRangeAdditionURIPrefixMidfixSuffix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(
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
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(
				"/range/prefix-midfix.properties",
				"/",
				"",
				10,
				true);

		JSONObject urisObject = jsonObject.getJSONArray(Processor.JSON_KEY_RANGE_FACETS)
				.getJSONObject(0)
				.getJSONObject(Processor.JSON_KEY_URIS);

		assertFalse(urisObject.toString().contains("PANL_WONT_APPEAR"));

		System.out.println(urisObject.toString(2));

		assertEquals("/weighing+", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("+to+", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("/w-w/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testRangeAdditionURIPrefix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(
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

		assertEquals("/this+is+the+prefix", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("~this+is+the+prefix", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("/w+w/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testRangeAdditionURISuffix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(
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
		assertEquals("+grams~", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("+grams/w+w/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testRangeAdditionURIPrefixSuffix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(
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
		assertEquals("this+is+the+suffix~this+is+the+prefix", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("this+is+the+suffix/w+w/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

//	@Test

	void testExistingRange() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(
				"/range/prefix-midfix-suffix.properties",
				"/test/default/weighing+from+18+to+35+grams/w-w/",
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

		assertEquals("/weighing+from+", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("+to+", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("+grams/w-w/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test public void testAdditionOfFacetOnPageNumber() throws PanlServerException, IOException {
		// test with the page number - it shouldn't have a 'p' LPSE code
		assertResetOfPageNumbers("/test/default/2/p/", "/w+w/");
		// test with the page number - it shouldn't have a 'p' LPSE code but should
		// have the 'n'
		assertResetOfPageNumbers("/test/default/2/3/pn/", "/3/w+wn/");
	}

	private void assertResetOfPageNumbers(String uriPath, String after) throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(
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
		TestHelper.assertCanonicalURI("/test/default/11~18/w+w/", "/11~18/1/10/w+wpn/");
	}

	@Test public void testNoMidFixSuffix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(
				"/range/suffix.properties",
				"/test/default/10+grams~20+grams/w+w/",
				"",
				10,
				true);

		System.out.println(jsonObject.toString(2));
		JSONObject firstRangeFacetObject = jsonObject.getJSONArray(Processor.JSON_KEY_RANGE_FACETS)
				.getJSONObject(0);

		JSONObject urisObject = firstRangeFacetObject
				.getJSONObject(Processor.JSON_KEY_URIS);

		assertEquals("/", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("+grams~", urisObject.getString(Processor.JSON_KEY_DURING));
		assertEquals("+grams/w+w/", urisObject.getString(Processor.JSON_KEY_AFTER));

	}

}
