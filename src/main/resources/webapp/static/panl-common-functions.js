/*
 * Copyright (c) 2008-2025 synapticloop.
 *
 * https://github.com/synapticloop/panl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

/**
 * Return a string representation of the facet type.
 *
 * @param orderedLpseFacet The facet to interrogate for the type
 * @returns {string} The string representation of the facet type
 */
var getFacetType = function (orderedLpseFacet) {
	if (orderedLpseFacet.is_boolean_facet) {
		return ("BOOLEAN");
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
