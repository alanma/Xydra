package org.xydra.core.model.session.impl.arm;

import java.util.Iterator;

import org.xydra.core.access.XAccessManager;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.session.XAccessException;
import org.xydra.core.model.session.XProtectedModel;
import org.xydra.core.model.session.XProtectedObject;


/**
 * An {@link XProtectedModel} that wraps an {@link XModel} for a specific actor
 * and checks all access against an {@link XAccessManager}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedModel implements XProtectedModel {
	
	private final XModel model;
	private final XAccessManager arm;
	private final XID actor;
	
	public ArmProtectedModel(XModel model, XAccessManager arm, XID actor) {
		this.model = model;
		this.arm = arm;
		this.actor = actor;
		
		assert model != null;
		assert arm != null;
	}
	
	private void checkReadAccess() throws XAccessException {
		if(!this.arm.canRead(this.actor, getAddress())) {
			throw new XAccessException(this.actor + " cannot read " + getAddress());
		}
	}
	
	public XProtectedObject createObject(XID objectId) {
		
		if(!this.arm.canWrite(this.actor, getAddress())) {
			throw new XAccessException(this.actor + " cannot write to " + getAddress());
		}
		
		XObject object = this.model.createObject(this.actor, objectId);
		
		assert object != null;
		
		return new ArmProtectedObject(object, this.arm, this.actor);
	}
	
	public long executeModelCommand(XModelCommand command) {
		
		if(!this.arm.canExecute(this.actor, command)) {
			throw new XAccessException(this.actor + " cannot execute " + command);
		}
		
		return this.model.executeModelCommand(this.actor, command);
	}
	
	public XProtectedObject getObject(XID objectId) {
		
		if(!this.arm.canKnowAboutObject(this.actor, getAddress(), objectId)) {
			throw new XAccessException(this.actor + " cannot read object " + objectId + " in "
			        + getAddress());
		}
		
		XObject object = this.model.getObject(objectId);
		
		if(object == null) {
			return null;
		}
		
		return new ArmProtectedObject(object, this.arm, this.actor);
	}
	
	public boolean removeObject(XID objectId) {
		
		if(!this.arm.canRemoveObject(this.actor, getAddress(), objectId)) {
			throw new XAccessException(this.actor + " cannot remove " + objectId + " from "
			        + getAddress());
		}
		
		return this.model.removeObject(this.actor, objectId);
	}
	
	public long getRevisionNumber() {
		
		checkReadAccess();
		
		return this.model.getRevisionNumber();
	}
	
	public boolean hasObject(XID objectId) {
		
		checkReadAccess();
		
		return this.model.hasObject(objectId);
	}
	
	public boolean isEmpty() {
		
		checkReadAccess();
		
		return this.model.isEmpty();
	}
	
	public XAddress getAddress() {
		return this.model.getAddress();
	}
	
	public XID getID() {
		return this.model.getID();
	}
	
	public Iterator<XID> iterator() {
		
		checkReadAccess();
		
		return this.model.iterator();
	}
	
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		
		checkReadAccess();
		
		return this.model.addListenerForObjectEvents(changeListener);
	}
	
	// TODO shouldn't the removeListener... methods check for write access?
	
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		return this.model.removeListenerForObjectEvents(changeListener);
	}
	
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		
		checkReadAccess();
		
		return this.model.addListenerForFieldEvents(changeListener);
	}
	
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.model.removeListenerForFieldEvents(changeListener);
	}
	
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		
		checkReadAccess();
		
		return this.model.addListenerForTransactionEvents(changeListener);
	}
	
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		return this.model.removeListenerForTransactionEvents(changeListener);
	}
	
	public long executeTransaction(XTransaction transaction) {
		
		if(!this.arm.canExecute(this.actor, transaction)) {
			throw new XAccessException(this.actor + " cannot execute " + transaction);
		}
		
		return this.model.executeTransaction(this.actor, transaction);
	}
	
	public long executeCommand(XCommand command) {
		
		if(!this.arm.canExecute(this.actor, command)) {
			throw new XAccessException(this.actor + " cannot execute " + command);
		}
		
		return this.model.executeCommand(this.actor, command);
	}
	
	public boolean addListenerForModelEvents(XModelEventListener changeListener) {
		
		checkReadAccess();
		
		return this.model.addListenerForModelEvents(changeListener);
	}
	
	public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
		return this.model.removeListenerForModelEvents(changeListener);
	}
	
	public XChangeLog getChangeLog() {
		return new ArmProtectedChangeLog(this.model.getChangeLog(), this.arm, this.actor);
	}
	
}
