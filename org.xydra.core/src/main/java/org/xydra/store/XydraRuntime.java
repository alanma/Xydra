package org.xydra.store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XID;
import org.xydra.core.model.impl.memory.UUID;
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
@RequiresAppEngine(false)
@RunsInAppEngine(true)
public class XydraRuntime {
	
	private static final Logger log = LoggerFactory.getLogger(XydraRuntime.class);
	
	public static final String PLATFORM_CLASS = "org.xydra.store.platform.RuntimeBinding";
	
	public static final String PROP_MEMCACHESTATS = "memcacheStats";
	
	public static final String PROP_PERSISTENCESTATS = "persistenceStats";
	
	/**
	 * To enable or disable memcache completely in the {@link XydraPersistence}
	 * instances.
	 * 
	 * FIXME clarify in docu differences to GaeConfigSetings.USE_MEMCACHE
	 */
	public static final String PROP_USEMEMCACHE = "useMemcacheInPersistenceImpl";
	
	/**
	 * For each repository Id, one {@link XydraPersistence} is cached.
	 */
	private static Map<XID,XydraPersistence> persistenceInstanceCache = new HashMap<XID,XydraPersistence>();
	
	private static boolean platformInitialised = false;
	
	private static XydraPlatformRuntime platformRuntime;
	
	private static IMemCache memcacheInstance;
	
	/** Runtime configuration that can be set from outside */
	private static Map<String,String> configMap = new HashMap<String,String>();
	
	private static long lastTimeInitialisedAt = -1;
	
	/**
	 * @param booleanString can be null
	 * @return true if string is not null and equals true (case ignored).
	 */
	public static boolean isTrue(String booleanString) {
		return booleanString != null && booleanString.equalsIgnoreCase("true");
	}
	
	/**
	 * @return a (potentially cached) instance of a IMemCache
	 */
	public static synchronized IMemCache getMemcache() {
		initialiseRuntimeOnce();
		if(memcacheInstance == null) {
			memcacheInstance = platformRuntime.getMemCache();
			
			// if configured this way: wrap in StatsGatheringMemCacheWrapper
			String memcacheStatsStr = configMap.get(PROP_MEMCACHESTATS);
			if(isTrue(memcacheStatsStr)) {
				memcacheInstance = new StatsGatheringMemCacheWrapper(memcacheInstance);
			}
		}
		return memcacheInstance;
	}
	
	/**
	 * Allows to explicitly set a {@link XydraPlatformRuntime} to be used. This
	 * is an alternative configuration approach compared to the RuntimeBinding.
	 * 
	 * @param platformRuntime_ ..
	 */
	public static synchronized void setPlatformRuntime(XydraPlatformRuntime platformRuntime_) {
		if(platformRuntime_ == null) {
			throw new IllegalArgumentException("XydraPlatformRuntime may not be null");
		}
		platformRuntime = platformRuntime_;
		platformInitialised = true;
	}
	
	/**
	 * Directly manipulate the internal persistence cache. Use with care. This
	 * instance is replaced with another one from the platform runtime after the
	 * next call of {@link #forceReInitialisation()}.
	 * 
	 * @param repositoryId ..
	 * @param persistence ..
	 */
	public static synchronized void setPersistence(XID repositoryId, XydraPersistence persistence) {
		persistenceInstanceCache.put(repositoryId, persistence);
	}
	
	private static synchronized void initialiseRuntimeOnce() {
		if(platformInitialised) {
			return;
		}
		lastTimeInitialisedAt = System.currentTimeMillis();
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
					        + platformRuntime.getClass().getCanonicalName());
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
		log.warn("No platform configured. Using default MemoryRuntime.");
		platformRuntime = new MemoryRuntime();
		platformInitialised = true;
	}
	
	/**
	 * @param repositoryId ..
	 * @return a (potentially cached) instance of a {@link XydraPersistence}
	 *         with the given repositoryId
	 */
	public static synchronized XydraPersistence getPersistence(XID repositoryId) {
		initialiseRuntimeOnce();
		XydraPersistence persistence = persistenceInstanceCache.get(repositoryId);
		if(persistence == null) {
			/** Get basic instance */
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
	
	/**
	 * @return the current configuration that can also be changed. Callers
	 *         should issue a {@link #forceReInitialisation()} if this
	 *         {@link XydraRuntime} instance has already handed out
	 *         {@link IMemCache} or {@link XydraPersistence} instances.
	 */
	public static Map<String,String> getConfigMap() {
		return configMap;
	}
	
	/**
	 * Clears all cached instances, so that the next call to each of them will
	 * be based on the current configuration. Looses about all caching
	 * information.
	 */
	public static synchronized void forceReInitialisation() {
		lastTimeInitialisedAt = System.currentTimeMillis();
		// keep configMap as it is, so that changing it has an effect
		memcacheInstance = null;
		persistenceInstanceCache.clear();
		platformInitialised = false;
		fireOnInitialisaion();
	}
	
	public static long getLastTimeInitialisedAt() {
		return lastTimeInitialisedAt;
	}
	
	/** A unique ID to distinguish several AppEngine instances */
	public static final String INSTANCE_ID = UUID.uuid(9);
	
	/**
	 * @return a unique string identifying this runtime instance. Within one JVM
	 *         this string can be the same for several
	 *         {@link XydraPlatformRuntime} instances.
	 */
	public static String getInstanceId() {
		return INSTANCE_ID;
	}
	
	public static interface Listener {
		/**
		 * Called whenever the XydraRuntime is initialised
		 */
		void onXydraRuntimeInit();
	}
	
	private static transient Set<Listener> listeners = new HashSet<Listener>();
	
	public static void addListener(Listener listener) {
		listeners.add(listener);
	}
	
	private static void fireOnInitialisaion() {
		for(Listener l : listeners) {
			l.onXydraRuntimeInit();
		}
	}
	
}
