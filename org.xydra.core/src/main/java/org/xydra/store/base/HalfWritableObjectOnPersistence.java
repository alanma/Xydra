package org.xydra.store.base;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XHalfWritableField;
import org.xydra.base.XHalfWritableObject;
import org.xydra.base.XID;
import org.xydra.core.X;
import org.xydra.core.change.XCommand;
import org.xydra.store.impl.delegate.XydraPersistence;


public class HalfWritableObjectOnPersistence extends AbstractHalfWritableOnPersistence implements
        XHalfWritableObject {
	
	private XID modelId;
	
	private XID objectId;
	
	public HalfWritableObjectOnPersistence(XydraPersistence persistence, XID executingActorId,
	        XID modelId, XID objectId) {
		super(persistence, executingActorId);
		this.modelId = modelId;
		this.objectId = objectId;
	}
	
	public XHalfWritableField createField(XID fieldId) {
		// assume model and object exist
		// create in persistence
		XCommand command = X.getCommandFactory().createAddFieldCommand(
		        this.persistence.getRepositoryId(), this.modelId, this.objectId, fieldId, false);
		this.persistence.executeCommand(this.executingActorId, command);
		return getField(fieldId);
	}
	
	@Override
	public XAddress getAddress() {
		if(this.address == null) {
			this.address = X.getIDProvider().fromComponents(this.persistence.getRepositoryId(),
			        this.modelId, this.objectId, null);
		}
		return this.address;
	}
	
	@Override
	public XID getID() {
		return this.objectId;
	}
	
	public XHalfWritableField getField(XID fieldId) {
		if(hasField(fieldId)) {
			// make sure changes to object are reflected in persistence
			return new HalfWritableFieldOnPersistence(this.persistence, this.executingActorId,
			        this.modelId, this.objectId, fieldId);
		} else {
			return null;
		}
	}
	
	public long getRevisionNumber() {
		return getObjectSnapshot().getRevisionNumber();
	}
	
	private XHalfWritableObject getObjectSnapshot() {
		return this.persistence.getModelSnapshot(
		        X.getIDProvider().fromComponents(this.persistence.getRepositoryId(), this.modelId,
		                null, null)).getObject(this.objectId);
	}
	
	public boolean hasField(XID fieldId) {
		return getObjectSnapshot().hasField(fieldId);
	}
	
	public boolean isEmpty() {
		return getObjectSnapshot().isEmpty();
	}
	
	public Iterator<XID> iterator() {
		return this.getObjectSnapshot().iterator();
	}
	
	public boolean removeField(XID fieldId) {
		boolean result = hasField(fieldId);
		XCommand command = X.getCommandFactory().createRemoveFieldCommand(
		        this.persistence.getRepositoryId(), this.modelId, this.objectId, fieldId,
		        XCommand.FORCED, false);
		long commandResult = this.persistence.executeCommand(this.executingActorId, command);
		assert commandResult >= 0;
		return result;
	}
	
}
