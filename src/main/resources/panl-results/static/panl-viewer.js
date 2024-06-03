$(document).ready(function() {
	var availableCollections = $("#available_collections");
	for (const collectionUrl of collections) {
		availableCollections.append("[<a href=\"" + panlResultsViewerUrl + collectionUrl + "\">" + collectionUrl + "</a>]&nbsp;");
	}

	// test to see whether we are ready to invoke the panl search service
	var uris = window.location.pathname.split("/");
	if(uris.length >= 4) {
		// we have a collection, and field set
		var collection = uris[2];
		var fieldset = uris[3];
		if(isValidUrl(collection, fieldset)) {
			$("#collection").append("/" + collection + "/" + fieldset);
			panlSearch();
		}
	}
});

function isValidUrl(collection, fieldset) {
	var fullUrl = "/" + collection + "/" + fieldset;
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

	$("#canonical_uri").append(panlJsonData.panl.canonical_uri);

	// first up the total results
	$("#num_results")
		.append("- Found " +
			panlJsonData.response.numFound +
			" result(s) " +
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
	$("#num_shown").append(panlJsonData.response.docs.length);

	// add in the results
	for(const document of panlJsonData.response.docs) {
		var innerList = "";
		for(const fieldName in document) {
			innerList += "<dt>" + panlJsonData.panl.fields[fieldName] + " (" + fieldName + ")</dt>";
			innerList += "<dd>" + document[fieldName] + "</dd>";
		}

		$("#documents").append("<dl>" + innerList + "</dl>");
	}

	addQueryOperand(panlJsonData.panl.query_operand);
	addSortingOptions(panlJsonData.panl.sorting);
	addPagination(panlJsonData.panl.pagination);
	addActiveFilters(panlJsonData.panl.active);
	addAvailableFilters(panlJsonData.panl.available);
}

function addQueryOperand(queryOperand) {
	$("#query_operand").append(
		"<a href=\"" +
    panlResultsViewerUrl +
    $("#collection").text() +
    queryOperand.AND +
    "\"/>AND</a>&nbsp;");
	$("#query_operand").append(
		" || <a href=\"" +
    panlResultsViewerUrl +
    $("#collection").text() +
    queryOperand.OR +
    "\"/>OR</a>&nbsp;");

}
function addSortingOptions(sortingObject) {
	$("#sorting_options").append(
		"Relevance: <a href=\"" +
    panlResultsViewerUrl +
    $("#collection").text() +
    sortingObject.relevance.replace_asc +
    "\"/>ASC</a>&nbsp;");

	$("#sorting_options").append(
		"<a href=\"" +
    panlResultsViewerUrl +
    $("#collection").text() +
    sortingObject.relevance.replace_desc +
    "\"/>DESC</a>&nbsp;");

	for(const sortName in sortingObject.fields) {
	$("#sorting_options").append(
		"||&nbsp;" +
		sortingObject.fields[sortName].name +
		": <a href=\"" +
    panlResultsViewerUrl +
    $("#collection").text() +
    sortingObject.fields[sortName].replace_asc +
    "\"/>ASC</a>&nbsp;");

	$("#sorting_options").append(
		"<a href=\"" +
    panlResultsViewerUrl +
    $("#collection").text() +
    sortingObject.fields[sortName].replace_desc +
    "\"/>DESC</a>&nbsp;");
	}
}

function addPagination(paginationObject) {
	$("#page_num").append(paginationObject.page_num);
	$("#num_pages").append(paginationObject.num_pages);
	$("#num_per_page").append(paginationObject.num_per_page);

	if(paginationObject.page_uris.next) {
		$("#next").append(
			"<a href=\"" +
			panlResultsViewerUrl +
			$("#collection").text() +
			paginationObject.page_uris.next +
			"\"/>NEXT</a> &raquo;");
	} else {
		$("#next").append("NEXT &raquo;");
	}

	if(paginationObject.page_uris.previous) {
		$("#previous").append(
			"&laquo; <a href=\"" +
			panlResultsViewerUrl +
			$("#collection").text() +
			paginationObject.page_uris.previous +
			"\"/>PREV</a>");
	} else {
		$("#previous").append("&laquo; PREV");
	}

	// now for the per_page_uris
	addPerPage(paginationObject, "3");
	addPerPage(paginationObject, "5");
	addPerPage(paginationObject, "10");
}

function addPerPage(paginationObject, number) {
$("#num_per_page_links").append("<a href=\"" +
	panlResultsViewerUrl +
  $("#collection").text() +
  paginationObject.num_per_page_uris.before +
  number +
  paginationObject.num_per_page_uris.after +
  "\">" + number +"</a>&nbsp;");
}

function addActiveFilters(activeObject) {
	const active = $("#active");
	// first up the query
	if(activeObject.query !== undefined) {
		active.append("<li><strong>Query <em>(" + activeObject.query[0].panl_code + ")</em></strong></li>");
		active.append("<li><a href=\"" + panlResultsViewerUrl +
                        $("#collection").text() +
                        activeObject.query[0].uri +
                        "\">[remove]</a>&nbsp;" +
                  			activeObject.query[0].value +
                  			"</li><li><hr /></li>");
	}

	// now for the facets
	if(activeObject.facet !== undefined) {
		addActiveFacets(activeObject.facet);
	}
}

function addActiveFacets(facets) {
	const active = $("#active");
	// facets first
	var currentFacetName = "";

	for(const facet of facets) {
		if(facet.facet_name !== currentFacetName) {
			active.append("<li><strong>" + facet.name + " <em>(" + facet.panl_code + ")</em></strong></li>");
			currentFacetName = facet.facet_name;
		}
		active.append("<li><a href=\"" + panlResultsViewerUrl +
                        $("#collection").text() +
                        facet.uri +
                        "\">[remove]</a>&nbsp;" +
                  			facet.value +
                  			"</li>");

	}
	active.append("<li><hr /></li>");
}

function addAvailableFilters(availableArray) {
	// first up the facets
	const available = $("#available");
	for(const facet of availableArray) {
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
		available.append("<li><strong>" + facet.name + " <em>(" + facet.panl_code + ")</em></strong>" + innerUl + "</li>");
	}

}