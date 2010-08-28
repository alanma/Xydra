package org.xydra.core.model.session.impl.arm;

import java.util.Iterator;

import org.xydra.core.access.XAccessManager;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XObject;
import org.xydra.core.model.session.XAccessException;
import org.xydra.core.model.session.XProtectedField;
import org.xydra.core.model.session.XProtectedObject;


/**
 * An {@link XProtectedObject} that wraps an {@link XObject} for a specific
 * actor and checks all access against an {@link XAccessManager}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedObject implements XProtectedObject {
	
	private final XObject object;
	private final XAccessManager arm;
	private final XID actor;
	
	public ArmProtectedObject(XObject object, XAccessManager arm, XID actor) {
		this.object = object;
		this.arm = arm;
		this.actor = actor;
		
		assert object != null;
		assert arm != null;
	}
	
	private void checkReadAccess() throws XAccessException {
		if(!this.arm.canRead(this.actor, getAddress())) {
			throw new XAccessException(this.actor + " cannot read " + getAddress());
		}
	}
	
	public XProtectedField createField(XID fieldId) {
		
		if(!this.arm.canWrite(this.actor, getAddress())) {
			throw new XAccessException(this.actor + " cannot write to " + getAddress());
		}
		
		XField field = this.object.createField(this.actor, fieldId);
		
		assert field != null;
		
		return new ArmProtectedField(field, this.arm, this.actor);
	}
	
	public long executeObjectCommand(XObjectCommand command) {
		
		if(!this.arm.canExecute(this.actor, command)) {
			throw new XAccessException(this.actor + " cannot execute " + command);
		}
		
		return this.object.executeObjectCommand(this.actor, command);
	}
	
	public XProtectedField getField(XID fieldId) {
		
		if(!this.arm.canKnowAboutField(this.actor, getAddress(), fieldId)) {
			throw new XAccessException(this.actor + " cannot read field " + fieldId + " in "
			        + getAddress());
		}
		
		XField field = this.object.getField(fieldId);
		
		if(field == null) {
			return null;
		}
		
		return new ArmProtectedField(field, this.arm, this.actor);
	}
	
	public boolean removeField(XID fieldId) {
		
		if(!this.arm.canRemoveField(this.actor, getAddress(), fieldId)) {
			throw new XAccessException(this.actor + " cannot remove " + fieldId + " from "
			        + getAddress());
		}
		
		return this.object.removeField(this.actor, fieldId);
	}
	
	public long getRevisionNumber() {
		
		checkReadAccess();
		
		return this.object.getRevisionNumber();
	}
	
	public boolean hasField(XID fieldId) {
		
		checkReadAccess();
		
		return this.object.hasField(fieldId);
	}
	
	public boolean isEmpty() {
		
		checkReadAccess();
		
		return this.object.isEmpty();
	}
	
	public XAddress getAddress() {
		return this.object.getAddress();
	}
	
	public XID getID() {
		return this.object.getID();
	}
	
	public Iterator<XID> iterator() {
		
		checkReadAccess();
		
		return this.object.iterator();
	}
	
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		
		checkReadAccess();
		
		return this.object.addListenerForObjectEvents(changeListener);
	}
	
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		return this.object.removeListenerForObjectEvents(changeListener);
	}
	
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		
		checkReadAccess();
		
		return this.object.addListenerForFieldEvents(changeListener);
	}
	
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.object.removeListenerForFieldEvents(changeListener);
	}
	
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		
		checkReadAccess();
		
		return this.object.addListenerForTransactionEvents(changeListener);
	}
	
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		return this.object.removeListenerForTransactionEvents(changeListener);
	}
	
	public long executeTransaction(XTransaction transaction) {
		
		if(!this.arm.canExecute(this.actor, transaction)) {
			throw new XAccessException(this.actor + " cannot execute " + transaction);
		}
		
		return this.object.executeTransaction(this.actor, transaction);
	}
	
	public long executeCommand(XCommand command) {
		
		if(!this.arm.canExecute(this.actor, command)) {
			throw new XAccessException(this.actor + " cannot execute " + command);
		}
		
		return this.object.executeCommand(this.actor, command);
	}
	
	public XChangeLog getChangeLog() {
		return new ArmProtectedChangeLog(this.object.getChangeLog(), this.arm, this.actor);
	}
	
	public XID getActor() {
		return this.actor;
	}
	
}
