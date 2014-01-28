/**
 * 
 */
package org.xydra.store.impl.gae.execute;

import java.util.concurrent.Future;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.XX;
import org.xydra.core.model.XModel;
import org.xydra.store.impl.gae.changes.GaeLocks;
import org.xydra.store.impl.gae.changes.IGaeChangesService;
import org.xydra.store.impl.gae.changes.KeyStructure;
import org.xydra.xgae.XGae;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;



/**
 * Internal helper class used by {@link IGaeChangesService} to access the
 * current model state.
 * 
 * @author dscharrer
 * 
 */
class InternalGaeModel extends InternalGaeContainerXEntity<InternalGaeObject> implements
        XReadableModel {
    
    private InternalGaeModel(IGaeChangesService changesService, XAddress modelAddr, long modelRev,
            GaeLocks locks) {
        super(changesService, modelAddr, modelRev, locks);
        assert modelAddr.getAddressedType() == XType.XMODEL;
    }
    
    @Override
    public XId getId() {
        return getAddress().getModel();
    }
    
    @Override
    public XReadableObject getObject(XId objectId) {
        return getChild(objectId);
    }
    
    @Override
    public boolean hasObject(XId objectId) {
        return hasChild(objectId);
    }
    
    @Override
    protected InternalGaeObject loadChild(XAddress childAddr, SEntity childEntity) {
        return new InternalGaeObject(getChangesService(), childAddr, childEntity, getLocks());
    }
    
    @Override
    protected XAddress resolveChild(XAddress addr, XId childId) {
        return XX.resolveObject(addr, childId);
    }
    
    /**
     * Get a read-only interface to an {@link XModel} in the GAE datastore.
     * 
     * @param changesService The changes service that manages the model to load.
     * @param modelRev The model revision to to be returned by
     *            {@link #getRevisionNumber()}.
     * @param locks The locks held by the current process. These are used to
     *            assert that we have enough locks when reading fields or
     *            objects as well as to determine if we can calculate object
     *            revisions or if we have to return
     *            {@link XEvent#REVISION_NOT_AVAILABLE} instead.
     * @return A {@link XModel} interface or null if the model doesn't exist.
     */
    static InternalGaeModel get(IGaeChangesService changesService, long modelRev, GaeLocks locks) {
        
        assert locks.canRead(changesService.getModelAddress());
        SEntity e = XGae.get().datastore().sync().getEntity(
                KeyStructure.createEntityKey(changesService.getModelAddress()));
        if(e == null) {
            return null;
        }
        
        XAddress modelAddr = changesService.getModelAddress();
        
        return new InternalGaeModel(changesService, modelAddr, modelRev, locks);
    }
    
    /**
     * Create an {@link XModel} in the GAE datastore.
     * 
     * It is up to the caller to acquire enough locks: The whole {@link XModel}
     * needs to be locked while adding it.
     * 
     * @param modelAddr The address of the model to add.
     * @param locks The locks held by the current process. These are used to
     *            assert that we are actually allowed to create the
     *            {@link XModel}.
     */
    static Future<SKey> createModel(XAddress modelAddr, GaeLocks locks) {
        assert locks.canWrite(modelAddr);
        assert modelAddr.getAddressedType() == XType.XMODEL;
        SEntity e = XGae.get().datastore().createEntity(KeyStructure.createEntityKey(modelAddr));
        return XGae.get().datastore().async().putEntity(e);
    }
    
    @Override
    public XType getType() {
        return XType.XMODEL;
    }
    
}
