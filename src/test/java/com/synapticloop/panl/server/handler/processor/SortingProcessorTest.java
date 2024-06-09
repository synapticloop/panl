package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.FacetCountBean;
import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SortingProcessorTest {

	@Test public void testAddFacet() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeSortingProcessor(
				"/default.properties",
				"/test/default/brand/b/",
				"",
				100L);

		System.out.println(jsonObject.toString(2));
		JSONObject relevanceObject = jsonObject.getJSONObject(Processor.JSON_KEY_RELEVANCE);
		assertEquals("/brand/s-b/", relevanceObject.getString(Processor.JSON_KEY_REPLACE_DESC));

		for (Object object : jsonObject.getJSONArray(Processor.JSON_KEY_FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			switch (sortFieldObject.getString(Processor.JSON_KEY_FACET_NAME)) {
				case "weight":
					assertSortFieldLpseCode(sortFieldObject.getString(Processor.JSON_KEY_REPLACE_DESC), "w", false);
					break;
				case "name":
					assertSortFieldLpseCode(sortFieldObject.getString(Processor.JSON_KEY_REPLACE_DESC), "m", false);
					break;
				default:
					// nothing to do....
			}
		}
	}

	private void assertSortFieldLpseCode(String uriPath, String lpseCode, boolean asc) {
		assertEquals("/brand/s" + lpseCode + (asc ? "+" : "-") + "b/", uriPath);
	}

	@Test public void testAdditionURIResetPageNum() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeSortingProcessor(
				"/default.properties",
				"/test/default/2/brand/nb/",
				"",
				100L);

		System.out.println(jsonObject.toString(2));
		JSONObject relevanceObject = jsonObject.getJSONObject(Processor.JSON_KEY_RELEVANCE);
		assertEquals("/2/brand/s-nb/", relevanceObject.getString(Processor.JSON_KEY_REPLACE_DESC));

		for (Object object : jsonObject.getJSONArray(Processor.JSON_KEY_FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			switch (sortFieldObject.getString(Processor.JSON_KEY_FACET_NAME)) {
				case "weight":
					assertEquals("/2/brand/sw-nb/", sortFieldObject.getString(Processor.JSON_KEY_REPLACE_DESC));
					assertEquals("/2/brand/sw+nb/", sortFieldObject.getString(Processor.JSON_KEY_REPLACE_ASC));
					break;
				case "name":
					assertEquals("/2/brand/sm-nb/", sortFieldObject.getString(Processor.JSON_KEY_REPLACE_DESC));
					assertEquals("/2/brand/sm+nb/", sortFieldObject.getString(Processor.JSON_KEY_REPLACE_ASC));
					break;
				default:
					// nothing to do....
			}
		}
	}

	@Test public void testAdditionURIResetPageNumWithNumPerPage() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeSortingProcessor(
				"/default.properties",
				"/test/default/2/3/brand/pnb/",
				"",
				100L);

		System.out.println(jsonObject.toString(2));
		JSONObject relevanceObject = jsonObject.getJSONObject(Processor.JSON_KEY_RELEVANCE);
		assertEquals("/3/brand/s-nb/", relevanceObject.getString(Processor.JSON_KEY_REPLACE_DESC));

		for (Object object : jsonObject.getJSONArray(Processor.JSON_KEY_FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			switch (sortFieldObject.getString(Processor.JSON_KEY_NAME)) {
				case "weight":
					assertSortFieldLpseCode(sortFieldObject.getString(Processor.JSON_KEY_REPLACE_DESC), "w", false);
					break;
				case "name":
					assertSortFieldLpseCode(sortFieldObject.getString(Processor.JSON_KEY_REPLACE_DESC), "m", false);
					break;
				default:
					// nothing to do....
			}
		}
	}

	@Test public void testMultiSortParsingFirstSortField() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeSortingProcessor(
				"/default.properties",
				"/test/default/sb-/",
				"",
				100L);
		System.out.println(jsonObject.toString(2));

		// in the above URI - we are sorting by brand when we look at the returned
		// JSON object there shouldn't be the following keys
		//
		//     - add_asc
		//     - add_dec
		//
		// for the 'brand' field as it already exists

		for (Object object : jsonObject.getJSONArray(Processor.JSON_KEY_FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_REPLACE_ASC, null));
			assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_REPLACE_ASC, null));

			switch (sortFieldObject.getString(Processor.JSON_KEY_FACET_NAME)) {
				case "brand":
					// we are sorting on 'brand'
					assertNull(sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null));
					assertNull(sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null));
					break;
				case "disassemble":
					assertEquals("/sb-sD+/", sortFieldObject.getString(Processor.JSON_KEY_ADD_ASC));
					assertEquals("/sb-sD-/", sortFieldObject.getString(Processor.JSON_KEY_ADD_DESC));
					break;
				case "weight":
					assertEquals("/sb-sw+/", sortFieldObject.getString(Processor.JSON_KEY_ADD_ASC));
					assertEquals("/sb-sw-/", sortFieldObject.getString(Processor.JSON_KEY_ADD_DESC));
					break;
				case "name":
					assertEquals("/sb-sm+/", sortFieldObject.getString(Processor.JSON_KEY_ADD_ASC));
					assertEquals("/sb-sm-/", sortFieldObject.getString(Processor.JSON_KEY_ADD_DESC));
					break;
				default:
					// nothing to do....
			}
		}
	}

	@Test public void testMultiSortParsingSecondSortField() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeSortingProcessor(
				"/default.properties",
				"/test/default/sm-/",
				"",
				100L);
		System.out.println(jsonObject.toString(2));

		for (Object object : jsonObject.getJSONArray(Processor.JSON_KEY_FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_REPLACE_ASC, null));
			assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_REPLACE_ASC, null));

			switch (sortFieldObject.getString(Processor.JSON_KEY_FACET_NAME)) {
				case "brand":
					// we are sorting on 'name' ('m')
					assertEquals("/sm-sb+/", sortFieldObject.getString(Processor.JSON_KEY_ADD_ASC));
					assertEquals("/sm-sb-/", sortFieldObject.getString(Processor.JSON_KEY_ADD_DESC));
					break;
				case "disassemble":
					assertEquals("/sm-sD+/", sortFieldObject.getString(Processor.JSON_KEY_ADD_ASC));
					assertEquals("/sm-sD-/", sortFieldObject.getString(Processor.JSON_KEY_ADD_DESC));
					break;
				case "weight":
					assertEquals("/sm-sw+/", sortFieldObject.getString(Processor.JSON_KEY_ADD_ASC));
					assertEquals("/sm-sw-/", sortFieldObject.getString(Processor.JSON_KEY_ADD_DESC));
					break;
				case "name":
					assertNull(sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null));
					assertNull(sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null));
					break;
				default:
					// nothing to do....

			}
		}
	}

	@Test public void testMultiSortParsingThirdSortField() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeSortingProcessor(
				"/default.properties",
				"/test/default/sw+/",
				"",
				100L);
		System.out.println(jsonObject.toString(2));

		for (Object object : jsonObject.getJSONArray(Processor.JSON_KEY_FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_REPLACE_ASC, null));
			assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_REPLACE_ASC, null));

			switch (sortFieldObject.getString(Processor.JSON_KEY_FACET_NAME)) {
				case "brand":
					// we are sorting on 'name' ('m')
					assertEquals("/sw+sb+/", sortFieldObject.getString(Processor.JSON_KEY_ADD_ASC));
					assertEquals("/sw+sb-/", sortFieldObject.getString(Processor.JSON_KEY_ADD_DESC));
					break;
				case "disassemble":
					assertEquals("/sw+sD+/", sortFieldObject.getString(Processor.JSON_KEY_ADD_ASC));
					assertEquals("/sw+sD-/", sortFieldObject.getString(Processor.JSON_KEY_ADD_DESC));
					break;
				case "weight":
					assertNull(sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null));
					assertNull(sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null));
					break;
				case "name":
					assertEquals("/sw+sm+/", sortFieldObject.getString(Processor.JSON_KEY_ADD_ASC));
					assertEquals("/sw+sm-/", sortFieldObject.getString(Processor.JSON_KEY_ADD_DESC));
					break;
				default:
					// nothing to do....

			}
		}
	}

	@Test public void testMultiSortParsingForthSortField() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeSortingProcessor(
				"/default.properties",
				"/test/default/sD+/",
				"",
				100L);
		System.out.println(jsonObject.toString(2));

		for (Object object : jsonObject.getJSONArray(Processor.JSON_KEY_FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_REPLACE_ASC, null));
			assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_REPLACE_ASC, null));

			switch (sortFieldObject.getString(Processor.JSON_KEY_FACET_NAME)) {
				case "brand":
					assertEquals("/sD+sb+/", sortFieldObject.getString(Processor.JSON_KEY_ADD_ASC));
					assertEquals("/sD+sb-/", sortFieldObject.getString(Processor.JSON_KEY_ADD_DESC));
					break;
				case "disassemble":
					assertNull(sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null));
					assertNull(sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null));
					break;
				case "weight":
					assertEquals("/sD+sw+/", sortFieldObject.getString(Processor.JSON_KEY_ADD_ASC));
					assertEquals("/sD+sw-/", sortFieldObject.getString(Processor.JSON_KEY_ADD_DESC));
					break;
				case "name":
					assertEquals("/sD+sm+/", sortFieldObject.getString(Processor.JSON_KEY_ADD_ASC));
					assertEquals("/sD+sm-/", sortFieldObject.getString(Processor.JSON_KEY_ADD_DESC));
					break;
				default:
					// nothing to do....

			}
		}
	}
	@Test public void testMultiSortParsingOneSortNotFirst() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeSortingProcessor(
				"/default.properties",
				"/test/default/sm-/",
				"",
				100L);
		System.out.println(jsonObject.toString(2));

		// in the above URI - we are sorting by brand when we look at the returned
		// JSON object there shouldn't be the following keys
		//
		//     - add_asc
		//     - add_dec
		//
		// for the 'brand' field as it already exists

		for (Object object : jsonObject.getJSONArray(Processor.JSON_KEY_FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_REPLACE_ASC, null));
			assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_REPLACE_ASC, null));

			switch (sortFieldObject.getString(Processor.JSON_KEY_FACET_NAME)) {
				case "brand":
					// sorting on name, brand comes first
					assertEquals("/sm-sb+/", sortFieldObject.getString(Processor.JSON_KEY_ADD_ASC));
					assertEquals("/sm-sb-/", sortFieldObject.getString(Processor.JSON_KEY_ADD_DESC));
					assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null));
					assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null));
					break;
				case "weight":
					// sorting on name, weight comes after
					assertEquals("/sm-sw+/", sortFieldObject.getString(Processor.JSON_KEY_ADD_ASC));
					assertEquals("/sm-sw-/", sortFieldObject.getString(Processor.JSON_KEY_ADD_DESC));
					assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null));
					assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null));
					break;
				case "disassemble":
					// sorting on name, weight comes after
					assertEquals("/sm-sD+/", sortFieldObject.getString(Processor.JSON_KEY_ADD_ASC));
					assertEquals("/sm-sD-/", sortFieldObject.getString(Processor.JSON_KEY_ADD_DESC));
					assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null));
					assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null));
					break;
				case "name":
					assertNull(sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null));
					assertNull(sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null));
					break;
				default:
					// nothing to do....

			}
		}
	}

	@Test public void testMultiSortParsingTwoSort() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeSortingProcessor(
				"/default.properties",
				"/test/default/sb-sm+/",
				"",
				100L);
		System.out.println(jsonObject.toString(2));

		// in the above URI - we are sorting by brand and the name, when we look at
		// the returned JSON object there shouldn't be the following keys
		//
		//     - add_asc
		//     - add_dec
		//
		// for the 'brand' and 'name' fields as they already exists

		for (Object object : jsonObject.getJSONArray(Processor.JSON_KEY_FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_REPLACE_ASC, null));
			assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_REPLACE_DESC, null));

			String addAsc = sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null);
			String addDesc = sortFieldObject.optString(Processor.JSON_KEY_ADD_DESC, null);

			switch (sortFieldObject.getString(Processor.JSON_KEY_FACET_NAME)) {
				case "brand":
				case "name":
					// we are sorting on 'brand'
					assertNull(addAsc);
					assertNull(addDesc);
					break;
				case "weight":
					assertEquals("/sb-sm+sw+/", addAsc);
					assertEquals("/sb-sm+sw-/", addDesc);
					break;
				case "disassemble":
					assertNotNull(addAsc);
					assertNotNull(addDesc);
					break;
				default:
					// nothing to do....

			}
		}
	}

	@Test public void testMultiSortParsingTwoSortOutOfOrderWithFacets() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeSortingProcessor(
				"/default.properties",
				"/test/default/11/name/query-string/brand/wmqsm+sb-b/",
				"",
				100L);
		System.out.println(jsonObject.toString(2));

		// in the above URI - we are sorting by brand and the name, when we look at
		// the returned JSON object there shouldn't be the following keys
		//
		//     - add_asc
		//     - add_dec
		//
		// for the 'brand' and 'name' fields as they already exists

		for (Object object : jsonObject.getJSONArray(Processor.JSON_KEY_FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_REPLACE_ASC, null));
			assertNotNull(sortFieldObject.optString(Processor.JSON_KEY_REPLACE_DESC, null));

			String addAsc = sortFieldObject.optString(Processor.JSON_KEY_ADD_ASC, null);
			String addDesc = sortFieldObject.optString(Processor.JSON_KEY_ADD_DESC, null);

			switch (sortFieldObject.getString(Processor.JSON_KEY_FACET_NAME)) {
				case "brand":
				case "name":
					// we are sorting on 'brand'
					assertNull(addAsc);
					assertNull(addDesc);
					break;
				case "weight":
					assertEquals("/11/name/query-string/brand/wmqsm+sb-sw+b/", addAsc);
					assertEquals("/11/name/query-string/brand/wmqsm+sb-sw-b/", addDesc);
					break;
				case "disassemble":
					assertEquals("/11/name/query-string/brand/wmqsm+sb-sD+b/", addAsc);
					assertEquals("/11/name/query-string/brand/wmqsm+sb-sD-b/", addDesc);
					break;
				default:
					// nothing to do....

			}
		}
	}
}

