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
import com.synapticloop.panl.generator.PanlGenerator;
import com.synapticloop.panl.generator.util.PropertiesMerger;
import com.synapticloop.panl.server.handler.properties.PanlProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class PanlPropertiesEditTab {
	public static final String PROPERTY_INCLUDE_COMMENTS = "include.comments";
	private PanlEditor panlEditor;

	private JList<String> listSolrURLs;
	private JScrollPane scrollPaneSolrURLs;

	private JButton buttonRemoveUrl;
	private JButton buttonEditUrl;
	private JButton buttonAddUrl;
	private JComboBox<String> comboBoxSolrJConnectors;
	private JTextArea textAreaOriginal;
	private JTextArea textAreaGenerated;

	private Vector<String> solrUrlConnectionStrings = new Vector<>();
	private Map<String, Object> formValues = new HashMap<>();


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
		optionsBox.add(getSubLabel("SolrJ client"));
		comboBoxSolrJConnectors = getDropDownList(panlProperties.getSolrjClient());
		optionsBox.add(comboBoxSolrJConnectors);
		optionsBox.add(getSubLabel("Connection strings"));


		solrUrlConnectionStrings.addAll(Arrays.asList(panlProperties.getSolrSearchServerUrl().split(",")));

		listSolrURLs = new JList<>(solrUrlConnectionStrings);
		listSolrURLs.setVisibleRowCount(3);

		listSolrURLs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		listSolrURLs.setLayoutOrientation(JList.VERTICAL);
		listSolrURLs.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("large.font")));
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
			if (returnVal == JOptionPane.OK_OPTION) {
				solrUrlConnectionStrings.remove(listSolrURLs.getSelectedIndex());
				listSolrURLs.repaint();
				panlEditor.setIsEdited(true);
				generatePreview();
			}
		});

		solrUrlsBox.add(buttonRemoveUrl);
		solrUrlsBox.add(Box.createRigidArea(new Dimension(20, 40)));
		buttonEditUrl = new JButton("Edit");
		solrUrlsBox.add(buttonEditUrl);
		buttonEditUrl.setEnabled(false);
		buttonEditUrl.addActionListener(e -> {
			String newSolrUrl = DialogHelper.showTextEntryDialog("Enter Solr URL", listSolrURLs.getSelectedValue());
			addIfValidSolrURL(newSolrUrl);
		});

		buttonAddUrl = new JButton("Add");
		buttonAddUrl.addActionListener(e -> {
			String newSolrUrl = DialogHelper.showTextEntryDialog("Enter Solr URL");
			addIfValidSolrURL(newSolrUrl);
		});

		solrUrlsBox.add(buttonAddUrl);

		buttonRemoveUrl.setEnabled(false);

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
			PROPERTY_INCLUDE_COMMENTS,
			"Whether to include comments in the output",
			true));

		optionsBox.add(Box.createRigidArea(new Dimension(10, 20)));
		optionsBox.add(Box.createVerticalGlue());

		optionsBox.add(Box.createVerticalGlue());

		mainPanel.add(optionsBox, BorderLayout.WEST);
		mainPanel.add(getPanlDotPropertiesScrollPane(panlProperties), BorderLayout.CENTER);
		mainPanel.add(getOriginalPanlDotPropertiesScrollPane(panlProperties), BorderLayout.EAST);

		return (mainPanel);
	}

	private void generatePreview() {
		// build the panl.collections keys
		StringBuilder panlCollectionsProperty = new StringBuilder();
		Map<String, List<String>> panlCollectionsMap = panlEditor.getPanlProperties().getPanlCollectionsMap();
		for (String key : panlCollectionsMap.keySet()) {
			panlCollectionsProperty
				.append(PanlProperties.PROPERTY_KEY_PREFIX_PANL_COLLECTION)
				.append(key)
				.append("=")
				.append(StringUtils.join(panlCollectionsMap.get(key), ",\\\n    "))
				.append("\n");
		}

		formValues.put("panl.collections", panlCollectionsProperty);
		formValues.put("solr.search.server.url", String.join(",", solrUrlConnectionStrings));
		formValues.put("solrj.client", comboBoxSolrJConnectors.getSelectedItem());

		textAreaGenerated.setText(
			PropertiesMerger.mergeProperties(
				PanlGenerator.TEMPLATE_LOCATION_PANL_PROPERTIES,
				formValues,
				(Boolean) formValues.get(PROPERTY_INCLUDE_COMMENTS)));
		textAreaGenerated.setCaretPosition(0);
	}

	/**
	 * <p>Add (or edit) the URL if it is valid.  This will attempt to validate
	 * the URL by checking for it starting with 'zookeepeer:http' or 'http' and will ask for confirmation if it
	 * doesn't.</p>
	 *
	 * @param newSolrUrl The Solr URL to check
	 */
	private void addIfValidSolrURL(String newSolrUrl) {
		if (null != newSolrUrl && !newSolrUrl.isBlank()) {
			boolean willAdd = true;
			if (!(newSolrUrl.startsWith("zookeeper:http")
				|| newSolrUrl.startsWith("http"))) {
				int retVal = DialogHelper.showWarning(
					"URL does not start with zookeeper:http or 'http', <br> would you still like to " +
						"add it?");
				if (retVal != JOptionPane.OK_OPTION) {
					willAdd = false;
				}
			}

			if (willAdd) {
				// removing and re-adding is better at repainting
				solrUrlConnectionStrings.remove(listSolrURLs.getSelectedValue());
				solrUrlConnectionStrings.add(newSolrUrl);
				listSolrURLs.removeAll();
				listSolrURLs.setListData(solrUrlConnectionStrings);
				panlEditor.setIsEdited(true);
				generatePreview();
			}
		}
	}

	private Box getOriginalPanlDotPropertiesScrollPane(PanlProperties panlProperties) {
		Box verticalBox = Box.createVerticalBox();
		verticalBox.setAlignmentX(1.0f);

		JLabel generatedFileLabel = getLabel("<html><p style=\"padding: 0 4 0 4;\">Original file</p></html>");
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(generatedFileLabel);

		verticalBox.add(horizontalBox);

		String text = "Could not read the " + panlEditor.getPanlDotPropertiesFile().getAbsolutePath() + " file.";
		try {
			text = FileUtils.readFileToString(panlEditor.getPanlDotPropertiesFile(), StandardCharsets.UTF_8);
		} catch (IOException ignored) {
			// do nothing, nothing we can do.
		}

		textAreaOriginal = new JTextArea(text, 30, 80);
		textAreaOriginal.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("large.font")));
		textAreaOriginal.putClientProperty("FlatLaf.styleClass", "monospaced");
		textAreaOriginal.setEditable(false);
		textAreaOriginal.setLineWrap(false);
		JScrollPane scrollPane = new JScrollPane(textAreaOriginal);
		scrollPane.setBorder(
			new CompoundBorder(BorderFactory.createEmptyBorder(0, 4, 40, 4),
				BorderFactory.createEtchedBorder())
		);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		verticalBox.add(scrollPane);
		return (verticalBox);
	}

	private void toggleButtonStates(ListSelectionModel selectionModel) {
		if (selectionModel.getSelectedItemsCount() != 1) {
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
		return (jSeparator);
	}

	private JComboBox<String> getDropDownList(String solrjClient) {
		JComboBox<String> comboBox = new JComboBox<>(SolrJConnector.AVAILABLE_SOLR_J_CONNECTORS);
		comboBox.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("large.font")));
		comboBox.setPrototypeDisplayValue("default text here");
		comboBox.setPreferredSize(new Dimension(220, 20));
		comboBox.setMaximumSize(new Dimension(220, 20));
		comboBox.setMinimumSize(new Dimension(220, 20));
		comboBox.setAlignmentX(-1.0f);
		comboBox.setSelectedItem(solrjClient);
		comboBox.addItemListener(evt -> {
			panlEditor.setIsEdited(true);
			generatePreview();
		});
		// we need to do this as we are adding items to the combo box which
		// flags this as edited
		panlEditor.setIsEdited(false);
		return (comboBox);
	}

	private Box getPanlDotPropertiesScrollPane(PanlProperties panlProperties) {
		Box verticalBox = Box.createVerticalBox();
		verticalBox.setAlignmentX(1.0f);

		JLabel generatedFileLabel = getLabel("<html><p style=\"padding: 0 4 0 4;\">Generated file</p></html>");
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(generatedFileLabel);

		verticalBox.add(horizontalBox);

		textAreaGenerated = new JTextArea("", 30, 80);
		textAreaGenerated.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("large.font")));
		textAreaGenerated.putClientProperty("FlatLaf.styleClass", "monospaced");
		textAreaGenerated.setEditable(false);
		textAreaGenerated.setLineWrap(false);
		JScrollPane scrollPane = new JScrollPane(textAreaGenerated);
		scrollPane.setBorder(
			new CompoundBorder(BorderFactory.createEmptyBorder(0, 4, 40, 4),
				BorderFactory.createEtchedBorder())
		);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		verticalBox.add(scrollPane);
		generatePreview();
		return (verticalBox);
	}

	private JLabel getLabel(String text) {
		JLabel label = new JLabel(text);
		label.putClientProperty("FlatLaf.styleClass", "h2");
		label.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));
		return (label);
	}

	private JLabel getSubLabel(String text) {
		JLabel label = new JLabel(text);
		label.putClientProperty("FlatLaf.styleClass", "h3");
		label.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));
		return (label);
	}

	private JCheckBox getCheckbox(String propertyName, String tooltip, boolean selected) {
		JCheckBox jCheckBox = new JCheckBox(propertyName);
		jCheckBox.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("large.font")));
		jCheckBox.putClientProperty("FlatLaf.styleClass", "monospaced");
		jCheckBox.setName(propertyName);
		jCheckBox.setToolTipText(tooltip);
		jCheckBox.setSelected(selected);
		formValues.put(propertyName, selected);
		jCheckBox.addItemListener(e -> {
			panlEditor.setIsEdited(true);
			formValues.put(propertyName, jCheckBox.isSelected());
			generatePreview();
		});
		return (jCheckBox);
	}

	public void saveFile() {
		try (OutputStream outputStream = Files.newOutputStream(panlEditor.getPanlDotPropertiesFile().toPath());
		     OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
		     BufferedWriter writer = new BufferedWriter(outputStreamWriter)) {
			writer.write(textAreaGenerated.getText());
			writer.flush();

		} catch (IOException e) {
			DialogHelper.showError("Could not write out the file.");
		}
	}
}
