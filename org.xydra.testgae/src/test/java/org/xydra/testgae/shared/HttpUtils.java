package org.xydra.testgae.shared;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.testgae.client.RemoteBenchmark;

/**
 * Tiny util to make calling remote HTTP methods easier
 *
 * @author xamde
 */
public class HttpUtils {

	private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);
	private static final int ConnectionTimeoutMillis = 8000;
	private static final int SocketTimeoutMillis = 10000;

	/**
	 * @param absoluteUrl
	 *            to make a GET request to
	 * @return true if request returned 200 code
	 */
	public static synchronized boolean makeGetRequest(final String absoluteUrl) {
		return makeGetRequest(absoluteUrl, RemoteBenchmark.SINGLETHREADONLY);
	}

	public static synchronized boolean makeGetRequest(final String absoluteUrl, final int threadNr) {
		assert absoluteUrl != null;
		assert absoluteUrl.startsWith("http");
		final DefaultHttpClient httpclient = createHttpClient();
		final HttpGet httpget = new HttpGet(absoluteUrl);
		try {
			// log.info("Thread Nr. " + threadNr + ": GET STATUS " +
			// absoluteUrl);
			final HttpResponse res = httpclient.execute(httpget);
			final int status = res.getStatusLine().getStatusCode();
			// log.info("Thread Nr. " + threadNr + ": GOT STATUS " + absoluteUrl
			// + " => " + status);
			return status == 200;
		} catch (final Exception e) {
			// In case of an unexpected exception you may want to abort
			// the HTTP request in order to shut down the underlying
			// connection immediately.
			httpget.abort();
			throw new RuntimeException("Thread Nr. " + threadNr + ": Failed on " + absoluteUrl, e);
		}
	}

	/**
	 * @param absoluteUrl
	 *            to make a GET request to
	 * @return true if request returned 200 code
	 */
	public static synchronized String getRequestAsStringResponse(final String absoluteUrl) {
		return getRequestAsStringResponse(absoluteUrl, RemoteBenchmark.SINGLETHREADONLY);
	}

	public static synchronized String getRequestAsStringResponse(final String absoluteUrl, final int threadNr) {
		assert absoluteUrl != null;
		assert absoluteUrl.startsWith("http");
		final DefaultHttpClient httpclient = createHttpClient();
		final HttpGet httpget = new HttpGet(absoluteUrl);
		try {
			// this may take a while
			log.info("Thread Nr. " + threadNr + ": GET CONTENT " + absoluteUrl);
			final HttpResponse res = httpclient.execute(httpget);
			final int status = res.getStatusLine().getStatusCode();
			assert status == 200 : "status is " + status + " for " + absoluteUrl;
			final InputStream in = res.getEntity().getContent();
			final Reader r = new InputStreamReader(in, "utf-8");
			final String content = IOUtils.toString(r);
			r.close();
			in.close();
			log.info("Thread Nr. " + threadNr + ": GOT CONTENT " + absoluteUrl + " "
					+ content.length() + " chars");
			return content;
		} catch (final AssertionError e) {
			// for example if returned status is 500
			return null;
		} catch (final Exception e) {
			// In case of an unexpected exception you may want to abort
			// the HTTP request in order to shut down the underlying
			// connection immediately.
			httpget.abort();
			log.info("Thread Nr. " + threadNr + ": Failed on " + absoluteUrl, e);

			return null;
		}
	}

	private static DefaultHttpClient createHttpClient() {
		final HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, ConnectionTimeoutMillis);
		HttpConnectionParams.setSoTimeout(httpParams, SocketTimeoutMillis);
		return new DefaultHttpClient(httpParams);

	}
}
