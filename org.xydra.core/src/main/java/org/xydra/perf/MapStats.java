package org.xydra.perf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * A stats recorder for map-like data-structures.
 * 
 * @author xamde
 */
public class MapStats {
	
	private static final Logger log = LoggerFactory.getLogger(MapStats.class);
	
	/**
	 * Statistics about one cache entry
	 */
	class Entry {
		String key;
		long entryGets;
		long entryPuts;
		List<Object> values = new LinkedList<Object>();
		long misses;
		
		public Entry(String key) {
			this.key = key;
		}
		
		public long currentValueSize() {
			Object value = currentValue();
			if(value == null)
				return 0;
			else {
				if(!(value instanceof Serializable)) {
					log.warn("Could not estimate size of non-Serializable type "
					        + value.getClass().getCanonicalName());
					return 0;
				} else
					return MapStats.this.sizeOf((Serializable)value);
			}
		}
		
		public Object currentValue() {
			if(this.values.isEmpty())
				return null;
			else
				return this.values.get(this.values.size() - 1);
		}
		
		public void writeStats(Writer w) throws IOException {
			w.write(this.key + " = " + this.entryGets + " gets, " + this.entryPuts
			        + " puts<br />\n");
			
		}
	}
	
	Map<String,Entry> statsMap = new HashMap<String,MapStats.Entry>();
	long gets = 0, puts = 0;
	
	public void recordGet(String key, boolean found) {
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
	}
	
	/**
	 * @param obj to be estimated in size
	 * @return estimated size by serialising to ObjectStream and counting bytes
	 */
	public long sizeOf(Serializable obj) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.close();
			return bos.toByteArray().length;
		} catch(IOException e) {
			log.warn("Could not estimate size of object with type "
			        + obj.getClass().getCanonicalName());
			return 0;
		}
	}
	
	public void recordPut(String key, Object value) {
		Entry e = this.statsMap.get(key);
		if(e == null) {
			e = new Entry(key);
			this.statsMap.put(key, e);
		}
		MapStats.this.puts++;
		e.entryPuts++;
		e.values.add(value);
	}
	
	public int size() {
		return this.statsMap.size();
	}
	
	public void writeStats(Writer w) throws IOException {
		Summary summary = calcSummary();
		
		int k = 10;
		w.write("Largest " + k + " entries by current value:<br />");
		for(Entry e : summary.getEntriesSortedByCurrentValueSizeDescending(k)) {
			e.writeStats(w);
		}
		w.write("Total memory used by current values: " + summary.getTotalCurrentMemorySize()
		        + "<br />");
		w.write("The " + k + " most frequently put'ed entries:<br />");
		for(Entry e : summary.getMostFrequentlyPuttetEntries(k)) {
			e.writeStats(w);
		}
		w.write("The " + k + " most frequent cache misses:<br />");
		for(Entry e : summary.getMostFrequentlyCacheMisses(k)) {
			e.writeStats(w);
		}
	}
	
	private Summary calcSummary() {
		return new Summary();
	}
	
	private static List<Entry> getTopKByComparator(Collection<Entry> entries, int k,
	        Comparator<Entry> comparator) {
		List<Entry> sorted = new ArrayList<Entry>();
		Collections.sort(sorted, comparator);
		sorted.addAll(entries);
		return sorted.subList(0, k);
	}
	
	class Summary {
		
		/**
		 * @param k top k entries are returned
		 * @return which entries are the largest?
		 */
		public List<Entry> getEntriesSortedByCurrentValueSizeDescending(int k) {
			return getTopKByComparator(MapStats.this.statsMap.values(), k, new Comparator<Entry>() {
				@Override
				public int compare(Entry a, Entry b) {
					return (int)(a.currentValueSize() - b.currentValueSize());
				}
			});
		}
		
		public List<Entry> getMostFrequentlyCacheMisses(int k) {
			return getTopKByComparator(MapStats.this.statsMap.values(), k, new Comparator<Entry>() {
				@Override
				public int compare(Entry a, Entry b) {
					return (int)(a.misses - b.misses);
				}
			});
		}
		
		public List<Entry> getMostFrequentlyPuttetEntries(int k) {
			return getTopKByComparator(MapStats.this.statsMap.values(), k, new Comparator<Entry>() {
				@Override
				public int compare(Entry a, Entry b) {
					return (int)(a.entryPuts - b.entryPuts);
				}
			});
		}
		
		/**
		 * @return how much memory is currently consumed?
		 */
		public long getTotalCurrentMemorySize() {
			long size = 0;
			for(java.util.Map.Entry<String,Entry> e : MapStats.this.statsMap.entrySet()) {
				size += e.getValue().currentValueSize();
			}
			return -1;
		}
		
	}
	
	public void clear() {
		this.statsMap.clear();
		this.gets = 0;
		this.puts = 0;
	}
	
}
