package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.impl.memory.MemoryFieldCommand;
import org.xydra.core.change.impl.memory.MemoryFieldEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.impl.memory.TemporaryFieldState;
import org.xydra.core.value.XValue;
import org.xydra.index.XI;


/**
 * An implementation of {@link XField}.
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */

public class MemoryField implements XField, Serializable {
	
	private static final long serialVersionUID = -4390811955475742528L;
	
	final private XFieldState state;
	
	/**
	 * the father-object which is holding this field (may be null)
	 */
	private final MemoryObject father;
	
	/** Has this field been removed? */
	boolean removed = false;
	
	/**
	 * The {@link MemoryEventManager} used by this MemoryField. Will also be
	 * used as the lock for synchronizing change operations.
	 * 
	 * If this MemoryField is created by an {@link MemoryObject}, the event
	 * queue used by the {@link MemoryObject} will be used.
	 */
	private final MemoryEventManager eventQueue;
	
	private Set<XFieldEventListener> fieldChangeListenerCollection;
	
	/**
	 * Creates a new MemoryField without a father-{@link XObject}.
	 * 
	 * TODO What can I do with it? Can I ever add it to an XObject?
	 * 
	 * @param actorId TODO
	 * @param fieldId The {@link XID} for this MemoryField.
	 */
	public MemoryField(XID actorId, XID fieldId) {
		this(actorId, new TemporaryFieldState(XX.toAddress(null, null, null, fieldId)));
	}
	
	/**
	 * Creates a new MemoryField without a father-{@link XObject}.
	 * 
	 * TODO What can I do with it? Can I ever add it to an XObject?
	 * 
	 * @param actorId TODO
	 * @param fieldState The initial {@link XFieldState} of this MemoryField.
	 */
	public MemoryField(XID actorId, XFieldState fieldState) {
		this(null, new MemoryEventManager(actorId, null, null, -1), fieldState);
	}
	
	/**
	 * Creates a new MemoryField with or without a father-{@link XObject}.
	 * 
	 * @param actorId TODO
	 * @param parent The father-{@link XObject} of this MemoryField (may be
	 *            null)
	 * @param eventQueue the {@link MemoryEventManager} this MemoryField will
	 *            use; never null.
	 * @param fieldState The initial {@link XFieldState} of this MemoryField.
	 */
	protected MemoryField(MemoryObject parent, MemoryEventManager eventQueue, XFieldState fieldState) {
		assert eventQueue != null;
		
		this.eventQueue = eventQueue;
		
		if(fieldState == null) {
			throw new IllegalArgumentException("objectState may not be null");
		}
		this.state = fieldState;
		
		if(parent == null && fieldState.getAddress().getObject() != null) {
			throw new IllegalArgumentException("must load field through containing object");
		}
		this.father = parent;
		
		this.fieldChangeListenerCollection = new HashSet<XFieldEventListener>();
	}
	
	@Override
	@ReadOperation
	public boolean equals(Object object) {
		if(!(object instanceof MemoryField)) {
			return false;
		}
		
		MemoryField memoryField = (MemoryField)object;
		
		// compare revision number, father-object id (if it exists),
		// father-model id (if it exists), father-repo id (if it exists)
		
		// Check value here to?! Max: not required, we can rely on
		// revision Number
		
		boolean result = (this.getRevisionNumber() == memoryField.getRevisionNumber())
		        && (this.getID().equals(memoryField.getID()));
		
		if(this.father != null) {
			if(memoryField.father == null) {
				return false;
			}
			
			result = result && (this.father.getID().equals(memoryField.father.getID()));
			
			MemoryModel fatherModel = this.father.getModel();
			
			if(fatherModel != null) {
				MemoryModel memoryFieldModel = memoryField.father.getModel();
				
				if(memoryFieldModel == null) {
					return false;
				}
				
				result = result && (fatherModel.getID().equals(memoryFieldModel.getID()));
				
				XRepository fatherRepo = fatherModel.getFather();
				
				if(fatherRepo != null) {
					XRepository memoryFieldRepo = memoryFieldModel.getFather();
					
					if(memoryFieldRepo == null) {
						return false;
					}
					
					result = result && (fatherRepo.getID().equals(memoryFieldRepo.getID()));
				}
			}
		}
		
		return result;
	}
	
	@Override
	public int hashCode() {
		int hashCode = this.getID().hashCode() + (int)this.getRevisionNumber();
		
		if(this.father != null) {
			hashCode += this.father.getID().hashCode();
			
			XModel fatherModel = this.father.getModel();
			
			if(fatherModel != null) {
				hashCode += fatherModel.getID().hashCode();
				
				if(fatherModel instanceof MemoryModel) {
					XRepository repoFather = ((MemoryModel)fatherModel).getFather();
					
					if(repoFather != null) {
						hashCode += repoFather.getID().hashCode();
					}
				}
			}
		}
		
		return hashCode;
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryField was already removed
	 */
	private void checkRemoved() throws IllegalStateException {
		if(this.removed) {
			throw new IllegalStateException("this field has been removed");
		}
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryField was already removed
	 */
	@ReadOperation
	public XID getID() {
		synchronized(this.eventQueue) {
			return this.state.getID();
		}
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryField was already removed
	 */
	@ModificationOperation
	public boolean setValue(XValue newValue) {
		
		// no synchronization necessary here (except that in
		// executeFieldCommand())
		
		XFieldCommand command;
		if(newValue == null) {
			command = MemoryFieldCommand.createRemoveCommand(getAddress(), XCommand.FORCED);
		} else {
			command = MemoryFieldCommand.createAddCommand(getAddress(), XCommand.FORCED, newValue);
		}
		
		long result = executeFieldCommand(command);
		assert result >= 0 || result == XCommand.NOCHANGE;
		return result != XCommand.NOCHANGE;
	}
	
	/**
	 * Set the new value, increase revision (if not in a transaction) and
	 * enqueue the corresponding event.
	 * 
	 * The caller is responsible for handling synchronization, for checking that
	 * this field has not been removed, for checking that the newValue is
	 * actually different from the current value.
	 */
	protected void setValueInternal(XValue newValue) {
		
		XValue oldValue = getValue();
		
		assert !XI.equals(oldValue, newValue);
		
		boolean inTrans = this.eventQueue.transactionInProgess;
		
		boolean hasOrphans = (this.eventQueue.orphans != null);
		if(!inTrans && !hasOrphans) {
			beginStateTransaction();
		}
		
		this.state.setValue(newValue);
		
		// check for field event type
		long modelRev = getModelRevisionNumber();
		long objectRev = getObjectRevisionNumber();
		long fieldRev = getRevisionNumber();
		XID actorId = this.eventQueue.getActor();
		XFieldEvent event = null;
		if((oldValue == null)) {
			assert newValue != null;
			event = MemoryFieldEvent.createAddEvent(actorId, getAddress(), newValue, modelRev,
			        objectRev, fieldRev, inTrans);
		} else {
			if(newValue == null) {
				// implies remove
				event = MemoryFieldEvent.createRemoveEvent(actorId, getAddress(), oldValue,
				        modelRev, objectRev, fieldRev, inTrans, false);
			} else {
				assert !newValue.equals(oldValue);
				// implies change
				event = MemoryFieldEvent.createChangeEvent(actorId, getAddress(), oldValue,
				        newValue, modelRev, objectRev, fieldRev, inTrans);
			}
		}
		
		assert event != null;
		
		this.eventQueue.enqueueFieldEvent(this, event);
		
		// event propagation and revision number increasing happens after
		// all events were successful
		if(!inTrans) {
			
			// increment revision number
			incrementRevisionAndSave();
			
			if(!hasOrphans) {
				endStateTransaction();
			}
			
			// dispatch events
			this.eventQueue.sendEvents();
			
		}
		
	}
	
	public long executeFieldCommand(XFieldCommand command) {
		return executeFieldCommand(command, null);
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryField was already removed
	 */
	protected long executeFieldCommand(XFieldCommand command, XLocalChangeCallback callback) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			assert !this.eventQueue.transactionInProgess;
			
			// check whether the given event actually refers to this field
			if(!getAddress().equals(command.getTarget())) {
				return XCommand.FAILED;
			}
			
			if(!command.isForced() && this.getRevisionNumber() != command.getRevisionNumber()) {
				return XCommand.FAILED;
			}
			
			long oldRev = getOldRevisionNumber();
			
			if(command.getChangeType() == ChangeType.ADD) {
				if(getValue() != null) {
					/*
					 * the forced event only cares about the postcondition -
					 * that there is the given value set, not about that there
					 * was no value before
					 */
					if(!command.isForced()) {
						// value already set
						return XCommand.FAILED;
					}
				}
				
			} else if(command.getChangeType() == ChangeType.REMOVE) {
				if(getValue() == null) {
					/*
					 * the forced event only cares about the postcondition -
					 * that there is no value set, not about that there was a
					 * value before
					 */
					if(!command.isForced()) {
						// value is not set and can not be removed or the given
						// value is not current anymore
						return XCommand.FAILED;
					}
				}
				
				assert command.getValue() == null;
				
			} else if(command.getChangeType() == ChangeType.CHANGE) {
				if(getValue() == null) {
					/*
					 * the forced event only cares about the postcondition -
					 * that there is the given value set, not about that there
					 * was no value before
					 */
					if(!command.isForced()) {
						// given old value does not concur with the current
						// value
						return XCommand.FAILED;
					}
				}
				
			} else {
				throw new IllegalArgumentException("Unknown field command type: " + command);
			}
			
			if(XI.equals(getValue(), command.getValue())) {
				return XCommand.NOCHANGE;
			}
			
			this.eventQueue.newLocalChange(command, callback);
			
			setValueInternal(command.getValue());
			
			return oldRev + 1;
		}
	}
	
	private long getOldRevisionNumber() {
		if(this.father != null)
			return this.father.getCurrentRevisionNumber();
		return getRevisionNumber();
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryField was already removed
	 */
	@ReadOperation
	public XValue getValue() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.getValue();
		}
	}
	
	protected void incrementRevisionAndSave() {
		assert !this.eventQueue.transactionInProgess;
		if(this.father != null) {
			// this increments the revisionNumber of the father and sets
			// this revNr to the
			// revNr of the father
			this.father.incrementRevisionAndSave();
			setRevisionNumber(this.father.getRevisionNumber());
		} else {
			setRevisionNumber(getRevisionNumber() + 1);
		}
		save();
	}
	
	protected void setRevisionNumber(long newRevision) {
		this.state.setRevisionNumber(newRevision);
	}
	
	/**
	 * Returns the father-{@link XObject} (the {@link XObject} containing this
	 * MemoryField) of this MemoryField.
	 * 
	 * @return The father-{@link XObject} of this MemoryField - may be null if
	 *         this MemoryField has no father-{@link XObject}
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryField was already removed
	 */
	@ReadOperation
	protected MemoryObject getObject() {
		return this.father;
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryField was already removed
	 */
	@ReadOperation
	public long getRevisionNumber() {
		synchronized(this.eventQueue) {
			return this.state.getRevisionNumber();
		}
	}
	
	protected long getObjectRevisionNumber() {
		if(this.father != null)
			return this.father.getRevisionNumber();
		else
			return XEvent.RevisionOfEntityNotSet;
	}
	
	protected long getModelRevisionNumber() {
		if(this.father != null)
			return this.father.getModelRevisionNumber();
		else
			return XEvent.RevisionOfEntityNotSet;
	}
	
	public boolean isEmpty() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.getValue() == null;
		}
	}
	
	public XAddress getAddress() {
		synchronized(this.eventQueue) {
			return this.state.getAddress();
		}
	}
	
	/**
	 * Notifies all listeners that have registered interest for notification on
	 * {@link XFieldEvent XFieldEvents} happening on this MemoryField.
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
	 * Adds the given {@link XFieldEventListener} to this MemoryField, if
	 * possible.
	 * 
	 * @param changeListener The {@link XFieldEventListener} which is to be
	 *            added
	 * @return false, if the given {@link XFieldEventListener} was already
	 *         registered on this MemoryField, true otherwise
	 */
	
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.fieldChangeListenerCollection.add(changeListener);
		}
	}
	
	/**
	 * Removes the given {@link XFieldEventListener} from this MemoryField.
	 * 
	 * @param changeListener The {@link XFieldEventListener} which is to be
	 *            removed
	 * @return true, if the given {@link XFieldEventListener} was registered on
	 *         this MemoryField, false otherwise
	 */
	
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.fieldChangeListenerCollection.remove(changeListener);
		}
	}
	
	@Override
	public String toString() {
		return this.getID() + "-v" + this.getRevisionNumber();
	}
	
	/**
	 * Deletes the state information of this MemoryField from the currently used
	 * persistence layer
	 * 
	 * @param transaction
	 */
	protected void delete() {
		this.state.setValue(null);
		this.state.delete(this.eventQueue.stateTransaction);
		this.removed = true;
	}
	
	/**
	 * Saves the state information of this MemoryField with the currently used
	 * persistence layer
	 */
	protected void save() {
		this.state.save(this.eventQueue.stateTransaction);
	}
	
	/**
	 * @return the {@link XFieldState} object representing the current state of
	 *         this MemoryField
	 */
	protected XFieldState getState() {
		return this.state;
	}
	
	/**
	 * Start a new state transaction.
	 * 
	 * @return true if a transaction was started and should be ended later.
	 */
	private void beginStateTransaction() {
		if(this.father != null) {
			this.father.beginStateTransaction();
		} else {
			assert this.eventQueue.stateTransaction == null : "multiple state transactions detected";
			// no transaction needed
		}
	}
	
	/**
	 * End the current state transaction.
	 */
	private void endStateTransaction() {
		if(this.father != null) {
			this.father.endStateTransaction();
		} else {
			assert this.eventQueue.stateTransaction == null : "unexpected state transaction";
		}
	}
	
	@Override
	public XID getSessionActor() {
		return this.eventQueue.getActor();
	}
	
	@Override
	public void setSessionActor(XID actorId) {
		if(this.father != null) {
			throw new IllegalStateException("cannot set actor on field with a parent");
		}
		this.eventQueue.setSessionActor(actorId, null);
	}
	
}
