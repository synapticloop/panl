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

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.synapticloop.panl.editor.PanlEditor;
import com.synapticloop.panl.editor.tab.solrj.SolrJConnector;
import com.synapticloop.panl.editor.util.DialogHelper;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Vector;

public class PanlPropertiesEditTab {
	private PanlEditor panlEditor;

	private JList<String> listSolrURLs;
	private JScrollPane scrollPaneSolrURLs;
	private JButton buttonRemoveUrl;
	private JButton buttonEditUrl;
	private JButton buttonAddUrl;

	private Vector<String> solrUrlVector = new Vector<>();


	public PanlPropertiesEditTab(PanlEditor panlEditor) {
		this.panlEditor = panlEditor;
	}

	public JPanel getJPanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		Box optionsBox = Box.createVerticalBox();
		optionsBox.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));

		PanlProperties panlProperties = panlEditor.getPanlProperties();

		optionsBox.add(getLabel("Connection properties"));
		optionsBox.add(getSeparator());
		optionsBox.add(getSubLabel("SolrJ connector"));
		optionsBox.add(getDropDownList(panlProperties.getSolrjClient()));
		optionsBox.add(getSubLabel("Connection strings"));


		solrUrlVector.addAll(Arrays.asList(panlProperties.getSolrSearchServerUrl().split(",")));

		listSolrURLs = new JList<>(solrUrlVector);
		listSolrURLs.setVisibleRowCount(3);

		listSolrURLs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		listSolrURLs.setLayoutOrientation(JList.VERTICAL);
		listSolrURLs.setFont(FlatUIUtils.nonUIResource(UIManager.getFont( "large.font" )));
		ListSelectionModel selectionModel = listSolrURLs.getSelectionModel();
		selectionModel.addListSelectionListener(e -> {
			toggleButtonStates(selectionModel);
		});

		listSolrURLs.setAlignmentX(-1.0f);

		scrollPaneSolrURLs = new JScrollPane(listSolrURLs);
		scrollPaneSolrURLs.setPreferredSize(new Dimension(240, 60));
		scrollPaneSolrURLs.setMaximumSize(new Dimension(240, 120));
		scrollPaneSolrURLs.setMinimumSize(new Dimension(240, 120));
		scrollPaneSolrURLs.setAlignmentX(-1.0f);
		scrollPaneSolrURLs.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPaneSolrURLs.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		optionsBox.add(scrollPaneSolrURLs);

		// now for the buttons - delete / edit / add
		Box solrUrlsBox = Box.createHorizontalBox();
		solrUrlsBox.setAlignmentX(Box.LEFT_ALIGNMENT);

		buttonRemoveUrl = new JButton("Remove");
		buttonRemoveUrl.addActionListener(e -> {
			// show a confirmation dialog
			int returnVal = DialogHelper.showWarning("Confirm removal of this connection string?");
			if(returnVal == JOptionPane.OK_OPTION) {
				solrUrlVector.remove(listSolrURLs.getSelectedIndex());
				listSolrURLs.repaint();
				panlEditor.setIsEdited(true);
			}
		});

		solrUrlsBox.add(buttonRemoveUrl);
		solrUrlsBox.add(Box.createRigidArea(new Dimension(20, 40)));
		buttonEditUrl = new JButton("Edit");
		solrUrlsBox.add(buttonEditUrl);
		buttonAddUrl = new JButton("Add");
		buttonAddUrl.addActionListener(e -> {
			String newSolrUrl = DialogHelper.showTextEntryDialog("Enter Solr URL");
			if(null != newSolrUrl && !newSolrUrl.isBlank()) {
				boolean willAdd = true;
				if(!newSolrUrl.startsWith("zookeekeeper:http")
					|| !newSolrUrl.startsWith("http")) {
					int retVal = DialogHelper.showWarning(
						"URL does not start with zookeeper:http or 'http', <br> would you still like to " +
							"add it?");
					if(retVal != JOptionPane.OK_OPTION) {
						willAdd = false;
					}
				}

				if(willAdd) {
					// removing and re-adding is better at repainting
					solrUrlVector.add(newSolrUrl);
					listSolrURLs.removeAll();
					listSolrURLs.setListData(solrUrlVector);
					panlEditor.setIsEdited(true);
				}
			}
		});

		solrUrlsBox.add(buttonAddUrl);

		buttonRemoveUrl.setEnabled(false);
		buttonEditUrl.setEnabled(false);

		optionsBox.add(solrUrlsBox);


		optionsBox.add(getLabel("Server options"));
		optionsBox.add(getSeparator());
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

		optionsBox.add(getLabel("Output options"));
		optionsBox.add(getSeparator());
		optionsBox.add(getCheckbox(
			"include.comments",
			"Whether to include comments in the output",
			true));

		optionsBox.add(Box.createRigidArea(new Dimension(10, 20)));
		optionsBox.add(Box.createVerticalGlue());
		optionsBox.add(new JButton("Generate preview"));
		optionsBox.add(Box.createVerticalGlue());

		mainPanel.add(optionsBox, BorderLayout.WEST);
		mainPanel.add(getPanlDotPropertiesScrollPane(panlProperties), BorderLayout.CENTER);
		mainPanel.add(getOriginalPanlDotPropertiesScrollPane(panlProperties), BorderLayout.EAST);

		return(mainPanel);
	}

	private Box getOriginalPanlDotPropertiesScrollPane(PanlProperties panlProperties) {
		Box verticalBox = Box.createVerticalBox();
		verticalBox.setAlignmentX(1.0f);

		JLabel generatedFileLabel = getLabel("<html><p style=\"padding: 0 4 0 4;\">Original file</p></html>");
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(generatedFileLabel);

		verticalBox.add(horizontalBox);

		String text= "Could not read the " + panlEditor.getPanlDotPropertiesFile().getAbsolutePath() + " file.";
		try {
			text = FileUtils.readFileToString(panlEditor.getPanlDotPropertiesFile(), StandardCharsets.UTF_8);
		} catch (IOException ignored) {
			// do nothing, nothing we can do.
		}
		JTextArea textArea = new JTextArea(text, 30, 80);
		textArea.setFont(FlatUIUtils.nonUIResource(UIManager.getFont( "large.font" )));
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
		verticalBox.add(scrollPane);
		return(verticalBox);
	}

	private void toggleButtonStates(ListSelectionModel selectionModel) {
		if(selectionModel.getSelectedItemsCount() != 1) {
			buttonRemoveUrl.setEnabled(false);
			buttonEditUrl.setEnabled(false);
		} else {
			buttonRemoveUrl.setEnabled(true);
			buttonEditUrl.setEnabled(true);
		}
	}

	private static JSeparator getSeparator() {
		JSeparator jSeparator = new JSeparator(JSeparator.HORIZONTAL);
		jSeparator.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		jSeparator.setMaximumSize(new Dimension(280, 8));
		jSeparator.setAlignmentX(0.0f);
		return(jSeparator);
	}

	private JComboBox<String> getDropDownList(String solrjClient) {
		JComboBox<String> comboBox = new JComboBox<>(SolrJConnector.AVAILABLE_SOLR_J_CONNECTORS);
		comboBox.setFont(FlatUIUtils.nonUIResource(UIManager.getFont( "large.font" )));
		comboBox.setPrototypeDisplayValue("default text here");
		comboBox.setPreferredSize(new Dimension(220, 20));
		comboBox.setMaximumSize(new Dimension(220, 20));
		comboBox.setMinimumSize(new Dimension(220, 20));
		comboBox.setAlignmentX(-1.0f);
		comboBox.setSelectedItem(solrjClient);
		comboBox.addPropertyChangeListener(evt -> panlEditor.setIsEdited(true));
		// we need to do this as we are adding items to the combo box which
		// flags this as edited
		panlEditor.setIsEdited(false);
		return(comboBox);
	}

	private Box getPanlDotPropertiesScrollPane(PanlProperties panlProperties) {
		Box verticalBox = Box.createVerticalBox();
		verticalBox.setAlignmentX(1.0f);

		JLabel generatedFileLabel = getLabel("<html><p style=\"padding: 0 4 0 4;\">Generated file</p></html>");
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(generatedFileLabel);

		verticalBox.add(horizontalBox);

		JTextArea textArea = new JTextArea(getGeneratedPanlProperties(panlProperties), 30, 80);
		textArea.setFont(FlatUIUtils.nonUIResource(UIManager.getFont( "large.font" )));
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
		verticalBox.add(scrollPane);
		return(verticalBox);
	}

	private JLabel getLabel(String text) {
		JLabel label = new JLabel(text);
		label.putClientProperty( "FlatLaf.styleClass", "h2" );
		label.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));
		return(label);
	}
	private JLabel getSubLabel(String text) {
		JLabel label = new JLabel(text);
		label.putClientProperty( "FlatLaf.styleClass", "h3" );
		label.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));
		return(label);
	}

	private JCheckBox getCheckbox(String propertyName, String tooltip, boolean selected) {
		JCheckBox jCheckBox = new JCheckBox(propertyName);
		jCheckBox.setFont(FlatUIUtils.nonUIResource(UIManager.getFont( "large.font" )));
		jCheckBox.putClientProperty("FlatLaf.styleClass", "monospaced");
		jCheckBox.setName(propertyName);
		jCheckBox.setToolTipText(tooltip);
		jCheckBox.setSelected(selected);
		jCheckBox.addItemListener(e -> panlEditor.setIsEdited(true));
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
