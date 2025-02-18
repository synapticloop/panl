/**
 * Return a string representation of the facet type.
 *
 * @param orderedLpseFacet The facet to interrogate for the type
 * @returns {string} The string representation of the facet type
 */
var getFacetType = function (orderedLpseFacet) {
	if (orderedLpseFacet.is_boolean_facet) {
		if(orderedLpseFacet.checkbox_value !== undefined) {
			return ("BOOLEAN Checkbox (" + orderedLpseFacet.checkbox_value +")");
		} else {
			return ("BOOLEAN");
		}

	} else if (orderedLpseFacet.is_or_facet) {
		if (undefined !== orderedLpseFacet.value_separator) {
			return ("OR SEP");
		} else {
			return ("OR");
		}
	} else if (orderedLpseFacet.is_range_facet) {
		return ("RANGE");
	} else if (orderedLpseFacet.is_date_range_facet) {
		return ("DATE Range");
	} else {
		if (orderedLpseFacet.is_multivalue) {
			if(orderedLpseFacet.value_separator !== undefined) {
				return ("REGULAR - Multi SEP");
			} else {
				return ("REGULAR - Multi");
			}
		} else {
			return ("REGULAR");
		}
	}
}

var encodePanl = function(text) {
	return (encodeURI(
			text
					.replaceAll(" ", "+")
					.replaceAll("%", "%25")));
}

var decodePanl = function(text) {
	return (decodeURI(text)
			.replaceAll("+", " ")
			.replaceAll("%2B", "+")
			.replaceAll("%3A", ":")
			.replaceAll("%2F", "/"));
}

export { getFacetType, encodePanl, decodePanl };
