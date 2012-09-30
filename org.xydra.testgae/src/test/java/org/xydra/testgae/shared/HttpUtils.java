package org.xydra.testgae.shared;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.testgae.client.RemoteBenchmark;

import com.google.appengine.repackaged.org.apache.http.HttpResponse;
import com.google.appengine.repackaged.org.apache.http.client.methods.HttpGet;
import com.google.appengine.repackaged.org.apache.http.impl.client.DefaultHttpClient;
import com.google.appengine.repackaged.org.apache.http.params.BasicHttpParams;
import com.google.appengine.repackaged.org.apache.http.params.HttpConnectionParams;
import com.google.appengine.repackaged.org.apache.http.params.HttpParams;


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
	 * @param absoluteUrl to make a GET request to
	 * @return true if request returned 200 code
	 */
	public static synchronized boolean makeGetRequest(String absoluteUrl) {
		return makeGetRequest(absoluteUrl, RemoteBenchmark.SINGLETHREADONLY);
	}
	
	public static synchronized boolean makeGetRequest(String absoluteUrl, int threadNr) {
		assert absoluteUrl != null;
		assert absoluteUrl.startsWith("http");
		DefaultHttpClient httpclient = createHttpClient();
		HttpGet httpget = new HttpGet(absoluteUrl);
		try {
			// log.info("Thread Nr. " + threadNr + ": GET STATUS " +
			// absoluteUrl);
			HttpResponse res = httpclient.execute(httpget);
			int status = res.getStatusLine().getStatusCode();
			// log.info("Thread Nr. " + threadNr + ": GOT STATUS " + absoluteUrl
			// + " => " + status);
			return status == 200;
		} catch(Exception e) {
			// In case of an unexpected exception you may want to abort
			// the HTTP request in order to shut down the underlying
			// connection immediately.
			httpget.abort();
			throw new RuntimeException("Thread Nr. " + threadNr + ": Failed on " + absoluteUrl, e);
		}
	}
	
	/**
	 * @param absoluteUrl to make a GET request to
	 * @return true if request returned 200 code
	 */
	public static synchronized String getRequestAsStringResponse(String absoluteUrl) {
		return getRequestAsStringResponse(absoluteUrl, RemoteBenchmark.SINGLETHREADONLY);
	}
	
	public static synchronized String getRequestAsStringResponse(String absoluteUrl, int threadNr) {
		assert absoluteUrl != null;
		assert absoluteUrl.startsWith("http");
		DefaultHttpClient httpclient = createHttpClient();
		HttpGet httpget = new HttpGet(absoluteUrl);
		try {
			// this may take a while
			log.info("Thread Nr. " + threadNr + ": GET CONTENT " + absoluteUrl);
			HttpResponse res = httpclient.execute(httpget);
			int status = res.getStatusLine().getStatusCode();
			assert status == 200 : "status is " + status + " for " + absoluteUrl;
			InputStream in = res.getEntity().getContent();
			Reader r = new InputStreamReader(in, "utf-8");
			String content = IOUtils.toString(r);
			r.close();
			in.close();
			log.info("Thread Nr. " + threadNr + ": GOT CONTENT " + absoluteUrl + " "
			        + content.length() + " chars");
			return content;
		} catch(AssertionError e) {
			// for example if returned status is 500
			return null;
		} catch(Exception e) {
			// In case of an unexpected exception you may want to abort
			// the HTTP request in order to shut down the underlying
			// connection immediately.
			httpget.abort();
			log.info("Thread Nr. " + threadNr + ": Failed on " + absoluteUrl, e);
			
			return null;
		}
	}
	
	private static DefaultHttpClient createHttpClient() {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, ConnectionTimeoutMillis);
		HttpConnectionParams.setSoTimeout(httpParams, SocketTimeoutMillis);
		return new DefaultHttpClient(httpParams);
		
	}
}
