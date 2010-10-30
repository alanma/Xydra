/**
 * 
 */
package org.xydra.server.impl.newgae;

import java.util.Arrays;
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
	private final long modelRev;
	private final XAtomicEvent[] events;
	private final XID actor;
	
	public GaeTransactionEvent(GaeChangesService changesService, int size, XID actor, long modelRev) {
		this.changesService = changesService;
		this.events = new XAtomicEvent[size];
		this.actor = actor;
		this.modelRev = modelRev;
	}
	
	public XAtomicEvent getEvent(int index) {
		
		if(index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException();
		}
		
		XAtomicEvent ae = this.events[index];
		
		if(ae == null) {
			ae = this.events[index] = this.changesService.getAtomicEvent(this.modelRev, index);
			
			assert ae != null;
			assert XI.equals(this.actor, ae.getActor());
			assert getTarget().equalsOrContains(ae.getTarget());
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
		return this.modelRev;
	}
	
	public long getOldObjectRevision() {
		return XEvent.RevisionOfEntityNotSet;
	}
	
	public XAddress getTarget() {
		return this.changesService.getBaseAddress();
	}
	
	public boolean inTransaction() {
		return true;
	}
	
	public Iterator<XAtomicEvent> iterator() {
		return Arrays.asList(this.events).iterator();
	}
	
	@Override
	public long getRevisionNumber() {
		return this.modelRev + 1;
	}
	
}
