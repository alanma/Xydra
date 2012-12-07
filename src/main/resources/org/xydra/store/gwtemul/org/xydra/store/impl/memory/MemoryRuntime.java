package org.xydra.store.impl.memory;

import org.xydra.base.XID;
import org.xydra.store.IMemCache;
import org.xydra.store.XydraPlatformRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;


public class MemoryRuntime implements XydraPlatformRuntime {
	
	@Override
	public IMemCache getMemCache() {
		throw new RuntimeException("Using a memcache in GWT makes no sense");
	}
	
	@Override
	public XydraPersistence getPersistence(XID repositoryId) {
		return new MemoryPersistence(repositoryId);
	}
	
	@Override
	public void finishRequest() {
	}
	
	@Override
	public void startRequest() {
	}
	
	@Override
	public String getName() {
		return "MemoryRuntime-GWTEmul";
	}
	
}
