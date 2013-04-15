package org.xydra.core.model.impl.memory;

import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XEntity;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XFieldSyncEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XModelSyncEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XObjectSyncEventListener;
import org.xydra.core.change.XRepositoryEventListener;
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
    
    public boolean addListenerForFieldSyncEvents(XEntity entity,
            XFieldSyncEventListener syncListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.FieldSync, entity, syncListener);
        }
    }
    
    public boolean addListenerForModelEvents(XEntity entity, XModelEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.ModelChange, entity, changeListener);
        }
    }
    
    public boolean addListenerForModelSyncEvents(XEntity entity,
            XModelSyncEventListener syncListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.ModelSync, entity, syncListener);
        }
    }
    
    public boolean addListenerForObjectEvents(XEntity entity, XObjectEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.ObjectChange, entity, changeListener);
        }
    }
    
    public boolean addListenerForObjectSyncEvents(XEntity entity,
            XObjectSyncEventListener syncListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.ObjectSync, entity, syncListener);
        }
    }
    
    public boolean addListenerForRepositoryEvents(XEntity entity,
            XRepositoryEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.RepositoryChange, entity, changeListener);
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
    
    public void fireFieldSyncEvent(XEntity entity, XFieldEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.FieldSync, entity, event);
        }
    }
    
    public void fireModelEvent(XEntity entity, XModelEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.ModelChange, entity, event);
        }
    }
    
    public void fireModelSyncEvent(XEntity entity, XModelEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.ModelSync, entity, event);
        }
    }
    
    public void fireObjectEvent(XEntity entity, XObjectEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.ObjectChange, entity, event);
        }
    }
    
    public void fireObjectSyncEvent(XEntity entity, XObjectEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.ObjectSync, entity, event);
        }
    }
    
    public void fireRepositoryEvent(XRepository entity, XRepositoryEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.RepositoryChange, entity, event);
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
    
    public boolean removeListenerForFieldSyncEvents(XEntity entity,
            XFieldSyncEventListener syncListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.FieldSync, entity, syncListener);
        }
    }
    
    public boolean removeListenerForModelEvents(XEntity entity, XModelEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.ModelChange, entity, changeListener);
        }
    }
    
    public boolean removeListenerForModelSyncEvents(XEntity entity,
            XModelSyncEventListener syncListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.ModelSync, entity, syncListener);
        }
    }
    
    public boolean removeListenerForObjectEvents(XEntity entity, XObjectEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.ObjectChange, entity, changeListener);
        }
    }
    
    public boolean removeListenerForObjectSyncEvents(XEntity entity,
            XObjectSyncEventListener syncListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.ObjectSync, entity, syncListener);
        }
    }
    
    public boolean removeListenerForRepositoryEvents(XRepository entity,
            XRepositoryEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.RepositoryChange, entity, changeListener);
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
