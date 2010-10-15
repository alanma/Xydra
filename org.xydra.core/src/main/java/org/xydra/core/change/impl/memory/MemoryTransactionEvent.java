package org.xydra.core.change.impl.memory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.index.XI;


/**
 * Implementation of the {@XTransaction} interface.
 * 
 * @author Kaidel
 * @author dscharrer
 */
public class MemoryTransactionEvent implements XTransactionEvent {
	
	private final XAtomicEvent[] events;
	
	/** The XAddress of the model or object this transaction applies to */
	private final XAddress target;
	
	private final XID actor;
	
	// the revision numbers before the event happened
	private final long modelRevision, objectRevision;
	
	@Override
	public boolean equals(Object object) {
		
		if(object == null)
			return false;
		
		if(!(object instanceof XTransactionEvent))
			return false;
		XTransactionEvent trans = (XTransactionEvent)object;
		
		if(size() != trans.size())
			return false;
		
		if(!this.target.equals(trans.getTarget()))
			return false;
		
		if(!XI.equals(this.actor, trans.getActor()))
			return false;
		
		if(this.modelRevision != trans.getModelRevisionNumber())
			return false;
		
		if(this.objectRevision != trans.getObjectRevisionNumber())
			return false;
		
		for(int i = 0; i < size(); ++i) {
			
			XAtomicEvent here = this.events[i];
			XAtomicEvent there = trans.getEvent(i);
			
			if(!here.equals(there))
				return false;
			
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		
		int result = 0;
		
		result ^= Arrays.hashCode(this.events);
		
		// target
		result ^= this.target.hashCode();
		
		// actor
		result ^= (this.actor != null) ? this.actor.hashCode() : 0;
		
		// old revisions
		result += this.modelRevision;
		result += this.objectRevision;
		
		return result;
	}
	
	public boolean inTransaction() {
		// transactions never occur as events during transactions
		return false;
	}
	
	private MemoryTransactionEvent(XID actor, XAddress target, XAtomicEvent[] events,
	        long modelRevision, long objectRevision) {
		
		if(events.length == 0) {
			throw new IllegalArgumentException("the event list must not be empty");
		}
		
		if((target.getModel() == null && target.getObject() == null) || target.getField() != null) {
			throw new IllegalArgumentException("target must be a model or object, was:" + target);
		}
		
		if(target.getObject() == null) {
			if(objectRevision >= 0)
				throw new IllegalArgumentException(
				        "object revision must not be set for model transactions");
			if(modelRevision < 0)
				throw new IllegalArgumentException(
				        "model revision must be set for model transactions");
		} else if(objectRevision < 0)
			throw new IllegalArgumentException(
			        "object revision must be set for object transactions");
		
		for(int i = 0; i < events.length; ++i) {
			
			if(!target.equalsOrContains(events[i].getTarget())) {
				throw new IllegalArgumentException("event #" + i + " " + events[i]
				        + " target is not contained in " + target);
			}
			
			if(!(events[i] instanceof XModelEvent || events[i] instanceof XObjectEvent || events[i] instanceof XFieldEvent)) {
				throw new IllegalArgumentException("event #" + i + " " + events[i]
				        + " is not an XModelEvent, XObjectEvent or XFieldEvent.");
			}
		}
		
		this.actor = actor;
		this.events = events;
		this.target = target;
		this.modelRevision = modelRevision;
		this.objectRevision = objectRevision;
	}
	
	/**
	 * Creates a new {@link XTransactionEvent} composed of the given array of
	 * {@link XAtomicEvent XAtomicEvents} referring to the specified target.
	 * Changes to the passed array will not affect the event after its creation.
	 * 
	 * @param actor the {@link XID} of the actor
	 * @param target the {@link XAddress} of the {@link XModel} or
	 *            {@link XObject} where the {@link XTransaction} represented by
	 *            this event was executed
	 * @param events the {@link XAtomicEvent XAtomicEvens} which were executed
	 *            by the {@link XTransaction} this event represents
	 * @param modelRevision the revision number of the {@link XModel} this event
	 *            refers to - must be set if this event represents an
	 *            {@link XTransaction} which was executed on an {@link XModel}
	 * @param objectRevision the revision number of the {@link XObject} this
	 *            event refers to - must be set if this event represents an
	 *            {@link XTransaction} which was executed on an {@link XObject}
	 * @return a new transaction with the specified target and events. Changes
	 *         to the passed array will not affect the transaction.
	 * @throws IllegalArgumentException if the given {@link XAddress} does not
	 *             specify an {@link XModel} or {@link XObject}.
	 * @throws IllegalArgumentException if the given array of
	 *             {@link XAtomicEvent XAtomicEvents} was empty
	 * @throws IllegalArgumentException if the event represents an
	 *             {@link XTransaction} which was executed on an {@link XModel}
	 *             and the given modelRevision equals
	 *             {@link XEvent#RevisionOfEntityNotSet} or the given
	 *             objectRevision does not equal
	 *             {@link XEvent#RevisionOfEntityNotSet}
	 * @throws IllegalArgumentException if the event represents an
	 *             {@link XTransaction} which was executed on an {@link XObject}
	 *             and the given objectRevision equals
	 *             {@link XEvent#RevisionOfEntityNotSet}
	 * @throws IllegalArgumentException if one of the targets of the given
	 *             {@link XAtomicEvent XAtomicEvents} is not contained by the
	 *             entity specified by the given {@link XAddress} 'target'
	 * @throws IllegalArgumentException if one of the given {@link XAtomicEvent
	 *             XAtomicEvents} is neither an {@link XModelEvent}, an
	 *             {@link XObjectEvent} nor an {@link XFieldEvent}
	 */
	public static XTransactionEvent createTransactionEvent(XID actor, XAddress target,
	        XAtomicEvent[] events, long modelRevision, long objectRevision) {
		// create a copy so the array can't be modified from the outside
		XAtomicEvent[] eventsCopy = new XAtomicEvent[events.length];
		System.arraycopy(events, 0, eventsCopy, 0, events.length);
		return new MemoryTransactionEvent(actor, target, eventsCopy, modelRevision, objectRevision);
	}
	
	/**
	 * Creates a new {@link XTransactionEvent} composed of the given array of
	 * {@link XAtomicEvent XAtomicEvents} referring to the specified target.
	 * Changes to the passed array will not affect the event after its creation.
	 * 
	 * @param actor the {@link XID} of the actor
	 * @param target the {@link XAddress} of the {@link XModel} or
	 *            {@link XObject} where the {@link XTransaction} represented by
	 *            this event was executed
	 * @param events the {@link XAtomicEvent XAtomicEvens} which were executed
	 *            by the {@link XTransaction} this event represents
	 * @param modelRevision the revision number of the {@link XModel} this event
	 *            refers to - must be set if this event represents an
	 *            {@link XTransaction} which was executed on an {@link XModel}
	 * @param objectRevision the revision number of the {@link XObject} this
	 *            event refers to - must be set if this event represents an
	 *            {@link XTransaction} which was executed on an {@link XObject}
	 * @return a new transaction with the specified target and events. Changes
	 *         to the passed array will not affect the transaction.
	 * @throws IllegalArgumentException if the given {@link XAddress} does not
	 *             specify an {@link XModel} or {@link XObject}.
	 * @throws IllegalArgumentException if the given list of
	 *             {@link XAtomicEvent XAtomicEvents} was empty
	 * @throws IllegalArgumentException if the event represents an
	 *             {@link XTransaction} which was executed on an {@link XModel}
	 *             and the given modelRevision equals
	 *             {@link XEvent#RevisionOfEntityNotSet} or the given
	 *             objectRevision does not equal
	 *             {@link XEvent#RevisionOfEntityNotSet}
	 * @throws IllegalArgumentException if the event represents an
	 *             {@link XTransaction} which was executed on an {@link XObject}
	 *             and the given objectRevision equals
	 *             {@link XEvent#RevisionOfEntityNotSet}
	 * @throws IllegalArgumentException if one of the targets of the given
	 *             {@link XAtomicEvent XAtomicEvents} is not contained by the
	 *             entity specified by the given {@link XAddress} 'target'
	 * @throws IllegalArgumentException if one of the given {@link XAtomicEvent
	 *             XAtomicEvents} is neither an {@link XModelEvent}, an
	 *             {@link XObjectEvent} nor an {@link XFieldEvent}
	 */
	public static XTransactionEvent createTransactionEvent(XID actor, XAddress target,
	        List<XAtomicEvent> events, long modelRevision, long objectRevision) {
		XAtomicEvent[] eventsCopy = new XAtomicEvent[events.size()];
		eventsCopy = events.toArray(eventsCopy);
		return new MemoryTransactionEvent(actor, target, eventsCopy, modelRevision, objectRevision);
	}
	
	public Iterator<XAtomicEvent> iterator() {
		return Arrays.asList(this.events).iterator();
	}
	
	public XAtomicEvent getEvent(int index) {
		return this.events[index];
	}
	
	public int size() {
		return this.events.length;
	}
	
	public ChangeType getChangeType() {
		return ChangeType.TRANSACTION;
	}
	
	@Override
	public String toString() {
		String str = "TransactionEvent @" + this.target + " r";
		str += (this.modelRevision < 0 ? "-" : this.modelRevision);
		if(this.objectRevision >= 0)
			str += "/" + this.objectRevision;
		return str + ": " + Arrays.toString(this.events);
	}
	
	public XAddress getTarget() {
		return this.target;
	}
	
	public XID getActor() {
		return this.actor;
	}
	
	public long getFieldRevisionNumber() {
		return XEvent.RevisionOfEntityNotSet;
	}
	
	public long getObjectRevisionNumber() {
		return this.objectRevision;
	}
	
	public long getModelRevisionNumber() {
		return this.modelRevision;
	}
	
	public XAddress getChangedEntity() {
		return null;
	}
	
}
