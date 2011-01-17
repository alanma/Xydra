package org.xydra.core.model.session.impl.arm;

import org.xydra.core.access.XAccessManager;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.IHasChangeLog;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XObject;
import org.xydra.core.model.session.XProtectedField;
import org.xydra.core.model.session.XProtectedObject;
import org.xydra.store.AccessException;


/**
 * An {@link XProtectedObject} that wraps an {@link XObject} for a specific
 * actor and checks all access against an {@link XAccessManager}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedObject extends ArmProtectedBaseObject implements XProtectedObject,
        IHasChangeLog {
	
	private final XObject object;
	
	public ArmProtectedObject(XObject object, XAccessManager arm, XID actor) {
		super(object, arm, actor);
		this.object = object;
	}
	
	public XProtectedField createField(XID fieldId) {
		
		if(!this.arm.canWrite(this.actor, getAddress())) {
			throw new AccessException(this.actor + " cannot write to " + getAddress());
		}
		
		XField field = this.object.createField(fieldId);
		
		assert field != null;
		
		return new ArmProtectedField(field, this.arm, this.actor);
	}
	
	public long executeObjectCommand(XObjectCommand command) {
		
		if(!this.arm.canExecute(this.actor, command)) {
			throw new AccessException(this.actor + " cannot execute " + command);
		}
		
		return this.object.executeObjectCommand(command);
	}
	
	@Override
	public XProtectedField getField(XID fieldId) {
		
		checkCanKnowAboutField(fieldId);
		
		XField field = this.object.getField(fieldId);
		
		if(field == null) {
			return null;
		}
		
		return new ArmProtectedField(field, this.arm, this.actor);
	}
	
	public boolean removeField(XID fieldId) {
		
		if(!this.arm.canRemoveField(this.actor, getAddress(), fieldId)) {
			throw new AccessException(this.actor + " cannot remove " + fieldId + " from "
			        + getAddress());
		}
		
		return this.object.removeField(fieldId);
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
	
	public long executeCommand(XCommand command) {
		
		if(!this.arm.canExecute(this.actor, command)) {
			throw new AccessException(this.actor + " cannot execute " + command);
		}
		
		return this.object.executeCommand(command);
	}
	
	public XChangeLog getChangeLog() {
		return new ArmProtectedChangeLog(this.object.getChangeLog(), this.arm, this.actor);
	}
	
}
