package com.synapticloop.panl.server.bean;

import com.synapticloop.panl.exception.PanlServerException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudHttp2SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Collection {
	private static final Logger LOGGER = LoggerFactory.getLogger(Collection.class);
	private final String collectionName;

	public String getCollectionName() {
		return collectionName;
	}
	private final String propertiesFileLocation;

	public Collection(String collectionName, String propertiesFileLocation) throws PanlServerException {
		this.collectionName = collectionName;
		this.propertiesFileLocation = propertiesFileLocation;

		Properties properties = new Properties();
		try {
			properties.load(new FileReader(propertiesFileLocation));
		} catch (IOException e) {
			throw new PanlServerException("Could not find the properties file '" + propertiesFileLocation + "'", e);
		}

		// at this point we can load up all the fields and orders etc.

	}

	/**
	 * The URI will be of the format /<collection_name>/<field_list>/<facets>/.../lpse/
	 *
	 * @param uri
	 */
	public void convertUri(String uri) {
		List<String> parts = new ArrayList<>();
		boolean first = true;
		String last = "";
		for (String part : uri.split("/")) {
			// we can skip the first
			if(first) {
				continue;
			}
			first = false;

			// the last part is the lpse
			parts.add(part);
			last = part;
		}

		// at this point we can iterate through the last part
		for(String part: parts) {
			System.out.println(part);
		}


	}

	public String request() {
		final List<String> solrUrls = new ArrayList<>();
		solrUrls.add("http://localhost:8983/solr/");
		solrUrls.add("http://localhost:7574/solr/");
		try (CloudHttp2SolrClient client = new CloudHttp2SolrClient.Builder(solrUrls).build()) {
			final SolrQuery query = new SolrQuery("*:*");
			query.addField("id");
			query.addField("name");
			query.setSort("id", SolrQuery.ORDER.asc);
			query.setRows(10);
			query.addFacetField("manu_id_s");
			query.addFacetField("cat");
			query.addFacetField("name");

			final QueryResponse response = client.query("techproducts", query);
			final SolrDocumentList documents = response.getResults();
			return(response.jsonStr());
		} catch (IOException | SolrServerException e) {
			throw new RuntimeException(e);
		}
	}
}
