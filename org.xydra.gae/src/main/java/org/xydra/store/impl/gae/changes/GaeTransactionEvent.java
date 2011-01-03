/**
 * 
 */
package org.xydra.store.impl.gae.changes;

import java.util.Iterator;

import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.change.impl.memory.AbstractTransactionEvent;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.index.XI;


/**
 * {@link XTransactionEvent} implementation that loads individual events from a
 * {@link GaeChangesService}.
 * 
 * @author dscharrer
 * 
 */
class GaeTransactionEvent extends AbstractTransactionEvent {
	
	private final GaeChangesService changesService;
	private final XAtomicEvent[] events;
	
	public GaeTransactionEvent(GaeChangesService changesService, int size, XID actor, long rev) {
		super(actor, changesService.getBaseAddress(), rev - 1, XEvent.RevisionOfEntityNotSet);
		
		this.changesService = changesService;
		this.events = new XAtomicEvent[size];
		
		assert size > 1;
		assert changesService.getBaseAddress().getAddressedType() == XType.XMODEL;
		assert rev >= 0;
		
		assert assertIsMinimal();
		assert assertIsCorrect();
	}
	
	public XAtomicEvent getEvent(int index) {
		
		if(index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException();
		}
		
		XAtomicEvent ae = this.events[index];
		
		if(ae == null) {
			ae = this.events[index] = this.changesService
			        .getAtomicEvent(getRevisionNumber(), index);
			
			assert ae != null;
			assert XI.equals(getActor(), ae.getActor());
			assert getTarget().equalsOrContains(ae.getChangedEntity());
			assert ae.getChangeType() != ChangeType.TRANSACTION;
			assert ae.inTransaction();
			
			assert (!(ae instanceof XRepositoryEvent))
			        || ((index == 0 || index == size() - 1) && ae.getChangeType() == (index == 0 ? ChangeType.ADD
			                : ChangeType.REMOVE));
			
		}
		
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
