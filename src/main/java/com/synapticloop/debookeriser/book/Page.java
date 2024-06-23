package com.synapticloop.debookeriser.book;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Page {
	private final String pageTitle;
	private final String baseDirectory;
	private final String template;
	private final List<PageElement> pageElements = new ArrayList<>();
	private String nextPage = "";
	private String previousPage = "";

	public Page(String pageTitle, String baseDirectory, String template) {
		this.pageTitle = pageTitle;
		this.baseDirectory = baseDirectory;
		this.template = template;
	}

	public void addPageElement(Element element) {
		pageElements.add(new PageElement(element));
	}

	public void writeContent(Map<String, LinkElement> linkElements) throws IOException {
		String temp = template.replace("##CONTENT##", getPageContent(linkElements))
				.replace("##NAVIGATION##", getPageNavigation(linkElements))
				.replace("##PREVIOUS_LINK##", previousPage)
				.replace("##NEXT_LINK##", nextPage);

		FileUtils.writeStringToFile(new File(baseDirectory + getNicePageName(pageTitle) + ".html"), temp, Charset.defaultCharset());
	}

	private String getNicePageName(String pageName) {
		return (pageName.toLowerCase().replaceAll("[^a-z0-9]", "-"));
	}

	private String getPageContent(Map<String, LinkElement> linkElements) {
		StringBuilder sb = new StringBuilder();

		for (PageElement pageElement : pageElements) {
			sb.append(pageElement.getContent(linkElements));
		}

		return (sb.toString());
	}

	private String getPageNavigation(Map<String, LinkElement> linkElements) {
		boolean foundUs = false;
		boolean hasFinished = false;
		StringBuilder sb = new StringBuilder();
		sb.append("<h2 style=\"margin: 0px 0px 0px 4px; padding: 2px;\"><a href=\"/panl/book/\">&raquo;Table of Contents</a></h2>");

		boolean hasPreviousPage = false;
		boolean hasNextPage = false;
		previousPage = "index.html";

		for (String key : linkElements.keySet()) {
			LinkElement linkElement = linkElements.get(key);

			if (linkElement.getTagName().equals("h1")) {
				if (pageTitle.equals("index") && !hasNextPage) {
					nextPage = String.format("<li><a href=\"%s\" class=\"button small\">Next Page &raquo; <span>(%s)</span></a></li>", linkElement.getLink(), linkElement.getPageTitle());
					hasNextPage = true;
				}

				if (linkElement.getPageTitle().equals(pageTitle)) {
					hasPreviousPage = true;
					sb.append("<section class=\"current-page\">");
					foundUs = true;
				} else {
					if (!hasNextPage) {
						nextPage = String.format("<li><a href=\"%s\" class=\"button small\">Next Page &raquo; <span>(%s)</span></a></li>", linkElement.getLink(), linkElement.getPageTitle());
					}
					if (foundUs) {
						hasFinished = true;
						hasNextPage = true;
					}
					foundUs = false;
				}
			}

			String content = linkElement.getContent();
			if (foundUs) {
				if (!content.isBlank()) {
					content = content
							.replaceAll(">", "&gt;")
							.replaceAll("<", "&lt;");

					switch (linkElement.getTagName()) {
						case "h1":
							sb.append(String.format("<h1 style=\"margin: 0px; padding: 2px;\">%s</h1>", content));
							break;
						case "h2":
							sb.append(String.format("<h2 style=\"margin: 0px 0px 0px 4px; padding: 2px;\"><a href=\"%s\">%s</a></h2>", linkElement.getLink(), content));
							break;
						case "h3":
							sb.append(String.format("<h3 style=\"margin: 0px 0px 0px 12px; padding: 2px;\"> - <a href=\"%s\">%s</a></h3>", linkElement.getLink(), content));
							break;
					}
				}
			} else {
				if (!content.isBlank()) {
					if (linkElement.getTagName().equals("h1")) {
						if (!hasPreviousPage) {
							previousPage = String.format("<li><a href=\"%s\" class=\"button small\">Previous Page &laquo; <span>(%s)</span></a></li>", linkElement.getLink(), linkElement.getPageTitle());
						}
						if (hasFinished) {
							sb.append("</section>");
							hasFinished = false;
						}
						sb.append(String.format("<h2 style=\"margin: 0px 0px 0px 4px; padding: 2px;\"><a href=\"%s\">&raquo;%s</a></h2>", linkElement.getLink(), content));
					}
				}
			}
		}

		// if we still don't have a next page - we are at the end of the book
		if(!hasNextPage) {
			nextPage = "";
		}

		if (pageTitle.equals("index")) {
			previousPage = "<li><a href=\"/panl/\" class=\"button small\">Previous Page &laquo; <span>Overview</span></a></li>";
		}

		if (previousPage.equals("index.html")) {
			previousPage = "<li><a href=\"/panl/book/\" class=\"button small\">Previous Page &laquo; <span>Table of Contents</span></a></li>";
		}

		return (sb.toString());
	}

	public String getPageTitle() {
		return (pageTitle);
	}
}
