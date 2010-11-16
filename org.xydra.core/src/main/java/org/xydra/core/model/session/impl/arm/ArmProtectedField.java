package org.xydra.core.model.session.impl.arm;

import org.xydra.core.access.XAccessManager;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.session.XProtectedField;
import org.xydra.core.value.XValue;
import org.xydra.store.AccessException;


/**
 * An {@link XProtectedField} that wraps an {@link XField} for a specific actor
 * and checks all access against an {@link XAccessManager}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedField extends ArmProtectedBaseField implements XProtectedField {
	
	private final XField field;
	
	public ArmProtectedField(XField field, XAccessManager arm, XID actor) {
		super(field, arm, actor);
		this.field = field;
	}
	
	public long executeFieldCommand(XFieldCommand command) {
		
		if(!this.arm.canExecute(this.actor, command)) {
			throw new AccessException(this.actor + " cannot execute " + command);
		}
		
		return this.field.executeFieldCommand(this.actor, command);
	}
	
	public boolean setValue(XValue value) {
		
		if(!this.arm.canWrite(this.actor, getAddress())) {
			throw new AccessException(this.actor + " cannot write to " + getAddress());
		}
		
		return this.field.setValue(this.actor, value);
	}
	
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		
		checkReadAccess();
		
		return this.field.addListenerForFieldEvents(changeListener);
	}
	
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.field.removeListenerForFieldEvents(changeListener);
	}
	
}
