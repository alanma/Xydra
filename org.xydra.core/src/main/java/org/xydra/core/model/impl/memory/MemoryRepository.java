package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.core.X;
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
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XRepositoryState;
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
	 * Create a new {@link MemoryRepository} object.
	 * 
	 * @param repositoryId never null.
	 */
	public MemoryRepository(XID repositoryId) {
		this(new TemporaryRepositoryState(X.getIDProvider().fromComponents(repositoryId, null,
		        null, null)));
	}
	
	/**
	 * Create a new {@link MemoryRepository} object wrapping an existing
	 * {@link XRepositoryState} (Memento Pattern).
	 * 
	 * @param repositoryState never null.
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
	
	/**
	 * Creates a new XModel with the given XID and adds it to this repository.
	 * Returns the already existing XModel, if the given XID was already taken.
	 * 
	 * @param actor The XID of the actor calling this operation.
	 * @param modelID The XID of the new model
	 * @return A new XModel or the already existing one, if the given XID was
	 *         already taken.
	 */
	@ModificationOperation
	public synchronized MemoryModel createModel(XID actor, XID modelID) {
		MemoryModel model = getModel(modelID);
		if(model == null) {
			XModelState modelState = this.state.createModelState(modelID);
			model = new MemoryModel(this, modelState);
			
			this.state.addModelState(modelState);
			model.save();
			save();
			
			// in memory
			this.loadedModels.put(model.getID(), model);
			
			XRepositoryEvent event = MemoryRepositoryEvent.createAddEvent(actor, getAddress(),
			        modelID);
			fireRepositoryEvent(event);
			
		}
		
		return model;
	}
	
	private void save() {
		this.state.save();
	}
	
	/**
	 * Returns the model corresponding to the given XID in this repository.
	 * 
	 * @return The model corresponding to the given XID in this repository (null
	 *         if the given XID isn't taken)
	 */
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
	
	/**
	 * Returns an iterator over the XIDs of the models in this repository.
	 * 
	 * @return An iterator over the XIDs of the models in this repository.
	 */
	@ReadOperation
	public synchronized Iterator<XID> iterator() {
		return this.state.iterator();
	}
	
	/**
	 * Removes the given model from this repository.
	 * 
	 * @param actor The XID of the actor calling this operation.
	 * @param model The model which is to be removed.
	 * @return true, if the removal was successful, false otherwise (i.e. if the
	 *         given model doesn't exist in this repository)
	 */
	@ModificationOperation
	public synchronized boolean removeModel(XID actor, XID modelID) {
		
		MemoryModel model = getModel(modelID);
		if(model == null) {
			return false;
		}
		
		long modelRev = model.getRevisionNumber();
		model.delete();
		this.state.removeModelState(modelID);
		this.loadedModels.remove(modelID);
		
		// set father attribute of the removed model to null
		
		save();
		
		XRepositoryEvent event = MemoryRepositoryEvent.createRemoveEvent(actor, getAddress(),
		        modelID, modelRev);
		fireRepositoryEvent(event);
		// FIXME must also fire events for removed values, fields and objects
		
		return true;
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
			
			createModel(actor, command.getModelID());
			
			return XCommand.CHANGED;
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
	
	/**
	 * Returns the XID of this repository.
	 * 
	 * @return The XID of this repository.
	 */
	
	public synchronized XID getID() {
		return this.state.getID();
	}
	
	/**
	 * Checks whether the given XID is already taken by a model in this
	 * repository or not.
	 * 
	 * @return true, if the given XID is already taken by a model in this
	 *         repository.
	 */
	
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
	 * RepositoryEvents.
	 * 
	 * @param event The event object.
	 */
	private void fireRepositoryEvent(XRepositoryEvent event) {
		for(XRepositoryEventListener listener : this.repoChangeListenerCollection) {
			listener.onChangeEvent(event);
		}
	}
	
	/**
	 * Notifies all listeners that have registered interest for notification on
	 * ModelEvents.
	 * 
	 * @param event The event object.
	 */
	protected void fireModelEvent(XModelEvent event) {
		for(XModelEventListener listener : this.modelChangeListenerCollection) {
			listener.onChangeEvent(event);
		}
	}
	
	/**
	 * Notifies all listeners that have registered interest for notification on
	 * ObjectEvents.
	 * 
	 * @param event The event object.
	 */
	protected void fireObjectEvent(XObjectEvent event) {
		for(XObjectEventListener listener : this.objectChangeListenerCollection) {
			listener.onChangeEvent(event);
		}
	}
	
	/**
	 * Notifies all listeners that have registered interest for notification on
	 * FieldEvents.
	 * 
	 * @param event The event object.
	 */
	protected void fireFieldEvent(XFieldEvent event) {
		for(XFieldEventListener listener : this.fieldChangeListenerCollection) {
			listener.onChangeEvent(event);
		}
	}
	
	/**
	 * Notifies all listeners that have registered interest for notification on
	 * TransactionEvents.
	 * 
	 * @param event The event object.
	 */
	protected void fireTransactionEvent(XTransactionEvent event) {
		for(XTransactionEventListener listener : this.transactionListenerCollection) {
			listener.onChangeEvent(event);
		}
	}
	
	/**
	 * Adds the given listener to this model, if possible
	 * 
	 * @param changeListener The listener which is to be added
	 * @return false, if the given listener is already added on this field, true
	 *         otherwise
	 */
	public synchronized boolean addListenerForRepositoryEvents(
	        XRepositoryEventListener changeListener) {
		return this.repoChangeListenerCollection.add(changeListener);
	}
	
	/**
	 * Removes the given listener from this model.
	 * 
	 * @param changeListener The listener which is to be removed
	 * @return true, if the given listener was registered on this field, false
	 *         otherwise
	 */
	public synchronized boolean removeListenerForRepositoryEvents(
	        XRepositoryEventListener changeListener) {
		return this.repoChangeListenerCollection.remove(changeListener);
		
	}
	
	/**
	 * Adds the given listener to this model, if possible
	 * 
	 * @param changeListener The listener which is to be added
	 * @return false, if the given listener is already added on this field, true
	 *         otherwise
	 */
	public synchronized boolean addListenerForModelEvents(XModelEventListener changeListener) {
		return this.modelChangeListenerCollection.add(changeListener);
		
	}
	
	/**
	 * Removes the given listener from this model.
	 * 
	 * @param changeListener The listener which is to be removed
	 * @return true, if the given listener was registered on this field, false
	 *         otherwise
	 */
	public synchronized boolean removeListenerForModelEvents(XModelEventListener changeListener) {
		return this.modelChangeListenerCollection.remove(changeListener);
		
	}
	
	/**
	 * Adds the given listener to this model, if possible
	 * 
	 * @param changeListener The listener which is to be added
	 * @return false, if the given listener is already added on this field, true
	 *         otherwise
	 */
	public synchronized boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		return this.objectChangeListenerCollection.add(changeListener);
		
	}
	
	/**
	 * Removes the given listener from this model.
	 * 
	 * @param changeListener The listener which is to be removed
	 * @return true, if the given listener was registered on this field, false
	 *         otherwise
	 */
	public synchronized boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		return this.objectChangeListenerCollection.remove(changeListener);
		
	}
	
	/**
	 * Adds the given listener to this model, if possible
	 * 
	 * @param changeListener The listener which is to be added
	 * @return false, if the given listener is already added on this field, true
	 *         otherwise
	 */
	public synchronized boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.fieldChangeListenerCollection.add(changeListener);
		
	}
	
	/**
	 * Removes the given listener from this model.
	 * 
	 * @param changeListener The listener which is to be removed
	 * @return true, if the given listener was registered on this field, false
	 *         otherwise
	 */
	public synchronized boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.fieldChangeListenerCollection.remove(changeListener);
		
	}
	
	public synchronized XAddress getAddress() {
		return this.state.getAddress();
	}
	
	public synchronized boolean addListenerForTransactionEvents(
	        XTransactionEventListener changeListener) {
		return this.transactionListenerCollection.add(changeListener);
	}
	
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
