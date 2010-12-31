package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.ReadOperation;
import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XRepositoryState;
import org.xydra.core.model.state.XStateTransaction;
import org.xydra.core.model.state.impl.memory.TemporaryRepositoryState;


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
public class MemoryRepository implements XRepository, Serializable {
	
	private static final long serialVersionUID = 2412386047787717740L;
	
	private final XRepositoryState state;
	private final Map<XID,MemoryModel> loadedModels = new HashMap<XID,MemoryModel>();
	
	private Set<XRepositoryEventListener> repoChangeListenerCollection;
	private Set<XModelEventListener> modelChangeListenerCollection;
	private Set<XObjectEventListener> objectChangeListenerCollection;
	private Set<XFieldEventListener> fieldChangeListenerCollection;
	private Set<XTransactionEventListener> transactionListenerCollection;
	
	private XID sessionActor;
	private String sessionPasswordHash;
	
	/**
	 * Creates a new MemoryRepository.
	 * 
	 * @param actorId TODO
	 * @param repositoryId The {@link XID} for this MemoryRepository.
	 */
	public MemoryRepository(XID actorId, String passwordHash, XID repositoryId) {
		this(actorId, passwordHash, new TemporaryRepositoryState(XX.toAddress(repositoryId, null,
		        null, null)));
	}
	
	/**
	 * Creates a new {@link MemoryRepository}.
	 * 
	 * @param actorId TODO
	 * @param repositoryState The initial {@link XRepositoryState} of this
	 *            MemoryRepository.
	 */
	public MemoryRepository(XID actorId, String passwordHash, XRepositoryState repositoryState) {
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
	
	public boolean removeModel(XID modelId) {
		
		// no synchronization necessary here (except that in
		// executeRepositoryCommand())
		
		XRepositoryCommand command = MemoryRepositoryCommand.createRemoveCommand(getAddress(),
		        XCommand.FORCED, modelId);
		
		long result = executeRepositoryCommand(command);
		assert result >= 0 || result == XCommand.NOCHANGE;
		return result != XCommand.NOCHANGE;
	}
	
	/**
	 * Saves the current state information of this MemoryRepository with the
	 * currently used persistence layer
	 */
	private void save(XStateTransaction transaction) {
		this.state.save(transaction);
	}
	
	private XStateTransaction beginStateTransaction() {
		return this.state.beginTransaction();
	}
	
	private void endStateTransaction(XStateTransaction transaction) {
		this.state.endTransaction(transaction);
	}
	
	@ReadOperation
	public synchronized MemoryModel getModel(XID modelID) {
		
		MemoryModel model = this.loadedModels.get(modelID);
		if(model != null) {
			return model;
		}
		
		if(!this.state.hasModelState(modelID)) {
			return null;
		}
		
		XModelState modelState = this.state.getModelState(modelID);
		model = new MemoryModel(this.sessionActor, this.sessionPasswordHash, this, modelState);
		this.loadedModels.put(modelID, model);
		
		return model;
	}
	
	@ReadOperation
	public synchronized Iterator<XID> iterator() {
		return this.state.iterator();
	}
	
	public void createModelInternal(XID modelId, XCommand command, XLocalChangeCallback callback) {
		
		XRepositoryEvent event = MemoryRepositoryEvent.createAddEvent(this.sessionActor,
		        getAddress(), modelId);
		
		XModelState modelState = this.state.createModelState(modelId);
		
		XChangeLogState ls = modelState.getChangeLogState();
		
		XStateTransaction trans = beginStateTransaction();
		
		ls.appendEvent(event, trans);
		ls.save(trans);
		
		modelState.save(trans);
		
		MemoryModel model = new MemoryModel(this.sessionActor, this.sessionPasswordHash, this,
		        modelState);
		assert model.getRevisionNumber() == 0;
		
		this.state.addModelState(modelState);
		save(trans);
		
		endStateTransaction(trans);
		
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
	
	private long removeModelInternal(MemoryModel model, XCommand command,
	        XLocalChangeCallback callback) {
		synchronized(model.eventQueue) {
			
			XID modelId = model.getID();
			
			long rev = model.getRevisionNumber() + 1;
			
			XStateTransaction trans = beginStateTransaction();
			assert model.eventQueue.stateTransaction == null;
			model.eventQueue.stateTransaction = trans;
			
			int since = model.eventQueue.getNextPosition();
			boolean inTrans = model.enqueueModelRemoveEvents(this.sessionActor);
			if(inTrans) {
				model.eventQueue.createTransactionEvent(this.sessionActor, model, null, since);
			}
			
			model.delete();
			this.state.removeModelState(modelId);
			this.loadedModels.remove(modelId);
			
			save(trans);
			
			endStateTransaction(trans);
			model.eventQueue.stateTransaction = null;
			
			model.eventQueue.newLocalChange(command, callback);
			
			model.eventQueue.sendEvents();
			model.eventQueue.setBlockSending(true);
			
			return rev;
		}
	}
	
	public long executeRepositoryCommand(XRepositoryCommand command) {
		return executeRepositoryCommand(command, null);
	}
	
	private synchronized long executeRepositoryCommand(XRepositoryCommand command,
	        XLocalChangeCallback callback) {
		
		if(!command.getRepositoryID().equals(getID())) {
			// given given repository-id are not consistent
			if(callback != null) {
				callback.onFailure();
			}
			return XCommand.FAILED;
		}
		
		if(command.getChangeType() == ChangeType.ADD) {
			if(hasModel(command.getModelID())) {
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
			
			createModelInternal(command.getModelID(), command, callback);
			
			// Models are always created at the revision number 0.
			return 0;
			
		} else if(command.getChangeType() == ChangeType.REMOVE) {
			MemoryModel oldModel = getModel(command.getModelID());
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
	
	public synchronized XID getID() {
		return this.state.getID();
	}
	
	public synchronized boolean hasModel(XID id) {
		return this.loadedModels.containsKey(id) || this.state.hasModelState(id);
	}
	
	public synchronized boolean isEmpty() {
		return this.state.isEmpty();
	}
	
	@Override
	public boolean equals(Object object) {
		if(!(object instanceof XRepository)) {
			return false;
		}
		
		return getID().equals(((XRepository)object).getID());
	}
	
	@Override
	public int hashCode() {
		return getID().hashCode();
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
	
	public boolean addListenerForRepositoryEvents(XRepositoryEventListener changeListener) {
		synchronized(this.repoChangeListenerCollection) {
			return this.repoChangeListenerCollection.add(changeListener);
		}
	}
	
	public boolean removeListenerForRepositoryEvents(XRepositoryEventListener changeListener) {
		synchronized(this.repoChangeListenerCollection) {
			return this.repoChangeListenerCollection.remove(changeListener);
		}
	}
	
	public boolean addListenerForModelEvents(XModelEventListener changeListener) {
		synchronized(this.modelChangeListenerCollection) {
			return this.modelChangeListenerCollection.add(changeListener);
		}
	}
	
	public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
		synchronized(this.modelChangeListenerCollection) {
			return this.modelChangeListenerCollection.remove(changeListener);
		}
	}
	
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		synchronized(this.objectChangeListenerCollection) {
			return this.objectChangeListenerCollection.add(changeListener);
		}
	}
	
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		synchronized(this.objectChangeListenerCollection) {
			return this.objectChangeListenerCollection.remove(changeListener);
		}
	}
	
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.fieldChangeListenerCollection) {
			return this.fieldChangeListenerCollection.add(changeListener);
		}
	}
	
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.fieldChangeListenerCollection) {
			return this.fieldChangeListenerCollection.remove(changeListener);
		}
	}
	
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		synchronized(this.transactionListenerCollection) {
			return this.transactionListenerCollection.add(changeListener);
		}
	}
	
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		synchronized(this.transactionListenerCollection) {
			return this.transactionListenerCollection.remove(changeListener);
		}
	}
	
	public XAddress getAddress() {
		return this.state.getAddress();
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
	
	@Override
	public XID getSessionActor() {
		return this.sessionActor;
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
				XStateTransaction trans = beginStateTransaction();
				this.state.removeModelState(modelId);
				this.loadedModels.remove(modelId);
				save(trans);
				endStateTransaction(trans);
			} else if(!model.removed && !hasModel) {
				XStateTransaction trans = beginStateTransaction();
				this.state.addModelState(model.getState());
				save(trans);
				endStateTransaction(trans);
				this.loadedModels.put(model.getID(), model);
			}
			model.eventQueue.sendEvents();
		}
	}
	
}
