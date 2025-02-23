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

package com.synapticloop.panl.indexer;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.PanlServer;
import com.synapticloop.panl.server.handler.helper.CollectionHelper;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Filesystem {
	public static void main(String[] args) {

		if(args.length != 1) {
			System.out.println("Invalid length of environment.");
			return;
		}

		File file = new File(args[0]);
		if(file.exists() && file.isDirectory()) {
			showFiles(new File[] { file });
		} else {
			System.out.println("The argument is not a directory");
		}
//		SolrClient solrClient = getSolrClient();

	}

	private static void indexFile(File file) {
		String fullFileName = file.getName();
		String fileName = null;
		String fileExtension = "[NONE]";

		int endIndex = fullFileName.lastIndexOf(".");
		if(endIndex == -1) {
			// we do not have a file extension
			fileName = fullFileName;
		} else {
			fileName = fullFileName.substring(0, endIndex);
			fileExtension = fullFileName.substring(endIndex);
		}
		long fileLengthInBytes = file.length();

		String[] tags = file.getParentFile().getAbsolutePath().split(File.separator);

	}

	public static void showFiles(File[] files) {
		for (File file : files) {
			if (file.isDirectory()) {
				System.out.println("Directory: " + file.getAbsolutePath());
				File[] newFiles = file.listFiles();
				if(null != newFiles) {
					showFiles(newFiles); // Calls same method again.
				}
			} else {
				System.out.println("File: " + file.getAbsolutePath());
			}
		}
	}
	private static SolrClient getSolrClient() {
		final List<String> solrUrls = new ArrayList<>();
		solrUrls.add("http://localhost:8983/solr");
		solrUrls.add("http://localhost:7574/solr");
		return new CloudSolrClient
			.Builder(solrUrls)
			.build();
	}
}
