package org.xydra.store.impl.gae;

import java.util.Map;

import org.xydra.annotations.Setting;
import org.xydra.base.XId;
import org.xydra.gae.admin.GaeConfigSettings;
import org.xydra.gae.admin.GaeConfiguration;
import org.xydra.gae.admin.GaeConfigurationManager;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.XydraConfigUtils;
import org.xydra.store.XydraPlatformRuntime;
import org.xydra.store.XydraRuntime;
import org.xydra.xgae.XGae;

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

		/* Set default values for GaeConf */
		@Setting(value = "")
		final
		GaeConfiguration defaultConf = GaeConfigurationManager.getDefaultConfiguration();
		defaultConf.map().put(GaeConfigSettings.PROP_ASSERT, "true");
		defaultConf.map().put(GaeConfigSettings.PROP_USEMEMCACHE, "true");
		// no PROP_CLEARMEMCACHE_NOW
		defaultConf.map().put(XydraRuntime.PROP_MEMCACHESTATS, "");
		defaultConf.map().put(XydraRuntime.PROP_PERSISTENCESTATS, "");

		XydraRuntime.addStaticListener(new XydraRuntime.Listener() {

			@Override
			public void onXydraRuntimeInit() {
				// FIXME config setup in test works not all, configMap is always
				// empty

				// gae assertions
				// Map<String,String> configMap = XydraRuntime.getConfigMap();
				// boolean gaeAssert =
				// configMap.get(GaeConfigSettings.PROP_ASSERT) != null;
				// FIXME GAE... XyAssert.setEnabled(gaeAssert);
				// memcache
				final boolean usememcache = XydraRuntime.getConfigMap().get(
						GaeConfigSettings.PROP_USEMEMCACHE) != null;
				Memcache.setUseMemCache(usememcache);
			}
		});

		/* register for config changes */
		GaeConfigurationManager.addListener(new GaeConfigurationManager.Listener() {

			@Override
			public void onChange(final GaeConfiguration conf) {
				/* Apply the diff */
				boolean requiresRuntimeInit = false;
				final Map<String, String> changes = XydraConfigUtils.getChanges(
						XydraRuntime.getConfigMap(), conf.map());
				for (final String key : changes.keySet()) {
					final String value = XydraConfigUtils.normalizeValue(changes.get(key));
					if (value.equals(XydraConfigUtils.EMPTY_VALUE)) {
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

				if (requiresRuntimeInit) {
					log.info("Changes require a XydraRuntime re-init");
					InstanceContext.clear();
					XydraRuntime.forceReInitialisation();
				}
			}

			private void handleClearLocalVmCache(final String key, final String value) {
				if (!key.equals(GaeConfigSettings.CLEAR_LOCAL_VM_CACHE)) {
					return;
				}
				assert value != null;
				assert !value.equals(XydraConfigUtils.EMPTY_VALUE);

				final String lastExecutedStr = XydraRuntime.getConfigMap().get(
						GaeConfigSettings.CLEAR_LOCAL_VM_CACHE_LAST_EXECUTED);
				final long lastExecuted = lastExecutedStr == null ? 0 : Long.parseLong(lastExecutedStr);
				final long clearRequested = Long.parseLong(value);
				if (lastExecuted < clearRequested) {
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
	public XydraPersistence createPersistence(final XId repositoryId) {
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
		return "GAE-" + XGae.get().inModeAsString();
	}
}
