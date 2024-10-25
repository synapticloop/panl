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
import com.synapticloop.panl.editor.tab.CollectionURLTab;
import com.synapticloop.panl.editor.tab.NewCollectionTab;
import com.synapticloop.panl.editor.tab.PanlPropertiesEditTab;
import com.synapticloop.panl.editor.util.DialogHelper;
import com.synapticloop.panl.editor.util.Settings;
import com.synapticloop.panl.server.handler.properties.PanlProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.synapticloop.panl.editor.Constants.*;

public class PanlEditor {
	private JFrame mainWindowFrame;
	private PanlProjectLauncher panlProjectLauncher;
	private JMenu fileMenuItem;
	private JMenuItem saveMenuItem;
	private JMenuItem quitMenuItem;
	private JLabel labelEdited;


	private File panlDotPropertiesFile;
	private boolean isEdited = false;
	private List<File> collectionPropertyFiles = new ArrayList<>();
	private PanlProperties panlProperties;
	private PanlPropertiesEditTab panlPropertiesEditTab;
	private int currentTabIndex = 0;

	public PanlEditor(File panlDotPropertiesFile, PanlProjectLauncher panlProjectLauncher) throws Exception {
		this.panlDotPropertiesFile = panlDotPropertiesFile;
		this.panlProjectLauncher = panlProjectLauncher;

		Properties properties = new Properties();
		properties.load(new FileInputStream(panlDotPropertiesFile));
		this.panlProperties = new PanlProperties(properties);

		labelEdited = new JLabel("");
		labelEdited.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

	}

	public void show() {
		FlatLightLaf.setup();
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
			UIManager.put("Button.arc", 10);
		} catch (Exception ex) {
			System.err.println("Failed to initialize Flat Look and Feel");
		}

		mainWindowFrame = new JFrame("Panl Configuration Editor");
		mainWindowFrame.setIconImage(ICON_APP.getImage());

		mainWindowFrame.setResizable(false);
		mainWindowFrame.addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent e) {
				if(actionOnWindowClosing()) {
					mainWindowFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				} else {
					mainWindowFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				}
			}
		});

		mainWindowFrame.setJMenuBar(createJMenuBar(mainWindowFrame));

		JTabbedPane jTabbedPane = new JTabbedPane();
		this.panlPropertiesEditTab = new PanlPropertiesEditTab(this);
		jTabbedPane.add("{PANL} " + panlDotPropertiesFile.getName(), panlPropertiesEditTab.getJPanel());
		// now add in the all of the collections
		Map<String, List<String>> panlCollectionsMap = panlProperties.getPanlCollectionsMap();
		for (String solrCollection : panlCollectionsMap.keySet()) {
			// TODO - figure out filename and file location
			for (String collectionFileLocation : panlCollectionsMap.get(solrCollection)) {
				jTabbedPane.add("[" + solrCollection + "] " + collectionFileLocation, new CollectionURLTab(this).getJPanel());
			}

		}

		Component newCollection = NewCollectionTab.createNewCollection();

		jTabbedPane.add("[ + ]", newCollection);

		jTabbedPane.addChangeListener(e -> {
//			JTabbedPane jTabbedPaneSource = (JTabbedPane) e.getSource();


			if(jTabbedPane.getTitleAt(jTabbedPane.getSelectedIndex()).equals("[ + ]")) {
				// TODO add new collection - show a dialog
				jTabbedPane.setSelectedIndex(currentTabIndex);
			} else {
				currentTabIndex = jTabbedPane.getSelectedIndex();
			}
		});

		Box fileLabelBox = Box.createHorizontalBox();

		JLabel jLabel = new JLabel(panlDotPropertiesFile.getAbsolutePath());
		jLabel.putClientProperty( "FlatLaf.styleClass", "h3" );
		jLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		fileLabelBox.add(jLabel);
		fileLabelBox.add(Box.createHorizontalGlue());
		fileLabelBox.add(labelEdited);


		mainWindowFrame.add(fileLabelBox, BorderLayout.NORTH);

		mainWindowFrame.getContentPane().add(jTabbedPane, BorderLayout.CENTER);
		mainWindowFrame.setLocation(Settings.getSubPosition(panlDotPropertiesFile.getAbsolutePath()));

		mainWindowFrame.pack();

		mainWindowFrame.setVisible(true);

	}

	private JMenuBar createJMenuBar(JFrame jFrame) {
		JMenuBar jMenuBar = new JMenuBar();

		fileMenuItem = new JMenu("File");
		fileMenuItem.setIcon(ICON_FILE);

		saveMenuItem = new JMenuItem("Save");
		Action saveAction = new AbstractAction("Save") {
			@Override public void actionPerformed(ActionEvent e) {
				// TODO - save file
				setIsEdited(false);
				panlPropertiesEditTab.saveFile();
			}
		};

		saveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
		saveMenuItem.setAction(saveAction);
		saveMenuItem.setIcon(ICON_SAVE);


		fileMenuItem.add(saveMenuItem);

		fileMenuItem.addSeparator();

		quitMenuItem = new JMenuItem("Quit");
		Action quitAction = new AbstractAction("Quit") {
			@Override public void actionPerformed(ActionEvent e) {
				if(isEdited) {
					// show a window
				}
				mainWindowFrame.dispatchEvent(new WindowEvent(mainWindowFrame, WindowEvent.WINDOW_CLOSING));
			}
		};
		quitAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
		quitMenuItem.setAction(quitAction);
		quitMenuItem.setIcon(ICON_QUIT);

		fileMenuItem.add(quitMenuItem);

		jMenuBar.add(fileMenuItem);

		jMenuBar.add( Box.createGlue() );

		return (jMenuBar);
	}

	public void moveToFront() {
		mainWindowFrame.requestFocus();
	}

	public boolean closeEditor() {
		if(actionOnWindowClosing()) {
			mainWindowFrame.dispatchEvent(new WindowEvent(mainWindowFrame, WindowEvent.WINDOW_CLOSING));
			return(true);
		}

		return(false);
	}

	public boolean actionOnWindowClosing() {
		if(isEdited) {
			// TODO show a file save window
			int retVal = DialogHelper.showFileSaveWarning(
				"<html><h2>The file '" + panlDotPropertiesFile.getName() + "' file has edits.<br>Would you like" +
					" to save the file?</br>");
			switch (retVal) {
				case JOptionPane.YES_OPTION:
					// save the file(s)
					break;
				case JOptionPane.NO_OPTION:
					// close window
					break;
				case JOptionPane.CANCEL_OPTION:
					return(false);
			}
		}

		panlProjectLauncher.removeActiveWindow(panlDotPropertiesFile.getAbsolutePath());

		Settings.setSubPosition(panlDotPropertiesFile.getAbsolutePath(), mainWindowFrame.getX(), mainWindowFrame.getY());
		Settings.saveSettings();
		return(true);
	}

	public void setUIDisplayMode(boolean isDarkUI) {
		try {
			if (!isDarkUI) {
				UIManager.setLookAndFeel(new FlatLightLaf());
				SwingUtilities.updateComponentTreeUI(mainWindowFrame);
				fileMenuItem.setIcon(ICON_FILE);
				saveMenuItem.setIcon(ICON_SAVE);
				quitMenuItem.setIcon(ICON_QUIT);
			} else {
				UIManager.setLookAndFeel(new FlatDarkLaf());
				SwingUtilities.updateComponentTreeUI(mainWindowFrame);
				fileMenuItem.setIcon(ICON_FILE_WHITE);
				saveMenuItem.setIcon(ICON_SAVE_WHITE);
				quitMenuItem.setIcon(ICON_QUIT_WHITE);
			}
		} catch (UnsupportedLookAndFeelException ex) {
			throw new RuntimeException(ex);
		}
	}

	public PanlProperties getPanlProperties() {
		return panlProperties;
	}

	public void setIsEdited(boolean isEdited) {
		this.isEdited = isEdited;
		if(isEdited) {
			labelEdited.setText("[edited]");
		} else {
			labelEdited.setText("");
		}
	}

	public File getPanlDotPropertiesFile() {
		return panlDotPropertiesFile;
	}
}
