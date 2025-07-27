package book;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.common.SolrInputDocument;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringTokenizer;

public class Main {
	public static void main(String[] args) {

		Http2SolrClient client = new Http2SolrClient.Builder("http://localhost:8983/solr/").build();

		try {
			String txtContents = FileUtils.readFileToString(
					new File("src/test/resources/sample/book/data/herman-melville-moby-dick.txt"),
					StandardCharsets.UTF_8);
			StringTokenizer stringTokenizer = new StringTokenizer(txtContents, "\n");

			int i = 0;
			while (stringTokenizer.hasMoreTokens()) {
				SolrInputDocument doc = new SolrInputDocument();
				String token = stringTokenizer.nextToken();
				if(token.isBlank()) {
					continue;
				}

				doc.addField("id", i++);

				doc.addField("contents", token);
				client.add("book", doc);
				if (i % 1000 == 0) {
					client.commit("book");
				}
			}

			client.commit("book");
		} catch(Exception ignored) {
		} finally {
			client.close();
		}
	}
}
