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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
			switch(sortFieldObject.getString(Processor.JSON_KEY_FACET_NAME)) {
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
			switch(sortFieldObject.getString(Processor.JSON_KEY_FACET_NAME)) {
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
			switch(sortFieldObject.getString(Processor.JSON_KEY_NAME)) {
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
}

