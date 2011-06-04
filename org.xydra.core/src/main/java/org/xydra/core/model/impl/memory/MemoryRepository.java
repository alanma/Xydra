package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.ReadOperation;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableRepository;
import org.xydra.base.rmof.impl.memory.SimpleRepository;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XChangeLogState;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;


/**
 * An implementation of {@link XRepository}.
 * 
 * A {@link MemoryRepository} is not expected to be used in GWT RPC. Although it
 * might work.
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */
public class MemoryRepository extends AbstractEntity implements XRepository, Serializable {
	
	private static final long serialVersionUID = 2412386047787717740L;
	
	private Set<XFieldEventListener> fieldChangeListenerCollection;
	private final Map<XID,MemoryModel> loadedModels = new HashMap<XID,MemoryModel>();
	
	private Set<XModelEventListener> modelChangeListenerCollection;
	private Set<XObjectEventListener> objectChangeListenerCollection;
	private Set<XRepositoryEventListener> repoChangeListenerCollection;
	private XID sessionActor;
	private String sessionPasswordHash;
	
	private final XRevWritableRepository state;
	private Set<XTransactionEventListener> transactionListenerCollection;
	
	/**
	 * Creates a new MemoryRepository.
	 * 
	 * @param actorId TODO
	 * @param repositoryId The {@link XID} for this MemoryRepository.
	 */
	public MemoryRepository(XID actorId, String passwordHash, XID repositoryId) {
		this(actorId, passwordHash, new SimpleRepository(XX.toAddress(repositoryId, null, null,
		        null)));
	}
	
	/**
	 * Creates a new {@link MemoryRepository}.
	 * 
	 * @param actorId TODO
	 * @param repositoryState The initial {@link XRevWritableRepository} state
	 *            of this MemoryRepository.
	 */
	public MemoryRepository(XID actorId, String passwordHash, XRevWritableRepository repositoryState) {
		assert repositoryState != null;
		
		assert actorId != null;
		this.sessionActor = actorId;
		this.sessionPasswordHash = passwordHash;
		this.state = repositoryState;
		
		this.repoChangeListenerCollection = new HashSet<XRepositoryEventListener>();
		this.modelChangeListenerCollection = new HashSet<XModelEventListener>();
		this.objectChangeListenerCollection = new HashSet<XObjectEventListener>();
		this.fieldChangeListenerCollection = new HashSet<XFieldEventListener>();
		this.transactionListenerCollection = new HashSet<XTransactionEventListener>();
	}
	
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.fieldChangeListenerCollection) {
			return this.fieldChangeListenerCollection.add(changeListener);
		}
	}
	
	public boolean addListenerForModelEvents(XModelEventListener changeListener) {
		synchronized(this.modelChangeListenerCollection) {
			return this.modelChangeListenerCollection.add(changeListener);
		}
	}
	
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		synchronized(this.objectChangeListenerCollection) {
			return this.objectChangeListenerCollection.add(changeListener);
		}
	}
	
	public boolean addListenerForRepositoryEvents(XRepositoryEventListener changeListener) {
		synchronized(this.repoChangeListenerCollection) {
			return this.repoChangeListenerCollection.add(changeListener);
		}
	}
	
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		synchronized(this.transactionListenerCollection) {
			return this.transactionListenerCollection.add(changeListener);
		}
	}
	
	public MemoryModel createModel(XID modelId) {
		
		XRepositoryCommand command = MemoryRepositoryCommand.createAddCommand(getAddress(), true,
		        modelId);
		
		// synchronize so that return is never null if command succeeded
		synchronized(this) {
			long result = executeRepositoryCommand(command);
			MemoryModel model = getModel(modelId);
			assert result == XCommand.FAILED || model != null;
			return model;
		}
	}
	
	public void createModelInternal(XID modelId, XCommand command, XLocalChangeCallback callback) {
		
		XRepositoryEvent event = MemoryRepositoryEvent.createAddEvent(this.sessionActor,
		        getAddress(), modelId);
		
		XRevWritableModel modelState = this.state.createModel(modelId);
		
		XChangeLogState ls = new MemoryChangeLogState(modelState.getAddress());
		ls.setFirstRevisionNumber(0);
		
		ls.appendEvent(event);
		
		MemoryModel model = new MemoryModel(this.sessionActor, this.sessionPasswordHash, this,
		        modelState, ls);
		assert model.getRevisionNumber() == 0;
		
		// in memory
		this.loadedModels.put(model.getID(), model);
		
		boolean oldLogging = model.eventQueue.setLogging(false);
		model.eventQueue.enqueueRepositoryEvent(this, event);
		model.eventQueue.setLogging(oldLogging);
		
		assert model.eventQueue.getLocalChanges().isEmpty();
		model.eventQueue.newLocalChange(command, callback);
		
		model.eventQueue.setSyncRevision(-1);
		
		model.eventQueue.sendEvents();
		
	}
	
	@Override
	public boolean equals(Object object) {
		return super.equals(object);
	}
	
	public long executeCommand(XCommand command) {
		return executeCommand(command, null);
	}
	
	public long executeCommand(XCommand command, XLocalChangeCallback callback) {
		if(command instanceof XRepositoryCommand) {
			return executeRepositoryCommand((XRepositoryCommand)command, callback);
		}
		MemoryModel model = getModel(command.getTarget().getModel());
		if(model == null) {
			return XCommand.FAILED;
		}
		synchronized(model.eventQueue) {
			if(model.removed) {
				return XCommand.FAILED;
			}
			XID modelActor = model.eventQueue.getActor();
			String modelPsw = model.eventQueue.getPasswordHash();
			model.eventQueue.setSessionActor(this.sessionActor, this.sessionPasswordHash);
			
			long res = model.executeCommand(command, callback);
			// FIXME model commands executed by listeners will use the
			// repository actor
			
			model.eventQueue.setSessionActor(modelActor, modelPsw);
			return res;
		}
	}
	
	public long executeRepositoryCommand(XRepositoryCommand command) {
		return executeRepositoryCommand(command, null);
	}
	
	private synchronized long executeRepositoryCommand(XRepositoryCommand command,
	        XLocalChangeCallback callback) {
		
		if(!command.getRepositoryId().equals(getID())) {
			// given given repository-id are not consistent
			if(callback != null) {
				callback.onFailure();
			}
			return XCommand.FAILED;
		}
		
		if(command.getChangeType() == ChangeType.ADD) {
			if(hasModel(command.getModelId())) {
				// ID already taken
				if(command.isForced()) {
					/*
					 * the forced event only cares about the postcondition -
					 * that there is a model with the given ID, not about that
					 * there was no such model before
					 */
					if(callback != null) {
						callback.onSuccess(XCommand.NOCHANGE);
					}
					return XCommand.NOCHANGE;
				}
				if(callback != null) {
					callback.onFailure();
				}
				return XCommand.FAILED;
			}
			
			createModelInternal(command.getModelId(), command, callback);
			
			// Models are always created at the revision number 0.
			return 0;
			
		} else if(command.getChangeType() == ChangeType.REMOVE) {
			MemoryModel oldModel = getModel(command.getModelId());
			if(oldModel == null) {
				// ID not taken
				if(command.isForced()) {
					/*
					 * the forced event only cares about the postcondition -
					 * that there is no model with the given ID, not about that
					 * there was such a model before
					 */
					if(callback != null) {
						callback.onSuccess(XCommand.NOCHANGE);
					}
					return XCommand.NOCHANGE;
				}
				if(callback != null) {
					callback.onFailure();
				}
				return XCommand.FAILED;
			}
			if(!command.isForced() && oldModel.getRevisionNumber() != command.getRevisionNumber()) {
				return XCommand.FAILED;
			}
			
			return removeModelInternal(oldModel, command, callback);
			
		} else {
			throw new IllegalArgumentException("unknown command type: " + command);
		}
	}
	
	/**
	 * Notifies all listeners that have registered interest for notification on
	 * {@link XFieldEvent XFieldEvents} happening on child- {@link MemoryField
	 * MemoryFields} of this MemoryRepository.
	 * 
	 * @param event The {@link XFieldEvent} which will be propagated to the
	 *            registered listeners.
	 */
	protected void fireFieldEvent(XFieldEvent event) {
		synchronized(this.fieldChangeListenerCollection) {
			for(XFieldEventListener listener : this.fieldChangeListenerCollection) {
				listener.onChangeEvent(event);
			}
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
	protected void fireModelEvent(XModelEvent event) {
		synchronized(this.modelChangeListenerCollection) {
			for(XModelEventListener listener : this.modelChangeListenerCollection) {
				listener.onChangeEvent(event);
			}
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
	protected void fireObjectEvent(XObjectEvent event) {
		synchronized(this.objectChangeListenerCollection) {
			for(XObjectEventListener listener : this.objectChangeListenerCollection) {
				listener.onChangeEvent(event);
			}
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
	protected void fireRepositoryEvent(XRepositoryEvent event) {
		synchronized(this.repoChangeListenerCollection) {
			for(XRepositoryEventListener listener : this.repoChangeListenerCollection) {
				listener.onChangeEvent(event);
			}
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
	protected void fireTransactionEvent(XTransactionEvent event) {
		synchronized(this.transactionListenerCollection) {
			for(XTransactionEventListener listener : this.transactionListenerCollection) {
				listener.onChangeEvent(event);
			}
		}
	}
	
	public XAddress getAddress() {
		return this.state.getAddress();
	}
	
	public synchronized XID getID() {
		return this.state.getID();
	}
	
	@ReadOperation
	public synchronized MemoryModel getModel(XID modelId) {
		
		MemoryModel model = this.loadedModels.get(modelId);
		if(model != null) {
			return model;
		}
		
		XRevWritableModel modelState = this.state.getModel(modelId);
		if(modelState == null) {
			return null;
		}
		
		XChangeLogState log = new MemoryChangeLogState(modelState.getAddress());
		log.setFirstRevisionNumber(modelState.getRevisionNumber() + 1);
		model = new MemoryModel(this.sessionActor, this.sessionPasswordHash, this, modelState, log);
		this.loadedModels.put(modelId, model);
		
		return model;
	}
	
	@Override
	public XID getSessionActor() {
		return this.sessionActor;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	public synchronized boolean hasModel(XID id) {
		return this.loadedModels.containsKey(id) || this.state.hasModel(id);
	}
	
	public synchronized boolean isEmpty() {
		return this.state.isEmpty();
	}
	
	@ReadOperation
	public synchronized Iterator<XID> iterator() {
		return this.state.iterator();
	}
	
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.fieldChangeListenerCollection) {
			return this.fieldChangeListenerCollection.remove(changeListener);
		}
	}
	
	public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
		synchronized(this.modelChangeListenerCollection) {
			return this.modelChangeListenerCollection.remove(changeListener);
		}
	}
	
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		synchronized(this.objectChangeListenerCollection) {
			return this.objectChangeListenerCollection.remove(changeListener);
		}
	}
	
	public boolean removeListenerForRepositoryEvents(XRepositoryEventListener changeListener) {
		synchronized(this.repoChangeListenerCollection) {
			return this.repoChangeListenerCollection.remove(changeListener);
		}
	}
	
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		synchronized(this.transactionListenerCollection) {
			return this.transactionListenerCollection.remove(changeListener);
		}
	}
	
	public boolean removeModel(XID modelId) {
		
		// no synchronization necessary here (except that in
		// executeRepositoryCommand())
		
		XRepositoryCommand command = MemoryRepositoryCommand.createRemoveCommand(getAddress(),
		        XCommand.FORCED, modelId);
		
		long result = executeRepositoryCommand(command);
		assert result >= 0 || result == XCommand.NOCHANGE;
		return result != XCommand.NOCHANGE;
	}
	
	private long removeModelInternal(MemoryModel model, XCommand command,
	        XLocalChangeCallback callback) {
		synchronized(model.eventQueue) {
			
			XID modelId = model.getID();
			
			long rev = model.getRevisionNumber() + 1;
			
			int since = model.eventQueue.getNextPosition();
			boolean inTrans = model.enqueueModelRemoveEvents(this.sessionActor);
			if(inTrans) {
				model.eventQueue.createTransactionEvent(this.sessionActor, model, null, since);
			}
			
			model.delete();
			this.state.removeModel(modelId);
			this.loadedModels.remove(modelId);
			
			model.eventQueue.newLocalChange(command, callback);
			
			model.eventQueue.sendEvents();
			model.eventQueue.setBlockSending(true);
			
			return rev;
		}
	}
	
	@Override
	public void setSessionActor(XID actorId, String passwordHash) {
		assert actorId != null;
		this.sessionActor = actorId;
		this.sessionPasswordHash = passwordHash;
		for(XModel model : this.loadedModels.values()) {
			model.setSessionActor(actorId, passwordHash);
		}
	}
	
	protected synchronized void updateRemoved(MemoryModel model) {
		assert model != null;
		synchronized(model.eventQueue) {
			XID modelId = model.getID();
			boolean hasModel = hasModel(modelId);
			if(model.removed && hasModel) {
				this.state.removeModel(modelId);
				this.loadedModels.remove(modelId);
			} else if(!model.removed && !hasModel) {
				this.state.addModel(model.getState());
				this.loadedModels.put(model.getID(), model);
			}
			model.eventQueue.sendEvents();
		}
	}
	
	/*
	 * TODO Why do repositories have no revision number? -- Because everything
	 * that uses revision numbers (synchronizing, change logs, transactions,
	 * locking in GAE) is per model. Having revision numbers (with a semantic
	 * consistent with existing model, object and field revisions) for
	 * repositories would mean increasing the revision for every repository,
	 * model, object and field change, which would quickly become a bottleneck.
	 */
	@Override
	protected long getRevisionNumber() {
		return 0;
	}
	
	@Override
	protected AbstractEntity getFather() {
		return null;
	}
	
	@Override
	public XType getType() {
		return XType.XREPOSITORY;
	}
	
}
