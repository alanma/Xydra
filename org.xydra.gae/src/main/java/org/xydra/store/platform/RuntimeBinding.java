package org.xydra.store.platform;

import org.xydra.base.XID;
import org.xydra.store.IMemCache;
import org.xydra.store.XydraPlatformRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaePlatformRuntime;


public class RuntimeBinding implements XydraPlatformRuntime {
	
	GaePlatformRuntime instance = null;
	
	@Override
	public IMemCache getMemCache() {
		init();
		return this.instance.getMemCache();
	}
	
	private synchronized void init() {
		if(this.instance == null) {
			this.instance = new GaePlatformRuntime();
		}
	}
	
	@Override
	public XydraPersistence getPersistence(XID repositoryId) {
		init();
		return this.instance.getPersistence(repositoryId);
	}
	
}
