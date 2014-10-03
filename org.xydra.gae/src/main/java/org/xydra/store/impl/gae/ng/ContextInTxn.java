package org.xydra.store.impl.gae.ng;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XStateWritableModel;
import org.xydra.base.rmof.XStateWritableObject;
import org.xydra.base.rmof.impl.XExists;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.ChangedObject;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * The execution context within a transaction. This is where things change while
 * things outside this context remain stable, so that before and after-effect
 * can be calculated.
 * 
 * @author xamde
 */
public class ContextInTxn implements XStateWritableModel, XExists {
    
    private static final Logger log = LoggerFactory.getLogger(ContextInTxn.class);
    
    private ChangedModel changedModel;
    
    @Override
	public XStateWritableObject createObject(XId objectId) {
        return this.changedModel.createObject(objectId);
    }
    
    @Override
	public XAddress getAddress() {
        return this.changedModel.getAddress();
    }
    
    @Override
	public XId getId() {
        return this.changedModel.getId();
    }
    
    @Override
	public boolean hasObject(XId objectId) {
        return this.changedModel.hasObject(objectId);
    }
    
    @Override
	public XStateWritableObject getObject(XId objectId) {
        return this.changedModel.getObject(objectId);
    }
    
    @Override
	public boolean isEmpty() {
        return this.changedModel.isEmpty();
    }
    
    @Override
	public Iterator<XId> iterator() {
        return this.changedModel.iterator();
    }
    
    @Override
	public XType getType() {
        return this.changedModel.getType();
    }
    
    @Override
	public boolean removeObject(XId objectId) {
        return this.changedModel.removeObject(objectId);
    }
    
    public ContextInTxn(@NeverNull ContextBeforeCommand ctxBeforeCmd) {
        XyAssert.xyAssert(ctxBeforeCmd != null);
        assert ctxBeforeCmd != null;
        
        this.changedModel = new ChangedModel(ctxBeforeCmd);
        assert this.changedModel.exists() == ctxBeforeCmd.exists();
        log.trace("At context creation time model '" + this.changedModel.getAddress()
                + "' exists: " + this.changedModel.exists());
    }
    
    /**
     * @return true if there is at least one event caused by changes
     */
    public boolean hasChanges() {
        return this.changedModel.hasChanges();
    }
    
    @Override
	public void setExists(boolean modelExists) {
        this.changedModel.setExists(modelExists);
    }
    
    @Override
	public boolean exists() {
        return this.changedModel.exists();
    }
    
    public @NeverNull
    List<XAtomicEvent> toEvents(XId actorId, ContextBeforeCommand ctxBeforeCommand,
            boolean inTransaction) {
        XyAssert.xyAssert(this.getAddress() != null);
        
        List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
        boolean inTxn = inTransaction || this.changedModel.countCommandsNeeded(2) > 1;
        DeltaUtils.createEventsForChangedModel(events, actorId, this.changedModel, inTxn
                || this.changedModel.modelWasRemoved());
        return events;
    }
    
    public Collection<? extends XReadableObject> getAdded() {
        return this.changedModel.getAdded();
    }
    
    public Collection<XId> getRemoved() {
        return this.changedModel.getRemoved();
    }
    
    public Iterable<ChangedObject> getChanged() {
        return this.changedModel.getChangedObjects();
    }
}
