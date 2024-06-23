package com.synapticloop.debookeriser.book;

import org.jsoup.nodes.Element;

import java.util.Map;

public class PageElement {
	public static final String GOOGLE_TRACKING_URL = "https://www.google.com/url?q=";
	private final Element element;

	public PageElement(Element element) {
		this.element = element;
	}

	public String getContent(Map<String, LinkElement> linkElements) {
		convertAnchors(element, linkElements);
		return (element.outerHtml());
	}

	private void convertAnchors(Element element, Map<String, LinkElement> linkElements) {
		for (Element childElement : element.children()) {
			if (childElement.tagName().equals("a")) {
				String href = childElement.attr("href");
				if (!href.startsWith("http")) {
					if (linkElements.containsKey(href)) {
						childElement.attr("href", linkElements.get(href).getLink());
					}
				} else {
					if(href.startsWith(GOOGLE_TRACKING_URL)) {
						href = href.substring(GOOGLE_TRACKING_URL.length(), href.indexOf("&"));
					}
					if(href.startsWith("https://docs.google.com")) {
						href = "#";
					}
					childElement.attr("href", href);
				}
			}
			convertAnchors(childElement, linkElements);
		}
	}
}
