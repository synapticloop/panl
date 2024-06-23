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

import com.synapticloop.debookeriser.book.LinkElement;
import com.synapticloop.debookeriser.book.Page;
import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import org.jsoup.*;

public class Main {
	private final File googleDocsHTMLFile;
	private String template;

	private final List<Page> pages = new ArrayList<>();
	private final Map<String, LinkElement> linkElements = new LinkedHashMap<>();

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

	public void parseGoogleHTMLFile() throws IOException {
		Document doc = Jsoup.parse(googleDocsHTMLFile);

		StringBuilder styles = new StringBuilder();
		for (Element style : doc.getElementsByTag("style")) {
			styles.append(style.outerHtml());
		}

		template = template.replace("##STYLE##", styles.toString());

		Page page = new Page("index", "book/", template);

		boolean hasRemovedFirstDiv = false;

		for (Element body : doc.getElementsByTag("body")) {
			for (Element childElement : body.children()) {
				String tagName = childElement.tag().getName();

				getAllIds(page.getPageTitle(), childElement);

				if(tagName.equals("h1")) {
					// now start a new page
					pages.add(page);

					page = new Page(childElement.text(), "book/", template);
					linkElements.put("#" + childElement.attr("id"), new LinkElement(page.getPageTitle(), childElement));
					page.addPageElement(childElement);
				} else {
					if(!hasRemovedFirstDiv && tagName.equals("div")) {
						// skip this one
					} else {
						page.addPageElement(childElement);
					}
				}
			}

			pages.add(page);
		}

		for(Page outputPage : pages) {
			outputPage.writeContent(linkElements);
		}
	}

	private void getAllIds(String pageTitle, Element element) {
		String id = element.attr("id");
		// we only want to put in heading, which will be in order, not links to headings
//		if(element.tagName().startsWith("h") && !id.isBlank()) {
//			linkElements.put("#" + id, new LinkElement(pageTitle, element));
//		}

		if(!id.isBlank()) {
			linkElements.put("#" + id, new LinkElement(pageTitle, element));
		}

		for(Element childElement : element.children()) {
			getAllIds(pageTitle, childElement);
		}
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
