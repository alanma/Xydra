package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.ReadOperation;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.change.impl.memory.MemoryFieldEvent;
import org.xydra.core.change.impl.memory.MemoryObjectEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.delta.WrapperModel;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.core.model.state.impl.memory.MemoryChangeLogState;
import org.xydra.core.model.state.impl.memory.TemporaryObjectState;


/**
 * An in-memory {@link XObject}.
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */
public class MemoryObject extends TransactionManager implements XObject, Serializable {
	
	private static final long serialVersionUID = -808702139986657842L;
	
	private final XObjectState state;
	private final Map<XID,MemoryField> loadedFields = new HashMap<XID,MemoryField>();
	
	/** The father-model of this object */
	private final MemoryModel father;
	
	/** Has this object been removed? */
	boolean removed = false;
	
	/**
	 * The object on which this object synchronizes its change operations.
	 * 
	 * - if this object has a father-model, it will be used as the lock - if
	 * this object has no father-model, it will use itself as the lock
	 */
	
	private Set<XObjectEventListener> objectChangeListenerCollection;
	private Set<XFieldEventListener> fieldChangeListenerCollection;
	private Set<XTransactionEventListener> transactionListenerCollection;
	
	/**
	 * Creates a new MemoryObject without a father.
	 * 
	 * @param objectId The {@link XID} for this object
	 */
	public MemoryObject(XID objectId) {
		this(createObjectState(objectId));
	}
	
	private static XObjectState createObjectState(XID objectId) {
		XAddress objectAddr = X.getIDProvider().fromComponents(null, null, objectId, null);
		XChangeLogState changeLogState = new MemoryChangeLogState(objectAddr, 0L);
		return new TemporaryObjectState(objectAddr, changeLogState);
	}
	
	/**
	 * Creates a new MemoryObject without a father.
	 * 
	 * @param objectState The {@link XObjectState} for this object
	 */
	public MemoryObject(XObjectState objectState) {
		this(null, new MemoryEventQueue(objectState.getChangeLogState() == null ? null
		        : new MemoryChangeLog(objectState.getChangeLogState())), objectState);
	}
	
	/**
	 * Creates a new MemoryObject with the given model a its father.
	 * 
	 * @param lock The object to synchronize operations on.
	 * @param objectState initial state
	 */
	protected MemoryObject(MemoryModel parent, MemoryEventQueue eventQueue, XObjectState objectState) {
		super(eventQueue);
		
		assert eventQueue != null;
		
		if(objectState == null) {
			throw new IllegalArgumentException("objectState may not be null");
		}
		this.state = objectState;
		
		if(parent == null && objectState.getAddress().getModel() != null) {
			throw new IllegalArgumentException("must load object through containing model");
		}
		this.father = parent;
		
		this.objectChangeListenerCollection = new HashSet<XObjectEventListener>();
		this.fieldChangeListenerCollection = new HashSet<XFieldEventListener>();
		this.transactionListenerCollection = new HashSet<XTransactionEventListener>();
	}
	
	/**
	 * @throws IllegalStateException if this object has already been removed
	 */
	@Override
	protected void checkRemoved() throws IllegalStateException {
		if(this.removed) {
			throw new IllegalStateException("this object has been removed");
		}
	}
	
	@ReadOperation
	@Override
	public int hashCode() {
		int hashCode = this.getID().hashCode() + (int)this.getRevisionNumber();
		
		if(this.father != null) {
			hashCode += this.father.getID().hashCode();
			
			XRepository repoFather = this.father.getFather();
			
			if(repoFather != null) {
				hashCode += repoFather.getID().hashCode();
			}
		}
		return hashCode;
	}
	
	@ReadOperation
	@Override
	public boolean equals(Object object) {
		if(!(object instanceof MemoryObject)) {
			return false;
		}
		
		MemoryObject memoryObject = (MemoryObject)object;
		
		// compare revision number, father-model id (if it exists),
		// father-repository id (if it exists)
		boolean result = (this.getRevisionNumber() == memoryObject.getRevisionNumber())
		        && (this.getID().equals(memoryObject.getID()));
		
		if(this.father != null) {
			if(memoryObject.father == null) {
				return false;
			}
			
			result = result && (this.father.getID().equals(memoryObject.father.getID()));
			
			if(this.father.getFather() != null) {
				if(memoryObject.father.getFather() == null) {
					return false;
				}
				
				result = result
				        && (this.father.getFather().getID().equals(memoryObject.father.getFather()
				                .getID()));
			}
		}
		
		return result;
	}
	
	@ReadOperation
	@Override
	public String toString() {
		return this.getID() + "-v" + this.getRevisionNumber() + " " + this.state.toString();
	}
	
	public XID getID() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.getID();
		}
	}
	
	public boolean removeField(XID actor, XID fieldID) {
		return removeField(actor, fieldID, null);
	}
	
	protected void removeInternal(Orphans orphans) {
		// all fields are already loaded for creating events
		
		for(MemoryField field : this.loadedFields.values()) {
			field.getState().setValue(null);
			orphans.fields.put(field.getAddress(), field);
			this.state.removeFieldState(field.getID());
		}
		
		this.loadedFields.clear();
	}
	
	protected boolean removeField(XID actor, XID fieldID, Orphans orphans) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			if(!hasField(fieldID)) {
				return false;
			}
			
			MemoryField field = getField(fieldID);
			assert field != null : "we checked above";
			
			boolean makeTrans = !field.isEmpty();
			int since = this.eventQueue.getNextPosition();
			enqueueFieldRemoveEvents(actor, field, makeTrans || transactionInProgress());
			
			// actually remove the field
			this.state.removeFieldState(field.getID());
			this.loadedFields.remove(field.getID());
			if(orphans != null) {
				field.getState().setValue(null);
				orphans.fields.put(field.getAddress(), field);
			} else {
				field.delete();
			}
			
			// event propagation must be handled differently if a
			// transaction is in progress
			// event propagation and revision number increasing happens
			// after all events were successful
			if(!transactionInProgress()) {
				
				if(makeTrans) {
					this.eventQueue.createTransactionEvent(actor, getFather(), this, since);
				}
				
				// increment revision number
				// only increment if this event is no subevent of a
				// transaction (needs to be handled differently)
				incrementRevisionAndSave();
				
				// propagate events
				this.eventQueue.sendEvents();
				
			}
			
			return true;
		}
	}
	
	protected void save() {
		this.state.save();
	}
	
	protected XID getModelId() {
		return this.father == null ? null : this.father.getID();
	}
	
	protected XID getRepositoryId() {
		return this.father == null ? null : this.father.getRepositoryId();
	}
	
	public MemoryField getField(XID fieldID) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			MemoryField field = this.loadedFields.get(fieldID);
			if(field != null) {
				return field;
			}
			
			if(!this.state.hasFieldState(fieldID)) {
				return null;
			}
			
			XFieldState fieldState = this.state.getFieldState(fieldID);
			assert fieldState != null;
			field = new MemoryField(this, this.eventQueue, fieldState);
			this.loadedFields.put(fieldID, field);
			
			return field;
		}
	}
	
	public MemoryField createField(XID actor, XID fieldID) {
		return createField(actor, fieldID, null);
	}
	
	protected MemoryField createField(XID actor, XID fieldID, Orphans orphans) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			if(hasField(fieldID)) {
				return getField(fieldID);
			}
			
			MemoryField field = null;
			
			if(orphans != null) {
				XAddress fieldAddr = XX.resolveField(getAddress(), fieldID);
				field = orphans.fields.remove(fieldAddr);
			}
			
			if(field == null) {
				XFieldState fieldState = this.state.createFieldState(fieldID);
				assert XX.contains(getAddress(), fieldState.getAddress());
				field = new MemoryField(this, this.eventQueue, fieldState);
			}
			
			this.state.addFieldState(field.getState());
			this.loadedFields.put(field.getID(), field);
			
			XObjectEvent event = MemoryObjectEvent.createAddEvent(actor, getAddress(), field
			        .getID(), getModelRevisionNumber(), getRevisionNumber(),
			        transactionInProgress());
			
			this.eventQueue.enqueueObjectEvent(this, event);
			
			// event propagation and revision number increasing happens
			// after all events were successful
			if(!transactionInProgress()) {
				
				// increment revision number
				// only increment if this event is no subevent of a transaction
				// (needs to be handled differently)
				field.incrementRevisionAndSave();
				
				// propagate events
				this.eventQueue.sendEvents();
				
			}
			
			return field;
		}
	}
	
	public long executeObjectCommand(XID actor, XObjectCommand command) {
		return executeObjectCommand(actor, command, null);
	}
	
	public long executeObjectCommand(XID actor, XObjectCommand command, Orphans orphans) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			if(!getAddress().equals(command.getTarget())) {
				return XCommand.FAILED;
			}
			
			if(command.getChangeType() == ChangeType.ADD) {
				if(hasField(command.getFieldID())) {
					// ID already taken
					if(command.isForced()) {
						/*
						 * the forced event only cares about the postcondition -
						 * that there is a field with the given ID, not about
						 * that there was no such field before
						 */
						return XCommand.NOCHANGE;
					}
					return XCommand.FAILED;
				}
				
				long oldRev = getOldRevisionNumber();
				
				createField(actor, command.getFieldID(), orphans);
				
				return oldRev;
			}
			
			if(command.getChangeType() == ChangeType.REMOVE) {
				XField oldField = getField(command.getFieldID());
				
				if(oldField == null) {
					// ID not taken
					if(command.isForced()) {
						/*
						 * the forced event only cares about the postcondition -
						 * that there is no field with the given ID, not about
						 * that there was such a field before
						 */
						return XCommand.NOCHANGE;
					}
					return XCommand.FAILED;
				}
				
				if(!command.isForced()
				        && oldField.getRevisionNumber() != command.getRevisionNumber()) {
					return XCommand.FAILED;
				}
				
				long oldRev = getOldRevisionNumber();
				
				removeField(actor, command.getFieldID(), orphans);
				
				return oldRev;
			}
			
			return XCommand.FAILED;
		}
	}
	
	@Override
	protected void incrementRevisionAndSave() {
		assert !transactionInProgress();
		if(hasFather()) {
			// this increments the revisionNumber of the father and sets
			// this revNr to the revNr of the father
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
	
	public Iterator<XID> iterator() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.iterator();
		}
	}
	
	/**
	 * Returns the father-model of this object.
	 * 
	 * @return The father-model of this object (may be null).
	 */
	@ReadOperation
	public MemoryModel getFather() {
		checkRemoved();
		return this.father;
	}
	
	/**
	 * Checks whether this object has a father-model or not.
	 * 
	 * @return true, if this object has a father-model, false otherwise.
	 */
	@ReadOperation
	public boolean hasFather() {
		checkRemoved();
		return (this.father != null);
	}
	
	public boolean hasField(XID id) {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.loadedFields.containsKey(id) || this.state.hasFieldState(id);
		}
	}
	
	@ReadOperation
	public long getRevisionNumber() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.getRevisionNumber();
		}
	}
	
	@Override
	protected boolean transactionInProgress() {
		if(hasFather())
			return super.transactionInProgress() || this.father.transactionInProgress();
		return super.transactionInProgress();
	}
	
	public boolean isEmpty() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.isEmpty();
		}
	}
	
	public XAddress getAddress() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.getAddress();
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
	
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.objectChangeListenerCollection.add(changeListener);
		}
	}
	
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.objectChangeListenerCollection.remove(changeListener);
		}
	}
	
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.fieldChangeListenerCollection.add(changeListener);
		}
	}
	
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.fieldChangeListenerCollection.remove(changeListener);
		}
	}
	
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.transactionListenerCollection.add(changeListener);
		}
	}
	
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.transactionListenerCollection.remove(changeListener);
		}
	}
	
	protected long getModelRevisionNumber() {
		if(hasFather())
			return this.father.getRevisionNumber();
		else
			return XEvent.RevisionOfEntityNotSet;
	}
	
	@Override
	protected long getOldRevisionNumber() {
		if(hasFather())
			return this.father.getRevisionNumber();
		else
			return getRevisionNumber();
	}
	
	/**
	 * Builds a transaction that first removes the value of the given field and
	 * then the given field itself.
	 * 
	 * @param actor The actor for this transaction
	 * @param field The field which should be removed by the transaction
	 * @return An {@link ObjectTransaction} that first removes the value of the
	 *         given field and then the given field itself.
	 */
	protected void enqueueFieldRemoveEvents(XID actor, MemoryField field, boolean inTrans) {
		
		if(field == null) {
			throw new NullPointerException("field must not be null");
		}
		
		long modelRev = getModelRevisionNumber();
		
		if(field.getValue() != null) {
			assert inTrans;
			XFieldEvent event = MemoryFieldEvent.createRemoveEvent(actor, field.getAddress(), field
			        .getValue(), modelRev, getRevisionNumber(), field.getRevisionNumber(), inTrans);
			this.eventQueue.enqueueFieldEvent(field, event);
		}
		
		XObjectEvent event = MemoryObjectEvent.createRemoveEvent(actor, getAddress(),
		        field.getID(), modelRev, getRevisionNumber(), field.getRevisionNumber(), inTrans);
		this.eventQueue.enqueueObjectEvent(this, event);
		
	}
	
	protected void delete() {
		for(XID fieldId : this) {
			MemoryField field = getField(fieldId);
			field.delete();
		}
		this.state.delete();
		this.removed = true;
	}
	
	public long executeCommand(XID actor, XCommand command) {
		return executeCommand(actor, command, null);
	}
	
	protected long executeCommand(XID actor, XCommand command, Orphans orphans) {
		synchronized(this.eventQueue) {
			checkRemoved();
			if(command instanceof XTransaction) {
				return executeTransaction(actor, (XTransaction)command, orphans);
			}
			if(command instanceof XObjectCommand) {
				return executeObjectCommand(actor, (XObjectCommand)command, orphans);
			}
			if(command instanceof XFieldCommand) {
				XField field = getField(command.getTarget().getField());
				if(field != null) {
					return field.executeFieldCommand(actor, (XFieldCommand)command);
				}
			}
			return XCommand.FAILED;
		}
	}
	
	@Override
	protected MemoryObject createObject(XID actor, XID objectId, Orphans orphans) {
		throw new AssertionError("object transactions cannot create objects");
	}
	
	@Override
	protected MemoryObject getObject(XID objectId) {
		if(getID().equals(objectId)) {
			return this;
		}
		return null;
	}
	
	protected boolean hasObject(XID objectId) {
		return getID().equals(objectId);
	}
	
	@Override
	protected boolean removeObject(XID actor, XID objectId, Orphans orphans) {
		throw new AssertionError("object transactions cannot remove objects");
	}
	
	@Override
	protected MemoryModel getModel() {
		return getFather();
	}
	
	@Override
	protected MemoryObject getObject() {
		return this;
	}
	
	@Override
	protected XBaseModel getTransactionTarget() {
		if(hasFather()) {
			return getFather();
		}
		return new WrapperModel(this);
	}
	
	protected XObjectState getState() {
		return this.state;
	}
	
	public XChangeLog getChangeLog() {
		return this.eventQueue.getChangeLog();
	}
	
}
