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
	
	public InModelTransactionObject(XAddress address, long revisionNumber, TransactionModel model) {
		this.address = address;
		this.revisionNumber = revisionNumber;
		this.model = model;
	}
	
	public XAddress getAddress() {
		return this.address;
	}
	
	public XID getID() {
		return this.address.getObject();
	}
	
	public long executeCommand(XCommand command) {
		// pass it to the transaction model!
		return this.model.executeCommand(command);
	}
	
	public synchronized long executeCommand(XCommand command, XLocalChangeCallback callback) {
		// pass it to the transaction model!
		return this.model.executeCommand(command, callback);
	}
	
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	public XWritableField createField(XID fieldId) {
		XCommand fieldCommand = X.getCommandFactory().createSafeAddFieldCommand(
		        this.model.getAddress(), fieldId);
		this.model.executeCommand(fieldCommand);
		
		return this.model.getField(XX.toAddress(this.address.getRepository(),
		        this.address.getModel(), this.address.getObject(), fieldId));
	}
	
	public boolean hasField(XID fieldId) {
		XWritableField field = this.model.getField(XX.toAddress(this.address.getRepository(),
		        this.address.getModel(), this.address.getObject(), fieldId));
		
		return (field != null);
	}
	
	public long executeObjectCommand(XObjectCommand command) {
		// pass it to the transaction model!
		return this.model.executeCommand(command);
	}
	
	public XWritableField getField(XID fieldId) {
		XAddress fieldAddress = XX.toAddress(this.address.getRepository(), this.address.getModel(),
		        this.address.getObject(), fieldId);
		return this.model.getField(fieldAddress);
	}
	
	public boolean removeField(XID fieldId) {
		XAddress fieldAddress = XX.toAddress(this.address.getRepository(), this.address.getModel(),
		        this.address.getObject(), fieldId);
		XWritableField field = this.model.getField(fieldAddress);
		// TODO Maybe implement a "getFieldRevNr" method in TransactionModel
		
		XCommand fieldCommand = X.getCommandFactory().createSafeRemoveFieldCommand(fieldAddress,
		        field.getRevisionNumber());
		
		// this.model.executeCommand(...) != XCommand.FAILED
		// and != XCommand.NOCHANGE
		return (this.model.executeCommand(fieldCommand) >= 0);
	}
	
	@Override
	public AbstractEntity getFather() {
		return this.model;
	}
	
	@Override
	public XType getType() {
		return XType.XOBJECT;
	}
	
	@Override
	public boolean equals(Object object) {
		return super.equals(object);
	}
	
	/*
	 * Unsupported Method
	 */
	public XChangeLog getChangeLog() {
		throw new UnsupportedOperationException();
	}
	
	public boolean isEmpty() {
		// TODO maybe implement a simple "isEmpty" method in
		// TransactionModel?
		throw new UnsupportedOperationException();
	}
	
	public boolean synchronize(XEvent[] remoteChanges) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Iterator<XID> iterator() {
		// TODO Implement
		throw new UnsupportedOperationException();
	}
}
