package org.xydra.core.model.session.impl.arm;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.value.XValue;
import org.xydra.store.AccessException;
import org.xydra.store.access.XAuthorisationManager;


/**
 * An {@link XReadableField} that wraps an {@link XReadableField} for a specific
 * actor and checks all access against an {@link XAuthorisationManager}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedBaseField implements XReadableField {
	
	protected final XID actor;
	protected final XAuthorisationManager arm;
	private final XReadableField field;
	
	public ArmProtectedBaseField(XReadableField field, XAuthorisationManager arm, XID actor) {
		this.field = field;
		this.arm = arm;
		this.actor = actor;
		
		assert field != null;
		assert arm != null;
	}
	
	protected void checkReadAccess() throws AccessException {
		// IMPROVE cache this?
		if(!this.arm.canRead(this.actor, getAddress())) {
			throw new AccessException(this.actor + " cannot read " + getAddress());
		}
	}
	
	public XID getActor() {
		return this.actor;
	}
	
	public XAddress getAddress() {
		return this.field.getAddress();
	}
	
	public XID getID() {
		return this.field.getID();
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
	
}
