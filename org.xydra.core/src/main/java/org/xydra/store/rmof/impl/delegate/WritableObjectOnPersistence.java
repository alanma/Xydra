package org.xydra.store.rmof.impl.delegate;

import java.util.Iterator;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.index.iterator.NoneIterator;
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
	
	@Override
	public XWritableField createField(XID fieldId) {
		// assume model and object exist
		XWritableField field = this.getField(fieldId);
		if(field != null) {
			return field;
		}
		// else: create in persistence
		XCommand command = X.getCommandFactory().createAddFieldCommand(
		        this.persistence.getRepositoryId(), this.modelId, this.objectId, fieldId, true);
		this.persistence.executeCommand(this.executingActorId, command);
		return getField(fieldId);
	}
	
	@Override
	public XAddress getAddress() {
		/* cache object after construction */
		if(this.address == null) {
			this.address = X.getIDProvider().fromComponents(this.persistence.getRepositoryId(),
			        this.modelId, this.objectId, null);
		}
		return this.address;
	}
	
	@Override
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
	
	/**
	 * @return always a fresh snapshot from the {@link XydraPersistence}
	 */
	private XWritableObject getObjectSnapshot() {
		// TODO test and delete old impl
		return this.persistence.getObjectSnapshot(this.getAddress());
		
		/* old, slower but working impl */
		// return this.persistence.getModelSnapshot(
		// X.getIDProvider().fromComponents(this.persistence.getRepositoryId(),
		// this.modelId,
		// null, null)).getObject(this.objectId);
	}
	
	@Override
	public long getRevisionNumber() {
		return getObjectSnapshot().getRevisionNumber();
	}
	
	@Override
	public boolean hasField(XID fieldId) {
		XWritableObject snapshot = getObjectSnapshot();
		return snapshot != null && snapshot.hasField(fieldId);
	}
	
	@Override
	public boolean isEmpty() {
		XWritableObject snapshot = getObjectSnapshot();
		return snapshot == null || snapshot.isEmpty();
	}
	
	@Override
	public Iterator<XID> iterator() {
		XWritableObject snapshot = getObjectSnapshot();
		return snapshot == null ? new NoneIterator<XID>() : snapshot.iterator();
	}
	
	@Override
	public boolean removeField(XID fieldId) {
		boolean result = hasField(fieldId);
		XCommand command = X.getCommandFactory().createRemoveFieldCommand(
		        this.persistence.getRepositoryId(), this.modelId, this.objectId, fieldId,
		        XCommand.FORCED, true);
		long commandResult = this.persistence.executeCommand(this.executingActorId, command);
		assert commandResult >= 0;
		return result;
	}
	
	public XID getModelId() {
		return this.modelId;
	}
	
	public XID getObjectId() {
		return this.objectId;
	}
	
	@Override
	public XType getType() {
		return XType.XOBJECT;
	}
}
