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

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class PanlPropertiesEditTab {
	private static int gridy = 0;
	public static JPanel getJPanel(File panlPropertiesFile) {
		JPanel jPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.ipadx = 10;
		gbc.ipady = 7;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(5, 10, 0, 10);

		addLabel(jPanel, gbc, "Output properties");
		addBooleanProperty(
			jPanel,
			gbc,
			"Include comments?",
			"Select this to include comments in the generated panl.properties file");

		addLabel(jPanel, gbc, "Connection properties");

		addLabel(jPanel, gbc, "Server options");

		addBooleanProperty(
			jPanel,
			gbc,
			"panl.results.testing.urls",
			"Select this to enable the in-built testing URLs");
		addBooleanProperty(jPanel, gbc, "panl.status.404.verbose", "Select this to enable verbose 404 error messages");
		addBooleanProperty(jPanel, gbc, "panl.status.500.verbose", "Select this to enable verbose 500 error messages");
		addBooleanProperty(jPanel, gbc, "panl.decimal.point", "Select this to use the decimal point as a separator " +
			"between the integer and fractional part");

		addLabel(jPanel, gbc, "Panl collections");


		return(jPanel);
	}

	private static void addLabel(JPanel jPanel, GridBagConstraints gbc, String label) {
		gbc.gridwidth = 4;
		gbc.gridx = 0;
		gbc.gridy = gridy;
		JLabel serverPropertiesLabel = new JLabel(label);
		serverPropertiesLabel.putClientProperty( "FlatLaf.styleClass", "h2" );
		jPanel.add(serverPropertiesLabel, gbc);
		gbc.gridwidth = 1;
		gridy++;
	}

	private static void addHelpTextLabel(JPanel jPanel, GridBagConstraints gbc, String label) {
		gbc.gridwidth = 4;
		gbc.gridx = 0;
		gbc.gridy = gridy;
		JLabel serverPropertiesLabel = new JLabel(label);
		serverPropertiesLabel.putClientProperty( "FlatLaf.styleClass", "light" );
		jPanel.add(serverPropertiesLabel, gbc);
		gbc.gridwidth = 1;
		gridy++;
	}

	private static void addBooleanProperty(
		JPanel jPanel,
		GridBagConstraints gbc,
		String propertyName,
		String help) {

		gbc.weightx = 0;
		gbc.gridx = 0;
		gbc.gridy = gridy;

		JCheckBox jCheckBox = new JCheckBox(propertyName);
		jCheckBox.putClientProperty("FlatLaf.styleClass", "monospaced");
		jCheckBox.setName(propertyName);
		jCheckBox.setToolTipText(help);
		jPanel.add(jCheckBox, gbc);
		gbc.gridx = 1;
		gbc.weightx = 100;
		JLabel filler = new JLabel();
		jPanel.add(filler, gbc);
		gbc.weightx = 0;
		gridy++;
	}
}
