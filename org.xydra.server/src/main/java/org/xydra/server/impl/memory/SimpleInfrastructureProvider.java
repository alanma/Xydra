package org.xydra.server.impl.memory;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.server.impl.IGaeMemCache;
import org.xydra.server.impl.IInfrastructureProvider;


@SuppressWarnings("deprecation")
@RunsInAppEngine(true)
@RequiresAppEngine(false)
@Deprecated
public class SimpleInfrastructureProvider implements IInfrastructureProvider {
	
	@Override
	public IGaeMemCache createMemCache() {
		return new MemCacheImpl();
	}
	
}
