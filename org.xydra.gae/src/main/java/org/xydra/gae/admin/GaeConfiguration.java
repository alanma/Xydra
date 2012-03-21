package org.xydra.gae.admin;

import java.util.HashMap;
import java.util.Map;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.gae.AsyncDatastore;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.impl.gae.SyncDatastore;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * Configuration data for all GAE application instances.
 * 
 * A simple map-like configuration object that can be put in datastore and
 * memcache. Has a 'time to live' to minimise data store hits.
 * 
 * This config information can be manipulated via the
 * {@link GaeConfigurationResource}.
 * 
 * It is the responsibility of each instance to periodically call <code>
 * GaeConfigurationManager.getCurrentConfiguration().applyIfNecessary();
 * </code>
 * 
 * @author xamde
 */
public class GaeConfiguration {
	
	private static final Logger log = LoggerFactory.getLogger(GaeConfiguration.class);
	
	public static final String PROP_VALID_UTC = "validUntilUTC";
	
	public static final long serialVersionUID = 1L;
	
	private static final Key getConfKey() {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		if(KEY_CFG == null) {
			KEY_CFG = KeyFactory.createKey("XCONF", "GaeConfig");
		}
		return KEY_CFG;
	}
	
	private static Key KEY_CFG;
	
	/** Internal state */
	private Map<String,String> map = new HashMap<String,String>();
	
	private transient long validUntilUTC;
	
	/**
	 * Compile-time flag to disable or enable any usage of static variables
	 * (local VM-wide caching)
	 */
	public static final boolean USE_LOCALVMCACHE = false;
	
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
	
	private GaeConfiguration() {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
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
	 * Persist this {@link GaeConfiguration} in data store
	 */
	public void store() {
		assertConsistetState();
		AsyncDatastore.putEntity(toEntity());
	}
	
	public void assertConsistetState() {
		if(!this.map.containsKey(PROP_VALID_UTC)) {
			throw new IllegalStateException("Missing " + PROP_VALID_UTC);
		}
	}
	
	private Entity toEntity() {
		Entity entity = new Entity(getConfKey());
		for(String key : this.map.keySet()) {
			String value = this.map.get(key);
			entity.setUnindexedProperty(key, value);
		}
		return entity;
	}
	
	/**
	 * @return {@link GaeConfiguration} from data store. It might be out of
	 *         date.
	 */
	public static GaeConfiguration load() {
		log.info("Load conf from data store. It might be out of date.");
		Entity entity = SyncDatastore.getEntity(getConfKey());
		if(entity == null) {
			log.warn("No gaeConfiguration in datastore.");
			return null;
		}
		// else:
		GaeConfiguration conf = new GaeConfiguration();
		for(String key : entity.getProperties().keySet()) {
			String value = (String)entity.getProperty(key);
			conf.map.put(key, value);
		}
		conf.assertConsistetState();
		try {
			conf.validUntilUTC = Long.parseLong(conf.map.get(PROP_VALID_UTC));
		} catch(NumberFormatException e) {
			log.warn("Bad config in store, setting time to live to 15 minutes");
			conf.validUntilUTC = System.currentTimeMillis() + (15 * 60 * 1000);
		}
		return conf;
	}
	
	/**
	 * @return how many ms is this config still valid
	 */
	public long getTimeToLive() {
		return this.validUntilUTC - System.currentTimeMillis();
	}
	
	/**
	 * @return UTC time-point when this config expires
	 */
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
	 * @return the property value as boolean value
	 */
	public boolean getAsBoolean(String propName) {
		String value = this.map.get(propName);
		if(value == null)
			return false;
		return Boolean.parseBoolean(value);
	}
	
	public void setLifetime(long ms) {
		setValidUntilUTC(System.currentTimeMillis() + ms);
	}
}
