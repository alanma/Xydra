package org.xydra.server.rest;

import org.xydra.core.model.XID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.server.IXydraServer;
import org.xydra.server.impl.memory.MemoryXydraServer;


/**
 * This class should only be used in tests or Java-only settings where no
 * servlet environments are involved. Servlet containers do not maintain static
 * variables across the servlet life-cycle -- at least they don't guarantee it.
 * 
 * @author voelkel
 */
public class XydraServerDefaultConfiguration {
	
	private static final Logger log = LoggerFactory
	        .getLogger(XydraServerDefaultConfiguration.class);
	
	private static IXydraServer defaultXydraServer_;
	
	/**
	 * @param xydraServer use null to unregister
	 */
	public synchronized static void setDefaultXydraServer(IXydraServer xydraServer) {
		/*
		 * FIXME "use null to unregister" will not work, as the
		 * IllegalStateException is thrown when a default server is set
		 * regardless of the parameter - add "&& xydraServer != null" to the if
		 * clause or better add a separate unregister function to avoid
		 * accidentally un-setting?
		 */
		if(defaultXydraServer_ != null) {
			throw new IllegalStateException("the xydraServer can only be set once");
		}
		defaultXydraServer_ = xydraServer;
	}
	
	/**
	 * @param actorId TODO
	 * @return a new instance of the default in-memory server
	 */
	public synchronized static IXydraServer getInMemoryServer(XID actorId) {
		if(defaultXydraServer_ == null) {
			IXydraServer builtInServer = new MemoryXydraServer(actorId);
			log.warn("No IXydraServer has been registered, using default server = "
			        + builtInServer.getClass().getCanonicalName());
			defaultXydraServer_ = builtInServer;
		}
		return defaultXydraServer_;
	}
	
	public static boolean isConfigured() {
		return defaultXydraServer_ != null;
	}
	
}
