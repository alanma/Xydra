package org.xydra.core.change;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


/**
 * TODO Document
 * 
 * TODO create special XObject- and XField-types that pass all changes that are
 * done on them to the TransactionModel and return these when a user tries to
 * get them by using the TransactionModel.
 * 
 * TODO check if this implementation is thread-safe "enough"
 * 
 * @author Kaidel
 * 
 */
// suppressing warning while this is in flux ~~max
@SuppressWarnings("unused")
public class TransactionModel implements XWritableModel {
	private static final long serialVersionUID = -5636313889791653240L;
	
	private XModel baseModel;
	private long revisionNumber;
	
	private Map<XID,XObject> changedObjects;
	private Map<XAddress,XField> changedFields;
	private Map<XAddress,XValue> changedValues;
	
	private Map<XID,XObject> removedObjects;
	private Map<XAddress,XField> removedFields;
	
	private Map<XID,Long> objectRevisionNumbers;
	private Map<XAddress,Long> fieldRevisionNumbers;
	
	private LinkedList<XCommand> commands;
	
	public TransactionModel(XModel model) {
		this.baseModel = model;
		this.revisionNumber = model.getRevisionNumber();
		
		this.changedObjects = new HashMap<XID,XObject>();
		this.changedFields = new HashMap<XAddress,XField>();
		this.changedValues = new HashMap<XAddress,XValue>();
		this.removedObjects = new HashMap<XID,XObject>();
		this.removedFields = new HashMap<XAddress,XField>();
		this.objectRevisionNumbers = new HashMap<XID,Long>();
		this.fieldRevisionNumbers = new HashMap<XAddress,Long>();
		
		this.commands = new LinkedList<XCommand>();
	}
	
	public XAddress getAddress() {
		return this.baseModel.getAddress();
	}
	
	public XID getID() {
		return this.baseModel.getID();
	}
	
	public long executeCommand(XCommand command) {
		// TODO implement
		return this.baseModel.executeCommand(command);
	}
	
	public long executeCommand(XCommand command, XLocalChangeCallback callback) {
		// TODO Implement!
		// TODO Maybe ignore Transactions?
		/*
		 * Make sure that revision numbers are increased correctly
		 */

		return this.baseModel.executeCommand(command, callback);
	}
	
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	public boolean hasObject(XID objectId) {
		/*
		 * only return true if the object is new or if it wasn't removed and it
		 * part of the baseModel
		 */

		return this.changedObjects.containsKey(objectId)
		        || (this.baseModel.hasObject(objectId) && !this.removedObjects
		                .containsKey(objectId));
	}
	
	public XWritableObject createObject(XID id) {
		XCommand addCommand = X.getCommandFactory().createSafeAddObjectCommand(
		        this.baseModel.getAddress(), id);
		
		executeCommand(addCommand);
		
		return getObject(id);
	}
	
	public boolean isEmpty() {
		return this.baseModel.isEmpty() && this.changedObjects.isEmpty()
		        && this.changedFields.isEmpty();
	}
	
	public long executeModelCommand(XModelCommand command) {
		// TODO use executeCommand
		return this.baseModel.executeModelCommand(command);
	}
	
	public XWritableObject getObject(XID objectId) {
		XObject object = null;
		
		if(this.changedObjects.containsKey(objectId)) {
			object = this.changedObjects.get(objectId);
		} else {
			/*
			 * only look into the base model if the object wasn't removed
			 */
			if(!this.removedObjects.containsKey(objectId)) {
				object = this.baseModel.getObject(objectId);
			}
		}
		
		if(object == null) {
			return null;
		} else {
			return new InModelTransactionObject(object, this);
		}
	}
	
	public boolean removeObject(XID objectId) {
		return this.baseModel.removeObject(objectId);
	}
	
	/*
	 * Special methods needed for the helperclasses InTransactionObject and
	 * InTransactionField
	 */

	protected XWritableField getField(XAddress address) {
		XField field = null;
		
		if(this.changedFields.containsKey(address)) {
			field = this.changedFields.get(address);
		} else {
			/*
			 * only look into the base model if the field wasn't removed
			 */
			if(!this.removedFields.containsKey(address)) {
				XObject object = this.baseModel.getObject(address.getObject());
				
				if(object != null) {
					field = object.getField(address.getField());
				}
			}
		}
		
		if(field == null) {
			return null;
		} else {
			return new InModelTransactionField(field, this);
		}
	}
	
	protected long getObjectRevisionNumber(XID id) {
		// assert: there exists and XObject with the given id either in
		// changedObjects or baseModel
		
		long revNr = 0;
		
		if(this.objectRevisionNumbers.containsKey(id)) {
			revNr = this.objectRevisionNumbers.get(id);
		} else {
			XObject object = this.baseModel.getObject(id);
			
			assert object != null;
			revNr = object.getRevisionNumber();
		}
		
		return revNr;
	}
	
	protected long getFieldRevisionNumber(XAddress fieldAddress) {
		// assert: there exists and XField with the given address either in
		// changedFields or baseModel
		
		long revNr = 0;
		
		if(this.fieldRevisionNumbers.containsKey(fieldAddress)) {
			revNr = this.fieldRevisionNumbers.get(fieldAddress);
		} else {
			XObject object = this.baseModel.getObject(fieldAddress.getObject());
			
			assert object != null;
			
			XField field = object.getField(fieldAddress.getField());
			
			assert field != null;
			
			revNr = field.getRevisionNumber();
		}
		
		return revNr;
	}
	
	// Unsupported Methods
	@Override
	public Iterator<XID> iterator() {
		throw new UnsupportedOperationException();
	}
}
