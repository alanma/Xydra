package org.xydra.core.change;

import java.util.Iterator;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLocalChange;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XObject;


/**
 * TODO Document
 * 
 * @author Björn
 * 
 */

public class InTransactionObject implements XObject {
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
	
	public long executeCommand(XCommand command, XLocalChangeCallback callback) {
		// pass it to the transaction model!
		return this.model.executeCommand(command, callback);
	}
	
	public long getRevisionNumber() {
		return this.model.getObjectRevisionNumber(this.object.getID());
	}
	
	public XField createField(XID fieldId) {
		XCommand fieldCommand = X.getCommandFactory().createSafeAddFieldCommand(
		        this.model.getAddress(), fieldId);
		this.model.executeCommand(fieldCommand);
		
		return this.model.getField(XX.toAddress(this.model.getAddress().getRepository(),
		        this.model.getID(), this.object.getID(), fieldId));
	}
	
	public boolean hasField(XID fieldId) {
		XField field = this.model.getField(XX.toAddress(this.model.getAddress().getRepository(),
		        this.model.getID(), this.object.getID(), fieldId));
		
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
	
	public XField getField(XID fieldId) {
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
	
	@Override
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int countUnappliedLocalChanges() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public XLocalChange[] getLocalChanges() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public XID getSessionActor() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public long getSynchronizedRevision() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void rollback(long revision) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setSessionActor(XID actorId, String passwordHash) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public XRevWritableObject createSnapshot() {
		throw new UnsupportedOperationException();
	}
}
