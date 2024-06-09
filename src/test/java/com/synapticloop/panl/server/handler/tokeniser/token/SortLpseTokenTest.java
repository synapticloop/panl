package com.synapticloop.panl.server.handler.tokeniser.token;

import com.synapticloop.panl.exception.PanlServerException;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.synapticloop.panl.TestHelper.*;

import java.io.IOException;

public class SortLpseTokenTest {
	private void assertValidUriPaths(String remainingUri, String lpseCode, String name, boolean asc) throws PanlServerException, IOException {
		String uriPath = lpseCode + (asc ? "+" : "-");
		SortLpseToken sortLpseToken = new SortLpseToken(
				getCollectionProperties(
						"/default.properties"),
				"s",
				getLpseTokeniser(uriPath));

		assertTrue(sortLpseToken.getIsValid());
		assertEquals(lpseCode, sortLpseToken.getLpseSortCode());
		assertEquals(name, sortLpseToken.getSolrFacetField());
		assertEquals((asc ? "+" : "-"), sortLpseToken.getSortOrderUriKey());
		assertEquals((asc ?  SolrQuery.ORDER.asc : SolrQuery.ORDER.desc), sortLpseToken.getSortOrder());
	}

	private void assertInvalidUriPaths(String remainingUri) throws PanlServerException, IOException {
		SortLpseToken sortLpseToken = new SortLpseToken(
				getCollectionProperties(
						"/default.properties"),
				"s",
				getLpseTokeniser(remainingUri));
		assertFalse(sortLpseToken.getIsValid());
	}

	@Test public void testInvalidTokenising() throws PanlServerException, IOException {

		// no +-
		assertInvalidUriPaths("ww");
		assertInvalidUriPaths("wm");

		// non-valid sortFields
		assertInvalidUriPaths("0-");
		assertInvalidUriPaths("0+");
	}

	@Test public void testValidTokenising() throws PanlServerException, IOException {
		assertValidUriPaths("-", "", null, false);
		assertValidUriPaths("", "", null, false);
		assertValidUriPaths("w", "w", "weight", false);
		assertValidUriPaths("w", "w", "weight", true);
		assertValidUriPaths("m", "m", "name", false);
		assertValidUriPaths("m", "m", "name", true);
	}

}
