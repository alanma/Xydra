package org.xydra.gae.admin;

import java.util.HashMap;
import java.util.Map;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.GaeAssert;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.impl.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * A simple map-like configuration object that can be put in datastore and
 * memcache. Has a 'time to live' to minimize data store hits.
 * 
 * @author xamde
 * 
 */
public class GaeConfiguration {
	
	private static final Logger log = LoggerFactory.getLogger(GaeConfiguration.class);
	
	public static final String PROP_VALID_UTC = "validUntilUTC";
	
	public static final String PROP_ASSERT = "assert";
	
	public static final String PROP_USEMEMCACHE = "usememcache";
	
	private static final long serialVersionUID = 1L;
	private static final Key KEY_CONF = KeyFactory.createKey("XCONF", "GaeConfig");
	
	/** first 60 seconds after boot this config is valid */
	public static final GaeConfiguration DEFAULT = GaeConfiguration.createWithLifetime(6 * 1000);
	
	public static final long CONFIG_APPLY_INTERVAL = 5 * 1000;
	
	static {
		DEFAULT.map().put(GaeConfiguration.PROP_ASSERT, "false");
		DEFAULT.map().put(GaeConfiguration.PROP_USEMEMCACHE, "true");
		// no PROP_CLEARMEMCACHE_NOW
		DEFAULT.map().put(XydraRuntime.PROP_MEMCACHESTATS, "false");
		DEFAULT.map().put(XydraRuntime.PROP_PERSISTENCESTATS, "false");
	}
	
	/** Internal state */
	private Map<String,String> map = new HashMap<String,String>();
	
	private transient long validUntilUTC;
	
	/**
	 * @param lifetimeInMs ..
	 * @return a {@link GaeConfiguration} that is valid for lifetimeInMs
	 *         milliseconds from now on.
	 */
	public static GaeConfiguration createWithLifetime(long lifetimeInMs) {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		GaeConfiguration conf = new GaeConfiguration();
		conf.setValidUntilUTC(System.currentTimeMillis() + lifetimeInMs);
		return conf;
	}
	
	/**
	 * Maybe found in memcache or data store. If coming from memcache and too
	 * old, a fresh one is fetched from data store and put in memcache.
	 * 
	 * @return a valid {@link GaeConfiguration}.
	 */
	public static synchronized GaeConfiguration getInstance() {
		GaeConfiguration conf = GaeConfiguration.load();
		if(conf == null) {
			/* no configuration set, using defaults */
			conf = GaeConfiguration.DEFAULT;
			store(conf);
		}
		assert conf != null;
		return conf;
	}
	
	private GaeConfiguration() {
	}
	
	public void setValidUntilUTC(long validUntilUTC) {
		this.map.put(PROP_VALID_UTC, "" + validUntilUTC);
		this.validUntilUTC = validUntilUTC;
	}
	
	/**
	 * @return if still valid. If not, a fresh configuration should be loaded
	 *         from memcache or datastore.
	 */
	public boolean isStillValid() {
		return System.currentTimeMillis() < this.validUntilUTC;
	}
	
	/**
	 * Persist given {@link GaeConfiguration} in data store
	 * 
	 * @param conf to be stored.
	 */
	public static void store(GaeConfiguration conf) {
		if(!conf.map.containsKey(PROP_VALID_UTC)) {
			throw new IllegalStateException("Missing " + PROP_VALID_UTC);
		}
		GaeUtils.putEntityAsync(conf.toEntity());
	}
	
	private Entity toEntity() {
		Entity entity = new Entity(KEY_CONF);
		for(String key : this.map.keySet()) {
			String value = this.map.get(key);
			entity.setUnindexedProperty(key, value);
		}
		return entity;
	}
	
	/**
	 * @return {@link GaeConfiguration} from data store.
	 */
	public static GaeConfiguration load() {
		Entity entity = GaeUtils.getEntity(KEY_CONF);
		if(entity == null) {
			log.warn("No gaeConfiguration in datastore.");
			return null;
		}
		GaeConfiguration conf = new GaeConfiguration();
		for(String key : entity.getProperties().keySet()) {
			String value = (String)entity.getProperty(key);
			conf.map.put(key, value);
		}
		if(!conf.map.containsKey(PROP_VALID_UTC)) {
			throw new IllegalStateException("Missing " + PROP_VALID_UTC);
		}
		conf.validUntilUTC = Long.parseLong(conf.map.get(PROP_VALID_UTC));
		if(!conf.isStillValid()) {
			log.warn("Freshly loaded GaeConfiguration is already out of date.");
		}
		return conf;
	}
	
	public long getTimeToLive() {
		return this.validUntilUTC - System.currentTimeMillis();
	}
	
	public long getValidUntilUTC() {
		return this.validUntilUTC;
	}
	
	/**
	 * @return the internal map
	 */
	public Map<String,String> map() {
		return this.map;
	}
	
	/**
	 * A null is interpreted as false.
	 * 
	 * @param propName ..
	 */
	public boolean getAsBoolean(String propName) {
		String value = this.map.get(propName);
		if(value == null)
			return false;
		return Boolean.parseBoolean(value);
	}
	
	/**
	 * Force a running instance to apply the current configuration to
	 * XydraRuntime.
	 * 
	 * This method should be called in every servlet handler. More precisely
	 * call <code>
	 * GaeConfigurationResource.getCurrentConfiguration().applyIfNecessary();
	 * </code>
	 * 
	 * The current configuration is distributed via data store, memcache and
	 * timeToLive. It it applied only by means of this method.
	 */
	public void applyIfNecessary() {
		long ago = System.currentTimeMillis() - XydraRuntime.getLastTimeInitialisedAt();
		if(ago >= CONFIG_APPLY_INTERVAL) {
			apply();
		}
	}
	
	public void apply() {
		// assertions
		boolean gaeAssert = getAsBoolean(PROP_ASSERT);
		GaeAssert.setEnabled(gaeAssert);
		// memcache
		boolean usememcache = getAsBoolean(PROP_USEMEMCACHE);
		GaeUtils.setUseMemCache(usememcache);
		// memcache stats
		boolean memcachestats = getAsBoolean(XydraRuntime.PROP_MEMCACHESTATS);
		setBooleanRuntimeConfigProperty(XydraRuntime.PROP_MEMCACHESTATS, memcachestats);
		// memcache stats
		boolean persistencestats = getAsBoolean(XydraRuntime.PROP_PERSISTENCESTATS);
		setBooleanRuntimeConfigProperty(XydraRuntime.PROP_PERSISTENCESTATS, persistencestats);
	}
	
	private static void setBooleanRuntimeConfigProperty(String property, boolean newValue) {
		String s = XydraRuntime.getConfigMap().get(property);
		boolean currentValue = Boolean.parseBoolean(s);
		if(newValue != currentValue) {
			XydraRuntime.getConfigMap().put(property, newValue + "");
			XydraRuntime.forceReInitialisation();
		}
	}
}
