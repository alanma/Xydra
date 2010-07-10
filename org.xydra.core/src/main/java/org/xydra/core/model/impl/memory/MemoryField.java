package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.core.X;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.impl.memory.MemoryFieldEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
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
	 * The {@link MemoryEventQueue} used by this MemoryField. Will also be used
	 * as the lock for synchronizing change operations.
	 * 
	 * If this MemoryField is created by an {@link MemoryObject}, the event
	 * queue used by the {@link MemoryObject} will be used.
	 */
	private final MemoryEventQueue eventQueue;
	
	private Set<XFieldEventListener> fieldChangeListenerCollection;
	
	/**
	 * Creates a new MemoryField without a father-{@link XObject}.
	 * 
	 * @param fieldId The {@link XID} for this MemoryField.
	 */
	public MemoryField(XID fieldId) {
		this(new TemporaryFieldState(X.getIDProvider().fromComponents(null, null, null, fieldId)));
	}
	
	/**
	 * Creates a new MemoryField without a father-{@link XObject}.
	 * 
	 * @param fieldState The initial {@link XFieldState} of this MemoryField.
	 */
	public MemoryField(XFieldState fieldState) {
		this(null, new MemoryEventQueue(null), fieldState);
	}
	
	/**
	 * Creates a new MemoryField with or without a father-{@link XObject}.
	 * 
	 * @param parent The father-{@link XObject} of this MemoryField (may be
	 *            null)
	 * @param eventQueue the {@link MemoryEventQueue} this MemoryField will use
	 * @param fieldState The initial {@link XFieldState} of this MemoryField.
	 */
	protected MemoryField(MemoryObject parent, MemoryEventQueue eventQueue, XFieldState fieldState) {
		this.eventQueue = eventQueue;
		
		assert eventQueue != null;
		
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
		
		if(hasFather()) {
			if(!memoryField.hasFather()) {
				return false;
			}
			
			result = result && (this.father.getID().equals(memoryField.father.getID()));
			
			MemoryModel fatherModel = this.father.getFather();
			
			if(fatherModel != null) {
				MemoryModel memoryFieldModel = memoryField.father.getFather();
				
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
			
			XModel fatherModel = this.father.getFather();
			
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
			checkRemoved();
			return this.state.getID();
		}
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryField was already removed
	 */
	@ModificationOperation
	public boolean setValue(XID actor, XValue newValue) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			XFieldEvent event = null;
			
			XValue oldValue = getValue();
			
			if(XI.equals(oldValue, newValue)) {
				// implies no change
				return false;
			}
			
			this.state.setValue(newValue);
			
			// check for field event type
			if((oldValue == null)) {
				assert newValue != null;
				event = MemoryFieldEvent.createAddEvent(actor, getAddress(), newValue,
				        getModelRevisionNumber(), getObjectRevisionNumber(), getRevisionNumber(),
				        transactionInProgress());
			} else {
				if(newValue == null) {
					// implies remove
					event = MemoryFieldEvent.createRemoveEvent(actor, getAddress(), oldValue,
					        getModelRevisionNumber(), getObjectRevisionNumber(),
					        getRevisionNumber(), transactionInProgress());
				} else {
					assert !newValue.equals(oldValue);
					// implies change
					event = MemoryFieldEvent.createChangeEvent(actor, getAddress(), oldValue,
					        newValue, getModelRevisionNumber(), getObjectRevisionNumber(),
					        getRevisionNumber(), transactionInProgress());
				}
			}
			
			assert event != null;
			
			this.eventQueue.enqueueFieldEvent(this, event);
			
			// event propagation and revision number increasing happens
			// after all events were successful for transactions
			if(!transactionInProgress()) {
				
				// increment revision number
				// only increment if this event is no subevent of a transaction
				// (needs to be handled differently)
				incrementRevisionAndSave();
				
				// dispatch events
				this.eventQueue.sendEvents();
			}
			
			return true;
		}
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryField was already removed
	 */
	public long executeFieldCommand(XID actor, XFieldCommand command) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			// check whether the given event actually refers to this field
			if(!getAddress().equals(command.getTarget())) {
				return XCommand.FAILED;
			}
			
			if(!command.isForced() && this.getRevisionNumber() != command.getRevisionNumber()) {
				return XCommand.FAILED;
			}
			
			long oldRev = getOldRevisionNumber();
			
			if(command.getChangeType() == ChangeType.ADD) {
				if(this.getValue() != null) {
					if(command.isForced()) {
						/*
						 * the forced event only cares about the postcondition -
						 * that there is the given value set, not about that
						 * there was no value before
						 */
					} else {
						// value already set
						return XCommand.FAILED;
					}
				}
				
				if(setValue(actor, command.getValue())) {
					return oldRev;
				} else {
					return XCommand.NOCHANGE;
				}
				
			}
			
			if(command.getChangeType() == ChangeType.REMOVE) {
				if(this.getValue() == null) {
					if(command.isForced()) {
						/*
						 * the forced event only cares about the postcondition -
						 * that there is no value set, not about that there was
						 * a value before
						 */
						return XCommand.NOCHANGE;
					} else {
						// value is not set and can not be removed or the given
						// value is not current anymore
						return XCommand.FAILED;
					}
				}
				
				setValue(actor, null);
				
				return oldRev;
			}
			
			if(command.getChangeType() == ChangeType.CHANGE) {
				if(this.getValue() == null) {
					if(command.isForced()) {
						/*
						 * the forced event only cares about the postcondition -
						 * that there is the given value set, not about that
						 * there was no value before
						 */
					} else {
						// given old value does not concur with the current
						// value
						return XCommand.FAILED;
					}
				}
				
				if(setValue(actor, command.getValue())) {
					return oldRev;
				} else {
					return XCommand.NOCHANGE;
				}
			}
			
			return XCommand.FAILED;
		}
	}
	
	private long getOldRevisionNumber() {
		if(hasFather())
			return this.father.getOldRevisionNumber();
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
		assert !transactionInProgress();
		if(hasFather()) {
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
	public MemoryObject getFather() {
		checkRemoved();
		return this.father;
	}
	
	/**
	 * Returns whether this MemoryField has a father-{@link XObject} or not.
	 * 
	 * @return true, if this field has a father, false otherwise
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryField was already removed
	 */
	
	@ReadOperation
	public boolean hasFather() {
		checkRemoved();
		return (this.father != null);
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryField was already removed
	 */
	@ReadOperation
	public long getRevisionNumber() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.getRevisionNumber();
		}
	}
	
	protected long getObjectRevisionNumber() {
		if(hasFather())
			return this.father.getRevisionNumber();
		else
			return XEvent.RevisionOfEntityNotSet;
	}
	
	protected long getModelRevisionNumber() {
		if(hasFather())
			return this.father.getModelRevisionNumber();
		else
			return XEvent.RevisionOfEntityNotSet;
	}
	
	private boolean transactionInProgress() {
		if(hasFather())
			return this.father.transactionInProgress();
		
		return false;
	}
	
	public boolean isEmpty() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.getValue() == null;
		}
	}
	
	public XAddress getAddress() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.getAddress();
		}
	}
	
	protected void propagateFieldEvent(XFieldEvent event) {
		fireFieldEvent(event);
		
		// propagate this event to the fathers of this field
		// this also increments the revisionNumber of the fathers
		if(hasFather()) {
			MemoryObject tempFather = this.father;
			tempFather.fireFieldEvent(event);
			
			// propagate event to father model
			if(tempFather.hasFather()) {
				MemoryModel tempModel = tempFather.getFather();
				tempModel.fireFieldEvent(event);
				
				// propagate event to father repository
				if(tempModel.hasFather()) {
					tempModel.getFather().fireFieldEvent(event);
				}
			}
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
	 */
	protected void delete() {
		this.state.delete();
		this.removed = true;
	}
	
	/**
	 * Saves the state information of this MemoryField with the currently used
	 * persistence layer
	 */
	protected void save() {
		this.state.save();
	}
	
	/**
	 * @return the {@link XFieldState} object representing the current state of
	 *         this MemoryField
	 */
	protected XFieldState getState() {
		return this.state;
	}
	
}
