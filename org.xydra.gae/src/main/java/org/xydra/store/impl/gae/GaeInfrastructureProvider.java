package org.xydra.store.impl.gae;

import org.xydra.server.impl.IInfrastructureProvider;
import org.xydra.server.impl.IMemCache;


public class GaeInfrastructureProvider implements IInfrastructureProvider {
	
	@Override
	public IMemCache createMemCache() {
		return new GaeMemCache();
	}
	
}
