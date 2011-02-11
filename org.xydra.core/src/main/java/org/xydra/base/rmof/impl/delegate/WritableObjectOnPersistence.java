package org.xydra.base.rmof.impl.delegate;

import java.util.Iterator;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.store.impl.delegate.XydraPersistence;


public class WritableObjectOnPersistence extends AbstractWritableOnPersistence implements
        XWritableObject {
	
	private XID modelId;
	
	private XID objectId;
	
	public WritableObjectOnPersistence(XydraPersistence persistence, XID executingActorId,
	        XID modelId, XID objectId) {
		super(persistence, executingActorId);
		this.modelId = modelId;
		this.objectId = objectId;
	}
	
	public XWritableField createField(XID fieldId) {
		// assume model and object exist
		XWritableField field = this.getField(fieldId);
		if(field != null) {
			return field;
		}
		// else: create in persistence
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
	
	public XWritableField getField(XID fieldId) {
		if(hasField(fieldId)) {
			// make sure changes to object are reflected in persistence
			return new WritableFieldOnPersistence(this.persistence, this.executingActorId,
			        this.modelId, this.objectId, fieldId);
		} else {
			return null;
		}
	}
	
	@Override
	public XID getID() {
		return this.objectId;
	}
	
	private XWritableObject getObjectSnapshot() {
		return this.persistence.getModelSnapshot(
		        X.getIDProvider().fromComponents(this.persistence.getRepositoryId(), this.modelId,
		                null, null)).getObject(this.objectId);
	}
	
	public long getRevisionNumber() {
		return getObjectSnapshot().getRevisionNumber();
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
