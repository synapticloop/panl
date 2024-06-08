package com.synapticloop.panl;

public class FacetCountBean {
	private final String name;
	private final long[] counts;

	public FacetCountBean(String name, long[] counts) {
		this.name = name;
		this.counts = counts;
	}

	public String getName() {
		return name;
	}

	public long[] getCounts() {
		return counts;
	}
}
