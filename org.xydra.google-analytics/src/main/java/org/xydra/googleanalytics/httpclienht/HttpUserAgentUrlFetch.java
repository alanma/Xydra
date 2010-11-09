package org.xydra.googleanalytics.httpclienht;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;


/**
 * @author voelkel
 * 
 * 
 *         On 2010-11-09
 *         http://code.google.com/intl/de-DE/appengine/docs/java/urlfetch
 *         /overview.html says:
 * 
 *         For security reasons, the following headers cannot be modified by the
 *         application:
 * 
 *         Content-Length, Host, Vary, Via, X-Forwarded-For
 * 
 *         These headers are set to accurate values by App Engine, as
 *         appropriate. For example, App Engine calculates the Content-Length
 *         header from the request data and adds it to the request prior to
 *         sending.*
 */
public class HttpUserAgentUrlFetch implements HttpUserAgent {
	
	private URLFetchService urlfetch;
	private FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
	private String userAgent;
	
	HttpUserAgentUrlFetch() {
		new URLFetchServiceFactory();
		this.urlfetch = URLFetchServiceFactory.getURLFetchService();
	}
	
	@Override
	public void setAutoRetry(boolean autoRetry) {
		// TODO implement
	}
	
	@Override
	/*
	 * By default, the deadline for a fetch is 5 seconds. The maximum deadline
	 * is 10 seconds.
	 */
	public void setConnectionTimeout(int maxMillis) {
		this.fetchOptions.setDeadline((double)maxMillis / 1000);
	}
	
	@Override
	public void setUserAgentIdentifier(String userAgent) {
		// not supported
		this.userAgent = userAgent;
	}
	
	@Override
	public Future<Integer> GET(String urlString) {
		try {
			URL url = new URL(urlString);
			
			HTTPRequest httpRequest = new HTTPRequest(url);
			if(this.userAgent != null) {
				httpRequest.setHeader(new HTTPHeader("User-Agent", this.userAgent));
			}
			Future<HTTPResponse> result = this.urlfetch.fetchAsync(url);
			return new WrappedFuture(result);
		} catch(MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	private static class WrappedFuture implements Future<Integer> {
		
		private Future<HTTPResponse> future;
		
		public WrappedFuture(Future<HTTPResponse> future) {
			this.future = future;
		}
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return this.future.cancel(mayInterruptIfRunning);
		}
		
		@Override
		public Integer get() throws InterruptedException, ExecutionException {
			return this.future.get().getResponseCode();
		}
		
		@Override
		public Integer get(long timeout, TimeUnit unit) throws InterruptedException,
		        ExecutionException, TimeoutException {
			return this.future.get(timeout, unit).getResponseCode();
		}
		
		@Override
		public boolean isCancelled() {
			return this.future.isCancelled();
		}
		
		@Override
		public boolean isDone() {
			return this.future.isDone();
		}
		
	}
	
}
