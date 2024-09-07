package com.synapticloop.panl.server.handler.webapp.explainer;

/*
 * Copyright (c) 2008-2024 synapticloop.
 *
 * https://github.com/synapticloop/panl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

import com.synapticloop.panl.server.handler.CollectionRequestHandler;
import com.synapticloop.panl.server.handler.webapp.util.ResourceHelper;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.*;
import com.synapticloop.panl.server.handler.tokeniser.token.param.QueryLpseToken;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;


public class PanlResultsExplainerExplainHandler implements HttpRequestHandler {
	private final List<CollectionProperties> collectionPropertiesList;

	public PanlResultsExplainerExplainHandler(List<CollectionProperties> collectionPropertiesList, List<CollectionRequestHandler> collectionRequestHandlers) {
		this.collectionPropertiesList = collectionPropertiesList;
	}

	@Override public void handle(HttpRequest request, HttpResponse response, HttpContext context) {
		String uri = request.getRequestLine().getUri();
		int startParam = uri.indexOf('?');
		String query = "";
		if (startParam != -1) {
			query = uri.substring(startParam + 1);
			uri = uri.substring(0, startParam);
		}

		String[] splits = uri.split("/");
		if (splits.length < 5) {
			// #TODO return a 404 or something
			return;
		}

		// the first part is the panl collection URI that we need


		CollectionProperties collectionProperties = null;
		String panlCollectionUri = splits[3];
		for(CollectionProperties collectionPropertiesTemp : collectionPropertiesList) {
			if(collectionPropertiesTemp.getPanlCollectionUri().equals(panlCollectionUri)) {
				collectionProperties = collectionPropertiesTemp;
				break;
			}
		}

		if(null == collectionProperties) {
			return;
		}

		List<LpseToken> lpseTokens = parseLpse(collectionProperties, uri, query);
		JSONArray jsonArray = new JSONArray();
		for (LpseToken lpseToken : lpseTokens) {
			jsonArray.put(lpseToken.explain());
		}


		JSONObject jsonObject = new JSONObject();
		jsonObject.put("explanation", jsonArray);

		jsonObject.put("parameters", getLpseParameters(collectionProperties));

		jsonObject.put("configuration", getLpseConfiguration(collectionProperties));

		response.setStatusCode(HttpStatus.SC_OK);
		response.setEntity(
				new StringEntity(jsonObject.toString(),
						ResourceHelper.CONTENT_TYPE_JSON));

	}
	private JSONArray getLpseParameters(CollectionProperties collectionProperties) {
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(createParameterDetails(
				CollectionProperties.PROPERTY_KEY_PANL_LPSE_LENGTH,
				collectionProperties.getLpseLength(),
				"The length of the LPSE codes that are used for fields.  The LPSE codes for Panl parameters is __ALWAYS__ one (1)."));

		jsonArray.put(createParameterDetails(
				CollectionProperties.PROPERTY_KEY_PANL_PARAM_QUERY,
				collectionProperties.getPanlParamQuery(),
				"[ LPSE PARAMETER ] The LPSE code for the user search query."));

		List<String> temp = new ArrayList<>();
		temp.add("[ LPSE PARAMETER ] The LPSE code for the sorting orders. The sort fields available (in order) explained: ");
		temp.addAll(getSortFieldDescriptions(collectionProperties));
		jsonArray.put(createParameterDetails(
				CollectionProperties.PROPERTY_KEY_PANL_PARAM_SORT,
				collectionProperties.getPanlParamSort(),
				temp.toArray(new String[0])));

		jsonArray.put(createParameterDetails(
				CollectionProperties.PROPERTY_KEY_PANL_PARAM_NUMROWS,
				collectionProperties.getPanlParamNumRows(),
				"[ LPSE PARAMETER ] The LPSE code for the default number of results to return per page."));

		jsonArray.put(createParameterDetails(
				CollectionProperties.PROPERTY_KEY_PANL_PARAM_QUERY_OPERAND,
				collectionProperties.getPanlParamQueryOperand(),
				"[ LPSE PARAMETER ] The LPSE code for the query operand."));

		jsonArray.put(createParameterDetails(
				CollectionProperties.PROPERTY_KEY_PANL_PARAM_PASSTHROUGH,
				collectionProperties.getPanlParamPassThrough(),
				"[ LPSE PARAMETER ] The LPSE code for a pass through token."));

		jsonArray.put(createParameterDetails(
				CollectionProperties.PROPERTY_KEY_PANL_INCLUDE_SINGLE_FACETS,
				collectionProperties.getPanlIncludeSingleFacets(),
				"Whether to include result facets that only have a single result (default \"false\")."));

		jsonArray.put(createParameterDetails(
				CollectionProperties.PROPERTY_KEY_PANL_INCLUDE_SAME_NUMBER_FACETS,
				collectionProperties.getPanlIncludeSameNumberFacets(),
				"Whether to include result facets that have the same number of results as the number of documents that have been returned (default \"false\")."));

		jsonArray.put(createParameterDetails(
				CollectionProperties.PROPERTY_KEY_PANL_FORM_QUERY_RESPONDTO,
				collectionProperties.getFormQueryRespondTo(),
				"The URL parameter that Panl will look for to build the text/phrase for the query search (default \"q\"), e.g. http://example.com/?q=search+text+here."));

		jsonArray.put(createParameterDetails(
				CollectionProperties.PROPERTY_KEY_SOLR_FACET_MIN_COUNT,
				collectionProperties.getFacetMinCount(),
				"The minimum number for facet value counts to include (default \"1\") i.e. if the facet returns a value that has less than this property, it will not be returned."));

		jsonArray.put(createParameterDetails(
				CollectionProperties.PROPERTY_KEY_SOLR_NUMROWS_DEFAULT,
				collectionProperties.getNumResultsPerPage(),
				"The default number of results to include if the number of results per page LPSE code is not set in the URI path."));

		jsonArray.put(createParameterDetails(
				CollectionProperties.PROPERTY_KEY_SOLR_FACET_LIMIT,
				collectionProperties.getSolrFacetLimit(),
				"The limit to the number of facet values that will be returned (default \"100\")."));

		temp = new ArrayList<>();
		temp.add("The order for the LPSE codes. The LPSE order explained:");
		temp.addAll(getLpseOrderDescriptions(collectionProperties));
		jsonArray.put(createParameterDetails(
				CollectionProperties.PROPERTY_KEY_PANL_LPSE_ORDER,
				collectionProperties.getPanlLpseOrder(),
				temp.toArray(new String[0])));

		return(jsonArray);
	}

	private List<String> getLpseOrderDescriptions(CollectionProperties collectionProperties) {
		List<String> strings = new ArrayList<>();
		int i = 1;
		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			strings.add(getDescriptionOfLpseCode(collectionProperties, i, lpseField.getLpseCode()));
			i++;
		}

		return(strings);

	}
	private List<String> getSortFieldDescriptions(CollectionProperties collectionProperties) {
		List<String> strings = new ArrayList<>();

		int i = 1;
		for (String sortFieldLpseCode : collectionProperties.getSortFieldLpseCodes()) {
			strings.add(getDescriptionOfLpseCode(collectionProperties, i, sortFieldLpseCode));
			i++;
		}

		return(strings);
	}

	private String getDescriptionOfLpseCode(CollectionProperties collectionProperties, int order, String lpseCode) {

		StringBuilder sb = new StringBuilder();
		String solrFieldNameFromLpseCode = collectionProperties.getSolrFieldNameFromLpseCode(lpseCode);
		String panlNameFromPanlCode = collectionProperties.getPanlNameFromPanlCode(lpseCode);
		sb.append(" ")
				.append(order)
				.append(". ")
				.append("Panl LPSE code [ ")
				.append(lpseCode)
				.append(" ] ");

		if(null != solrFieldNameFromLpseCode) {
			sb.append(" Solr field '")
					.append(solrFieldNameFromLpseCode)
					.append("', Panl name '")
					.append(panlNameFromPanlCode)
					.append("'.  ");

			if(collectionProperties.getLpseWhenCode(lpseCode) != null) {
				sb.append("Will only be available if any of the following LPSE codes are selected: ");
				Iterator<String> iterator = collectionProperties.getLpseWhenCode(lpseCode).iterator();
				while(iterator.hasNext()) {
					String nextLpseCode = iterator.next();
					sb.append("'")
							.append(nextLpseCode)
							.append("' - solr field '")
							.append(collectionProperties.getSolrFieldNameFromLpseCode(nextLpseCode))
							.append("'");
					if(iterator.hasNext()) {
						sb.append(", ");
					} else {
						sb.append(".  ");
					}
				}
			}

		} else {
			sb.append(" Not mapped to a Solr field.");
		}

		return(sb.toString());
	}

	private JSONObject createParameterDetails(String propertyKey, Object value, String... description) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("property", propertyKey);
		jsonObject.put("value", value);
		jsonObject.put("description", description);

		return (jsonObject);
	}

	private JSONArray getLpseConfiguration(CollectionProperties collectionProperties) {
		JSONArray jsonArray = new JSONArray();

		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			jsonArray.put(lpseField.explain());
		}
		return(jsonArray);
	}

	private List<LpseToken> parseLpse(CollectionProperties collectionProperties, String uri, String query) {
		List<LpseToken> lpseTokens = new ArrayList<>();

		String[] searchQuery = uri.split("/");

		boolean hasQuery = false;

		if (searchQuery.length > 5) {
			String lpseEncoding = searchQuery[searchQuery.length - 1];

			LpseTokeniser lpseTokeniser = new LpseTokeniser(lpseEncoding, CollectionRequestHandler.CODES_AND_METADATA, true);

			StringTokenizer valueTokeniser = new StringTokenizer(uri, "/", false);
			// we need to skip the first two - as they will be the collection and the
			// field set
			valueTokeniser.nextToken();
			valueTokeniser.nextToken();
			valueTokeniser.nextToken();
			valueTokeniser.nextToken();

			while (lpseTokeniser.hasMoreTokens()) {
				String token = lpseTokeniser.nextToken();
				LpseToken lpseToken = LpseToken.getLpseToken(collectionProperties, token, query, valueTokeniser, lpseTokeniser);
				lpseTokens.add(lpseToken);
			}
		}

		if (!hasQuery && !query.isBlank()) {
			lpseTokens.add(new QueryLpseToken(collectionProperties, query, collectionProperties.getPanlParamQuery()));
		}

		return (lpseTokens);
	}
}
