package com.synapticloop.panl.editor;

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

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.synapticloop.panl.editor.tab.NewCollectionTab;
import com.synapticloop.panl.editor.tab.PanlPropertiesEditTab;
import com.synapticloop.panl.editor.tab.PanlPropertiesNewTab;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static com.synapticloop.panl.editor.Constants.*;

public class PanlEditor {

	private boolean isDarkUI = false;

	public void show() {
		FlatLightLaf.setup();
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
			UIManager.put("Button.arc", 10);
		} catch (Exception ex) {
			System.err.println("Failed to initialize Flat Look and Feel");
		}


		JFrame mainWindowFrame = new JFrame("Panl Configuration Editor");
		mainWindowFrame.setIconImage(ICON_APP.getImage());

		mainWindowFrame.setPreferredSize(new Dimension(800, 600));
		mainWindowFrame.setMinimumSize(new Dimension(800, 600));
		mainWindowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mainWindowFrame.setJMenuBar(createJMenuBar(mainWindowFrame));

		JTabbedPane jTabbedPane = new JTabbedPane();
		jTabbedPane.add("panl", PanlPropertiesEditTab.getJPanel(null));
		Component newCollection = NewCollectionTab.createNewCollection();
		jTabbedPane.add("[ + ]", newCollection);
		jTabbedPane.setEnabledAt(1, false);

		jTabbedPane.addChangeListener(e -> {
			JTabbedPane selectedTab = (JTabbedPane) e.getSource();
			System.out.println(selectedTab.getTitleAt(selectedTab.getSelectedIndex()));
		});

		mainWindowFrame.getContentPane().add(jTabbedPane, BorderLayout.NORTH);

		mainWindowFrame.pack();

		mainWindowFrame.setVisible(true);
	}

	private JMenuBar createJMenuBar(JFrame jFrame) {
		JMenuBar jMenuBar = new JMenuBar();

		JMenu fileMenuItem = new JMenu("File");
		fileMenuItem.setIcon(ICON_FILE);

		JMenuItem recentsMenuItem = new JMenuItem("Recent files...");
		recentsMenuItem.setIcon(ICON_RECENT);

		fileMenuItem.add(recentsMenuItem);
		fileMenuItem.addSeparator();

		JMenuItem saveMenuItem = new JMenuItem("Save");
		saveMenuItem.setIcon(ICON_SAVE);
		fileMenuItem.add(saveMenuItem);

		fileMenuItem.addSeparator();

		JMenuItem exitMenuItem = new JMenuItem("Quit");
		exitMenuItem.setIcon(ICON_QUIT);
		fileMenuItem.add(exitMenuItem);

		jMenuBar.add(fileMenuItem);

		jMenuBar.add( Box.createGlue() );

		jMenuBar.add(new JLabel("       "));
		JButton uiButton = new JButton("Dark mode", ICON_MOON);
		jMenuBar.add(uiButton);
		uiButton.addActionListener(e -> {
			try {
				if (isDarkUI) {
					UIManager.setLookAndFeel(new FlatLightLaf());
					SwingUtilities.updateComponentTreeUI(jFrame);
					uiButton.setText("Dark mode");
					uiButton.setIcon(ICON_MOON);
					fileMenuItem.setIcon(ICON_FILE);
					saveMenuItem.setIcon(ICON_SAVE);
					exitMenuItem.setIcon(ICON_QUIT);
					recentsMenuItem.setIcon(ICON_RECENT);

					isDarkUI = false;
				} else {
					UIManager.setLookAndFeel(new FlatDarkLaf());
					SwingUtilities.updateComponentTreeUI(jFrame);
					uiButton.setText("Light mode");
					uiButton.setIcon(ICON_SUN);
					fileMenuItem.setIcon(ICON_FILE_WHITE);
					saveMenuItem.setIcon(ICON_SAVE_WHITE);
					exitMenuItem.setIcon(ICON_QUIT_WHITE);
					recentsMenuItem.setIcon(ICON_RECENT_WHITE);

					isDarkUI = true;
				}
			} catch (UnsupportedLookAndFeelException ex) {
				throw new RuntimeException(ex);
			}
		});

		return (jMenuBar);
	}
}
