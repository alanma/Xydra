package org.xydra.restless;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Caller should supply a "progressToken" string, which it can use to retrieve
 * progress information from the server
 * 
 * @author xamde
 */
public interface IMultipartFormDataHandler {

	/**
	 * Callback interface to report upload/processing progress to the server --
	 * which can relay it to clients
	 * 
	 * @author xamde
	 */
	static interface IPartProgress {

		void reportProgress(String bytesProcessedSoFar);

	}

	/**
	 * This is called for each content part
	 * 
	 * @param contentName
	 * @param fieldName
	 * @param headers
	 * @param contentType
	 * @param in
	 * @param progress
	 */
	void onContentPartStream(String fieldName, String contentName, Map<String, String> headers,
			String contentType, InputStream in, IPartProgress progress) throws IOException;

	/**
	 * @param fieldName
	 * @param contentName
	 * @param headerMap
	 * @param contentType
	 * @param value
	 * @param progress
	 */
	void onContentPartString(String fieldName, String contentName, Map<String, String> headerMap,
			String contentType, String value, IPartProgress progress) throws IOException;

	/**
	 * should do a redirect
	 * @param ctx
	 * @param progress
	 * @throws IOException
	 */
	void onEndOfRequest(IRestlessContext ctx, IPartProgress progress) throws IOException;

}
