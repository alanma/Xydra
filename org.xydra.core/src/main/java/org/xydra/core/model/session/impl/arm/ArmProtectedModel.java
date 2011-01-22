package org.xydra.core.model.session.impl.arm;

import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.IHasChangeLog;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.session.XProtectedModel;
import org.xydra.core.model.session.XProtectedObject;
import org.xydra.store.AccessException;
import org.xydra.store.access.XAuthorisationManager;


/**
 * An {@link XProtectedModel} that wraps an {@link XModel} for a specific actor
 * and checks all access against an {@link XAuthorisationManager}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedModel extends ArmProtectedBaseModel implements XProtectedModel,
        IHasChangeLog {
	
	private final XModel model;
	
	public ArmProtectedModel(XModel model, XAuthorisationManager arm, XID actor) {
		super(model, arm, actor);
		this.model = model;
	}
	
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		
		checkReadAccess();
		
		return this.model.addListenerForFieldEvents(changeListener);
	}
	
	public boolean addListenerForModelEvents(XModelEventListener changeListener) {
		
		checkReadAccess();
		
		return this.model.addListenerForModelEvents(changeListener);
	}
	
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		
		checkReadAccess();
		
		return this.model.addListenerForObjectEvents(changeListener);
	}
	
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		
		checkReadAccess();
		
		return this.model.addListenerForTransactionEvents(changeListener);
	}
	
	public XProtectedObject createObject(XID objectId) {
		
		if(!this.arm.canWrite(this.actor, getAddress())) {
			throw new AccessException(this.actor + " cannot write to " + getAddress());
		}
		
		XObject object = this.model.createObject(objectId);
		
		assert object != null;
		
		return new ArmProtectedObject(object, this.arm, this.actor);
	}
	
	public long executeCommand(XCommand command) {
		
		if(!this.arm.canExecute(this.actor, command)) {
			throw new AccessException(this.actor + " cannot execute " + command);
		}
		
		return this.model.executeCommand(command);
	}
	
	public long executeModelCommand(XModelCommand command) {
		
		if(!this.arm.canExecute(this.actor, command)) {
			throw new AccessException(this.actor + " cannot execute " + command);
		}
		
		return this.model.executeModelCommand(command);
	}
	
	public XChangeLog getChangeLog() {
		return new ArmProtectedChangeLog(this.model.getChangeLog(), this.arm, this.actor);
	}
	
	@Override
	public XProtectedObject getObject(XID objectId) {
		
		checkCanKnowAboutObject(objectId);
		
		XObject object = this.model.getObject(objectId);
		
		if(object == null) {
			return null;
		}
		
		return new ArmProtectedObject(object, this.arm, this.actor);
	}
	
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.model.removeListenerForFieldEvents(changeListener);
	}
	
	public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
		return this.model.removeListenerForModelEvents(changeListener);
	}
	
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		return this.model.removeListenerForObjectEvents(changeListener);
	}
	
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		return this.model.removeListenerForTransactionEvents(changeListener);
	}
	
	public boolean removeObject(XID objectId) {
		
		if(!this.arm.canRemoveObject(this.actor, getAddress(), objectId)) {
			throw new AccessException(this.actor + " cannot remove " + objectId + " from "
			        + getAddress());
		}
		
		return this.model.removeObject(objectId);
	}
	
}
