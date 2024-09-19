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

package com.synapticloop.panl.editor;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class PanlEditor {
	public void show() {
		FlatLightLaf.setup();
		try {
			UIManager.setLookAndFeel( new FlatLightLaf() );
		} catch( Exception ex ) {
			System.err.println( "Failed to initialize Flat Look and Feel" );
		}

		//1. Create the frame.
		JFrame frame = new JFrame("Panl Configuration Editor");
		frame.setPreferredSize(new Dimension(400, 600));
		frame.setMinimumSize(new Dimension(400, 600));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//		JButton button = new JButton("Select panl.properties file");
//		button.setPreferredSize(new Dimension(200, 40));
//
//		frame.setContentPane(button);
		frame.getContentPane().add(generateSelectFile(), BorderLayout.CENTER);
		JLabel jLabel = new JLabel("No panl.properties file found");
		jLabel.setHorizontalAlignment(SwingConstants.CENTER);
		jLabel.putClientProperty( "FlatLaf.styleClass", "h1" );

		frame.getContentPane().add(jLabel, BorderLayout.NORTH);

		frame.pack();

		frame.setVisible(true);
	}

	private JPanel generateSelectFile() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 3;
		gbc.gridheight = 10;

		gbc.gridx = 0;
		gbc.gridy = 1;



		JButton button = new JButton("Select panl.properties file");
		button.setMaximumSize(new Dimension(200, 40));
		button.setPreferredSize(new Dimension(200, 40));
		button.setMinimumSize(new Dimension(200, 40));
		gbc.gridx = 1;
		gbc.gridy = 5;
		panel.add(button, gbc);
		return(panel);
	}
}
