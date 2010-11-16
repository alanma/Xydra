package org.xydra.server;

import java.util.Iterator;

import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabaseWithListeners;
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
	 *         Returns null if no such model exists.
	 * 
	 *         TODO Models may not actually be loaded lazyly, better to leave
	 *         this up to the implementation. ~Daniel
	 */
	XBaseModel getModelSnapshot(XID modelId);
	
	/**
	 * @return an interface to read the change log entries for the given model.
	 *         This may be null if the model doesn't exist.
	 */
	XChangeLog getChangeLog(XID modelId);
	
	/**
	 * Execute the given command and log the actorId. No
	 * authentication/authorization checks are done.
	 * 
	 * TODO replace actorId with a more general context to allow logging time,
	 * IP Address, etc.
	 * 
	 * @return TODO document die long return value verhält sich wie bei allen
	 *         anderen #executeCommand methoden in core (z.B. in XModel) ->
	 *         XCommand#FAILED für fehler, XCommand#NOCHANGE wenn sich nichts
	 *         geändert hat, revision number sonst
	 */
	long executeCommand(XCommand command, XID actorId);
	
	/**
	 * @return the group database for this server.
	 */
	XGroupDatabaseWithListeners getGroups();
	
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
