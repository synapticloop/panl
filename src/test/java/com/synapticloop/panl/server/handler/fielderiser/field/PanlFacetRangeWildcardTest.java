package com.synapticloop.panl.server.handler.fielderiser.field;
import com.synapticloop.panl.TestHelper;
import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.CollectionRequestHandler;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PanlFacetRangeWildcardTest {
	@BeforeAll public static void beforeAll() throws PanlServerException, IOException {
		TestHelper.beforeAll();
	}

	private void testWildcard(String propertiesFileLocation, String uriPath, String contains) throws PanlServerException, IOException {
		SolrQuery solrQuery = new SolrQuery("");
		CollectionProperties collectionProperties = TestHelper.getCollectionProperties(propertiesFileLocation);
		CollectionRequestHandler collectionRequestHandler = TestHelper.getCollectionRequestHandler(propertiesFileLocation);
		List<LpseToken> lpseTokens = collectionRequestHandler.parseLpse(uriPath, "");
		Map<String, List<LpseToken>> panlTokenMap = TestHelper.getPanlTokenMap(lpseTokens);

		for (BaseField lpseField : collectionProperties.getLpseFields()) {
			lpseField.applyToQuery(solrQuery, panlTokenMap);
		}

		assertTrue(solrQuery.toString().contains(contains));

	}

	@Test public void testFromWildcard() throws PanlServerException, IOException {
		testWildcard("/facet/wildcard-min.properties", "/test/default/10~50/w+w/", "weight:[*+TO+50]");
		testWildcard("/facet/wildcard-min.properties", "/test/default/11~50/w+w/", "weight:[11+TO+50]");
		testWildcard("/facet/wildcard-max.properties", "/test/default/10~50/w+w/", "weight:[10+TO+*]");
		testWildcard("/facet/wildcard-max.properties", "/test/default/10~49/w+w/", "weight:[10+TO+49]");
		testWildcard("/facet/wildcard-min-max.properties", "/test/default/10~50/w+w/", "weight:[*+TO+*]");
		testWildcard("/facet/wildcard-min-max.properties", "/test/default/11~50/w+w/", "weight:[11+TO+*]");
		testWildcard("/facet/wildcard-min-max.properties", "/test/default/11~49/w+w/", "weight:[11+TO+49]");
	}
}
