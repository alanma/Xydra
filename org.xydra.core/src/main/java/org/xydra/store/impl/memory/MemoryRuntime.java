package org.xydra.store.impl.memory;

import org.xydra.base.XId;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.IMemCache;
import org.xydra.store.XydraPlatformRuntime;


public class MemoryRuntime implements XydraPlatformRuntime {
	
	private static final Logger log = LoggerFactory.getLogger(MemoryRuntime.class);
	
	@Override
	public IMemCache getMemCache() {
		return new LocalMemcache();
	}
	
	@Override
	public XydraPersistence getPersistence(XId repositoryId) {
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
