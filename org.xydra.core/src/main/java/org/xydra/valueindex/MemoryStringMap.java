package org.xydra.valueindex;

import java.util.HashMap;


/**
 * This is just a mock-up of the StringMap interface using Javas HashMap, purely
 * for testing purposes. Not for use with GWT/GAE.
 * 
 * @author kaidel
 * 
 */
public class MemoryStringMap implements StringMap {
	private HashMap<String,String> map;
	
	public MemoryStringMap() {
		this.map = new HashMap<String,String>();
	}
	
	@Override
	public String put(String key, String value) {
		return this.map.put(key, value);
	}
	
	@Override
	public String get(String key) {
		return this.map.get(key);
	}
	
	@Override
	public void remove(String key) {
		this.map.remove(key);
	}
	
}
