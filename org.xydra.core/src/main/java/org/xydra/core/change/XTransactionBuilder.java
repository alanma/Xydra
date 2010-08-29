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
import org.xydra.core.model.XRepository;
import org.xydra.core.model.delta.ChangedField;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.ChangedObject;
import org.xydra.core.model.delta.NewField;
import org.xydra.core.model.delta.NewObject;
import org.xydra.core.value.XValue;


/**
 * An XTransactionBuilder provides methods for building {@link XTransaction
 * XTransactions} that change {@link XModel XModels}/{@link XObject XObjects}/
 * {@link XField XFields} according to the given {@link XCommands}.
 * 
 * To build an {@link XTransaction} using an XTransactionBuilder, simply use the
 * constructor to create an XTransactionBuider referring to the {@link XModel}/
 * {@link XObject} you want to change, use the provided method to manipulate the
 * {@link XTransaction} you want to create and get it using the
 * {@link XTransaction#build} method. It is possible to have multiple
 * XTransactionBuilders referring to the same entity at the same time.
 * 
 * Every XTransactionBuilder should only be used to create one
 * {@link XTransaction} to avoid confusions. It is possible to create multiple
 * {@link XTransactions} with only one XTransactionBuilder, but we strongly
 * encourage you to avoid this and look at each XTransactionBuilder instance as
 * an instance to create one specific {@link XTransaction}.
 * 
 * @author dscharrer
 * 
 */
public class XTransactionBuilder implements Iterable<XAtomicCommand> {
	
	private final XAddress target;
	private final List<XAtomicCommand> commands;
	
	/**
	 * Create a new transaction builder for constructing an {@link XTransaction}
	 * that will operate on the given target.
	 * 
	 * @param target The address of an {@link XModel} or an {@link XObject} to
	 *            which all the commands in the {@link XTransaction} will apply.
	 * @throws RuntimeException if the given {@link XAddress} is not an
	 *             {@link XAddres} of an {@link XModel} or {@link XObject}
	 */
	public XTransactionBuilder(XAddress target) {
		
		if((target.getModel() == null && target.getObject() == null) || target.getField() != null)
			throw new RuntimeException("target must be a model or object, was:" + target);
		
		this.target = target;
		this.commands = new ArrayList<XAtomicCommand>();
	}
	
	/**
	 * Adds the given {@link XCommand} to the end of the {@link XTransaction}
	 * which is being built by this transaction builder.
	 * 
	 * @param command The {@link XCommand} which is to be added
	 * @return true, if adding the {@link XCommand} was successful, false if not
	 * @throws IllegalArgumentException if the command is not an
	 *             {@link XTransaction}, {@link XFieldCommand},
	 *             {@link XObjectCommand} (or {@link XModelCommand} for model
	 *             transactions) that applies to the {@link XModel}/
	 *             {@link XObject} the {@link XTransaction} which is being built
	 *             will apply to
	 * @throws NullPointerException if the given {@link XCommand} equals null
	 */
	public void addCommand(XCommand command) throws IllegalArgumentException, NullPointerException {
		addCommand(size(), command);
	}
	
	/**
	 * Adds the given {@link XCommand} to this {@link XTransaction} at the given
	 * index. If the given {@link XCommand} is an {@link XTransaction}, the
	 * commands of this {@link XTransaction} will be added from index to
	 * index+size of the transaction which is being built by this transaction
	 * builder.
	 * 
	 * @param command The {@link XCommand} which is to be added
	 * @return true, if adding the {@link XCommand} was successful, false if not
	 * @throws IllegalArgumentException if the command is not an
	 *             {@link XTransaction}, {@link XFieldCommand},
	 *             {@link XObjectCommand} (or {@link XModelCommand} for model
	 *             transactions) that applies to the {@link XModel}/
	 *             {@link XObject} the {@link XTransaction} which is being built
	 *             will apply to
	 * @throws NullPointerException if the given {@link XCommand} equals null
	 * @throws IndexOutOfBoundsException if the given index is negative or
	 *             greater than the current size of the {@link XTransaction}
	 *             which is being built
	 */
	public void addCommand(int index, XCommand command) throws IllegalArgumentException,
	        NullPointerException, IndexOutOfBoundsException {
		
		if(!this.target.equalsOrContains(command.getTarget())) {
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
	 * Removes the first occurrence of the given {@link XCommand} from the
	 * {@link XTransaction} which is being built by this transaction builder.
	 * 
	 * If the given {@link XCommand} which is to removed is an
	 * {@link XTransaction}, this method will try to remove all {@link XCommand}
	 * that are contained in the given {@link XTransaction} from the
	 * {@link XTransaction} which is being built by this transaction builder.
	 * 
	 * This method will fail, if:
	 * 
	 * @param actorID The {@link XID} of the actor of this {@link XTransaction}
	 * @param command The {@link XCommand} which is to be deleted
	 * @return true if the {@link XTransaction} previously contained the given
	 *         {@link XCommand}
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
	 * Removes the {@link XCommand} at the given index from the
	 * {@link XTransaction} which is built by this transaction builder.
	 * 
	 * @param actorID The {@link XID} of the actor of this {@link XTransaction}
	 * @param index The index of the {@link XCommand} which is to be removed
	 * @return the element previously at the given index or null if the method
	 *         fails
	 * @throws IndexOutOfBoundsException if the given index is negative or
	 *             greater than or equal to the size of this
	 *             {@link XTransaction}
	 */
	public XAtomicCommand removeCommand(int index) throws IndexOutOfBoundsException {
		return this.commands.remove(index);
	}
	
	/**
	 * Returns the {@link XCommand} at the given index of the
	 * {@link XTransaction} which is being built by this transaction builder.
	 * 
	 * @param index The {@link XCommand} at the given index.
	 * @return The {@link XCommand} at the given index.
	 */
	public XAtomicCommand getCommand(int index) {
		return this.commands.get(index);
	}
	
	/**
	 * @return the current number of {@link XCommands} of the
	 *         {@link XTransaction} which is being built by this transaction
	 *         builder.
	 */
	public int size() {
		return this.commands.size();
	}
	
	/**
	 * @return true, if there currently are no {@link XCommand XCommands} in the
	 *         {@link XTransaction} which is being built by this builder.
	 */
	public boolean isEmpty() {
		return this.commands.isEmpty();
	}
	
	/**
	 * @return an iterator over the {@link XCommands} which are currently part
	 *         of the {@link XTransaction} which is being built by this
	 *         transaction builder.
	 */
	public Iterator<XAtomicCommand> iterator() {
		return this.commands.iterator();
	}
	
	/**
	 * @return an {@link XTransaction} with the current contents of this
	 *         builder.
	 */
	public XTransaction build() {
		return MemoryTransaction.createTransaction(this.target, this.commands);
	}
	
	/**
	 * If there is only one {@link XCommand} in the {@link XTransaction} which
	 * is being built, return that, otherwise return the built
	 * {@link XTransaction}.
	 */
	public XCommand buildCommand() {
		if(size() == 1) {
			return getCommand(0);
		}
		return build();
	}
	
	/**
	 * Adds {@link XCommand XCommands} to the transaction which is being built,
	 * that will change the state of one existing {@link XModel} into that of
	 * another.
	 * 
	 * The revision number of the created {@link XCommands} will be set so that
	 * the transaction will apply to the old {@link XModel} in the state it is
	 * in while this method is being executed. Revision numbers in the new
	 * {@link XModel} are ignored.
	 * 
	 * @param oldModel The old {@link XModel} that is to be changed.
	 * @param newModel The new {@link XModel} that the old {@link XModel} will
	 *            be changed to.
	 * 
	 * @throws IllegalArgumentException if this builders target is not oldModel
	 */
	/*
	 * FIXME All of the change... Methods here have the same problem: It is
	 * possible that someone will change the state of the given
	 * model/object/etc. while the method is being executed, which may result in
	 * incoherent transformations. We'll probably need a way to lock the
	 * entities while these methods are being executed.
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
	 * Adds {@link XCommand XCommands} to the transaction which is being built,
	 * which will create or change an {@link XObject} to the state of another.
	 * 
	 * The revision number of the created {@link XCommand XCommands} will be set
	 * so that the transaction will apply to the old {@link XModel} in the state
	 * it is in while this method is being executed. Revision numbers in the new
	 * {@link XObject} are ignored.
	 * 
	 * The {@link XRepository} and {@link XModel} {@link XID XIDs} the created
	 * {@link XCommand XCommands} will refer to will be taken from the old
	 * {@link XModel}.
	 * 
	 * @param oldModel The old {@link XModel} that is to be changed.
	 * @param newObject The {@link XObject} that is being added to or changed in
	 *            oldModel.
	 * 
	 * @throws IllegalArgumentException if this builders target is not the
	 *             oldModel
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
	 * Adds {@link XCommand XCommands} to the transaction which is being built
	 * which will create or change an {@link XField} to the state of another.
	 * 
	 * The revision number of the created {@link XCommand XCommands} will be set
	 * so that the transaction will apply to the old {@link XObject} in the
	 * state it is in while this method is being executed. Revision numbers in
	 * the new {@link XField} are ignored.
	 * 
	 * The {@link XRepository}, {@link XModel} and {@link XObject} {@link XID
	 * XIDs} the created {@link XCommand XCommands} will refer to will be taken
	 * from the old {@link XObject}.
	 * 
	 * @param oldObject The old {@link XObject} that is to be changed.
	 * @param newField The {@link XField} that is being added to or changed in
	 *            oldObject.
	 * 
	 * @throws IllegalArgumentException if this builder's target is not the
	 *             oldObject or its parent {@link XModel}.
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
	 * Adds an {@link XCommand} to the transaction which is being built which
	 * will remove the given object.
	 * 
	 * The revision number of the created {@link XCommand} will be set so that
	 * the transaction will apply to the old {@link XObject} in the state it is
	 * in while this method is being executed.
	 * 
	 * @param oldObject The old {@link XObject} that is to be removed.
	 * 
	 * @throws IllegalArgumentException if this builders target is not the
	 *             oldObject or its parent {@link XModel}.
	 */
	public void removeObject(XBaseObject oldObject) {
		removeObject(oldObject.getAddress().getParent(), oldObject.getRevisionNumber(), oldObject
		        .getID());
	}
	
	/**
	 * Adds {@link XCommand XCommands} to the transaction which is being built
	 * which will add the given {@link XObject} to the {@link XModel} with the
	 * given {@link XAddress}.
	 * 
	 * @param modelAddr The {@link XAddress} of the {@link XModel} to which the
	 *            given {@link XObject} is to be added by the {@link XCommand
	 *            XCommands} that will be created
	 * @param newObject The new {@link XObject} that is to be added.
	 * 
	 * @throws IllegalArgumentException if this builders target is not the
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
	 * Adds {@link XCommand XCommands} to the transaction which is being built
	 * which will change an {@link XObject} to the state of another.
	 * 
	 * The revision number of the created {@link XCommand XCommands} will be set
	 * so that the transaction will apply to the old {@link XObject} in the
	 * state it is in while this method is being executed. Revision numbers in
	 * the new {@link XObject} are ignored.
	 * 
	 * The {@link XRepository} and {@link XModel} {@link XID XIDs} the created
	 * {@link XCommand XCommands} will be taken from the old {@link XObject}.
	 * 
	 * @param oldObject The old {@link XObject} that is to be changed.
	 * @param newObject The {@link XObject} to which state the oldObject will
	 *            being changed to by the created {@link XCommands}.
	 * 
	 * @throws IllegalArgumentException if this builders target is not the
	 *             oldObject or its parent {@link XModel}.
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
	 * Adds an {@link XCommand} to the transaction which is being built which
	 * will remove the given {@link XField}.
	 * 
	 * The revision number of the created {@link XCommand} will be set so that
	 * the transaction will apply to the old {@link XField} in the state it is
	 * in while this method is being executed.
	 * 
	 * @param oldField The old {@link XField} that is to be removed.
	 * 
	 * @throws IllegalArgumentException if this builders target does not contain
	 *             oldField.
	 */
	public void removeField(XBaseField oldField) {
		removeField(oldField.getAddress().getParent(), oldField.getRevisionNumber(), oldField
		        .getID());
	}
	
	/**
	 * Adds {@link XCommand XCommands} to the transaction which is being built
	 * which will add the given {@link XField} to the {@link XObject} with the
	 * given {@link XAddress}.
	 * 
	 * @param newField The new {@link XField} that is to be added.
	 * 
	 * @throws IllegalArgumentException if this builders target is not the
	 *             objectAddr or the {@link XAddress} of its parent
	 *             {@link XModel}.
	 */
	public void addField(XAddress objectAddr, XBaseField newField) {
		addField(objectAddr, XCommand.SAFE, newField.getID());
		if(newField.getValue() != null) {
			XAddress fieldAddr = XX.resolveField(objectAddr, newField.getID());
			addValue(fieldAddr, XCommand.NEW, newField.getValue());
		}
	}
	
	/**
	 * Adds {@link XCommand XCommands} to the transaction which is being built
	 * which will change the given {@link XField} to the state of another.
	 * 
	 * The revision number of the created {@link XCommand XCommands} will be set
	 * so that the transaction will apply to the old {@link XField} in the state
	 * it is in while this method is being executed. Revision numbers in the new
	 * {@link XField} are ignored.
	 * 
	 * The {@link XRepository}, {@link XModel} and {@link XObject} {@link XID
	 * XIDs} the created {@link XCommand XCommands} will refer to will be taken
	 * from the old {@link XField}.
	 * 
	 * @param oldField The old {@link XField} that is to be changed.
	 * @param newField The {@link XField} to which state oldField will be
	 *            changed to.
	 * 
	 * @throws IllegalArgumentException if this builders target does not contain
	 *             the oldField.
	 */
	public void changeField(XBaseField oldField, XBaseField newField) {
		XAddress target = oldField.getAddress();
		long revision = oldField.getRevisionNumber();
		changeValue(target, revision, oldField.getValue(), newField.getValue());
	}
	
	/**
	 * Adds {@link XCommand XCommands} to the transaction which is being built
	 * which will change an {@link XField} containing an {@link XValue} into one
	 * containing another {@link XValue}.
	 * 
	 * @param fieldAddr Address of the {@link XField} which is to be changed.
	 * @param revision Current revision number of the {@link XField}.
	 * @param oldValue The old {@link XValue} that the {@link XField} contains
	 *            before the change.
	 * @param newValue The new {@link XValue} the {@link XField} will contain
	 *            after the change.
	 */
	public void changeValue(XAddress fieldAddr, long revision, XValue oldValue, XValue newValue) {
		if(oldValue == null) {
			if(newValue != null)
				addValue(fieldAddr, revision, newValue);
		} else if(!oldValue.equals(newValue)) {
			if(newValue == null) {
				removeValue(fieldAddr, revision);
			} else {
				changeValue(fieldAddr, revision, newValue);
			}
		}
	}
	
	/**
	 * Adds an {@link XCommand} to the transaction which is being built which
	 * will add an {@link XObject} or {@link XField} according to the set
	 * {@link XID XIDs} in the given {@link XAddress}. If only the object
	 * {@link XID} is set the created {@link XCommand} will add an
	 * {@link XObject}, if the field {@link XID} is set an {@link XField} will
	 * be added. If neither are set, no {@link XCommand} will be created.
	 * 
	 * @param address The {@link XAddress} defining which kind of entity will be
	 *            added (this address will also be the {@link XAddress} of the
	 *            newly created entity after the execution of the
	 *            transaction/command).
	 * @param revision {@link XCommand#SAFE} or {@link XCommand#FORCED}, to
	 *            define whether the {@link XCommand} which is to be created
	 *            shall be a forced or a safe event.
	 * 
	 * @throws IllegalArgumentException if this builders target doesn't contain
	 *             the given {@link XAddress} or if the given revision number is
	 *             neither {@link XCommand#SAFE} or {@link XCommand#FORCED}.
	 */
	public void addEntity(XAddress address, long revision) {
		if(revision != XCommand.SAFE && revision != XCommand.FORCED) {
			throw new IllegalArgumentException(
			        "given revision number needs to be XCommand.SAFE or XCommand.FORCED, was "
			                + revision);
		}
		
		if(address.getField() == null) {
			addObject(address.getParent(), revision, address.getObject());
		} else {
			addField(address.getParent(), revision, address.getField());
		}
	}
	
	/**
	 * Adds an {@link XCommand} to the transaction which is being built which
	 * will will add an {@link XObject} with the given {@link XID}.
	 * 
	 * @param modelAddr The {@link XAddress} of the {@link XModel} to which the
	 *            {@link XObject} is to be added.
	 * @param revision {@link XCommand#SAFE} or {@link XCommand#FORCED}, to
	 *            define whether the {@link XCommand} which is to be created
	 *            shall be a forced or a safe event
	 * @param objectId The {@link XID} for the {@link XObject} which will be
	 *            added
	 * 
	 * @throws IllegalArgumentException if this builders target doesn't contain
	 *             the given {@link XAddress} or if the given revision number is
	 *             neither {@link XCommand#SAFE} or {@link XCommand#FORCED}.
	 */
	public void addObject(XAddress modelAddr, long revision, XID objectId) {
		if(revision != XCommand.SAFE && revision != XCommand.FORCED) {
			throw new IllegalArgumentException(
			        "given revision number needs to be XCommand.SAFE or XCommand.FORCED, was "
			                + revision);
		}
		
		addCommand(MemoryModelCommand.createAddCommand(modelAddr, revision, objectId));
	}
	
	/**
	 * Adds an {@link XCommand} to the transaction which is being built which
	 * will add and {@link XField} with the given {@link XID}.
	 * 
	 * @param objectAddr The {@link XAddress} of the {@link XObject} to which
	 *            the {@link XField} is to be added.
	 * @param revision {@link XCommand#SAFE} or {@link XCommand#FORCED}, to
	 *            define whether the {@link XCommand} which is to be created
	 *            shall be a forced or a safe event
	 * @param fieldId The {@link XID} of the {@link XField} which will be added
	 * 
	 * @throws IllegalArgumentException if this builders target doesn't contain
	 *             the given {@link XAddress} or if the given revision number is
	 *             neither {@link XCommand#SAFE} or {@link XCommand#FORCED}.
	 */
	public void addField(XAddress objectAddr, long revision, XID fieldId) {
		if(revision != XCommand.SAFE && revision != XCommand.FORCED) {
			throw new IllegalArgumentException(
			        "given revision number needs to be XCommand.SAFE or XCommand.FORCED, was "
			                + revision);
		}
		
		addCommand(MemoryObjectCommand.createAddCommand(objectAddr, revision, fieldId));
	}
	
	/**
	 * Adds an {@link XCommand} to the transaction which is being built which
	 * will remove the {@link XObject} or {@link XField} specified by the given
	 * {@link XAddress}.
	 * 
	 * @param address The {@link XAddress} of the entity which is to be removed.
	 * @param revision The old revision number of the entity or
	 *            {@link XCommand#FORCED} .
	 * 
	 * @throws IllegalArgumentException if this builders target doesn't contain
	 *             the entity with the given {@link XAddress}.
	 */
	public void removeEntity(XAddress address, long revision) {
		if(revision != XCommand.SAFE && revision != XCommand.FORCED) {
			throw new IllegalArgumentException(
			        "given revision number needs to be XCommand.SAFE or XCommand.FORCED, was "
			                + revision);
		}
		if(address.getField() == null) {
			addObject(address.getParent(), revision, address.getObject());
		} else {
			addField(address.getParent(), revision, address.getField());
		}
	}
	
	/**
	 * Adds an {@link XCommand} to the transaction which is being built which
	 * will remove the {@link XObject} specified by the given {@link XAddress}
	 * and {@link XID}.
	 * 
	 * @param modelAddr The {@link XAddress} of the {@link XModel} which is to
	 *            supposed to hold the {@link XObject} which is to be removed
	 * @param revision The old revision number of the {@link XObject} or
	 *            {@link XCommand#FORCED}
	 * @param objectId The {@link XID} of the {@link XObject} which will be
	 *            removed.
	 * 
	 * @throws IllegalArgumentException if this builders target doesn't contain
	 *             the given {@link XObject}.
	 */
	public void removeObject(XAddress modelAddr, long revision, XID objectId) {
		addCommand(MemoryModelCommand.createRemoveCommand(modelAddr, revision, objectId));
	}
	
	/**
	 * Adds an {@link XCommand} to the transaction which is being built which
	 * will remove the {@link XField} specified by the given {@link XAddress}
	 * and {@link XID}.
	 * 
	 * @param objectAddr The {@link XAddress} of the {@link XObject} which is
	 *            supposed to hold the {@link XField} which is be removed.
	 * @param revision The old revision number of the {@link XField} or
	 *            {@link XCommand#FORCED}
	 * @param fieldId The {@link XID} of the {@link XField} being removed.
	 * 
	 * @throws IllegalArgumentException if this builders target doesn't contain
	 *             the given {@link XField}.
	 */
	public void removeField(XAddress objectAddr, long revision, XID fieldId) {
		addCommand(MemoryObjectCommand.createRemoveCommand(objectAddr, revision, fieldId));
	}
	
	/**
	 * Adds an {@link XCommand} to the transaction which is being built which
	 * will add the given {@link XValue} to an existing {@link XField} specified
	 * by the given {@link XAddress} which {@link XValue} is not yet set.
	 * 
	 * @param fieldAddr The {@link XAddress} of the {@link XField} the
	 *            {@link XValue} is to be added to.
	 * @param revision The old revision number of the {@link XField} or
	 *            {@link XCommand#FORCED}.
	 * @param value The {@link XValue} which will be added.
	 * 
	 * @throws IllegalArgumentException if this builders target doesn't contain
	 *             the {@link XField} specified by the given {@link XAddress}.
	 */
	public void addValue(XAddress fieldAddr, long revision, XValue value) {
		addCommand(MemoryFieldCommand.createAddCommand(fieldAddr, revision, value));
	}
	
	/**
	 * Adds an {@link XCommand} to the transaction which is being built which
	 * will change the {@link XValue} of the specified {@link XField} (which
	 * {@link XValue} needs to be set!) to the given {@link XValue})
	 * 
	 * @param fieldAddr The {@link XAddress} of the {@link XField} which
	 *            {@link XValue} is to be changed.
	 * @param revision The old revision number of the {@link XField} or
	 *            {@link XCommand#FORCED}.
	 * @param value The new {@link XValue} to which the current {@link XValue}
	 *            of the specified {@link XField} is to be changed to.
	 * 
	 * @throws IllegalArgumentException if this builders target doesn't contain
	 *             the given {@link XField} specified by the given
	 *             {@link XAddress}.
	 */
	public void changeValue(XAddress fieldAddr, long revision, XValue value) {
		addCommand(MemoryFieldCommand.createChangeCommand(fieldAddr, revision, value));
	}
	
	/**
	 * Adds an {@link XCommand} to the transaction which is being built which
	 * will remove the {@link XValue} of the {@link XField} specified by the
	 * given {@link XAddress}.
	 * 
	 * @param fieldAddr The {@link XAddress} of the {@link XField} which
	 *            {@link XValue} is to be removed.
	 * @param revision The old revision number of the {@link XField} or
	 *            {@link XCommand#FORCED}.
	 * 
	 * @throws IllegalArgumentException if this builders target doesn't contain
	 *             the {@link XField} specified by the given {@link XAddress}.
	 */
	public void removeValue(XAddress fieldAddr, long revision) {
		addCommand(MemoryFieldCommand.createRemoveCommand(fieldAddr, revision));
	}
	
	/**
	 * Adds {@link XCommand XCommands} to the transaction which is being built
	 * which will apply the changes represented by the given
	 * {@link ChangedModel}
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
	 * Adds {@link XCommand XCommands} to the transaction which is being built
	 * which will apply the changes represented by the given
	 * {@link ChangedObject}.
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
	 * Adds {@link XCommand XCommands} to the transaction which is being built
	 * which will apply the changes represented by the given
	 * {@link ChangedField}.
	 */
	public void applyChanges(ChangedField field) {
		XAddress target = field.getAddress();
		long revision = field.getRevisionNumber();
		changeValue(target, revision, field.getOldValue(), field.getValue());
	}
	
}
