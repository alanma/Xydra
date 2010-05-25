package org.xydra.core.model.session.impl.arm;

import java.util.Iterator;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;
import org.xydra.core.model.session.XAccessException;
import org.xydra.index.iterator.AbstractFilteringIterator;


/**
 * An {@link XChangeLog} wrapper for a specific actor that checks all access
 * against an {@link XAccessManager}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedChangeLog implements XChangeLog {
	
	private final XChangeLog log;
	private final XAccessManager arm;
	private final XID actor;
	
	public ArmProtectedChangeLog(XChangeLog log, XAccessManager arm, XID actor) {
		this.log = log;
		this.arm = arm;
		this.actor = actor;
	}
	
	public List<XEvent> getAllEventsAfter(long revisionNumber) {
		
		List<XEvent> events = this.log.getAllEventsAfter(revisionNumber);
		
		Iterator<XEvent> i = events.iterator();
		
		while(i.hasNext()) {
			if(!canSee(i.next())) {
				i.remove();
			}
		}
		
		return events;
	}
	
	public List<XEvent> getAllEventsUntil(long revisionNumber) {
		
		List<XEvent> events = this.log.getAllEventsUntil(revisionNumber);
		
		Iterator<XEvent> i = events.iterator();
		
		while(i.hasNext()) {
			if(!canSee(i.next())) {
				i.remove();
			}
		}
		
		return events;
	}
	
	private void checkReadAccess() throws XAccessException {
		if(!this.arm.canRead(this.actor, getModelAddress())) {
			throw new XAccessException(this.actor + " cannot read " + getModelAddress());
		}
	}
	
	public long getCurrentRevisionNumber() {
		
		checkReadAccess();
		
		return this.log.getCurrentRevisionNumber();
	}
	
	private boolean canSee(XEvent event) {
		
		if(this.arm.canRead(this.actor, event.getTarget())) {
			return true;
		}
		
		if(event instanceof XRepositoryEvent) {
			XAddress modelAddr = XX.resolveModel(event.getTarget(), ((XRepositoryEvent)event)
			        .getModelID());
			if(this.arm.canRead(this.actor, modelAddr)) {
				return true;
			}
		} else if(event instanceof XModelEvent) {
			XAddress objectAddr = XX.resolveObject(event.getTarget(), ((XModelEvent)event)
			        .getObjectID());
			if(this.arm.canRead(this.actor, objectAddr)) {
				return true;
			}
		} else if(event instanceof XObjectEvent) {
			XAddress fieldAddr = XX.resolveField(event.getTarget(), ((XObjectEvent)event)
			        .getFieldID());
			if(this.arm.canRead(this.actor, fieldAddr)) {
				return true;
			}
		}
		
		return false;
	}
	
	public XEvent getEventAt(long revisionNumber) {
		
		XEvent event = this.log.getEventAt(revisionNumber);
		
		if(event == null) {
			return null;
		}
		
		if(!canSee(event)) {
			return null;
		}
		
		return event;
	}
	
	public long getFirstRevisionNumber() {
		
		checkReadAccess();
		
		return this.log.getCurrentRevisionNumber();
	}
	
	public XAddress getModelAddress() {
		return this.log.getModelAddress();
	}
	
	public Iterator<XEvent> getEventsBetween(long beginRevision, long endRevision) {
		return new AbstractFilteringIterator<XEvent>(this.log.getEventsBetween(beginRevision,
		        endRevision)) {
			@Override
			protected boolean matchesFilter(XEvent entry) {
				return canSee(entry);
			}
		};
	}
	
}
