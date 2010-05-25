package org.xydra.core.change;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.change.impl.memory.MemoryFieldCommand;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.change.impl.memory.MemoryObjectCommand;
import org.xydra.core.change.impl.memory.MemoryTransaction;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.delta.ChangedField;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.ChangedObject;
import org.xydra.core.model.delta.NewField;
import org.xydra.core.model.delta.NewObject;
import org.xydra.core.value.XValue;



/**
 * A collection of helper methods to build transaction that change a
 * model/object/field to a specific state.
 * 
 * @author dscharrer
 * 
 */
public class XTransactionBuilder implements Iterable<XAtomicCommand> {
	
	private final XAddress target;
	private final List<XAtomicCommand> commands;
	
	/**
	 * Create a new transaction builder for constructing a transaction that will
	 * operate on the given target.
	 * 
	 * @param target The address of a model or an object to which all the
	 *            commands in this transaction apply.
	 */
	public XTransactionBuilder(XAddress target) {
		
		if((target.getModel() == null && target.getObject() == null) || target.getField() != null)
			throw new RuntimeException("target must be a model or object, was:" + target);
		
		this.target = target;
		this.commands = new ArrayList<XAtomicCommand>();
	}
	
	/**
	 * Adds the given {@link XCommand} to the end of this transaction.
	 * 
	 * @param command The command which is to be added
	 * @return true, if adding the command was successful false if not
	 * @throws IllegalArgumentException if the command is not an
	 *             {@link XTransaction }link XFieldCommand},
	 *             {@link XObjectCommand} (or {@link XModelCommand} for model
	 *             transactions) that applies to the model/object of this
	 *             transaction
	 * @throws NullPointerException if command is null
	 */
	public void addCommand(XCommand command) throws IllegalArgumentException, NullPointerException {
		addCommand(size(), command);
	}
	
	/**
	 * Adds the given {@link XCommand} to this transaction at the given index.
	 * If the given {@link XCommand} is an XTransaction, the commands of this
	 * transaction will be added from index to index+size of the transaction.
	 * 
	 * @param command The command which is to be added
	 * @return true, if adding the command was successful false if not
	 * @throws IllegalArgumentException if the command is not an
	 *             {@link XTransaction }link XFieldCommand},
	 *             {@link XObjectCommand} (or {@link XModelCommand} for model
	 *             transactions) that applies to the model/object of this
	 *             transaction
	 * @throws NullPointerException if command is null
	 * @throws IndexOutOfBoundsException if the given index is negative or
	 *             greater than the size of this transaction
	 */
	public void addCommand(int index, XCommand command) throws IllegalArgumentException,
	        NullPointerException, IndexOutOfBoundsException {
		
		if(!XX.equalsOrContains(this.target, command.getTarget())) {
			throw new IllegalArgumentException(command + " is not contained in " + this.target);
		}
		
		if(command instanceof XTransaction) {
			
			int idx = index;
			XTransaction trans = (XTransaction)command;
			for(XAtomicCommand atomicCommand : trans) {
				// commands have already been checked when added to the
				// transaction
				this.commands.add(idx, atomicCommand);
				idx++;
			}
			
			return;
		} else if(command instanceof XModelCommand || command instanceof XObjectCommand
		        || command instanceof XFieldCommand) {
			this.commands.add(index, (XAtomicCommand)command);
		} else
			throw new IllegalArgumentException(
			        "Can only add XModelCommands, XObjectCommands and XFieldCommands to transactions.");
		
	}
	
	/**
	 * Removes the first occurrence of the given command from this transaction.
	 * 
	 * If the given {@link XCommand} which is to removed is an XTransaction,
	 * this method will try to remove all {@link XCommand} that are contained in
	 * the given XTransaction from this XTransaction.
	 * 
	 * This method will fail, if:
	 * 
	 * @param actorID The XID of the actor of this transaction
	 * @param command The command which is to be deleted
	 * @return true if the transaction previously contained the given command
	 */
	public boolean removeCommand(XCommand command) {
		
		if(!(command instanceof XTransaction))
			return this.commands.remove(command);
		
		XTransaction transaction = (XTransaction)command;
		
		if(transaction.size() == 0) {
			return false;
		}
		
		int index = this.commands.indexOf(transaction.getCommand(0));
		
		if(index + transaction.size() > this.size()) {
			return false;
		}
		
		boolean removeable = true;
		for(int j = 0; j < transaction.size() && removeable; j++) {
			if(!transaction.getCommand(j).equals(this.commands.get(index + j))) {
				removeable = false;
			}
		}
		
		if(!removeable)
			return false;
		
		for(; index < transaction.size(); index++) {
			this.commands.remove(index);
		}
		
		return true;
		
	}
	
	/**
	 * Removes the command at the given index.
	 * 
	 * @param actorID The XID of the actor of this transaction
	 * @param index The index of the command which is to be removed
	 * @return the element previously at the given index or null if the method
	 *         fails
	 * @throws IndexOutOfBoundsException if the given index is negative or
	 *             greater than or equal to the size of this transaction
	 */
	public XAtomicCommand removeCommand(int index) throws IndexOutOfBoundsException {
		return this.commands.remove(index);
	}
	
	/**
	 * Returns the command at the given index.
	 * 
	 * @param index The command at the given index.
	 * @return The command at the given index.
	 */
	public XAtomicCommand getCommand(int index) {
		return this.commands.get(index);
	}
	
	/**
	 * Returns the number of commands in this builder.
	 * 
	 * @return the number of commands in this builder.
	 */
	public int size() {
		return this.commands.size();
	}
	
	/**
	 * @return true if there are no commands in this builder.
	 */
	public boolean isEmpty() {
		return this.commands.isEmpty();
	}
	
	public Iterator<XAtomicCommand> iterator() {
		return this.commands.iterator();
	}
	
	/**
	 * @return a transaction with the current contents of this builder.
	 */
	public XTransaction build() {
		return MemoryTransaction.createTransaction(this.target, this.commands);
	}
	
	/**
	 * If there is only one command in this transaction, return that, otherwise
	 * build a transaction.
	 */
	public XCommand buildCommand() {
		if(size() == 1) {
			return getCommand(0);
		}
		return build();
	}
	
	/**
	 * Add commands that change the state of one existing {@link XModel} into
	 * that of another.
	 * 
	 * The revision number of the created commands will be set so that the
	 * transaction applies to the old model in it's current state. Revision
	 * numbers in the new model are ignored.
	 * 
	 * @param oldModel The old model that is to be changed.
	 * @param newModel The new model that the old model will be changed to.
	 * 
	 * @throws IllegalArgumentException if this builder's target is not oldModel
	 */
	public void changeModel(XBaseModel oldModel, XBaseModel newModel)
	        throws IllegalArgumentException {
		for(XID oldObjectId : oldModel) {
			if(!newModel.hasObject(oldObjectId))
				removeObject(oldModel.getObject(oldObjectId));
		}
		for(XID newObjectId : newModel)
			setObject(oldModel, newModel.getObject(newObjectId));
	}
	
	/**
	 * Add commands that create or change an {@link XObject} to the state of
	 * another.
	 * 
	 * The revision number of the created commands will be set so that the
	 * transaction applies to the old model in it's current state. Revision
	 * numbers in the new object are ignored.
	 * 
	 * The repository and model IDs are taken from the old model.
	 * 
	 * @param oldModel The old model that is to be changed.
	 * @param newObject The object that is being added to or changed in
	 *            oldModel.
	 * 
	 * @throws IllegalArgumentException if this builder's target is not the
	 *             oldModel or, if the object already exists, the object being
	 *             set.
	 */
	public void setObject(XBaseModel oldModel, XBaseObject newObject)
	        throws IllegalArgumentException {
		XBaseObject oldObject = oldModel.getObject(newObject.getID());
		if(oldObject == null)
			addObject(oldModel.getAddress(), newObject);
		else
			changeObject(oldObject, newObject);
	}
	
	/**
	 * Add commands that create or change an {@link XField} to the state of
	 * another.
	 * 
	 * The revision number of the created commands will be set so that the
	 * transaction applies to the old object in it's current state. Revision
	 * numbers in the new field are ignored.
	 * 
	 * The repository, model and object IDs are taken from the old object.
	 * 
	 * @param oldObject The old object that is to be changed.
	 * @param newField The object that is being added to or changed in oldModel.
	 * 
	 * @throws IllegalArgumentException if this builder's target is not the
	 *             oldObject or it's containing model.
	 */
	public void setField(XBaseObject oldObject, XBaseField newField)
	        throws IllegalArgumentException {
		XBaseField oldField = oldObject.getField(newField.getID());
		if(oldField == null)
			addField(oldObject.getAddress(), newField);
		else
			changeField(oldField, newField);
	}
	
	/**
	 * Add a command that removes the given object.
	 * 
	 * The revision number of the created command will be set so that the
	 * transaction applies to the old object in it's current state.
	 * 
	 * @param oldObject The old object that is to be removed.
	 * 
	 * @throws IllegalArgumentException if this builder's target is not the
	 *             oldObject or it's containing model.
	 */
	public void removeObject(XBaseObject oldObject) {
		removeObject(oldObject.getAddress().getParent(), oldObject.getRevisionNumber(), oldObject
		        .getID());
	}
	
	/**
	 * Add commands that add the given object to the model with the given
	 * address.
	 * 
	 * @param newObject The new object that is to be added.
	 * 
	 * @throws IllegalArgumentException if this builder's target is not the
	 *             modelAddr.
	 */
	public void addObject(XAddress modelAddr, XBaseObject newObject) {
		XAddress objectAddr = XX.resolveObject(modelAddr, newObject.getID());
		addObject(modelAddr, XCommand.SAFE, newObject.getID());
		for(XID newFieldId : newObject) {
			addField(objectAddr, newObject.getField(newFieldId));
		}
	}
	
	/**
	 * Add commands that change an {@link XObject} to the state of another.
	 * 
	 * The revision number of the created commands will be set so that the
	 * transaction applies to the old object in it's current state. Revision
	 * numbers in the new object are ignored.
	 * 
	 * The repository and model IDs are taken from the old object.
	 * 
	 * @param oldObject The old object that is to be changed.
	 * @param newObject The state the object is being changed to.
	 * 
	 * @throws IllegalArgumentException if this builder's target is not the
	 *             oldObject or it's containing model.
	 */
	public void changeObject(XBaseObject oldObject, XBaseObject newObject) {
		for(XID oldFieldId : oldObject) {
			if(!newObject.hasField(oldFieldId))
				removeField(oldObject.getField(oldFieldId));
		}
		for(XID newFieldId : newObject)
			setField(oldObject, newObject.getField(newFieldId));
	}
	
	/**
	 * Add a command that removes the given field.
	 * 
	 * The revision number of the created command will be set so that the
	 * transaction applies to the old field in it's current state.
	 * 
	 * @param oldField The old field that is to be removed.
	 * 
	 * @throws IllegalArgumentException if this builder's target does not
	 *             contain oldField.
	 */
	public void removeField(XBaseField oldField) {
		removeField(oldField.getAddress().getParent(), oldField.getRevisionNumber(), oldField
		        .getID());
	}
	
	/**
	 * Add commands that add the given field to the object with the given
	 * address.
	 * 
	 * @param newField The new object that is to be added.
	 * 
	 * @throws IllegalArgumentException if this builder's target is not the
	 *             objectAddr or it's parent.
	 */
	public void addField(XAddress objectAddr, XBaseField newField) {
		addField(objectAddr, XCommand.SAFE, newField.getID());
		if(newField.getValue() != null) {
			XAddress fieldAddr = XX.resolveField(objectAddr, newField.getID());
			addValue(fieldAddr, XCommand.NEW, newField.getValue());
		}
	}
	
	/**
	 * Add commands that change an {@link XField} to the state of another.
	 * 
	 * The revision number of the created commands will be set so that the
	 * transaction applies to the old field in it's current state. Revision
	 * numbers in the new field are ignored.
	 * 
	 * The repository, model and object IDs are taken from the old field.
	 * 
	 * @param oldField The old field that is to be changed.
	 * @param newField The state the field is being changed to.
	 * 
	 * @throws IllegalArgumentException if this builder's target does not
	 *             contain the oldField.
	 */
	public void changeField(XBaseField oldField, XBaseField newField) {
		XAddress target = oldField.getAddress();
		long revision = oldField.getRevisionNumber();
		changeValue(target, revision, oldField.getValue(), newField.getValue());
	}
	
	/**
	 * Add commands that change a field containing a value into one containing
	 * another value.
	 * 
	 * @param fieldAddr Address of the changed field.
	 * @param revision Old revision of the field.
	 */
	public void changeValue(XAddress fieldAddr, long revision, XValue oldValue, XValue newValue) {
		if(oldValue == null) {
			if(newValue != null)
				addValue(fieldAddr, revision, newValue);
		} else if(!oldValue.equals(newValue)) {
			if(newValue == null)
				removeValue(fieldAddr, revision);
			else
				changeValue(fieldAddr, revision, newValue);
		}
	}
	
	/**
	 * Add a command that adds the given object or field.
	 * 
	 * @param address The address of the entity being added.
	 * @param revision {@link XCommand#SAFE} or {@link XCommand#FORCED}
	 * 
	 * @throws IllegalArgumentException if this builder's target doesn't contain
	 *             the given address.
	 */
	public void addEntity(XAddress address, long revision) {
		if(address.getField() == null)
			addObject(address.getParent(), revision, address.getObject());
		else
			addField(address.getParent(), revision, address.getField());
	}
	
	/**
	 * Add a command that adds the given object.
	 * 
	 * @param modelAddr The address of the model to which the object is being
	 *            added.
	 * @param revision {@link XCommand#SAFE} or {@link XCommand#FORCED}
	 * @param objectId The id of the object being added
	 * 
	 * @throws IllegalArgumentException if this builder's target doesn't contain
	 *             the given address.
	 */
	public void addObject(XAddress modelAddr, long revision, XID objectId) {
		addCommand(MemoryModelCommand.createAddCommand(modelAddr, revision, objectId));
	}
	
	/**
	 * Add a command that adds the given field.
	 * 
	 * @param objectAddr The address of the object to which the field is being
	 *            added.
	 * @param revision {@link XCommand#SAFE} or {@link XCommand#FORCED}
	 * @param fieldId The id of the field being added
	 * 
	 * @throws IllegalArgumentException if this builder's target doesn't contain
	 *             the given address.
	 */
	public void addField(XAddress objectAddr, long revision, XID fieldId) {
		addCommand(MemoryObjectCommand.createAddCommand(objectAddr, revision, fieldId));
	}
	
	/**
	 * Add a command that removes the given object or field.
	 * 
	 * @param address The address of the entity being removed.
	 * @param revision The old revision of the entity or {@link XCommand#FORCED}
	 *            .
	 * 
	 * @throws IllegalArgumentException if this builder's target doesn't contain
	 *             the given address.
	 */
	public void removeEntity(XAddress address, long revision) {
		if(address.getField() == null)
			addObject(address.getParent(), revision, address.getObject());
		else
			addField(address.getParent(), revision, address.getField());
	}
	
	/**
	 * Add a command that removes the given object.
	 * 
	 * @param modelAddr The address of model the object is being removed from.
	 * @param revision The old revision of the object or {@link XCommand#FORCED}
	 * @param objectId The ID of the object being removed.
	 * 
	 * @throws IllegalArgumentException if this builder's target doesn't contain
	 *             the given object.
	 */
	public void removeObject(XAddress modelAddr, long revision, XID objectId) {
		addCommand(MemoryModelCommand.createRemoveCommand(modelAddr, revision, objectId));
	}
	
	/**
	 * Add a command that removes the given field.
	 * 
	 * @param objectAddr The address of object the field is being removed from.
	 * @param revision The old revision of the field or {@link XCommand#FORCED}
	 * @param fieldId The ID of the field being removed.
	 * 
	 * @throws IllegalArgumentException if this builder's target doesn't contain
	 *             the given field.
	 */
	public void removeField(XAddress objectAddr, long revision, XID fieldId) {
		addCommand(MemoryObjectCommand.createRemoveCommand(objectAddr, revision, fieldId));
	}
	
	/**
	 * Add a command that adds the given value to an existing field that doesn't
	 * have a value yet.
	 * 
	 * @param fieldAddr The address of the field the value is being added to.
	 * @param revision The old revision of the field or {@link XCommand#FORCED}.
	 * @param value The value being added.
	 * 
	 * @throws IllegalArgumentException if this builder's target doesn't contain
	 *             the given fieldAddr.
	 */
	public void addValue(XAddress fieldAddr, long revision, XValue value) {
		addCommand(MemoryFieldCommand.createAddCommand(fieldAddr, revision, value));
	}
	
	/**
	 * Add a command that changes the given value of an existing field that
	 * already has a value.
	 * 
	 * @param fieldAddr The address of the field whose value is being changed.
	 * @param revision The old revision of the field or {@link XCommand#FORCED}.
	 * @param value The new value.
	 * 
	 * @throws IllegalArgumentException if this builder's target doesn't contain
	 *             the given fieldAddr.
	 */
	public void changeValue(XAddress fieldAddr, long revision, XValue value) {
		addCommand(MemoryFieldCommand.createChangeCommand(fieldAddr, revision, value));
	}
	
	/**
	 * Add a command that removes the given field's value.
	 * 
	 * @param fieldAddr The address of the field whose value is being changed.
	 * @param revision The old revision of the field or {@link XCommand#FORCED}.
	 * 
	 * @throws IllegalArgumentException if this builder's target doesn't contain
	 *             the given fieldAddr.
	 */
	public void removeValue(XAddress fieldAddr, long revision) {
		addCommand(MemoryFieldCommand.createRemoveCommand(fieldAddr, revision));
	}
	
	/**
	 * Add commands that apply the changes represented by the given changed
	 * model.
	 */
	public void applyChanges(ChangedModel model) {
		
		for(XID objectId : model.getRemovedObjects()) {
			XBaseObject object = model.getOldObject(objectId);
			removeObject(object);
		}
		
		for(NewObject object : model.getNewObjects()) {
			addObject(model.getAddress(), object);
		}
		
		for(ChangedObject object : model.getChangedObjects()) {
			applyChanges(object);
		}
		
	}
	
	/**
	 * Add commands that apply the changes represented by the given changed
	 * object.
	 */
	public void applyChanges(ChangedObject object) {
		
		for(XID fieldId : object.getRemovedFields()) {
			XBaseField field = object.getOldField(fieldId);
			removeField(field);
		}
		
		for(NewField field : object.getNewFields()) {
			addField(object.getAddress(), field);
		}
		
		for(ChangedField field : object.getChangedFields()) {
			applyChanges(field);
		}
	}
	
	/**
	 * Add commands that apply the changes represented by the given changed
	 * field.
	 */
	public void applyChanges(ChangedField field) {
		XAddress target = field.getAddress();
		long revision = field.getRevisionNumber();
		changeValue(target, revision, field.getOldValue(), field.getValue());
	}
	
}
