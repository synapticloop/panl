package com.synapticloop.panl.server;

import com.synapticloop.panl.server.bean.Collection;
import org.apache.solr.client.solrj.impl.CloudHttp2SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.rapidoid.http.Req;
import org.rapidoid.http.ReqRespHandler;
import org.rapidoid.http.Resp;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class PanlReqRespHandler implements ReqRespHandler {
	private final Collection collection;
	public PanlReqRespHandler(Collection collection) {
		this.collection = collection;
	}

	@Override
	public Object execute(Req req, Resp resp) throws Exception {
		collection.convertUri(req.uri());
		return(collection.request());
	}
}
