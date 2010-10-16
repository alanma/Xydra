package org.xydra.core.model.delta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicCommand;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.index.iterator.AbstractFilteringIterator;
import org.xydra.index.iterator.BagUnionIterator;


/**
 * An {@link XBaseField}/{@link DeltaField} that represents changes to an
 * {@link XBaseField}.
 * 
 * An {@link XBaseField} is passed as an argument of the constructor. This
 * ChangedField will than basically represent the given {@link XBaseField} and
 * allow changes on its set of {@link XBaseObject XBaseObjects}. The changes do
 * not happen directly on the passed {@link XBaseField} but rather on a sort of
 * copy that emulates the passed {@link XBaseModel}. A ChangedModel provides
 * methods to compare the current state to the state the passed
 * {@link XBaseModel} was in at creation time.
 * 
 * @author dscharrer
 * 
 */
public class ChangedModel implements DeltaModel {
	
	// Fields that are in base but have been removed.
	// Contains no XIDs that are in added or changed.
	private final Set<XID> removed = new HashSet<XID>();
	
	// Fields that are not in base and have been added.
	// Contains no XIDs that are in removed or changed.
	private final Map<XID,NewObject> added = new HashMap<XID,NewObject>();
	
	// Fields that are in base and have not been removed.
	// While they were changed once, those changes might have been reverted.
	// Contains no XIDs that are in added or removed.
	private final Map<XID,ChangedObject> changed = new HashMap<XID,ChangedObject>();
	
	private final XBaseModel base;
	
	/**
	 * Wrap an {@link XBaseModel} to record a set of changes made. Multiple
	 * changes will be combined as much as possible such that a minimal set of
	 * changes remains.
	 * 
	 * Note that this is a very lightweight wrapper intended for a short
	 * lifetime. As a consequence, the wrapped {@link XBaseModel} is not copied
	 * and changes to it or any contained objects and fields (as opposed to this
	 * {@link ChangedModel}) may result in undefined behavior of the
	 * {@link ChangedModel}.
	 * 
	 * @param base The {@link XBaseModel} this ChangedModel will encapsulate and
	 *            represent
	 */
	public ChangedModel(XBaseModel base) {
		this.base = base;
	}
	
	private boolean checkSetInvariants() {
		
		for(XID id : this.removed) {
			assert !this.added.containsKey(id) && !this.changed.containsKey(id);
			assert this.base.hasObject(id);
		}
		
		for(XID id : this.added.keySet()) {
			assert !this.removed.contains(id) && !this.changed.containsKey(id);
			assert !this.base.hasObject(id);
			assert id.equals(this.added.get(id).getID());
		}
		
		for(XID id : this.changed.keySet()) {
			assert !this.removed.contains(id) && !this.added.containsKey(id);
			assert this.base.hasObject(id);
			assert id.equals(this.changed.get(id).getID());
		}
		
		return true;
	}
	
	public void createObject(XID objectId) {
		
		if(!hasObject(objectId)) {
			XBaseObject object = this.base.getObject(objectId);
			if(object != null) {
				// If the field previously existed it must have been removed
				// previously and we can merge the remove and add changes.
				assert this.removed.contains(objectId);
				this.removed.remove(objectId);
				if(!object.isEmpty()) {
					ChangedObject cf = new ChangedObject(object);
					cf.clear();
					this.changed.put(objectId, cf);
				}
			} else {
				// Otherwise, the field is completely new.
				XAddress fieldAddr = XX.resolveObject(getAddress(), objectId);
				this.added.put(objectId, new NewObject(fieldAddr));
			}
		}
		
		assert checkSetInvariants();
	}
	
	/**
	 * @return the {@link XID XIDs} of objects that existed in the original
	 *         model but have been removed from this ChangedModel
	 */
	public Iterable<XID> getRemovedObjects() {
		return this.removed;
	}
	
	/**
	 * @return the {@link NewObject NewObjects} that have been added to this
	 *         ChangedModel and were not contained in the original
	 *         {@link XBaseModel}
	 */
	public Iterable<NewObject> getNewObjects() {
		return this.added.values();
	}
	
	/**
	 * @return an iterable of the objects that already existed in the original
	 *         {@link XBaseModel} but have been changed. Note: their current
	 *         state might be the same as the original one
	 */
	public Iterable<ChangedObject> getChangedObjects() {
		return this.changed.values();
	}
	
	/**
	 * Count the minimal number of {@link XCommand XCommands} that would be
	 * needed to transform the original {@link XBaseModel} to the current state
	 * which is represented by this ChangedModel.
	 * 
	 * This is different to {@link #countEventsNeeded} in that a removed object
	 * or field may cause several events while only needing one command.
	 * 
	 * @param max An upper bound for counting the amount of needed
	 *            {@link XCommands}. Note that setting this bound to little may
	 *            result in the return of an integer which does not actually
	 *            represent the minimal amount of needed {@link XCommand
	 *            XCommands} for the transformation.
	 * @result the amount of needed {@link XCommand XCommands} for the
	 *         transformation
	 */
	public int countCommandsNeeded(int max) {
		int n = this.removed.size() + this.added.size();
		if(n < max) {
			for(NewObject object : this.added.values()) {
				n += object.countChanges(max - n + 1) - 1;
				if(n >= max) {
					return n;
				}
			}
			for(ChangedObject object : this.changed.values()) {
				n += object.countCommandsNeeded(max - n);
				if(n >= max) {
					return n;
				}
			}
		}
		return n;
	}
	
	/**
	 * Count the number of {@link XEvents XEvents} that would be needed to log
	 * the transformation of the original {@link XBaseModel} to the current
	 * state which is represented by this ChangedModel.
	 * 
	 * This is different to {@link #countCommandsNeeded} in that a removed
	 * object or field may cause several events while only needing one command.
	 * 
	 * @param max An upper bound for counting the amount of needed
	 *            {@link XEvents XEvents}. Note that setting this bound to
	 *            little may result in the return of an integer which does not
	 *            actually represent the minimal amount of needed
	 *            {@link XEvents XEvents} for the transformation.
	 * @result the amount of needed {@link XEvents XEvents} for the
	 *         transformation
	 */
	public int countEventsNeeded(int max) {
		int n = this.removed.size() + this.added.size();
		if(n < max) {
			for(XID objectId : this.removed) {
				// removing object itself already counted
				XBaseObject oldObject = getOldObject(objectId);
				for(XID fieldId : this.removed) {
					n++; // removing the field
					if(n >= max) {
						return n;
					}
					XBaseField oldField = oldObject.getField(fieldId);
					if(!oldField.isEmpty()) {
						n++; // removing the value
						if(n >= max) {
							return n;
						}
					}
				}
			}
			for(NewObject object : this.added.values()) {
				n += object.countChanges(max - n + 1) - 1;
				if(n >= max) {
					break;
				}
			}
			if(n < max) {
				for(ChangedObject object : this.changed.values()) {
					n += object.countEventsNeeded(max - n);
					if(n >= max) {
						break;
					}
				}
			}
		}
		return n;
	}
	
	public DeltaObject getObject(XID objectId) {
		
		NewObject newObject = this.added.get(objectId);
		if(newObject != null) {
			return newObject;
		}
		
		ChangedObject changedObject = this.changed.get(objectId);
		if(changedObject != null) {
			return changedObject;
		}
		
		if(this.removed.contains(objectId)) {
			return null;
		}
		
		XBaseObject object = this.base.getObject(objectId);
		if(object == null) {
			return null;
		}
		
		changedObject = new ChangedObject(object);
		this.changed.put(objectId, changedObject);
		
		assert checkSetInvariants();
		
		return changedObject;
	}
	
	public void removeObject(XID objectId) {
		
		if(this.added.containsKey(objectId)) {
			
			// Never existed in base, so removing from added is sufficient.
			assert !this.base.hasObject(objectId) && !this.changed.containsKey(objectId);
			assert !this.removed.contains(objectId);
			
			this.added.remove(objectId);
			
			assert checkSetInvariants();
			
		} else if(!this.removed.contains(objectId) && this.base.hasObject(objectId)) {
			
			// Exists in base and not removed yet.
			assert !this.added.containsKey(objectId);
			
			this.removed.add(objectId);
			this.changed.remove(objectId);
			
			assert checkSetInvariants();
			
		}
		
	}
	
	/**
	 * Return the revision number of the wrapped {@link XBaseModel}. The
	 * revision number does not increase with changes to this
	 * {@link ChangedModel}.
	 * 
	 * @return the revision number of the original {@link XBaseModel}
	 */
	public long getRevisionNumber() {
		return this.base.getRevisionNumber();
	}
	
	public XID getID() {
		return this.base.getID();
	}
	
	public boolean hasObject(XID objectId) {
		return this.added.containsKey(objectId)
		        || (!this.removed.contains(objectId) && this.base.hasObject(objectId));
	}
	
	public XAddress getAddress() {
		return this.base.getAddress();
	}
	
	public Iterator<XID> iterator() {
		
		Iterator<XID> filtered = new AbstractFilteringIterator<XID>(this.base.iterator()) {
			@Override
			protected boolean matchesFilter(XID entry) {
				return !ChangedModel.this.removed.contains(entry);
			}
		};
		
		return new BagUnionIterator<XID>(filtered, this.added.keySet().iterator());
	}
	
	/**
	 * @return the {@link XBaseObject} with the given {@link XID} as it exists
	 *         in the original {@link XBaseModel}.
	 */
	public XBaseObject getOldObject(XID objectId) {
		return this.base.getObject(objectId);
	}
	
	public boolean isEmpty() {
		
		if(!this.added.isEmpty()) {
			return false;
		}
		
		if(this.removed.isEmpty()) {
			return this.base.isEmpty();
		}
		
		if(this.changed.size() > this.removed.size()) {
			return false;
		}
		
		for(XID objectId : this.base) {
			if(!this.removed.contains(objectId)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Checks if the given {@link XModelCommand} is valid and can be
	 * successfully executed on this ChangedModel or if the attempt to execute
	 * it will fail.
	 * 
	 * @param command The {@link XModelCommand} which is to be checked.
	 * 
	 * @return true, if the {@link XModelCommand} is valid and can be executed,
	 *         false otherwise
	 */
	public boolean executeCommand(XModelCommand command) {
		
		XID objectId = command.getObjectID();
		
		switch(command.getChangeType()) {
		
		case ADD:
			if(hasObject(objectId)) {
				// command is invalid or doesn't change anything
				return command.isForced();
			}
			// command is OK and adds a new object
			createObject(objectId);
			return true;
			
		case REMOVE:
			XBaseObject object = getObject(objectId);
			
			if(object == null) {
				// command is invalid or doesn't change anything
				return command.isForced();
			}
			if(object.getRevisionNumber() != command.getRevisionNumber() && !command.isForced()) {
				// command is invalid
				return false;
			}
			// command is OK and removes an existing object
			removeObject(objectId);
			return true;
			
		default:
			throw new AssertionError("impossible type for model commands");
		}
		
	}
	
	/**
	 * Checks if the given {@link XObjectCommand} is valid and can be
	 * successfully executed on this ChangedModel or if the attempt to execute
	 * it will fail.
	 * 
	 * @param command The {@link XObjectCommand} which is to be checked.
	 * 
	 * @return true, if the {@link XObjectCommand} is valid and can be executed,
	 *         false otherwise
	 */
	public boolean executeCommand(XObjectCommand command) {
		
		DeltaObject object = getObject(command.getObjectID());
		if(object == null) {
			// command is invalid
			return false;
		}
		
		XID fieldId = command.getFieldID();
		
		switch(command.getChangeType()) {
		
		case ADD:
			if(object.hasField(fieldId)) {
				// command is invalid or doesn't change anything
				return command.isForced();
			}
			// command is OK and adds a new field
			object.createField(fieldId);
			return true;
			
		case REMOVE:
			XBaseField field = object.getField(fieldId);
			
			if(field == null) {
				// command is invalid or doesn't change anything
				return command.isForced();
			}
			if(field.getRevisionNumber() != command.getRevisionNumber() && !command.isForced()) {
				// command is invalid
				return false;
			}
			// command is OK and removes an existing field
			object.removeField(fieldId);
			return true;
			
		default:
			throw new AssertionError("impossible type for object commands");
		}
		
	}
	
	/**
	 * Checks if the given {@link XFieldCommand} is valid and can be
	 * successfully executed on this ChangedModel or if the attempt to execute
	 * it will fail.
	 * 
	 * @param command The {@link XFieldCommand} which is to be checked.
	 * 
	 * @return true, if the {@link XFieldCommand} is valid and can be executed,
	 *         false otherwise
	 */
	public boolean executeCommand(XFieldCommand command) {
		
		DeltaObject object = getObject(command.getObjectID());
		if(object == null) {
			// command is invalid
			return false;
		}
		
		DeltaField field = object.getField(command.getFieldID());
		if(field == null) {
			// command is invalid
			return false;
		}
		
		if(!command.isForced()) {
			if(field.getRevisionNumber() != command.getRevisionNumber()) {
				// command is invalid (wrong revision)
				return false;
			}
			if((command.getChangeType() == ChangeType.ADD) != field.isEmpty()) {
				// command is invalid (wrong type)
				return false;
			}
		}
		
		// command is OK
		field.setValue(command.getValue());
		
		return true;
	}
	
	/**
	 * Apply the {@link XCommand XCommands} contained in the given
	 * {@link XTransaction} and return true, if all {@link XCommand XCommands}
	 * could be applied. If one of the {@link XCommand XCommands} failed, the
	 * {@link XTransactio}n will remain partially applied, already executed
	 * {@link XCommand XCommands} will not be rolled back.
	 * 
	 * @param transaction The {@link XTransaction} which is to be executed
	 * @return true, if the given {@link XTransaction} could be executed, false
	 *         otherwise
	 * 
	 *         TODO it might be a good idea to tell the caller of this method
	 *         which commands of the transaction where executed and not only
	 *         return false
	 */
	public boolean executeCommand(XTransaction transaction) {
		for(int i = 0; i < transaction.size(); i++) {
			XAtomicCommand command = transaction.getCommand(i);
			
			if(command instanceof XModelCommand) {
				if(!executeCommand((XModelCommand)command)) {
					return false;
				}
			} else if(command instanceof XObjectCommand) {
				if(!executeCommand((XObjectCommand)command)) {
					return false;
				}
			} else if(command instanceof XFieldCommand) {
				if(!executeCommand((XFieldCommand)command)) {
					return false;
				}
			} else {
				assert false : "transactions can only contain model, object and field commands";
			}
		}
		return true;
	}
	
	/**
	 * Apply the given command to this changed mode. Failed commands may be left
	 * partially applied.
	 * 
	 * @return true if the command succeeded, false otherwise.
	 */
	public boolean executeCommand(XCommand command) {
		if(command instanceof XTransaction) {
			return executeCommand((XTransaction)command);
		} else if(command instanceof XModelCommand) {
			return executeCommand((XModelCommand)command);
		} else if(command instanceof XObjectCommand) {
			return executeCommand((XObjectCommand)command);
		} else if(command instanceof XFieldCommand) {
			return executeCommand((XFieldCommand)command);
		} else {
			throw new IllegalArgumentException("unexpected command type: " + command);
		}
	}
	
	public void clear() {
		
		this.added.clear();
		this.changed.clear();
		for(XID id : this.base) {
			// IMPROVE maybe add a "cleared" flag to remove all fields more
			// efficiently?
			this.removed.add(id);
		}
		
		assert checkSetInvariants();
	}
	
}
