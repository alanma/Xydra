package org.xydra.store.impl.gae;

import java.util.Map;

import org.xydra.base.XID;
import org.xydra.gae.AboutAppEngine;
import org.xydra.gae.admin.GaeConfigSettings;
import org.xydra.gae.admin.GaeConfiguration;
import org.xydra.gae.admin.GaeConfigurationManager;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.IMemCache;
import org.xydra.store.XydraConfigUtils;
import org.xydra.store.XydraPlatformRuntime;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.memory.LocalMemcache;


/**
 * GAE implementation of {@link XydraPlatformRuntime}.
 * 
 * Maps memcache to Google AppEngine memcache service; {@link XydraPersistence}
 * to Google AppEngine data store service.
 */
public class GaePlatformRuntime implements XydraPlatformRuntime {
	
	private static final Logger log = LoggerFactory.getLogger(GaePlatformRuntime.class);
	
	static {
		log.info("Configuring default gae conf");
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		/* Set default values for GaeConf */
		GaeConfiguration defaultConf = GaeConfigurationManager.getDefaultConfiguration();
		defaultConf.map().put(GaeConfigSettings.PROP_ASSERT, "");
		defaultConf.map().put(GaeConfigSettings.PROP_USEMEMCACHE, "true");
		// no PROP_CLEARMEMCACHE_NOW
		defaultConf.map().put(XydraRuntime.PROP_MEMCACHESTATS, "");
		defaultConf.map().put(XydraRuntime.PROP_PERSISTENCESTATS, "");
		
		XydraRuntime.addStaticListener(new XydraRuntime.Listener() {
			
			@Override
			public void onXydraRuntimeInit() {
				// gae assertions
				boolean gaeAssert = XydraRuntime.getConfigMap().get(GaeConfigSettings.PROP_ASSERT) != null;
				GaeAssert.setEnabled(gaeAssert);
				// memcache
				boolean usememcache = XydraRuntime.getConfigMap().get(
				        GaeConfigSettings.PROP_USEMEMCACHE) != null;
				Memcache.setUseMemCache(usememcache);
			}
		});
		
		/* register for config changes */
		GaeConfigurationManager.addListener(new GaeConfigurationManager.Listener() {
			
			@Override
			public void onChange(GaeConfiguration conf) {
				/* Apply the diff */
				boolean requiresRuntimeInit = false;
				Map<String,String> changes = XydraConfigUtils.getChanges(
				        XydraRuntime.getConfigMap(), conf.map());
				for(String key : changes.keySet()) {
					String value = XydraConfigUtils.normalizeValue(changes.get(key));
					if(value.equals(XydraConfigUtils.EMPTY_VALUE)) {
						// remove
						log.info("Instance conf: remove key '" + key + "'");
						XydraRuntime.getConfigMap().remove(key);
					} else {
						// change or add
						log.info("Instance conf: set key '" + key + "' = '" + value + "'");
						XydraRuntime.getConfigMap().put(key, value);
						// process individual gae instance config settings
						handleClearLocalVmCache(key, value);
					}
					requiresRuntimeInit |= key.equals(XydraRuntime.PROP_MEMCACHESTATS);
					requiresRuntimeInit |= key.equals(XydraRuntime.PROP_PERSISTENCESTATS);
					requiresRuntimeInit |= key.equals(XydraRuntime.PROP_USEMEMCACHE);
					
				}
				
				if(requiresRuntimeInit) {
					log.info("Changes require a XydraRuntime re-init");
					InstanceContext.clear();
					XydraRuntime.forceReInitialisation();
				}
			}
			
			private void handleClearLocalVmCache(String key, String value) {
				if(!key.equals(GaeConfigSettings.CLEAR_LOCAL_VM_CACHE)) {
					return;
				}
				assert value != null;
				assert !value.equals(XydraConfigUtils.EMPTY_VALUE);
				
				String lastExecutedStr = XydraRuntime.getConfigMap().get(
				        GaeConfigSettings.CLEAR_LOCAL_VM_CACHE_LAST_EXECUTED);
				long lastExecuted = lastExecutedStr == null ? 0 : Long.parseLong(lastExecutedStr);
				long clearRequested = Long.parseLong(value);
				if(lastExecuted < clearRequested) {
					log.info("clearLocalVmCache requested with Nr. " + clearRequested
					        + " lastExecuted: " + lastExecuted);
					InstanceContext.clear();
					// prevent executing twice
					XydraRuntime.getConfigMap().put(
					        GaeConfigSettings.CLEAR_LOCAL_VM_CACHE_LAST_EXECUTED,
					        "" + clearRequested);
				} else {
					log.info("No clearLocalVmCache necessary. lastExecuted = " + lastExecuted);
				}
			}
		});
	}
	
	@Override
	public synchronized IMemCache getMemCache() {
		log.info("INIT IMemcache instance.");
		if(AboutAppEngine.onAppEngine()) {
			return new GaeLowLevelMemCache();
		} else {
			return new LocalMemcache();
		}
	}
	
	@Override
	public XydraPersistence getPersistence(XID repositoryId) {
		log.info("INIT XydraPersistence instance with id '" + repositoryId + "'.");
		return new GaePersistence(repositoryId);
	}
	
	@Override
	public void finishRequest() {
		log.info("Request finished.");
		// InstanceContext.clearThreadContext();
	}
	
	// public static interface XRequestListener {
	// void onRequestFinish();
	// }
	//
	// private static Set<XRequestListener> requestlisteners = new
	// HashSet<XRequestListener>();
	//
	// public static void addRequestListener(XRequestListener listener) {
	// synchronized(requestlisteners) {
	// requestlisteners.add(listener);
	// }
	// }
	//
	// public static void removeRequestListener(XRequestListener listener) {
	// synchronized(requestlisteners) {
	// requestlisteners.remove(listener);
	// }
	// }
	
	@Override
	public void startRequest() {
		log.info("Request started.");
	}
	
	@Override
	public String getName() {
		return "GAE-" + AboutAppEngine.inModeAsString();
	}
}
