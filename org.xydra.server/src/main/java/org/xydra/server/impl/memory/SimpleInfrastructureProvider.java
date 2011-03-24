package org.xydra.server.impl.memory;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.server.impl.IInfrastructureProvider;
import org.xydra.server.impl.IMemCache;


@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class SimpleInfrastructureProvider implements IInfrastructureProvider {
	
	@Override
	public IMemCache createMemCache() {
		return new MemCacheImpl();
	}
	
}
