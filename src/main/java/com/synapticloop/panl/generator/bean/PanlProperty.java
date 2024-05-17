package com.synapticloop.panl.generator.bean;

public class PanlProperty {
	private final String panlProperty;
	private final String code;
	private final int lpseNum;
	private final String panlCode;

	public PanlProperty(String panlProperty, String code, int lpseNum) {
		this.panlProperty = panlProperty;
		this.code = code;
		this.lpseNum = lpseNum;

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < lpseNum; i ++) {
			sb.append(code);
		}
		panlCode = sb.toString();
	}

	public String getPanlCode() {
		return(panlCode);
	}

	public String toProperties() {
		return(panlProperty + "=" + panlCode);
	}
}
