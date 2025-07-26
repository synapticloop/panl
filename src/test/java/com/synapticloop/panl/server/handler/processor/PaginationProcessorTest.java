package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.util.Constants;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

public class PaginationProcessorTest {
	@Test void testPageAdditionURINeitherWithNext() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/neither.properties",
				"/test/default/",
				"",
				100L
		);
		JSONObject urisObject = jsonObject.getJSONObject(Constants.Json.Panl.PAGE_URIS);

		System.out.println(urisObject.toString(2));

		assertEquals("/", urisObject.getString(Constants.Json.Panl.BEFORE));
		assertEquals("/2/p/", urisObject.getString(Constants.Json.Panl.NEXT));
		assertEquals("/p/", urisObject.getString(Constants.Json.Panl.AFTER));
	}

	@Test void testPageAdditionURINeitherWithBoth() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/neither.properties",
				"/test/default/2/p/",
				"",
				100L
		);
		JSONObject urisObject = jsonObject.getJSONObject(Constants.Json.Panl.PAGE_URIS);

		System.out.println(urisObject.toString(2));

		assertEquals("/", urisObject.getString(Constants.Json.Panl.BEFORE));
		assertEquals("/3/p/", urisObject.getString(Constants.Json.Panl.NEXT));
		assertEquals("/1/p/", urisObject.getString(Constants.Json.Panl.PREVIOUS));
		assertEquals("/p/", urisObject.getString(Constants.Json.Panl.AFTER));
	}

	@Test void testPageAdditionURINeitherWithoutPrevious() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/neither.properties",
				"/test/default/10/p/",
				"",
				100L
		);
		JSONObject urisObject = jsonObject.getJSONObject(Constants.Json.Panl.PAGE_URIS);

		System.out.println(urisObject.toString(2));

		assertEquals("/", urisObject.getString(Constants.Json.Panl.BEFORE));
		assertNull(urisObject.optString(Constants.Json.Panl.NEXT, null));
		assertEquals("/9/p/", urisObject.getString(Constants.Json.Panl.PREVIOUS));
		assertEquals("/p/", urisObject.getString(Constants.Json.Panl.AFTER));
	}

	@Test void testPageAdditionURIPrefixSuffix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/prefix-suffix.properties",
				"/test/default/",
				"",
				100L
		);
		JSONObject urisObject = jsonObject.getJSONObject(Constants.Json.Panl.PAGE_URIS);

		System.out.println(urisObject.toString(2));

		assertEquals("/page-prefix-", urisObject.getString(Constants.Json.Panl.BEFORE));
		assertEquals("/page-prefix-2-page-suffix/p/", urisObject.getString(Constants.Json.Panl.NEXT));
		assertEquals("-page-suffix/p/", urisObject.getString(Constants.Json.Panl.AFTER));
	}

	@Test void testPageAdditionURIPrefix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/prefix.properties",
				"/test/default/",
				"",
				100L
		);
		JSONObject urisObject = jsonObject.getJSONObject(Constants.Json.Panl.PAGE_URIS);

		System.out.println(urisObject.toString(2));

		assertEquals("/page-prefix-", urisObject.getString(Constants.Json.Panl.BEFORE));
		assertEquals("/page-prefix-2/p/", urisObject.getString(Constants.Json.Panl.NEXT));
		assertEquals("/p/", urisObject.getString(Constants.Json.Panl.AFTER));
	}

	@Test void testPageAdditionURISuffix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/suffix.properties",
				"/test/default/",
				"",
				100L
		);
		JSONObject urisObject = jsonObject.getJSONObject(Constants.Json.Panl.PAGE_URIS);

		System.out.println(urisObject.toString(2));
		assertEquals(1, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PER_PAGE));

		assertEquals("/", urisObject.getString(Constants.Json.Panl.BEFORE));
		assertEquals("/2-page-suffix/p/", urisObject.getString(Constants.Json.Panl.NEXT));
		assertEquals("-page-suffix/p/", urisObject.getString(Constants.Json.Panl.AFTER));
	}


	@Test void testPageAdditionURIPrefixSuffixPagination() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/prefix-suffix.properties",
				"/test/default/page-prefix-2-page-suffix/p/",
				"",
				100L
		);

		JSONObject pageUrisObject = jsonObject.getJSONObject(Constants.Json.Panl.PAGE_URIS);
		System.out.println(jsonObject.toString(2));
		assertEquals(2, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PER_PAGE));

		assertEquals("/page-prefix-", pageUrisObject.getString(Constants.Json.Panl.BEFORE));
		assertEquals("/page-prefix-3-page-suffix/p/", pageUrisObject.getString(Constants.Json.Panl.NEXT));
		assertEquals("/page-prefix-1-page-suffix/p/", pageUrisObject.getString(Constants.Json.Panl.PREVIOUS));
		assertEquals("-page-suffix/p/", pageUrisObject.getString(Constants.Json.Panl.AFTER));
	}

	@Test void testPageAdditionURIPrefixSuffixPaginationNoNext() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/prefix-suffix.properties",
				"/test/default/page-prefix-10-page-suffix/p/",
				"",
				100L
		);
		JSONObject pageUrisObject = jsonObject.getJSONObject(Constants.Json.Panl.PAGE_URIS);
		System.out.println(jsonObject.toString(2));

		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PER_PAGE));

		assertEquals("/page-prefix-", pageUrisObject.getString(Constants.Json.Panl.BEFORE));
		assertNull(pageUrisObject.optString(Constants.Json.Panl.NEXT, null));
		assertEquals("/page-prefix-9-page-suffix/p/", pageUrisObject.getString(Constants.Json.Panl.PREVIOUS));
		assertEquals("-page-suffix/p/", pageUrisObject.getString(Constants.Json.Panl.AFTER));
	}


	private void assertBaseValidity(JSONObject jsonObject) {
		JSONObject pageUrisObject = jsonObject.getJSONObject(Constants.Json.Panl.PAGE_URIS);

		assertEquals(1, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PER_PAGE));

		assertEquals("/page-prefix-", pageUrisObject.getString(Constants.Json.Panl.BEFORE));
		assertNull(pageUrisObject.optString(Constants.Json.Panl.PREVIOUS, null));
		assertEquals("/page-prefix-2-page-suffix/p/", pageUrisObject.getString(Constants.Json.Panl.NEXT));
		assertEquals("-page-suffix/p/", pageUrisObject.getString(Constants.Json.Panl.AFTER));
	}

	@Test void testInvalidPageNumber() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/prefix-suffix.properties",
				"/test/default/page-prefix-NOT_AN_INTEGER-page-suffix/p/",
				"",
				100L
		);
		assertBaseValidity(jsonObject);
	}

	@Test void testInvalidPageNumberNegative() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/prefix-suffix.properties",
				"/test/default/page-prefix--4-page-suffix/p/",
				"",
				100L
		);
		assertBaseValidity(jsonObject);
	}

	@Test void testAfterCode()  throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/prefix-suffix.properties",
				"/test/default/11/page-prefix-2-page-suffix/wp/",
				"",
				100L
		);
		System.out.println(jsonObject.toString(2));
		JSONObject pageUrisObject = jsonObject.getJSONObject(Constants.Json.Panl.PAGE_URIS);
		assertEquals("/11/page-prefix-", pageUrisObject.getString(Constants.Json.Panl.BEFORE));
		assertEquals("/11/page-prefix-3-page-suffix/wp/", pageUrisObject.getString(Constants.Json.Panl.NEXT));
		assertEquals("/11/page-prefix-1-page-suffix/wp/", pageUrisObject.getString(Constants.Json.Panl.PREVIOUS));
		assertEquals("-page-suffix/wp/", pageUrisObject.getString(Constants.Json.Panl.AFTER));
	}

	@Test void testBeforeCode()  throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/prefix-suffix.properties",
				"/test/default/page-prefix-2-page-suffix/brand-name/pb/",
				"",
				100L
		);
		System.out.println(jsonObject.toString(2));
		JSONObject pageUrisObject = jsonObject.getJSONObject(Constants.Json.Panl.PAGE_URIS);
		assertEquals("/page-prefix-", pageUrisObject.getString(Constants.Json.Panl.BEFORE));
		assertEquals("/page-prefix-3-page-suffix/brand-name/pb/", pageUrisObject.getString(Constants.Json.Panl.NEXT));
		assertEquals("/page-prefix-1-page-suffix/brand-name/pb/", pageUrisObject.getString(Constants.Json.Panl.PREVIOUS));
		assertEquals("-page-suffix/brand-name/pb/", pageUrisObject.getString(Constants.Json.Panl.AFTER));
	}

	@Test void testBeforeAndAfterCode()  throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/prefix-suffix.properties",
				"/test/default/11/page-prefix-2-page-suffix/brand-name/wpb/",
				"",
				100L
		);
		System.out.println(jsonObject.toString(2));
		JSONObject pageUrisObject = jsonObject.getJSONObject(Constants.Json.Panl.PAGE_URIS);
		assertEquals("/11/page-prefix-", pageUrisObject.getString(Constants.Json.Panl.BEFORE));
		assertEquals("/11/page-prefix-3-page-suffix/brand-name/wpb/", pageUrisObject.getString(Constants.Json.Panl.NEXT));
		assertEquals("/11/page-prefix-1-page-suffix/brand-name/wpb/", pageUrisObject.getString(Constants.Json.Panl.PREVIOUS));
		assertEquals("-page-suffix/brand-name/wpb/", pageUrisObject.getString(Constants.Json.Panl.AFTER));
	}

	@Test void testNumPerPagePrefixSuffix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/prefix-suffix.properties",
				"/test/default/",
				"",
				100L
		);
		System.out.println(jsonObject.toString(2));
		JSONObject numPerPageUris = jsonObject.getJSONObject(Constants.Json.Panl.NUM_PER_PAGE_URIS);
		assertEquals(1, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PER_PAGE));

		assertEquals("/num-rows-prefix-", numPerPageUris.getString(Constants.Json.Panl.BEFORE));
		assertEquals("-num-rows-suffix/n/", numPerPageUris.getString(Constants.Json.Panl.AFTER));
	}

	@Test void testNumPerPageNeither() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/neither.properties",
				"/test/default/",
				"",
				100L
		);
		System.out.println(jsonObject.toString(2));
		JSONObject numPerPageUris = jsonObject.getJSONObject(Constants.Json.Panl.NUM_PER_PAGE_URIS);
		assertEquals(1, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PER_PAGE));

		assertEquals("/", numPerPageUris.getString(Constants.Json.Panl.BEFORE));
		assertEquals("/n/", numPerPageUris.getString(Constants.Json.Panl.AFTER));
	}

	@Test void testNumPerPagePrefix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/prefix.properties",
				"/test/default/",
				"",
				100L
		);
		System.out.println(jsonObject.toString(2));
		JSONObject numPerPageUris = jsonObject.getJSONObject(Constants.Json.Panl.NUM_PER_PAGE_URIS);
		assertEquals(1, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PER_PAGE));

		assertEquals("/num-rows-prefix-", numPerPageUris.getString(Constants.Json.Panl.BEFORE));
		assertEquals("/n/", numPerPageUris.getString(Constants.Json.Panl.AFTER));
	}

	@Test void testNumPerPageSuffix() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/suffix.properties",
				"/test/default/",
				"",
				100L
		);
		System.out.println(jsonObject.toString(2));
		JSONObject numPerPageUris = jsonObject.getJSONObject(Constants.Json.Panl.NUM_PER_PAGE_URIS);
		assertEquals(1, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PER_PAGE));

		assertEquals("/", numPerPageUris.getString(Constants.Json.Panl.BEFORE));
		assertEquals("-num-rows-suffix/n/", numPerPageUris.getString(Constants.Json.Panl.AFTER));
	}

	@Test void testPageNumbers() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/default.properties",
				"/test/default/",
				"",
				90L
		);

		assertEquals(1, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(9, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));

		jsonObject = TestHelper.invokePaginationProcessor(
				"/default.properties",
				"/test/default/",
				"",
				88L
		);

		assertEquals(1, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(9, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));
		jsonObject = TestHelper.invokePaginationProcessor(
				"/default.properties",
				"/test/default/",
				"",
				2L
		);

		assertEquals(1, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(1, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));
	}

	@Test void testNumPerPageNumbers() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/prefix-suffix.properties",
				"/test/default/num-rows-prefix-3-num-rows-suffix/n/",
				"",
				90L
		);

		assertEquals(1, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(30, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));

		jsonObject = TestHelper.invokePaginationProcessor(
				"/page/prefix-suffix.properties",
				"/test/default/num-rows-prefix-3-num-rows-suffix/n/",
				"",
				91L
		);

		assertEquals(1, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(31, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));

		jsonObject = TestHelper.invokePaginationProcessor(
				"/page/prefix-suffix.properties",
				"/test/default/num-rows-prefix-5-num-rows-suffix/n/",
				"",
				12L
		);

		assertEquals(1, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(3, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));
		jsonObject = TestHelper.invokePaginationProcessor(
				"/page/prefix-suffix.properties",
				"/test/default/",
				"",
				2L
		);

		assertEquals(1, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(1, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));
	}

	@Test public void testResetOfPageNumber() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/neither.properties",
				"/test/default/2/p/",
				"",
				12L
		);

		// On any page, and the number of results per page is not the first page
		// this should reset the page number
		System.out.println(jsonObject.toString(2));
		JSONObject numPerPageUris = jsonObject.getJSONObject(Constants.Json.Panl.NUM_PER_PAGE_URIS);
		assertEquals(2, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(2, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PER_PAGE));

		assertEquals("/", numPerPageUris.getString(Constants.Json.Panl.BEFORE));
		assertEquals("/n/", numPerPageUris.getString(Constants.Json.Panl.AFTER));

		JSONObject pageUrisObject = jsonObject.getJSONObject(Constants.Json.Panl.PAGE_URIS);

		assertEquals("/", pageUrisObject.getString(Constants.Json.Panl.BEFORE));
		assertNull(pageUrisObject.optString(Constants.Json.Panl.NEXT, null));
		assertEquals("/1/p/", pageUrisObject.getString(Constants.Json.Panl.PREVIOUS));
		assertEquals("/p/", pageUrisObject.getString(Constants.Json.Panl.AFTER));
	}

	@Test public void testOutOfBoundsPageNumber() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/neither.properties",
				"/test/default/8/p/",
				"",
				12L
		);

		// On any page, and the number of results per page is not the first page
		// this should reset the page number
		System.out.println(jsonObject.toString(2));
		JSONObject numPerPageUris = jsonObject.getJSONObject(Constants.Json.Panl.NUM_PER_PAGE_URIS);
		assertEquals(8, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(2, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PER_PAGE));

		assertEquals("/", numPerPageUris.getString(Constants.Json.Panl.BEFORE));
		assertEquals("/n/", numPerPageUris.getString(Constants.Json.Panl.AFTER));

		JSONObject pageUrisObject = jsonObject.getJSONObject(Constants.Json.Panl.PAGE_URIS);

		assertEquals("/", pageUrisObject.getString(Constants.Json.Panl.BEFORE));
		assertNull(pageUrisObject.optString(Constants.Json.Panl.NEXT, null));
		assertEquals("/7/p/", pageUrisObject.getString(Constants.Json.Panl.PREVIOUS));
		assertEquals("/p/", pageUrisObject.getString(Constants.Json.Panl.AFTER));
	}

	@Test public void testInvalidNumRows() throws PanlServerException, IOException {
		testInvalidRowNumbers("0/");
		testInvalidRowNumbers("-1/");
		testInvalidRowNumbers("akjdsfads/");
		testInvalidRowNumbers("1.0/");
		testInvalidRowNumbers(" /");
		testInvalidRowNumbers("/");
		testInvalidRowNumbers("");
		testInvalidRowNumbers("////////////////");
	}

	private static void testInvalidRowNumbers(String numRows) throws IOException, PanlServerException {
		JSONObject jsonObject = TestHelper.invokePaginationProcessor(
				"/page/neither.properties",
				"/test/default/" + numRows + "n/",
				"",
				12L
		);

		// On any page, and the number of results per page is not the first page
		// this should reset the page number
		System.out.println(jsonObject.toString(2));
		JSONObject numPerPageUris = jsonObject.getJSONObject(Constants.Json.Panl.NUM_PER_PAGE_URIS);
		assertEquals(1, jsonObject.getInt(Constants.Json.Panl.PAGE_NUM));
		assertEquals(2, jsonObject.getInt(Constants.Json.Panl.NUM_PAGES));
		assertEquals(10, jsonObject.getInt(Constants.Json.Panl.NUM_PER_PAGE));

		assertEquals("/", numPerPageUris.getString(Constants.Json.Panl.BEFORE));
		assertEquals("/n/", numPerPageUris.getString(Constants.Json.Panl.AFTER));

		JSONObject pageUrisObject = jsonObject.getJSONObject(Constants.Json.Panl.PAGE_URIS);

		assertEquals("/", pageUrisObject.getString(Constants.Json.Panl.BEFORE));
		assertNull(pageUrisObject.optString(Constants.Json.Panl.PREVIOUS, null));
		assertEquals("/2/p/", pageUrisObject.getString(Constants.Json.Panl.NEXT));
		assertEquals("/p/", pageUrisObject.getString(Constants.Json.Panl.AFTER));
	}

}
