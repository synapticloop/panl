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

public class PanlPropertiesNewTab {

	public static final Dimension JBUTTON_DEFALT_DIMENSIONS = new Dimension(200, 40);
	public static final Dimension JBUTTON_SMALL_DIMENSIONS = new Dimension(120, 40);

	public static JPanel createSelectPanlProperties(JFrame jFrame) {
		JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.ipadx = 40;
		gbc.insets = new Insets(30, 10, 10, 10);


		JButton newButton = new JButton("Create new panl.properties file");
		newButton.setMaximumSize(JBUTTON_DEFALT_DIMENSIONS);
		newButton.setPreferredSize(JBUTTON_DEFALT_DIMENSIONS);
		newButton.setMinimumSize(JBUTTON_DEFALT_DIMENSIONS);
		gbc.gridx = 1;
		gbc.gridy = 0;
		panel.add(newButton, gbc);
		newButton.addActionListener(e -> {
			JFileChooser jFileChooser = new JFileChooser();
			int returnCode = jFileChooser.showSaveDialog(panel);
			if(returnCode == JFileChooser.APPROVE_OPTION) {
				File file = jFileChooser.getSelectedFile();
				System.out.println(file.exists());
				if(file.exists()) {
					Object[] options = {"Overwrite",
						"Load",
						"Cancel"};
					int n = JOptionPane.showOptionDialog(jFrame,
						"The file already exists. What would you like to do?",
						"File action confirmation",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[2]);
				}
				System.out.println(file.getAbsolutePath());
			}
		});


		JButton selectButton = new JButton("Load panl.properties file");
		selectButton.setMaximumSize(JBUTTON_DEFALT_DIMENSIONS);
		selectButton.setPreferredSize(JBUTTON_DEFALT_DIMENSIONS);
		selectButton.setMinimumSize(JBUTTON_DEFALT_DIMENSIONS);
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(selectButton, gbc);
		selectButton.addActionListener(e -> {

			JFileChooser jFileChooser = new JFileChooser();
			int returnCode = jFileChooser.showOpenDialog(panel);
			if(returnCode == JFileChooser.APPROVE_OPTION) {
				File file = jFileChooser.getSelectedFile();
				System.out.println(file.getAbsolutePath());
			}
		});

		return(panel);
	}
}
