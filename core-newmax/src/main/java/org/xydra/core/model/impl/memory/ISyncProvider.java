package org.xydra.core.model.impl.memory;

import org.xydra.annotations.NeverNull;
import org.xydra.base.IHasXAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


/**
 * Provides an API for the {@link SynchronisationState}
 * 
 * @author xamde
 * 
 */
public interface ISyncProvider extends IHasXAddress {
    
    /**
     * Create a new object, increase revision (if not in a transaction) and
     * enqueue the corresponding event.
     * 
     * The caller is responsible for handling synchronization, for checking that
     * this model has not been removed and for checking that the object doesn't
     * already exist.
     * 
     * @param objectId
     * @return ...
     */
    OldIMemoryObject createObjectInternal(XId objectId);
    
    /**
     * Get the {@link MemoryObject} with the given {@link XId}.
     * 
     * If the entity this method is called on already is an {@link MemoryObject}
     * the method returns this entity exactly when the given {@link XId} matches
     * its {@link XId} and null otherwise.
     * 
     * @param objectId The {@link XId} of the {@link MemoryObject} which is to
     *            be returned
     * 
     * @return true if there is an {@link XObject} with the given {@link XId}
     */
    OldIMemoryObject getObject(@NeverNull XId objectId);
    
    /**
     * @return Return the proxy for reading the current state.
     */
    XReadableModel getTransactionTarget();
    
    /**
     * Increment this entity's revision number.
     */
    void incrementRevision();
    
    /**
     * Remove an existing object, increase revision (if not in a transaction)
     * and enqueue the corresponding event(s).
     * 
     * The caller is responsible for handling synchronization, for checking that
     * this model has not been removed and for checking that the object actually
     * exists.
     * 
     * @param objectId
     */
    void removeObjectInternal(XId objectId);
    
    /**
     * Set the new revision number, if this is a subtype of {@link XModel}.
     * 
     * @param modelRevisionNumber
     */
    void setRevisionNumberIfModel(long modelRevisionNumber);
    
    /**
     * @return the revision number to return when executing {@link XCommand
     *         XCommands}.
     */
    long getRevisionNumber();
    
    long executeCommand(XCommand command);
    
}
