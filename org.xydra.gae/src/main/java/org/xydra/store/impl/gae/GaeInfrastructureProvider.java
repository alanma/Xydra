package org.xydra.store.impl.gae;

import org.xydra.server.impl.IGaeMemCache;
import org.xydra.server.impl.IInfrastructureProvider;


@SuppressWarnings("deprecation")
@Deprecated
public class GaeInfrastructureProvider implements IInfrastructureProvider {
	
	@Override
	public IGaeMemCache createMemCache() {
		return new GaeMemCacheUnused();
	}
	
}
