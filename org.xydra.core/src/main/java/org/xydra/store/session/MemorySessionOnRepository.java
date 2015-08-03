package org.xydra.store.session;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.index.iterator.Iterators;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.sharedutils.XyAssert;


/**
 * Creates only read-only session models.
 *
 * @author xamde
 *
 */
public class MemorySessionOnRepository implements ISessionPersistence {

    private final XWritableRepository repo;

    public MemorySessionOnRepository(final XWritableRepository repo) {
        this.repo = repo;
    }

    @Override
    public long createModel(final XId modelId) {
        if(this.repo.hasModel(modelId)) {
            return XCommand.NOCHANGE;
        } else {
            final XWritableModel model = this.repo.createModel(modelId);
            final long rev = model.getRevisionNumber();
            return rev > 0 ? rev : 1;
        }
    }

    @Override
    public XId getRepositoryId() {
        return this.repo.getId();
    }

    @Override
    public long applyChangesAsTxn(final SessionCachedModel changedModel, final XId actorId) {
        final XWritableModel writableModel = this.repo.getModel(changedModel.getId());
        changedModel.commitTo(writableModel);
        return 1;
    }

    @Override
    public long removeModel(final XId modelId) {
        return this.repo.removeModel(modelId) ? 1 : XCommand.NOCHANGE;
    }

    public Collection<XReadableModel> getModelSnapshots(final XId ... modelIds) {
        final List<XReadableModel> result = new LinkedList<XReadableModel>();
        if(modelIds != null) {
            for(final XId modelId : modelIds) {
                final XWritableModel model = this.repo.getModel(modelId);
                if(model != null) {
                    result.add(model);
                }
            }
        }
        return result;
    }

    public Collection<XReadableObject> getObjectSnapshots(final XAddress ... objectAddresses) {
        final List<XReadableObject> result = new LinkedList<XReadableObject>();
        if(objectAddresses != null) {
            for(final XAddress objectAddress : objectAddresses) {
                final XWritableModel model = this.repo.getModel(objectAddress.getModel());
                if(model != null) {
                    final XWritableObject object = model.getObject(objectAddress.getObject());
                    result.add(object);
                }
            }
        }
        return result;
    }

    @Override
    public XReadableModel getModelSnapshot(final GetWithAddressRequest modelRequest) {
        XyAssert.xyAssert(modelRequest != null);
        assert modelRequest != null;
        return this.repo.getModel(modelRequest.address.getModel());
    }

    @Override
    public XReadableObject getObjectSnapshot(final GetWithAddressRequest objectAddressRequest) {
        XyAssert.xyAssert(objectAddressRequest != null);
        assert objectAddressRequest != null;
        final XWritableModel model = this.repo.getModel(objectAddressRequest.address.getModel());
        if(model == null) {
            return null;
        }
        return model.getObject(objectAddressRequest.address.getObject());
    }

    @Override
    public void deleteAllData() {
        final Collection<XId> modelIds = Iterators.addAll(this.repo.iterator(), new HashSet<XId>());
        for(final XId modelId : modelIds) {
            this.repo.removeModel(modelId);
        }
    }

}
