package org.xydra.core.change;

import java.util.Iterator;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XObject;


/**
 * TODO Document & Implement
 * 
 * @author Kaidel
 */
public class InTransactionObject implements XWritableObject {
	private XObject object;
	private TransactionModel model;
	
	public InTransactionObject(XObject object, TransactionModel model) {
		this.object = object;
		this.model = model;
	}
	
	public XChangeLog getChangeLog() {
		throw new UnsupportedOperationException();
	}
	
	public XAddress getAddress() {
		return this.object.getAddress();
	}
	
	public XID getID() {
		return this.object.getID();
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
		return this.model.getObjectRevisionNumber(this.object.getID());
	}
	
	public XWritableField createField(XID fieldId) {
		XCommand fieldCommand = X.getCommandFactory().createSafeAddFieldCommand(
		        this.model.getAddress(), fieldId);
		this.model.executeCommand(fieldCommand);
		
		return this.model.getField(XX.toAddress(this.model.getAddress().getRepository(),
		        this.model.getID(), this.object.getID(), fieldId));
	}
	
	public boolean hasField(XID fieldId) {
		XWritableField field = this.model.getField(XX.toAddress(this.model.getAddress()
		        .getRepository(), this.model.getID(), this.object.getID(), fieldId));
		
		return (field != null);
	}
	
	public boolean isEmpty() {
		// TODO maybe implement a simple "isEmpty" method in
		// TransactionModel?
		throw new UnsupportedOperationException();
	}
	
	public long executeObjectCommand(XObjectCommand command) {
		// pass it to the transaction model!
		return this.model.executeCommand(command);
	}
	
	public boolean synchronize(XEvent[] remoteChanges) {
		throw new UnsupportedOperationException();
	}
	
	public XWritableField getField(XID fieldId) {
		XAddress fieldAddress = XX.toAddress(this.model.getAddress().getRepository(),
		        this.model.getID(), this.object.getID(), fieldId);
		return this.model.getField(fieldAddress);
	}
	
	public boolean removeField(XID fieldId) {
		XAddress fieldAddress = XX.toAddress(this.model.getAddress().getRepository(),
		        this.model.getID(), this.object.getID(), fieldId);
		long fieldRevision = this.model.getFieldRevisionNumber(fieldAddress);
		
		XCommand fieldCommand = X.getCommandFactory().createSafeRemoveFieldCommand(fieldAddress,
		        fieldRevision);
		
		// this.model.executeCommand(...) != XCommand.FAILED
		// and != XCommand.NOCHANGE
		return (this.model.executeCommand(fieldCommand) >= 0);
	}
	
	@Override
	public Iterator<XID> iterator() {
		throw new UnsupportedOperationException();
	}
}
