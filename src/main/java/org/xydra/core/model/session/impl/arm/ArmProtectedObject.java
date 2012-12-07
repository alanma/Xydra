package org.xydra.core.model.session.impl.arm;

import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.IHasChangeLog;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XField;
import org.xydra.core.model.XObject;
import org.xydra.core.model.session.XProtectedField;
import org.xydra.core.model.session.XProtectedObject;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.AccessException;
import org.xydra.store.access.XAuthorisationManager;


/**
 * An {@link XProtectedObject} that wraps an {@link XObject} for a specific
 * actor and checks all access against an {@link XAuthorisationManager}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedObject extends ArmProtectedBaseObject implements XProtectedObject,
        IHasChangeLog {
	
	private final XObject object;
	
	public ArmProtectedObject(XObject object, XAuthorisationManager arm, XID actor) {
		super(object, arm, actor);
		this.object = object;
	}
	
	@Override
    public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		
		checkReadAccess();
		
		return this.object.addListenerForFieldEvents(changeListener);
	}
	
	@Override
    public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		
		checkReadAccess();
		
		return this.object.addListenerForObjectEvents(changeListener);
	}
	
	@Override
    public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		
		checkReadAccess();
		
		return this.object.addListenerForTransactionEvents(changeListener);
	}
	
	@Override
    public XProtectedField createField(XID fieldId) {
		
		if(!this.arm.canWrite(this.actor, getAddress())) {
			throw new AccessException(this.actor + " cannot write to " + getAddress());
		}
		
		XField field = this.object.createField(fieldId);
		
		XyAssert.xyAssert(field != null); assert field != null;
		
		return new ArmProtectedField(field, this.arm, this.actor);
	}
	
	@Override
    public long executeCommand(XCommand command) {
		
		if(!this.arm.canExecute(this.actor, command)) {
			throw new AccessException(this.actor + " cannot execute " + command);
		}
		
		return this.object.executeCommand(command);
	}
	
	@Override
    public long executeObjectCommand(XObjectCommand command) {
		
		if(!this.arm.canExecute(this.actor, command)) {
			throw new AccessException(this.actor + " cannot execute " + command);
		}
		
		return this.object.executeObjectCommand(command);
	}
	
	@Override
    public XChangeLog getChangeLog() {
		return new ArmProtectedChangeLog(this.object.getChangeLog(), this.arm, this.actor);
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
	
	@Override
    public boolean removeField(XID fieldId) {
		
		if(!this.arm.canRemoveField(this.actor, getAddress(), fieldId)) {
			throw new AccessException(this.actor + " cannot remove " + fieldId + " from "
			        + getAddress());
		}
		
		return this.object.removeField(fieldId);
	}
	
	@Override
    public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.object.removeListenerForFieldEvents(changeListener);
	}
	
	@Override
    public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		return this.object.removeListenerForObjectEvents(changeListener);
	}
	
	@Override
    public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		return this.object.removeListenerForTransactionEvents(changeListener);
	}
	
}
