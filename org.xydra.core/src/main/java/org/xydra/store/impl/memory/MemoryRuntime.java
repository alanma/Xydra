package org.xydra.store.impl.memory;

import java.util.HashMap;
import java.util.Map;

import org.xydra.base.XID;
import org.xydra.store.XydraPlatformRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;


public class MemoryRuntime implements XydraPlatformRuntime {
	
	@Override
	public Map<Object,Object> getMemCache() {
		return new HashMap<Object,Object>();
	}
	
	@Override
	public XydraPersistence getPersistence(XID repositoryId) {
		return new MemoryPersistence(repositoryId);
	}
	
}
