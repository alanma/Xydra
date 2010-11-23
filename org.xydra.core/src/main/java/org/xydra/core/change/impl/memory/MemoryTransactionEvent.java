package org.xydra.core.change.impl.memory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.index.XI;

import com.sun.org.apache.xpath.internal.objects.XObject;


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
		
		if(object == null) {
			return false;
		}
		
		if(!(object instanceof XTransactionEvent)) {
			return false;
		}
		XTransactionEvent trans = (XTransactionEvent)object;
		
		if(this.events.length != trans.size()) {
			return false;
		}
		
		if(!this.target.equalsOrContains(trans.getTarget())
		        && !trans.getTarget().contains(this.target)) {
			return false;
		}
		
		if(!XI.equals(this.actor, trans.getActor())) {
			return false;
		}
		
		if(this.modelRevision != trans.getOldModelRevision()) {
			return false;
		}
		
		if(this.target.getObject() != null && trans.getTarget().getObject() != null) {
			if(this.objectRevision != trans.getOldObjectRevision()) {
				return false;
			}
		}
		
		// assumes this transaction is minimal
		// otherwise the order is not completely irrelevant
		
		Set<XAtomicEvent> events = new HashSet<XAtomicEvent>();
		events.addAll(Arrays.asList(this.events));
		for(XAtomicEvent event : trans) {
			if(!events.contains(event)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * @return true if this transaction event cannot be the result of a valid
	 *         {@link XModel} / {@link XObject} transaction. Throws an
	 *         {@link AssertionError} otherwise. Assumes the transaction is
	 *         minimal.
	 */
	private boolean assertIsCorrect() {
		
		Map<XAddress,Boolean> entities = new HashMap<XAddress,Boolean>();
		
		for(XAtomicEvent event : this) {
			
			for(XAddress addr = event.getTarget(); addr != null; addr = addr.getParent()) {
				if(Boolean.FALSE.equals(entities.get(addr))) {
					assert false : "modified entity after remove";
				}
				entities.put(addr, true);
			}
			
			if(!(event instanceof XFieldEvent)) {
				Boolean value;
				XAddress entity = event.getChangedEntity();
				if(event.getChangeType() == ChangeType.REMOVE) {
					value = Boolean.FALSE;
				} else {
					value = Boolean.TRUE;
					assert entities.get(entity) == null : "adding already touched entity";
				}
				entities.put(entity, value);
			}
			
		}
		
		// check if implied events are marked correctly
		for(XAtomicEvent event : this) {
			
			boolean implied = false;
			boolean b = true;
			for(XAddress addr = event.getTarget(); addr != null; addr = addr.getParent()) {
				if(Boolean.FALSE.equals(entities.get(addr))) {
					implied = true;
					assert b : "removed the an entity but not all children";
				} else {
					b = false;
				}
			}
			
			assert event.isImplied() == implied : "event has incorrect implied flag: " + event;
		}
		
		return true;
	}
	
	/**
	 * @return true if this transaction contains any redundant events. Throws an
	 *         {@link AssertionError} otherwise.
	 */
	private boolean assertIsMinimal() {
		
		Set<XAddress> entities = new HashSet<XAddress>();
		Set<XAddress> values = new HashSet<XAddress>();
		
		for(XAtomicEvent event : this) {
			
			if(event instanceof XFieldEvent) {
				assert !values.contains(event.getTarget()) : "changed value of field twice";
				values.add(event.getTarget());
			} else {
				XAddress addr = event.getChangedEntity();
				assert !entities.contains(addr) : "added and removed entity in same transaction "
				        + "or added / removed entity twice";
				entities.add(addr);
			}
			
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		
		int result = 0;
		
		result ^= this.events.length;
		
		// target
		XID repoId = this.target.getRepository();
		if(repoId != null) {
			result ^= repoId.hashCode();
		}
		XID modelId = this.target.getModel();
		if(modelId != null) {
			result ^= modelId.hashCode();
		}
		
		// actor
		result ^= (this.actor != null) ? this.actor.hashCode() : 0;
		
		// old revisions
		result += this.modelRevision;
		if(this.modelRevision == XEvent.RevisionOfEntityNotSet) {
			result += this.objectRevision;
		}
		
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
			
			if(!target.equalsOrContains(events[i].getChangedEntity())) {
				throw new IllegalArgumentException("event #" + i + " " + events[i]
				        + " target is not contained in " + target);
			}
			
			if(!XI.equals(events[i].getActor(), actor)) {
				throw new IllegalArgumentException("cannot add event " + events[i]
				        + " to transaction with actorId=" + actor);
			}
			
			if(events[i] instanceof XRepositoryEvent) {
				if(i == 0) {
					if(events[i].getChangeType() != ChangeType.ADD) {
						throw new IllegalArgumentException(
						        "can only add the model at the beginning of a transaction");
					}
				} else if(i == events.length - 1) {
					if(events[i].getChangeType() != ChangeType.REMOVE) {
						throw new IllegalArgumentException(
						        "can only remove the model at the end of a transaction");
					}
				} else {
					throw new IllegalArgumentException(
					        "Repository events shold only occur at the beginning or end of a transaction");
				}
			}
			
			if(!events[i].inTransaction()) {
				throw new IllegalArgumentException("cannot add event " + events[i]
				        + " to a transaction event, as it is not marked as inTransaction");
			}
			
			assert events[i].getChangeType() != ChangeType.TRANSACTION;
		}
		
		this.actor = actor;
		this.events = events;
		this.target = target;
		this.modelRevision = modelRevision;
		this.objectRevision = objectRevision;
		
		assert assertIsMinimal() : "redundant events in transaction: " + toString();
		assert assertIsCorrect() : "impossible transaction event: " + toString();
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
		String str = "TransactionEvent by " + this.actor + " @" + this.target + " r";
		str += (this.modelRevision < 0 ? "-" : this.modelRevision);
		if(this.objectRevision >= 0) {
			str += "/" + this.objectRevision;
		}
		return str + ": " + Arrays.toString(this.events);
	}
	
	public XAddress getTarget() {
		return this.target;
	}
	
	public XID getActor() {
		return this.actor;
	}
	
	public long getOldFieldRevision() {
		return XEvent.RevisionOfEntityNotSet;
	}
	
	public long getOldObjectRevision() {
		return this.objectRevision;
	}
	
	public long getOldModelRevision() {
		return this.modelRevision;
	}
	
	public XAddress getChangedEntity() {
		return this.target;
	}
	
	public long getRevisionNumber() {
		
		if(this.modelRevision >= 0) {
			return this.modelRevision + 1;
		}
		
		if(this.objectRevision >= 0) {
			return this.objectRevision + 1;
		}
		
		return 0;
	}
	
	@Override
	public boolean isImplied() {
		return false;
	}
	
}
