package com.synapticloop.panl.util;

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
 * <p>This class contains the majority of constants strings that are used
 * throughout the code.</p>
 *
 * @author Synapticloop
 */
public class Constants {

	public static final String BOOLEAN_FALSE_VALUE = "false";
	public static final String BOOLEAN_TRUE_VALUE = "true";
	public static final String FORWARD_SLASH = "/";
	public static final String JSON_VALUE_NO_INFIX_REPLACEMENT = "~";

	public static final String DEFAULT_VALUE_QUERY_RESPOND_TO = "q";
	public static final int DEFAULT_VALUE_FACET_MIN_COUNT = 1;
	public static final int DEFAULT_VALUE_NUM_RESULTS_LOOKAHEAD = 5;
	public static final int DEFAULT_VALUE_NUM_RESULTS_MORELIKETHIS = 5;
	public static final int DEFAULT_VALUE_NUM_RESULTS_PER_PAGE = 10;
	public static final int DEFAULT_VALUE_SOLR_FACET_LIMIT = 100;

	public static final String DEFAULT_MLT_HANDLER = "/mlt";

	/**
	 * <p>JSON Key Constants</p>
	 */
	public static class Json {

		/**
		 * <p>JSON keys for HTTP Responses</p>
		 */
		public static class Response {
			public static final String JSON_VALUE_MESSAGE_404 = "Not found";
			public static final String JSON_VALUE_MESSAGE_500 = "Internal server error";

			public static final String ERROR = "error";
			public static final String MESSAGE = "message";
			public static final String STATUS = "status";
			public static final String VALID_URLS = "valid_urls";
		}

		/**
		 * <p>JSON keys for Solr JSON response addition/removal</p>
		 */
		public static class Solr {
			public static final String FACET_COUNTS = "facetCounts";
			public static final String PARAMS = "params";
			public static final String RESPONSE = "response";
			public static final String RESPONSE_HEADER = "responseHeader";
			public static final String STATS = "stats";
			public static final String STATS_FIELDS = "stats_fields";
		}

		/**
		 * <p>JSON keys for Panl response object</p>
		 */
		public static class Panl {
			public static final String ACTIVE = "active";
			public static final String ADD_URI_ASC = "add_uri_asc";
			public static final String ADD_URI_DESC = "add_uri_desc";
			public static final String AFTER = "after";
			public static final String AFTER_MAX_VALUE = "after_max_value";
			public static final String AND = "AND";
			public static final String AVAILABLE = "available";
			public static final String BEFORE = "before";
			public static final String BEFORE_MIN_VALUE = "before_min_value";
			public static final String BUILD_REQUEST_TIME = "panl_build_request_time";
			public static final String BUILD_RESPONSE_TIME = "panl_build_response_time";
			public static final String CANONICAL_URI = "canonical_uri";
			public static final String CHECKBOX_VALUE = "checkbox_value";
			public static final String COUNT = "count";
			public static final String DATE_RANGE_FACETS = "date_range_facets";
			public static final String DAYS = "days";
			public static final String DESIGNATOR = "designator";
			public static final String DESIGNATORS = "designators";
			public static final String DURING = "during";
			public static final String DYNAMIC_MAX = "dynamic_max";
			public static final String DYNAMIC_MIN = "dynamic_min";
			public static final String ENCODED = "encoded";
			public static final String ENCODED_MULTI = "encoded_multi";
			public static final String EXTRA = "extra";
			public static final String FACETORDER = "facetorder";
			public static final String FACETS = "facets";
			public static final String FACET_LIMIT = "facet_limit";
			public static final String FACET_NAME = "facet_name";
			public static final String FIELDS = "fields";
			public static final String HAS_INFIX = "has_infix";
			public static final String HOURS = "hours";
			public static final String INVERSE_ENCODED = "inverse_encoded";
			public static final String INVERSE_URI = "inverse_uri";
			public static final String IS_BOOLEAN_FACET = "is_boolean_facet";
			public static final String IS_DATE_FACET = "is_date_facet";
			public static final String IS_DATE_RANGE_FACET = "is_date_range_facet";
			public static final String IS_DESCENDING = "is_descending";
			public static final String IS_MULTIVALUE = "is_multivalue";
			public static final String IS_OR_FACET = "is_or_facet";
			public static final String IS_RANGE_FACET = "is_range_facet";
			public static final String IS_RANGE_FACETS = "is_range_facets";
			public static final String KEYWORD = "keyword";
			public static final String LPSE_LOOKUP = "lpse_lookup";
			public static final String LPSE_ORDER = "lpse_order";
			public static final String MAX = "max";
			public static final String MIN = "min";
			public static final String MONTHS = "months";
			public static final String NAME = "name";
			public static final String NEXT = "next";
			public static final String NUM_PAGES = "num_pages";
			public static final String NUM_PER_PAGE = "num_per_page";
			public static final String NUM_PER_PAGE_URIS = "num_per_page_uris";
			public static final String NUM_RESULTS = "num_results";
			public static final String NUM_RESULTS_EXACT = "num_results_exact";
			public static final String OR = "OR";
			public static final String PAGE_NUM = "page_num";
			public static final String PAGE_URIS = "page_uris";
			public static final String PAGINATION = "pagination";
			public static final String PANL = "panl";
			public static final String PANL_CODE = "panl_code";
			public static final String PARSE_REQUEST_TIME = "panl_parse_request_time";
			public static final String PREFIX = "prefix";
			public static final String PREVIOUS = "previous";
			public static final String PREVIOUS_NEXT = "previous_next";
			public static final String QUERY_OPERAND = "query_operand";
			public static final String QUERY_RESPOND_TO = "query_respond_to";
			public static final String RANGE_FACETS = "range_facets";
			public static final String RANGE_MAX_VALUE = "range_max_value";
			public static final String RANGE_MIN_VALUE = "range_min_value";
			public static final String REMOVE_URI = "remove_uri";
			public static final String SEARCH = "search";
			public static final String SEND_REQUEST_TIME = "panl_send_request_time";
			public static final String SET_URI_ASC = "set_uri_asc";
			public static final String SET_URI_DESC = "set_uri_desc";
			public static final String SOLR_DESIGNATOR = "solr_range_designator";
			public static final String SORTING = "sorting";
			public static final String SORT_FIELDS = "sort_fields";
			public static final String SUFFIX = "suffix";
			public static final String TIMINGS = "timings";
			public static final String TOTAL_TIME = "panl_total_time";
			public static final String TYPE = "type";
			public static final String URIS = "uris";
			public static final String VALUE = "value";
			public static final String VALUES = "values";
			public static final String VALUE_SEPARATOR = "value_separator";
			public static final String VALUE_TO = "value_to";
			public static final String YEARS = "years";
		}
	}

	/**
	 * <p>URL/Solr query parameters</p>
	 */
	public static class Parameter {

		/**
		 * <p>Parameters that are sent through to the Solr server, either as a key,
		 * the value, or a part of the key or value.</p>
		 */
		public static class Solr {
			public static final String HL = "hl";
			public static final String HL_FL = "hl.fl";
			public static final String Q_OP = "q.op";
			public static final String STATS = "stats";
			public static final String STATS_FIELD = "stats.field";

			public static final String QUERY_DESIGNATOR_DAYS = "DAYS";
			public static final String QUERY_DESIGNATOR_HOURS = "HOURS";
			public static final String QUERY_DESIGNATOR_MONTHS = "MONTHS";
			public static final String QUERY_DESIGNATOR_YEARS = "YEARS";

			// Solr Query operands that are passed through to the Solr server
			public static final String SOLR_DEFAULT_QUERY_OPERAND_OR = "OR";
			public static final String SOLR_DEFAULT_QUERY_OPERAND_AND = "AND";
		}

		/**
		 * <p>Query parameter keys that are passed through to the Panl response
		 * handlers.</p>
		 */
		public static class Panl {
			public static final String CODE = "code";
			public static final String LIMIT = "limit";
		}
	}

	/**
	 * <p>Context constants</p>
	 */
	public static class Context {

		/**
		 * <p>Context keys for the Panl server</p>
		 */
		public static class Panl {
			public static final String FACET_LIMIT = "facet_limit";
			public static final String LPSE_CODE = "lpse_code";
		}
	}

	/**
	 * <p>Property file constants which are used to lookup properties within the
	 * <code>.properties</code> files.</p>
	 *
	 * <p>The constants are split into two subclasses, the <code>Panl</code>
	 * class has all properties that start with <code>panl.</code> string and the
	 * <code>Solr</code> subclass has all constants that start with the
	 * <code>solr.</code> String.</p>
	 */
	public static class Property {

		/**
		 * <p>property keys, suffix, prefixes, or substrings that are used by the
		 * property files for picking up properties.</p>
		 */
		public static class Panl {
			public static final String PANL_BOOL = "panl.bool.";
			public static final String PANL_BOOL_CHECKBOX = "panl.bool.checkbox.";
			public static final String PANL_COLLECTION = "panl.collection.";
			public static final String PANL_COLLECTION_EXTRA = "panl.collection.extra";
			public static final String PANL_DATE = "panl.date.";
			public static final String PANL_DECIMAL_POINT = "panl.decimal.point";
			public static final String PANL_EXTRA = "panl.extra.";
			public static final String PANL_FACET = "panl.facet.";
			public static final String PANL_FACETSORT = "panl.facetsort.";
			public static final String PANL_FIELD = "panl.field.";
			public static final String PANL_FORM_QUERY_RESPONDTO = "panl.form.query.respondto";
			public static final String PANL_INCLUDE_SAME_NUMBER_FACETS = "panl.include.same.number.facets";
			public static final String PANL_INCLUDE_SINGLE_FACETS = "panl.include.single.facets";
			public static final String PANL_LPSE_FACETORDER = "panl.lpse.facetorder";
			public static final String PANL_LPSE_IGNORE = "panl.lpse.ignore";
			public static final String PANL_LPSE_LENGTH = "panl.lpse.length";
			public static final String PANL_LPSE_ORDER = "panl.lpse.order";
			public static final String PANL_MULTIVALUE = "panl.multivalue.";
			public static final String PANL_MULTIVALUE_SEPARATOR = "panl.multivalue.separator.";
			public static final String PANL_NAME = "panl.name.";
			public static final String PANL_OR_ALWAYS = "panl.or.always.";
			public static final String PANL_OR_FACET = "panl.or.facet.";
			public static final String PANL_OR_SEPARATOR = "panl.or.separator.";
			public static final String PANL_PARAM_NUMROWS = "panl.param.numrows";
			public static final String PANL_PARAM_PAGE = "panl.param.page";
			public static final String PANL_PARAM_PASSTHROUGH = "panl.param.passthrough";
			public static final String PANL_PARAM_QUERY = "panl.param.query";
			public static final String PANL_PARAM_QUERY_OPERAND = "panl.param.query.operand";
			public static final String PANL_PARAM_SORT = "panl.param.sort";
			public static final String PANL_PREFIX = "panl.prefix.";
			public static final String PANL_RANGE_FACET = "panl.range.facet.";
			public static final String PANL_RANGE_INFIX = "panl.range.infix.";
			public static final String PANL_RANGE_MAX = "panl.range.max.";
			public static final String PANL_RANGE_MAX_VALUE = "panl.range.max.value.";
			public static final String PANL_RANGE_MAX_WILDCARD = "panl.range.max.wildcard.";
			public static final String PANL_RANGE_MIN = "panl.range.min.";
			public static final String PANL_RANGE_MIN_VALUE = "panl.range.min.value.";
			public static final String PANL_RANGE_MIN_WILDCARD = "panl.range.min.wildcard.";
			public static final String PANL_RANGE_PREFIX = "panl.range.prefix.";
			public static final String PANL_RANGE_SUFFIX = "panl.range.suffix.";
			public static final String PANL_RANGE_SUPPRESS = "panl.range.suppress.";
			public static final String PANL_REMOVE_SOLR_JSON_KEYS = "panl.remove.solr.json.keys";
			public static final String PANL_RESULTS_FIELDS = "panl.results.fields.";
			public static final String PANL_RESULTS_TESTING_URLS = "panl.results.testing.urls";
			public static final String PANL_SEARCH = "panl.search.";
			public static final String PANL_SEARCH_FIELDS = "panl.search.fields";
			public static final String PANL_SERVER_EXTRA = "panl.server.extra";
			public static final String PANL_SORT_FIELDS = "panl.sort.fields";
			public static final String PANL_STATUS_404_VERBOSE = "panl.status.404.verbose";
			public static final String PANL_STATUS_500_VERBOSE = "panl.status.500.verbose";
			public static final String PANL_SUFFIX = "panl.suffix.";
			public static final String PANL_TYPE = "panl.type.";
			public static final String PANL_UNLESS = "panl.unless.";
			public static final String PANL_UNIQUEKEY = "panl.uniquekey.";
			public static final String PANL_WHEN = "panl.when.";

			public static final String SUFFIX_DAYS = ".days";
			public static final String SUFFIX_FALSE = ".false";
			public static final String SUFFIX_HOURS = ".hours";
			public static final String SUFFIX_MONTHS = ".months";
			public static final String SUFFIX_NEXT = ".next";
			public static final String SUFFIX_PREFIX = ".prefix";
			public static final String SUFFIX_PREVIOUS = ".previous";
			public static final String SUFFIX_SUFFIX = ".suffix";
			public static final String SUFFIX_TRUE = ".true";
			public static final String SUFFIX_YEARS = ".years";

			public static final String SOLR_VALUE_COUNT = "count";
			public static final String SOLR_VALUE_INDEX = "index";

			public static final String SOLRJ_CLIENT = "solrj.client";
			public static final String SOLR_SEARCH_SERVER_URL = "solr.search.server.url";

			public static final String DEFAULT_CLOUD_SOLR_CLIENT = "CloudSolrClient";
			public static final String DEFAULT_SOLR_URL = "http://localhost:8983/solr";

			public static final String PANL_MLT_ENABLE = "panl.mlt.enable";
			public static final String PANL_MLT_HANDLER = "panl.mlt.handler";

			public static final String PANL_MLT_BOOST = "panl.mlt.boost";
			public static final String PANL_MLT_FL = "panl.mlt.fl";
			public static final String PANL_MLT_INTERESTINGTERMS = "panl.mlt.interestingTerms";
			public static final String PANL_MLT_MATCH_INCLUDE = "panl.mlt.match.include";
			public static final String PANL_MLT_MATCH_OFFSET = "panl.mlt.match.offset";
			public static final String PANL_MLT_MAXDF = "panl.mlt.maxdf";
			public static final String PANL_MLT_MAXDFPCT = "panl.mlt.maxdfpct";
			public static final String PANL_MLT_MAXNTP = "panl.mlt.maxntp";
			public static final String PANL_MLT_MAXQT = "panl.mlt.maxqt";
			public static final String PANL_MLT_MAXWL = "panl.mlt.maxwl";
			public static final String PANL_MLT_MINDF = "panl.mlt.mindf";
			public static final String PANL_MLT_MINTF = "panl.mlt.mintf";
			public static final String PANL_MLT_MINWL = "panl.mlt.minwl";
			public static final String PANL_MLT_QF = "panl.mlt.qf";
		}

		/**
		 * <p>Properties that pertain to the Solr server setup</p>
		 */
		public static class Solr {
			public static final String SOLR_DEFAULT_QUERY_OPERAND = "solr.default.query.operand";
			public static final String SOLR_FACET_LIMIT = "solr.facet.limit";
			public static final String SOLR_FACET_MIN_COUNT = "solr.facet.min.count";
			public static final String SOLR_HIGHLIGHT = "solr.highlight";

			public static final String SOLR_NUMROWS_DEFAULT = "solr.numrows.default";
			public static final String SOLR_NUMROWS_LOOKAHEAD = "solr.numrows.lookahead";
			public static final String SOLR_NUMROWS_MAXIMUM = "solr.numrows.maximum";
			public static final String SOLR_NUMROWS_MORELIKETHIS = "solr.numrows.morelikethis";
		}
	}

	/**
	 * <p>Constants for Strings that are in the URL</p>
	 */
	public static class Url {
		public static class Panl {
			public static final String SORTING_OPTION_ASC = "+";
			public static final String SORTING_OPTION_DESC = "-";

			// STATIC strings for properties or property prefixes that are used to
			// look up the configuration in the <panl_collection_url>.panl.properties
			// file
			// The default fieldsets that will __ALWAYS__ be registered
			public static final String FIELDSETS_DEFAULT = "default";
			public static final String FIELDSETS_EMPTY = "empty";
		}
	}
}
