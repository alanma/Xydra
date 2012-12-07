package org.xydra.base.change;

import java.io.Serializable;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


/**
 * An XEvent represents a change that happened, for example on an {@link XModel}
 * . It has a {@link ChangeType}, holds an actor (the one who executed the
 * change that is represented by this event), the revision number of the entity
 * when this change happened etc.
 * 
 * @author voelkel
 */
public interface XEvent extends Serializable {
	
	public static long RevisionNotAvailable = -2;
	public static long RevisionOfEntityNotSet = -1;
	
	/**
	 * WHO executed this?
	 * 
	 * @return the {@link XID} of the actor who executed the change that is
	 *         represented by this event.
	 */
	XID getActor();
	
	/**
	 * WHAT has been changed?
	 * 
	 * @return the {@link XAddress} of the {@link XModel}, {@link XObject} or
	 *         {@link XField} that was added, removed or the {@link XField}
	 *         which value was changed; same as getTarget() for transactions
	 */
	XAddress getChangedEntity();
	
	/**
	 * @return the type of change.
	 */
	ChangeType getChangeType();
	
	/**
	 * @return The revision number of the {@link XField} holding the changed
	 *         entity (or which is the changed entity) at the time when this
	 *         event happened (may be {@link #RevisionOfEntityNotSet} if this
	 *         XEvent refers to something that is not a field)
	 */
	long getOldFieldRevision();
	
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
	 *         event happened. The returned value may be
	 *         {@link #RevisionOfEntityNotSet} if this XEvent is not an object
	 *         event, e.g. if the event is an {@link XTransactionEvent}, or if
	 *         it has no father-object. The returned value may be
	 *         {@link #RevisionNotAvailable} if the object revision cannot be
	 *         efficiently calculated)
	 * 
	 *         TODO improve this documentation
	 */
	long getOldObjectRevision();
	
	/**
	 * @return the index of this event (or the containing
	 *         {@link XTransactionEvent}) in the change log.
	 */
	long getRevisionNumber();
	
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
	 * @return true, if this event occurred during a transaction.
	 */
	boolean inTransaction();
	
	/**
	 * @return true if this event describes removing an entity (
	 *         {@link #getChangedEntity()}) whose parent is also removed in the
	 *         same transaction event; false for transactions or events where
	 *         {@link #getChangeType()} is not {@link ChangeType#REMOVE}.
	 * 
	 *         An atomic event can be implies if it is part of a transaction. A
	 *         stand-alone atomic event cannot be implied.
	 * 
	 *         Repository events are never implied, as the repository cannot be
	 *         removed, only individual models.
	 */
	boolean isImplied();
	
}
