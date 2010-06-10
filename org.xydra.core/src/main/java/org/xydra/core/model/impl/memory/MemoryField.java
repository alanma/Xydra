package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.core.X;
import org.xydra.core.XX;
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
	 * The object on which this field synchronizes its change operations
	 * 
	 * - if this field has a father-object with a father-model, the father-model
	 * will be used as the lock - if this field has a father-object with no
	 * father-model, the father-object will be used as the lock - if this field
	 * has no father-object it will use itself as the lock
	 */
	private final MemoryEventQueue eventQueue;
	
	private Set<XFieldEventListener> fieldChangeListenerCollection;
	
	/**
	 * Creates a new MemoryField without a father-object.
	 * 
	 * @param fieldId The {@link XID} for the field.
	 */
	public MemoryField(XID fieldId) {
		this(new TemporaryFieldState(X.getIDProvider().fromComponents(null, null, null, fieldId)));
	}
	
	/**
	 * Creates a new MemoryField without a father-object.
	 * 
	 * @param fieldState The {@link XFieldState} for the field.
	 */
	public MemoryField(XFieldState fieldState) {
		this(null, new MemoryEventQueue(null), fieldState);
	}
	
	/**
	 * Creates a new MemoryField with the given father-object.
	 * 
	 * @param lock The object used for synchronization.
	 * @param fieldState initial state
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
	 * @throws IllegalStateException if this field has already been removed
	 */
	private void checkRemoved() throws IllegalStateException {
		if(this.removed) {
			throw new IllegalStateException("this field has been removed");
		}
	}
	
	/**
	 * Returns the XID of this field.
	 * 
	 * @return The XID of this field
	 */
	@ReadOperation
	public XID getID() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.getID();
		}
	}
	
	/**
	 * Sets the value of this field.
	 * 
	 * @param actor The XID of the actor of this operation.
	 * @param newValue The value.
	 */
	@ModificationOperation
	public boolean setValue(XID actor, XValue newValue) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			XFieldEvent event = null;
			
			XValue oldValue = getValue();
			
			if(XX.equals(oldValue, newValue)) {
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
	 * Returns the value of this field.
	 * 
	 * @return The value of this field
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
	 * Returns the father-object of this field.
	 * 
	 * @return The father-object of this field.
	 */
	
	@ReadOperation
	public MemoryObject getFather() {
		checkRemoved();
		return this.father;
	}
	
	/**
	 * Returns whether this field has a father or not.
	 * 
	 * @return true, if this field has a father, false otherwise
	 */
	
	@ReadOperation
	public boolean hasFather() {
		checkRemoved();
		return (this.father != null);
	}
	
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
	 * Adds the given listener to this field, if possible
	 * 
	 * @param changeListener The listener which is to be added
	 * @return false, if the given listener is already added on this field, true
	 *         otherwise
	 */
	
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.fieldChangeListenerCollection.add(changeListener);
		}
	}
	
	/**
	 * Removes the given listener from this field.
	 * 
	 * @param changeListener The listener which is to be removed
	 * @return true, if the given listener was registered on this field, false
	 *         otherwise
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
	
	protected void delete() {
		this.state.delete();
		this.removed = true;
	}
	
	protected void save() {
		this.state.save();
	}
	
}
