package com.synapticloop.debookeriser;

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
 *  IN THE SOFTWARE.
 */

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jsoup.*;

public class Main {
	private final File googleDocsHTMLFile;
	private String template;
	public Main(String fileName) {
		this.googleDocsHTMLFile = new File(fileName);
	}

	public void generate() throws IOException {
		loadTemplate();
		parseGoogleHTMLFile();
	}

	public void loadTemplate() throws IOException {
		template = FileUtils.readFileToString(new File("src/main/resources/template.html"), Charset.defaultCharset());
	}

	private Map<String, String> pages = new LinkedHashMap<>();
	public void parseGoogleHTMLFile() throws IOException {
		Document doc = Jsoup.parse(googleDocsHTMLFile);
		boolean hasFirstPage = false;
		String outputPage = "book/index.html";

		StringBuilder styles = new StringBuilder();
		for (Element style : doc.getElementsByTag("style")) {
			styles.append(style.outerHtml());
		}
		template = template.replace("##STYLE##", styles.toString());

		StringBuilder contents = new StringBuilder();
		for (Element body : doc.getElementsByTag("body")) {
			for (Element allElement : body.getAllElements()) {
				if(allElement.tag().getName().equals("h1")) {
					// todo get nicer page name
					String temp = template.replace("##CONTENT##", contents.toString());
					FileUtils.writeStringToFile(new File(outputPage), temp, Charset.defaultCharset());
					outputPage = "book/" + getNicePageName(allElement.text()) +".html";
					System.out.println(outputPage);
					contents.setLength(0);
				} else {
					contents.append(allElement.outerHtml());
				}
			}
		}
	}
	private String getNicePageName(String pageName) {
		return(pageName.toLowerCase().replaceAll("[^a-z0-9]", "-"));
	}

	public static void main(String[] args) {
		Main main = new Main(args[0]);
		try {
			main.generate();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
