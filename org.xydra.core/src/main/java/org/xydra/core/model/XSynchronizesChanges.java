package org.xydra.core.model;

import org.xydra.annotations.ModificationOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XTransaction;


/**
 * An interface that indicates that this entity is able to execute
 * {@link XTransaction XTransactions} and synchronize remote changes.
 * 
 * @author Kaidel
 * @author dscharrer
 * 
 */
public interface XSynchronizesChanges extends XExecutesCommands, IHasXAddress {
	
	/**
	 * Roll back the state (including revisions) to a specific revision. This
	 * will erase all {@link XEvent XEvents} following this revision from the
	 * {@link XChangeLog} of this XSynchronizesChanges. Listeners that were/are
	 * registered to the entities that are manipulated by this rollback are not
	 * automatically restored or removed, but {@link XEvent XEvents} are sent
	 * out for all changes made.
	 * 
	 * @param revision The revision number to which will be rolled back
	 * @throws IllegalStateException if this entity has already been removed
	 */
	void rollback(long revision);
	
	/**
	 * TODO document
	 * 
	 * @param remoteChanges The remote changes that happened since the last
	 *            Synchronization, including local changes that have been saved
	 *            remotely.
	 */
	void synchronize(XEvent[] remoteChanges);
	
	/**
	 * @return the {@link XChangeLog} which is logging the {@link XEvent
	 *         XEvents} which happen on this XSynchronizeChanges
	 */
	XChangeLog getChangeLog();
	
	/**
	 * @return the actor that is represented by this interface. This is the
	 *         actor that is recorded for change operations. Operations will
	 *         only succeed if this actor has access.
	 */
	XID getSessionActor();
	
	/**
	 * Set a new actor to be used when building commands for changes to this
	 * entity and its children.
	 * 
	 * @param actorId for this entity and its children, if any.
	 * @param passwordHash the password for the given actor.
	 */
	void setSessionActor(XID actorId, String passwordHash);
	
	// TODO document
	XLocalChange[] getLocalChanges();
	
	/**
	 * Execute the given {@link XCommand} if possible.
	 * 
	 * Not all implementations will be able to execute all commands.
	 * 
	 * @param command The {@link XCommand} which is to be executed
	 * 
	 * @return {@link XCommand#FAILED} if the command failed,
	 *         {@link XCommand#NOCHANGE} if the command didn't change anything
	 *         or the revision number of the {@link XEvent} caused by the
	 *         command.
	 * @throws IllegalStateException if this entity has already been removed
	 */
	@ModificationOperation
	long executeCommand(XCommand command, XLocalChangeCallback callback);
	
	// TODO document
	long getSynchronizedRevision();
	
	int countUnappliedLocalChanges();
	
}
