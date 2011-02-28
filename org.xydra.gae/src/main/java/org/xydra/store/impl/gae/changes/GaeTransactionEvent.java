/**
 * 
 */
package org.xydra.store.impl.gae.changes;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.AbstractTransactionEvent;
import org.xydra.index.XI;
import org.xydra.store.impl.gae.changes.GaeEventService.AsyncAtomicEvent;


/**
 * {@link XTransactionEvent} implementation that loads individual events from a
 * {@link GaeChangesService}.
 * 
 * @author dscharrer
 * 
 */
class GaeTransactionEvent extends AbstractTransactionEvent {
	
	private final AsyncAtomicEvent[] events;
	
	public GaeTransactionEvent(XAddress modelAddr, AsyncAtomicEvent[] events, XID actor, long rev) {
		super(actor, modelAddr, rev - 1, XEvent.RevisionOfEntityNotSet);
		
		this.events = events;
		
		assert rev >= 0;
		
		assert assertIsMinimal();
		assert assertIsCorrect();
	}
	
	public XAtomicEvent getEvent(int index) {
		
		XAtomicEvent ae = this.events[index].get();
		
		assert ae != null;
		assert XI.equals(getActor(), ae.getActor());
		assert getTarget().equalsOrContains(ae.getChangedEntity());
		assert ae.getChangeType() != ChangeType.TRANSACTION;
		assert ae.inTransaction();
		
		assert (!(ae instanceof XRepositoryEvent))
		        || ((index == 0 || index == size() - 1) && ae.getChangeType() == (index == 0 ? ChangeType.ADD
		                : ChangeType.REMOVE));
		
		return ae;
	}
	
	public int size() {
		return this.events.length;
	}
	
	class EventIterator implements Iterator<XAtomicEvent> {
		
		private int i = 0;
		private XAtomicEvent next;
		
		public boolean hasNext() {
			getNext();
			return this.next != null;
		}
		
		public XAtomicEvent next() {
			XAtomicEvent event = this.next;
			this.next = null;
			getNext();
			return event;
		}
		
		private void getNext() {
			while(this.i < size() && this.next == null) {
				this.next = getEvent(this.i);
				this.i++;
			}
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	public Iterator<XAtomicEvent> iterator() {
		return new EventIterator();
	}
	
}
