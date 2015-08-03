package org.xydra.store;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.StoreException;
import org.xydra.core.X;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.persistence.GetEventsRequest;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.persistence.XydraPersistence;
import org.xydra.sharedutils.XyAssert;


/**
 * Makes it easier to use a remote {@link XydraStore} by using it like a
 * {@link XydraPersistence}. <em>Makes only sense for tests.</em>
 *
 * @author xamde
 */
public class PersistenceOnStore implements XydraPersistence {

    /** Default wait time for callback */
    private static final int WAIT_TIMEOUT = 2000;

    public PersistenceOnStore(final XId actorId, final String passwordHash, final XydraStore store) {
        super();
        this.actorId = actorId;
        this.passwordHash = passwordHash;
        this.store = store;
    }

    private final XydraStore store;

    private final XId actorId;

    private final String passwordHash;

    private XId repositoryId;

    @Override
    public void clear() {
        for(final XId modelId : getManagedModelIds()) {
            deleteModel(modelId);
        }
    }

    private void deleteModel(final XId modelId) {
        executeCommand(this.actorId,
                BaseRuntime.getCommandFactory()
                        .createRemoveModelCommand(getRepositoryId(), modelId, -1, true));
    }

    @Override
    public long executeCommand(final XId actorId, final XCommand command) {
        final SynchronousCallbackWithOneResult<BatchedResult<Long>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<Long>[]>();
        this.store
                .executeCommands(actorId, this.passwordHash, new XCommand[] { command }, callback);
        callback.waitOnCallbackAndThrowExceptionForProblems(WAIT_TIMEOUT);
        {
            Throwable e = callback.getException();
            if(e != null) {
                throw new StoreException("Callback returned an error", e);
            }
            XyAssert.xyAssert(callback.getEffect().length == 1);
            e = callback.getEffect()[0].getException();
            if(e != null) {
                throw new StoreException("Callback effect[0] returned an error", e);
            }
        }
        return callback.getEffect()[0].getResult();
    }

    @Override
    public List<XEvent> getEvents(final XAddress address, final long beginRevision, final long endRevision) {
        final SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
        final GetEventsRequest ger = new GetEventsRequest(address, beginRevision, endRevision);
        this.store.getEvents(this.actorId, this.passwordHash, new GetEventsRequest[] { ger },
                callback);
        callback.waitOnCallbackAndThrowExceptionForProblems(WAIT_TIMEOUT);
        {
            Throwable e = callback.getException();
            if(e != null) {
                throw new StoreException("Callback returned an error", e);
            }
            XyAssert.xyAssert(callback.getEffect().length == 1);
            e = callback.getEffect()[0].getException();
            if(e != null) {
                throw new StoreException("Callback effect[0] returned an error", e);
            }
        }

        return Arrays.asList(callback.getEffect()[0].getResult());
    }

    @Override
    public Set<XId> getManagedModelIds() {
        final SynchronousCallbackWithOneResult<Set<XId>> callback = new SynchronousCallbackWithOneResult<Set<XId>>();
        this.store.getModelIds(this.actorId, this.passwordHash, callback);
        callback.waitOnCallbackAndThrowExceptionForProblems(WAIT_TIMEOUT);
        {
            final Throwable e = callback.getException();
            if(e != null) {
                throw new StoreException("Callback returned an error", e);
            }
        }
        return callback.getEffect();
    }

    @Override
    public ModelRevision getModelRevision(final GetWithAddressRequest addressRequest) {
        final SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]>();
        this.store.getModelRevisions(this.actorId, this.passwordHash,
                new GetWithAddressRequest[] { addressRequest }, callback);
        callback.waitOnCallbackAndThrowExceptionForProblems(WAIT_TIMEOUT);
        {
            Throwable e = callback.getException();
            if(e != null) {
                throw new StoreException("Callback returned an error", e);
            }
            XyAssert.xyAssert(callback.getEffect().length == 1);
            e = callback.getEffect()[0].getException();
            if(e != null) {
                throw new StoreException("Callback effect[0] returned an error", e);
            }
        }
        return callback.getEffect()[0].getResult();
    }

    @Override
    public XWritableModel getModelSnapshot(final GetWithAddressRequest addressRequest) {
        final SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();
        this.store.getModelSnapshots(this.actorId, this.passwordHash,
                new GetWithAddressRequest[] { addressRequest }, callback);
        callback.waitOnCallbackAndThrowExceptionForProblems(WAIT_TIMEOUT);
        {
            Throwable e = callback.getException();
            if(e != null) {
                throw new StoreException("Callback returned an error", e);
            }
            XyAssert.xyAssert(callback.getEffect().length == 1);
            e = callback.getEffect()[0].getException();
            if(e != null) {
                throw new StoreException("Callback effect[0] returned an error", e);
            }
        }
        final XReadableModel readableModel = callback.getEffect()[0].getResult();
        return XCopyUtils.createSnapshot(readableModel);
    }

    @Override
    public XWritableObject getObjectSnapshot(final GetWithAddressRequest addressRequest) {
        final SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();
        this.store.getObjectSnapshots(this.actorId, this.passwordHash,
                new GetWithAddressRequest[] { addressRequest }, callback);
        callback.waitOnCallbackAndThrowExceptionForProblems(WAIT_TIMEOUT);
        {
            Throwable e = callback.getException();
            if(e != null) {
                throw new StoreException("Callback returned an error", e);
            }
            XyAssert.xyAssert(callback.getEffect().length == 1);
            e = callback.getEffect()[0].getException();
            if(e != null) {
                throw new StoreException("Callback effect[0] returned an error", e);
            }
        }
        final XReadableObject readableObject = callback.getEffect()[0].getResult();
        return XCopyUtils.createSnapshot(readableObject);
    }

    @Override
    public XId getRepositoryId() {
        if(this.repositoryId == null) {
            final SynchronousCallbackWithOneResult<XId> callback = new SynchronousCallbackWithOneResult<XId>();
            this.store.getRepositoryId(this.actorId, this.passwordHash, callback);
            callback.waitOnCallbackAndThrowExceptionForProblems(WAIT_TIMEOUT);
            {
                final Throwable e = callback.getException();
                if(e != null) {
                    throw new StoreException("Callback returned an error", e);
                }
            }
            this.repositoryId = callback.getEffect();
        }
        return this.repositoryId;
    }

    @Override
    public boolean hasManagedModel(final XId modelId) {
        return getModelRevision(new GetWithAddressRequest(modeladdress(modelId), false)) != null;
    }

    private XAddress modeladdress(final XId modelId) {
        return Base.toAddress(getRepositoryId(), modelId, null, null);
    }

}
