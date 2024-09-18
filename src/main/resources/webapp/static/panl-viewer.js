$(document).ready(function() {
	var availableCollections = $("#available_collections");
	var currentCollectionName = "";
	for (const collectionUrl of collections) {
		const lastIndex = collectionUrl.lastIndexOf("/");
		const collectionName = collectionUrl.substring(1, lastIndex);
		const fieldSet = collectionUrl.substring(lastIndex +1);
		if(currentCollectionName !== collectionName) {
			availableCollections.append("<br />&nbsp;<strong>" + collectionName + "</strong>")
			currentCollectionName = collectionName;
		}

		availableCollections.append("&nbsp;-&nbsp;<a href=\"" + panlResultsViewerUrl + collectionUrl + "\">[&nbsp;" + fieldSet + "&nbsp;]</a>");
	}

	availableCollections.append("<br />")

	// test to see whether we are ready to invoke the panl search service
	var uris = window.location.pathname.split("/");
	if(uris.length >= 4) {
		// we have a collection, and field set
		var collection = uris[2];
		var fieldset = uris[3];
		if(isValidUrl(collection, fieldset)) {
			$("#searchfield").removeAttr("disabled");
			$("#searchbutton").removeAttr("disabled");
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
	console.log("[ RETURNED JSON OBJECT ]")
	console.log(panlJsonData);

	console.log("[ RETURNED PANL JSON OBJECT ]")
	console.log(panlJsonData.panl);

	console.log("[ RETURNED PANL CANONICAL URI JSON OBJECT ]")
	console.log(panlJsonData.panl.timings);

	$("#explain-collection").attr("href", "/panl-results-explainer" + $("#collection").text() + "/?explain=" + panlJsonData.panl.canonical_uri)

	$("#canonical_uri").append(panlJsonData.panl.canonical_uri);

	// first up the total results
	$("#num_results")
		.append("- Found " +
			panlJsonData.panl.pagination.num_results +
			" result(s) " +
			(panlJsonData.panl.pagination.num_results_exact ? "(exact)" : "(estimated)"));

	console.log("[ RETURNED PANL TIMINGS JSON OBJECT ]")
	console.log(panlJsonData.panl.timings);

	// now for the timings
	const timings = panlJsonData.panl.timings;
	$("#timings")
			.append("<strong>Solr:</strong> query time " + panlJsonData.responseHeader.QTime +
				"ms.<br /><strong>Panl:</strong> parse request " + timings.panl_parse_request_time +
				"ms, build request " + timings.panl_build_request_time +
				"ms, send and receive request " + timings.panl_send_request_time +
				"ms, parse response " + timings.panl_build_response_time +
				"ms. Total time " + timings.panl_total_time + "ms.");

	// Solr previous to 9 has a different structure
	var documents = [];
	// now the number that we are showing
	if(panlJsonData.response.docs !== undefined) {
		documents = panlJsonData.response.docs;
	} else {
		documents = panlJsonData.response;
	}

	// add in the results
	for(const document of documents) {
		var innerList = "";
		for(const fieldName in document) {
			innerList += "<dt>" + panlJsonData.panl.fields[fieldName] + " (" + fieldName + ")</dt>";
			innerList += "<dd>" + document[fieldName] + "</dd>";
		}

		$("#documents").append("<dl>" + innerList + "</dl>");
	}

	$("#num_shown").append(documents.length);

	addQueryOperand(panlJsonData.panl.query_operand);
	addSortingOptions(panlJsonData.panl.sorting, panlJsonData.panl.active);
	addPagination(panlJsonData.panl.pagination);
	addActiveFilters(panlJsonData.panl.active, panlJsonData.panl.sorting.remove_uri);
	addAvailableFilters(panlJsonData.panl.available, panlJsonData.panl.active);
}

function addQueryOperand(queryOperand) {
	console.log("[ RETURNED PANL QUERY OPERAND JSON OBJECT ]")
	console.log(queryOperand);

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

function getActiveSortingObject(sortFacetName, activeObject) {
	if(activeObject.sort === undefined) {
		return(undefined);
	}

	for(const activeSortingObject of activeObject.sort) {
		if(activeSortingObject.facet_name === sortFacetName) {
			return(activeSortingObject);
		}
	}
}

function addSortingOptions(sortingObject, activeObject) {
	console.log("[ RETURNED PANL SORTING JSON OBJECT ]")
	console.log(sortingObject);

	var hasFirst = false;
	for(const sortIndex in sortingObject.fields) {
		if(hasFirst) {
			$("#sorting_options").append("||&nbsp;");
		}

		hasFirst = true;

		var isAscending = false;
		var isDescending = false;

		activeSortingObject = getActiveSortingObject(sortingObject.fields[sortIndex].facet_name, activeObject);
		if(activeSortingObject !== undefined) {
			isAscending = !activeSortingObject.is_descending;
			isDescending = activeSortingObject.is_descending;
		}


		if(!isAscending) {
			$("#sorting_options").append(
				sortingObject.fields[sortIndex].name +
				": <a href=\"" +
				panlResultsViewerUrl +
				$("#collection").text() +
				sortingObject.fields[sortIndex].set_uri_asc +
				"\"/>ASC</a>&nbsp;");
		} else {
			$("#sorting_options").append(
				sortingObject.fields[sortIndex].name +
				": ASC&nbsp;");
		}

		if(!isDescending) {
			$("#sorting_options").append(
				"<a href=\"" +
				panlResultsViewerUrl +
				$("#collection").text() +
				sortingObject.fields[sortIndex].set_uri_desc +
				"\"/>DESC</a>&nbsp;");
		} else {
			$("#sorting_options").append("DESC&nbsp;");
		}
	}

	var hasAddedThen = false;
	// now for the additive fields
	var numThenSorts = 0;
	for(const sortIndex in sortingObject.fields) {
		if(sortingObject.fields[sortIndex].add_uri_asc !== undefined) {
			numThenSorts++;
		}
	}

	if(numThenSorts == sortingObject.fields.length) {
		return;
	}

	for(const sortIndex in sortingObject.fields) {
		if(sortingObject.fields[sortIndex].add_uri_asc !== undefined) {

			if(!hasAddedThen) {
				$("#sorting_options").append("<br/><strong>Then sort by </strong>");
				hasAddedThen = true;
			} else {
				$("#sorting_options").append("||&nbsp;");
			}

			$("#sorting_options").append(
				sortingObject.fields[sortIndex].name +
				": <a href=\"" +
				panlResultsViewerUrl +
				$("#collection").text() +
				sortingObject.fields[sortIndex].add_uri_asc +
				"\"/>ASC</a>&nbsp;");

			$("#sorting_options").append(
				"<a href=\"" +
				panlResultsViewerUrl +
				$("#collection").text() +
				sortingObject.fields[sortIndex].add_uri_desc +
				"\"/>DESC</a>&nbsp;");
		}
	}
}

function addPagination(paginationObject) {
	console.log("[ RETURNED PANL PAGINATION JSON OBJECT ]")
	console.log(paginationObject);
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

function addActiveFilters(activeObject, removeUri) {
	console.log("[ RETURNED PANL ACTIVE FACETS JSON OBJECT ]")
	console.log(activeObject);

	const active = $("#active");
	// first up the query
	if(activeObject.query !== undefined) {
		active.append("<li><strong>Query <em>(" + activeObject.query.panl_code + ")</em></strong></li>");
		active.append("<li><a href=\"" + panlResultsViewerUrl +
				$("#collection").text() +
				activeObject.query.remove_uri +
				"\">[remove]</a>&nbsp;" +
				activeObject.query.value +
				"</li><li><hr /></li>");
	}

	// now for the facets
	if(activeObject.facet !== undefined) {
		addActiveFacets(activeObject.facet);
	}

	// finally, the sort
	if(activeObject.sort !== undefined) {
		addActiveSorts(activeObject.sort, removeUri);
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
				facet.remove_uri +
				"\">[remove]</a>&nbsp;" +
				decodePanl(facet.encoded) +
				"</li>");

		if(facet.is_boolean_facet) {
			active.append("<li><a href=\"" + panlResultsViewerUrl +
					$("#collection").text() +
					facet.inverse_uri +
					"\">[invert]</a>&nbsp;" +
					decodePanl(facet.inverse_encoded) +
					"</li>");
		}

	}
	active.append("<li><hr /></li>");
}

function addActiveSorts(sorts, removeUri) {
	const active = $("#active");

	for(const sort of sorts) {
		active.append("<li>Sorted by: <strong>" +
				sort.name +
				" <em>(" +
				sort.panl_code +
				")</em> " +
				(sort.is_descending ? "[DESC]" : "[ASC]") +
				"</strong></li>");

		active.append("<li><a href=\"" + panlResultsViewerUrl +
				$("#collection").text() +
				sort.remove_uri +
				"\">[Remove sort]</a>&nbsp;" +
				"<a href=\"" + panlResultsViewerUrl +
				$("#collection").text() +
				sort.inverse_uri +
				"\">[Change to " +
				(sort.is_descending ? "ASC" : "DESC")+
				"]</a>&nbsp;" +
				"</li>");

	}

	if(sorts.length > 0 ) {
		active.append("<li><br /><a href=\"" +
			panlResultsViewerUrl +
			$("#collection").text() +
			removeUri +
			"\">[Clear all sorting]</a></li>");
	}

	active.append("<li><hr /></li>");
	}

function getActiveRangeObject(rangeFacetName, activeObject) {
	if(activeObject.facet === undefined) {
		return(undefined);
	}

	for(const activeRangeFacetObject of activeObject.facet) {
		if(activeRangeFacetObject.facet_name === rangeFacetName && activeRangeFacetObject.value_to !== undefined) {
			return(activeRangeFacetObject);
		}
	}
}

function addAvailableFilters(availableObject, activeObject) {
	console.log("[ RETURNED PANL AVAILABLE FILTERS JSON OBJECT ]")
	console.log(availableObject);

	// first up the facets
	const available = $("#available");
	for(const facet of availableObject.facets) {
		var innerUl = "<ul>";
		for(const value of facet.values) {
			innerUl += "<li>" +
			"<a href=\"" +
			panlResultsViewerUrl +
			$("#collection").text() +
			facet.uris.before +
			value.encoded +
			facet.uris.after +
			"\">[add]</a>&nbsp;" +
			decodePanl(value.encoded);

			if(!facet.is_or_facet) {
				innerUl += "&nbsp;(" + value.count + ")";
			}
			innerUl += "</li>";
		}
		innerUl += "</ul>"
		available.append("<li><strong>" + facet.name + " <em>(" + facet.panl_code + ")</em></strong>" + innerUl + "</li>");
	}

	const ranges = $("#ranges");
	// now check for the ranges - we are adding another list item so that
	// people can choose which implementation to use.
	for(const facet of availableObject.range_facets) {
		$("#ranges-marker").removeClass("hidden");

		ranges.append("<p><strong>" + facet.name + " <em>(" + facet.panl_code + ") Range</em></strong></p>");

		ranges.append("<div id=\"slider-ui-" + facet.facet_name + "\" class=\"slider-round\"></div>");

		var slider = document.getElementById("slider-ui-" + facet.facet_name);

		var inboundMinValue = parseInt(facet.min);
		var inboundMaxValue = parseInt(facet.max);

		var activeRange = getActiveRangeObject(facet.facet_name, activeObject);
		if(activeRange !== undefined) {
			inboundMaxValue = parseInt(activeRange.value_to);
			inboundMinValue = parseInt(activeRange.value);
		}

		var options = {
				start: [inboundMinValue, inboundMaxValue],
				connect: true,
				range: {
						'min': parseInt(facet.min),
						'max': parseInt(facet.max)
				},
				step: 1,
				pips: {
					mode: 'range',
					density: 10
				},
				facet: facet,
				format: {
					from: function(value) {
						return(parseInt(value));
					},
					to: function(value) {
						return(parseInt(value));
					}
				}
		};
		noUiSlider.create(slider, options);


		ranges.append("<br /><div id=\"range-link-" +
				facet.facet_name +
				"\" class=\"clear range-link\">" +
				"<br /><span class=\"" +
				facet.facet_name +
				"-link\">" +
				inboundMinValue +
				" to " +
				inboundMaxValue +
				"</span></div><div class=\"center\"><a class=\"range-link\" href=\"" +
				panlResultsViewerUrl +
				$("#collection").text() +
				facet.uris.before +
				inboundMinValue +
				facet.uris.during +
				inboundMaxValue +
				facet.uris.after +
				"\" id=\"range-anchor-" +
				facet.facet_name +
				"\">[Apply]</a></div>");
		ranges.append("<div class=\"center\"><span><em>(Actual dynamic range: " + facet.dynamic_min + " to " + facet.dynamic_max + ")</em></span></div>")

		slider.noUiSlider.on("update", function(values, handle, unencoded, tap, positions, noUiSlider) {
			var values = values;
			var facet = noUiSlider.options.facet;
			var rangeLink = $("#range-anchor-" + facet.facet_name);

			// This is used for the min/max value replacement
			var hasMinReplacement = facet.uris.before_min_value !== undefined &&
							values[0] === parseInt(facet.min);
			var hasMaxReplacement = facet.uris.before_max_value !== undefined
							&& values[1] === parseInt(facet.max);

			var generatedHrefBefore =
								facet.uris.before +
								values[0] +
								(!facet.uris.has_infix ? "" : (facet.suffix !== undefined ? facet.suffix : ""));
			var generatedHrefAfter =
								(facet.uris.has_infix ? "" : (facet.prefix !== undefined ? facet.prefix : "")) +
								values[1] +
								facet.uris.after;

			var generatedHrefDuring = facet.uris.during;

			if(facet.uris.before_min_value !== undefined && values[0] === parseInt(facet.min)) {
				generatedHrefBefore = facet.uris.before_min_value;
				if(!facet.uris.has_infix && values[0] === parseInt(facet.min)) {
					generatedHrefDuring = "~";
				}
			}

			if(facet.uris.after_max_value !== undefined && values[1] === parseInt(facet.max)) {
				generatedHrefAfter = facet.uris.after_max_value;
				if(!facet.uris.has_infix&& values[0] === parseInt(facet.max)) {
					generatedHrefDuring = "~";
				}
			}

			rangeLink.attr("href",
					panlResultsViewerUrl +
						$("#collection").text() +
						generatedHrefBefore +
						generatedHrefDuring +
						generatedHrefAfter
					);

			var text =
					(facet.range_min_value !== undefined && values[0] === parseInt(facet.min) ? decodePanl(facet.range_min_value) :
					(facet.prefix !== undefined ? decodePanl(facet.prefix) : "") +
					values[0] +
					(facet.suffix !== undefined ? decodePanl(facet.suffix) : "")
					) +
					" to " +
					(facet.range_max_value !== undefined && values[1] === parseInt(facet.max) ? decodePanl(facet.range_max_value) :
					(facet.prefix !== undefined ? decodePanl(facet.prefix) : "") +
					values[1] +
					(facet.suffix !== undefined ? decodePanl(facet.suffix) : "")
					);
			$("." +facet.facet_name + "-link").text(text);
		});
	}

	// now check for the date
	for(const facet of availableObject.date_range_facets) {
		$("#ranges-marker").removeClass("hidden");

		ranges.append("<p><strong>" + facet.name + " <em>(" + facet.panl_code + ") Date Range</em></strong></p>");

		ranges.append("<form method=\"GET\">" +
				"	<select name=\"previous_next\" id=\"previous_next" + facet.facet_name + "\">" +
				"		<option value=\"next\"" + (facet.next === facet.previous_next ? "selected=\"selected\"" : "") + ">" + decodePanl(facet.next) + "</option>" +
				"		<option value=\"previous\" " + (facet.previous === facet.previous_next ? "selected=\"selected\"" : "") + ">" + decodePanl(facet.previous) + "</option>" +
				"	</select>" +
				"	<label><input class=\"date_number\" id=\"date_number" + facet.facet_name + "\" type=\"text\" name=\"date_number\" value=\"" + (facet.value !== undefined ? facet.value : "") + "\"></label>" +
				"	<select name=\"designator\" id=\"designator" + facet.facet_name + "\">" +
				"		<option value=\"hours\" " + (facet.solr_range_designator === "HOURS" ? "selected=\"selected\"" : "") + ">" + decodePanl(facet.designators.hours) + "</option>" +
				"		<option value=\"days\" " + (facet.solr_range_designator === "DAYS" ? "selected=\"selected\"" : "") + ">" + decodePanl(facet.designators.days) + "</option>" +
				"		<option value=\"months\" " + (facet.solr_range_designator === "MONTHS" ? "selected=\"selected\"" : "") + ">" + decodePanl(facet.designators.months) + "</option>" +
				"		<option value=\"years\" " + (facet.solr_range_designator === "YEARS" ? "selected=\"selected\"" : "") + ">" + decodePanl(facet.designators.years) + "</option>" +
				"	</select>" +
				"</form>");

			ranges.append("<div class=\"center\"><a href=\"\" class=\"range-link\" id=\"anchor-date-range-" + facet.facet_name + "\"></a></div>");

			updateDateRangeLink(facet);

			$("#previous_next" + facet.facet_name).on('change', { facet : facet }, function (e) {
				updateDateRangeLink(e.data.facet);
			});

			$("#designator" + facet.facet_name).on('change', { facet : facet }, function (e) {
				updateDateRangeLink(e.data.facet);
			});

			$("#date_number" + facet.facet_name).on('change', { facet : facet }, function (e) {
				updateDateRangeLink(e.data.facet);
			});
	}
}

function updateDateRangeLink(facet) {
var facetName = facet.facet_name;

	var rangeLink = $("#anchor-date-range-" + facetName);

	var previousNext = $("#previous_next" + facetName + " option:selected").text();
	var dateNumber = $("#date_number" + facetName).val();
	var designator = $("#designator" + facetName + " option:selected").text();
	if(dateNumber === "") {
		rangeLink.text("INVALID");
		rangeLink.attr("href", "#");
		return;
	}

	var text = previousNext + dateNumber + designator;
	rangeLink.text("Apply range: " + decodePanl(text));

	rangeLink.attr("href",
			panlResultsViewerUrl +
			$("#collection").text() +
			facet.uris.before +
			encodePanl(text) +
			facet.uris.after
	);

}

function encodePanl(text) {
	return(encodeURI(text.replaceAll(" ", "+")));
}

function decodePanl(text) {
	return(decodeURI(text)
		.replaceAll("+", " ")
		.replaceAll("%2B", "+")
		.replaceAll("%3A", ":"));
}
