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

package com.synapticloop.panl.editor.handler;

import com.synapticloop.panl.editor.PanlProjectLauncher;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class BaseFileDropHandler extends TransferHandler {
	protected PanlProjectLauncher panlProjectLauncher;

	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		for (DataFlavor flavor : support.getDataFlavors()) {
			if (flavor.isFlavorJavaFileListType()) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public File importData(TransferHandler.TransferSupport support, String... endsWith) {
		if (!this.canImport(support)) {
			return null;
		}

		List<File> files;
		try {
			files = (List<File>) support
				.getTransferable()
				.getTransferData(DataFlavor.javaFileListFlavor);

		} catch (UnsupportedFlavorException | IOException ex) {
			// should never happen (or JDK is buggy)
			return(null);
		}

		if (files.size() != 1) {
			showErrorMessage("Please drop only one (1) file on the editor.");
			return(null);
		} else {
			File file = files.get(0);
			if(file.isDirectory()) {
				showErrorMessage("Directories are not supported with drag and drop.");
				return(null);
			}

			boolean looksGood = false;
			for(String ending: endsWith) {
				if(file.getName().toLowerCase().endsWith(ending.toLowerCase())) {
					looksGood = true;
				}
			}

			if(!looksGood) {
				StringBuilder stringBuilder = new StringBuilder("<html><h2>The filename does not look valid" +
					".</h2><br>Expecting a name of the form:<br>");
				for(String ending: endsWith) {
					stringBuilder
						.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
						.append(ending)
						.append("<br>");
				}
				stringBuilder.append("Was:<br>");
				stringBuilder
					.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
					.append(file.getName())
					.append("<br><h2>Do you wish to continue?</h2><html>");

				int returnCode = showWarningMessage(stringBuilder.toString());
				if(returnCode == 0) {
					panlProjectLauncher.openPanlPropertiesFile(file);
				}
			} else {
				return(file);
			}
		}
		return(null);
	}

	protected void showErrorMessage(String message) {
		JOptionPane.showMessageDialog(
			new JFrame(),
			message,
			"!! ERROR !!",
			JOptionPane.ERROR_MESSAGE);
	}

	protected int showWarningMessage(String message) {
		return(JOptionPane.showConfirmDialog(
			new JFrame(),
			message,
			"!! WARNING !!",
			JOptionPane.OK_CANCEL_OPTION));
	}
}
