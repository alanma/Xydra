package org.xydra.perf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.minio.MiniIOException;
import org.xydra.base.minio.MiniWriter;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.ReflectionUtils;


/**
 * A stats recorder for map-like data-structures.
 *
 * @author xamde
 */
@RunsInGWT(true)
public class MapStats {

	private static final Logger log = LoggerFactory.getLogger(MapStats.class);

	private static final int RECORD_FIRST_N_ACTIONS = 100;

	/**
	 * Very memory and performance intensive
	 */
	public static boolean RECORD_STACKTRACES = true;

	/**
	 * Statistics about one cache entry
	 */
	class Entry {
		String key;
		long entryGets;
		long entryPuts;
		List<Object> values = new LinkedList<Object>();
		long misses;
		/* local list of actions per key */
		List<Action> first_k_actions_per_entry = new LinkedList<MapStats.Action>();

		public Entry(final String key) {
			this.key = key;
		}

		public long currentValueSize() {
			final Object value = currentValue();
			if(value == null) {
				return 0;
			} else {
				if(!(value instanceof Serializable)) {
					log.warn("Could not estimate size of non-Serializable type "
					        + value.getClass().getName());
					return 0;
				} else {
					return ReflectionUtils.sizeOf((Serializable)value);
				}
			}
		}

		public Object currentValue() {
			if(this.values.isEmpty()) {
				return null;
			} else {
				return this.values.get(this.values.size() - 1);
			}
		}

		public void writeStats(final MiniWriter w) throws MiniIOException {
			w.write("  " + this.key + " = " + this.entryGets + " gets, " + this.entryPuts
			        + " puts, " + this.misses + " misses<br />\n");
			// write actions per key
			w.write("  Actions: <br />\n");
			for(final Action action : this.first_k_actions_per_entry) {
				action.writeStats("    ", w);
			}
		}

		public void recordAction(final Action action) {
			// unlimited adding!
			this.first_k_actions_per_entry.add(action);
		}
	}

	class Action {
		Throwable t;
		String method, key;
		Object value;

		public Action(final String method, final String key, final Object value) {
			super();
			this.method = method;
			this.key = key;
			this.value = value;

			if(RECORD_STACKTRACES) {
				try {
					throw new RuntimeException("HERE");
				} catch(final Exception e) {
					this.t = e.fillInStackTrace();
				}
			}
			if(log.isDebugEnabled()) {
				log.debug("Recorded action on map: " + method + " " + key + " = " + value + " \n"
				        + stacktrace());
			}
		}

		public String stacktrace() {
			if(this.t == null) {
				return "  ";
			} else {
				return ReflectionUtils.firstNLines(this.t, 7);
			}
		}

		public void writeStats(final String whitespace, final MiniWriter w) throws MiniIOException {
			w.write(whitespace
			        + this.method
			        + " '"
			        + this.key
			        + "'"
			        + (this.value == null ? "" : " = '"
			                + ReflectionUtils.getCanonicalName(this.value.getClass()) + "'")
			        + "     <br/>\n" + stacktrace());
		}
	}

	/* global list of actions */
	private final List<Action> first_k_actions = new LinkedList<MapStats.Action>();

	Map<String,Entry> statsMap = new HashMap<String,MapStats.Entry>();
	long gets = 0, puts = 0;

	public void recordGet(final String key, final boolean found, final int batchSize) {
		Entry e = this.statsMap.get(key);
		if(e == null) {
			e = new Entry(key);
			this.statsMap.put(key, e);
		}
		MapStats.this.gets++;
		e.entryGets++;
		if(!found) {
			e.misses++;
		}

		final Action action = new Action("Get-" + (found ? "HIT!!!" : "Miss  ") + " "
		        + (batchSize == 1 ? "single" : "batch-" + batchSize), key, null);
		e.recordAction(action);
		if(this.first_k_actions.size() < RECORD_FIRST_N_ACTIONS) {
			this.first_k_actions.add(action);
		}
	}

	public void recordPut(final String key, final Object value) {
		Entry e = this.statsMap.get(key);
		if(e == null) {
			e = new Entry(key);
			this.statsMap.put(key, e);
		}
		MapStats.this.puts++;
		e.entryPuts++;
		e.values.add(value);
		final Action action = new Action("Put " + "      ", key, value);
		e.recordAction(action);
		if(this.first_k_actions.size() < RECORD_FIRST_N_ACTIONS) {
			this.first_k_actions.add(action);
		}
	}

	public int size() {
		return this.statsMap.size();
	}

	public void writeStats(final MiniWriter w) throws MiniIOException {
		final Summary summary = calcSummary();

		final int k = 10;
		w.write("Largest " + k + " entries by current value: ----------------------------<br />\n");
		for(final Entry e : summary.getEntriesSortedByCurrentValueSizeDescending(k)) {
			e.writeStats(w);
		}
		w.write("Total memory used by current values: " + summary.getTotalCurrentMemorySize()
		        + "<br />\n");
		w.write("The " + k
		        + " most frequently put'ed entries: ----------------------------<br />\n");
		for(final Entry e : summary.getMostFrequentlyPuttetEntries(k)) {
			e.writeStats(w);
		}
		w.write("The " + k + " most frequent cache misses: ----------------------------<br />\n");
		for(final Entry e : summary.getMostFrequentlyCacheMisses(k)) {
			e.writeStats(w);
		}
		w.write("The first max " + RECORD_FIRST_N_ACTIONS
		        + " actions: ----------------------------<br />\n");
		for(final Action action : this.first_k_actions) {
			action.writeStats("  ", w);
		}
	}

	private Summary calcSummary() {
		return new Summary();
	}

	private static List<Entry> getTopKByComparator(final Collection<Entry> entries, final int k,
	        final Comparator<Entry> comparator) {
		final List<Entry> sorted = new ArrayList<Entry>(entries.size());
		sorted.addAll(entries);
		Collections.sort(sorted, comparator);
		return sorted.subList(0, Math.min(sorted.size(), k));
	}

	class Summary {

		/**
		 * @param k top k entries are returned
		 * @return which entries are the largest?
		 */
		public List<Entry> getEntriesSortedByCurrentValueSizeDescending(final int k) {
			return getTopKByComparator(MapStats.this.statsMap.values(), k, new Comparator<Entry>() {
				@Override
				public int compare(final Entry a, final Entry b) {
					return (int)(b.currentValueSize() - a.currentValueSize());
				}
			});
		}

		public List<Entry> getMostFrequentlyCacheMisses(final int k) {
			return getTopKByComparator(MapStats.this.statsMap.values(), k, new Comparator<Entry>() {
				@Override
				public int compare(final Entry a, final Entry b) {
					return (int)(b.misses - a.misses);
				}
			});
		}

		public List<Entry> getMostFrequentlyPuttetEntries(final int k) {
			return getTopKByComparator(MapStats.this.statsMap.values(), k, new Comparator<Entry>() {
				@Override
				public int compare(final Entry a, final Entry b) {
					return (int)(b.entryPuts - a.entryPuts);
				}
			});
		}

		/**
		 * @return how much memory is currently consumed?
		 */
		public long getTotalCurrentMemorySize() {
			long size = 0;
			for(final java.util.Map.Entry<String,Entry> e : MapStats.this.statsMap.entrySet()) {
				size += e.getValue().currentValueSize();
			}
			return size;
		}

	}

	public void clear() {
		this.statsMap.clear();
		this.gets = 0;
		this.puts = 0;
		this.first_k_actions.clear();
	}

}
