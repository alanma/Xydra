/**
 * 
 */
package org.xydra.server.impl.newgae.changes;

import java.util.Iterator;

import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.index.XI;


/**
 * {@link XTransactionEvent} implementation that loads individual events from a
 * {@link GaeChangesService}.
 * 
 * @author dscharrer
 * 
 */
class GaeTransactionEvent implements XTransactionEvent {
	
	private final GaeChangesService changesService;
	private final long rev;
	private final XAtomicEvent[] events;
	private final XID actor;
	
	public GaeTransactionEvent(GaeChangesService changesService, int size, XID actor, long rev) {
		this.changesService = changesService;
		this.events = new XAtomicEvent[size];
		this.actor = actor;
		this.rev = rev;
		assert size > 1;
	}
	
	public XAtomicEvent getEvent(int index) {
		
		if(index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException();
		}
		
		XAtomicEvent ae = this.events[index];
		
		if(ae == null) {
			ae = this.events[index] = this.changesService.getAtomicEvent(this.rev, index);
			
			assert ae != null;
			assert XI.equals(this.actor, ae.getActor());
			assert getTarget().equalsOrContains(ae.getChangedEntity());
			assert ae.getChangeType() != ChangeType.TRANSACTION;
			assert ae.inTransaction();
		}
		
		return ae;
	}
	
	public int size() {
		return this.events.length;
	}
	
	public XID getActor() {
		return this.actor;
	}
	
	public ChangeType getChangeType() {
		return ChangeType.TRANSACTION;
	}
	
	public XAddress getChangedEntity() {
		return null;
	}
	
	public long getOldFieldRevision() {
		return XEvent.RevisionOfEntityNotSet;
	}
	
	public long getOldModelRevision() {
		return this.rev - 1;
	}
	
	public long getOldObjectRevision() {
		return XEvent.RevisionOfEntityNotSet;
	}
	
	public XAddress getTarget() {
		return this.changesService.getBaseAddress();
	}
	
	public boolean inTransaction() {
		return false;
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
	
	@Override
	public long getRevisionNumber() {
		return this.rev + 1;
	}
	
}
