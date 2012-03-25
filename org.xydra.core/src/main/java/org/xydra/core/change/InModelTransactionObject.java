package org.xydra.core.change;

import java.util.Iterator;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.impl.memory.AbstractEntity;


/**
 * TODO Document & Implement
 * 
 * @author Kaidel
 */
public class InModelTransactionObject extends AbstractEntity implements XWritableObject {
	
	private XAddress address;
	private TransactionModel model;
	private long revisionNumber;
	private long transactionNumber;
	private boolean isRemoved;
	
	public InModelTransactionObject(XAddress address, long revisionNumber, TransactionModel model) {
		this.address = address;
		this.revisionNumber = revisionNumber;
		this.model = model;
		this.transactionNumber = model.getTransactionNumber();
		this.isRemoved = false;
	}
	
	@Override
	public XAddress getAddress() {
		isValid();
		
		return this.address;
	}
	
	@Override
	public XID getId() {
		isValid();
		
		return this.address.getObject();
	}
	
	public long executeCommand(XCommand command) {
		isValid();
		
		// pass it to the transaction model!
		return this.model.executeCommand(command);
	}
	
	public synchronized long executeCommand(XCommand command, XLocalChangeCallback callback) {
		isValid();
		
		// pass it to the transaction model!
		return this.model.executeCommand(command, callback);
	}
	
	@Override
	public long getRevisionNumber() {
		isValid();
		
		return this.revisionNumber;
	}
	
	@Override
	public XWritableField createField(XID fieldId) {
		isValid();
		
		XCommand fieldCommand = X.getCommandFactory().createSafeAddFieldCommand(this.address,
		        fieldId);
		this.model.executeCommand(fieldCommand);
		
		return this.model.getField(XX.toAddress(this.address.getRepository(),
		        this.address.getModel(), this.address.getObject(), fieldId));
	}
	
	@Override
	public boolean hasField(XID fieldId) {
		isValid();
		
		XWritableField field = this.model.getField(XX.toAddress(this.address.getRepository(),
		        this.address.getModel(), this.address.getObject(), fieldId));
		
		return (field != null);
	}
	
	public long executeObjectCommand(XObjectCommand command) {
		isValid();
		
		// pass it to the transaction model!
		return this.model.executeCommand(command);
	}
	
	@Override
	public XWritableField getField(XID fieldId) {
		isValid();
		
		XAddress fieldAddress = XX.toAddress(this.address.getRepository(), this.address.getModel(),
		        this.address.getObject(), fieldId);
		return this.model.getField(fieldAddress);
	}
	
	@Override
	public boolean removeField(XID fieldId) {
		isValid();
		
		XAddress fieldAddress = XX.toAddress(this.address.getRepository(), this.address.getModel(),
		        this.address.getObject(), fieldId);
		XWritableField field = this.model.getField(fieldAddress);
		// TODO Maybe implement a "getFieldRevNr" method in TransactionModel
		
		if(field == null) {
			return false;
		}
		
		XCommand fieldCommand = X.getCommandFactory().createSafeRemoveFieldCommand(fieldAddress,
		        field.getRevisionNumber());
		
		// this.model.executeCommand(...) != XCommand.FAILED
		// and != XCommand.NOCHANGE
		return (this.model.executeCommand(fieldCommand) >= 0);
	}
	
	@Override
	public AbstractEntity getFather() {
		isValid();
		
		return this.model;
	}
	
	@Override
	public XType getType() {
		isValid();
		
		return XType.XOBJECT;
	}
	
	@Override
	public boolean equals(Object object) {
		isValid();
		
		return super.equals(object);
	}
	
	@Override
	public boolean isEmpty() {
		isValid();
		
		return this.model.objectIsEmpty(this.getId());
	}
	
	@Override
	public Iterator<XID> iterator() {
		isValid();
		
		return this.model.objectIterator(this.getId());
	}
	
	private void isValid() {
		if(this.isRemoved) {
			throw new IllegalArgumentException(
			        "InModelTransactionObject was already removed and can no longer be used.");
		} else if(this.transactionNumber != this.model.getTransactionNumber()) {
			throw new IllegalArgumentException(
			        "InModelTransactionObject refers to an already committed or canceled transaction and "
			                + "can no longer be used.");
		}
		
	}
	
	protected void setRemoved() {
		this.isRemoved = true;
	}
	
	/*
	 * Unsupported Method
	 */
	public XChangeLog getChangeLog() {
		throw new UnsupportedOperationException();
	}
	
	public boolean synchronize(XEvent[] remoteChanges) {
		throw new UnsupportedOperationException();
	}
	
}
