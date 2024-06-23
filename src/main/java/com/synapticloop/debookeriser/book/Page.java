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
	private List<String> styles;
	private List<PageElement> pageElements = new ArrayList<>();
	private String previousPage = "";
	private String nextPage = "";

	public Page(String pageTitle, String baseDirectory, String template) {
		this.pageTitle = pageTitle;
		this.baseDirectory = baseDirectory;
		this.template = template;
	}

	public void addPageElement(Element element) {
		pageElements.add(new PageElement(element));
	}

	public void setStyles(List<String> styles) {
		this.styles = styles;
	}

	public void writeContent(Map<String, LinkElement> linkElements) throws IOException {
		String temp = template.replace("##CONTENT##", getPageContent(linkElements))
				.replace("##NAVIGATION##", getPageNavigation(linkElements));
		FileUtils.writeStringToFile(new File(baseDirectory + getNicePageName(pageTitle) + ".html"), temp, Charset.defaultCharset());
	}

	private String getNicePageName(String pageName) {
		return(pageName.toLowerCase().replaceAll("[^a-z0-9]", "-"));
	}

	private String getPageContent(Map<String, LinkElement> linkElements) {
		StringBuilder sb = new StringBuilder();

		for(PageElement pageElement : pageElements) {
			sb.append(pageElement.getContent(linkElements));
		}

		return(sb.toString());
	}

	private String getPageNavigation(Map<String, LinkElement> linkElements) {
		boolean foundUs = false;
		boolean hasFinished = false;
		StringBuilder sb = new StringBuilder();
		sb.append("<h2 style=\"margin: 0px 0px 0px 4px; padding: 2px;\"><a href=\"/panl/book/\">&raquo;Table of Contents</a></h2>");

		for (String key : linkElements.keySet()) {
			LinkElement linkElement = linkElements.get(key);
			if(linkElement.getTagName().equals("h1")) {
				if (linkElement.getPageTitle().equals(pageTitle)) {
					sb.append("<section class=\"current-page\">");
					foundUs = true;
				} else {
					if(foundUs) {
						hasFinished = true;
					}
					foundUs = false;
				}
			}

			String content = linkElement.getContent();
			if(foundUs) {
				if(!content.isBlank()) {
					switch(linkElement.getTagName()) {
						case "h1":
							sb.append(String.format("<h1 style=\"margin: 0px; padding: 2px;\">%s</h1>", content));
							break;
						case "h2":
							sb.append(String.format("<h2 style=\"margin: 0px 0px 0px 4px; padding: 2px;\"><a href=\"%s\">%s</a></h2>", linkElement.getLink(), content));
							break;
						case "h3":
							sb.append(String.format("<h3 style=\"margin: 0px 0px 0px 8px; padding: 2px;\"><a href=\"%s\">%s</a></h3>", linkElement.getLink(), content));
							break;
					}
				}
			} else {
				if(!content.isBlank()) {
					if (linkElement.getTagName().equals("h1")) {
						if (hasFinished) {
							sb.append("</section>");
							hasFinished = false;
						}
						sb.append(String.format("<h2 style=\"margin: 0px 0px 0px 4px; padding: 2px;\"><a href=\"%s\">&raquo;%s</a></h2>", linkElement.getLink(), content));
					}
				}
			}
		}

		return(sb.toString());
	}

	public String getPageTitle() {
		return (pageTitle);
	}
}
