package org.xydra.xgae.memcache.impl;

import org.xydra.store.XydraRuntime;

/**
 * This class contains the parts of {@link XydraRuntime} that can only be
 * emulated on GWT. I.e. on GWT a different class, located in
 * /src/main/resources/..../gwtemul/... is used.
 *
 * @author xamde
 *
 */
public class XydraRuntime_GwtEmulated {

	// public static IMemCache wrapOrReturn(Map<String,String> configMap,
	// IMemCache memcacheInstance) {
	// // if configured this way: wrap in StatsGatheringMemCacheWrapper
	// String memcacheStatsStr = configMap.get(XydraRuntime.PROP_MEMCACHESTATS);
	// if(ConfigUtils.isTrue(memcacheStatsStr)) {
	// return new StatsGatheringMemCacheWrapper(memcacheInstance);
	// } else {
	// return memcacheInstance;
	// }
	// }

}
