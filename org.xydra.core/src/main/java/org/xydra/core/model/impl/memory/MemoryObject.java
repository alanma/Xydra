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
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.delta.WrapperModel;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.core.model.state.impl.memory.MemoryChangeLogState;
import org.xydra.core.model.state.impl.memory.TemporaryObjectState;


/**
 * An implementation of {@link XObject}.
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */
public class MemoryObject extends SynchronizesChangesImpl implements XObject, Serializable {
	
	private static final long serialVersionUID = -808702139986657842L;
	
	private final XObjectState state;
	private final Map<XID,MemoryField> loadedFields = new HashMap<XID,MemoryField>();
	
	/** The father-model of this MemoryObject */
	private final MemoryModel father;
	
	/** Has this MemoryObject been removed? */
	boolean removed = false;
	
	private Set<XObjectEventListener> objectChangeListenerCollection;
	private Set<XFieldEventListener> fieldChangeListenerCollection;
	private Set<XTransactionEventListener> transactionListenerCollection;
	
	/**
	 * Creates a new MemoryObject without a father-{@link XModel}.
	 * 
	 * @param objectId The {@link XID} for this MemoryObject
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
	 * Creates a new MemoryObject without a father-{@link XModel}.
	 * 
	 * @param objectState The {@link XObjectState} for this MemoryObject
	 */
	public MemoryObject(XObjectState objectState) {
		this(null, createEventQueue(objectState), objectState);
	}
	
	private static MemoryEventQueue createEventQueue(XObjectState objectState) {
		XChangeLogState logState = objectState.getChangeLogState();
		MemoryChangeLog log = logState == null ? null : new MemoryChangeLog(logState);
		return new MemoryEventQueue(log);
	}
	
	/**
	 * Creates a new MemoryObject with the given {@link MemoryModel} as its
	 * father.
	 * 
	 * @param parent The father-{@link MemoryModel} of this MemoryObject.
	 * @param eventQueue The {@link MemoryEventQueue} which will be used by this
	 *            MemoryObject.
	 * @param objectState The initial {@link XObjectState} of this MemoryObject.
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
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryObject was already removed
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
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryObject was already removed
	 */
	public XID getID() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.getID();
		}
	}
	
	/**
	 * Removes all {@link XField XFields} of this MemoryObject from the
	 * persistence layer and the MemoryObject itself.
	 * 
	 * @param orphans The collection to which the removed {@link XField XFields}
	 *            are to be added to for server-client synchronization purposes
	 */
	protected void removeInternal(Orphans orphans) {
		// all fields are already loaded for creating events
		
		for(MemoryField field : this.loadedFields.values()) {
			field.getState().setValue(null);
			orphans.fields.put(field.getAddress(), field);
			this.state.removeFieldState(field.getID());
		}
		
		this.loadedFields.clear();
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryObject was already removed
	 */
	public boolean removeField(XID actor, XID fieldID) {
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
			Orphans orphans = getOrphans();
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
				// only increment if this event is no sub-event of a
				// transaction (needs to be handled differently)
				incrementRevisionAndSave();
				
				// propagate events
				this.eventQueue.sendEvents();
				
			}
			
			return true;
		}
	}
	
	/**
	 * Saves the current state information of this MemoryObject with the
	 * currently used persistence layer
	 */
	protected void save() {
		this.state.save();
	}
	
	/**
	 * @return the {@link XID} of the father-{@link XModel} of this MemoryObject
	 *         or null, if this object has no father.
	 */
	protected XID getModelId() {
		return this.father == null ? null : this.father.getID();
	}
	
	/**
	 * @return the {@link XID} of the father-{@link XRepository} of this
	 *         MemoryObject or null, if this object has no father.
	 */
	protected XID getRepositoryId() {
		return this.father == null ? null : this.father.getRepositoryId();
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryObject was already removed
	 */
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
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryObject was already removed
	 */
	public MemoryField createField(XID actor, XID fieldID) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			if(hasField(fieldID)) {
				return getField(fieldID);
			}
			
			MemoryField field = null;
			
			Orphans orphans = getOrphans();
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
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryObject was already removed
	 */
	public long executeObjectCommand(XID actor, XObjectCommand command) {
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
				
				createField(actor, command.getFieldID());
				
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
				
				removeField(actor, command.getFieldID());
				
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
			XChangeLog log = this.eventQueue.getChangeLog();
			if(log != null) {
				assert log.getCurrentRevisionNumber() > getRevisionNumber();
				setRevisionNumber(log.getCurrentRevisionNumber());
			} else {
				setRevisionNumber(getRevisionNumber() + 1);
			}
		}
		save();
	}
	
	/**
	 * Sets the revision number of this MemoryObject
	 * 
	 * @param newRevision the new revision number
	 */
	protected void setRevisionNumber(long newRevision) {
		this.state.setRevisionNumber(newRevision);
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryObject was already removed
	 */
	public Iterator<XID> iterator() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.iterator();
		}
	}
	
	/**
	 * Returns the father-{@link MemoryModel} of this MemoryObject.
	 * 
	 * @return The father-{@link MemoryModel} of this MemoryObject (may be
	 *         null).
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryObject was already removed
	 */
	@ReadOperation
	public MemoryModel getFather() {
		checkRemoved();
		return this.father;
	}
	
	/**
	 * Checks whether this object has a father-{@link MemoryModel} or not.
	 * 
	 * @return true, if this object has a father-model, false otherwise.
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryObject was already removed
	 */
	@ReadOperation
	public boolean hasFather() {
		checkRemoved();
		return (this.father != null);
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryObject was already removed
	 */
	public boolean hasField(XID id) {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.loadedFields.containsKey(id) || this.state.hasFieldState(id);
		}
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryObject was already removed
	 */
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
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryObject was already removed
	 */
	public boolean isEmpty() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.isEmpty();
		}
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryObject was already removed
	 */
	public XAddress getAddress() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.getAddress();
		}
	}
	
	/**
	 * Notifies all listeners that have registered interest for notification on
	 * {@link XObjectEvent XObjectEvents} happening on this MemoryObject.
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
	 * {@link XFieldEvent XFieldEvents} happening on child-{@link MemoryField
	 * MemoryFields} of this MemoryObject.
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
	 * {@link XTransactionEvent XTransactionEvents} happening on this
	 * MemoryModel.
	 * 
	 * @param event The {@link XTransactonEvent} which will be propagated to the
	 *            registered listeners.
	 */
	protected void fireTransactionEvent(XTransactionEvent event) {
		for(XTransactionEventListener listener : this.transactionListenerCollection) {
			listener.onChangeEvent(event);
		}
	}
	
	/**
	 * Adds the given {@link XObjectEventListener} to this MemoryObject, if
	 * possible.
	 * 
	 * @param changeListener The {@link XObjectEventListener} which is to be
	 *            added
	 * @return false, if the given {@link XObjectEventListener} was already
	 *         registered on this MemoryObject, true otherwise
	 */
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.objectChangeListenerCollection.add(changeListener);
		}
	}
	
	/**
	 * Removes the given {@link XObjectEventListener} from this MemoryObject.
	 * 
	 * @param changeListener The {@link XObjectEventListener} which is to be
	 *            removed
	 * @return true, if the given {@link XObjectEventListener} was registered on
	 *         this MemoryObject, false otherwise
	 */
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.objectChangeListenerCollection.remove(changeListener);
		}
	}
	
	/**
	 * Adds the given {@link XFieldEventListener} to this MemoryObject, if
	 * possible.
	 * 
	 * @param changeListener The {@link XFieldEventListener} which is to be
	 *            added
	 * @return false, if the given {@link XFieldEventListener} was already
	 *         registered on this MemoryObject, true otherwise
	 */
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.fieldChangeListenerCollection.add(changeListener);
		}
	}
	
	/**
	 * Removes the given {@link XFieldEventListener} from this MemoryObject.
	 * 
	 * @param changeListener The {@link XFieldEventListener} which is to be
	 *            removed
	 * @return true, if the given {@link XFieldEventListener} was registered on
	 *         this MemoryObject, false otherwise
	 */
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.fieldChangeListenerCollection.remove(changeListener);
		}
	}
	
	/**
	 * Adds the given {@link XTransactionEventListener} to this MemoryObject, if
	 * possible.
	 * 
	 * @param changeListener The {@link XTransactionEventListener} which is to
	 *            be added
	 * @return false, if the given {@link XTransactionEventListener} was already
	 *         registered on this MemoryObject, true otherwise
	 */
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.transactionListenerCollection.add(changeListener);
		}
	}
	
	/**
	 * Removes the given {@link XTransactionEventListener} from this
	 * MemoryObject.
	 * 
	 * @param changeListener The {@link XTransactionEventListener} which is to
	 *            be removed
	 * @return true, if the given {@link XTransactionEventListener} was
	 *         registered on this MemoryObject, false otherwise
	 */
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.transactionListenerCollection.remove(changeListener);
		}
	}
	
	/**
	 * @return the current revision number of the father-{@link MemoryModel} of
	 *         this MemoryObject or {@link XEvent#RevisionOfEntityNotSet} if
	 *         this MemoryObject has no father.
	 */
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
	
	/**
	 * Deletes the state information of this MemoryObject from the currently
	 * used persistence layer
	 */
	protected void delete() {
		for(XID fieldId : this) {
			MemoryField field = getField(fieldId);
			field.delete();
		}
		this.state.delete();
		this.removed = true;
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryObject was already removed
	 */
	public long executeCommand(XID actor, XCommand command) {
		synchronized(this.eventQueue) {
			checkRemoved();
			if(command instanceof XTransaction) {
				return executeTransaction(actor, (XTransaction)command);
			}
			if(command instanceof XObjectCommand) {
				return executeObjectCommand(actor, (XObjectCommand)command);
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
	protected MemoryObject createObject(XID actor, XID objectId) {
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
	protected boolean removeObject(XID actor, XID objectId) {
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
	
	@Override
	protected void saveIfModel() {
		// not a model, so nothing to do here
	}
	
	@Override
	protected void setRevisionNumberIfModel(long modelRevisionNumber) {
		// not a model, so nothing to do here
	}
	
	@Override
	protected void checkSync() {
		if(hasFather()) {
			throw new IllegalStateException(
			        "an object that is part of a model cannot be rolled abck / synchronized individualy");
		}
	}
}
