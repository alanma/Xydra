package org.xydra.store.impl.memory;

import org.xydra.base.XID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.IMemCache;
import org.xydra.store.XydraPlatformRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;


public class MemoryRuntime implements XydraPlatformRuntime {
	
	private static final Logger log = LoggerFactory.getLogger(MemoryRuntime.class);
	
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
		log.info("Request finished.");
	}
	
	@Override
	public void startRequest() {
		log.info("Request started.");
		// InstanceContext.clearThreadContext();
	}
	
	@Override
	public String getName() {
		return "MemoryRuntime";
	}
	
}
