package com.synapticloop.panl;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.CollectionRequestHandler;
import com.synapticloop.panl.server.handler.processor.*;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.json.JSONObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.*;

public class TestHelper {
	private static PanlProperties panlProperties;

	public static Properties getTestProperties(String propertiesName) throws IOException {
		Properties properties = new Properties();
		properties.load(TestHelper.class.getResourceAsStream(propertiesName));
		return (properties);
	}

	public static PanlProperties getTestPanlProperties() throws IOException {
		if (null == panlProperties) {
			panlProperties = new PanlProperties(getTestProperties("/test.panl.properties"));
		}
		return (panlProperties);
	}


	public static Map<String, List<LpseToken>> getPanlTokenMap(List<LpseToken> lpseTokens) {
		Map<String, List<LpseToken>> panlTokenMap = new HashMap<>();

		for (LpseToken lpseToken : lpseTokens) {
			String lpseCode = lpseToken.getLpseCode();

			List<LpseToken> lpseTokenList = panlTokenMap.get(lpseCode);
			if (null == lpseTokenList) {
				lpseTokenList = new ArrayList<>();
			}

			// only adding valid tokens
			if (lpseToken.getIsValid()) {
				lpseTokenList.add(lpseToken);
				panlTokenMap.put(lpseCode, lpseTokenList);
			}
		}

		return (panlTokenMap);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static QueryResponse getMockedQueryResponse(long numFound, boolean numFoundExact) {
		QueryResponse mockedQueryResponse = mock(QueryResponse.class);
		NamedList mockedNamedListResponse = mock(NamedList.class);
		SolrDocumentList mockedSolrDocumentList = mock(SolrDocumentList.class);

		when(mockedQueryResponse.getResponse()).thenReturn(mockedNamedListResponse);
		when(mockedNamedListResponse.get("response")).thenReturn(mockedSolrDocumentList);
		when(mockedSolrDocumentList.getNumFound()).thenReturn(numFound);
		when(mockedSolrDocumentList.getNumFoundExact()).thenReturn(numFoundExact);

		return(mockedQueryResponse);
	}

	public static JSONObject invokePaginationProcesser(
			String propertiesFileLocation,
			String URIPath,
			String query,
			long numResults) throws IOException, PanlServerException {
		PanlProperties panlProperties = TestHelper.getTestPanlProperties();
		CollectionProperties collectionProperties = new CollectionProperties(
				"test",
				TestHelper.getTestProperties(propertiesFileLocation));
		// now to parse the query

		CollectionRequestHandler collectionRequestHandler = new CollectionRequestHandler(
				"test",
				panlProperties,
				collectionProperties);

		List<LpseToken> lpseTokens = collectionRequestHandler.parseLpse(URIPath, query);

		Map<String, List<LpseToken>> panlTokenMap = TestHelper.getPanlTokenMap(lpseTokens);
		PaginationProcessor paginationProcessor = new PaginationProcessor(collectionProperties);

		return(paginationProcessor.processToObject(panlTokenMap, getMockedQueryResponse(numResults, true)));
	}

	public static JSONObject invokeAvailableProcesser(
			String propertiesFileLocation,
			String URIPath,
			String query,
			long numResults,
			boolean numFoundExact) throws IOException, PanlServerException {
		PanlProperties panlProperties = TestHelper.getTestPanlProperties();
		CollectionProperties collectionProperties = new CollectionProperties(
				"test",
				TestHelper.getTestProperties(propertiesFileLocation));
		// now to parse the query

		CollectionRequestHandler collectionRequestHandler = new CollectionRequestHandler(
				"test",
				panlProperties,
				collectionProperties);

		List<LpseToken> lpseTokens = collectionRequestHandler.parseLpse(URIPath, query);

		Map<String, List<LpseToken>> panlTokenMap = TestHelper.getPanlTokenMap(lpseTokens);
		AvailableProcessor availableProcessor = new AvailableProcessor(collectionProperties);

		return(availableProcessor.processToObject(panlTokenMap, getMockedQueryResponse(numResults, numFoundExact)));
	}

	public static JSONObject invokeQueryOperandProcesser(
			String propertiesFileLocation,
			String URIPath,
			String query) throws IOException, PanlServerException {
		PanlProperties panlProperties = TestHelper.getTestPanlProperties();
		CollectionProperties collectionProperties = new CollectionProperties(
				"test",
				TestHelper.getTestProperties(propertiesFileLocation));
		// now to parse the query

		CollectionRequestHandler collectionRequestHandler = new CollectionRequestHandler(
				"test",
				panlProperties,
				collectionProperties);

		List<LpseToken> lpseTokens = collectionRequestHandler.parseLpse(URIPath, query);

		Map<String, List<LpseToken>> panlTokenMap = TestHelper.getPanlTokenMap(lpseTokens);
		QueryOperandProcessor queryOperandProcessor = new QueryOperandProcessor(collectionProperties);

		return(queryOperandProcessor.processToObject(panlTokenMap, getMockedQueryResponse(100, true)));
	}

	public static String invokeCanonicalURIProcessor(
			String propertiesFileLocation,
			String URIPath,
			String query) throws IOException, PanlServerException {

		assertTrue(URIPath.startsWith("/test/default/"));

		PanlProperties panlProperties = TestHelper.getTestPanlProperties();
		CollectionProperties collectionProperties = new CollectionProperties(
				"test",
				TestHelper.getTestProperties(propertiesFileLocation));
		// now to parse the query

		CollectionRequestHandler collectionRequestHandler = new CollectionRequestHandler(
				"test",
				panlProperties,
				collectionProperties);

		List<LpseToken> lpseTokens = collectionRequestHandler.parseLpse(URIPath, query);

		Map<String, List<LpseToken>> panlTokenMap = TestHelper.getPanlTokenMap(lpseTokens);
		CanonicalURIProcessor canonicalURIProcessor = new CanonicalURIProcessor(collectionProperties);

		return(canonicalURIProcessor.processToString(panlTokenMap));
	}

}
