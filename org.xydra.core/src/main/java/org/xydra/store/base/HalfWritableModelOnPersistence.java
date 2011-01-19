package org.xydra.store.base;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XHalfWritableModel;
import org.xydra.base.XHalfWritableObject;
import org.xydra.base.XID;
import org.xydra.core.X;
import org.xydra.core.change.XCommand;
import org.xydra.store.impl.delegate.XydraPersistence;


public class HalfWritableModelOnPersistence extends AbstractHalfWritableOnPersistence implements
        XHalfWritableModel {
	
	private XID modelId;
	
	public HalfWritableModelOnPersistence(XydraPersistence persistence, XID executingActorId,
	        XID modelId) {
		super(persistence, executingActorId);
		this.modelId = modelId;
	}
	
	public XHalfWritableObject createObject(XID objectId) {
		// create in persistence
		XCommand command = X.getCommandFactory().createAddObjectCommand(
		        this.persistence.getRepositoryId(), this.modelId, objectId, false);
		this.persistence.executeCommand(this.executingActorId, command);
		return getObject(objectId);
	}
	
	@Override
	public XAddress getAddress() {
		if(this.address == null) {
			this.address = X.getIDProvider().fromComponents(this.persistence.getRepositoryId(),
			        this.modelId, null, null);
		}
		return this.address;
	}
	
	@Override
	public XID getID() {
		return this.modelId;
	}
	
	public XHalfWritableObject getObject(XID objectId) {
		if(hasObject(objectId)) {
			// make sure changes to object are reflected in persistence
			return new HalfWritableObjectOnPersistence(this.persistence, this.executingActorId,
			        this.modelId, objectId);
		} else {
			return null;
		}
	}
	
	public long getRevisionNumber() {
		return this.persistence.getModelSnapshot(getAddress()).getRevisionNumber();
	}
	
	public boolean hasObject(XID objectId) {
		return this.persistence.getModelSnapshot(getAddress()).hasObject(objectId);
	}
	
	public boolean isEmpty() {
		return this.persistence.getModelSnapshot(getAddress()).isEmpty();
	}
	
	public Iterator<XID> iterator() {
		return this.persistence.getModelSnapshot(getAddress()).iterator();
	}
	
	public boolean removeObject(XID objectId) {
		boolean result = hasObject(objectId);
		XCommand command = X.getCommandFactory().createRemoveObjectCommand(
		        this.persistence.getRepositoryId(), this.modelId, objectId, XCommand.FORCED, false);
		long commandResult = this.persistence.executeCommand(this.executingActorId, command);
		assert commandResult >= 0;
		return result;
	}
	
}
