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
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.synapticloop.panl.Main;
import com.synapticloop.panl.editor.handler.PanlPropertiesFileDropHandler;
import com.synapticloop.panl.editor.handler.SolrManagedSchemeFileDropHandler;
import com.synapticloop.panl.editor.util.DialogHelper;
import com.synapticloop.panl.editor.util.Settings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.synapticloop.panl.editor.Constants.*;

/**
 * This is the project launcher for the Panl Editor
 */
public class PanlProjectLauncher {
	private JFrame mainWindowFrame;
	private JButton buttonPresentationMode;
	private JMenuItem quitMenuItem;
	private JButton buttonOpenFile;
	private JButton buttonRemoveRecentFile;
	private JList<String> listRecentFiles;

	private final Map<String, PanlEditor> panlEditorsMap = new HashMap<>();
	private File currentFile;
	private boolean isDarkUI = false;

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

		mainWindowFrame.addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent e) {
				actionOnWindowClosing();
			}
		});

		mainWindowFrame.setJMenuBar(createJMenuBar(mainWindowFrame));

		mainWindowFrame.getContentPane().add(createProjectPane());

		mainWindowFrame.setLocation(Settings.getMainPosition());

		mainWindowFrame.pack();
		buttonOpenFile.setPreferredSize(new Dimension(150, buttonOpenFile.getHeight()));
		mainWindowFrame.pack();

		this.isDarkUI = Settings.getIsDarkMode();
		setUIDisplayMode();

		mainWindowFrame.setVisible(true);
	}


	private JMenuBar createJMenuBar(JFrame jFrame) {
		JMenuBar jMenuBar = new JMenuBar();

		JMenu fileMenuItem = new JMenu("File");

		quitMenuItem = new JMenuItem("Quit");
		Action quitAction = new AbstractAction("Quit") {
			@Override public void actionPerformed(ActionEvent e) {
				actionOnWindowClosing();
			}
		};

		quitAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));

		quitMenuItem.setIcon(ICON_QUIT);
		quitMenuItem.setAction(quitAction);
		fileMenuItem.add(quitMenuItem);

		jMenuBar.add(fileMenuItem);

		jMenuBar.add(Box.createGlue());

		jMenuBar.add(new JLabel("       "));
		buttonPresentationMode = new JButton("Dark mode", ICON_MOON);
		jMenuBar.add(buttonPresentationMode);
		buttonPresentationMode.addActionListener(e -> {
			isDarkUI = !isDarkUI;
			setUIDisplayMode();
		});

		return (jMenuBar);
	}

	private void setUIDisplayMode() {
		try {
			if (!isDarkUI) {
				UIManager.setLookAndFeel(new FlatLightLaf());
				SwingUtilities.updateComponentTreeUI(mainWindowFrame);
				buttonPresentationMode.setText("Dark mode");
				buttonPresentationMode.setIcon(ICON_MOON);
				quitMenuItem.setIcon(ICON_QUIT);
			} else {
				UIManager.setLookAndFeel(new FlatDarkLaf());
				SwingUtilities.updateComponentTreeUI(mainWindowFrame);
				buttonPresentationMode.setText("Light mode");
				buttonPresentationMode.setIcon(ICON_SUN);
				quitMenuItem.setIcon(ICON_QUIT_WHITE);
			}
		} catch (UnsupportedLookAndFeelException ex) {
			// do nothing
		}

		for (PanlEditor panlEditor : panlEditorsMap.values()) {
			panlEditor.setUIDisplayMode(this.isDarkUI);
		}
	}

	private JPanel createProjectPane() {
		JPanel jPanel = new JPanel(new BorderLayout());

		jPanel.add(createFileList(), BorderLayout.CENTER);
		jPanel.add(createDropZones(), BorderLayout.EAST);
		jPanel.add(createFileButtons(), BorderLayout.SOUTH);

		return (jPanel);
	}

	private Box createFileButtons() {
		Box vBox = Box.createHorizontalBox();
		JLabel versionInfo = new JLabel(
			String.format(
				"<html><B>Panl version: %s<br>Solr integration version: %s</B></html>",
				Main.panlVersion,
				Main.solrVersion));
		versionInfo.setMaximumSize(new Dimension(200, 30));
		versionInfo.setMinimumSize(new Dimension(200, 30));
		vBox.add(versionInfo);

		vBox.add(Box.createHorizontalGlue());
		vBox.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
		buttonOpenFile = new JButton("Open existing file...");
		buttonOpenFile.setEnabled(true);
		buttonOpenFile.addActionListener(e -> {
			if(currentFile == null) {
				// we are going to open a file
				JFileChooser fileChooser = new JFileChooser(Settings.getLastDirectory());
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setDialogTitle("Open existing file");
				fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
				fileChooser.setAcceptAllFileFilterUsed(false);
				int retVal = fileChooser.showOpenDialog(mainWindowFrame);
				if(retVal == JFileChooser.APPROVE_OPTION) {
					currentFile = fileChooser.getSelectedFile();
					Settings.setLastDirectory(currentFile.getParentFile());
				} else {
					return;
				}
			}

			// add it to the list of open files
			File tempFile = currentFile;

			Settings.addRecentFile(currentFile);
			refreshFileListing();

			currentFile = tempFile;

			String absolutePath = currentFile.getAbsolutePath();
			if (panlEditorsMap.containsKey(absolutePath)) {
				panlEditorsMap.get(absolutePath).moveToFront();
			} else {
				PanlEditor panlEditor = null;
				try {
					panlEditor = new PanlEditor(currentFile, this);
					panlEditorsMap.put(absolutePath, panlEditor);
					panlEditor.show();
					panlEditor.setUIDisplayMode(isDarkUI);
					panlEditor.setIsEdited(false);
				} catch (Exception ex) {
					DialogHelper.showError("<html><h2>Could not open the file, message was:&nbsp;</h2>" +
						"<br>" +
						ex.getMessage() +
						"</html>");
					ex.printStackTrace();
				}
			}

			currentFile = null;
		});
		vBox.add(buttonOpenFile);
		vBox.add(Box.createHorizontalGlue());

		buttonRemoveRecentFile = new JButton("Remove selected from list");
		buttonRemoveRecentFile.setEnabled(false);
		buttonRemoveRecentFile.addActionListener(e -> {
			boolean shouldRemove = true;
			Settings.removeRecentFile(currentFile);
			String absolutePath = currentFile.getAbsolutePath();
			if (panlEditorsMap.containsKey(absolutePath)) {
				PanlEditor panlEditor = panlEditorsMap.get(absolutePath);
				panlEditor.moveToFront();
				if (!panlEditor.closeEditor()) {
					shouldRemove = false;
				}
			}

			if (shouldRemove) {
				refreshFileListing();
			}
		});

		vBox.add(buttonRemoveRecentFile);


		return (vBox);
	}

	private void refreshFileListing() {
		listRecentFiles.removeAll();
		listRecentFiles.setListData(Settings.getRecentFiles());
		listRecentFiles.repaint();
	}

	private Box createFileList() {
		Box recentFilesBox = Box.createVerticalBox();
		recentFilesBox.setBorder(new EmptyBorder(0, 12, 12, 0));

		Box vbox = Box.createHorizontalBox();
		vbox.putClientProperty(FlatClientProperties.STYLE, "margin: 16");

		JLabel jLabel = new JLabel("<html><body style=\"text-align: left; padding-right: 16; margin: 0;\">" +
			"<h2>Recent files...</h2></body></html>");
		jLabel.putClientProperty("FlatLaf.styleClass", "h2");
		jLabel.setHorizontalAlignment(SwingConstants.LEFT);
		vbox.add(jLabel);
		recentFilesBox.add(vbox);

		listRecentFiles = new JList<>(Settings.getRecentFiles());

		listRecentFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listRecentFiles.setLayoutOrientation(JList.VERTICAL);
		listRecentFiles.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("large.font")));
		ListSelectionModel selectionModel = listRecentFiles.getSelectionModel();
		selectionModel.addListSelectionListener(e -> {
			toggleOpenFileButtonState(selectionModel);
		});

		listRecentFiles.addMouseListener(new MouseListener() {
			@Override public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					buttonOpenFile.doClick();
				}
			}

			@Override public void mousePressed(MouseEvent e) {

			}

			@Override public void mouseReleased(MouseEvent e) {

			}

			@Override public void mouseEntered(MouseEvent e) {

			}

			@Override public void mouseExited(MouseEvent e) {

			}
		});

		JScrollPane scrollPaneRecentFiles = new JScrollPane(listRecentFiles);
		scrollPaneRecentFiles.putClientProperty("FlatLaf.style", "font: 200% $light.font");


		recentFilesBox.add(scrollPaneRecentFiles);
		return (recentFilesBox);
	}

	private void toggleOpenFileButtonState(ListSelectionModel selectionModel) {
		if (selectionModel.getSelectedItemsCount() != 1) {
			buttonOpenFile.setText("Open existing file...");
			this.currentFile = null;
			buttonRemoveRecentFile.setEnabled(false);
			return;
		}
		this.currentFile = new File(listRecentFiles.getSelectedValue());
		buttonRemoveRecentFile.setEnabled(true);
		buttonOpenFile.setText("Open selected file...");
	}

	private Box createDropZones() {
		Box dropZoneBox = Box.createVerticalBox();
		dropZoneBox.setBorder(new EmptyBorder(0, 12, 0, 12));
		dropZoneBox.putClientProperty(FlatClientProperties.STYLE, "margin: 16");

		JLabel quickOpen = new JLabel("<html><body style=\"text-align: left; padding-right: 0; margin: 0;\">" +
			"<h2>Quick Open...</h2></body></html>");
		quickOpen.putClientProperty("FlatLaf.styleClass", "h2");
		quickOpen.setHorizontalAlignment(SwingConstants.LEFT);

		dropZoneBox.add(quickOpen);

		JLabel labelPanlProperties = createDropLabel("Drop panl.properties file");
		labelPanlProperties.setTransferHandler(new PanlPropertiesFileDropHandler(this));

		dropZoneBox.add(labelPanlProperties);
		dropZoneBox.add(Box.createRigidArea(new Dimension(10, 10)));
		JLabel labelManagedSchema = createDropLabel("Drop Solr managed schema file");
		labelManagedSchema.setTransferHandler(new SolrManagedSchemeFileDropHandler(this));
		dropZoneBox.add(labelManagedSchema);

		return (dropZoneBox);
	}

	private JLabel createDropLabel(String text) {
		JLabel dropPanlProperties = new JLabel(
			"<html><body style=\"text-align: center; padding: 0px;\"><h2>" + text + "</h2></body></html>",
			SwingConstants.CENTER);
		dropPanlProperties.putClientProperty("FlatLaf.styleClass", "h1");
		dropPanlProperties.setPreferredSize(new Dimension(140, 140));
		dropPanlProperties.putClientProperty(FlatClientProperties.STYLE_CLASS, "dragAndDropPanel");
		return (dropPanlProperties);
	}

	private void actionOnWindowClosing() {
		Settings.setMainPosition(mainWindowFrame.getX(), mainWindowFrame.getY());
		Settings.setIsDarkMode(isDarkUI);
		Settings.saveSettings();

		// go through all of the windows
		for (PanlEditor panlEditor : panlEditorsMap.values()) {
			panlEditor.moveToFront();
			if (!panlEditor.actionOnWindowClosing()) {
				mainWindowFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				return;
			}
		}

		mainWindowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void openPanlPropertiesFile(File file) {
		PanlEditor panlEditor = null;
		try {
			panlEditor = new PanlEditor(file, this);
			panlEditorsMap.put(file.getAbsolutePath(), panlEditor);
			panlEditor.show();
			panlEditor.setUIDisplayMode(isDarkUI);

			Settings.addRecentFile(file);
			listRecentFiles.removeAll();
			listRecentFiles.setListData(Settings.getRecentFiles());
			listRecentFiles.repaint();

		} catch (Exception ex) {
			DialogHelper.showError(
				"<html>" +
					"<h2>Could not open the file, message was:&nbsp;</h2>" +
					"<br>" +
					ex.getMessage() +
					"</html>");
		}
	}

	/**
	 * <p>Open a schema file, parse it and create a new panl.properties file.</p>
	 *
	 * @param file the SolrManagedSchema file location
	 */
	public void openSolrManagedSchemaFile(File file) {
	}

	public void removeActiveWindow(String filename) {
		panlEditorsMap.remove(filename);
	}
}
