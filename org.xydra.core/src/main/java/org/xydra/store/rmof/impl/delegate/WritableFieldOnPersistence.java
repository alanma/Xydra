package org.xydra.store.rmof.impl.delegate;

import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.value.XValue;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.XydraPersistence;
import org.xydra.sharedutils.XyAssert;


public class WritableFieldOnPersistence extends AbstractWritableOnPersistence implements
        XWritableField {

    private static final Logger log = LoggerFactory.getLogger(WritableFieldOnPersistence.class);

    private final XId fieldId;

    private final XId modelId;

    private final XId objectId;

    public WritableFieldOnPersistence(final XydraPersistence persistence, final XId executingActorId,
            final XId modelId, final XId objectId, final XId fieldId) {
        super(persistence, executingActorId);
        this.modelId = modelId;
        this.objectId = objectId;
        this.fieldId = fieldId;
    }

    private boolean changeValueTo(final XValue value) {
        XCommand command;
        if(value == null) {
            command = BaseRuntime.getCommandFactory().createRemoveValueCommand(
                    this.persistence.getRepositoryId(), this.modelId, this.objectId, this.fieldId,
                    XCommand.FORCED, true);
        } else {
            command = BaseRuntime.getCommandFactory().createChangeValueCommand(
                    Base.resolveField(this.persistence.getRepositoryId(), this.modelId,
                            this.objectId, this.fieldId), XCommand.FORCED, value, true);
        }
        final long result = this.persistence.executeCommand(this.executingActorId, command);
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
            this.address = BaseRuntime.getIDProvider().fromComponents(this.persistence.getRepositoryId(),
                    this.modelId, this.objectId, this.fieldId);
        }
        return this.address;
    }

    private XWritableField getFieldSnapshot() {
        return this.persistence.getObjectSnapshot(
                new GetWithAddressRequest(BaseRuntime.getIDProvider().fromComponents(
                        this.persistence.getRepositoryId(), this.modelId, this.objectId, null),
                        WritableRepositoryOnPersistence.USE_TENTATIVE_STATE))
                .getField(this.fieldId);
    }

    @Override
    public XId getId() {
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
    public boolean setValue(final XValue value) {
        final XValue currentValue = getValue();
        if(currentValue == null) {
            setValueInitially(value);
            return true;
        } else {
            return changeValueTo(value);
        }

    }

    private void setValueInitially(final XValue value) {
        if(value == null) {
            return;
        }
        final XCommand command = BaseRuntime.getCommandFactory().createAddValueCommand(getAddress(),
                XCommand.FORCED, value, true);
        final long result = this.persistence.executeCommand(this.executingActorId, command);
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
