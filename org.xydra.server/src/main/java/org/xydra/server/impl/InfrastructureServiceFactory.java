package org.xydra.server.impl;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.server.impl.memory.SimpleInfrastructureProvider;


/**
 * A factory for back-end infrastructure services.
 * 
 * Acts as an {@link IMemCache} factory.
 * 
 * @author voelkel
 * 
 */
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class InfrastructureServiceFactory {
	
	private static final Logger log = LoggerFactory.getLogger(InfrastructureServiceFactory.class);
	
	private static IInfrastructureProvider provider = new SimpleInfrastructureProvider();
	private static IMemCache memcache;
	
	/**
	 * This method needs to be called by other infrastructure environments to
	 * register their provider.
	 * 
	 * If set, all calls are delegates to the given delegate. Otherwise local
	 * implementations are used.
	 * 
	 * @param provider_ Set to null to remove the provider and fall back to the
	 *            built-in provider.
	 */
	public static synchronized void setProvider(IInfrastructureProvider provider_) {
		provider = provider_;
		if(provider == null) {
			provider = new SimpleInfrastructureProvider();
		}
		
		if(memcache != null) {
			log
			        .warn("Changing infrastructure provider after services have been used. This is likely leads to inconsistencies!");
		}
		
		// reset instances
		memcache = null;
	}
	
	/**
	 * @return the {@link IMemCache} singleton.
	 */
	public static synchronized IMemCache getMemCache() {
		if(memcache == null) {
			memcache = provider.createMemCache();
		}
		return memcache;
	}
	
}
