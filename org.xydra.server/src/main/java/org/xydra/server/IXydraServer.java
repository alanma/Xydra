package org.xydra.server;

import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.model.XRepository;


/**
 * A back-end server interface. Not REST-related itself, but used by REST-aware
 * components.
 * 
 * @author voelkel
 */
public interface IXydraServer {
	
	/**
	 * @return the {@link XRepository} which contains the data of this server.
	 */
	XRepository getRepository();
	
	/**
	 * @return the group database for this server.
	 */
	XGroupDatabase getGroups();
	
	/**
	 * @return the {@link XAccessManager} responsible for accesses to the
	 *         repository.
	 */
	XAccessManager getAccessManager();
	
}
