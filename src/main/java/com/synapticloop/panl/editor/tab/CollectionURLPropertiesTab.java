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

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Vector;

public class CollectionURLPropertiesTab extends PropertiesTab {
	private final File collectionUrlPropertiesFile;
	private final Vector<String> alphaProperties = new Vector<>(
		Arrays.asList("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".split("(?<=\\G.{1})")));
	private final Vector<String> plusMinus = new Vector<>(Arrays.asList(new String[] { "+", "-"}));

	public CollectionURLPropertiesTab(PanlEditor panlEditor, File collectionUrlPropertiesFile) {
		super(panlEditor);
		this.collectionUrlPropertiesFile = collectionUrlPropertiesFile;

	}

	public JPanel getJPanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		Box optionsBox = Box.createVerticalBox();
		optionsBox.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));

		optionsBox.add(getLabel("Collection metadata"));
		optionsBox.add(getHorizontalSeparator());
		optionsBox.add(getSubLabel("Panl LPSE length"));
		JSlider jSlider = new JSlider(1, 2, 1);
		jSlider.setPaintLabels(true);
		jSlider.setPaintTicks(true);
		jSlider.setMajorTickSpacing(1);
		jSlider.setSnapToTicks(true);
		jSlider.setMaximumSize(DEFAULT_DIMENSIONS);
		jSlider.setMinimumSize(DEFAULT_DIMENSIONS);
		jSlider.setAlignmentX(-1.0f);
		optionsBox.add(jSlider);

		optionsBox.add(getSubLabel("Panl params"));
		optionsBox.add(getDropDownList("panl.param.query", "a", alphaProperties));
		optionsBox.add(getDropDownList("panl.param.sort", "a", alphaProperties));

		optionsBox.add(Box.createRigidArea(new Dimension(1, 12)));

		Box pageHorizontalBox = Box.createHorizontalBox();
		pageHorizontalBox.add(getVerticalSeparator());
		pageHorizontalBox.add(Box.createRigidArea(new Dimension(8, 12)));
		pageHorizontalBox.setAlignmentX(-1.0f);
		Box pageVerticalBox = Box.createVerticalBox();
		pageVerticalBox.add(getTextArea("panl.param.page.suffix", "suffix", "suffix"));
		pageVerticalBox.add(getDropDownList("panl.param.page", "a", alphaProperties));
		pageVerticalBox.add(getTextArea("panl.param.page.prefix", "prefix", "prefix"));
		pageHorizontalBox.add(pageVerticalBox);
		pageHorizontalBox.add(Box.createHorizontalGlue());
		optionsBox.add(pageHorizontalBox);

		optionsBox.add(Box.createRigidArea(new Dimension(1, 12)));

		Box numrowsHorizontalBox = Box.createHorizontalBox();
		numrowsHorizontalBox.add(getVerticalSeparator());
		numrowsHorizontalBox.add(Box.createRigidArea(new Dimension(8, 12)));
		numrowsHorizontalBox.setAlignmentX(-1.0f);
		Box numrowsVerticalBox = Box.createVerticalBox();
		numrowsVerticalBox.add(getTextArea("panl.param.numrows.prefix", "prefix", "prefix"));
		numrowsVerticalBox.add(getDropDownList("panl.param.numrows", "a", alphaProperties));
		numrowsVerticalBox.add(getTextArea("panl.param.numrows.suffix", "suffix", "suffix"));
		numrowsHorizontalBox.add(numrowsVerticalBox);
		numrowsHorizontalBox.add(Box.createHorizontalGlue());
		optionsBox.add(numrowsHorizontalBox);

		optionsBox.add(Box.createRigidArea(new Dimension(1, 12)));

		optionsBox.add(getDropDownList("panl.param.query.operand", "a", alphaProperties));

		optionsBox.add(Box.createRigidArea(new Dimension(1, 12)));

		Box canonicalHorizontalBox = Box.createHorizontalBox();
		canonicalHorizontalBox.add(getVerticalSeparator());
		canonicalHorizontalBox.add(Box.createRigidArea(new Dimension(8, 12)));
		canonicalHorizontalBox.setAlignmentX(-1.0f);

		Box canonicalVerticalBox = Box.createVerticalBox();
		canonicalVerticalBox.setAlignmentX(-1.0f);

		canonicalVerticalBox.add(getDropDownList("panl.param.passthrough", "a", alphaProperties));
		canonicalVerticalBox.add(
			getCheckbox(
			"panl.param.passthrough.canonical",
				"Whether to display the passthrough parameter in the canonical URL",
				true
			));
		canonicalHorizontalBox.add(Box.createHorizontalGlue());
		canonicalHorizontalBox.add(canonicalVerticalBox);
		optionsBox.add(canonicalHorizontalBox);

		optionsBox.add(Box.createRigidArea(new Dimension(1, 12)));

		optionsBox.add(getDropDownList("panl.form.query.respondto", "a", alphaProperties));
		optionsBox.add(Box.createRigidArea(new Dimension(1, 12)));

		optionsBox.add(
			getCheckbox(
				"panl.include.single.facets",
				"Whether to include facets with only a single value",
				true
			));

		optionsBox.add(
			getCheckbox(
				"panl.include.same.number.facets",
				"Whether to include facets with have the same count as the number of documents",
				true
			));
		optionsBox.add(Box.createRigidArea(new Dimension(1, 12)));

		optionsBox.add(getSubLabel("Query options"));
		optionsBox.add(getDropDownList("solr.default.query.operand", "-", plusMinus));

		optionsBox.add(getTextArea("solr.facet.limit", "100", "solr.facet.limit", 3));
		optionsBox.add(getTextArea("solr.facet.min.count", "100", "solr.facet.min.count", 3));
		optionsBox.add(getTextArea("solr.numrows.default", "100", "solr.numrows.default", 3));


		optionsBox.add(getCheckbox(
			"solr.highlight",
			"Whether to include the highlight results from the Solr server",
			true));

		optionsBox.add(getLabel("Output options"));
		optionsBox.add(getHorizontalSeparator());
		optionsBox.add(getCheckbox(
			PROPERTY_INCLUDE_COMMENTS,
			"Whether to include comments in the output",
			true));

		mainPanel.add(optionsBox, BorderLayout.WEST);

		return(mainPanel);
	}

	@Override protected void generatePreview() {

	}
}
