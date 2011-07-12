package org.xydra.store.platform;

import org.xydra.store.XydraPlatformRuntime;
import org.xydra.store.impl.gae.GaePlatformRuntime;


/**
 * Class provided for configuration of {@link XydraPlatformRuntime} via class
 * path. Must provide a parameter-less constructor.
 * 
 * @author xamde
 * 
 */
public class RuntimeBinding extends GaePlatformRuntime implements XydraPlatformRuntime {
	
}
