package org.xydra.perf;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A generic in-memory statistics store to be used by performance monitors.
 * 
 * Usage:
 * 
 * <code><pre>
 * Clock c = stats.startClock("operationName");
 * // operation here ...
 * c.stop();
 * </pre></code>
 * 
 * @author xamde
 */
public class Stats {
	
	public Map<String,Data> stats = new ConcurrentHashMap<String,Stats.Data>();
	
	private static class Data {
		public long count = 0;
		public long duration = 0;
		
		public void writeStats(String name, Writer w) throws IOException {
			long nsPerCall = this.count > 0 ? this.duration / this.count : -1;
			w.write("  " + name + " called " + this.count + " times. Total: "
			        + (this.duration / 1000) + " micros = " + (this.duration / 1000000)
			        + " ms. Per call: " + (nsPerCall / 1000) + " micros = " + (nsPerCall / 1000000)
			        + " ms\n");
		}
	}
	
	class Clock {
		private long start;
		private String name;
		
		public Clock(String name) {
			this.name = name;
			this.start = System.nanoTime();
		}
		
		public void stop() {
			long duration = System.nanoTime() - this.start;
			Data data = Stats.this.stats.get(this.name);
			if(data == null) {
				data = new Data();
				Stats.this.stats.put(this.name, data);
			}
			data.count++;
			data.duration += duration;
		}
	}
	
	public Clock startClock(String name) {
		Clock c = new Clock(name);
		return c;
	}
	
	public void writeStats(Writer w) throws IOException {
		for(String name : this.stats.keySet()) {
			Data d = this.stats.get(name);
			d.writeStats(name, w);
		}
	}
	
}
