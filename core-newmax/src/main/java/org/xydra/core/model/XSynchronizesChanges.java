package org.xydra.core.model;

import org.xydra.base.IHasXAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XTransaction;
import org.xydra.core.change.XSendsSyncEvents;
import org.xydra.core.model.impl.memory.Synchronizable;


/**
 * An interface that indicates that this entity is able to execute
 * {@link XTransaction XTransactions} and synchronize remote changes.
 * 
 * @author Kaidel
 * @author dscharrer
 * 
 */
public interface XSynchronizesChanges extends IHasChangeLog,

XExecutesCommands,

IHasXAddress,

XSendsSyncEvents,

Synchronizable {
	
	/**
	 * @return the number of local changes that have not been synced with the
	 *         server yet. Can be used to estimate the duration of the sync
	 *         process or to devise heuristics when to sync.
	 */
	int countUnappliedLocalChanges();
	
	/**
	 * @return the actor that is represented by this interface. This is the
	 *         actor that is recorded for change operations. Operations will
	 *         only succeed if this actor has access.
	 */
	XId getSessionActor();
	
	/**
	 * @return the password used by Synchronizer to talk to the back-end
	 */
	String getSessionPasswordHash();
	
	/**
	 * @return the highest known revision number for which the client got a
	 *         confirmation about the servers state to have the same agreed-upon
	 *         history.
	 */
	long getSynchronizedRevision();
	
	/**
	 * Set a new actor to be used when building commands for changes to this
	 * entity and its children.
	 * 
	 * @param actorId for this entity and its children, if any.
	 * @param passwordHash the password for the given actor.
	 */
	void setSessionActor(XId actorId, String passwordHash);
	
}
