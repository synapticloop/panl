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
import static com.synapticloop.panl.TestHelper.testApplyToQuery;
public class PanlSortFieldTest {
	@BeforeAll public static void beforeAll() throws PanlServerException, IOException {
		TestHelper.beforeAll();
	}

	@Test public void testFromWildcard() throws PanlServerException, IOException {
		testApplyToQuery("/sort/default.properties", "/test/default/sb-/", "&sort=brand+desc");
		testApplyToQuery("/sort/default.properties", "/test/default/sm+/", "&sort=name+asc");
		testApplyToQuery("/sort/default.properties", "/test/default/sm+sb-/", "&sort=name+asc,brand+desc");
		testApplyToQuery("/sort/default.properties", "/test/default/sb-sm+/", "&sort=brand+desc,name+asc");
	}
}
