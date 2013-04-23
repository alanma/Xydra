package org.xydra.core.model;

import org.xydra.annotations.ModificationOperation;
import org.xydra.base.IHasXAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
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
    // long executeCommand(XCommand command);
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
     * Roll back the state (including revisions) to a specific revision. This
     * will erase all {@link XEvent XEvents} following this revision from the
     * {@link XChangeLog} of this {@link XSynchronizesChanges}. Listeners that
     * were/are registered to the entities that are manipulated by this
     * roll-back are not automatically restored or removed, but {@link XEvent
     * XEvents} are sent out for all changes made.
     * 
     * @param revision The revision number to which will be rolled back
     * @throws IllegalStateException if this entity has already been removed
     */
    // void rollback(long revision);
    
    /**
     * Set a new actor to be used when building commands for changes to this
     * entity and its children.
     * 
     * @param actorId for this entity and its children, if any.
     * @param passwordHash the password for the given actor.
     */
    void setSessionActor(XId actorId, String passwordHash);
    
    /**
     * TODO document
     * 
     * @param remoteChanges The remote changes that happened since the last
     *            synchronisation, including local changes that have been saved
     *            remotely.
     * @return true iff synchronisation was a success
     */
    // boolean synchronize(XEvent[] remoteChanges);
    
}