package org.xydra.base.change.impl.memory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.index.XI;
import org.xydra.sharedutils.XyAssert;


/**
 * Implementation of the {@XTransaction} interface.
 * 
 * @author Kaidel
 * @author dscharrer
 */
@RunsInGWT(true)
public class MemoryTransactionEvent extends AbstractTransactionEvent {
	
	private static final long serialVersionUID = 6281227584641817166L;
	
	/**
	 * Creates a new {@link XTransactionEvent} composed of the given array of
	 * {@link XAtomicEvent XAtomicEvents} referring to the specified target.
	 * Changes to the passed array will not affect the event after its creation.
	 * 
	 * @param actor the {@link XId} of the actor
	 * @param target the {@link XAddress} of the model or
	 *            object where the {@link XTransaction} represented by
	 *            this event was executed
	 * @param events the {@link XAtomicEvent XAtomicEvens} which were executed
	 *            by the {@link XTransaction} this event represents
	 * @param modelRevision the revision number of the model this event
	 *            refers to - must be set if this event represents an
	 *            {@link XTransaction} which was executed on an model
	 * @param objectRevision the revision number of the object this
	 *            event refers to - must be set if this event represents an
	 *            {@link XTransaction} which was executed on an object
	 * @return a new transaction with the specified target and events. Changes
	 *         to the passed array will not affect the transaction.
	 * @throws IllegalArgumentException if the given {@link XAddress} does not
	 *             specify an model or object.
	 * @throws IllegalArgumentException if the given list of
	 *             {@link XAtomicEvent XAtomicEvents} was empty
	 * @throws IllegalArgumentException if the event represents an
	 *             {@link XTransaction} which was executed on an model
	 *             and the given modelRevision equals
	 *             {@link XEvent#REVISIONOFENTITYNOTSET} or the given
	 *             objectRevision does not equal
	 *             {@link XEvent#REVISIONOFENTITYNOTSET}
	 * @throws IllegalArgumentException if the event represents an
	 *             {@link XTransaction} which was executed on an object
	 *             and the given objectRevision equals
	 *             {@link XEvent#REVISIONOFENTITYNOTSET}
	 * @throws IllegalArgumentException if one of the targets of the given
	 *             {@link XAtomicEvent XAtomicEvents} is not contained by the
	 *             entity specified by the given {@link XAddress} 'target'
	 * @throws IllegalArgumentException if one of the given {@link XAtomicEvent
	 *             XAtomicEvents} is neither an {@link XModelEvent}, an
	 *             {@link XObjectEvent} nor an {@link XFieldEvent}
	 */
	public static XTransactionEvent createTransactionEvent(XId actor, XAddress target,
	        List<XAtomicEvent> events, long modelRevision, long objectRevision) {
		XAtomicEvent[] eventsCopy = new XAtomicEvent[events.size()];
		eventsCopy = events.toArray(eventsCopy);
		return new MemoryTransactionEvent(actor, target, eventsCopy, modelRevision, objectRevision);
	}
	
	/**
	 * Creates a new {@link XTransactionEvent} composed of the given array of
	 * {@link XAtomicEvent XAtomicEvents} referring to the specified target.
	 * Changes to the passed array will not affect the event after its creation.
	 * 
	 * @param actor the {@link XId} of the actor
	 * @param target the {@link XAddress} of the model or
	 *            object where the {@link XTransaction} represented by
	 *            this event was executed
	 * @param events the {@link XAtomicEvent XAtomicEvens} which were executed
	 *            by the {@link XTransaction} this event represents
	 * @param modelRevision the revision number of the model this event
	 *            refers to - must be set if this event represents an
	 *            {@link XTransaction} which was executed on an model
	 * @param objectRevision the revision number of the object this
	 *            event refers to - must be set if this event represents an
	 *            {@link XTransaction} which was executed on an object
	 * @return a new transaction with the specified target and events. Changes
	 *         to the passed array will not affect the transaction.
	 * @throws IllegalArgumentException if the given {@link XAddress} does not
	 *             specify an model or object.
	 * @throws IllegalArgumentException if the given array of
	 *             {@link XAtomicEvent XAtomicEvents} was empty
	 * @throws IllegalArgumentException if the event represents an
	 *             {@link XTransaction} which was executed on an model
	 *             and the given modelRevision equals
	 *             {@link XEvent#REVISIONOFENTITYNOTSET} or the given
	 *             objectRevision does not equal
	 *             {@link XEvent#REVISIONOFENTITYNOTSET}
	 * @throws IllegalArgumentException if the event represents an
	 *             {@link XTransaction} which was executed on an object
	 *             and the given objectRevision equals
	 *             {@link XEvent#REVISIONOFENTITYNOTSET}
	 * @throws IllegalArgumentException if one of the targets of the given
	 *             {@link XAtomicEvent XAtomicEvents} is not contained by the
	 *             entity specified by the given {@link XAddress} 'target'
	 * @throws IllegalArgumentException if one of the given {@link XAtomicEvent
	 *             XAtomicEvents} is neither an {@link XModelEvent}, an
	 *             {@link XObjectEvent} nor an {@link XFieldEvent}
	 */
	public static XTransactionEvent createTransactionEvent(XId actor, XAddress target,
	        XAtomicEvent[] events, long modelRevision, long objectRevision) {
		// create a copy so the array can't be modified from the outside
		XAtomicEvent[] eventsCopy = new XAtomicEvent[events.length];
		System.arraycopy(events, 0, eventsCopy, 0, events.length);
		return new MemoryTransactionEvent(actor, target, eventsCopy, modelRevision, objectRevision);
	}
	
	private XAtomicEvent[] events;
	
	/** For GWT only! */
	private MemoryTransactionEvent() {
		super();
	}
	
	private MemoryTransactionEvent(XId actor, XAddress target, XAtomicEvent[] events,
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
			
			XyAssert.xyAssert(events[i].getChangeType() != ChangeType.TRANSACTION);
		}
		
		this.events = events;
		
		assert assertIsMinimal() : "redundant events in transaction: " + toString();
		assert assertIsCorrect() : "impossible transaction event: " + toString();
	}
	
	@Override
	public XAtomicEvent getEvent(int index) {
		return this.events[index];
	}
	
	@Override
	public Iterator<XAtomicEvent> iterator() {
		return Arrays.asList(this.events).iterator();
	}
	
	@Override
	public int size() {
		return this.events.length;
	}
	
}
