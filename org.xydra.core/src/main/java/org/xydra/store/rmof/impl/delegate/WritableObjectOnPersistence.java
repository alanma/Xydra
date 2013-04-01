package org.xydra.store.rmof.impl.delegate;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.X;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.XydraPersistence;
import org.xydra.sharedutils.XyAssert;


public class WritableObjectOnPersistence extends AbstractWritableOnPersistence implements
        XWritableObject {
	
	private XId modelId;
	
	private XId objectId;
	
	public WritableObjectOnPersistence(XydraPersistence persistence, XId executingActorId,
	        XId modelId, XId objectId) {
		super(persistence, executingActorId);
		this.modelId = modelId;
		this.objectId = objectId;
	}
	
	@Override
	public XWritableField createField(XId fieldId) {
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
	public XWritableField getField(XId fieldId) {
		if(hasField(fieldId)) {
			// make sure changes to object are reflected in persistence
			return new WritableFieldOnPersistence(this.persistence, this.executingActorId,
			        this.modelId, this.objectId, fieldId);
		} else {
			return null;
		}
	}
	
	@Override
	public XId getId() {
		return this.objectId;
	}
	
	/**
	 * @return always a fresh snapshot from the {@link XydraPersistence}
	 */
	private XWritableObject getObjectSnapshot() {
		// TODO test and delete old impl
		return this.persistence.getObjectSnapshot(new GetWithAddressRequest(this.getAddress(),
		        WritableRepositoryOnPersistence.USE_TENTATIVE_STATE));
		
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
	public boolean hasField(XId fieldId) {
		XWritableObject snapshot = getObjectSnapshot();
		return snapshot != null && snapshot.hasField(fieldId);
	}
	
	@Override
	public boolean isEmpty() {
		XWritableObject snapshot = getObjectSnapshot();
		return snapshot == null || snapshot.isEmpty();
	}
	
	@Override
	public Iterator<XId> iterator() {
		XWritableObject snapshot = getObjectSnapshot();
		return snapshot == null ? new NoneIterator<XId>() : snapshot.iterator();
	}
	
	@Override
	public boolean removeField(XId fieldId) {
		boolean result = hasField(fieldId);
		XCommand command = X.getCommandFactory().createRemoveFieldCommand(
		        this.persistence.getRepositoryId(), this.modelId, this.objectId, fieldId,
		        XCommand.FORCED, true);
		long commandResult = this.persistence.executeCommand(this.executingActorId, command);
		XyAssert.xyAssert(commandResult >= 0);
		return result;
	}
	
	public XId getModelId() {
		return this.modelId;
	}
	
	public XId getObjectId() {
		return this.objectId;
	}
	
	@Override
	public XType getType() {
		return XType.XOBJECT;
	}
}
