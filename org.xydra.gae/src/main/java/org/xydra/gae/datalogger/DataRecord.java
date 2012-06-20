package org.xydra.gae.datalogger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.xydra.annotations.RunsInGWT;


/**
 * TODO instead of storing _creationDate it can also be computed from the key
 * 
 * @author xamde
 */
@RunsInGWT(true)
public class DataRecord implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
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
	
	final long utcCreationDate;
	
	/**
	 * key-value-pairs
	 */
	final Map<String,String> map;
	
	String key;
	
	public long getCreationDate() {
		return this.utcCreationDate;
	}
	
	/**
	 * @return a quasi-unique key, if defined
	 */
	public String getKey() {
		return this.key;
	}
	
	protected void setKey(String key) {
		this.key = key;
	}
	
	public Map<String,String> getMap() {
		return this.map;
	}
	
}
