var panlLpsePath = [];
var panlObject = {};
var currentCollection = "";

$(document).ready(function () {
	var availableCollections = $("#available_collections");
	var currentCollectionName = "";
	for (const collectionUrl of collections) {
		const lastIndex = collectionUrl.lastIndexOf("/");
		const collectionName = collectionUrl.substring(1, lastIndex);
		const fieldSet = collectionUrl.substring(lastIndex + 1);
		if (currentCollectionName !== collectionName) {
			availableCollections.append("<a href=\"/panl-single-page-search/" + collectionName + "/\">[&nbsp;" + collectionName + "&nbsp;]</a>&nbsp;");
			currentCollectionName = collectionName;
		}
	}

	availableCollections.append("<br />")

	// test to see whether we are ready to invoke the panl search service
	let uris = window.location.pathname.split("/");
	if (uris.length >= 3) {
		// we have a collection, and field set
		let collection = uris[2];
		if (collection != "") {
			panlConfiguration(collection);
		}
	}
});

function panlConfiguration(collection) {
	currentCollection = collection;
	let panlQueryUrl = "/panl-single-page/" + collection + "/";
	$.ajax({
		url: panlQueryUrl,
		success: function (panlJsonData) {
			populatePanlConfiguration(panlJsonData);
		}
	});
}

function populatePanlConfiguration(panlJsonData) {
	console.log("[ RETURNED JSON OBJECT ]")
	console.log(panlJsonData);

	console.log("[ RETURNED PANL JSON OBJECT ]")
	console.log(panlJsonData.panl);
	panlObject = panlJsonData.panl;

	// now to go through the fields and just add them

	for (const orderedLpseFacet of panlJsonData.panl.lpse_order) {
		if (null !== orderedLpseFacet) {
			$("#searchfields").append(
					"<div class=\"searchfield\">" +
					"<p class=\"searchheading\"><strong>" +
					orderedLpseFacet.name +
					"</strong> <em>(" +
					getFacetType(orderedLpseFacet) +
					")</em></p>" +
					appendFacet(orderedLpseFacet) +
					"</div>");

			bindRange(orderedLpseFacet);
			bindDateRange(orderedLpseFacet);
			$("#" + orderedLpseFacet.facet_name).on('change', {facet: orderedLpseFacet}, function (e) {
				setLpseValue(this.value, e.data.facet);
			});

			$("input[type=radio][name=\"" + orderedLpseFacet.facet_name + "\"]").change(function () {
				setLpseValue(this.value, orderedLpseFacet);
			});

			$("input[type=checkbox][name=\"" + orderedLpseFacet.facet_name + "\"]").change(function () {
				updateLpseValue(this.value, this.checked, orderedLpseFacet);
			});
		}
	}
}

function setLpseValue(value, orderedLpseFacet) {
	let lpseOffset = panlObject.lpse_lookup[orderedLpseFacet.panl_code];
	if (value === "") {
		panlLpsePath[lpseOffset] = null;
	} else {
		let valueKey = "" + value;
		panlLpsePath[lpseOffset] = {"lpseCode": orderedLpseFacet.panl_code};
		panlLpsePath[lpseOffset][value] = value;
	}

	updateSearchLinks();
}

function updateLpseValue(value, checked, orderedLpseFacet) {
	let lpseOffset = panlObject.lpse_lookup[orderedLpseFacet.panl_code];

	if (panlLpsePath[lpseOffset] === null || panlLpsePath[lpseOffset] === undefined) {
		panlLpsePath[lpseOffset] = {"lpseCode": orderedLpseFacet.panl_code};
	}

	if (checked) {
		panlLpsePath[lpseOffset][value] = value;
	} else {
		delete panlLpsePath[lpseOffset][value];
	}

	updateSearchLinks();
}

function updateSearchLinks() {
	$("#panl-lpse-path").text("");

	let lpsePath = "/";
	let lpseCodePath = "";

	// no go through the new value and then regen the lpse path
	for (const [i, panlPathValue] of panlLpsePath.entries()) {
		if (panlPathValue !== undefined && panlPathValue !== null) {
			if (Object.keys(panlPathValue).length > 1) {
				let lpseCode = panlPathValue["lpseCode"];
				for (let key in panlPathValue) {
					if (panlPathValue.hasOwnProperty(key)) {
						if (key !== "lpseCode") {
							// at this point we need to work out ranges
							if (panlObject.lpse_order[i]) {
								var isRangeFacet = panlObject.lpse_order[i].is_range_facet;
								if (isRangeFacet) {
									var hasInfix = panlObject.lpse_order[i].uris.has_infix;
									if (hasInfix) {
										lpseCodePath = lpseCodePath + lpseCode + '-';
									} else {
										lpseCodePath = lpseCodePath + lpseCode + '+';
									}
								} else {
									lpseCodePath = lpseCodePath + lpseCode;
								}
							}

							lpsePath = lpsePath + key + "/"
						}
					}
				}
			}
		}
	}

	var fullPath = lpsePath + lpseCodePath + "/";
	if (fullPath === "//") {
		fullPath = "/";
	}
	$("#panl-lpse-path").text(fullPath);
	$("#searchbutton").attr("href", "/panl-results-viewer/" + currentCollection + "/default" + fullPath);
	$("#panl-lpse-path-searchbutton").text(fullPath);
}

function getFacetType(orderedLpseFacet) {
	if (orderedLpseFacet.is_boolean_facet) {
		return ("BOOLEAN");
	} else if (undefined !== orderedLpseFacet.or_separator) {
		return ("OR SEP");
	} else if (orderedLpseFacet.is_or_facet) {
		return ("OR");
	} else if (orderedLpseFacet.is_range_facet) {
		return ("RANGE");
	} else if (orderedLpseFacet.is_date_range_facet) {
		return ("DATE Range");
	} else {
		return ("Regular");
	}
}

function appendFacet(orderedLpseFacet) {
	if (orderedLpseFacet.is_boolean_facet) {
		// BOOLEAN facet
		return (
				"<select id=\"" + orderedLpseFacet.facet_name + "\">" +
				"<option value=\"\">[No selection]</option>" +
				"<option value=\"" + orderedLpseFacet.values[0].encoded + "\">" + decodePanl(orderedLpseFacet.values[0].encoded) + "</option>" +
				"<option value=\"" + orderedLpseFacet.values[1].encoded + "\">" + decodePanl(orderedLpseFacet.values[1].encoded) + "</option>" +
				"</select>"
		);
	} else if (orderedLpseFacet.is_or_facet) {
		// OR facet
		var returnHTML = "";
		for (const value of orderedLpseFacet.values) {
			returnHTML = returnHTML + "<input type=\"checkbox\" name=\"" + orderedLpseFacet.facet_name + "\" value=\"" + value.encoded + "\">&nbsp;" + value.value + "<br />";
		}
		return (returnHTML);
	} else if (orderedLpseFacet.is_range_facet) {
		// RANGE facet
		return (
				"<div style=\"padding-bottom: 30px;\" id=\"range-" + orderedLpseFacet.facet_name + "\">" +
				"<div id=\"slider-ui-" + orderedLpseFacet.facet_name + "\" class=\"slider-round\"></div>" +
				"<div style=\"z-index: 1000; padding-top: 80px; text-align: center;\"><a href=\"#\" id=\"range-clear-" + orderedLpseFacet.facet_name + "\">[ clear " + orderedLpseFacet.name + " range ]</a></div>" +
				"</div>"
		);
	} else if (orderedLpseFacet.is_date_range_facet) {
		// DATE Range facet
		return ("<form method=\"GET\" id=\"date-range-" + orderedLpseFacet.facet_name + "\">" +
				"	<select class=\"date-range\" name=\"previous_next\" id=\"previous_next" + orderedLpseFacet.facet_name + "\">" +
				"		<option value=\"next\">" + decodePanl(orderedLpseFacet.next) + "</option>" +
				"		<option value=\"previous\">" + decodePanl(orderedLpseFacet.previous) + "</option>" +
				"	</select>" +
				"	<label><input class=\"date_number\" id=\"date_number" + orderedLpseFacet.facet_name + "\" type=\"text\" name=\"date_number\"></label>" +
				"	<select  class=\"date-range\" name=\"designator\" id=\"designator" + orderedLpseFacet.facet_name + "\">" +
				"		<option value=\"hours\" " + (orderedLpseFacet.solr_range_designator === "HOURS" ? "selected=\"selected\"" : "") + ">" + decodePanl(orderedLpseFacet.designators.hours) + "</option>" +
				"		<option value=\"days\" " + (orderedLpseFacet.solr_range_designator === "DAYS" ? "selected=\"selected\"" : "") + ">" + decodePanl(orderedLpseFacet.designators.days) + "</option>" +
				"		<option value=\"months\" " + (orderedLpseFacet.solr_range_designator === "MONTHS" ? "selected=\"selected\"" : "") + ">" + decodePanl(orderedLpseFacet.designators.months) + "</option>" +
				"		<option value=\"years\" " + (orderedLpseFacet.solr_range_designator === "YEARS" ? "selected=\"selected\"" : "") + ">" + decodePanl(orderedLpseFacet.designators.years) + "</option>" +
				"	</select>" +
				"</form>" +
				"<div class=\"center\"><a href=\"\" class=\"range-link\" id=\"anchor-date-range-" + orderedLpseFacet.facet_name + "\"></a></div>" +
				"<div class=\"center\"><a href=\"\" class=\"range-link\" id=\"anchor-date-range-clear-" + orderedLpseFacet.facet_name + "\">[ clear " + orderedLpseFacet.name + " date range ]</a></div>");
	} else {
		// regular facet
		// go through and print all of the details
		var returnHTML = "<input type=\"radio\" name=\"" + orderedLpseFacet.facet_name + "\" value=\"\">&nbsp;<em>[No selection]</em><br />";
		for (const value of orderedLpseFacet.values) {
			returnHTML = returnHTML + "<input type=\"radio\" name=\"" + orderedLpseFacet.facet_name + "\" value=\"" + value.encoded + "\">&nbsp;" + decodePanl(value.value) + "<br />";
		}
		return (returnHTML);
	}
}

function bindRange(orderedLpseFacet) {
	if (!orderedLpseFacet.is_range_facet) {
		return;
	}

	const ranges = $("#range" + orderedLpseFacet.facet_name);

	var slider = document.getElementById("slider-ui-" + orderedLpseFacet.facet_name);

	$("#range-clear-" + orderedLpseFacet.facet_name).on('click', {facet: orderedLpseFacet}, function (e) {
		e.preventDefault();
		setLpseValue("", e.data.facet);
	});

	var inboundMinValue = parseInt(orderedLpseFacet.min);
	var inboundMaxValue = parseInt(orderedLpseFacet.max);

	var options = {
		start: [inboundMinValue, inboundMaxValue],
		connect: true,
		range: {
			'min': parseInt(orderedLpseFacet.min),
			'max': parseInt(orderedLpseFacet.max)
		},
		step: 1,
		pips: {
			mode: 'range',
			density: 10
		},
		facet: orderedLpseFacet,
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

	slider.noUiSlider.on("update", function (values, handle, unencoded, tap, positions, noUiSlider) {
		var values = values;
		var facet = noUiSlider.options.facet;

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

		const value = generatedHrefBefore + generatedHrefDuring + generatedHrefAfter;
		const valueArray = value.split("/");

		var lpseOffset = panlObject.lpse_lookup[facet.panl_code];

		panlLpsePath[lpseOffset] = {"lpseCode": facet.panl_code};

		panlLpsePath[lpseOffset][valueArray[1]] = valueArray[1];

		updateSearchLinks();
	});

	$("#range-clear-" + orderedLpseFacet.facet_name).click();

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
}

function bindDateRange(orderedLpseFacet) {
	if (!orderedLpseFacet.is_date_range_facet) {
		return;
	}

	$("#date-range-" + orderedLpseFacet.facet_name).on("keydown", function (event) {
		return event.key != "Enter";
	});

	updateDateRangeLink(orderedLpseFacet);

	$("#previous_next" + orderedLpseFacet.facet_name).on('change', {facet: orderedLpseFacet}, function (e) {
		e.preventDefault();
		updateDateRangeLink(e.data.facet);
	});

	$("#designator" + orderedLpseFacet.facet_name).on('change', {facet: orderedLpseFacet}, function (e) {
		e.preventDefault();
		updateDateRangeLink(e.data.facet);
	});

	$("#date_number" + orderedLpseFacet.facet_name).on('input', {facet: orderedLpseFacet}, function (e) {
		e.preventDefault();
		updateDateRangeLink(e.data.facet);
	});

	$("#anchor-date-range-clear-" + orderedLpseFacet.facet_name).on('click', {facet: orderedLpseFacet}, function (e) {
		e.preventDefault();
		setLpseValue("", e.data.facet);
	});

	$("#anchor-date-range-" + orderedLpseFacet.facet_name).on('click', {facet: orderedLpseFacet}, function (e) {
		e.preventDefault();
		var facetName = orderedLpseFacet.facet_name;

		var previousNext = $("#previous_next" + encodePanl(facetName) + " option:selected").text();
		var dateNumber = $("#date_number" + facetName).val();
		var designator = $("#designator" + encodePanl(facetName) + " option:selected").text();
		if (dateNumber === "") {
			return;
		}

		var text = previousNext + dateNumber + designator;

		updateDateRangeLink(text, e.data.facet);
		setLpseValue(encodePanl(text), e.data.facet);
	});

}

function encodePanl(text) {
	return (encodeURI(text.replaceAll(" ", "+")));
}

function decodePanl(text) {
	return (decodeURI(text)
			.replaceAll("+", " ")
			.replaceAll("%2B", "+")
			.replaceAll("%3A", ":"));
}
