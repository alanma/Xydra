package org.xydra.store;

import java.util.Map;

/**
 * This class is the GWT-replacement for the real XydraRuntime_GwtEmulated.
 * 
 * @author xamde
 */
public class XydraRuntime_GwtEmulated {
	
	public static IMemCache wrapOrReturn(Map<String,String> configMap, IMemCache memcacheInstance) {
		return memcacheInstance;
	}
	
}
