package org.xydra.core.model.session.impl.arm;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.AccessException;
import org.xydra.store.access.XAuthorisationManager;


/**
 * An {@link XReadableObject} that wraps an {@link XReadableObject} for a
 * specific actor and checks all access against an {@link XAuthorisationManager}
 * .
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedBaseObject implements XReadableObject {
	
	protected final XID actor;
	protected final XAuthorisationManager arm;
	private final XReadableObject object;
	
	public ArmProtectedBaseObject(XReadableObject object, XAuthorisationManager arm, XID actor) {
		this.object = object;
		this.arm = arm;
		this.actor = actor;
		
		XyAssert.xyAssert(object != null); assert object != null;
		XyAssert.xyAssert(arm != null); assert arm != null;
	}
	
	protected void checkCanKnowAboutField(XID fieldId) {
		if(!this.arm.canKnowAboutField(this.actor, getAddress(), fieldId)) {
			throw new AccessException(this.actor + " cannot read field " + fieldId + " in "
			        + getAddress());
		}
	}
	
	protected void checkReadAccess() throws AccessException {
		// IMPROVE cache this
		if(!this.arm.canRead(this.actor, getAddress())) {
			throw new AccessException(this.actor + " cannot read " + getAddress());
		}
	}
	
	public XID getActor() {
		return this.actor;
	}
	
	@Override
    public XAddress getAddress() {
		return this.object.getAddress();
	}
	
	@Override
    public XReadableField getField(XID fieldId) {
		
		checkCanKnowAboutField(fieldId);
		
		XReadableField field = this.object.getField(fieldId);
		
		if(field == null) {
			return null;
		}
		
		return new ArmProtectedBaseField(field, this.arm, this.actor);
	}
	
	@Override
    public XID getId() {
		return this.object.getId();
	}
	
	@Override
    public long getRevisionNumber() {
		
		checkReadAccess();
		
		return this.object.getRevisionNumber();
	}
	
	@Override
    public boolean hasField(XID fieldId) {
		
		checkReadAccess();
		
		return this.object.hasField(fieldId);
	}
	
	@Override
    public boolean isEmpty() {
		
		checkReadAccess();
		
		return this.object.isEmpty();
	}
	
	@Override
    public Iterator<XID> iterator() {
		
		checkReadAccess();
		
		return this.object.iterator();
		
	}
	
	@Override
	public XType getType() {
		return XType.XOBJECT;
	}
	
}