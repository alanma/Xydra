package org.xydra.core.model.session.impl.arm;

import java.util.Iterator;

import org.xydra.core.access.XAccessManager;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.store.AccessException;


/**
 * An {@link XBaseModel} that wraps an {@link XBaseModel} for a specific actor
 * and checks all access against an {@link XAccessManager}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedBaseModel implements XBaseModel {
	
	private final XBaseModel model;
	protected final XAccessManager arm;
	protected final XID actor;
	
	public ArmProtectedBaseModel(XBaseModel model, XAccessManager arm, XID actor) {
		this.model = model;
		this.arm = arm;
		this.actor = actor;
		
		assert model != null;
		assert arm != null;
	}
	
	protected void checkReadAccess() throws AccessException {
		// IMPROVE cache this
		if(!this.arm.canRead(this.actor, getAddress())) {
			throw new AccessException(this.actor + " cannot read " + getAddress());
		}
	}
	
	public XBaseObject getObject(XID objectId) {
		
		checkCanKnowAboutObject(objectId);
		
		XBaseObject object = this.model.getObject(objectId);
		
		if(object == null) {
			return null;
		}
		
		return new ArmProtectedBaseObject(object, this.arm, this.actor);
	}
	
	protected void checkCanKnowAboutObject(XID objectId) {
		if(!this.arm.canKnowAboutObject(this.actor, getAddress(), objectId)) {
			throw new AccessException(this.actor + " cannot read object " + objectId + " in "
			        + getAddress());
		}
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
	
	public XAddress getAddress() {
		return this.model.getAddress();
	}
	
	public XID getID() {
		return this.model.getID();
	}
	
	public Iterator<XID> iterator() {
		
		checkReadAccess();
		
		return this.model.iterator();
	}
	
	public XID getActor() {
		return this.actor;
	}
	
}
