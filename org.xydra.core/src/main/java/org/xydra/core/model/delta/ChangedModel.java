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
	
	private final Set<XID> removed = new HashSet<XID>();
	private final Map<XID,NewObject> added = new HashMap<XID,NewObject>();
	private final Map<XID,ChangedObject> changed = new HashMap<XID,ChangedObject>();
	
	private final XBaseModel base;
	
	/**
	 * @param base The {@link XBaseModel} this ChangedModel will encapsulate and
	 *            represent
	 */
	/*
	 * TODO Woudln't it be better to actually copy the given base entitiy?
	 * (think about synchronization problems - somebody might change the base
	 * entity while this "changed" entity is being used, which may result in
	 * complete confusion (?))
	 */
	public ChangedModel(XBaseModel base) {
		this.base = base;
	}
	
	public void createObject(XID objectId) {
		if(!hasObject(objectId)) {
			XAddress objectAddr = XX.resolveObject(getAddress(), objectId);
			this.added.put(objectId, new NewObject(objectAddr));
		}
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
	 * @param max An upper bound for counting the amount of needed
	 *            {@link XCommands}. Note that setting this bound to little may
	 *            result in the return of an integer which does not actually
	 *            represent the minimal amount of needed {@link XCommand
	 *            XCommands} for the transformation
	 * @result the amount of needed {@link XCommand XCommands} for the
	 *         transformation
	 */
	// TODO I'm not sure if I got the purpose of "max" right
	public int countChanges(int max) {
		int n = this.removed.size();
		if(n < max) {
			for(NewObject object : this.added.values()) {
				n += object.countChanges(max - n);
				if(n >= max) {
					break;
				}
			}
			if(n < max) {
				for(ChangedObject object : this.changed.values()) {
					n += object.countChanges(max - n);
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
		
		return changedObject;
	}
	
	public void removeObject(XID objectId) {
		if(hasObject(objectId)) {
			if(!this.added.containsKey(objectId)) {
				this.removed.add(objectId);
			} else {
				this.added.remove(objectId);
			}
			this.changed.remove(objectId);
		}
	}
	
	/**
	 * @return the revision number of the original {@link XBaseModel}
	 */
	// TODO Maybe a method for returning the revision number this ChangedModel
	// would have if it would be a real model would be a good idea?
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
	 * successfully executed on this {@link DeltaModel} or if the attempt to
	 * execute it will fail.
	 * 
	 * @param DeltaModel The {@link DeltaModel} on which the given
	 *            {@link XModelCommand} is to be executed
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
			if(object.getRevisionNumber() != command.getRevisionNumber()) {
				if(!command.isForced()) {
					// command is invalid
					return false;
				}
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
	 * successfully executed on this {@link DeltaModel} or if the attempt to
	 * execute it will fail.
	 * 
	 * @param DeltaModel The {@link DeltaModel} on which the given
	 *            {@link XObjectCommand} is to be executed
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
			if(field.getRevisionNumber() != command.getRevisionNumber()) {
				if(!command.isForced()) {
					// command is invalid
					return false;
				}
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
	 * successfully executed on this {@link DeltaModel} or if the attempt to
	 * execute it will fail.
	 * 
	 * @param DeltaModel The {@link DeltaModel} on which the given
	 *            {@link XFieldCommand} is to be executed
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
	 * Apply the events contained in the transaction and return if all commands
	 * could be applied. If commands failed, the transaction will remain
	 * partially applied.
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
	
}
