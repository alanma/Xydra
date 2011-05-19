package org.xydra.server.impl;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;


@RunsInAppEngine(true)
@RequiresAppEngine(false)
@Deprecated
public interface IInfrastructureProvider {
	
	IGaeMemCache createMemCache();
	
}
