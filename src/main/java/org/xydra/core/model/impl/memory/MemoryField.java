package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XReversibleFieldEvent;
import org.xydra.base.change.impl.memory.MemoryFieldCommand;
import org.xydra.base.change.impl.memory.MemoryReversibleFieldEvent;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.impl.memory.SimpleField;
import org.xydra.base.value.XValue;
import org.xydra.core.XCopyUtils;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XFieldSyncEventListener;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XObject;
import org.xydra.index.XI;
import org.xydra.sharedutils.XyAssert;


/**
 * An implementation of {@link XField}.
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */

public class MemoryField extends AbstractEntity implements XField, Serializable,
        XSendsFieldSyncEvents, Synchronizable {
	
	private static final long serialVersionUID = -4390811955475742528L;
	
	/**
	 * The {@link MemoryEventManager} used by this MemoryField. Will also be
	 * used as the lock for synchronizing change operations.
	 * 
	 * If this MemoryField is created by an {@link MemoryObject}, the event
	 * queue used by the {@link MemoryObject} will be used.
	 */
	private final MemoryEventManager eventQueue;
	
	/**
	 * the father-object which is holding this field (may be null)
	 */
	private final MemoryObject father;
	
	private final Set<XFieldEventListener> fieldChangeListenerCollection;
	
	/** Has this field been removed? */
	private boolean removed = false;
	
	private final XRevWritableField state;
	
	private final Set<XFieldSyncEventListener> syncChangeListenerCollection;
	
	/**
	 * Creates a new MemoryField with or without a father-{@link XObject}.
	 * 
	 * @param parent The father-{@link XObject} of this MemoryField (may be
	 *            null)
	 * @param eventQueue the {@link MemoryEventManager} this MemoryField will
	 *            use; never null.
	 * @param fieldState A {@link XRevWritableField} representing the initial
	 *            state of this field. The {@link XField} will continue using
	 *            this state object, so it must not be modified directly after
	 *            wrapping it in an {@link XField}.
	 */
	protected MemoryField(MemoryObject parent, MemoryEventManager eventQueue,
	        XRevWritableField fieldState) {
		XyAssert.xyAssert(eventQueue != null);
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
		this.syncChangeListenerCollection = new HashSet<XFieldSyncEventListener>();
	}
	
	/**
	 * Creates a new MemoryField without a father-{@link XObject}.
	 * 
	 * TODO What can I do with it? Can I ever add it to an XObject?
	 * 
	 * @param actorId The actor to be used in events generated by this field.
	 * @param fieldState A {@link XRevWritableField} representing the initial
	 *            state of this model. The {@link XField} will continue using
	 *            this state object, so it must not be modified directly after
	 *            wrapping it in an {@link XField}.
	 */
	public MemoryField(XID actorId, XRevWritableField fieldState) {
		this(null, new MemoryEventManager(actorId, null, null, -1), fieldState);
	}
	
	/**
	 * Creates a new MemoryField without a father-{@link XObject}.
	 * 
	 * TODO What can I do with it? Can I ever add it to an XObject?
	 * 
	 * @param actorId The actor to be used in events generated by this field.
	 * @param fieldId The {@link XID} for this MemoryField.
	 */
	public MemoryField(XID actorId, XID fieldId) {
		this(actorId, new SimpleField(XX.toAddress(null, null, null, fieldId)));
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
	
	@Override
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.fieldChangeListenerCollection.add(changeListener);
		}
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
	 * Cleans up a field that is being removed.
	 * 
	 * @param transaction
	 */
	protected void delete() {
		this.state.setValue(null);
		this.removed = true;
	}
	
	@Override
	@ReadOperation
	public boolean equals(Object object) {
		synchronized(this.eventQueue) {
			return super.equals(object);
		}
	}
	
	@Override
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
			
			XyAssert.xyAssert(!this.eventQueue.transactionInProgess);
			
			// check whether the given event actually refers to this field
			if(!getAddress().equals(command.getTarget())) {
				if(callback != null) {
					callback.onFailure();
				}
				return XCommand.FAILED;
			}
			
			if(!command.isForced() && this.getRevisionNumber() != command.getRevisionNumber()) {
				if(callback != null) {
					callback.onFailure();
				}
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
						if(callback != null) {
							callback.onFailure();
						}
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
						if(callback != null) {
							callback.onFailure();
						}
						return XCommand.FAILED;
					}
				}
				
				XyAssert.xyAssert(command.getValue() == null);
				
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
						if(callback != null) {
							callback.onFailure();
						}
						return XCommand.FAILED;
					}
				}
				
			} else {
				throw new IllegalArgumentException("Unknown field command type: " + command);
			}
			
			if(XI.equals(getValue(), command.getValue())) {
				if(callback != null) {
					callback.onSuccess(XCommand.NOCHANGE);
				}
				return XCommand.NOCHANGE;
			}
			
			this.eventQueue.newLocalChange(command, callback);
			
			setValueInternal(command.getValue());
			
			return oldRev + 1;
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
	
	@Override
	public XAddress getAddress() {
		synchronized(this.eventQueue) {
			return this.state.getAddress();
		}
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryField was already removed
	 */
	@Override
	@ReadOperation
	public XID getId() {
		synchronized(this.eventQueue) {
			return this.state.getId();
		}
	}
	
	protected long getModelRevisionNumber() {
		if(this.father != null)
			return this.father.getModelRevisionNumber();
		else
			return XEvent.RevisionOfEntityNotSet;
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
	
	protected long getObjectRevisionNumber() {
		if(this.father != null)
			return this.father.getRevisionNumber();
		else
			return XEvent.RevisionOfEntityNotSet;
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
	@Override
	@ReadOperation
	public long getRevisionNumber() {
		synchronized(this.eventQueue) {
			return this.state.getRevisionNumber();
		}
	}
	
	@Override
	public XID getSessionActor() {
		synchronized(this.eventQueue) {
			return this.eventQueue.getActor();
		}
	}
	
	/**
	 * @return the {@link XRevWritableField} object representing the current
	 *         state of this MemoryField
	 */
	protected XRevWritableField getState() {
		return this.state;
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryField was already removed
	 */
	@Override
	@ReadOperation
	public XValue getValue() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.getValue();
		}
	}
	
	@Override
	public MemoryObject getFather() {
		return this.father;
	}
	
	@Override
	public int hashCode() {
		synchronized(this.eventQueue) {
			return super.hashCode();
		}
	}
	
	protected void incrementRevision() {
		XyAssert.xyAssert(!this.eventQueue.transactionInProgess);
		if(this.father != null) {
			// this increments the revisionNumber of the father and sets
			// this revNr to the
			// revNr of the father
			this.father.incrementRevision();
			setRevisionNumber(this.father.getRevisionNumber());
		} else {
			setRevisionNumber(getRevisionNumber() + 1);
		}
	}
	
	@Override
	public boolean isEmpty() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.getValue() == null;
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
	
	@Override
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.fieldChangeListenerCollection.remove(changeListener);
		}
	}
	
	protected void setRevisionNumber(long newRevision) {
		this.state.setRevisionNumber(newRevision);
	}
	
	@Override
	public void setSessionActor(XID actorId) {
		synchronized(this.eventQueue) {
			if(this.father != null) {
				throw new IllegalStateException("cannot set actor on field with a parent");
			}
			this.eventQueue.setSessionActor(actorId, null);
		}
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryField was already removed
	 */
	@Override
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
		XyAssert.xyAssert(result >= 0 || result == XCommand.NOCHANGE);
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
		
		XyAssert.xyAssert(!XI.equals(oldValue, newValue));
		
		boolean inTrans = this.eventQueue.transactionInProgess;
		
		this.state.setValue(newValue);
		
		// check for field event type
		long modelRev = getModelRevisionNumber();
		long objectRev = getObjectRevisionNumber();
		long fieldRev = getRevisionNumber();
		XID actorId = this.eventQueue.getActor();
		XReversibleFieldEvent event = null;
		if((oldValue == null)) {
			XyAssert.xyAssert(newValue != null);
			assert newValue != null;
			event = MemoryReversibleFieldEvent.createAddEvent(actorId, getAddress(), newValue,
			        modelRev, objectRev, fieldRev, inTrans);
		} else {
			if(newValue == null) {
				// implies remove
				event = MemoryReversibleFieldEvent.createRemoveEvent(actorId, getAddress(),
				        oldValue, modelRev, objectRev, fieldRev, inTrans, false);
			} else {
				XyAssert.xyAssert(!newValue.equals(oldValue));
				// implies change
				event = MemoryReversibleFieldEvent.createChangeEvent(actorId, getAddress(),
				        oldValue, newValue, modelRev, objectRev, fieldRev, inTrans);
			}
		}
		
		XyAssert.xyAssert(event != null);
		assert event != null;
		
		this.eventQueue.enqueueFieldEvent(this, event);
		
		// event propagation and revision number increasing happens after
		// all events were successful
		if(!inTrans) {
			
			// increment revision number
			incrementRevision();
			
			// dispatch events
			this.eventQueue.sendEvents();
			
		}
		
	}
	
	@Override
	public String toString() {
		return this.getId() + " rev[" + this.getRevisionNumber() + "]";
	}
	
	@Override
	synchronized public XRevWritableField createSnapshot() {
		if(this.removed) {
			return null;
		}
		return XCopyUtils.createSnapshot(this);
	}
	
	@Override
	public XType getType() {
		return XType.XFIELD;
	}
	
	@Override
	public boolean removeListenerForFieldSyncEvents(XFieldSyncEventListener syncListener) {
		synchronized(this.eventQueue) {
			return this.syncChangeListenerCollection.remove(syncListener);
		}
	}
	
	public void fireFieldSyncEvent(XFieldEvent event) {
		for(XFieldSyncEventListener listener : this.syncChangeListenerCollection) {
			listener.onSynced(event);
		}
	}
	
	@Override
	public boolean addListenerForFieldSyncEvents(XFieldSyncEventListener syncListener) {
		synchronized(this.eventQueue) {
			return this.syncChangeListenerCollection.add(syncListener);
		}
	}
	
	@Override
	public boolean isSynchronized() {
		
		assert getFather() != null : "A standalone MemoryField can't be synchronized.";
		
		/*
		 * If the field's RevNo is smaller than its father's SyncRevNo, the
		 * field is synchronized.
		 */
		return (getRevisionNumber() <= getFather().getSynchronizedRevision());
	}
}
