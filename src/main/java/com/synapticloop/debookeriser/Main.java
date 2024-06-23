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
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.jsoup.*;

public class Main {
	private final File googleDocsHTMLFile;
	private String template;
	private Map<String, String> replacements = new HashMap<>();
	private final List<Page> pages = new ArrayList<>();
	private final Map<String, LinkElement> linkElements = new LinkedHashMap<>();

	public Main(String fileName) {
		try (BufferedReader br = new BufferedReader(new FileReader("replacements.csv", StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] split = line.split(",");
				if(split.length == 2) {
					replacements.put(split[0], split[1]);
				}
			}
		} catch (IOException ignored) {
		}

		this.googleDocsHTMLFile = new File(fileName);
	}

	public void generate() throws IOException {
		loadTemplate();
		parseGoogleHTMLFile();
	}

	public void loadTemplate() throws IOException {
		template = FileUtils.readFileToString(new File("src/main/resources/template.html"), StandardCharsets.UTF_8);
	}

	public void parseGoogleHTMLFile() throws IOException {
		String googleDocsContent = FileUtils.readFileToString(googleDocsHTMLFile, StandardCharsets.UTF_8);
		googleDocsContent = googleDocsContent.replaceAll("&rsquo;", "'");
		googleDocsContent = googleDocsContent.replaceAll("&lsquo;", "'");
		googleDocsContent = googleDocsContent.replaceAll("&trade;", "™");
		googleDocsContent = googleDocsContent.replaceAll("&ndash;", "-");
		googleDocsContent = googleDocsContent.replaceAll("&hellip;", "...");
		googleDocsContent = googleDocsContent.replaceAll("&#8617;", "↩");
		googleDocsContent = googleDocsContent.replaceAll("&reg;", "®");

		Document doc = Jsoup.parse(googleDocsContent);

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

				if (tagName.equals("h1")) {
					// now start a new page
					pages.add(page);

					page = new Page(childElement.text(), "book/", template);
					linkElements.put("#" + childElement.attr("id"), new LinkElement(page.getPageTitle(), childElement));
					page.addPageElement(childElement);
				} else {
					if (!hasRemovedFirstDiv && tagName.equals("div")) {
						// skip this one
						hasRemovedFirstDiv = true;
					} else {
						page.addPageElement(childElement);
					}
				}
			}

			pages.add(page);
		}

		for (Page outputPage : pages) {
			if (!outputPage.getPageTitle().isBlank()) {
				outputPage.writeContent(linkElements);
			}
		}
	}

	private void getAllIds(String pageTitle, Element element) {
		String id = element.attr("id");
		if (!id.isBlank()) {
			linkElements.put("#" + id, new LinkElement(pageTitle, element));
		}

		for (Element childElement : element.children()) {
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
