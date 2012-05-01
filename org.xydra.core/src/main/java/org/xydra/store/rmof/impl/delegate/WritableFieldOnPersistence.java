package org.xydra.store.rmof.impl.delegate;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.value.XValue;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.GetWithAddressRequest;
import org.xydra.store.impl.delegate.XydraPersistence;


public class WritableFieldOnPersistence extends AbstractWritableOnPersistence implements
        XWritableField {
	
	private static final Logger log = LoggerFactory.getLogger(WritableFieldOnPersistence.class);
	
	private XID fieldId;
	
	private XID modelId;
	
	private XID objectId;
	
	public WritableFieldOnPersistence(XydraPersistence persistence, XID executingActorId,
	        XID modelId, XID objectId, XID fieldId) {
		super(persistence, executingActorId);
		this.modelId = modelId;
		this.objectId = objectId;
		this.fieldId = fieldId;
	}
	
	private boolean changeValueTo(XValue value) {
		XCommand command;
		if(value == null) {
			command = X.getCommandFactory().createRemoveValueCommand(
			        this.persistence.getRepositoryId(), this.modelId, this.objectId, this.fieldId,
			        XCommand.FORCED, true);
		} else {
			command = X.getCommandFactory().createChangeValueCommand(
			        this.persistence.getRepositoryId(), this.modelId, this.objectId, this.fieldId,
			        XCommand.FORCED, value, true);
		}
		long result = this.persistence.executeCommand(this.executingActorId, command);
		if(result == XCommand.FAILED) {
			// TODO throw exception?
			log.warn("Could not execute command " + command);
			return false;
		}
		if(result == XCommand.NOCHANGE) {
			log.info("Command made no change " + command);
			return false;
		}
		XyAssert.xyAssert(result >= 0);
		return true;
	}
	
	@Override
	public XAddress getAddress() {
		if(this.address == null) {
			this.address = X.getIDProvider().fromComponents(this.persistence.getRepositoryId(),
			        this.modelId, this.objectId, this.fieldId);
		}
		return this.address;
	}
	
	private XWritableField getFieldSnapshot() {
		return this.persistence
		        .getModelSnapshot(
		                new GetWithAddressRequest(X.getIDProvider().fromComponents(
		                        this.persistence.getRepositoryId(), this.modelId, null, null),
		                        WritableRepositoryOnPersistence.USE_TENTATIVE_STATE))
		        .getObject(this.objectId).getField(this.fieldId);
	}
	
	@Override
	public XID getId() {
		return this.fieldId;
	}
	
	@Override
	public long getRevisionNumber() {
		return getFieldSnapshot().getRevisionNumber();
	}
	
	@Override
	public XValue getValue() {
		return getFieldSnapshot().getValue();
	}
	
	@Override
	public boolean isEmpty() {
		return getValue() == null;
	}
	
	@Override
	public boolean setValue(XValue value) {
		XValue currentValue = getValue();
		if(currentValue == null) {
			setValueInitially(value);
			return true;
		} else {
			return changeValueTo(value);
		}
		
	}
	
	private void setValueInitially(XValue value) {
		if(value == null) {
			return;
		}
		XCommand command = X.getCommandFactory().createAddValueCommand(
		        this.persistence.getRepositoryId(), this.modelId, this.objectId, this.fieldId,
		        XCommand.FORCED, value, true);
		long result = this.persistence.executeCommand(this.executingActorId, command);
		if(result == XCommand.FAILED) {
			throw new RuntimeException("Could not execute set-value-initially for value " + value
			        + ". Result was " + result);
		}
		if(result == XCommand.NOCHANGE) {
			throw new AssertionError(
			        "How can the command to set a null value to something not work?");
		}
		XyAssert.xyAssert(result >= 0);
	}
	
	@Override
	public XType getType() {
		return XType.XFIELD;
	}
	
}
