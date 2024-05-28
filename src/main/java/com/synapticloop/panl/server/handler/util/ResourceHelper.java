package com.synapticloop.panl.server.handler.util;

import com.synapticloop.panl.server.handler.results.PanlResultsStaticHandler;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ResourceHelper {
	public static final String URL_PANL_RESULTS_VIEWER = "/panl-results-viewer/";
	public static final String URL_PANL_RESULTS_VIEWER_SUBSET = "/panl-results-viewer";

	public static final ContentType CONTENT_TYPE_JSON = ContentType.create("application/json", "UTF-8");
	public static final ContentType CONTENT_TYPE_TEXT = ContentType.create("text/plain", "UTF-8");
	public static final ContentType CONTENT_TYPE_CSS = ContentType.create("text/css", "UTF-8");
	public static final ContentType CONTENT_TYPE_HTML = ContentType.create("text/html", "UTF-8");
	public static final ContentType CONTENT_TYPE_JS = ContentType.create("text/javascript ", "UTF-8");

	private static final Map<String, ContentType> CONTENT_TYPE_MAP = new HashMap<>();
	static {
		CONTENT_TYPE_MAP.put(".json", CONTENT_TYPE_JSON);
		CONTENT_TYPE_MAP.put(".css", CONTENT_TYPE_CSS);
		CONTENT_TYPE_MAP.put(".js", CONTENT_TYPE_JS);
		CONTENT_TYPE_MAP.put(".html", CONTENT_TYPE_HTML);
	}

	public static void serveResource(String resource, HttpResponse response) {

		InputStreamReader reader = null;
		try (InputStream resourceAsStream = PanlResultsStaticHandler.class.getResourceAsStream(resource)) {
			if (null != resourceAsStream) {
				reader = new InputStreamReader(resourceAsStream);
				response.setEntity(new StringEntity(IOUtils.toString(reader), getContentType(resource)));
				reader.close();
			} else {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("error", true);
				jsonObject.put("message", "Could not find the resource '" + resource + "'");
				response.setStatusCode(HttpStatus.SC_NOT_FOUND);
				response.setEntity(new StringEntity(jsonObject.toString(), CONTENT_TYPE_JSON));
			}
		} catch (IOException ignored) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("error", true);
			jsonObject.put("message", "Could not serve the resource '" + resource + "'");
			response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			response.setEntity(new StringEntity(jsonObject.toString(), CONTENT_TYPE_JSON));
		} finally {
			if(null != reader) {
				try {
					reader.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	private static ContentType getContentType(String resource) {
		int lastIndexOf = resource.lastIndexOf('.');
		if(lastIndexOf != -1) {
			String extension = resource.substring(lastIndexOf);
			return(CONTENT_TYPE_MAP.getOrDefault(extension, CONTENT_TYPE_TEXT));
		}
		return(CONTENT_TYPE_TEXT);
	}
}
