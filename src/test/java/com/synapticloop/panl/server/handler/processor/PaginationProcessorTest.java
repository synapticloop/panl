package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

public class PaginationProcessorTest {
	@Test void testPageAdditionURINeitherWithNext() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.paginationProcesser(
				"/page/page-with-neither.properties",
				"/test/default/",
				"",
				100L
		);
		JSONObject urisObject = jsonObject.getJSONObject(Processor.JSON_KEY_PAGE_URIS);

		System.out.println(urisObject.toString(2));

		assertEquals("/", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("/2/p/", urisObject.getString(Processor.JSON_KEY_NEXT));
		assertEquals("/p/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testPageAdditionURINeitherWithBoth() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.paginationProcesser(
				"/page/page-with-neither.properties",
				"/test/default/2/p/",
				"",
				100L
		);
		JSONObject urisObject = jsonObject.getJSONObject(Processor.JSON_KEY_PAGE_URIS);

		System.out.println(urisObject.toString(2));

		assertEquals("/", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("/3/p/", urisObject.getString(Processor.JSON_KEY_NEXT));
		assertEquals("/1/p/", urisObject.getString(Processor.JSON_KEY_PREVIOUS));
		assertEquals("/p/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testPageAdditionURINeitherWithoutPrevious() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.paginationProcesser(
				"/page/page-with-neither.properties",
				"/test/default/10/p/",
				"",
				100L
		);
		JSONObject urisObject = jsonObject.getJSONObject(Processor.JSON_KEY_PAGE_URIS);

		System.out.println(urisObject.toString(2));

		assertEquals("/", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertNull(urisObject.optString(Processor.JSON_KEY_NEXT, null));
		assertEquals("/9/p/", urisObject.getString(Processor.JSON_KEY_PREVIOUS));
		assertEquals("/p/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testPageAdditionURIPrefixSuffix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.paginationProcesser(
				"/page/page-with-prefix-suffix.properties",
				"/test/default/",
				"",
				100L
		);
		JSONObject urisObject = jsonObject.getJSONObject(Processor.JSON_KEY_PAGE_URIS);

		System.out.println(urisObject.toString(2));

		assertEquals("/this-is-the-page-prefix", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("/this-is-the-page-prefix2this-is-the-page-suffix/p/", urisObject.getString(Processor.JSON_KEY_NEXT));
		assertEquals("this-is-the-page-suffix/p/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testPageAdditionURIPrefix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.paginationProcesser(
				"/page/page-with-prefix.properties",
				"/test/default/",
				"",
				100L
		);
		JSONObject urisObject = jsonObject.getJSONObject(Processor.JSON_KEY_PAGE_URIS);

		System.out.println(urisObject.toString(2));

		assertEquals("/this-is-the-page-prefix", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("/this-is-the-page-prefix2/p/", urisObject.getString(Processor.JSON_KEY_NEXT));
		assertEquals("/p/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testPageAdditionURISuffix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.paginationProcesser(
				"/page/page-with-suffix.properties",
				"/test/default/",
				"",
				100L
		);
		JSONObject urisObject = jsonObject.getJSONObject(Processor.JSON_KEY_PAGE_URIS);

		System.out.println(urisObject.toString(2));
		assertEquals(1, jsonObject.getInt(Processor.JSON_KEY_PAGE_NUM));
		assertEquals(10, jsonObject.getInt(Processor.JSON_KEY_NUM_PAGES));
		assertEquals(10, jsonObject.getInt(Processor.JSON_KEY_NUM_PER_PAGE));

		assertEquals("/", urisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("/2this-is-the-page-suffix/p/", urisObject.getString(Processor.JSON_KEY_NEXT));
		assertEquals("this-is-the-page-suffix/p/", urisObject.getString(Processor.JSON_KEY_AFTER));
	}


	@Test void testPageAdditionURIPrefixSuffixPagination() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.paginationProcesser(
				"/page/default.properties",
				"/test/default/page-prefix-2-page-suffix/p/",
				"",
				100L
		);

		JSONObject pageUrisObject = jsonObject.getJSONObject(Processor.JSON_KEY_PAGE_URIS);
		System.out.println(jsonObject.toString(2));
		assertEquals(2, jsonObject.getInt(Processor.JSON_KEY_PAGE_NUM));
		assertEquals(10, jsonObject.getInt(Processor.JSON_KEY_NUM_PAGES));
		assertEquals(10, jsonObject.getInt(Processor.JSON_KEY_NUM_PER_PAGE));

		assertEquals("/page-prefix-", pageUrisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("/page-prefix-3-page-suffix/p/", pageUrisObject.getString(Processor.JSON_KEY_NEXT));
		assertEquals("/page-prefix-1-page-suffix/p/", pageUrisObject.getString(Processor.JSON_KEY_PREVIOUS));
		assertEquals("-page-suffix/p/", pageUrisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testPageAdditionURIPrefixSuffixPaginationNoNext() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.paginationProcesser(
				"/page/default.properties",
				"/test/default/page-prefix-10-page-suffix/p/",
				"",
				100L
		);
		JSONObject pageUrisObject = jsonObject.getJSONObject(Processor.JSON_KEY_PAGE_URIS);
		System.out.println(jsonObject.toString(2));

		assertEquals(10, jsonObject.getInt(Processor.JSON_KEY_PAGE_NUM));
		assertEquals(10, jsonObject.getInt(Processor.JSON_KEY_NUM_PAGES));
		assertEquals(10, jsonObject.getInt(Processor.JSON_KEY_NUM_PER_PAGE));

		assertEquals("/page-prefix-", pageUrisObject.getString(Processor.JSON_KEY_BEFORE));
		assertNull(pageUrisObject.optString(Processor.JSON_KEY_NEXT, null));
		assertEquals("/page-prefix-9-page-suffix/p/", pageUrisObject.getString(Processor.JSON_KEY_PREVIOUS));
		assertEquals("-page-suffix/p/", pageUrisObject.getString(Processor.JSON_KEY_AFTER));
	}


	private void assertBaseValidity(JSONObject jsonObject) {
		JSONObject pageUrisObject = jsonObject.getJSONObject(Processor.JSON_KEY_PAGE_URIS);

		assertEquals(1, jsonObject.getInt(Processor.JSON_KEY_PAGE_NUM));
		assertEquals(10, jsonObject.getInt(Processor.JSON_KEY_NUM_PAGES));
		assertEquals(10, jsonObject.getInt(Processor.JSON_KEY_NUM_PER_PAGE));

		assertEquals("/page-prefix-", pageUrisObject.getString(Processor.JSON_KEY_BEFORE));
		assertNull(pageUrisObject.optString(Processor.JSON_KEY_PREVIOUS, null));
		assertEquals("/page-prefix-2-page-suffix/p/", pageUrisObject.getString(Processor.JSON_KEY_NEXT));
		assertEquals("-page-suffix/p/", pageUrisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testInvalidPageNumber() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.paginationProcesser(
				"/page/default.properties",
				"/test/default/page-prefix-NOT_AN_INTEGER-page-suffix/p/",
				"",
				100L
		);
		assertBaseValidity(jsonObject);
	}

	@Test void testInvalidPageNumberNegative() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.paginationProcesser(
				"/page/default.properties",
				"/test/default/page-prefix--4-page-suffix/p/",
				"",
				100L
		);
		assertBaseValidity(jsonObject);
	}

	@Test void testAfterCode()  throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.paginationProcesser(
				"/page/default.properties",
				"/test/default/11/page-prefix-2-page-suffix/wp/",
				"",
				100L
		);
		System.out.println(jsonObject.toString(2));
		JSONObject pageUrisObject = jsonObject.getJSONObject(Processor.JSON_KEY_PAGE_URIS);
		assertEquals("/11/page-prefix-", pageUrisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("/11/page-prefix-3-page-suffix/wp/", pageUrisObject.getString(Processor.JSON_KEY_NEXT));
		assertEquals("/11/page-prefix-1-page-suffix/wp/", pageUrisObject.getString(Processor.JSON_KEY_PREVIOUS));
		assertEquals("-page-suffix/wp/", pageUrisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testBeforeCode()  throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.paginationProcesser(
				"/page/default.properties",
				"/test/default/page-prefix-2-page-suffix/brand-name/pb/",
				"",
				100L
		);
		System.out.println(jsonObject.toString(2));
		JSONObject pageUrisObject = jsonObject.getJSONObject(Processor.JSON_KEY_PAGE_URIS);
		assertEquals("/page-prefix-", pageUrisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("/page-prefix-3-page-suffix/brand-name/pb/", pageUrisObject.getString(Processor.JSON_KEY_NEXT));
		assertEquals("/page-prefix-1-page-suffix/brand-name/pb/", pageUrisObject.getString(Processor.JSON_KEY_PREVIOUS));
		assertEquals("-page-suffix/brand-name/pb/", pageUrisObject.getString(Processor.JSON_KEY_AFTER));
	}

	@Test void testBeforeAndAfterCode()  throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.paginationProcesser(
				"/page/default.properties",
				"/test/default/11/page-prefix-2-page-suffix/brand-name/wpb/",
				"",
				100L
		);
		System.out.println(jsonObject.toString(2));
		JSONObject pageUrisObject = jsonObject.getJSONObject(Processor.JSON_KEY_PAGE_URIS);
		assertEquals("/11/page-prefix-", pageUrisObject.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("/11/page-prefix-3-page-suffix/brand-name/wpb/", pageUrisObject.getString(Processor.JSON_KEY_NEXT));
		assertEquals("/11/page-prefix-1-page-suffix/brand-name/wpb/", pageUrisObject.getString(Processor.JSON_KEY_PREVIOUS));
		assertEquals("-page-suffix/brand-name/wpb/", pageUrisObject.getString(Processor.JSON_KEY_AFTER));
	}

//	@Test void testInvalidPageNumberOutOfBounds() throws PanlServerException, IOException {
//		JSONObject jsonObject = TestHelper.paginationProcesser(
//				"/page/default.properties",
//				"/test/default/page-prefix-100-page-suffix/p/",
//				"",
//				100L
//		);
//		assertBaseValidity(jsonObject);
//	}

//	@Test
	void textNumPerPage() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.paginationProcesser(
				"/page/default.properties",
				"/test/default/",
				"",
				100L
		);
		System.out.println(jsonObject.toString(2));
		JSONObject numPerPageUris = jsonObject.getJSONObject(Processor.JSON_KEY_NUM_PER_PAGE_URIS);
		assertEquals(1, jsonObject.getInt(Processor.JSON_KEY_PAGE_NUM));
		assertEquals(10, jsonObject.getInt(Processor.JSON_KEY_NUM_PAGES));
		assertEquals(10, jsonObject.getInt(Processor.JSON_KEY_NUM_PER_PAGE));

		assertEquals("/num-rows-prefix-", numPerPageUris.getString(Processor.JSON_KEY_BEFORE));
		assertEquals("-num-rows-suffix/n/", numPerPageUris.getString(Processor.JSON_KEY_AFTER));
	}
}
