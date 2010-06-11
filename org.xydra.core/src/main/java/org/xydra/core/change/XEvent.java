package org.xydra.core.change;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;


/**
 * Any kind of changes has some entity that did it, a time point when it
 * happened and a change type.
 * 
 * @author voelkel
 * 
 */
public interface XEvent {
	
	public static long RevisionOfEntityNotSet = -1;
	
	/**
	 * @return the type of change.
	 */
	ChangeType getChangeType();
	
	/**
	 * WHO is doing this?
	 * 
	 * @return the XID of the actor requesting this change.
	 */
	XID getActor();
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the XAddress of the place where the change happened: the
	 *         repository, model or object to which an entity has been added or
	 *         removed or a field whose walue has changed
	 */
	XAddress getTarget();
	
	/**
	 * @return The revision number of the model at the time when this event
	 *         happened (may be -1 if this XEvent refers to something that is
	 *         not a model or has no father-model)
	 */
	long getModelRevisionNumber();
	
	/**
	 * @return The revision number of the object at the time when this event
	 *         happened (may be -1 if this XEvent refers to something that is
	 *         not an object or has no father-object)
	 */
	long getObjectRevisionNumber();
	
	/**
	 * @return The revision number of the field at the time when this event
	 *         happened (may be -1 if this XEvent refers to something that is
	 *         not a field)
	 */
	long getFieldRevisionNumber();
	
	/**
	 * @return true, if this event occurred during a transaction.
	 */
	boolean inTransaction();
	
	/**
	 * WHAT is being changed?
	 * 
	 * @return the model, object or field that was added or removed or the field
	 *         whose value changed; null for transactions.
	 */
	XAddress getChangedEntity();
	
}
