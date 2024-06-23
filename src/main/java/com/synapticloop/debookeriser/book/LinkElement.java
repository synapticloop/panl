package com.synapticloop.debookeriser.book;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;

public class LinkElement {
	private final String pageTitle;
	private final String id;
	private final String tagName;
	private final Element element;

	public LinkElement(String pageTitle, Element element) {
		this.pageTitle = pageTitle;
		this.element = element;
		this.tagName = element.tagName();

		this.id = element.attr("id");
	}

	public String getLink() {
		return(getNicePageName(pageTitle) + ".html#" + id);
	}

	private String getNicePageName(String pageTitle) {
		return pageTitle.toLowerCase().replaceAll("[^a-z0-9]", "-");
	}

	public String getPageTitle() {
		return (pageTitle);
	}

	public String getId() {
		return (id);
	}

	public String getContent() {
		return(element.text());
	}

	public String getTagName() {
		return (tagName);
	}
}
