package org.xydra.testgae.client;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.xydra.testgae.shared.SimulatedUser;


/**
 * Benchmark test for a remote server
 * 
 * @author xamde
 * 
 */
public class RemoteBenchmark {
	
	@SuppressWarnings("unused")
	public static void runBenchmark(String absoluteUrl) throws InterruptedException, IOException {
		SimulatedUser u1 = new SimulatedUser(absoluteUrl, "repo1");
		
		Writer w = new OutputStreamWriter(System.out);
		u1.doBenchmark1(w);
		
		// u1.start();
		// // tell other thread after some seconds to stop.
		// Thread.sleep(60 * 1000);
		// u1.pleaseStopSoon();
	}
	
}
