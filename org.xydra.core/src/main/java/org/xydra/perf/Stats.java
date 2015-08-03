package org.xydra.perf;

import java.util.Map;

import org.xydra.base.minio.MiniIOException;
import org.xydra.base.minio.MiniWriter;
import org.xydra.sharedutils.SystemUtils;

import com.google.common.collect.MapMaker;


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

	public Map<String,Data> stats =

	new MapMaker().concurrencyLevel(1).initialCapacity(50).makeMap();

	// new ConcurrentHashMap<String,Stats.Data>();

	private static class Data {
		public long count = 0;
		public long duration = 0;

		public void writeStats(final String name, final MiniWriter w) throws MiniIOException {
			final long nsPerCall = this.count > 0 ? this.duration / this.count : -1;
			w.write("  " + name + " called " + this.count + " times. Total: "
			        + this.duration / 1000 + " micros = " + this.duration / 1000000
			        + " ms. Per call: " + nsPerCall / 1000 + " micros = " + nsPerCall / 1000000
			        + " ms\n");
		}
	}

	class Clock {
		private final long start;
		private final String name;

		public Clock(final String name) {
			this.name = name;
			this.start = SystemUtils.nanoTime();
		}

		public void stop() {
			final long duration = SystemUtils.nanoTime() - this.start;
			Data data = Stats.this.stats.get(this.name);
			if(data == null) {
				data = new Data();
				Stats.this.stats.put(this.name, data);
			}
			data.count++;
			data.duration += duration;
		}
	}

	public Clock startClock(final String name) {
		final Clock c = new Clock(name);
		return c;
	}

	public void writeStats(final MiniWriter w) throws MiniIOException {
		for(final String name : this.stats.keySet()) {
			final Data d = this.stats.get(name);
			d.writeStats(name, w);
		}
	}

}
