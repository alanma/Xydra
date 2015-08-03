package org.xydra.store.rmof.impl.delegate;

import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.NeverNull;
import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.persistence.XydraPersistence;
import org.xydra.sharedutils.XyAssert;


/**
 * Delegates all reads and writes to a {@link XydraPersistence}. No caching is
 * used.
 *
 * Additionally you can retrieve all events that happened since the revision of
 * this entity since the last call of {@link #ignoreAllEventsUntilNow()}.
 *
 * @author xamde
 */
public class WritableModelOnPersistence extends AbstractWritableOnPersistence implements
        XWritableModel {

    private static final Logger log = LoggerFactory.getLogger(WritableModelOnPersistence.class);

    private long lastRev = 0;

    private final XId modelId;

    public WritableModelOnPersistence(final XydraPersistence persistence, final XId executingActorId,
            final XId modelId) {
        super(persistence, executingActorId);
        this.modelId = modelId;
    }

    @Override
    public XWritableObject createObject(@NeverNull final XId objectId) {
        XyAssert.xyAssert(this.persistence.hasManagedModel(this.modelId));

        final XWritableObject object = getObject(objectId);
        if(object != null) {
            return object;
        }
        // create in persistence
        final XCommand command = BaseRuntime.getCommandFactory().createAddObjectCommand(getAddress(),
                objectId, true);
        final long commandResult = this.persistence.executeCommand(this.executingActorId, command);
        assert commandResult >= 0 : "Command " + command + " failed with " + commandResult;
        return getObject(objectId);
    }

    @Override
    public XAddress getAddress() {
        if(this.address == null) {
            this.address = BaseRuntime.getIDProvider().fromComponents(this.persistence.getRepositoryId(),
                    this.modelId, null, null);
        }
        return this.address;
    }

    @Override
    public XId getId() {
        return this.modelId;
    }

    /**
     * @return an array of XEvent or null if there are no events. The array
     *         contains all events that happened in the underlying
     *         {@link XydraPersistence} since the last call of this method.
     */
    public List<XEvent> getNewEvents() {
        // get highestEvent
        final ModelRevision rs = this.persistence.getModelRevision(new GetWithAddressRequest(getAddress(), WritableRepositoryOnPersistence.USE_TENTATIVE_STATE));
        final long currentRev = rs.revision();
        List<XEvent> events = null;
        if(currentRev > this.lastRev) {
            // get event between lastProcessed and highest
            events = this.persistence.getEvents(getAddress(), this.lastRev, currentRev);
        }
        this.lastRev = currentRev;
        return events;
    }

    @Override
    public XWritableObject getObject(@NeverNull final XId objectId) {
        if(hasObject(objectId)) {
            // make sure changes to object are reflected in persistence
            return new WritableObjectOnPersistence(this.persistence, this.executingActorId,
                    this.modelId, objectId);
        } else {
            return null;
        }
    }

    @Override
    public long getRevisionNumber() {
        final XWritableModel snapshot = this.persistence.getModelSnapshot(new GetWithAddressRequest(getAddress(), WritableRepositoryOnPersistence.USE_TENTATIVE_STATE));
        if(snapshot == null) {
            log.warn("Modelsnapshot for " + getAddress() + " is null");
            return XCommand.NEW;
        }
        return snapshot.getRevisionNumber();
    }

    @Override
    public boolean hasObject(@NeverNull final XId objectId) {
        final XAddress objectAddress = Base.resolveObject(getAddress(), objectId);
        final XWritableObject objectSnapshot = this.persistence
                .getObjectSnapshot(new GetWithAddressRequest(objectAddress,
                        WritableRepositoryOnPersistence.USE_TENTATIVE_STATE));
        if(objectSnapshot == null) {
            return false;
        }
        return true;
    }

    /**
     * Allows load() methods to init this model correctly.
     */
    public void ignoreAllEventsUntilNow() {
        this.lastRev = this.persistence.getModelRevision(
                new GetWithAddressRequest(getAddress(),
                        WritableRepositoryOnPersistence.USE_TENTATIVE_STATE)).revision();
    }

    @Override
    public boolean isEmpty() {
        return this.persistence.getModelSnapshot(
                new GetWithAddressRequest(getAddress(),
                        WritableRepositoryOnPersistence.USE_TENTATIVE_STATE)).isEmpty();
    }

    @Override
    public Iterator<XId> iterator() {
        final XWritableModel modelSnapshot = this.persistence.getModelSnapshot(new GetWithAddressRequest(
                getAddress(), WritableRepositoryOnPersistence.USE_TENTATIVE_STATE));
        if(modelSnapshot == null || modelSnapshot.isEmpty()) {
            return NoneIterator.create();
        }
        return modelSnapshot.iterator();
    }

    @Override
    public boolean removeObject(@NeverNull final XId objectId) {
        final boolean result = hasObject(objectId);
        final XCommand command = BaseRuntime.getCommandFactory().createRemoveObjectCommand(
                this.persistence.getRepositoryId(), this.modelId, objectId, XCommand.FORCED, true);
        final long commandResult = this.persistence.executeCommand(this.executingActorId, command);
        XyAssert.xyAssert(commandResult >= 0);
        return result && commandResult >= 0;
    }

    @Override
    public XType getType() {
        return XType.XMODEL;
    }

}
