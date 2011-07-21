package org.xydra.store.impl.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.store.IMemCache;


/**
 * This 'cache' will grow and grow and never release any items. Not intended for
 * production use.
 * 
 * @author xamde
 * 
 */
@RunsInGWT(true)
// @RunsInAppEngine but makes no sense
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class MemoryMemCache extends HashMap<Object,Object> implements IMemCache {
	
	private static final long serialVersionUID = 8755558878571655815L;
	
	@Override
	public String stats() {
		return "MemoryCache, no stats";
	}
	
	@Override
	public Map<Object,Object> getAll(Collection<Object> keys) {
		Map<Object,Object> result = new HashMap<Object,Object>();
		for(Object key : keys) {
			Object value = this.get(key);
			if(value != null) {
				result.put(key, value);
			}
		}
		return result;
	}
	
}
