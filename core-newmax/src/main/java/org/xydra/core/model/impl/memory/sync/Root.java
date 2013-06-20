package org.xydra.core.model.impl.memory.sync;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XSyncEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.change.XSyncEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XChangeLogState;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryChangeLogState;
import org.xydra.core.model.impl.memory.MemoryEventBus;
import org.xydra.core.model.impl.memory.MemoryEventBus.EventType;


/**
 * A xydra data tree can have one of these forms: R-M-O-F, M-O-F, O-F, or F. Of
 * course, R-M-O-F can also be just R, i.e. a {@link XRepository} is not
 * required to have children. All such trees use a single {@link Root} instance.
 * 
 * The root manages event sending.
 * 
 * @author xamde
 */
public class Root {
    
    /**
     * @param eventBus
     * @param syncLog
     * @param sessionActor
     */
    public Root(MemoryEventBus eventBus, ISyncLog syncLog, XId sessionActor) {
        this.eventBus = eventBus;
        this.syncLog = syncLog;
        this.sessionActor = sessionActor;
        this.isTransactionInProgress = false;
    }
    
    /** for registering listeners and firing events */
    private final MemoryEventBus eventBus;
    
    private MemoryEventBus repositoryEventBus;
    
    private XId sessionActor;
    
    private boolean isTransactionInProgress;
    
    private String sessionPasswordHash;
    
    private ISyncLog syncLog;
    
    public String getSessionPasswordHash() {
        return this.sessionPasswordHash;
    }
    
    public void setSessionPasswordHash(String sessionPasswordHash) {
        this.sessionPasswordHash = sessionPasswordHash;
    }
    
    public boolean addListenerForFieldEvents(XAddress entityAddress,
            XFieldEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.FieldChange, entityAddress, changeListener);
        }
    }
    
    public boolean addListenerForModelEvents(XAddress entityAddress,
            XModelEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.ModelChange, entityAddress, changeListener);
        }
    }
    
    public boolean addListenerForObjectEvents(XAddress entityAddress,
            XObjectEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.ObjectChange, entityAddress, changeListener);
        }
    }
    
    public boolean addListenerForRepositoryEvents(XAddress entityAddress,
            XRepositoryEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.RepositoryChange, entityAddress,
                    changeListener);
        }
    }
    
    public boolean addListenerForSyncEvents(XAddress entityAddress, XSyncEventListener syncListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.Sync, entityAddress, syncListener);
        }
    }
    
    public boolean addListenerForTransactionEvents(XAddress entityAddress,
            XTransactionEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.TransactionChange, entityAddress,
                    changeListener);
        }
    }
    
    public void fireFieldEvent(XAddress entityAddress, XFieldEvent event) {
        synchronized(this.eventBus) {
            assert entityAddress.getAddressedType() == XType.XFIELD;
            this.eventBus.fireEvent(EventType.FieldChange, entityAddress, event);
            // object
            XAddress parent = entityAddress.getParent();
            this.eventBus.fireEvent(EventType.FieldChange, parent, event);
            // model
            parent = parent.getParent();
            this.eventBus.fireEvent(EventType.FieldChange, parent, event);
            // repository
            parent = parent.getParent();
            if(this.repositoryEventBus != null) {
                this.repositoryEventBus.fireEvent(EventType.FieldChange, parent, event);
            }
        }
    }
    
    public void fireModelEvent(XAddress entityAddress, XModelEvent event) {
        synchronized(this.eventBus) {
            assert entityAddress.getAddressedType() == XType.XMODEL;
            this.eventBus.fireEvent(EventType.ModelChange, entityAddress, event);
            // repository
            XAddress parent = entityAddress.getParent();
            
            if(this.repositoryEventBus != null) {
                this.repositoryEventBus.fireEvent(EventType.ModelChange, parent, event);
            }
        }
    }
    
    public void fireObjectEvent(XAddress entityAddress, XObjectEvent event) {
        synchronized(this.eventBus) {
            assert entityAddress.getAddressedType() == XType.XOBJECT : "type is "
                    + entityAddress.getAddressedType();
            this.eventBus.fireEvent(EventType.ObjectChange, entityAddress, event);
            // model
            XAddress parent = entityAddress.getParent();
            this.eventBus.fireEvent(EventType.ObjectChange, parent, event);
            // repository
            parent = parent.getParent();
            
            if(this.repositoryEventBus != null) {
                this.repositoryEventBus.fireEvent(EventType.ObjectChange, parent, event);
            }
        }
    }
    
    public void fireRepositoryEvent(XAddress entityAddress, XRepositoryEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.RepositoryChange, entityAddress, event);
        }
    }
    
    public void fireSyncEvent(XAddress entityAddress, XSyncEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.Sync, entityAddress, event);
        }
    }
    
    public void fireTransactionEvent(XAddress entityAddress, XTransactionEvent event) {
        this.eventBus.fireEvent(EventType.TransactionChange, entityAddress, event);
    }
    
    public boolean removeListenerForFieldEvents(XAddress entityAddress,
            XFieldEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.FieldChange, entityAddress,
                    changeListener);
        }
    }
    
    public boolean removeListenerForModelEvents(XAddress entityAddress,
            XModelEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.ModelChange, entityAddress,
                    changeListener);
        }
    }
    
    public boolean removeListenerForObjectEvents(XAddress entityAddress,
            XObjectEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.ObjectChange, entityAddress,
                    changeListener);
        }
    }
    
    public boolean removeListenerForRepositoryEvents(XAddress entityAddress,
            XRepositoryEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.RepositoryChange, entityAddress,
                    changeListener);
        }
    }
    
    public boolean removeListenerForSyncEvents(XAddress entityAddress,
            XSyncEventListener syncListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.Sync, entityAddress, syncListener);
        }
    }
    
    public boolean removeListenerForTransactionEvents(XAddress entityAddress,
            XTransactionEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.TransactionChange, entityAddress,
                    changeListener);
        }
    }
    
    public boolean isTransactionInProgess() {
        return this.isTransactionInProgress;
    }
    
    public void setTransactionInProgress(boolean b) {
        this.isTransactionInProgress = b;
    }
    
    public XId getSessionActor() {
        return this.sessionActor;
    }
    
    /**
     * Set a new actor to be used when building commands for changes.
     * 
     * @param actor for this field.
     */
    public void setSessionActor(XId actor) {
        this.sessionActor = actor;
    }
    
    /**
     * @param actorId @NeverNull
     * @param baseAddress for sync log @NeverNull
     * @param changeLogBaseRevision
     * @return ...
     */
    public static Root createWithActor(XId actorId, XAddress baseAddress, long changeLogBaseRevision) {
        return new Root(new MemoryEventBus(), MemorySyncLog.create(baseAddress,
                changeLogBaseRevision), actorId);
    }
    
    /**
     * @param actorId @NeverNull
     * @param baseAddress for sync log @NeverNull
     * @param changeLogState @CanBeNull
     * @return ...
     */
    public static Root createWithActorAndChangeLogState(XId actorId, XAddress baseAddress,
            XChangeLogState changeLogState) {
        XChangeLogState usedChangeLogState = changeLogState == null ? new MemoryChangeLogState(
                baseAddress) : changeLogState;
        
        return new Root(
        
        new MemoryEventBus(),
        
        new MemorySyncLog(usedChangeLogState),
        
        actorId);
    }
    
    public int countUnappliedLocalChanges() {
        return this.syncLog.countUnappliedLocalChanges();
    }
    
    public void startExecutingTransaction() {
        this.isTransactionInProgress = true;
    }
    
    public void stopExecutingTransaction() {
        this.isTransactionInProgress = false;
    }
    
    private boolean locked = false;
    
    public void lock() {
        this.locked = true;
    }
    
    public void unlock() {
        this.locked = false;
    }
    
    public boolean isLocked() {
        return this.locked;
    }
    
    public ISyncLog getSyncLog() {
        return this.syncLog;
    }
    
    public long getSynchronizedRevision() {
        return this.syncLog.getSynchronizedRevision();
    }
    
    public void registerRepositoryEventBus(MemoryEventBus repoBus) {
        this.repositoryEventBus = repoBus;
    }
    
    @Override
    public String toString() {
        return "inTrans?" + this.isTransactionInProgress + " locked?" + this.locked + " "
                + this.syncLog.toString();
    }
}
