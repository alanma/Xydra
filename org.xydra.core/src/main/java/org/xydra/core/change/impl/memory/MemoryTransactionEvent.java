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
import org.xydra.core.change.XRepositoryEvent;
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
public class MemoryTransactionEvent extends AbstractTransactionEvent {
	
	private final XAtomicEvent[] events;
	
	private MemoryTransactionEvent(XID actor, XAddress target, XAtomicEvent[] events,
	        long modelRevision, long objectRevision) {
		super(actor, target, modelRevision, objectRevision);
		
		if(events.length == 0) {
			throw new IllegalArgumentException("the event list must not be empty");
		}
		
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
		
		this.events = events;
		
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
	
}
