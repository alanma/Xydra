package org.xydra.core.change.impl.memory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;



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
		
		if(!XX.equals(this.actor, trans.getActor()))
			return false;
		
		if(this.modelRevision != trans.getModelRevisionNumber())
			return false;
		
		if(this.objectRevision != trans.getObjectRevisionNumber())
			return false;
		
		for(int i = 0; i < size(); ++i) {
			
			if(!this.events[i].equals(trans.getEvent(i)))
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
			throw new RuntimeException("the event list must not be empty");
		}
		
		if((target.getModel() == null && target.getObject() == null) || target.getField() != null) {
			throw new RuntimeException("target must be a model or object, was:" + target);
		}
		
		if(target.getObject() == null) {
			if(objectRevision != XEvent.RevisionOfEntityNotSet)
				throw new RuntimeException("object revision must not be set for model transactions");
			if(modelRevision == XEvent.RevisionOfEntityNotSet)
				throw new RuntimeException("model revision must be set for model transactions");
		} else if(objectRevision == XEvent.RevisionOfEntityNotSet)
			throw new RuntimeException("object revision must be set for object transactions");
		
		for(int i = 0; i < events.length; ++i) {
			
			if(!XX.equalsOrContains(target, events[i].getTarget())) {
				throw new IllegalArgumentException("event #" + i + " " + events[i]
				        + " is not contained in " + target);
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
	 * @return a new transaction with the specified target and events. Changes
	 *         to the passed array will not affect the transaction.
	 */
	public static XTransactionEvent createTransactionEvent(XID actor, XAddress target,
	        XAtomicEvent[] events, long modelRevision, long objectRevision) {
		// create a copy so the array can't be modified from the outside
		XAtomicEvent[] eventsCopy = new XAtomicEvent[events.length];
		System.arraycopy(events, 0, eventsCopy, 0, events.length);
		return new MemoryTransactionEvent(actor, target, eventsCopy, modelRevision, objectRevision);
	}
	
	/**
	 * @return a new transaction with the specified target and events. Changes
	 *         to the passed {@link List} will not affect the transaction.
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
		str += (this.modelRevision == XEvent.RevisionOfEntityNotSet ? "-" : this.modelRevision);
		if(this.objectRevision != XEvent.RevisionOfEntityNotSet)
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
	
}
