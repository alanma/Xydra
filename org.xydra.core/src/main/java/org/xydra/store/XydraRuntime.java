package org.xydra.store;

import java.util.HashMap;
import java.util.Map;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.perf.StatsGatheringMemCacheWrapper;
import org.xydra.perf.StatsGatheringPersistenceWrapper;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.memory.MemoryRuntime;


/**
 * A singleton for accessing platform specific variants of certain services such
 * as a cache which is a different thing on Google AppEngine and on plain Java.
 * 
 * The initialisation process is triggered with the first call to
 * {@link #getMemcache()} or {@link #getPersistence(XID)}. The initialisation
 * process runs in three phases:
 * <ol>
 * <li>Let another class set a {@link XydraPlatformRuntime}</li>
 * <li>Try to load {@link #PLATFORM_CLASS} which must have a zero-argument
 * constructor and be an instance of {@link XydraPlatformRuntime}</li>
 * <li>Use an in-memory platform as a fall-back</li>
 * </ol>
 * 
 * TODO GWT doesn't have Class.forName()
 * 
 * @author xamde
 */
@RunsInGWT(false)
public class XydraRuntime {
	
	private static final Logger log = LoggerFactory.getLogger(XydraRuntime.class);
	
	public static final String PLATFORM_CLASS = "org.xydra.store.platform.RuntimeBinding";
	
	public static final String PROP_MEMCACHESTATS = "memcacheStats";
	
	public static final String PROP_PERSISTENCESTATS = "persistenceStats";
	
	public static final String PROP_USEMEMCACHE = "usememcache";
	
	private static Map<XID,XydraPersistence> persistenceInstanceCache = new HashMap<XID,XydraPersistence>();
	
	private static boolean platformInitialised = false;
	
	private static XydraPlatformRuntime platformRuntime;
	
	private static IMemCache memcacheInstance;
	
	/** Runtime configuration that can be set from outside */
	private static Map<String,String> configMap = new HashMap<String,String>();
	
	/**
	 * @return a re-used instance of a Cache
	 */
	public static synchronized IMemCache getMemcache() {
		initialiseRuntimeOnce();
		if(memcacheInstance == null) {
			memcacheInstance = platformRuntime.getMemCache();
			// wrap if requested
			String memcacheStatsStr = configMap.get(PROP_MEMCACHESTATS);
			boolean memcacheStats = memcacheStatsStr != null
			        && memcacheStatsStr.equalsIgnoreCase("true");
			if(memcacheStats) {
				memcacheInstance = new StatsGatheringMemCacheWrapper(memcacheInstance);
			}
		}
		return memcacheInstance;
	}
	
	public static synchronized void setPlatformRuntime(XydraPlatformRuntime platformRuntime_) {
		if(platformRuntime_ == null) {
			throw new IllegalArgumentException("XydraPlatformRuntime may not be null");
		}
		platformRuntime = platformRuntime_;
		platformInitialised = true;
	}
	
	private static synchronized void initialiseRuntimeOnce() {
		if(platformInitialised) {
			return;
		}
		// try to load dynamically
		try {
			Class<?> platformClass = Class.forName(PLATFORM_CLASS);
			try {
				Object platformInstance = platformClass.newInstance();
				try {
					XydraPlatformRuntime xPlatformInstance = (XydraPlatformRuntime)platformInstance;
					platformRuntime = xPlatformInstance;
					platformInitialised = true;
					log.info("Using default platform "
					        + xPlatformInstance.getClass().getCanonicalName());
				} catch(ClassCastException e) {
					log.warn("Found the class with name " + PLATFORM_CLASS
					        + " but it is not implementing " + XydraPlatformRuntime.class, e);
				}
			} catch(InstantiationException e) {
				log.warn("Found the class with name " + PLATFORM_CLASS
				        + " but could not instantiate it", e);
			} catch(IllegalAccessException e) {
				log.warn("Found the class with name " + PLATFORM_CLASS
				        + " but could not instantiate it", e);
			}
		} catch(ClassNotFoundException e) {
			// so we fall back to defaults
		}
		
		if(platformInitialised) {
			return;
		}
		// still no platform, use defaults
		log.info("Using default MemoryRuntime");
		platformRuntime = new MemoryRuntime();
		platformInitialised = true;
	}
	
	public static void setParameter(String key, String value) {
		configMap.put(key, value);
	}
	
	/**
	 * @return a re-used instance of a {@link XydraPersistence}
	 */
	public static synchronized XydraPersistence getPersistence(XID repositoryId) {
		initialiseRuntimeOnce();
		XydraPersistence persistence = persistenceInstanceCache.get(repositoryId);
		if(persistence == null) {
			persistence = platformRuntime.getPersistence(repositoryId);
			/** Check if we are gathering stats */
			
			String persistenceStatsStr = configMap.get(PROP_PERSISTENCESTATS);
			boolean persistenceStats = persistenceStatsStr != null
			        && persistenceStatsStr.equalsIgnoreCase("true");
			if(persistenceStats) {
				/**
				 * Statistics can be accesses by getting the persistence and
				 * casting it to StatsPersistence
				 */
				StatsGatheringPersistenceWrapper statsPersistence = new StatsGatheringPersistenceWrapper(
				        persistence);
				persistence = statsPersistence;
			}
			// put in cache
			persistenceInstanceCache.put(repositoryId, persistence);
		}
		return persistence;
	}
	
	public static synchronized void forceReInitialisation() {
		// keep configMap as it is
		memcacheInstance = null;
		persistenceInstanceCache.clear();
		platformInitialised = false;
	}
	
}
