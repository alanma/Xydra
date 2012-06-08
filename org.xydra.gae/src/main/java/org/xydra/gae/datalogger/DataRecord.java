package org.xydra.gae.datalogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.store.impl.gae.UniCache;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;


/**
 * TODO instead of storing _creationDate it can also be computed from the key
 * 
 * @author xamde
 */
public class DataRecord {
	
	public static final String CREATION_DATE = "_creationDate";
	
	public static final String INDEXED_PREFIX = "i";
	
	public static final String KEY = "__key__";
	
	/**
	 * @param creationDate UTC
	 * @param map no key name may start with '_'. All keys starting with 'i' are
	 *            stored as indexed properties. Values may not be longer than
	 *            255 chars.
	 */
	public DataRecord(final long creationDate, final Map<String,String> map) {
		this.utcCreationDate = creationDate;
		this.map = map;
	}
	
	public static Builder create() {
		return create(System.currentTimeMillis());
	}
	
	public static Builder create(long utcTime) {
		return new Builder(utcTime);
	}
	
	public static class Builder {
		
		private long utc;
		private Map<String,String> map = new HashMap<String,String>();
		
		public Builder(long utc) {
			this.utc = utc;
		}
		
		public Builder withParam(String key, String value) {
			this.map.put(key, value);
			return this;
		}
		
		public DataRecord done() {
			return new DataRecord(this.utc, this.map);
		}
		
		public Builder withParams(Map<String,String> map) {
			this.map.putAll(map);
			return this;
		}
		
	}
	
	private final long utcCreationDate;
	
	/**
	 * key-value-pairs
	 */
	private final Map<String,String> map;
	
	private String key;
	
	public static class DataRecordEntryHandler implements
	        UniCache.DatastoreEntryHandler<DataRecord> {
		
		@Override
		public Entity toEntity(final Key datastoreKey, final DataRecord entry) {
			Entity e = new Entity(datastoreKey);
			e.setUnindexedProperty(CREATION_DATE, entry.utcCreationDate);
			for(Entry<String,String> pair : entry.map.entrySet()) {
				assert !pair.getKey().startsWith("_");
				if(pair.getKey().startsWith(INDEXED_PREFIX)) {
					e.setProperty(pair.getKey(), pair.getValue());
				} else {
					String s = pair.getValue();
					if(s == null) {
						continue;
					}
					if(s.length() > 400) {
						Text value = new Text(s);
						e.setUnindexedProperty(pair.getKey(), value);
					} else {
						e.setUnindexedProperty(pair.getKey(), s);
					}
				}
			}
			entry.key = datastoreKey.getName();
			return e;
		}
		
		@Override
		public DataRecord fromEntity(final Entity entity) {
			Map<String,String> map = new HashMap<String,String>();
			long creationDate = -1;
			
			for(Entry<String,Object> prop : entity.getProperties().entrySet()) {
				if(prop.getKey().equals(CREATION_DATE)) {
					creationDate = (Long)prop.getValue();
				} else {
					Object v = prop.getValue();
					String s;
					if(v instanceof Text) {
						s = ((Text)v).getValue();
					} else {
						s = v.toString();
					}
					map.put(prop.getKey(), s);
				}
			}
			DataRecord dr = new DataRecord(creationDate, map);
			dr.key = entity.getKey().getName();
			return dr;
		}
		
	}
	
	public long getCreationDate() {
		return this.utcCreationDate;
	}
	
	/**
	 * @return a quasi-unique key, if defined
	 */
	public String getKey() {
		return this.key;
	}
	
	public Map<String,String> getMap() {
		return this.map;
	}
	
}
