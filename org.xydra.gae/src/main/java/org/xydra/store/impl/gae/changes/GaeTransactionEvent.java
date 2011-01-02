/**
 * 
 */
package org.xydra.store.impl.gae.changes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.model.XAddress;
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
		assert changesService.getBaseAddress().getAddressedType() == XType.XMODEL;
		assert rev >= 0;
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
			
			assert (!(ae instanceof XRepositoryEvent))
			        || ((index == 0 || index == size() - 1) && ae.getChangeType() == (index == 0 ? ChangeType.ADD
			                : ChangeType.REMOVE));
			
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
	
	@Override
	public int hashCode() {
		
		// TODO share this code with MemoryTransactionEvent?
		
		int result = 0;
		
		result ^= this.events.length;
		
		// target
		XID repoId = getTarget().getRepository();
		if(repoId != null) {
			result ^= repoId.hashCode();
		}
		XID modelId = getTarget().getModel();
		if(modelId != null) {
			result ^= modelId.hashCode();
		}
		
		// actor
		result ^= (this.actor != null) ? this.actor.hashCode() : 0;
		
		// old revisions
		result += this.rev;
		
		return result;
	}
	
	@Override
	public boolean equals(Object object) {
		
		// TODO share this code with MemoryTransactionEvent?
		
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
		
		if(!getTarget().equalsOrContains(trans.getTarget())
		        && !trans.getTarget().contains(getTarget())) {
			return false;
		}
		
		if(!XI.equals(this.actor, trans.getActor())) {
			return false;
		}
		
		if(this.rev != trans.getOldModelRevision()) {
			return false;
		}
		
		if(getTarget().getObject() != null && trans.getTarget().getObject() != null) {
			if(trans.getOldObjectRevision() != XEvent.RevisionOfEntityNotSet) {
				return false;
			}
		}
		
		// assumes this transaction is minimal
		// otherwise the order is not completely irrelevant
		
		Set<XAtomicEvent> events = new HashSet<XAtomicEvent>();
		for(XAtomicEvent event : this) {
			events.add(event);
		}
		for(XAtomicEvent event : trans) {
			if(!events.contains(event)) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean isImplied() {
		return false;
	}
	
}
