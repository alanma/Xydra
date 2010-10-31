package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.ModificationOperation;
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
import org.xydra.core.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
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
	
	/**
	 * Creates a new MemoryRepository.
	 * 
	 * @param repositoryId The {@link XID} for this MemoryRepository.
	 */
	public MemoryRepository(XID repositoryId) {
		this(new TemporaryRepositoryState(XX.toAddress(repositoryId, null, null, null)));
	}
	
	/**
	 * Creates a new {@link MemoryRepository}.
	 * 
	 * @param repositoryState The initial {@link XRepositoryState} of this
	 *            MemoryRepository.
	 */
	public MemoryRepository(XRepositoryState repositoryState) {
		assert repositoryState != null;
		
		this.state = repositoryState;
		
		this.repoChangeListenerCollection = new HashSet<XRepositoryEventListener>();
		this.modelChangeListenerCollection = new HashSet<XModelEventListener>();
		this.objectChangeListenerCollection = new HashSet<XObjectEventListener>();
		this.fieldChangeListenerCollection = new HashSet<XFieldEventListener>();
		this.transactionListenerCollection = new HashSet<XTransactionEventListener>();
	}
	
	@ModificationOperation
	public synchronized MemoryModel createModel(XID actor, XID modelID) {
		MemoryModel model = getModel(modelID);
		if(model == null) {
			XRepositoryEvent event = MemoryRepositoryEvent.createAddEvent(actor, getAddress(),
			        modelID);
			
			XModelState modelState = this.state.createModelState(modelID);
			
			model = new MemoryModel(this, modelState);
			
			XStateTransaction trans = beginStateTransaction();
			
			XChangeLogState log = modelState.getChangeLogState();
			if(log != null) {
				log.appendEvent(event, trans);
				log.save(trans);
			}
			
			this.state.addModelState(modelState);
			model.save(trans);
			save(trans);
			
			endStateTransaction(trans);
			
			// in memory
			this.loadedModels.put(model.getID(), model);
			
			fireRepositoryEvent(event);
			
		}
		
		return model;
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
		model = new MemoryModel(this, modelState);
		this.loadedModels.put(modelID, model);
		
		return model;
	}
	
	@ReadOperation
	public synchronized Iterator<XID> iterator() {
		return this.state.iterator();
	}
	
	@ModificationOperation
	public synchronized boolean removeModel(XID actor, XID modelID) {
		
		MemoryModel model = getModel(modelID);
		if(model == null) {
			return false;
		}
		
		synchronized(model.eventQueue) {
			
			long modelRev = model.getRevisionNumber();
			
			enqueueModelRemoveEvents(actor, model);
			
			XStateTransaction trans = beginStateTransaction();
			
			model.delete(trans);
			this.state.removeModelState(modelID);
			this.loadedModels.remove(modelID);
			
			save(trans);
			
			endStateTransaction(trans);
			
			model.eventQueue.sendEvents();
			XRepositoryEvent event = MemoryRepositoryEvent.createRemoveEvent(actor, getAddress(),
			        modelID, modelRev, false);
			fireRepositoryEvent(event);
			
		}
		
		return true;
	}
	
	protected void enqueueModelRemoveEvents(XID actor, MemoryModel model) {
		
		for(XID objectId : model) {
			MemoryObject object = model.getObject(objectId);
			model.enqueueObjectRemoveEvents(actor, object, true);
		}
		
	}
	
	public synchronized long executeRepositoryCommand(XID actor, XRepositoryCommand command) {
		
		if(!command.getRepositoryID().equals(getID())) {
			// given given repository-id are not consistent
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
					return XCommand.NOCHANGE;
				}
				return XCommand.FAILED;
			}
			
			XModel model = createModel(actor, command.getModelID());
			
			return model.getRevisionNumber();
		}
		
		if(command.getChangeType() == ChangeType.REMOVE) {
			XModel oldModel = getModel(command.getModelID());
			if(oldModel == null) {
				// ID not taken
				if(command.isForced()) {
					/*
					 * the forced event only cares about the postcondition -
					 * that there is no model with the given ID, not about that
					 * there was such a model before
					 */
					return XCommand.NOCHANGE;
				}
				return XCommand.FAILED;
			}
			if(!command.isForced() && oldModel.getRevisionNumber() != command.getRevisionNumber()) {
				return XCommand.FAILED;
			}
			
			removeModel(actor, command.getModelID());
			
			return XCommand.CHANGED;
		}
		
		return XCommand.FAILED;
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
	private void fireRepositoryEvent(XRepositoryEvent event) {
		for(XRepositoryEventListener listener : this.repoChangeListenerCollection) {
			listener.onChangeEvent(event);
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
		for(XModelEventListener listener : this.modelChangeListenerCollection) {
			listener.onChangeEvent(event);
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
		for(XObjectEventListener listener : this.objectChangeListenerCollection) {
			listener.onChangeEvent(event);
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
		for(XFieldEventListener listener : this.fieldChangeListenerCollection) {
			listener.onChangeEvent(event);
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
		for(XTransactionEventListener listener : this.transactionListenerCollection) {
			listener.onChangeEvent(event);
		}
	}
	
	/**
	 * Adds the given {@link XRepositoryEventListener} to this MemoryRepository,
	 * if possible.
	 * 
	 * @param changeListener The {@link XRepositoryEventListener} which is to be
	 *            added
	 * @return false, if the given {@link XRepositoryEventListener} was already
	 *         registered on this MemoryRepository, true otherwise
	 */
	public synchronized boolean addListenerForRepositoryEvents(
	        XRepositoryEventListener changeListener) {
		return this.repoChangeListenerCollection.add(changeListener);
	}
	
	/**
	 * Removes the given {@link XRepositoryEventListener} from this
	 * MemoryRepository.
	 * 
	 * @param changeListener The {@link XRepositoryEventListener} which is to be
	 *            removed
	 * @return true, if the given {@link XRepositoryEventListener} was
	 *         registered on this MemoryRepository, false otherwise
	 */
	public synchronized boolean removeListenerForRepositoryEvents(
	        XRepositoryEventListener changeListener) {
		return this.repoChangeListenerCollection.remove(changeListener);
		
	}
	
	/**
	 * Adds the given {@link XModelEventListener} to this MemoryRepository, if
	 * possible.
	 * 
	 * @param changeListener The {@link XModelEventListener} which is to be
	 *            added
	 * @return false, if the given {@link XModelEventListener} was already
	 *         registered on this MemoryRepository, true otherwise
	 */
	public synchronized boolean addListenerForModelEvents(XModelEventListener changeListener) {
		return this.modelChangeListenerCollection.add(changeListener);
		
	}
	
	/**
	 * Removes the given {@link XModelEventListener} from this MemoryRepository.
	 * 
	 * @param changeListener The {@link XModelEventListener} which is to be
	 *            removed
	 * @return true, if the given {@link XModelEventListener} was registered on
	 *         this MemoryRepository, false otherwise
	 */
	public synchronized boolean removeListenerForModelEvents(XModelEventListener changeListener) {
		return this.modelChangeListenerCollection.remove(changeListener);
		
	}
	
	/**
	 * Adds the given {@link XObjectEventListener} to this MemoryRepository, if
	 * possible.
	 * 
	 * @param changeListener The {@link XObjectEventListener} which is to be
	 *            added
	 * @return false, if the given {@link XObjectEventListener} was already
	 *         registered on this MemoryRepository, true otherwise
	 */
	public synchronized boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		return this.objectChangeListenerCollection.add(changeListener);
		
	}
	
	/**
	 * Removes the given {@link XObjectEventListener} from this
	 * MemoryRepository.
	 * 
	 * @param changeListener The {@link XObjectEventListener} which is to be
	 *            removed
	 * @return true, if the given {@link XObjectEventListener} was registered on
	 *         this MemoryRepository, false otherwise
	 */
	public synchronized boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		return this.objectChangeListenerCollection.remove(changeListener);
		
	}
	
	/**
	 * Adds the given {@link XFieldEventListener} to this MemoryRepository, if
	 * possible.
	 * 
	 * @param changeListener The {@link XFieldEventListener} which is to be
	 *            added
	 * @return false, if the given {@link XFieldEventListener} was already
	 *         registered on this MemoryRepository, true otherwise
	 */
	public synchronized boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.fieldChangeListenerCollection.add(changeListener);
		
	}
	
	/**
	 * Removes the given {@link XFieldEventListener} from this MemoryRepository.
	 * 
	 * @param changeListener The {@link XFieldEventListener} which is to be
	 *            removed
	 * @return true, if the given {@link XFieldEventListener} was registered on
	 *         this MemoryRepository, false otherwise
	 */
	public synchronized boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.fieldChangeListenerCollection.remove(changeListener);
		
	}
	
	public synchronized XAddress getAddress() {
		return this.state.getAddress();
	}
	
	/**
	 * Adds the given {@link XTransactionEventListener} to this
	 * MemoryRepository, if possible.
	 * 
	 * @param changeListener The {@link XTransactionEventListener} which is to
	 *            be added
	 * @return false, if the given {@link XTransactionEventListener} was already
	 *         registered on this MemoryRepository, true otherwise
	 */
	public synchronized boolean addListenerForTransactionEvents(
	        XTransactionEventListener changeListener) {
		return this.transactionListenerCollection.add(changeListener);
	}
	
	/**
	 * Removes the given {@link XTransactionEventListener} from this
	 * MemoryRepository.
	 * 
	 * @param changeListener The {@link XTransactionEventListener} which is to
	 *            be removed
	 * @return true, if the given {@link XTransactionEventListener} was
	 *         registered on this MemoryRepository, false otherwise
	 */
	public synchronized boolean removeListenerForTransactionEvents(
	        XTransactionEventListener changeListener) {
		return this.transactionListenerCollection.remove(changeListener);
	}
	
	public synchronized long executeCommand(XID actor, XCommand command) {
		if(command instanceof XRepositoryCommand) {
			return executeRepositoryCommand(actor, (XRepositoryCommand)command);
		}
		XModel model = getModel(command.getTarget().getModel());
		if(model == null) {
			return XCommand.FAILED;
		}
		return model.executeCommand(actor, command);
	}
	
}
