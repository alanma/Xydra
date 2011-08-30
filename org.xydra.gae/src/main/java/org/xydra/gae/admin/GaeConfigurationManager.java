package org.xydra.gae.admin;

import java.util.HashSet;
import java.util.Set;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * A singleton for managing the {@link GaeConfiguration} on each instance.
 * 
 * Every servlet handler should call
 * {@link GaeConfigurationManager#assertValidGaeConfiguration()}
 * 
 * @author xamde
 * 
 */
public class GaeConfigurationManager {
	
	private static final Logger log = LoggerFactory.getLogger(GaeConfigurationManager.class);
	
	private static GaeConfiguration currentConf = null;
	
	private static final long ONE_MINUTE = 60 * 1000;
	
	/**
	 * The default configuration is valid for 60 seconds after instance boot.
	 */
	private static GaeConfiguration defaultConfiguration = GaeConfiguration
	        .createWithLifetime(ONE_MINUTE);
	
	/**
	 * @return the default configuration. Changes to this config are effective.
	 */
	public static GaeConfiguration getDefaultConfiguration() {
		defaultConfiguration.setLifetime(ONE_MINUTE);
		assert defaultConfiguration.isStillValid();
		return defaultConfiguration;
	}
	
	/**
	 * @return a valid {@link GaeConfiguration}. Current config is reused if
	 *         present and still valid; otherwise a fresh config is obtained.
	 */
	public static synchronized GaeConfiguration getCurrentConfiguration() {
		if(currentConf == null) {
			log.info("Current config is null. Getting one.");
			currentConf = loadConfigOrUseDefaults();
			fireOnChange(currentConf);
		} else if(!currentConf.isStillValid()) {
			log.info("Current config is too old. Getting a fresh one.");
			currentConf = loadConfigOrUseDefaults();
			fireOnChange(currentConf);
		} else {
			// config present and valid. no changes.
		}
		assert currentConf.isStillValid() : "freshly loaded config is out of date";
		return currentConf;
	}
	
	/**
	 * Maybe found in memcache or data store. If coming from memcache and too
	 * old, a fresh one is fetched from data store and put in memcache.
	 * 
	 * @return a valid {@link GaeConfiguration}.
	 */
	public static synchronized GaeConfiguration loadConfigOrUseDefaults() {
		GaeConfiguration conf = GaeConfiguration.load();
		if(conf == null) {
			log.info("No configuration found in backend."
			        + " Using defaults and writing them to backend");
			conf = getDefaultConfiguration();
			conf.store();
		} else {
			log.info("Loaded config from backend.");
			if(!conf.isStillValid()) {
				log.info("Config was no longer valid. Extending lifetime for 1 minute. Persisting that.");
				conf.setLifetime(ONE_MINUTE);
				conf.store();
			}
		}
		assert conf != null;
		assert conf.isStillValid();
		return conf;
	}
	
	/**
	 * Makes sure current config is valid.
	 * 
	 * Force a running instance to apply the current configuration to
	 * XydraRuntime.
	 * 
	 * 
	 * The current configuration is distributed via data store, memcache and
	 * timeToLive. It is applied only by means of this method.
	 */
	public static void assertValidGaeConfiguration() {
		getCurrentConfiguration();
	}
	
	public static interface Listener {
		/**
		 * Called whenever the GaeConfiguration changes (i.e. is reloaded from
		 * back-end).
		 * 
		 * @param conf
		 */
		void onChange(GaeConfiguration conf);
	}
	
	private static transient Set<Listener> listeners = new HashSet<Listener>();
	
	public static void addListener(Listener listener) {
		listeners.add(listener);
	}
	
	public static void fireOnChange(GaeConfiguration conf) {
		for(Listener l : listeners) {
			l.onChange(conf);
		}
	}
	
}
