package org.xydra.xgae.gaeutils;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.Base64;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.repackaged.org.apache.http.HttpResponse;
import com.google.appengine.repackaged.org.apache.http.client.ClientProtocolException;
import com.google.appengine.repackaged.org.apache.http.client.methods.HttpGet;
import com.google.appengine.repackaged.org.apache.http.impl.client.DefaultHttpClient;

/**
 * TODO run in GWT as well? requires a custom http response wrapper
 *
 * @author xamde
 */
@RunsInGWT(false)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class UniversalUrlFetch {

	private static final Logger log = LoggerFactory.getLogger(UniversalUrlFetch.class);

	/**
	 * @param urlStr
	 *            never null
	 * @param username
	 *            if not null, will be used for HTTP Basic Authentication
	 * @param password
	 *            if not null, will be used for HTTP Basic Authentication
	 * @param async
	 *            TODO implement: if true, run asynchronously
	 * @return the HTTP status code
	 * @throws IOException
	 *             ...
	 */
	public static int callUrl(final String urlStr, final String username, final String password, final boolean async)
			throws IOException {
		log.info("Calling URL '" + urlStr + "'");
		final boolean doBasicAuth = username != null && password != null;
		String encodedAuth = null;
		if (doBasicAuth) {
			final byte[] auth = Base64.utf8(username + ":" + password);
			encodedAuth = Base64.encode(auth);
		}
		if (AboutAppEngine.onAppEngine()) {
			final URLFetchService service = URLFetchServiceFactory.getURLFetchService();
			final URL u = new URL(urlStr);
			final HTTPRequest request = new HTTPRequest(u, HTTPMethod.GET);
			if (doBasicAuth) {
				request.addHeader(new HTTPHeader("Authorization", "Basic " + encodedAuth));
			}
			final Future<HTTPResponse> future = service.fetchAsync(new URL(urlStr));
			try {
				return future.get().getResponseCode();
			} catch (final InterruptedException e) {
				throw new RuntimeException(e);
			} catch (final ExecutionException e) {
				throw new RuntimeException(e);
			}
		} else {

			// TODO dont keep connections open (maybe we do that here)
			final DefaultHttpClient httpClient = new DefaultHttpClient();
			final HttpGet request = new HttpGet(urlStr);
			if (doBasicAuth) {
				request.setHeader("Authorization", "Basic " + encodedAuth);
			}
			try {
				final HttpResponse response = httpClient.execute(request);
				final int responseCode = response.getStatusLine().getStatusCode();
				request.abort();
				return responseCode;
			} catch (final ClientProtocolException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
