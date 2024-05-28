$(document).ready(function() {
	var availableCollections = $("#available_collections");
	for (const collectionUrl of collections) {
		availableCollections.append("[<a href=\"" + panlResultsViewerUrl + collectionUrl + "\">" + collectionUrl + "</a>]&nbsp;");
	}

	// test to see whether we are ready to invoke the panl search service
	var uris = window.location.pathname.split("/");
	if(uris.length >= 5) {
		// we have a collection, and field set
		var collection = uris[2];
		var fieldset = uris[3];
		if(isValidUrl(collection, fieldset)) {
			$("#collection").append("/" + collection + "/" + fieldset + "/");
			panlSearch();
		}
	}
});

function isValidUrl(collection, fieldset) {
	var fullUrl = "/" + collection + "/" + fieldset + "/";
	for (const collectionUrl of collections) {
		if(collectionUrl === fullUrl) {
			return(true);
		}
	}

	return(false);
}

function panlSearch() {
	var panlQueryUrl = window.location.pathname.substring(panlResultsViewerUrl.length) + window.location.search;
	$.ajax({
		url:panlQueryUrl,
		success: function (panlJsonData) {
			populatePanlResults(panlJsonData);
	  }
	});
}

function populatePanlResults(panlJsonData) {
	console.log(panlJsonData);
	// first up the total results
	$("#num_results")
		.append("- Found " +
			panlJsonData.response.numFound +
			" results " +
			(panlJsonData.response.numFoundExact ? "(exact)" : "estimated)"));

	// now for the timings
	const timings = panlJsonData.panl.timings;
	$("#timings")
			.append("<strong>Solr:</strong> query time " + panlJsonData.responseHeader.QTime +
				"ms.<br /><strong>Panl:</strong> parse request " + timings.panl_parse_request_time +
				"ms, build request " + timings.panl_build_request_time +
				"ms, send and receive request " + timings.panl_send_request_time +
				"ms, parse response " + timings.panl_build_response_time +
				"ms.  Total time " + timings.panl_total_time + "ms.");

	// now the number that we are showing
	$("#num_shown")
		.append(panlJsonData.response.docs.length +
			" results ");

	// add in the results
	for(const document of panlJsonData.response.docs) {
		$("#documents").append("<p class\"doc_result\">" + JSON.stringify(document) + "</p>");
	}

	addAvailableFilters(panlJsonData.panl.available);
}

function addAvailableFilters(availableObject) {
	// first up the facets
	const available = $("#available");
	for(const facet of availableObject.facets) {
		var innerUl = "<ul>";
		for(const value of facet.values) {
			innerUl += "<li>" +
			"<a href=\"" + panlResultsViewerUrl +
      $("#collection").text() +
      facet.uris.before +
      value.encoded +
      facet.uris.after +
      "\">[add]</a>&nbsp;" +
			value.value +
			"&nbsp;(" + value.count + ")</li>";
		}
		innerUl += "</ul>"
		available.append("<li>" + facet.name + " <em>(" + facet.panl_code + ")</em>" + innerUl + "</li>");
	}

}