package org.xydra.gae.admin;

import java.util.HashMap;
import java.util.Map;

import org.xydra.store.IMemCache;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.impl.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * A simple map-like configuration object that can be put in datastore and
 * memcache. Has a time to live to minimize data store hits.
 * 
 * @author xamde
 * 
 */
public class GaeConfiguration {
	
	public static final String PROP_VALID_UTC = "validUntilUTC";
	
	private static final long serialVersionUID = 1L;
	private static final Key KEY_CONF = KeyFactory.createKey("XCONF", "GaeConfig");
	
	private Map<String,String> map = new HashMap<String,String>();
	
	private transient long validUntilUTC;
	
	public static GaeConfiguration createWithLifetime(long lifetimeInMs) {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		GaeConfiguration conf = new GaeConfiguration();
		conf.setValidUntilUTC(System.currentTimeMillis() + lifetimeInMs);
		return conf;
	}
	
	/**
	 * @return a {@link GaeConfiguration} found in memcache or data store. If
	 *         coming from memcache and too old, a fresh one is fetched.
	 */
	public static GaeConfiguration getInstance() {
		IMemCache memcache = XydraRuntime.getMemcache();
		GaeConfiguration conf = (GaeConfiguration)memcache.get(KEY_CONF);
		if(conf == null || !conf.isStillValid()) {
			// get fresh
			conf = GaeConfiguration.load();
			// store in memcache
			memcache.put(KEY_CONF, conf);
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
	
	public boolean isStillValid() {
		return System.currentTimeMillis() < this.validUntilUTC;
	}
	
	public static void store(GaeConfiguration conf) {
		if(!conf.map.containsKey(PROP_VALID_UTC)) {
			throw new IllegalStateException("Missing " + PROP_VALID_UTC);
		}
		Entity entity = new Entity(KEY_CONF);
		for(String key : conf.map.keySet()) {
			String value = conf.map.get(key);
			entity.setUnindexedProperty(key, value);
		}
		GaeUtils.putEntityAsync(entity);
	}
	
	public static GaeConfiguration load() {
		Entity entity = GaeUtils.getEntity(KEY_CONF);
		GaeConfiguration conf = new GaeConfiguration();
		for(String key : entity.getProperties().keySet()) {
			String value = (String)entity.getProperty(key);
			conf.map.put(key, value);
		}
		if(!conf.map.containsKey(PROP_VALID_UTC)) {
			throw new IllegalStateException("Missing " + PROP_VALID_UTC);
		}
		conf.validUntilUTC = Long.parseLong(conf.map.get(PROP_VALID_UTC));
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
	
}
