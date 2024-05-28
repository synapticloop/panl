package com.synapticloop.panl.server.handler.results.util;

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

/**
 * <p>A helper class for serving up assets from the classpath.  Used by the
 * panl result viewer (and helpers) in-built servlet.</p>
 *
 * @author synapticloop
 */
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

	/**
	 * <p>Serve a resourcePath from the class loader.</p>
	 *
	 * <p><strong> NOTE:</strong> that this is not supposed to be performant.</p>
	 *
	 * @param resourcePath the path to the resource
	 * @param response The response object to write to
	 */
	public static void serveResource(String resourcePath, HttpResponse response) {

		InputStreamReader reader = null;
		try (InputStream resourceAsStream = PanlResultsStaticHandler.class.getResourceAsStream(resourcePath)) {
			if (null != resourceAsStream) {
				reader = new InputStreamReader(resourceAsStream);
				response.setEntity(new StringEntity(IOUtils.toString(reader), getContentType(resourcePath)));
				reader.close();
			} else {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("error", true);
				jsonObject.put("message", "Could not find the resourcePath '" + resourcePath + "'");
				response.setStatusCode(HttpStatus.SC_NOT_FOUND);
				response.setEntity(new StringEntity(jsonObject.toString(), CONTENT_TYPE_JSON));
			}
		} catch (IOException ignored) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("error", true);
			jsonObject.put("message", "Could not serve the resourcePath '" + resourcePath + "'");
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

	/**
	 * <p>Simple get the content type from the extension helper method.  Just a
	 * quick lookup on the extension of the file.</p>
	 *
	 * <p>There is a limited number of content types that this will work on, with
	 * the default for unknown extensions being 'text/plain'.</p>
	 *
	 * @param resource The resource to look up the extension for
	 *
	 * @return The content type for the resource
	 */
	private static ContentType getContentType(String resource) {
		int lastIndexOf = resource.lastIndexOf('.');
		if(lastIndexOf != -1) {
			String extension = resource.substring(lastIndexOf);
			return(CONTENT_TYPE_MAP.getOrDefault(extension, CONTENT_TYPE_TEXT));
		}
		return(CONTENT_TYPE_TEXT);
	}
}
