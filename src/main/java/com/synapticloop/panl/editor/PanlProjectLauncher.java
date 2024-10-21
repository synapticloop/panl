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

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.synapticloop.panl.editor.util.Settings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static com.synapticloop.panl.editor.Constants.*;

public class PanlProjectLauncher implements ActionListener {
	public static final EmptyBorder EMPTY_BORDER = new EmptyBorder(12, 12, 12, 12);
	private boolean isDarkUI = false;
	private JFrame mainWindowFrame;

	private JButton presentationModeButton;
	private JMenuItem exitMenuItem;

	public void show() {
		Settings.loadSettings();
		FlatLightLaf.setup();
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
			UIManager.put("Button.arc", 10);
			UIManager.put("[style]Label.dragAndDropPanel",
				"[light]background: tint(@background,50%);" +
					"[dark]background: shade(@background,15%);" +
					"[light]border: 16,16,16,16,shade(@background,10%),,8;" +
					"[dark]border: 16,16,16,16,tint(@background,10%),,8");
		} catch (Exception ex) {
			System.err.println("Failed to initialize Flat Look and Feel");
		}

		mainWindowFrame = new JFrame("Panl Project Launcher");
		mainWindowFrame.setIconImage(ICON_APP.getImage());

		mainWindowFrame.setPreferredSize(new Dimension(800, 480));
		mainWindowFrame.setMinimumSize(new Dimension(800, 480));
		mainWindowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mainWindowFrame.addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent e) {
				actionOnWindowClosing();
				super.windowClosing(e);
			}
		}) ;

		mainWindowFrame.setJMenuBar(createJMenuBar(mainWindowFrame));

		mainWindowFrame.getContentPane().add(createProjectPane());

		mainWindowFrame.pack();

		this.isDarkUI = Settings.getIsDarkMode();
		setPresentationMode();

		mainWindowFrame.setVisible(true);
	}


	private JMenuBar createJMenuBar(JFrame jFrame) {
		JMenuBar jMenuBar = new JMenuBar();

		JMenu fileMenuItem = new JMenu("File");

		exitMenuItem = new JMenuItem("Quit");
		exitMenuItem.setIcon(ICON_QUIT);
		exitMenuItem.addActionListener(this);
		fileMenuItem.add(exitMenuItem);

		jMenuBar.add(fileMenuItem);

		jMenuBar.add(Box.createGlue());

		jMenuBar.add(new JLabel("       "));
		presentationModeButton = new JButton("Dark mode", ICON_MOON);
		jMenuBar.add(presentationModeButton);
		presentationModeButton.addActionListener(e -> {
			isDarkUI = !isDarkUI;
			setPresentationMode();

		});

		return (jMenuBar);
	}

	private void setPresentationMode() {
		try {
			if (!isDarkUI) {
				UIManager.setLookAndFeel(new FlatLightLaf());
				SwingUtilities.updateComponentTreeUI(mainWindowFrame);
				presentationModeButton.setText("Dark mode");
				presentationModeButton.setIcon(ICON_MOON);
				exitMenuItem.setIcon(ICON_QUIT);
			} else {
				UIManager.setLookAndFeel(new FlatDarkLaf());
				SwingUtilities.updateComponentTreeUI(mainWindowFrame);
				presentationModeButton.setText("Light mode");
				presentationModeButton.setIcon(ICON_SUN);
				exitMenuItem.setIcon(ICON_QUIT_WHITE);
			}
		} catch (UnsupportedLookAndFeelException ex) {
			throw new RuntimeException(ex);
		}
	}

	private JPanel createProjectPane() {
		JPanel jPanel = new JPanel(new BorderLayout());


		jPanel.add(createFileList(), BorderLayout.CENTER);
		jPanel.add(createDropZones(), BorderLayout.EAST);
		jPanel.add(createOpenFile(), BorderLayout.SOUTH);

		return (jPanel);
	}

	private Box createOpenFile() {
		Box vBox = Box.createHorizontalBox();
		vBox.add(Box.createHorizontalGlue());
		vBox.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
		JButton openFile = new JButton("Open file");
		openFile.setEnabled(false);

		vBox.add(openFile);
		vBox.add(Box.createHorizontalGlue());
		return(vBox);
	}

	private Box createFileList() {
		Box scrollPaneBox = Box.createVerticalBox();
		scrollPaneBox.setBorder(new EmptyBorder(0, 12, 12, 0));

		Box vbox = Box.createHorizontalBox();
		vbox.putClientProperty(FlatClientProperties.STYLE, "margin: 16");

		JLabel jLabel = new JLabel("<html><body style=\"text-align: left; padding-right: 16; margin: 0;\"><h2>Recent" +
			" " +
			"files" +
			"." +
			".." +
			"</h2></body></html>");
		jLabel.putClientProperty( "FlatLaf.styleClass", "h2" );
		jLabel.setHorizontalAlignment(SwingConstants.LEFT);
		vbox.add(jLabel);
		scrollPaneBox.add(vbox);

		JScrollPane jScrollPane = new JScrollPane();

		scrollPaneBox.add(jScrollPane);
		return(scrollPaneBox);
	}

	private Box createDropZones() {
		Box labelBox = Box.createVerticalBox();
		labelBox.setBorder(new EmptyBorder(50, 12, 0, 12));
		labelBox.putClientProperty(FlatClientProperties.STYLE, "margin: 16");

		labelBox.add(createDropLabel("Drop panl.properties file"));
		labelBox.add(Box.createRigidArea(new Dimension(10, 10)));
		labelBox.add(createDropLabel("Drop Solr managed schema file"));

		return(labelBox);
	}

	private JLabel createDropLabel(String text) {
		JLabel dropPanlProperties = new JLabel(
			"<html><body style=\"text-align: center; padding: 0px;\"><h2>" + text + "</h2></body></html>",
			SwingConstants.CENTER);
		dropPanlProperties.putClientProperty( "FlatLaf.styleClass", "h1" );
		dropPanlProperties.setPreferredSize(new Dimension(140, 140));
		dropPanlProperties.putClientProperty(FlatClientProperties.STYLE_CLASS, "dragAndDropPanel");
		return(dropPanlProperties);
	}

	@Override	public void actionPerformed(ActionEvent actionEvent) {
		if(actionEvent.getActionCommand().equals("Quit")) {
			actionOnWindowClosing();
			mainWindowFrame.dispatchEvent(new WindowEvent(mainWindowFrame, WindowEvent.WINDOW_CLOSING));
		}
	}

	private void actionOnWindowClosing() {
		Settings.setIsDarkMode(isDarkUI);
		Settings.saveSettings();
	}
}
