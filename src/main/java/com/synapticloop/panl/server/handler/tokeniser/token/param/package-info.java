/**
 * <p>This package contains Parameter LPSE Tokens which are instantiated from
 * the incoming HTTP request to the Panl server.</p>
 *
 * <p>Parameter tokens do not filter the results with facets, they affect the
 * display of the results and filter the results through other means.  For
 * example:</p>
 *
 * <ul>
 *   <li>The number of rows to display</li>
 *   <li>The page number for the results</li>
 *   <li>Any passthrough parameters</li>
 *   <li>Sorting options</li>
 *   <li>The query string</li>
 *   <li>The Solr query operand</li>
 * </ul>
 *
 * @author synapticloop
 * @version 1.0
 * @since 1.0
 */
package com.synapticloop.panl.server.handler.tokeniser.token.param;