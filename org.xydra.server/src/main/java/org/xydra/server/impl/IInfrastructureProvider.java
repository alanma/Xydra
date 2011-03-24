package org.xydra.server.impl;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;


@RunsInAppEngine(true)
@RequiresAppEngine(false)
public interface IInfrastructureProvider {
	
	IMemCache createMemCache();
	
}
