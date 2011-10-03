package org.xydra.store.impl.memory;

import org.xydra.base.XID;
import org.xydra.store.IMemCache;
import org.xydra.store.XydraPlatformRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;


public class MemoryRuntime implements XydraPlatformRuntime {
	
	@Override
	public IMemCache getMemCache() {
		return new LocalMemcache();
	}
	
	@Override
	public XydraPersistence getPersistence(XID repositoryId) {
		return new MemoryPersistence(repositoryId);
	}
	
	@Override
	public void finishRequest() {
	}
	
}
