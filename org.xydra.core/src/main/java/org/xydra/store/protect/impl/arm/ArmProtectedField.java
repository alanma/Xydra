package org.xydra.store.protect.impl.arm;

import org.xydra.base.XId;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.value.XValue;
import org.xydra.core.AccessException;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.model.XField;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.protect.XProtectedField;



/**
 * An {@link XProtectedField} that wraps an {@link XField} for a specific actor
 * and checks all access against an {@link XAuthorisationManager}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedField extends ArmProtectedBaseField implements XProtectedField {
	
	private final XField field;
	
	public ArmProtectedField(XField field, XAuthorisationManager arm, XId actor) {
		super(field, arm, actor);
		this.field = field;
	}
	
	@Override
    public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		
		checkReadAccess();
		
		return this.field.addListenerForFieldEvents(changeListener);
	}
	
	@Override
    public long executeFieldCommand(XFieldCommand command) {
		
		if(!this.arm.canExecute(this.actor, command)) {
			throw new AccessException(this.actor + " cannot execute " + command);
		}
		
		return this.field.executeFieldCommand(command);
	}
	
	@Override
    public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.field.removeListenerForFieldEvents(changeListener);
	}
	
	@Override
    public boolean setValue(XValue value) {
		
		if(!this.arm.canWrite(this.actor, getAddress())) {
			throw new AccessException(this.actor + " cannot write to " + getAddress());
		}
		
		return this.field.setValue(value);
	}
	
}
