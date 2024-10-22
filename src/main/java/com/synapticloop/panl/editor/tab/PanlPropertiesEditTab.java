/*
 * Copyright (c) 2008-2024 synapticloop.
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

package com.synapticloop.panl.editor.tab;

import com.synapticloop.panl.editor.PanlEditor;
import com.synapticloop.panl.generator.PanlGenerator;
import com.synapticloop.panl.generator.bean.PanlCollection;
import com.synapticloop.panl.server.handler.properties.PanlProperties;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class PanlPropertiesEditTab {
	private PanlEditor panlEditor;

	public PanlPropertiesEditTab(PanlEditor panlEditor) {
		this.panlEditor = panlEditor;
	}

	public JPanel getJPanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		Box optionsBox = Box.createVerticalBox();
		optionsBox.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));

		optionsBox.add(getLabel("Connection properties"));

		PanlProperties panlProperties = panlEditor.getPanlProperties();

		optionsBox.add(getLabel("Server options"));
		optionsBox.add(getCheckbox(
			"panl.results.testing.urls",
			"Select this to enable the in-built testing URLs",
			panlProperties.getHasPanlResultsTestingUrls()));
		optionsBox.add(getCheckbox(
			"panl.status.404.verbose",
			"Select this to enable verbose 404 error messages",
			panlProperties.getUseVerbose404Messages()));
		optionsBox.add(getCheckbox(
			"panl.status.500.verbose",
			"Select this to enable verbose 500 error messages",
			panlProperties.getUseVerbose500Messages()));
		optionsBox.add(getCheckbox(
			"panl.decimal.point",
			"Select this to use the decimal point as a separator between the integer and fractional part",
			PanlProperties.getIsDecimalPoint()));
		optionsBox.add(getLabel("Panl collections"));

		optionsBox.add(Box.createVerticalGlue());

		JTextArea textArea = new JTextArea(getGeneratedPanlProperties(panlProperties), 30, 80);
		textArea.putClientProperty("FlatLaf.styleClass", "monospaced");
		textArea.setEditable(false);
		textArea.setLineWrap(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBorder(
			new CompoundBorder(BorderFactory.createEmptyBorder(0, 4, 40, 4),
				BorderFactory.createEtchedBorder())
			);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		mainPanel.add(optionsBox, BorderLayout.WEST);
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		mainPanel.add(new JButton("hello"), BorderLayout.SOUTH);

		return(mainPanel);
	}

	private JLabel getLabel(String text) {
		JLabel label = new JLabel(text);
		label.putClientProperty( "FlatLaf.styleClass", "h2" );
		return(label);
	}

	private JCheckBox getCheckbox(String propertyName, String tooltip, boolean selected) {
		JCheckBox jCheckBox = new JCheckBox(propertyName);
		jCheckBox.putClientProperty("FlatLaf.styleClass", "monospaced");
		jCheckBox.setName(propertyName);
		jCheckBox.setToolTipText(tooltip);
		jCheckBox.setSelected(selected);
		jCheckBox.addItemListener(e -> {
			panlEditor.setIsEdited(true);
		});
		return(jCheckBox);
	}

	private String getGeneratedPanlProperties(PanlProperties panlProperties) {
		return("# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #\n" +
			"#                                              __                             #\n" +
			"#                          .-----.---.-.-----.|  |                            #\n" +
			"#                          |  _  |  _  |     ||  |                            #\n" +
			"#                          |   __|___._|__|__||__|                            #\n" +
			"#                          |__|     ... .-..                                  #\n" +
			"#                                                                             #\n" +
			"#                                ~ ~ ~ * ~ ~ ~                                #\n" +
			"#                                                                             #\n" +
			"#                PANL/SOLR SERVER CONNECTION CONFIGURATION                    #\n" +
			"#                --------- ------ ---------- -------------                    #\n" +
			"#                                                                             #\n" +
			"# This is the Panl configuration file which configures the base functionality #\n" +
			"# and defines how Panl will connect to the Solr server.                       #\n" +
			"#                                                                             #\n" +
			"# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #\n" +
			"\n" +
			"#                            Which Solr Client To Use\n" +
			"#                            ----- ---- ------ -- ---\n" +
			"# Choose the correct SolrJ client for the Solr installation that you require,\n" +
			"# by default, it is the CloudSolrClient.\n" +
			"#\n" +
			"# NOTE: What the solr.search.server.url will be will depend on the client that\n" +
			"#       you choose.  The SolrJ client __MUST__ be one of\n" +
			"#\n" +
			"#  - Http2SolrClient\n" +
			"#  - HttpJdkSolrClient\n" +
			"#  - LBHttp2SolrClient\n" +
			"#  - CloudSolrClient\n" +
			"#\n" +
			"# By default - we will be using the CloudSolrClient as it works with the\n" +
			"# example instructions for spinning up a test solr instance\n" +
			"#\n" +
			"#                                ~ ~ ~ * ~ ~ ~\n" +
			"\n" +
			"#solrj.client=Http2SolrClient\n" +
			"#solrj.client=HttpJdkSolrClient\n" +
			"#solrj.client=LBHttp2SolrClient\n" +
			"solrj.client=CloudSolrClient\n" +
			"\n" +
			"#                           Which URLs To Connect To\n" +
			"#                           ----- ---- -- ------- --\n" +
			"# Dependant on which Solr server installation you have, and consequently the\n" +
			"# SolrJ Client that is configured, this will either be a single url, or a comma\n" +
			"# separated list of URLs\n" +
			"#\n" +
			"#     solr.search.server.url - the search server URL to connect to which must\n" +
			"#         NOT include the core that it is connecting to - this will be taken\n" +
			"#         care of by the Panl request mechanism.\n" +
			"#\n" +
			"#     NOTE: that if you are using connector that has multiple URLs, then\n" +
			"#           they MUST be comma separated.\n" +
			"#\n" +
			"#     NOTE: If you are using the CloudSolrClient as a connector and you wish to\n" +
			"#           use the zookeeper URLs, then you __MUST__ prefix the URLs with\n" +
			"#\n" +
			"#             zookeeper:\n" +
			"#\n" +
			"#           The below property would then become:\n" +
			"#\n" +
			"#             solr.search.server.url=zookeeper:http://localhost:9983\n" +
			"#\n" +
			"#                                ~ ~ ~ * ~ ~ ~\n" +
			"\n" +
			"solr.search.server.url=http://localhost:8983/solr,http://localhost:7574/solr\n" +
			"\n" +
			"#                      Whether To Enable The Testing URLs\n" +
			"#                      ------- -- ------ --- ------- ----\n" +
			"# The Panl results viewer / explainer URLs, this is a simple web app which will\n" +
			"# allow you to test and explain the collections and the URLs that are\n" +
			"# generated, including fields, faceting, querying, sorting, and results.\n" +
			"#\n" +
			"# If this property does not exist, or if it is set to false, then no results\n" +
			"# viewer /explainer will be available.  You may wish to remove this property\n" +
			"# for production (or perhaps just disallow access to it).\n" +
			"#\n" +
			"# The URI paths are __ALWAYS__\n" +
			"#     /panl-results-viewer/    - for testing the queries, facets, and results\n" +
			"#     /panl-results-explainer/ - for explaining LPSE encoded URIs and describing\n" +
			"#                                the configuration for the Panl server.\n" +
			"#\n" +
			"#                                ~ ~ ~ * ~ ~ ~\n" +
			"\n" +
			"panl.results.testing.urls=true\n" +
			"\n" +
			"#                     Whether To Enable Verbose Error Messaging\n" +
			"#                     ------- -- ------ ------- ----- ---------\n" +
			"# Whether verbose messaging is turned on for the error (404 / 500) http status\n" +
			"# messages.  Verbose messaging for the 404 error will provide valid URI paths\n" +
			"# to connect to.  Verbose messaging for the 500 error will provide a\n" +
			"# stacktrace.\n" +
			"#\n" +
			"# The recommendation is to set both of these properties to false to reduce the\n" +
			"# possibility of information leakage.\n" +
			"#\n" +
			"#                                ~ ~ ~ * ~ ~ ~\n" +
			"\n" +
			"panl.status.404.verbose=true\n" +
			"panl.status.500.verbose=true\n" +
			"\n" +
			"#                            Decimal Values Format\n" +
			"#                            ------- ------ ------\n" +
			"# Whether decimal values use a decimal point or a decimal comma.  For example\n" +
			"# The number\n" +
			"#\n" +
			"#     1,234,567.89\n" +
			"#\n" +
			"# uses the decimal point '.' as the separator between the integer and the\n" +
			"# fractional part, whereas the number\n" +
			"#\n" +
			"#     1.234.567,89\n" +
			"#\n" +
			"# uses the decimal comma ',' as the separator between the integer and the\n" +
			"# fractional part.\n" +
			"#\n" +
			"# The default for the Panl server is to use the decimal point, should you wish\n" +
			"# to change this, set the property below to false to use the decimal comma\n" +
			"# character for decimal values.\n" +
			"#\n" +
			"#                                ~ ~ ~ * ~ ~ ~\n" +
			"\n" +
			"panl.decimal.point=true\n" +
			"\n" +
			"#                      Collection Property File Inclusion\n" +
			"#                      ---------- -------- ---- ---------\n" +
			"# Each property file defines a singular collection, with the associated\n" +
			"# properties and configuration.  The format of the property is:\n" +
			"#\n" +
			"#     panl.collection.<solr_collection_name>=<properties_file_location>\n" +
			"#\n" +
			"# Where:\n" +
			"#       <solr_collection_name> is the collection to query on the Solr server\n" +
			"#   <properties_file_location> is the relative location __FROM__ this file\n" +
			"#                              (i.e. the panl.properties file).  The format of\n" +
			"#                              the filename is:\n" +
			"#\n" +
			"#     <panl_collection_uri>.panl.properties\n" +
			"#\n" +
			"# Where:\n" +
			"#     <panl_collection_uri> is the base URI path that the Panl server will\n" +
			"#                           respond to\n" +
			"#\n" +
			"#     NOTE: You may have multiple <panl_collection_uri> values for each\n" +
			"#           <solr_collection_name> with different configurations\n" +
			"#\n" +
			"#                                ~ ~ ~ * ~ ~ ~\n" +
			"\n" +
			"panl.collection.book-store=book-store.panl.properties\n" +
			"\n" +
			"\n");
	}

}
