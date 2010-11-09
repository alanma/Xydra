package org.xydra.googleanalytics.httpclienht;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;


public class HttpUserAgentUrlFetch implements HttpUserAgent {
	
	private URLFetchService urlfetch;
	
	HttpUserAgentUrlFetch() {
		new URLFetchServiceFactory();
		this.urlfetch = URLFetchServiceFactory.getURLFetchService();
	}
	
	@Override
	public void setAutoRetry(boolean autoRetry) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setConnectionTimeout(int maxMillis) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setUserAgentIdentifier(String uSERAGENT) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Future<Integer> GET(String urlString) {
		try {
			URL url = new URL(urlString);
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
