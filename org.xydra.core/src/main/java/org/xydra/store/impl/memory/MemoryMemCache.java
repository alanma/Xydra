package org.xydra.store.impl.memory;

import java.util.HashMap;

import org.xydra.store.IMemCache;


public class MemoryMemCache extends HashMap<Object,Object> implements IMemCache {
	
	private static final long serialVersionUID = 8755558878571655815L;
	
	@Override
	public String stats() {
		return "MemoryCache, no stats";
	}
	
}
