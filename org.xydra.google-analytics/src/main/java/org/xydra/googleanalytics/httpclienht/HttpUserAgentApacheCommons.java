package org.xydra.googleanalytics.httpclienht;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * TODO fix timeout handling future vs. httpclient
 * 
 * @author voelkel
 * 
 */
public class HttpUserAgentApacheCommons implements HttpUserAgent {
	
	private static final Logger log = LoggerFactory.getLogger(HttpUserAgentApacheCommons.class);
	
	private HttpClient httpClient;
	
	private LinkedBlockingQueue<Job> jobQueue;
	
	private TrackingThread workerThread;
	
	public HttpUserAgentApacheCommons() {
		super();
		this.httpClient = new HttpClient();
		this.jobQueue = new LinkedBlockingQueue<Job>(1000);
	}
	
	@Override
	public void setUserAgentIdentifier(String userAgent) {
		System.getProperties().setProperty("httpclient.useragent", userAgent);
	}
	
	@Override
	public void setConnectionTimeout(int maxMillis) {
		this.httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
	}
	
	@Override
	public void setAutoRetry(boolean autoRetry) {
		this.httpClient.getHttpConnectionManager().getParams().setParameter(
		        "http.method.retry-handler", new DefaultHttpMethodRetryHandler(0, false));
	}
	
	private class TrackingThread extends Thread {
		public TrackingThread() {
			this.setPriority(Thread.MIN_PRIORITY);
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					Job job = HttpUserAgentApacheCommons.this.jobQueue.take();
					int status = HttpUserAgentApacheCommons.httpGet(
					        HttpUserAgentApacheCommons.this.httpClient, job);
					// retry every 30 seconds
					while(!isOkStatus(status)) {
						Thread.sleep(30000);
						status = HttpUserAgentApacheCommons.httpGet(
						        HttpUserAgentApacheCommons.this.httpClient, job);
					}
				} catch(InterruptedException e) {
					log.debug("waiting for urls to track", e);
				}
			}
		}
	}
	
	private static boolean isOkStatus(int status) {
		return 200 <= status && status < 300;
	}
	
	private static int httpGet(HttpClient httpClient, Job job) {
		log.debug("GET: " + job.url);
		GetMethod get = new GetMethod(job.url);
		try {
			int status = httpClient.executeMethod(get);
			job.status = status;
			if(isOkStatus(status)) {
				log.trace("Status " + status);
			} else {
				log.debug("Status code is " + status);
			}
			return status;
		} catch(IOException e) {
			log.info("Network error. Could not track " + job.url);
			log.debug("Network error = ", e);
			return 404;
		} finally {
			get.releaseConnection();
		}
		
	}
	
	private static final long toMillis(long time, TimeUnit unit) {
		long result = time;
		switch(unit) {
		case NANOSECONDS:
			return result / 1000000;
		case MICROSECONDS:
			return result / 1000;
		case MILLISECONDS:
			return result;
		case SECONDS:
			return result * 1000;
		case MINUTES:
			return result * 1000 * 60;
		case HOURS:
			return result * 1000 * 60 * 60;
		case DAYS:
			return result * 1000 * 60 * 60 * 24;
		}
		throw new AssertionError();
	}
	
	private static class Job implements Future<Integer> {
		public int status = -1;
		String url;
		private boolean cancelled = false;
		private boolean done = false;
		
		public Job(String url) {
			this.url = url;
		}
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			if(this.done || this.cancelled) {
				return false;
			} else {
				this.cancelled = true;
				return true;
			}
		}
		
		@Override
		public Integer get() throws InterruptedException, ExecutionException {
			while(!isDone() && !isCancelled() && this.status < 0) {
				Thread.sleep(1000);
			}
			return this.status;
		}
		
		@Override
		public Integer get(long timeout, TimeUnit unit) throws InterruptedException,
		        ExecutionException, TimeoutException {
			while(!isDone() && !isCancelled() && this.status < 0) {
				Thread.sleep(toMillis(timeout, unit));
			}
			return null;
		}
		
		@Override
		public boolean isCancelled() {
			return this.cancelled;
		}
		
		@Override
		public boolean isDone() {
			return this.done;
		}
	}
	
	@Override
	public synchronized Future<Integer> GET(String url) {
		// put in queue
		Job job = new Job(url);
		boolean scheduled = this.jobQueue.offer(job);
		if(!scheduled) {
			log.warn("More than 1000 urls scheduled for tracking.");
		}
		
		// make sure the worker thread runs
		if(this.workerThread == null) {
			this.workerThread = new TrackingThread();
			this.workerThread.start();
		}
		
		return job;
	}
	
}
