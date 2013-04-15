package org.xydra.core.model.impl.memory;

import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XSyncEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XEntity;
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
    
    private MemoryEventBus eventBus = new MemoryEventBus();
    
    public boolean addListenerForFieldEvents(XEntity entity, XFieldEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.FieldChange, entity, changeListener);
        }
    }
    
    public boolean addListenerForModelEvents(XEntity entity, XModelEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.ModelChange, entity, changeListener);
        }
    }
    
    public boolean addListenerForObjectEvents(XEntity entity, XObjectEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.ObjectChange, entity, changeListener);
        }
    }
    
    public boolean addListenerForRepositoryEvents(XEntity entity,
            XRepositoryEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.RepositoryChange, entity, changeListener);
        }
    }
    
    public boolean addListenerForSyncEvents(XEntity entity, XSyncEventListener syncListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.Sync, entity, syncListener);
        }
    }
    
    public boolean addListenerForTransactionEvents(XEntity entity,
            XTransactionEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.TransactionChange, entity, changeListener);
        }
    }
    
    public void fireFieldEvent(XEntity entity, XFieldEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.FieldChange, entity, event);
        }
    }
    
    public void fireModelEvent(XEntity entity, XModelEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.ModelChange, entity, event);
        }
    }
    
    public void fireObjectEvent(XEntity entity, XObjectEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.ObjectChange, entity, event);
        }
    }
    
    public void fireRepositoryEvent(XRepository entity, XRepositoryEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.RepositoryChange, entity, event);
        }
    }
    
    public void fireSyncEvent(XEntity entity, XSyncEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.Sync, entity, event);
        }
    }
    
    public void fireTransactionEvent(XEntity entity, XTransactionEvent event) {
        this.eventBus.fireEvent(EventType.TransactionChange, entity, event);
    }
    
    public boolean removeListenerForFieldEvents(XEntity entity, XFieldEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.FieldChange, entity, changeListener);
        }
    }
    
    public boolean removeListenerForModelEvents(XEntity entity, XModelEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.ModelChange, entity, changeListener);
        }
    }
    
    public boolean removeListenerForObjectEvents(XEntity entity, XObjectEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.ObjectChange, entity, changeListener);
        }
    }
    
    public boolean removeListenerForRepositoryEvents(XRepository entity,
            XRepositoryEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.RepositoryChange, entity, changeListener);
        }
    }
    
    public boolean removeListenerForSyncEvents(XEntity entity, XSyncEventListener syncListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.Sync, entity, syncListener);
        }
    }
    
    public boolean removeListenerForTransactionEvents(XEntity entity,
            XTransactionEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus
                    .removeListener(EventType.TransactionChange, entity, changeListener);
        }
    }
    
}
