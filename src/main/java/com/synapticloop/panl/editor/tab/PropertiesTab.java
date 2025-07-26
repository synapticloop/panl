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

package com.synapticloop.panl.editor.tab;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.synapticloop.panl.editor.PanlEditor;
import com.synapticloop.panl.editor.util.Settings;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public abstract class PropertiesTab {
	public static final Dimension DEFAULT_DIMENSIONS = new Dimension(200, 26);
	public static final Dimension MEDIUM_DIMENSIONS = new Dimension(90, 26);
	public static final Dimension SMALL_DIMENSIONS = new Dimension(45, 26);
	public static final String PROPERTY_INCLUDE_COMMENTS = "include.comments";
	public static final String FLAT_LAF_STYLE_CLASS = "FlatLaf.styleClass";

	protected final PanlEditor panlEditor;

	protected final Map<String, Object> formValues = new HashMap<>();

	protected PropertiesTab(PanlEditor panlEditor) {
		this.panlEditor = panlEditor;
	}


	protected JCheckBox getCheckbox(String propertyName, String tooltip, boolean selected, JButton buttonSaveFile) {
		JCheckBox jCheckBox = new JCheckBox(propertyName);
		jCheckBox.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("large.font")));
		jCheckBox.putClientProperty(FLAT_LAF_STYLE_CLASS, "monospaced");
		jCheckBox.setName(propertyName);
		jCheckBox.setToolTipText(tooltip);
		jCheckBox.setSelected(selected);
		formValues.put(propertyName, selected);
		jCheckBox.addItemListener(e -> {
			panlEditor.setIsEdited(true, buttonSaveFile);
			formValues.put(propertyName, jCheckBox.isSelected());
			if(jCheckBox.getName().equals(PROPERTY_INCLUDE_COMMENTS)) {
				Settings.setIncludeComments(panlEditor.getPanlDotPropertiesFile(), jCheckBox.isSelected());
			}
			generatePreview();
		});
		return (jCheckBox);
	}

	protected static JSeparator getHorizontalSeparator() {
		JSeparator jSeparator = new JSeparator(JSeparator.HORIZONTAL);
		jSeparator.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		jSeparator.setMaximumSize(new Dimension(280, 8));
		jSeparator.setAlignmentX(0.0f);
		return (jSeparator);
	}

	protected static JSeparator getVerticalSeparator() {
		JSeparator jSeparator = new JSeparator(JSeparator.VERTICAL);
		jSeparator.setBackground(Color.BLACK);
		jSeparator.setForeground(Color.BLACK);
		jSeparator.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		jSeparator.setMaximumSize(new Dimension(12, 60));
		jSeparator.setAlignmentX(0.0f);
		return (jSeparator);
	}

	protected JLabel getLabel(String text) {
		JLabel label = new JLabel(text);
		label.putClientProperty(FLAT_LAF_STYLE_CLASS, "h2");
		label.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));
		return (label);
	}

	protected JLabel getSubLabel(String text) {
		JLabel label = new JLabel(text);
		label.putClientProperty(FLAT_LAF_STYLE_CLASS, "h3");
		label.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));
		return (label);
	}

	protected JLabel getSubLabelMono(String text) {
		JLabel label = new JLabel(text);
		label.putClientProperty(FLAT_LAF_STYLE_CLASS, "monospaced");
		label.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));
		return (label);
	}

	protected JComboBox<String> getDropDownList(String currentValue, Vector<String> values) {
		JComboBox<String> comboBox = new JComboBox<>(values);
		comboBox.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("large.font")));
		comboBox.setPreferredSize(DEFAULT_DIMENSIONS);
		comboBox.setMaximumSize(DEFAULT_DIMENSIONS);
		comboBox.setMinimumSize(DEFAULT_DIMENSIONS);
		comboBox.setAlignmentX(-1.0f);
		comboBox.setSelectedItem(currentValue);
		comboBox.addItemListener(evt -> {
			panlEditor.setIsEdited(true);
			generatePreview();
		});
		// we need to do this as we are adding items to the combo box which
		// flags this as edited
		panlEditor.setIsEdited(false);
		return (comboBox);
	}

	protected Box getDropDownList(String parameter, String currentValue, Vector<String> values) {
		Box horizontalBox = Box.createHorizontalBox();
		JComboBox<String> comboBox = new JComboBox<>(values);
		comboBox.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("large.font")));
		comboBox.setPreferredSize(SMALL_DIMENSIONS);
		comboBox.setMaximumSize(SMALL_DIMENSIONS);
		comboBox.setMinimumSize(SMALL_DIMENSIONS);
		comboBox.setAlignmentX(-1.0f);
		comboBox.setSelectedItem(currentValue);
		comboBox.addItemListener(evt -> {
			panlEditor.setIsEdited(true);
			generatePreview();
		});
		// we need to do this as we are adding items to the combo box which
		// flags this as edited
		panlEditor.setIsEdited(false);
		horizontalBox.add(comboBox);
		horizontalBox.add(Box.createRigidArea(new Dimension(10, 10)));
		horizontalBox.add(getSubLabelMono(parameter));
		horizontalBox.add(Box.createHorizontalGlue());
		horizontalBox.setAlignmentX(-1.0f);
		return (horizontalBox);
	}

	protected Box getTextArea(String parameter, String currentValue, String text) {
		return(getTextArea(parameter, currentValue, text, 10));
	}

	protected Box getTextArea(String parameter, String currentValue, String text, int numColumns) {
		Box horizontalBox = Box.createHorizontalBox();
		TextField textField = new TextField(currentValue, numColumns);
		textField.setMaximumSize(MEDIUM_DIMENSIONS);
		textField.setMinimumSize(MEDIUM_DIMENSIONS);
		horizontalBox.add(textField);
		horizontalBox.add(Box.createRigidArea(new Dimension(10, 10)));
		horizontalBox.add(getSubLabelMono(text));
		horizontalBox.add(Box.createHorizontalGlue());
		horizontalBox.setAlignmentX(-1.0f);
		return(horizontalBox);
	}


	protected abstract void generatePreview();
}
