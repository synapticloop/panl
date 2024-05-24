package com.synapticloop.panl.server.handler;

import com.synapticloop.panl.server.handler.field.BaseField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LpseHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(LpseHandler.class);

	private final List<BaseField> lpseFields;
	private final int lpseNumber;

	public LpseHandler(List<BaseField> lpseFields, int lpseNumber) {
		this.lpseFields = lpseFields;
		this.lpseNumber = lpseNumber;
	}

	public void populate(String uri, String query) {
		LOGGER.info("Handling uri '{}', with query '{}'", uri, query);

		String[] searchQuery = uri.split("/");

		if(searchQuery.length > 3) {
			String encoding = searchQuery[searchQuery.length - 1];
			for (int i = 0; i < encoding.length(); i = i + lpseNumber) {
				String key = encoding.substring(i, i + lpseNumber);
				try {
					String value = searchQuery[i + 3];
					LOGGER.info("Found '{}', with value '{}'", key, value);
				} catch(ArrayIndexOutOfBoundsException ignored) {
					// don't care
				}
			}
		}

	}
}
