package org.xydra.store.impl.gae;

import org.xydra.base.XID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.IMemCache;
import org.xydra.store.XydraPlatformRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * @author xamde
 * 
 */
public class GaePlatformRuntime implements XydraPlatformRuntime {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(GaePlatformRuntime.class);
	
	@Override
	public synchronized IMemCache getMemCache() {
		return new GaeMemCache();
	}
	
	@Override
	public XydraPersistence getPersistence(XID repositoryId) {
		return new GaePersistence(repositoryId);
	}
	
}
