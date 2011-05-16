package org.xydra.store;

import java.util.Map;


public interface IMemCache extends Map<Object,Object> {
	
	/**
	 * @return a human-readable String with statistical information
	 */
	public String stats();
	
}
