package org.xydra.core.model.impl.memory.garbage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.annotations.ReadOperation;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandUtils;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableRepository;
import org.xydra.base.rmof.impl.memory.SimpleRepository;
import org.xydra.core.XX;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XChangeLogState;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.AbstractEntity;
import org.xydra.core.model.impl.memory.IMemoryModel;
import org.xydra.core.model.impl.memory.IMemoryRepository;
import org.xydra.core.model.impl.memory.MemoryChangeLogState;
import org.xydra.core.model.impl.memory.MemoryEventBus;
import org.xydra.core.model.impl.memory.MemoryField;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;
import org.xydra.core.model.impl.memory.MemoryEventBus.EventType;
import org.xydra.sharedutils.XyAssert;


/**
 * An implementation of {@link XRepository}.
 * 
 * <h3>Serialisation</h3> A {@link OldMemoryRepository} is not expected to be
 * used in GWT RPC. Although it might work. Instead, serialise the smaller
 * {@link SimpleRepository} or {@link XRevWritableRepository} instances.
 * 
 * @author voelkel
 * @author Kaidel
 */
public class OldMemoryRepository extends AbstractEntity implements IMemoryRepository, XRepository,
        Serializable {
    
    private static final long serialVersionUID = 2412386047787717740L;
    
    private MemoryEventBus eventBus = new MemoryEventBus();
    
    /* in-memory cache to instantiate models only once */
    private final Map<XId,IMemoryModel> loadedModels = new HashMap<XId,IMemoryModel>();
    
    private XId sessionActor;
    
    private String sessionPasswordHash;
    
    private final XRevWritableRepository state;
    
    /**
     * Creates a new MemoryRepository.
     * 
     * @param actorId the actor doing the commands; relevant for access rights.
     * @param passwordHash
     * @param repositoryId The {@link XId} for this MemoryRepository.
     */
    public OldMemoryRepository(XId actorId, String passwordHash, XId repositoryId) {
        this(actorId, passwordHash, new SimpleRepository(XX.toAddress(repositoryId, null, null,
                null)));
    }
    
    /**
     * Creates a new {@link OldMemoryRepository}.
     * 
     * @param actorId the actor doing the commands; relevant for access rights.
     * @param passwordHash
     * @param repositoryState The initial {@link XRevWritableRepository} state
     *            of this MemoryRepository.
     */
    public OldMemoryRepository(XId actorId, String passwordHash,
            XRevWritableRepository repositoryState) {
        super();
        XyAssert.xyAssert(repositoryState != null);
        assert repositoryState != null;
        
        XyAssert.xyAssert(actorId != null);
        assert actorId != null;
        this.sessionActor = actorId;
        this.sessionPasswordHash = passwordHash;
        this.state = repositoryState;
    }
    
    @Override
    public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.FieldChange, getAddress(), changeListener);
        }
    }
    
    @Override
    public boolean addListenerForModelEvents(XModelEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.ModelChange, getAddress(), changeListener);
        }
    }
    
    @Override
    public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.ObjectChange, getAddress(), changeListener);
        }
    }
    
    @Override
    public boolean addListenerForRepositoryEvents(XRepositoryEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.RepositoryChange, getAddress(),
                    changeListener);
        }
    }
    
    @Override
    public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.addListener(EventType.TransactionChange, getAddress(),
                    changeListener);
        }
    }
    
    @Override
    public IMemoryModel createModel(XId modelId) {
        XRepositoryCommand command = MemoryRepositoryCommand.createAddCommand(getAddress(), true,
                modelId);
        
        // synchronize so that return is never null if command succeeded
        synchronized(this) {
            long result = executeRepositoryCommand(command);
            IMemoryModel model = getModel(modelId);
            XyAssert.xyAssert(result == XCommand.FAILED || model != null,
                    "failed?" + XCommandUtils.failed(result) + " model null?" + (model == null));
            return model;
        }
    }
    
    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }
    
    @Override
    public long executeCommand(XCommand command) {
        return executeCommand(command, null);
    }
    
    @Override
    public long executeRepositoryCommand(XRepositoryCommand command) {
        return executeCommand(command, null);
    }
    
    @Override
    public long executeCommand(XCommand command, XLocalChangeCallback callback) {
        /*
         * find out which model should handle it, defer all execution and error
         * checking there
         */
        XAddress changed = command.getChangedEntity();
        XId repoId = changed.getRepository();
        assert repoId != null : "executing a command on a repo imlpies a repoId is there";
        XId modelId = changed.getModel();
        assert modelId != null : "all commands have a modelId";
        IMemoryModel model = this.getModel(modelId);
        if(model == null) {
            // id is not taken yet
            model = MemoryModel.createNonExistantModel(this, this.sessionActor,
                    this.sessionPasswordHash, repoId, modelId);
        }
        long result = model.executeCommandWithActor(command, this.sessionActor,
                this.sessionPasswordHash, callback);
        assert XCommandUtils.success(result);
        
        // fire events
        XEvent event = model.getChangeLog().getEventAt(result);
        fireEvent(event);
        return result;
    }
    
    private void fireEvent(XEvent event) {
        if(event instanceof XTransactionEvent) {
            fireTransactionEvent((XTransactionEvent)event);
        } else if(event instanceof XRepositoryEvent) {
            fireRepositoryEvent((XRepositoryEvent)event);
        } else if(event instanceof XModelEvent) {
            fireModelEvent((XModelEvent)event);
        } else if(event instanceof XObjectEvent) {
            fireObjectEvent((XObjectEvent)event);
        } else if(event instanceof XFieldEvent) {
            fireFieldEvent((XFieldEvent)event);
        }
    }
    
    // implement IMemoryRepository
    @Override
    public void addModel(IMemoryModel model) {
        // link pure state
        this.state.addModel(model.getState());
        
        // in memory
        this.loadedModels.put(model.getId(), model);
    }
    
    /**
     * Notifies all listeners that have registered interest for notification on
     * {@link XFieldEvent XFieldEvents} happening on child- {@link MemoryField
     * MemoryFields} of this MemoryRepository.
     * 
     * @param event The {@link XFieldEvent} which will be propagated to the
     *            registered listeners.
     */
    // implement IMemoryRepository
    @Override
    public void fireFieldEvent(XFieldEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.FieldChange, getAddress(), event);
        }
    }
    
    /**
     * Notifies all listeners that have registered interest for notification on
     * {@link XModelEvent XModelEvents} happening on child- {@link MemoryModel
     * MemoryModels} of this MemoryRepository.
     * 
     * @param event The {@link XModelEvent} which will be propagated to the
     *            registered listeners.
     */
    // implement IMemoryRepository
    @Override
    public void fireModelEvent(XModelEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.ModelChange, getAddress(), event);
        }
        
    }
    
    /**
     * Notifies all listeners that have registered interest for notification on
     * {@link XObjectEvent XObjectEvents} happening on child-
     * {@link MemoryObject MemoryObjects} of this MemoryRepository.
     * 
     * @param event The {@link XObjectEvent} which will be propagated to the
     *            registered listeners.
     */
    // implement IMemoryRepository
    @Override
    public void fireObjectEvent(XObjectEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.ObjectChange, getAddress(), event);
        }
    }
    
    /**
     * Notifies all listeners that have registered interest for notification on
     * {@link XRepositoryEvent XRepositoryEvents} happening on this
     * MemoryRepository.
     * 
     * @param event The {@link XRepositoryEvent} which will be propagated to the
     *            registered listeners.
     */
    // implement IMemoryRepository
    @Override
    public void fireRepositoryEvent(XRepositoryEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.RepositoryChange, getAddress(), event);
        }
    }
    
    /**
     * Notifies all listeners that have registered interest for notification on
     * {@link XTransactionEvent XTransactionEvents} happening on child-
     * {@link MemoryModel MemoryModels} or child- {@link MemoryObject
     * MemoryObjects} of this MemoryRepository.
     * 
     * @param event The {@link XModelEvent} which will be propagated to the
     *            registered listeners.
     */
    // implement IMemoryRepository
    @Override
    public void fireTransactionEvent(XTransactionEvent event) {
        synchronized(this.eventBus) {
            this.eventBus.fireEvent(EventType.TransactionChange, getAddress(), event);
        }
    }
    
    @Override
    public XAddress getAddress() {
        return this.state.getAddress();
    }
    
    @Override
    public synchronized XId getId() {
        return this.state.getId();
    }
    
    @Override
    @ReadOperation
    public synchronized IMemoryModel getModel(XId modelId) {
        // use cached instance?
        IMemoryModel model = this.loadedModels.get(modelId);
        if(model != null) {
            return model;
        }
        
        // try to wrap modelState, if present
        XRevWritableModel modelState = this.state.getModel(modelId);
        if(modelState == null) {
            return null;
        }
        
        XChangeLogState log = new MemoryChangeLogState(modelState.getAddress());
        log.setBaseRevisionNumber(modelState.getRevisionNumber());
        
        model = new MemoryModel(this, this.sessionActor, this.sessionPasswordHash, modelState, log);
        this.loadedModels.put(modelId, model);
        
        return model;
    }
    
    /*
     * Why do repositories have no revision number? -- Because everything that
     * uses revision numbers (synchronizing, change logs, transactions, locking
     * in GAE) is per model. Having revision numbers (with a semantic consistent
     * with existing model, object and field revisions) for repositories would
     * mean increasing the revision for every repository, model, object and
     * field change, which would quickly become a bottleneck.
     */
    @Override
    public long getRevisionNumber() {
        return 0;
    }
    
    @Override
    public XId getSessionActor() {
        return this.sessionActor;
    }
    
    @Override
    public XType getType() {
        return XType.XREPOSITORY;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public synchronized boolean hasModel(XId id) {
        return this.loadedModels.containsKey(id) || this.state.hasModel(id);
    }
    
    @Override
    public synchronized boolean isEmpty() {
        return this.state.isEmpty();
    }
    
    @Override
    @ReadOperation
    public synchronized Iterator<XId> iterator() {
        return this.state.iterator();
    }
    
    @Override
    public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus
                    .removeListener(EventType.FieldChange, getAddress(), changeListener);
        }
    }
    
    @Override
    public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus
                    .removeListener(EventType.ModelChange, getAddress(), changeListener);
        }
    }
    
    @Override
    public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.ObjectChange, getAddress(),
                    changeListener);
        }
    }
    
    @Override
    public boolean removeListenerForRepositoryEvents(XRepositoryEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.RepositoryChange, getAddress(),
                    changeListener);
        }
    }
    
    @Override
    public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
        synchronized(this.eventBus) {
            return this.eventBus.removeListener(EventType.TransactionChange, getAddress(),
                    changeListener);
        }
    }
    
    @Override
    public boolean removeModel(XId modelId) {
        /*
         * no synchronization necessary here (except that in
         * executeRepositoryCommand())
         */
        XRepositoryCommand command = MemoryRepositoryCommand.createRemoveCommand(getAddress(),
                XCommand.FORCED, modelId);
        
        long result = executeRepositoryCommand(command);
        XyAssert.xyAssert(result >= 0 || result == XCommand.NOCHANGE);
        return result != XCommand.NOCHANGE;
    }
    
    // implement IMemoryRepository
    @Override
    public boolean removeModelInternal(XId modelId) {
        this.loadedModels.remove(modelId);
        return this.state.removeModel(modelId);
    }
    
    @Override
    public void setSessionActor(XId actorId, String passwordHash) {
        XyAssert.xyAssert(actorId != null);
        assert actorId != null;
        this.sessionActor = actorId;
        this.sessionPasswordHash = passwordHash;
        for(XModel model : this.loadedModels.values()) {
            model.setSessionActor(actorId, passwordHash);
        }
    }
    
    // implement IMemoryRepository
    @Override
    public synchronized void updateRemoved(IMemoryModel model) {
        XyAssert.xyAssert(model != null);
        assert model != null;
        synchronized(model.getRoot()) {
            XId modelId = model.getId();
            boolean hasModel = hasModel(modelId);
            if(!model.exists() && hasModel) {
                this.state.removeModel(modelId);
                this.loadedModels.remove(modelId);
            } else if(model.exists() && !hasModel) {
                this.state.addModel(model.getState());
                this.loadedModels.put(model.getId(), model);
            }
            model.getSyncState().eventQueue.sendEvents();
        }
    }
    
    @Override
    public XRevWritableRepository getState() {
        return this.state;
    }
}
