package org.xydra.core.model.session.impl.arm;

import org.xydra.core.access.XAccessManager;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.session.XAccessException;
import org.xydra.core.model.session.XProtectedField;
import org.xydra.core.value.XValue;


/**
 * An {@link XProtectedField} that wraps an {@link XField} for a specific actor
 * and checks all access against an {@link XAccessManager}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedField implements XProtectedField {
	
	private final XField field;
	private final XAccessManager arm;
	private final XID actor;
	
	public ArmProtectedField(XField field, XAccessManager arm, XID actor) {
		this.field = field;
		this.arm = arm;
		this.actor = actor;
		
		assert field != null;
		assert arm != null;
	}
	
	public long executeFieldCommand(XFieldCommand command) {
		
		if(!this.arm.canExecute(this.actor, command)) {
			throw new XAccessException(this.actor + " cannot execute " + command);
		}
		
		return this.field.executeFieldCommand(this.actor, command);
	}
	
	public boolean setValue(XValue value) {
		
		if(!this.arm.canWrite(this.actor, getAddress())) {
			throw new XAccessException(this.actor + " cannot write to " + getAddress());
		}
		
		return this.field.setValue(this.actor, value);
	}
	
	public long getRevisionNumber() {
		
		checkReadAccess();
		
		return this.field.getRevisionNumber();
	}
	
	public XValue getValue() {
		
		checkReadAccess();
		
		return this.field.getValue();
	}
	
	public boolean isEmpty() {
		
		checkReadAccess();
		
		return this.field.isEmpty();
	}
	
	private void checkReadAccess() throws XAccessException {
		if(!this.arm.canRead(this.actor, getAddress())) {
			throw new XAccessException(this.actor + " cannot read " + getAddress());
		}
	}
	
	public XAddress getAddress() {
		return this.field.getAddress();
	}
	
	public XID getID() {
		return this.field.getID();
	}
	
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		
		checkReadAccess();
		
		return this.field.addListenerForFieldEvents(changeListener);
	}
	
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.field.removeListenerForFieldEvents(changeListener);
	}
	
	public XID getActor() {
		return this.actor;
	}
	
}
