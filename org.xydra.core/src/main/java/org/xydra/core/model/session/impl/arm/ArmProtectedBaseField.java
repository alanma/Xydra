package org.xydra.core.model.session.impl.arm;

import org.xydra.base.XAddress;
import org.xydra.base.XReadableField;
import org.xydra.base.XID;
import org.xydra.base.value.XValue;
import org.xydra.core.access.XAccessManager;
import org.xydra.store.AccessException;


/**
 * An {@link XReadableField} that wraps an {@link XReadableField} for a specific actor
 * and checks all access against an {@link XAccessManager}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedBaseField implements XReadableField {
	
	private final XReadableField field;
	protected final XAccessManager arm;
	protected final XID actor;
	
	public ArmProtectedBaseField(XReadableField field, XAccessManager arm, XID actor) {
		this.field = field;
		this.arm = arm;
		this.actor = actor;
		
		assert field != null;
		assert arm != null;
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
	
	protected void checkReadAccess() throws AccessException {
		// IMPROVE cache this?
		if(!this.arm.canRead(this.actor, getAddress())) {
			throw new AccessException(this.actor + " cannot read " + getAddress());
		}
	}
	
	public XAddress getAddress() {
		return this.field.getAddress();
	}
	
	public XID getID() {
		return this.field.getID();
	}
	
	public XID getActor() {
		return this.actor;
	}
	
}
