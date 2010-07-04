package org.xydra.core.model.session.impl.arm;

import java.util.Iterator;

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
import org.xydra.index.iterator.AbstractTransformingIterator;


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
	
	public Iterator<XEvent> getEventsAfter(long revisionNumber) {
		return getEventsBetween(revisionNumber, Long.MAX_VALUE);
	}
	
	public Iterator<XEvent> getEventsUntil(long revisionNumber) {
		return getEventsBetween(0, revisionNumber);
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
		return new AbstractTransformingIterator<XEvent,XEvent>(this.log.getEventsBetween(
		        beginRevision, endRevision)) {
			@Override
			public XEvent transform(XEvent entry) {
				return canSee(entry) ? entry : null;
			}
		};
	}
	
}
