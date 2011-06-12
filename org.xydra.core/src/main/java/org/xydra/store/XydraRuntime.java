package org.xydra.store;

import java.util.HashMap;
import java.util.Map;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
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
	
	private static Map<XID,XydraPersistence> persistenceInstance = new HashMap<XID,XydraPersistence>();
	
	private static boolean platformInitialised = false;
	
	private static XydraPlatformRuntime platformRuntime;
	
	private static IMemCache cacheInstance;
	
	/**
	 * @return a re-used instance of a Cache
	 */
	public static synchronized IMemCache getMemcache() {
		initialiseRuntime();
		if(cacheInstance == null) {
			cacheInstance = platformRuntime.getMemCache();
		}
		return cacheInstance;
	}
	
	public static synchronized void setPlatformRuntime(XydraPlatformRuntime platformRuntime_) {
		if(platformRuntime_ == null) {
			throw new IllegalArgumentException("XydraPlatformRuntime may not be null");
		}
		platformRuntime = platformRuntime_;
		platformInitialised = true;
	}
	
	private static synchronized void initialiseRuntime() {
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
		platformRuntime = new MemoryRuntime();
		platformInitialised = true;
	}
	
	/**
	 * @return a re-used instance of a {@link XydraPersistence}
	 */
	public static synchronized XydraPersistence getPersistence(XID repositoryId) {
		initialiseRuntime();
		XydraPersistence xp = persistenceInstance.get(repositoryId);
		if(xp == null) {
			xp = platformRuntime.getPersistence(repositoryId);
		}
		return xp;
	}
	
}
