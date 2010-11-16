package org.xydra.core.model.session.impl.arm;

import java.util.Iterator;

import org.xydra.core.access.XAccessManager;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.store.AccessException;


/**
 * An {@link XBaseObject} that wraps an {@link XBaseObject} for a specific actor
 * and checks all access against an {@link XAccessManager}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedBaseObject implements XBaseObject {
	
	private final XBaseObject object;
	protected final XAccessManager arm;
	protected final XID actor;
	
	public ArmProtectedBaseObject(XBaseObject object, XAccessManager arm, XID actor) {
		this.object = object;
		this.arm = arm;
		this.actor = actor;
		
		assert object != null;
		assert arm != null;
	}
	
	protected void checkReadAccess() throws AccessException {
		// IMPROVE cache this
		if(!this.arm.canRead(this.actor, getAddress())) {
			throw new AccessException(this.actor + " cannot read " + getAddress());
		}
	}
	
	public XBaseField getField(XID fieldId) {
		
		checkCanKnowAboutField(fieldId);
		
		XBaseField field = this.object.getField(fieldId);
		
		if(field == null) {
			return null;
		}
		
		return new ArmProtectedBaseField(field, this.arm, this.actor);
	}
	
	protected void checkCanKnowAboutField(XID fieldId) {
		if(!this.arm.canKnowAboutField(this.actor, getAddress(), fieldId)) {
			throw new AccessException(this.actor + " cannot read field " + fieldId + " in "
			        + getAddress());
		}
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
	
	public XID getActor() {
		return this.actor;
	}
	
}
