package org.xydra.base.change;

import java.io.Serializable;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.impl.memory.RevisionConstants;


/**
 * An XEvent represents a change that happened, for example on an model . It has
 * a {@link ChangeType}, holds an actor (the one who executed the change that is
 * represented by this event), the revision number of the entity when this
 * change happened etc.
 * 
 * @author voelkel
 */
public interface XEvent extends Serializable {
    
    /**
     * The revision of an event cannot be efficiently calculated.
     */
    public static long REVISION_NOT_AVAILABLE = RevisionConstants.REVISION_NOT_AVAILABLE;
    
    /**
     * A revision number has not been set for this entity. E.g. if this XEvent
     * has no such father-entity.
     */
    public static long REVISION_OF_ENTITY_NOT_SET = RevisionConstants.REVISION_OF_ENTITY_NOT_SET;
    
    /**
     * WHO executed this?
     * 
     * @return the {@link XId} of the actor who executed the change that is
     *         represented by this event.
     */
    XId getActor();
    
    /**
     * WHAT has been changed?
     * 
     * @return the {@link XAddress} of the model, object or field that was
     *         added, removed or the field which value was changed; same as
     *         getTarget() for transactions
     */
    XAddress getChangedEntity();
    
    /**
     * @return the type of change.
     */
    ChangeType getChangeType();
    
    /**
     * @return The revision number of the field holding the changed entity (or
     *         which is the changed entity) at the time when this event happened
     *         (may be {@link #REVISION_OF_ENTITY_NOT_SET} if this XEvent refers
     *         to something that is not a field)
     */
    long getOldFieldRevision();
    
    /**
     * @return The revision number of the model holding the changed entity (or
     *         which is the changed entity) before this event happened (may be
     *         {@link #REVISION_OF_ENTITY_NOT_SET} if this XEvent refers to
     *         something that is not a model or has no father-model)
     */
    long getOldModelRevision();
    
    /**
     * @return The revision number of the object holding the changed entity (or
     *         which is the changed entity) at the time when this event
     *         happened.
     * 
     *         The returned value may be {@link #REVISION_OF_ENTITY_NOT_SET} if
     *         this XEvent is not an object event, e.g. if the event is an
     *         {@link XTransactionEvent}, or if it has no father-object.
     * 
     *         The returned value may be {@link #REVISION_NOT_AVAILABLE} if the
     *         object revision cannot be efficiently calculated)
     */
    long getOldObjectRevision();
    
    /**
     * @return the index of this event (or the containing
     *         {@link XTransactionEvent}) in the change log beginning with 0.
     */
    long getRevisionNumber();
    
    /**
     * WHERE did the change happen?
     * 
     * @return the {@link XAddress} of the entity where the change happened: the
     *         {@link XAddress} of the repository, model or object to which an
     *         entity has been added or was removed from or of an field which
     *         value has been changed
     */
    XAddress getTarget();
    
    /**
     * @return true, if this event occurred during a transaction.
     */
    boolean inTransaction();
    
    /**
     * @return true if this event describes removing an entity (
     *         {@link #getChangedEntity()}) whose parent is also removed in the
     *         same transaction event; false for transactions or events where
     *         {@link #getChangeType()} is not {@link ChangeType#REMOVE}.
     * 
     *         An atomic event can be implied if it is part of a transaction. A
     *         stand-alone atomic event cannot be implied.
     * 
     *         Repository events are never implied, as the repository cannot be
     *         removed, only individual models.
     */
    boolean isImplied();
    
}
