package com.synapticloop.panl.server.handler.processor;

import com.synapticloop.panl.FacetCountBean;
import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.util.Constants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AvailableProcessorTest {

	public static final List<FacetCountBean> WEIGHT_NAME_FACETS = new ArrayList<>();
	public static final List<FacetCountBean> WEIGHT_FACETS = new ArrayList<>();
	static {
		WEIGHT_NAME_FACETS.add(new FacetCountBean("weight", new long[] { 1L, 45L, 128L }));
		WEIGHT_FACETS.add(new FacetCountBean("weight", new long[] { 1L, 45L, 128L }));

		WEIGHT_NAME_FACETS.add(new FacetCountBean("name", new long[] { 3L, 12L, 17L }));
	}

	@Test public void testAddFacet() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(WEIGHT_NAME_FACETS,
				"/default.properties",
				"/test/default/brand/b/",
				"",
				100L,
				false);

		System.out.println(jsonObject.toString(2));
		JSONArray availableFacets = jsonObject.getJSONArray(Constants.Json.Panl.FACETS);
		assertEquals(WEIGHT_NAME_FACETS.size(), availableFacets.length());
		for(int i = 0; i < availableFacets.length(); i++) {
			JSONObject facetObject = availableFacets.getJSONObject(i);
			JSONObject urisObject = facetObject.getJSONObject(Constants.Json.Panl.URIS);

			assertEquals("/", urisObject.getString(Constants.Json.Panl.BEFORE));
			assertEquals("/brand/" +
					facetObject.getString(Constants.Json.Panl.PANL_CODE) +
					"b/", urisObject.getString(Constants.Json.Panl.AFTER));

		}

		JSONArray rangeFacets = jsonObject.getJSONArray(Constants.Json.Panl.RANGE_FACETS);
		for(int i = 0; i < rangeFacets.length(); i++) {
			JSONObject facetObject = rangeFacets.getJSONObject(i);
			JSONObject urisObject = facetObject.getJSONObject(Constants.Json.Panl.URIS);

			assertEquals("/", urisObject.getString(Constants.Json.Panl.BEFORE));
			String lpseCode = facetObject.getString(Constants.Json.Panl.PANL_CODE);
			assertEquals("/brand/" +
					lpseCode +
					"+" +
					"b/", urisObject.getString(Constants.Json.Panl.AFTER));

		}
	}

	@Test public void testAddFacetTwoSorts() throws PanlServerException, IOException {
		JSONObject jsonObject = TestHelper.invokeAvailableProcessor(WEIGHT_NAME_FACETS,
				"/default.properties",
				"/test/default/sb-sm+/",
				"",
				100L,
				false);

		System.out.println(jsonObject.toString(2));
		JSONArray availableFacets = jsonObject.getJSONArray(Constants.Json.Panl.FACETS);
		assertEquals(WEIGHT_NAME_FACETS.size(), availableFacets.length());
		for(int i = 0; i < availableFacets.length(); i++) {
			JSONObject facetObject = availableFacets.getJSONObject(i);
			JSONObject urisObject = facetObject.getJSONObject(Constants.Json.Panl.URIS);

			assertEquals("/", urisObject.getString(Constants.Json.Panl.BEFORE));
			assertEquals("/" +
					facetObject.getString(Constants.Json.Panl.PANL_CODE) +
					"sb-sm+/", urisObject.getString(Constants.Json.Panl.AFTER));

		}

		JSONArray rageFacets = jsonObject.getJSONArray(Constants.Json.Panl.RANGE_FACETS);
		for(int i = 0; i < rageFacets.length(); i++) {
			JSONObject facetObject = rageFacets.getJSONObject(i);
			JSONObject urisObject = facetObject.getJSONObject(Constants.Json.Panl.URIS);

			assertEquals("/", urisObject.getString(Constants.Json.Panl.BEFORE));
			String lpseCode = facetObject.getString(Constants.Json.Panl.PANL_CODE);
			assertEquals("/" +
					lpseCode +
					"+" +
					"sb-sm+/", urisObject.getString(Constants.Json.Panl.AFTER));

		}
	}
}
