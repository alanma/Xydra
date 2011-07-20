package org.xydra.store.impl.gae;

import org.xydra.base.XID;
import org.xydra.gae.AboutAppEngine;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.IMemCache;
import org.xydra.store.XydraPlatformRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * GAE implementation of {@link XydraPlatformRuntime}.
 * 
 * Maps memcache to Google AppEngine memcache service; {@link XydraPersistence}
 * to data store service.
 */
public class GaePlatformRuntime implements XydraPlatformRuntime {
	
	private static final Logger log = LoggerFactory.getLogger(GaePlatformRuntime.class);
	
	@Override
	public synchronized IMemCache getMemCache() {
		log.info("Instantiating a new IMemcache instance.");
		if(AboutAppEngine.inProduction()) {
			return new GaeLowLevelMemCache();
		} else {
			return new LocalMemcache();
		}
	}
	
	@Override
	public XydraPersistence getPersistence(XID repositoryId) {
		log.info("Instantiating a new XydraPersistence instance with id '" + repositoryId + "'.");
		return new GaePersistence(repositoryId);
	}
	
}
