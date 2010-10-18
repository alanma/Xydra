package org.xydra.server;

import java.util.Iterator;

import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;


/**
 * A back-end server interface. Not REST-related itself, but used by REST-aware
 * components.
 * 
 * @author voelkel
 */
public interface IXydraServer extends Iterable<XID> {
	
	XAddress getRepositoryAddress();
	
	/**
	 * @return a read-only interface to the snapshot for the model this time.
	 *         Individual parts of the model are loaded into memory as-needed.
	 * 
	 *         TODO what is returned if there is no XModel with the given id?
	 */
	XBaseModel getModelSnapshot(XID modelId);
	
	/**
	 * @return an interface to read the change log entries for the given model.
	 */
	XChangeLog getChangeLog(XID modelId);
	
	/**
	 * Execute the given command and log the actorId. No
	 * authentication/authorization checks are done. TODO replace actorId with a
	 * more general context to allow logging time, IP Address, etc.
	 */
	long executeCommand(XCommand command, XID actorId);
	
	/**
	 * @return the group database for this server.
	 */
	XGroupDatabase getGroups();
	
	/**
	 * @return the {@link XAccessManager} responsible for accesses to the
	 *         repository.
	 */
	XAccessManager getAccessManager();
	
	/**
	 * Get the available model IDs.
	 * 
	 * Implements {@link Iterable} interface.
	 * 
	 * TODO Iterators are problematic with synchronization
	 */
	Iterator<XID> iterator();
	
}
