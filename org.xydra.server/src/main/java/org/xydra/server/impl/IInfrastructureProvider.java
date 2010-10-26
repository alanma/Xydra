package org.xydra.server.impl;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInJava;


@RunsInAppEngine
@RunsInJava
public interface IInfrastructureProvider {
	
	IMemCache createMemCache();
	
}
