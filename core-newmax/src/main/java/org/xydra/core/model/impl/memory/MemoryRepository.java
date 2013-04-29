package org.xydra.core.model.impl.memory;

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
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.base.rmof.XRevWritableRepository;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.base.rmof.impl.XExistsRevWritableRepository;
import org.xydra.base.rmof.impl.memory.SimpleRepository;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryEventBus.EventType;
import org.xydra.sharedutils.XyAssert;


/**
 * An implementation of {@link XRepository}.
 * 
 * <h3>Serialisation</h3> A {@link MemoryRepository} is not expected to be used
 * in GWT RPC. Although it might work. Instead, serialise the smaller
 * {@link SimpleRepository} or {@link XRevWritableRepository} instances.
 * 
 * @author voelkel
 * @author Kaidel
 */
public class MemoryRepository extends AbstractEntity implements IMemoryRepository, XRepository,
        Serializable {
    
    private static final long serialVersionUID = 2412386047787717740L;
    
    private MemoryEventBus eventBus = new MemoryEventBus();
    
    /* in-memory cache to instantiate models only once */
    private final Map<XId,IMemoryModel> loadedModels = new HashMap<XId,IMemoryModel>();
    
    private XId sessionActor;
    
    @SuppressWarnings("unused")
    private String sessionPasswordHash;
    
    private final XExistsRevWritableRepository repositoryState;
    
    /**
     * Creates a new MemoryRepository.
     * 
     * @param actorId the actor doing the commands; relevant for access rights.
     * @param passwordHash
     * @param repositoryId The {@link XId} for this MemoryRepository.
     */
    public MemoryRepository(XId actorId, String passwordHash, XId repositoryId) {
        this(actorId, passwordHash, new SimpleRepository(XX.toAddress(repositoryId, null, null,
                null)));
    }
    
    /**
     * Creates a new {@link MemoryRepository}.
     * 
     * @param actorId the actor doing the commands; relevant for access rights.
     * @param passwordHash
     * @param repositoryState The initial {@link XRevWritableRepository} state
     *            of this MemoryRepository.
     */
    public MemoryRepository(XId actorId, String passwordHash, XReadableRepository repositoryState) {
        super();
        XyAssert.xyAssert(repositoryState != null);
        assert repositoryState != null;
        
        XyAssert.xyAssert(actorId != null);
        assert actorId != null;
        this.sessionActor = actorId;
        this.sessionPasswordHash = passwordHash;
        if(repositoryState instanceof XExistsRevWritableRepository) {
            this.repositoryState = (XExistsRevWritableRepository)repositoryState;
        } else {
            this.repositoryState = XCopyUtils.cloneRepository(repositoryState);
        }
        
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
        // synchronise so that return is never null if command succeeded
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
    public long executeRepositoryCommand(XRepositoryCommand command) {
        return executeCommand(command);
    }
    
    @Override
    public long executeCommand(XCommand command) {
        /*
         * find out which model should handle it, defer all execution and error
         * checking there
         */
        XAddress modelAddress = command.getChangedEntity();
        XId repoId = modelAddress.getRepository();
        assert repoId != null : "executing a command on a repo implies a repoId is there";
        
        XId modelId = modelAddress.getModel();
        assert modelId != null : "all commands have a modelId";
        
        IMemoryModel model = getModel(modelId);
        if(model == null) {
            // id is not taken yet
            model = MemoryModel.createNonExistantModel(getSessionActor(), this, modelId);
        }
        assert model != null;
        assert model.getState() != null;
        
        long result = model.executeCommand(command);
        assert XCommandUtils.success(result);
        
        if(XCommandUtils.changedSomething(result)) {
            // change repo state
            switch(command.getChangeType()) {
            case ADD:
                this.repositoryState.createModel(modelId);
                break;
            case REMOVE:
                this.loadedModels.remove(modelId);
                this.repositoryState.removeModel(modelId);
                break;
            default:
                assert false;
            }
            
            // fire events
            XEvent event = model.getChangeLog().getEventAt(result);
            fireEvent(event);
        }
        
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
        this.repositoryState.addModel(model.getState());
        
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
        return this.repositoryState.getAddress();
    }
    
    @Override
    public synchronized XId getId() {
        return this.repositoryState.getId();
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
        XExistsRevWritableModel modelState = this.repositoryState.getModel(modelId);
        if(modelState == null) {
            return null;
        }
        
        if(!modelState.exists()) {
            return null;
        }
        
        model = new MemoryModel(getSessionActor(), this, modelState);
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
        return this.loadedModels.containsKey(id) || this.repositoryState.hasModel(id);
    }
    
    @Override
    public synchronized boolean isEmpty() {
        return this.repositoryState.isEmpty();
    }
    
    @Override
    @ReadOperation
    public synchronized Iterator<XId> iterator() {
        return this.repositoryState.iterator();
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
    
    @Override
    public XExistsRevWritableRepository getState() {
        return this.repositoryState;
    }
    
}
