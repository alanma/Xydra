package org.xydra.server.impl;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RequiresAppEngine;


@RunsInAppEngine(true)
@RequiresAppEngine(false)
public interface IInfrastructureProvider {
	
	IMemCache createMemCache();
	
}
