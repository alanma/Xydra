package org.xydra.store.protect.impl.arm;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.core.AccessException;
import org.xydra.core.XX;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XChangeLogState;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.store.access.XAuthorisationManager;


/**
 * An {@link XChangeLog} wrapper for a specific actor that checks all access
 * against an {@link XAuthorisationManager}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedChangeLog implements XChangeLog {
    
    private static final long serialVersionUID = -1236561973087579785L;
    
    private final XId actor;
    private final XAuthorisationManager arm;
    private final XChangeLog log;
    
    public ArmProtectedChangeLog(XChangeLog log, XAuthorisationManager arm, XId actor) {
        this.log = log;
        this.arm = arm;
        this.actor = actor;
    }
    
    private boolean canSee(XEvent event) {
        
        if(this.arm.canRead(this.actor, event.getTarget())) {
            return true;
        }
        
        if(event instanceof XRepositoryEvent) {
            XAddress modelAddr = XX.resolveModel(event.getTarget(),
                    ((XRepositoryEvent)event).getModelId());
            if(this.arm.canRead(this.actor, modelAddr)) {
                return true;
            }
        } else if(event instanceof XModelEvent) {
            XAddress objectAddr = XX.resolveObject(event.getTarget(),
                    ((XModelEvent)event).getObjectId());
            if(this.arm.canRead(this.actor, objectAddr)) {
                return true;
            }
        } else if(event instanceof XObjectEvent) {
            XAddress fieldAddr = XX.resolveField(event.getTarget(),
                    ((XObjectEvent)event).getFieldId());
            if(this.arm.canRead(this.actor, fieldAddr)) {
                return true;
            }
        }
        
        return false;
    }
    
    private void checkReadAccess() throws AccessException {
        if(!this.arm.canRead(this.actor, getBaseAddress())) {
            throw new AccessException(this.actor + " cannot read " + getBaseAddress());
        }
    }
    
    @Override
    public XAddress getBaseAddress() {
        return this.log.getBaseAddress();
    }
    
    @Override
    public long getCurrentRevisionNumber() {
        
        checkReadAccess();
        
        return this.log.getCurrentRevisionNumber();
    }
    
    @Override
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
    
    @Override
    public Iterator<XEvent> getEventsBetween(long beginRevision, long endRevision) {
        return new AbstractTransformingIterator<XEvent,XEvent>(this.log.getEventsBetween(
                beginRevision, endRevision)) {
            @Override
            public XEvent transform(XEvent entry) {
                return canSee(entry) ? entry : null;
            }
        };
    }
    
    @Override
    public Iterator<XEvent> getEventsSince(long revisionNumber) {
        return getEventsBetween(revisionNumber, Long.MAX_VALUE);
    }
    
    @Override
    public Iterator<XEvent> getEventsUntil(long revisionNumber) {
        return getEventsBetween(0, revisionNumber);
    }
    
    @Override
    public long getBaseRevisionNumber() {
        
        checkReadAccess();
        
        return this.log.getCurrentRevisionNumber();
    }
    
    @Override
    public XChangeLogState getChangeLogState() {
        return this.log.getChangeLogState();
    }
    
}
