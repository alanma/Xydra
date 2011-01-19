package org.xydra.store.base;

import org.xydra.base.HalfWritableUtils;
import org.xydra.base.XAddress;
import org.xydra.base.XHalfWritableField;
import org.xydra.base.XID;
import org.xydra.base.value.XValue;
import org.xydra.core.X;
import org.xydra.store.impl.delegate.XydraPersistence;


public class HalfWritableFieldOnPersistence extends AbstractHalfWritableOnPersistence implements
        XHalfWritableField {
	
	private XID modelId;
	
	private XID objectId;
	
	private XID fieldId;
	
	public HalfWritableFieldOnPersistence(XydraPersistence persistence, XID executingActorId,
	        XID modelId, XID objectId, XID fieldId) {
		super(persistence, executingActorId);
		this.modelId = modelId;
		this.objectId = objectId;
		this.fieldId = fieldId;
	}
	
	@Override
	public XValue getValue() {
		return getFieldSnapshot().getValue();
	}
	
	private XHalfWritableField getFieldSnapshot() {
		return this.persistence
		        .getModelSnapshot(
		                X.getIDProvider().fromComponents(this.persistence.getRepositoryId(),
		                        this.modelId, null, null)).getObject(this.objectId)
		        .getField(this.fieldId);
	}
	
	@Override
	public long getRevisionNumber() {
		return getFieldSnapshot().getRevisionNumber();
	}
	
	@Override
	public boolean isEmpty() {
		return getValue() == null;
	}
	
	@Override
	public XAddress getAddress() {
		if(this.address == null) {
			this.address = X.getIDProvider().fromComponents(this.persistence.getRepositoryId(),
			        this.modelId, this.objectId, this.fieldId);
		}
		return this.address;
	}
	
	@Override
	public XID getID() {
		return this.fieldId;
	}
	
	@Override
	public boolean setValue(XValue value) {
		// set in persistence
		HalfWritableRepositoryOnPersistence repository = new HalfWritableRepositoryOnPersistence(
		        this.persistence, this.executingActorId);
		return HalfWritableUtils.setValue(repository, this.modelId, this.objectId, this.fieldId,
		        value);
	}
	
}
