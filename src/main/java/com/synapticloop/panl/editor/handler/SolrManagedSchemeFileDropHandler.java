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

package com.synapticloop.panl.editor.handler;

import com.synapticloop.panl.editor.PanlProjectLauncher;

import javax.swing.*;
import java.io.File;

public class SolrManagedSchemeFileDropHandler extends BaseFileDropHandler {
	public SolrManagedSchemeFileDropHandler(PanlProjectLauncher panlProjectLauncher) {
		super.panlProjectLauncher = panlProjectLauncher;
	}

	@Override
	public boolean importData(TransferSupport support) {
		File file = super.importData(support, "managed-schema", "managed-schema.xml");
		if(null != file) {
			// do what we need to do.
			panlProjectLauncher.openSolrManagedSchemaFile(file);
			return(true);
		} else {
			// do nothing
			return(false);
		}
	}
}
