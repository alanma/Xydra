package org.xydra.store.impl.memory;

import java.util.HashMap;

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
	
}
