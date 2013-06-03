package org.xydra.base.change.impl.memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
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
public abstract class AbstractTransactionEvent implements XTransactionEvent {
    
    private static final long serialVersionUID = -3997550048059538237L;
    
    private XId actor;
    
    // the revision numbers before the event happened
    private long modelRevision, objectRevision;
    
    /** The XAddress of the model or object this transaction applies to */
    private XAddress target;
    
    /** For GWT only */
    protected AbstractTransactionEvent() {
    }
    
    protected AbstractTransactionEvent(XId actor, XAddress target, long modelRevision,
            long objectRevision) {
        XyAssert.xyAssert(actor != null);
        XyAssert.xyAssert(target != null);
        assert target != null;
        
        if((target.getModel() == null && target.getObject() == null) || target.getField() != null) {
            throw new IllegalArgumentException("target must be a model or object, was:" + target);
        }
        
        if(target.getObject() == null) {
            if(objectRevision >= 0) {
                throw new IllegalArgumentException(
                        "object revision must not be set for model transactions");
            }
            if(modelRevision < -1) {
                throw new IllegalArgumentException(
                        "model revision must be set for model transactions");
            }
        } else if(objectRevision < -1) {
            throw new IllegalArgumentException(
                    "object revision must be set for object transactions, was " + objectRevision);
        }
        
        this.actor = actor;
        this.target = target;
        this.modelRevision = modelRevision;
        this.objectRevision = objectRevision;
    }
    
    private static enum TxnChangeType {
        REMOVE, ADD_OR_CHANGE
    }
    
    /**
     * @return true if this transaction event is the result of a valid model /
     *         object transaction. Throws an {@link AssertionError} otherwise.
     *         Assumes the transaction is minimal.
     */
    protected boolean assertIsCorrect() {
        
        /**
         * Verify that no entity is added/changed AND removed in the same txn.
         * false = remove, true = add or change
         */
        Map<XAddress,TxnChangeType> entities = new HashMap<XAddress,TxnChangeType>();
        for(XAtomicEvent event : this) {
            for(XAddress addr = event.getTarget(); addr != null; addr = addr.getParent()) {
                if(Boolean.FALSE.equals(entities.get(addr))) {
                    assert false : "modified entity after remove";
                }
                entities.put(addr, TxnChangeType.ADD_OR_CHANGE);
            }
            if(!(event instanceof XFieldEvent)) {
                TxnChangeType value;
                XAddress entity = event.getChangedEntity();
                if(event.getChangeType() == ChangeType.REMOVE) {
                    value = TxnChangeType.REMOVE;
                } else {
                    value = TxnChangeType.ADD_OR_CHANGE;
                    assert entities.get(entity) == null : "adding already touched entity";
                }
                entities.put(entity, value);
            }
        }
        
        /* check if implied events are marked correctly */
        for(XAtomicEvent event : this) {
            boolean implied = false;
            boolean lowestInMofHierarchy = true;
            for(XAddress addr = event.getTarget(); addr != null; addr = addr.getParent()) {
                if(TxnChangeType.REMOVE.equals(entities.get(addr))) {
                    implied = true;
                    assert lowestInMofHierarchy : "removed an entity but not all children";
                } else {
                    lowestInMofHierarchy = false;
                }
            }
            assert event.isImplied() == implied : "event has incorrect implied flag: " + event
                    + " expected=" + implied;
        }
        
        return true;
    }
    
    /**
     * @return true if this transaction contains any redundant events. Throws an
     *         {@link AssertionError} otherwise.
     */
    protected boolean assertIsMinimal() {
        
        Set<XAddress> entities = new HashSet<XAddress>();
        Set<XAddress> values = new HashSet<XAddress>();
        
        for(XAtomicEvent event : this) {
            // FIXME ####
            System.out.println("EVENT " + event);
            
            if(event instanceof XFieldEvent) {
                assert !values.contains(event.getTarget()) : "changed value of field twice "
                        + event.getTarget();
                values.add(event.getTarget());
            } else {
                XAddress addr = event.getChangedEntity();
                assert !entities.contains(addr) : "added and removed entity in same transaction "
                        + "or added / removed entity twice. Second event: " + event;
                entities.add(addr);
            }
            
        }
        
        return true;
    }
    
    @Override
    public boolean equals(Object object) {
        
        if(object == null) {
            return false;
        }
        
        if(!(object instanceof XTransactionEvent)) {
            return false;
        }
        XTransactionEvent trans = (XTransactionEvent)object;
        
        if(size() != trans.size()) {
            return false;
        }
        
        if(!this.target.equalsOrContains(trans.getTarget())
                && !trans.getTarget().contains(this.target)) {
            return false;
        }
        
        if(!XI.equals(this.actor, trans.getActor())) {
            return false;
        }
        
        if(this.modelRevision != trans.getOldModelRevision()) {
            return false;
        }
        
        if(this.target.getObject() != null && trans.getTarget().getObject() != null) {
            if(this.objectRevision != trans.getOldObjectRevision()) {
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
    public XId getActor() {
        return this.actor;
    }
    
    @Override
    public XAddress getChangedEntity() {
        return this.target;
    }
    
    @Override
    public ChangeType getChangeType() {
        return ChangeType.TRANSACTION;
    }
    
    @Override
    public long getOldFieldRevision() {
        return XEvent.REVISION_OF_ENTITY_NOT_SET;
    }
    
    @Override
    public long getOldModelRevision() {
        return this.modelRevision;
    }
    
    @Override
    public long getOldObjectRevision() {
        return this.objectRevision;
    }
    
    @Override
    public long getRevisionNumber() {
        
        if(this.modelRevision >= 0) {
            return this.modelRevision + 1;
        }
        
        if(this.objectRevision >= 0) {
            return this.objectRevision + 1;
        }
        
        return 0;
    }
    
    @Override
    public XAddress getTarget() {
        return this.target;
    }
    
    @Override
    public int hashCode() {
        
        int result = 0;
        
        result ^= size();
        
        // target
        XId repoId = this.target.getRepository();
        if(repoId != null) {
            result ^= repoId.hashCode();
        }
        XId modelId = this.target.getModel();
        if(modelId != null) {
            result ^= modelId.hashCode();
        }
        
        // actor
        result ^= (this.actor != null) ? this.actor.hashCode() : 0;
        
        // old revisions
        result += this.modelRevision;
        if(this.modelRevision == XEvent.REVISION_OF_ENTITY_NOT_SET) {
            result += this.objectRevision;
        }
        
        return result;
    }
    
    @Override
    public boolean inTransaction() {
        // transactions never occur as events during transactions
        return false;
    }
    
    @Override
    public boolean isImplied() {
        return false;
    }
    
    @Override
    public String toString() {
        String str = "TransactionEvent by actor '" + this.actor + "' @" + this.target + " r";
        str += (this.modelRevision < 0 ? "-" : this.modelRevision);
        // if(this.objectRevision >= 0) {
        str += "/" + this.objectRevision;
        // }
        str += ": [\n";
        boolean b = false;
        for(XAtomicEvent event : this) {
            if(b) {
                str += ", \n";
            }
            b = true;
            str += event.toString();
        }
        return str + "]";
    }
    
    @CanBeNull
    public XAtomicEvent getLastEvent() {
        int size = size();
        if(size > 0) {
            return getEvent(size - 1);
        } else {
            return null;
        }
    }
    
}
