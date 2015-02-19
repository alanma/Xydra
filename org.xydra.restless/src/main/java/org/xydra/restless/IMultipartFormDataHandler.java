package org.xydra.restless;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Caller should supply a parameter ({@link #PARAM_PROGRESS_TOKEN}=
 * {@value #PARAM_PROGRESS_TOKEN}), which it can use to retrieve progress
 * information from the server. This parameter should be supplied as any
 * out-of-body data (cookie,url param, query string param).
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
	static interface IProgressReporter {

		void reportProgress(String bytesProcessedSoFar);

	}

	String PARAM_PROGRESS_TOKEN = "_progressToken_";

	/**
	 * This is called for each content part within the multi-part request
	 * 
	 * @param contentName
	 * @param fieldName
	 * @param headers
	 * @param contentType
	 * @param in
	 * @param progress
	 */
	void onContentPartStream(String fieldName, String contentName, Map<String, String> headers,
			String contentType, InputStream in, IProgressReporter progress) throws IOException;

	/**
	 * This is called for each simple part within the multi-part request
	 * 
	 * @param fieldName
	 * @param contentName
	 * @param headerMap
	 * @param contentType
	 * @param value
	 * @param progress
	 */
	void onContentPartString(String fieldName, String contentName, Map<String, String> headerMap,
			String contentType, String value, IProgressReporter progress) throws IOException;

	/**
	 * This is called once at the end.
	 * 
	 * Usually should do a redirect. Could also generate directly a response.
	 * 
	 * @param ctx
	 * @param progress
	 * @throws IOException
	 */
	void onEndOfRequest(IRestlessContext ctx, IProgressReporter progress) throws IOException;

}
