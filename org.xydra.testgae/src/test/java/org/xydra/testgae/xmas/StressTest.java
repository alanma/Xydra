package org.xydra.testgae.xmas;

import static org.junit.Assert.assertEquals;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class StressTest {
	
	private static final Logger log = LoggerFactory.getLogger(StressTest.class);
	
	@Test
	public void testConcurrentRequests() {
		long threads = 1;
		long THREADS_PER_MINUTE_MAX = 1;
		long start = System.currentTimeMillis();
		while(true) {
			long now = System.currentTimeMillis();
			double threadsPerMinute = (now - start) / threads;
			if(threadsPerMinute < THREADS_PER_MINUTE_MAX) {
				Worker worker = new Worker(threads++);
				worker.start();
				log.info("Running threads: " + threads);
			}
			Thread.yield();
		}
	}
	
	class Worker extends Thread {
		
		public Worker(long id) {
			this.id = id;
		}
		
		long id;
		
		long count = 0;
		
		@Override
		public void run() {
			while(true) {
				assertUrl("id " + this.id + " has count " + this.count++,
				        "http://localhost:8080/test1");
				try {
					sleep((long)(Math.random() * 1000));
				} catch(InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
	}
	
	public static void assertUrl(String logMsg, String absoluteUrl) {
		log.info(logMsg + " trying url " + absoluteUrl);
		try {
			HttpClient httpclient = new HttpClient();
			GetMethod httpget = new GetMethod(absoluteUrl);
			int status = httpclient.executeMethod(httpget);
			assertEquals(absoluteUrl, 200, status);
		} catch(Exception e) {
			throw new RuntimeException("Failed on " + absoluteUrl, e);
		}
	}
	
}
