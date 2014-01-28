package org.xydra.testgae.client;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


@SuppressWarnings("unused")
public class LocalConsistencyTest {
	
	private static final Logger log = LoggerFactory.getLogger(LocalConsistencyTest.class);
	
	private static final String SERVER_ROOT = "http://localhost:8080"; // 8765";
	
	public static final int THREADS = 30;
	
	public static void main(String[] args) {
		for(int i = 0; i < THREADS; i++) {
			ConsistencyTestClient client = new ConsistencyTestClient(SERVER_ROOT, "thread" + i
			        + "-", 10);
			client.start();
		}
	}
}
