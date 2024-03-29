package org.xydra.store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.id.UUID;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.perf.StatsGatheringPersistenceWrapper;
import org.xydra.persistence.XydraPersistence;
import org.xydra.sharedutils.ReflectionUtils;
import org.xydra.store.impl.memory.MemoryRuntime;


/**
 * A singleton for accessing platform specific variants of certain services such
 * as a cache which is a different thing on Google AppEngine and on plain Java.
 *
 * The initialisation process is triggered with the first call to
 * {@link #getPersistence(XId)}. The initialisation process runs in three
 * phases:
 * <ol>
 * <li>Let another class set a {@link XydraPlatformRuntime}</li>
 * <li>Try to load {@link #PLATFORM_CLASS} which must have a zero-argument
 * constructor and be an instance of {@link XydraPlatformRuntime}</li>
 * <li>Use an in-memory platform as a fall-back</li>
 * </ol>
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
    private static Map<XId,XydraPersistence> persistenceInstanceCache = new HashMap<XId,XydraPersistence>();

    private static boolean platformInitialised = false;

    private static XydraPlatformRuntime platformRuntime;

    /** Runtime configuration that can be set from outside */
    private static Map<String,String> configMap = new HashMap<String,String>();

    private static long lastTimeInitialisedAt = -1;

    /**
     * Allows to explicitly set a {@link XydraPlatformRuntime} to be used. This
     * is an alternative configuration approach compared to the RuntimeBinding.
     *
     * @param platformRuntime_ ..
     */
    public static synchronized void setPlatformRuntime(final XydraPlatformRuntime platformRuntime_) {
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
    public static synchronized void setPersistence(final XId repositoryId, final XydraPersistence persistence) {
        initialiseRuntimeOnce();
        persistenceInstanceCache.put(repositoryId, persistence);
    }

    /**
     * For tests, to make sure there is no static state
     */
    public static void hardReset() {
        XydraRuntime.configMap.clear();
        XydraRuntime.dynamicListeners.clear();
        XydraRuntime.lastTimeInitialisedAt = -1;
        XydraRuntime.persistenceInstanceCache.clear();
        XydraRuntime.platformInitialised = false;
        XydraRuntime.platformRuntime = null;
        XydraRuntime.staticListeners.clear();
    }

    private static synchronized void initialiseRuntimeOnce() {
        if(platformInitialised) {
            return;
        }
        lastTimeInitialisedAt = System.currentTimeMillis();
        // try to load dynamically
        try {
            try {
                final XydraPlatformRuntime xPlatformInstance = (XydraPlatformRuntime)ReflectionUtils
                        .createInstanceOfClass(PLATFORM_CLASS);
                platformRuntime = xPlatformInstance;
                platformInitialised = true;
            } catch(final ClassCastException e) {
                log.warn("Found the class with name " + PLATFORM_CLASS
                        + " but it is not implementing " + XydraPlatformRuntime.class, e);
            }
            log.info("Using platform " + platformRuntime.getName() + " loaded from "
                    + ReflectionUtils.getCanonicalName(platformRuntime.getClass()));
        } catch(final Exception e) {
            log.warn("Could no instantiate " + PLATFORM_CLASS);
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
    public static synchronized XydraPersistence getPersistence(final XId repositoryId) {
        initialiseRuntimeOnce();
        XydraPersistence persistence = persistenceInstanceCache.get(repositoryId);
        if(persistence == null) {
            /** Get basic instance */
            persistence = platformRuntime.createPersistence(repositoryId);
            /** Check if we are gathering stats */

            final String persistenceStatsStr = configMap.get(PROP_PERSISTENCESTATS);
            final boolean persistenceStats = persistenceStatsStr != null
                    && persistenceStatsStr.equalsIgnoreCase("true");
            if(persistenceStats) {
                /**
                 * Statistics can be accesses by getting the persistence and
                 * casting it to StatsPersistence
                 */
                final StatsGatheringPersistenceWrapper statsPersistence = new StatsGatheringPersistenceWrapper(
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
     *         {@link XydraPersistence} instances. Never null.
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
        persistenceInstanceCache.clear();
        platformInitialised = false;
        fireOnInitialisaion();
    }

    /**
     * @return the last time the XydraRuntime was initialised. Value is UTC
     *         milliseconds (Unix time). -1 if never initialised.
     */
    public static long getLastTimeInitialisedAt() {
        return lastTimeInitialisedAt;
    }

    /** A unique ID to distinguish several XydraRuntime instances */
    private static final String INSTANCE_ID = UUID.uuid(9);

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

    private static transient Set<Listener> staticListeners = new HashSet<Listener>();

    private static transient Set<Listener> dynamicListeners = new HashSet<Listener>();

    public static void addStaticListener(final Listener listener) {
        staticListeners.add(listener);
    }

    /**
     * Are notified only once.
     *
     * @param listener
     */
    public static void addDynamicListener(@NeverNull final Listener listener) {
        dynamicListeners.add(listener);
    }

    private static void fireOnInitialisaion() {
        for(final Listener l : staticListeners) {
            l.onXydraRuntimeInit();
        }
        for(final Listener l : dynamicListeners) {
            l.onXydraRuntimeInit();
        }
        dynamicListeners.clear();
    }

    public static void startRequest() {
        log.info("Request started.");
        if(platformRuntime != null) {
            platformRuntime.startRequest();
        }
        /*
         * Don't do anything if platform is not yet ready. Makes testing things
         * outside of servlet containers much easier.
         */
    }

    public static void finishRequest() {
        log.info("Request finished.");
        if(platformRuntime != null) {
            platformRuntime.finishRequest();
        } else {
            log.warn("Request finished, but XydraRuntime.init() was never directly or indirectly performed.");
        }
    }

    public static void init() {
        initialiseRuntimeOnce();
    }

}
