package org.xydra.core.change;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;


/**
 * An XEvent represents a change that happened, for example on an {@link XModel}
 * . It has a {@link ChangeType}, holds an actor (the one who executed the
 * change that is represented by this event), the revision number of the entity
 * when this change happened etc.
 * 
 * @author voelkel
 * 
 */
public interface XEvent {
	
	public static long RevisionOfEntityNotSet = -1;
	public static long RevisionNotAvailable = -2;
	
	/**
	 * @return the type of change.
	 */
	ChangeType getChangeType();
	
	/**
	 * WHO executed this?
	 * 
	 * @return the {@link XID} of the actor who executed the change that is
	 *         represented by this event.
	 */
	XID getActor();
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XAddress} of the entity where the change happened: the
	 *         {@link XAddress} of the {@link XRepository}, {@link XModel} or
	 *         {@link XObject} to which an entity has been added or was removed
	 *         from or of an {@link XField} which value has been changed
	 */
	XAddress getTarget();
	
	/**
	 * @return The revision number of the {@link XModel} holding the changed
	 *         entity (or which is the changed entity) before this event
	 *         happened (may be {@link #RevisionOfEntityNotSet} if this XEvent
	 *         refers to something that is not a model or has no father-model)
	 */
	long getOldModelRevision();
	
	/**
	 * @return The revision number of the {@link XObject} holding the changed
	 *         entity (or which is the changed entity) at the time when this
	 *         event happened (may be {@link #RevisionOfEntityNotSet} if this
	 *         XEvent refers to something that is not an object or has no
	 *         father-object; may be {@link #RevisionNotAvailable} if the object
	 *         revision cannot be efficiently calculated)
	 */
	long getOldObjectRevision();
	
	/**
	 * @return The revision number of the {@link XField} holding the changed
	 *         entity (or which is the changed entity) at the time when this
	 *         event happened (may be {@link #RevisionOfEntityNotSet} if this
	 *         XEvent refers to something that is not a field)
	 */
	long getOldFieldRevision();
	
	/**
	 * @return the index of this event (or the containing
	 *         {@link XTransactionEvent}) in the change log.
	 */
	long getRevisionNumber();
	
	/**
	 * @return true, if this event occurred during a transaction.
	 */
	boolean inTransaction();
	
	/**
	 * WHAT has been changed?
	 * 
	 * @return the {@link XAddress} of the {@link XModel}, {@link XObject} or
	 *         {@link XField} that was added, removed or the {@link XField}
	 *         which value was changed; same as getTarget() for transactions
	 */
	XAddress getChangedEntity();
	
	/**
	 * @return true if this event describes removing an entity (
	 *         {@link #getChangedEntity()}) whose parent is also removed in the
	 *         same transaction event; false for transactions or events where
	 *         {@link #getChangeType()} is not {@link ChangeType#REMOVE}.
	 * 
	 *         TODO clarify: can this every be true for {@link XAtomicEvent}?
	 * 
	 *         Repository events are never implied, as the repository cannot be
	 *         removed, only individual models.
	 */
	boolean isImplied();
	
}
