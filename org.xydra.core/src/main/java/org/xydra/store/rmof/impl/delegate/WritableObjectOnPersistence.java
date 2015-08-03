package org.xydra.store.rmof.impl.delegate;

import java.util.Iterator;

import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.X;
import org.xydra.index.iterator.Iterators;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.XydraPersistence;
import org.xydra.sharedutils.XyAssert;


public class WritableObjectOnPersistence extends AbstractWritableOnPersistence implements
        XWritableObject {

    private final XId modelId;

    private final XId objectId;

    public WritableObjectOnPersistence(final XydraPersistence persistence, final XId executingActorId,
            final XId modelId, final XId objectId) {
        super(persistence, executingActorId);
        this.modelId = modelId;
        this.objectId = objectId;
    }

    @Override
    public XWritableField createField(final XId fieldId) {
        // assume model and object exist
        final XWritableField field = getField(fieldId);
        if(field != null) {
            return field;
        }
        // else: create in persistence
        final XCommand command = BaseRuntime.getCommandFactory().createAddFieldCommand(getAddress(), fieldId, true);
        this.persistence.executeCommand(this.executingActorId, command);
        return getField(fieldId);
    }

    @Override
    public XAddress getAddress() {
        /* cache object after construction */
        if(this.address == null) {
            this.address = BaseRuntime.getIDProvider().fromComponents(this.persistence.getRepositoryId(),
                    this.modelId, this.objectId, null);
        }
        return this.address;
    }

    @Override
    public XWritableField getField(final XId fieldId) {
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
        return this.persistence.getObjectSnapshot(new GetWithAddressRequest(getAddress(),
                WritableRepositoryOnPersistence.USE_TENTATIVE_STATE));

    }

    @Override
    public long getRevisionNumber() {
        return getObjectSnapshot().getRevisionNumber();
    }

    @Override
    public boolean hasField(final XId fieldId) {
        final XWritableObject snapshot = getObjectSnapshot();
        return snapshot != null && snapshot.hasField(fieldId);
    }

    @Override
    public boolean isEmpty() {
        final XWritableObject snapshot = getObjectSnapshot();
        return snapshot == null || snapshot.isEmpty();
    }

    @Override
    public Iterator<XId> iterator() {
        final XWritableObject snapshot = getObjectSnapshot();
        if(snapshot==null) {
        	return Iterators.none();
        } else {
        	return snapshot.iterator();
        }
    }

    @Override
    public boolean removeField(final XId fieldId) {
        final boolean result = hasField(fieldId);
        final XCommand command = BaseRuntime.getCommandFactory().createRemoveFieldCommand(
                this.persistence.getRepositoryId(), this.modelId, this.objectId, fieldId,
                XCommand.FORCED, true);
        final long commandResult = this.persistence.executeCommand(this.executingActorId, command);
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
