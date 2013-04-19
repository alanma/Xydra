package org.xydra.core.model.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
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
import org.xydra.core.model.XRepository;
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
     * @param writableChangeLog
     * @param localChanges
     * @param sessionActor
     */
    public Root(MemoryEventBus eventBus, XWritableChangeLog writableChangeLog,
            LocalChanges localChanges, XId sessionActor) {
        this.eventBus = eventBus;
        this.writableChangeLog = writableChangeLog;
        this.localChanges = localChanges;
        this.sessionActor = sessionActor;
        this.isTransactionInProgress = false;
    }
    
    private final MemoryEventBus eventBus;
    private XId sessionActor;
    private boolean isTransactionInProgress;
    private final XWritableChangeLog writableChangeLog;
    private final LocalChanges localChanges;
    private String sessionPasswordHash;
    
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
            this.eventBus.fireEvent(EventType.FieldChange, entityAddress, event);
        }
    }
    
    public void fireModelEvent(XAddress entityAddress, XModelEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.ModelChange, entityAddress, event);
        }
    }
    
    public void fireObjectEvent(XAddress entityAddress, XObjectEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.ObjectChange, entityAddress, event);
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
    void setSessionActor(XId actor) {
        this.sessionActor = actor;
    }
    
    /**
     * @param baseAddress
     * @param actorId
     * @return ...
     */
    public static Root createWithActor(XAddress baseAddress, XId actorId) {
        return new Root(new MemoryEventBus(), MemoryChangeLog.create(baseAddress),
                LocalChanges.create(), actorId);
    }
    
    public XWritableChangeLog getWritableChangeLog() {
        return this.writableChangeLog;
    }
    
    public int countUnappliedLocalChanges() {
        return this.localChanges.countUnappliedLocalChanges();
    }
    
    public LocalChanges getLocalChanges() {
        return this.localChanges;
    }
    
}
