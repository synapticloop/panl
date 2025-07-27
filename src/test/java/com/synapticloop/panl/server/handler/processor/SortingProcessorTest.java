package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.util.Constants;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class SortingProcessorTest {

	@Test public void testAddFacet() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeSortingProcessor(
				"/default.properties",
				"/test/default/brand/b/",
				"",
				100L);

		System.out.println(jsonObject.toString(2));
		String resetUri = jsonObject.getString(Constants.Json.Panl.REMOVE_URI);
		assertEquals("/brand/b/", resetUri);

		for (Object object : jsonObject.getJSONArray(Constants.Json.Panl.FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			switch (sortFieldObject.getString(Constants.Json.Panl.FACET_NAME)) {
				case "weight":
					assertSortFieldLpseCode(sortFieldObject.getString(Constants.Json.Panl.SET_URI_DESC), "w", false);
					break;
				case "name":
					assertSortFieldLpseCode(sortFieldObject.getString(Constants.Json.Panl.SET_URI_DESC), "m", false);
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
		String resetUri = jsonObject.getString(Constants.Json.Panl.REMOVE_URI);
		assertEquals("/2/brand/nb/", resetUri);

		for (Object object : jsonObject.getJSONArray(Constants.Json.Panl.FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			switch (sortFieldObject.getString(Constants.Json.Panl.FACET_NAME)) {
				case "weight":
					assertEquals("/2/brand/sw-nb/", sortFieldObject.getString(Constants.Json.Panl.SET_URI_DESC));
					assertEquals("/2/brand/sw+nb/", sortFieldObject.getString(Constants.Json.Panl.SET_URI_ASC));
					break;
				case "name":
					assertEquals("/2/brand/sm-nb/", sortFieldObject.getString(Constants.Json.Panl.SET_URI_DESC));
					assertEquals("/2/brand/sm+nb/", sortFieldObject.getString(Constants.Json.Panl.SET_URI_ASC));
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
		String resetUri = jsonObject.getString(Constants.Json.Panl.REMOVE_URI);
		assertEquals("/3/brand/nb/", resetUri);

		for (Object object : jsonObject.getJSONArray(Constants.Json.Panl.FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			switch (sortFieldObject.getString(Constants.Json.Panl.NAME)) {
				case "weight":
					assertSortFieldLpseCode(sortFieldObject.getString(Constants.Json.Panl.SET_URI_DESC), "w", false);
					break;
				case "name":
					assertSortFieldLpseCode(sortFieldObject.getString(Constants.Json.Panl.SET_URI_DESC), "m", false);
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

		for (Object object : jsonObject.getJSONArray(Constants.Json.Panl.FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			assertNotNull(sortFieldObject.optString(Constants.Json.Panl.SET_URI_ASC, null));
			assertNotNull(sortFieldObject.optString(Constants.Json.Panl.SET_URI_ASC, null));

			switch (sortFieldObject.getString(Constants.Json.Panl.FACET_NAME)) {
				case "brand":
					// we are sorting on 'brand'
					assertNull(sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null));
					assertNull(sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null));
					break;
				case "disassemble":
					assertEquals("/sb-sD+/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_ASC));
					assertEquals("/sb-sD-/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_DESC));
					break;
				case "weight":
					assertEquals("/sb-sw+/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_ASC));
					assertEquals("/sb-sw-/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_DESC));
					break;
				case "name":
					assertEquals("/sb-sm+/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_ASC));
					assertEquals("/sb-sm-/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_DESC));
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

		for (Object object : jsonObject.getJSONArray(Constants.Json.Panl.FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			assertNotNull(sortFieldObject.optString(Constants.Json.Panl.SET_URI_ASC, null));
			assertNotNull(sortFieldObject.optString(Constants.Json.Panl.SET_URI_ASC, null));

			switch (sortFieldObject.getString(Constants.Json.Panl.FACET_NAME)) {
				case "brand":
					// we are sorting on 'name' ('m')
					assertEquals("/sm-sb+/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_ASC));
					assertEquals("/sm-sb-/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_DESC));
					break;
				case "disassemble":
					assertEquals("/sm-sD+/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_ASC));
					assertEquals("/sm-sD-/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_DESC));
					break;
				case "weight":
					assertEquals("/sm-sw+/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_ASC));
					assertEquals("/sm-sw-/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_DESC));
					break;
				case "name":
					assertNull(sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null));
					assertNull(sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null));
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

		for (Object object : jsonObject.getJSONArray(Constants.Json.Panl.FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			assertNotNull(sortFieldObject.optString(Constants.Json.Panl.SET_URI_ASC, null));
			assertNotNull(sortFieldObject.optString(Constants.Json.Panl.SET_URI_ASC, null));

			switch (sortFieldObject.getString(Constants.Json.Panl.FACET_NAME)) {
				case "brand":
					// we are sorting on 'name' ('m')
					assertEquals("/sw+sb+/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_ASC));
					assertEquals("/sw+sb-/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_DESC));
					break;
				case "disassemble":
					assertEquals("/sw+sD+/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_ASC));
					assertEquals("/sw+sD-/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_DESC));
					break;
				case "weight":
					assertNull(sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null));
					assertNull(sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null));
					break;
				case "name":
					assertEquals("/sw+sm+/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_ASC));
					assertEquals("/sw+sm-/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_DESC));
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

		for (Object object : jsonObject.getJSONArray(Constants.Json.Panl.FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			assertNotNull(sortFieldObject.optString(Constants.Json.Panl.SET_URI_ASC, null));
			assertNotNull(sortFieldObject.optString(Constants.Json.Panl.SET_URI_ASC, null));

			switch (sortFieldObject.getString(Constants.Json.Panl.FACET_NAME)) {
				case "brand":
					assertEquals("/sD+sb+/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_ASC));
					assertEquals("/sD+sb-/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_DESC));
					break;
				case "disassemble":
					assertNull(sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null));
					assertNull(sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null));
					break;
				case "weight":
					assertEquals("/sD+sw+/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_ASC));
					assertEquals("/sD+sw-/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_DESC));
					break;
				case "name":
					assertEquals("/sD+sm+/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_ASC));
					assertEquals("/sD+sm-/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_DESC));
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

		for (Object object : jsonObject.getJSONArray(Constants.Json.Panl.FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			assertNotNull(sortFieldObject.optString(Constants.Json.Panl.SET_URI_ASC, null));
			assertNotNull(sortFieldObject.optString(Constants.Json.Panl.SET_URI_ASC, null));

			switch (sortFieldObject.getString(Constants.Json.Panl.FACET_NAME)) {
				case "brand":
					// sorting on name, brand comes first
					assertEquals("/sm-sb+/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_ASC));
					assertEquals("/sm-sb-/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_DESC));
					assertNotNull(sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null));
					assertNotNull(sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null));
					break;
				case "weight":
					// sorting on name, weight comes after
					assertEquals("/sm-sw+/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_ASC));
					assertEquals("/sm-sw-/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_DESC));
					assertNotNull(sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null));
					assertNotNull(sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null));
					break;
				case "disassemble":
					// sorting on name, weight comes after
					assertEquals("/sm-sD+/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_ASC));
					assertEquals("/sm-sD-/", sortFieldObject.getString(Constants.Json.Panl.ADD_URI_DESC));
					assertNotNull(sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null));
					assertNotNull(sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null));
					break;
				case "name":
					assertNull(sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null));
					assertNull(sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null));
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

		for (Object object : jsonObject.getJSONArray(Constants.Json.Panl.FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			assertNotNull(sortFieldObject.optString(Constants.Json.Panl.SET_URI_ASC, null));
			assertNotNull(sortFieldObject.optString(Constants.Json.Panl.SET_URI_DESC, null));

			String addAsc = sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null);
			String addDesc = sortFieldObject.optString(Constants.Json.Panl.ADD_URI_DESC, null);

			switch (sortFieldObject.getString(Constants.Json.Panl.FACET_NAME)) {
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

		for (Object object : jsonObject.getJSONArray(Constants.Json.Panl.FIELDS)) {
			JSONObject sortFieldObject = (JSONObject) object;
			assertNotNull(sortFieldObject.optString(Constants.Json.Panl.SET_URI_ASC, null));
			assertNotNull(sortFieldObject.optString(Constants.Json.Panl.SET_URI_DESC, null));

			String addAsc = sortFieldObject.optString(Constants.Json.Panl.ADD_URI_ASC, null);
			String addDesc = sortFieldObject.optString(Constants.Json.Panl.ADD_URI_DESC, null);

			switch (sortFieldObject.getString(Constants.Json.Panl.FACET_NAME)) {
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

