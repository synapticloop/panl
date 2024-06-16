package com.synapticloop.panl;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.generator.bean.Collection;
import com.synapticloop.panl.server.client.PanlCloudSolrClient;
import com.synapticloop.panl.server.handler.CollectionRequestHandler;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.helper.CollectionHelper;
import com.synapticloop.panl.server.handler.processor.*;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.json.JSONObject;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TestHelper {
	public static final String DEFAULT_PROPERTIES = "/default.properties";
	public static final String COLLECTION_NAME_TEST = "test";

	private static PanlProperties testPanlProperties;
	private static final Map<String, CollectionRequestHandler> COLLECTION_REQUEST_HANDLER_CACHE = new HashMap<>();

	public static CollectionRequestHandler DEFAULT_HANDLER;
	public static CollectionRequestHandler DEFAULT_OR_HANDLER;
	public static CollectionRequestHandler DEFAULT_LPSE_LENGTH_TWO_HANDLER;
	public static CollectionRequestHandler MECHANICAL_PENCILS_HANDLER;

	public static void beforeAll() throws PanlServerException, IOException {
		if (null == DEFAULT_HANDLER) {
			DEFAULT_HANDLER = TestHelper.getCollectionRequestHandler("/default.properties");
		}

		if (null == DEFAULT_OR_HANDLER) {
			DEFAULT_OR_HANDLER = TestHelper.getCollectionRequestHandler("/default.or.properties");
		}

		if (null == DEFAULT_LPSE_LENGTH_TWO_HANDLER) {
			DEFAULT_LPSE_LENGTH_TWO_HANDLER = TestHelper.getCollectionRequestHandler("/default.lpse.length.2.properties");
		}

		if (null == MECHANICAL_PENCILS_HANDLER) {
			MECHANICAL_PENCILS_HANDLER = TestHelper.getCollectionRequestHandler("/mechanical-pencils.panl.properties");
		}

	}

	public static CollectionRequestHandler getCollectionRequestHandler(String propertiesFileLocation) throws IOException, PanlServerException {
		if (!COLLECTION_REQUEST_HANDLER_CACHE.containsKey(propertiesFileLocation)) {
			PanlProperties panlProperties = TestHelper.getTestPanlProperties();
			CollectionProperties collectionProperties = getCollectionProperties(propertiesFileLocation);
			// now to parse the query

			CollectionRequestHandler collectionRequestHandler = new CollectionRequestHandler(
					COLLECTION_NAME_TEST,
					COLLECTION_NAME_TEST,
					panlProperties,
					collectionProperties);

			COLLECTION_REQUEST_HANDLER_CACHE.put(propertiesFileLocation, collectionRequestHandler);
		}

		return (COLLECTION_REQUEST_HANDLER_CACHE.get(propertiesFileLocation));

	}

	private static final Map<String, CollectionProperties> collectionPropertiesCache = new HashMap<>();

	public static CollectionProperties getCollectionProperties(String propertiesFileLocation) {
		CollectionProperties collectionProperties;
		if(!collectionPropertiesCache.containsKey(propertiesFileLocation)) {
			try {
				collectionProperties = new CollectionProperties(COLLECTION_NAME_TEST, COLLECTION_NAME_TEST, TestHelper.getTestProperties(propertiesFileLocation));
				collectionPropertiesCache.put(propertiesFileLocation, collectionProperties);
			} catch (PanlServerException | IOException e) {
				fail(e);
			}
		}

		return(collectionPropertiesCache.get(propertiesFileLocation));
	}

	public static LpseTokeniser getLpseTokeniser(String uriPath) {
		return (new LpseTokeniser(uriPath, Collection.CODES_AND_METADATA, true));
	}

	public static Properties getTestProperties(String propertiesName) throws IOException {
		Properties properties = new Properties();
		properties.load(TestHelper.class.getResourceAsStream(propertiesName));
		return (properties);
	}

	public static PanlProperties getTestPanlProperties() throws IOException {
		if (null == testPanlProperties) {
			testPanlProperties = new PanlProperties(getTestProperties("/test.panl.properties"));
		}
		return (testPanlProperties);
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

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static QueryResponse getMockedQueryResponse(long numFound, boolean numFoundExact) {
		return (getMockedQueryResponse(new ArrayList<>(), numFound, numFoundExact));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static QueryResponse getMockedQueryResponse(List<FacetCountBean> facetCountBeans, long numFound, boolean numFoundExact) {
		QueryResponse mockedQueryResponse = mock(QueryResponse.class);
		NamedList mockedNamedListResponse = mock(NamedList.class);
		SolrDocumentList mockedSolrDocumentList = mock(SolrDocumentList.class);

		List<FacetField> facetFields = new ArrayList<>();
		for (FacetCountBean facetCountBean : facetCountBeans) {
			FacetField facetField = new FacetField(facetCountBean.getName());
			for (long count : facetCountBean.getCounts()) {
				facetField.insert("facet-count-" + count, count);
			}
			facetFields.add(facetField);
		}
		when(mockedQueryResponse.getFacetFields()).thenReturn(facetFields);

		when(mockedQueryResponse.getResponse()).thenReturn(mockedNamedListResponse);
		when(mockedNamedListResponse.get("response")).thenReturn(mockedSolrDocumentList);
		when(mockedSolrDocumentList.getNumFound()).thenReturn(numFound);
		when(mockedSolrDocumentList.getNumFoundExact()).thenReturn(numFoundExact);

		return (mockedQueryResponse);
	}

	public static JSONObject invokePaginationProcessor(
			String propertiesFileLocation,
			String URIPath,
			String query,
			long numResults) throws IOException, PanlServerException {

		PanlProperties panlProperties = TestHelper.getTestPanlProperties();
		CollectionProperties collectionProperties = getCollectionProperties(propertiesFileLocation);
		// now to parse the query

		CollectionRequestHandler collectionRequestHandler = new CollectionRequestHandler(
				COLLECTION_NAME_TEST,
				COLLECTION_NAME_TEST,
				panlProperties,
				collectionProperties);

		List<LpseToken> lpseTokens = collectionRequestHandler.parseLpse(URIPath, query);

		Map<String, List<LpseToken>> panlTokenMap = TestHelper.getPanlTokenMap(lpseTokens);
		PaginationProcessor paginationProcessor = new PaginationProcessor(collectionProperties);

		return (paginationProcessor.processToObject(panlTokenMap, getMockedQueryResponse(numResults, true)));
	}

	public static JSONObject invokeActiveProcessor(
			String propertiesFileLocation,
			String URIPath,
			String query) throws IOException, PanlServerException {
		PanlProperties panlProperties = TestHelper.getTestPanlProperties();

		CollectionProperties collectionProperties = getCollectionProperties(propertiesFileLocation);
		// now to parse the query

		CollectionRequestHandler collectionRequestHandler = new CollectionRequestHandler(
				COLLECTION_NAME_TEST,
				COLLECTION_NAME_TEST,
				panlProperties,
				collectionProperties);

		List<LpseToken> lpseTokens = collectionRequestHandler.parseLpse(URIPath, query);

		Map<String, List<LpseToken>> panlTokenMap = TestHelper.getPanlTokenMap(lpseTokens);
		ActiveProcessor activeProcessor = new ActiveProcessor(collectionProperties);

		return (activeProcessor.processToObject(panlTokenMap));
	}

	public static JSONObject invokeSortingProcessor(
			String propertiesFileLocation,
			String URIPath,
			String query,
			long numResults) throws IOException, PanlServerException {

		PanlProperties panlProperties = TestHelper.getTestPanlProperties();

		CollectionProperties collectionProperties = getCollectionProperties(propertiesFileLocation);
		// now to parse the query

		CollectionRequestHandler collectionRequestHandler = new CollectionRequestHandler(
				COLLECTION_NAME_TEST,
				COLLECTION_NAME_TEST,
				panlProperties,
				collectionProperties);

		List<LpseToken> lpseTokens = collectionRequestHandler.parseLpse(URIPath, query);

		Map<String, List<LpseToken>> panlTokenMap = TestHelper.getPanlTokenMap(lpseTokens);
		SortingProcessor sortingProcessor = new SortingProcessor(collectionProperties);

		return (sortingProcessor.processToObject(panlTokenMap, getMockedQueryResponse(numResults, true)));
	}

	public static JSONObject invokeAvailableProcessor(
			String propertiesFileLocation,
			String URIPath,
			String query,
			long numResults,
			boolean numFoundExact) throws IOException, PanlServerException {

		return (invokeAvailableProcessor(new ArrayList<>(),
				propertiesFileLocation,
				URIPath,
				query,
				numResults,
				numFoundExact)

		);
	}

	public static JSONObject invokeAvailableProcessor(
			List<FacetCountBean> facetList,
			String propertiesFileLocation,
			String URIPath,
			String query,
			long numResults,
			boolean numFoundExact) throws IOException, PanlServerException {
		PanlProperties panlProperties = TestHelper.getTestPanlProperties();

		CollectionProperties collectionProperties = getCollectionProperties(propertiesFileLocation);
		// now to parse the query

		CollectionRequestHandler collectionRequestHandler = new CollectionRequestHandler(
				COLLECTION_NAME_TEST,
				COLLECTION_NAME_TEST,
				panlProperties,
				collectionProperties);

		List<LpseToken> lpseTokens = collectionRequestHandler.parseLpse(URIPath, query);

		Map<String, List<LpseToken>> panlTokenMap = TestHelper.getPanlTokenMap(lpseTokens);
		AvailableProcessor availableProcessor = new AvailableProcessor(collectionProperties);

		return (availableProcessor.processToObject(panlTokenMap, getMockedQueryResponse(facetList, numResults, numFoundExact)));
	}

	public static JSONObject invokeQueryOperandProcessor(
			String propertiesFileLocation,
			String URIPath,
			String query) throws IOException, PanlServerException {
		PanlProperties panlProperties = TestHelper.getTestPanlProperties();

		CollectionProperties collectionProperties = getCollectionProperties(propertiesFileLocation);
		// now to parse the query

		CollectionRequestHandler collectionRequestHandler = new CollectionRequestHandler(
				COLLECTION_NAME_TEST,
				COLLECTION_NAME_TEST,
				panlProperties,
				collectionProperties);

		List<LpseToken> lpseTokens = collectionRequestHandler.parseLpse(URIPath, query);

		Map<String, List<LpseToken>> panlTokenMap = TestHelper.getPanlTokenMap(lpseTokens);
		QueryOperandProcessor queryOperandProcessor = new QueryOperandProcessor(collectionProperties);

		return (queryOperandProcessor.processToObject(panlTokenMap, getMockedQueryResponse(100, true)));
	}

	public static String invokeCanonicalURIProcessor(
			String propertiesFileLocation,
			String URIPath,
			String query) throws IOException, PanlServerException {

		assertTrue(URIPath.startsWith("/test/default/"));

		PanlProperties panlProperties = TestHelper.getTestPanlProperties();

		CollectionProperties collectionProperties = getCollectionProperties(propertiesFileLocation);
		// now to parse the query

		CollectionRequestHandler collectionRequestHandler = new CollectionRequestHandler(
				COLLECTION_NAME_TEST,
				COLLECTION_NAME_TEST,
				panlProperties,
				collectionProperties);

		List<LpseToken> lpseTokens = collectionRequestHandler.parseLpse(URIPath, query);

		Map<String, List<LpseToken>> panlTokenMap = TestHelper.getPanlTokenMap(lpseTokens);
		CanonicalURIProcessor canonicalURIProcessor = new CanonicalURIProcessor(collectionProperties);

		return (canonicalURIProcessor.processToString(panlTokenMap));
	}

	public static void assertCanonicalURI(String URIPath, String expect) {
		assertCanonicalURI("/default.properties", URIPath, expect);
	}

	public static void assertCanonicalURI(String propertiesFileLocation, String URIPath, String expect) {
		String uriPath = null;
		try {
			uriPath = TestHelper.invokeCanonicalURIProcessor(
					propertiesFileLocation,
					URIPath,
					"");
		} catch (IOException | PanlServerException e) {
			fail(e);
		}
		assertEquals(expect, uriPath);
	}

	public static void mockCollectionRequestHandler(
			String propertiesFilLocation,
			String returnJsonResponse,
			String uri,
			String query) throws IOException, PanlServerException, SolrServerException {

		CollectionRequestHandler collectionRequestHandler = getCollectionRequestHandler(propertiesFilLocation);
		PanlCloudSolrClient mockPanlClient = mock(PanlCloudSolrClient.class);
		try (MockedStatic<CollectionHelper> collectionHelperMockedStatic = mockStatic(CollectionHelper.class)) {
			collectionHelperMockedStatic.when(() ->
					CollectionHelper.getPanlClient(
							"CloudSolrClient",
							collectionRequestHandler.getSolrCollection(),
							getTestPanlProperties(),
							getCollectionProperties(propertiesFilLocation))).thenReturn(mockPanlClient);
		}


		SolrClient mockSolrClient = mock(CloudSolrClient.class);
		when(mockPanlClient.getClient()).thenReturn(mockSolrClient);

		QueryResponse mockQueryResponse = mock(QueryResponse.class);

		SolrQuery mockSolrQuery = mock(SolrQuery.class);
		when(mockSolrClient.query(collectionRequestHandler.getSolrCollection(), mockSolrQuery)).thenReturn(mockQueryResponse);

		when(mockQueryResponse.jsonStr()).thenReturn(IOUtils.toString(TestHelper.class.getResourceAsStream(returnJsonResponse), StandardCharsets.UTF_8));
	}

	public static void mockSolrQuery(
			String propertiesFilLocation,
			String returnJsonResponse,
			String uri,
			String query) throws IOException, PanlServerException, SolrServerException {

		CollectionRequestHandler collectionRequestHandler = getCollectionRequestHandler(propertiesFilLocation);
		PanlCloudSolrClient mockPanlClient = mock(PanlCloudSolrClient.class);
		try (MockedStatic<CollectionHelper> collectionHelperMockedStatic = mockStatic(CollectionHelper.class)) {
			collectionHelperMockedStatic.when(() ->
					CollectionHelper.getPanlClient(
							"CloudSolrClient",
							collectionRequestHandler.getSolrCollection(),
							getTestPanlProperties(),
							getCollectionProperties(propertiesFilLocation))).thenReturn(mockPanlClient);
		}


		SolrClient mockSolrClient = mock(CloudSolrClient.class);
		when(mockPanlClient.getClient()).thenReturn(mockSolrClient);

		QueryResponse mockQueryResponse = mock(QueryResponse.class);

		SolrQuery mockSolrQuery = mock(SolrQuery.class);
		when(mockSolrClient.query(collectionRequestHandler.getSolrCollection(), mockSolrQuery)).thenReturn(mockQueryResponse);

		when(mockQueryResponse.jsonStr()).thenReturn(IOUtils.toString(TestHelper.class.getResourceAsStream(returnJsonResponse), StandardCharsets.UTF_8));
	}

	public static SolrQuery testApplyToQueryContains(String propertiesFileLocation, String uriPath, String contains) throws PanlServerException, IOException {
		SolrQuery solrQuery = new SolrQuery("");
		CollectionProperties collectionProperties = TestHelper.getCollectionProperties(propertiesFileLocation);
		CollectionRequestHandler collectionRequestHandler = TestHelper.getCollectionRequestHandler(propertiesFileLocation);
		List<LpseToken> lpseTokens = collectionRequestHandler.parseLpse(uriPath, "");
		Map<String, List<LpseToken>> panlTokenMap = TestHelper.getPanlTokenMap(lpseTokens);

		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			lpseField.applyToQuery(solrQuery, panlTokenMap);
		}

		System.out.println(solrQuery.toString());
		assertTrue(solrQuery.toString().contains(contains));
		return(solrQuery);
	}

	public static SolrQuery testApplyToQueryEquals(String propertiesFileLocation, String uriPath, String equals) throws PanlServerException, IOException {
		SolrQuery solrQuery = new SolrQuery("");
		CollectionProperties collectionProperties = TestHelper.getCollectionProperties(propertiesFileLocation);
		CollectionRequestHandler collectionRequestHandler = TestHelper.getCollectionRequestHandler(propertiesFileLocation);
		List<LpseToken> lpseTokens = collectionRequestHandler.parseLpse(uriPath, "");
		Map<String, List<LpseToken>> panlTokenMap = TestHelper.getPanlTokenMap(lpseTokens);

		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			lpseField.applyToQuery(solrQuery, panlTokenMap);
		}

		System.out.println(solrQuery.toString());
		assertEquals(solrQuery.toString(), equals);
		return(solrQuery);
	}

	public static List<LpseToken> getLpseTokens(String propertiesFileLocation, String uriPath, String query) throws PanlServerException, IOException {
		CollectionRequestHandler collectionRequestHandler = TestHelper.getCollectionRequestHandler(propertiesFileLocation);
		return(collectionRequestHandler.parseLpse(uriPath, query));
	}

}
