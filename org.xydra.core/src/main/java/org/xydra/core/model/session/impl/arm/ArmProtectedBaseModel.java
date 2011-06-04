package org.xydra.core.model.session.impl.arm;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.store.AccessException;
import org.xydra.store.access.XAuthorisationManager;


/**
 * An {@link XReadableModel} that wraps an {@link XReadableModel} for a specific
 * actor and checks all access against an {@link XAuthorisationManager}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedBaseModel implements XReadableModel {
	
	protected final XID actor;
	protected final XAuthorisationManager arm;
	private final XReadableModel model;
	
	public ArmProtectedBaseModel(XReadableModel model, XAuthorisationManager arm, XID actor) {
		this.model = model;
		this.arm = arm;
		this.actor = actor;
		
		assert model != null;
		assert arm != null;
	}
	
	protected void checkCanKnowAboutObject(XID objectId) {
		if(!this.arm.canKnowAboutObject(this.actor, getAddress(), objectId)) {
			throw new AccessException(this.actor + " cannot read object " + objectId + " in "
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
	
	public XAddress getAddress() {
		return this.model.getAddress();
	}
	
	public XID getID() {
		return this.model.getID();
	}
	
	public XReadableObject getObject(XID objectId) {
		
		checkCanKnowAboutObject(objectId);
		
		XReadableObject object = this.model.getObject(objectId);
		
		if(object == null) {
			return null;
		}
		
		return new ArmProtectedBaseObject(object, this.arm, this.actor);
	}
	
	public long getRevisionNumber() {
		
		checkReadAccess();
		
		return this.model.getRevisionNumber();
	}
	
	public boolean hasObject(XID objectId) {
		
		checkReadAccess();
		
		return this.model.hasObject(objectId);
	}
	
	public boolean isEmpty() {
		
		checkReadAccess();
		
		return this.model.isEmpty();
	}
	
	public Iterator<XID> iterator() {
		
		checkReadAccess();
		
		return this.model.iterator();
	}
	
	@Override
	public XType getType() {
		return XType.XMODEL;
	}
	
}
