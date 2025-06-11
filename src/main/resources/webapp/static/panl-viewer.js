import {
	getFacetType,
	encodePanl,
	decodePanl
} from "/webapp/static/panl-common-functions.js"

$(document).ready(function () {
	let availableCollections = $("#available_collections");
	let currentCollectionName = "";

	for (const collectionUrl of collections) {
		const lastIndex = collectionUrl.lastIndexOf("/");
		const collectionName = collectionUrl.substring(1, lastIndex);
		const fieldSet = collectionUrl.substring(lastIndex + 1);
		if (currentCollectionName !== collectionName) {
			availableCollections.append("<br />&nbsp;<strong>" + collectionName + "</strong>")
			currentCollectionName = collectionName;
		}

		availableCollections.append("&nbsp;-&nbsp;<a href=\"" + panlResultsViewerUrl + collectionUrl + "/\">[&nbsp;" + fieldSet + "&nbsp;]</a>");
	}

	availableCollections.append("<br />")
	$("#collection_toggle").on("click", function (event) {
		$("#collection_holder").toggle();
	});

	// test to see whether we are ready to invoke the panl search service
	var uris = window.location.pathname.split("/");
	if (uris.length >= 4) {
		// we have a collection, and field set
		var collection = uris[2];
		var fieldset = uris[3];
		if (isValidUrl(collection, fieldset)) {
			$("#searchfield").removeAttr("disabled");
			$("#searchbutton").removeAttr("disabled");
			$("#collection").append("/" + collection + "/" + fieldset);

			panlSearch();

			// now we need to get the lookahead functionality for the search box
			$("#searchfield").autocomplete({
				source: function (request, response) {

					$.ajax({
						url: "http://localhost:8181/panl-lookahead/" + collection + "/" + fieldset,
						data: {
							"search": request.term
						},

						success: function (data) {
							// the data that we receive we shall need to do some re-work
							var responseDocs = data.response.docs;
							var autocompleteDocs = [];
							for (const doc of responseDocs) {
								autocompleteDocs.push(doc.brand + " " + doc.name);
							}
							response(autocompleteDocs);
						},

						error: function (data) {
							response([]);
						}
					});
				},
				minLength: 3
			});
		}
	}
});

function isValidUrl(collection, fieldset) {
	var fullUrl = "/" + collection + "/" + fieldset;
	for (const collectionUrl of collections) {
		if (collectionUrl === fullUrl) {
			return (true);
		}
	}

	return (false);
}

/**
 * Perform the Panl search
 */
function panlSearch() {
	var panlQueryUrl = window.location.pathname.substring(panlResultsViewerUrl.length) + window.location.search;
	$.ajax({
		url: panlQueryUrl,
		success: function (panlJsonData) {
			$("#response_code").text("200");
			$("#response_code").attr("class", "valid");
			populatePanlResults(panlJsonData);
		},
		error: function(request, status, errorThrown) {
			$("#response_code").text(status + " (" + errorThrown + ")");
			$("#response_code").attr("class", "invalid");
		}
	});
}

/**
 * Populate the Panl Results with the JSON results from the server.  This will
 * also console.log individual objects from the JSON response with headings
 * for ease of debugging.
 *
 * @param panlJsonData The Returned Panl JSON data
 */
function populatePanlResults(panlJsonData) {
	console.log("[ RETURNED JSON OBJECT ]")
	console.log(panlJsonData);

	console.log("[ RETURNED PANL JSON OBJECT ]")
	console.log(panlJsonData.panl);

	console.log("[ RETURNED PANL CANONICAL URI JSON OBJECT ]")
	console.log(panlJsonData.panl.canonical_uri);

	$("#explain-collection").attr("href", "/panl-results-explainer" + $("#collection").text() + "/?explain=" + panlJsonData.panl.canonical_uri)

	$("#canonical_uri").append(decodeURIComponent(panlJsonData.panl.canonical_uri));

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
	if (panlJsonData.response.docs !== undefined) {
		documents = panlJsonData.response.docs;
	} else {
		documents = panlJsonData.response;
	}

	// add in the results
	for (const document of documents) {
		var innerList = "";
		var id = document["id"];

		if(panlJsonData.highlighting !== undefined && panlJsonData.highlighting[id] !== undefined) {
			for(const highlight in panlJsonData.highlighting[id]) {
				innerList += "<dt><em>Highlight field: " + highlight + "</em></dt>";
				for(const value of panlJsonData.highlighting[id][highlight]) {
					innerList += "<dd>" + value + "</dd>";
				}
			}
			innerList += "<br /><br />";
		}

		for (const fieldName in document) {
			innerList += "<dt>" + panlJsonData.panl.fields[fieldName] + " (" + fieldName + ")</dt>";
			innerList += "<dd>" + document[fieldName] + "</dd>";
		}



		$("#documents").append("<dl>" + innerList + "</dl>");
	}

	$("#num_shown").append(documents.length);

	addSearchFieldCheckboxes(panlJsonData.panl.search)
	addQueryOperand(panlJsonData.panl.query_operand);
	addSortingOptions(panlJsonData.panl.sorting, panlJsonData.panl.active);
	addPagination(panlJsonData.panl.pagination, panlJsonData.panl.extra);
	addActiveFilters(panlJsonData.panl.active, panlJsonData.panl.sorting.remove_uri);
	addAvailableFilters(panlJsonData.panl.available, panlJsonData.panl.active);
	addMoreLikeThis(documents);
}

function addSearchFieldCheckboxes(searchJson) {
	console.log("[ RETURNED PANL SEARCH FIELDS ]")
	console.log(searchJson);

	$("#searchfield").val(searchJson.keyword);

	if(searchJson.fields !== undefined) {
		let first = true;
		for(const searchField of searchJson.fields) {

			if(first) {
				$("#search-checkboxes").append("<p><strong>Search in:</strong></p>");
				first = false;
			}

			let fieldName = searchJson.query_respond_to + "." + searchField.panl_code;
			$("#search-checkboxes").append(
					"<input type=\"checkbox\" id=\"" +
					fieldName +
					"\" name=\"" +
					fieldName +
					"\"" +
					(searchField.active? " checked" : "") +
					" value=\"-\">\n" +
					"  <label for=\"" +
					fieldName +
					"\">" +
					searchField.value +
					"</label><br>"
			);
		}
	}
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
	if (activeObject.sort === undefined) {
		return (undefined);
	}

	for (const activeSortingObject of activeObject.sort) {
		if (activeSortingObject.facet_name === sortFacetName) {
			return (activeSortingObject);
		}
	}
}

function addSortingOptions(sortingObject, activeObject) {
	console.log("[ RETURNED PANL SORTING JSON OBJECT ]")
	console.log(sortingObject);

	var hasFirst = false;
	for (const sortIndex in sortingObject.fields) {
		if (hasFirst) {
			$("#sorting_options").append("||&nbsp;");
		}

		hasFirst = true;

		var isAscending = false;
		var isDescending = false;

		let activeSortingObject = getActiveSortingObject(sortingObject.fields[sortIndex].facet_name, activeObject);
		if (activeSortingObject !== undefined) {
			isAscending = !activeSortingObject.is_descending;
			isDescending = activeSortingObject.is_descending;
		}


		if (!isAscending) {
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

		if (!isDescending) {
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
	for (const sortIndex in sortingObject.fields) {
		if (sortingObject.fields[sortIndex].add_uri_asc !== undefined) {
			numThenSorts++;
		}
	}

	if (numThenSorts == sortingObject.fields.length) {
		return;
	}

	for (const sortIndex in sortingObject.fields) {
		if (sortingObject.fields[sortIndex].add_uri_asc !== undefined) {

			if (!hasAddedThen) {
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

function addPagination(paginationObject, extraObject) {
	console.log("[ RETURNED PANL PAGINATION JSON OBJECT ]")
	console.log(paginationObject);
	$("#page_num").append(paginationObject.page_num);
	$("#num_pages").append(paginationObject.num_pages);
	$("#num_per_page").append(paginationObject.num_per_page);


	if (paginationObject.page_uris.next) {
		$("#next").append(
				"<a href=\"" +
				panlResultsViewerUrl +
				$("#collection").text() +
				paginationObject.page_uris.next +
				"\"/>NEXT</a> &raquo;");
	} else {
		$("#next").append("NEXT &raquo;");
	}

	if (paginationObject.page_uris.previous) {
		$("#previous").append(
				"&laquo; <a href=\"" +
				panlResultsViewerUrl +
				$("#collection").text() +
				paginationObject.page_uris.previous +
				"\"/>PREV</a>");
	} else {
		$("#previous").append("&laquo; PREV");
	}

	var foundExtra = false;
	if(undefined !== extraObject) {
		// we will look into the num_per_page object
		if(extraObject.num_per_page !== undefined) {
			for (const index in extraObject.num_per_page) {

				addPerPage(paginationObject, extraObject.num_per_page[index]);
			}
			foundExtra = true;
		}
	}

	if(!foundExtra) {
		// now for the per_page_uris
		addPerPage(paginationObject, "3");
		addPerPage(paginationObject, "5");
		addPerPage(paginationObject, "10");
	}
}

function addPerPage(paginationObject, number) {
	$("#num_per_page_links").append("<a href=\"" +
			panlResultsViewerUrl +
			$("#collection").text() +
			paginationObject.num_per_page_uris.before +
			number +
			paginationObject.num_per_page_uris.after +
			"\">" + number + "</a>&nbsp;");
}

function addActiveFilters(activeObject, removeUri) {
	console.log("[ RETURNED PANL ACTIVE FACETS JSON OBJECT ]")
	console.log(activeObject);

	const active = $("#active");
	// first up the query
	if (activeObject.query !== undefined) {
		active.append("<li><strong>Query <em>(" + activeObject.query.panl_code + ")</em></strong></li>");
		active.append("<li><a href=\"" + panlResultsViewerUrl +
				$("#collection").text() +
				activeObject.query.remove_uri +
				"\"><img class=\"remove\" src=\"/webapp/static/remove.png\" title=\"Remove this facet\">&nbsp;" +
				activeObject.query.value +
				"</a></li><li><hr /></li>");
	}

	// now for the facets
	if (activeObject.facet !== undefined) {
		addActiveFacets(activeObject.facet);
	}

	// finally, the sort
	if (activeObject.sort !== undefined) {
		addActiveSorts(activeObject.sort, removeUri);
	}
}

function addActiveFacets(facets) {
	const active = $("#active");
	const checkboxes = $("#boolean-checkbox");
	// facets first
	var currentFacetName = "";

	for (const facet of facets) {
		if(facet.checkbox_value !== undefined) {
			// this is a boolean checkbox
			checkboxes.append("<li><strong>" + facet.name + " <em>(" + facet.panl_code + ")</em></strong></li>");
			currentFacetName = facet.facet_name;
			var  checkboxText = "";

			if(facet.checkbox_value) {
				checkboxText = "Only include '" + decodePanl(facet.encoded) + "'";
			} else {
				checkboxText = "Exclude '" + decodePanl(facet.inverse_encoded) + "'";
			}
			var innerUl = "<li>" +
					"<a href=\"" +
					panlResultsViewerUrl +
					$("#collection").text() +
					facet.remove_uri +
					"\"><img class=\"add\" src=\"/webapp/static/checked.png\" title=\"Remove facet\">" +
					checkboxText +
					"</a>";

			var complete = "<li class=\"heading\" id=\"facet-" +
					facet.facet_name +
					"\"><strong>" +
					facet.name +
					" <em>(" +
					facet.panl_code +
					")</em></strong> [" +
					getFacetType(facet) +
					"]<br />" +
					innerUl +
					"</li>"

			checkboxes.append(complete);

		} else {

			// these are not checkboxes that can be set.
			if (facet.facet_name !== currentFacetName) {
				active.append("<li><strong>" + facet.name + " <em>(" + facet.panl_code + ")</em></strong></li>");
				currentFacetName = facet.facet_name;
			}

			active.append("<li><a href=\"" + panlResultsViewerUrl +
					$("#collection").text() +
					facet.remove_uri +
					"\"><img class=\"remove\" src=\"/webapp/static/remove.png\" title=\"Remove this facet\"/>" +
					(facet.extra !== undefined && facet.extra.swatch === true ? "<img class='add' src='/webapp/static/swatches/" + facet.value + ".png'/>": "") +
					(facet.extra !== undefined && facet.extra.short_value === true ? facet.value : decodePanl(facet.encoded)) +
					"</a></li>");

			if (facet.is_boolean_facet) {
				active.append("<li><a href=\"" + panlResultsViewerUrl +
						$("#collection").text() +
						facet.inverse_uri +
						"\"><img class=\"invert\" src=\"/webapp/static/invert.png\" title=\"Invert this boolean facet\"/>Invert to '" +
						decodePanl(facet.inverse_encoded) +
						"'</a></li>");
			}
		}
	}
	active.append("<li><hr /></li>");
}

function addActiveSorts(sorts, removeUri) {
	let first = true;

	let content = "";

	for (const sort of sorts) {
		const activesort = $("#activesort");
		if (first) {
			first = false;
			activesort.html("<strong>Active Sorting</strong>");
			content += "<ul>";
		}

		content += "<strong>" +
				sort.name +
				" <em>(" +
				sort.panl_code +
				")</em> " +
				(sort.is_descending ? "[DESC]" : "[ASC]") +
				"</strong></li>";

		content += "<li><a href=\"" + panlResultsViewerUrl +
				$("#collection").text() +
				sort.remove_uri +
				"\"><img class=\"remove\" src=\"/webapp/static/remove.png\" title=\"Remove this sort order\"/>Remove this sorting</a><br \>" +
				"<a href=\"" + panlResultsViewerUrl +
				$("#collection").text() +
				sort.inverse_uri +
				"\"><img class=\"invert\" src=\"/webapp/static/invert.png\" title=\"Invert this sort order\"/>Change to " +
				(sort.is_descending ? "ASC" : "DESC") +
				"</a>" +
				"</li>";
	}

	if (sorts.length > 0) {
		const activesort = $("#activesort");
		content += "<li class=\"center\"><br /><a href=\"" +
				panlResultsViewerUrl +
				$("#collection").text() +
				removeUri +
				"\"><img class=\"remove\" src=\"/webapp/static/remove.png\" title=\"Clear all sorting\"/>Clear all sorting</a></li></ul>";
		activesort.after(content);
	}
}

function getActiveRangeObject(rangeFacetName, activeObject) {
	if (activeObject.facet === undefined) {
		return (undefined);
	}

	for (const activeRangeFacetObject of activeObject.facet) {
		if (activeRangeFacetObject.facet_name === rangeFacetName && activeRangeFacetObject.value_to !== undefined) {
			return (activeRangeFacetObject);
		}
	}
}

function addAvailableFilters(availableObject, activeObject) {
	console.log("[ RETURNED PANL AVAILABLE FILTERS JSON OBJECT ]")
	console.log(availableObject);

	// first up the facets
	for (const facet of availableObject.facets) {
		generateFacet(facet, activeObject);
	}

	const ranges = $("#ranges");
	// now check for the ranges - we are adding another list item so that
	// people can choose which implementation to use.
	for (const facet of availableObject.range_facets) {
		$("#ranges-marker").removeClass("hidden");

		ranges.append("<p><strong>" + facet.name + " <em>(" + facet.panl_code + ") Range</em></strong></p>");

		ranges.append("<div id=\"slider-ui-" + facet.facet_name + "\" class=\"slider-round\"></div>");

		var slider = document.getElementById("slider-ui-" + facet.facet_name);

		var inboundMinValue = parseInt(facet.min);
		var inboundMaxValue = parseInt(facet.max);

		var activeRange = getActiveRangeObject(facet.facet_name, activeObject);
		if (activeRange !== undefined) {
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
				from: function (value) {
					return (parseInt(value));
				},
				to: function (value) {
					return (parseInt(value));
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
				"</span></div><div class=\"center\"><a class=\"range-link add\" href=\"" +
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

		slider.noUiSlider.on("update", function (values, handle, unencoded, tap, positions, noUiSlider) {
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

			//
			if (facet.uris.before_min_value !== undefined && values[0] === parseInt(facet.min)) {
				generatedHrefBefore = facet.uris.before_min_value;
				if (!facet.uris.has_infix && values[0] === parseInt(facet.min)) {
					generatedHrefDuring = "~";
				}
			}

			if (facet.uris.after_max_value !== undefined && values[1] === parseInt(facet.max)) {
				generatedHrefAfter = facet.uris.after_max_value;
				if (!facet.uris.has_infix && values[0] === parseInt(facet.max)) {
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
			$("." + facet.facet_name + "-link").text(text);
		});
	}

	// now check for the date
	for (const facet of availableObject.date_range_facets) {
		$("#ranges-marker").removeClass("hidden");

		ranges.append("<p><strong>" + facet.name + " <em>(" + facet.panl_code + ") Date Range</em></strong></p>");

		ranges.append("<form method=\"GET\" id=\"date-range-" + facet.facet_name + "\">" +
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

		$("#date-range-" + facet.facet_name).on("keydown", function (event) {
			return event.key != "Enter";
		});

		updateDateRangeLink(facet);

		$("#previous_next" + facet.facet_name).on('change', {facet: facet}, function (e) {
			e.preventDefault();
			updateDateRangeLink(e.data.facet);
		});

		$("#designator" + facet.facet_name).on('change', {facet: facet}, function (e) {
			e.preventDefault();
			updateDateRangeLink(e.data.facet);
		});

		$("#date_number" + facet.facet_name).on('input', {facet: facet}, function (e) {
			e.preventDefault();
			updateDateRangeLink(e.data.facet);
		});
	}
}

function generateFacet(facet, activeFacetObject) {
	const available = $("#available");
	if(facet.checkbox_value === undefined) {
		available.append(generateFacetHTML(facet));

		if (facet.facet_limit !== -1 && facet.facet_limit <= facet.values.length) {
			// bind the update
			$("#facet-link-" + facet.facet_name).on("click", {"facet": facet}, function (event) {
				event.preventDefault();
				replaceFacetHTML(event.data.facet);
			});
		}
	} else {
		// add a boolean checkbox
		var innerUl = "<ul>";

		// if this is already selected, then we need to choose the correct icon
		var checkboxLink = "";
		var checkboxText = "";

		for (const value of facet.values) {
			if(facet.checkbox_value) {
				// we want to highlight the include 'true' value
				if(value.value === "true") {
					checkboxLink = facet.uris.before +
							value.encoded +
							facet.uris.after;
					checkboxText = value.encoded;
				}
			} else {
				// checkbox is set to false
				// we want to exclude the negative values, so only
				if(value.value === "false") {
					checkboxLink = facet.uris.before +
							value.encoded +
							facet.uris.after;
				} else {
					checkboxText = value.encoded;
				}
			}
		}

		for (const value of facet.values) {
			if(value.value === (!facet.checkbox_value) +"") {
				innerUl += "<li>" +
						"<a href=\"" +
						panlResultsViewerUrl +
						$("#collection").text() +
						checkboxLink +
						"\"><img class=\"add\" src=\"/webapp/static/unchecked.png\" title=\"Add facet\">" +
						(facet.checkbox_value ? "Only include '" : "Exclude '") + decodePanl(checkboxText) + "'</a>";
			}
		}
		innerUl = innerUl + "</ul>";

		var complete = "<li class=\"heading\" id=\"facet-" +
			facet.facet_name +
			"\"><strong>" +
			facet.name +
			" <em>(" +
			facet.panl_code +
			")</em></strong> [" +
			getFacetType(facet) +
			"]<br />" +
			innerUl +
			"</li>"

		$("#boolean-checkbox").append(complete);
	}
}

function replaceFacetHTML(facet) {
	// go off and get the more facets
	var moreFacetsUrl = "/panl-more-facets" +
			$("#collection").text() +
			$("#canonical_uri").text() +
			"?code=" + facet.panl_code +
			"&limit=-1";

	$.ajax({
		url: moreFacetsUrl,
		success: function (panlJsonData) {
			$("#facet-" + facet.facet_name).replaceWith(generateFacetHTML(panlJsonData.panl.facet));
		}
	});
}

function generateFacetHTML(facet) {
	var innerUl = "<ul>";
	for (const value of facet.values) {
		innerUl += "<li>" +
				"<a href=\"" +
				panlResultsViewerUrl +
				$("#collection").text() +
				facet.uris.before +
				((facet.value_separator !== undefined) ? value.encoded_multi : value.encoded) +
				facet.uris.after +
				"\"><img class=\"add\" src=\"/webapp/static/add.png\" title=\"Add facet\"/>" +
				(facet.extra !== undefined && facet.extra.swatch === true ? "<img class='add' src='/webapp/static/swatches/" + value.value + ".png'/>": "") +
				(
						facet.extra !== undefined && facet.extra.short_value === true ?
								value.value : (
										facet.is_multivalue && value.encoded_multi !== undefined ?
												decodePanl(value.encoded_multi) :
												decodePanl(value.encoded)
								)
				) + "</a>";

		if (!facet.is_or_facet) {
			innerUl += "&nbsp;(" + value.count + ")";
		}
		innerUl += "</li>";
	}

	// check for more facets...
	if (facet.facet_limit !== -1 && facet.facet_limit <= facet.values.length) {
		innerUl += "<li class=\"more-facets\"><a id=\"facet-link-" + facet.facet_name + "\" href=\"#\">See all...</a></li>";
	}

	innerUl += "</ul>"

	return (
			"<li class=\"heading\" id=\"facet-" +
			facet.facet_name +
			"\"><strong>" +
			facet.name +
			" <em>(" +
			facet.panl_code +
			")</em></strong> [" +
			getFacetType(facet) +
			"]<br />" +
			innerUl +
			"</li>");
}

function updateDateRangeLink(facet) {
	var facetName = facet.facet_name;

	var rangeLink = $("#anchor-date-range-" + facetName);

	var previousNext = $("#previous_next" + facetName + " option:selected").text();
	var dateNumber = $("#date_number" + facetName).val();
	var designator = $("#designator" + facetName + " option:selected").text();
	if (dateNumber === "") {
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

function addMoreLikeThis(documentsObject) {
	if(documentsObject.length === 1) {
		// we are going to attempt to do a more like this on it
		if(documentsObject[0].id) {
			var moreLikeThisUrl = "/panl-more-like-this" +
					$("#collection").text() +
					"/" +
					documentsObject[0].id;


			$.ajax({
				url: moreLikeThisUrl,
				success: function (panlJsonData) {
					console.log("[ RETURNED MORE LIKE THIS JSON OBJECT ]")
					console.log(panlJsonData);

					// now go through the documents and print the more like this...
					addMoreLikeThisDocuments(panlJsonData);
				},
				error: function(request, status, errorThrown) {
					// do nothing
				}
			});

		}
	}
}

function addMoreLikeThisDocuments(panlJsonData) {
	$("#morelikethis").append("<h1>More Like This</h1>")
	let documents;
	if (panlJsonData.response.docs !== undefined) {
		documents = panlJsonData.response.docs;
	} else {
		// for version 8 anbd below
		documents = panlJsonData.response;
	}

	for (const document of documents) {
		var innerList = "";

		for (const fieldName in document) {
			innerList += "<dt>" + panlJsonData.panl.fields[fieldName] + " (" + fieldName + ")</dt>";
			innerList += "<dd>" + document[fieldName] + "</dd>";
		}
		$("#morelikethis").append("<dl>" + innerList + "</dl>");
	}
}