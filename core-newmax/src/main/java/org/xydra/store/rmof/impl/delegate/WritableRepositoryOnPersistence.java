package org.xydra.store.rmof.impl.delegate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.persistence.XydraPersistence;
import org.xydra.sharedutils.XyAssert;


/**
 * An implementation of {@link XWritableRepository} that uses no internal
 * caching.
 * 
 * @author xamde
 */
public class WritableRepositoryOnPersistence extends AbstractWritableOnPersistence implements
        XWritableRepository {
    
    public static final boolean USE_TENTATIVE_STATE = false;
    
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory
            .getLogger(WritableRepositoryOnPersistence.class);
    
    public WritableRepositoryOnPersistence(XydraPersistence persistence, XId executingActorId) {
        super(persistence, executingActorId);
    }
    
    /** First try to get model, if not found: create it. */
    @Override
    public XWritableModel createModel(XId modelId) {
        XWritableModel model = getModel(modelId);
        if(model == null) {
            ModelRevision modelRev = this.persistence.getModelRevision(new GetWithAddressRequest(
                    getModelAddress(modelId), USE_TENTATIVE_STATE));
            XyAssert.xyAssert(modelRev != null);
            assert modelRev != null;
            XyAssert.xyAssert(!modelRev.modelExists(), "modelExists should be false but rev is "
                    + modelRev);
            
            XCommand command = X.getCommandFactory().createAddModelCommand(
                    this.persistence.getRepositoryId(), modelId, true);
            long l = this.persistence.executeCommand(this.executingActorId, command);
            
            XyAssert.xyAssert(l >= 0, "creating model '" + modelId + "' failed with " + l);
            
            XyAssert.xyAssert(this.persistence.hasManagedModel(modelId));
            XyAssert.xyAssert(hasModel(modelId));
            model = getModel(modelId);
            XyAssert.xyAssert(model != null, "model == null");
            assert model != null;
        }
        return model;
    }
    
    private XAddress getModelAddress(XId modelId) {
        return X.getIDProvider().fromComponents(this.persistence.getRepositoryId(), modelId, null,
                null);
    }
    
    @Override
    public XAddress getAddress() {
        if(this.address == null) {
            this.address = X.getIDProvider().fromComponents(this.persistence.getRepositoryId(),
                    null, null, null);
        }
        return this.address;
    }
    
    @Override
    public XId getId() {
        return this.persistence.getRepositoryId();
    }
    
    @Override
    public XWritableModel getModel(XId modelId) {
        if(hasModel(modelId)) {
            // make sure changes to model are reflected in persistence
            return new WritableModelOnPersistence(this.persistence, this.executingActorId, modelId);
        } else {
            return null;
        }
    }
    
    @Override
    public boolean hasModel(XId modelId) {
        XyAssert.xyAssert(this.persistence != null);
        assert this.persistence != null;
        if(!this.persistence.hasManagedModel(modelId))
            return false;
        
        ModelRevision info = this.persistence.getModelRevision(new GetWithAddressRequest(XX
                .resolveModel(this.persistence.getRepositoryId(), modelId), USE_TENTATIVE_STATE));
        return info.modelExists();
    }
    
    @Override
    public boolean isEmpty() {
        return this.persistence.getManagedModelIds().isEmpty();
    }
    
    @Override
    public Iterator<XId> iterator() {
        Set<XId> existingModelIds = new HashSet<XId>();
        for(XId modelId : this.persistence.getManagedModelIds()) {
            if(this.persistence.getModelRevision(
                    new GetWithAddressRequest(XX.resolveModel(this.persistence.getRepositoryId(),
                            modelId), USE_TENTATIVE_STATE)).modelExists()) {
                existingModelIds.add(modelId);
            }
        }
        return existingModelIds.iterator();
    }
    
    @Override
    public boolean removeModel(XId modelId) {
        boolean result = hasModel(modelId);
        // long modelRevision =
        // this.persistence.getModelRevision(XX.resolveModel(getAddress(),
        // modelId));
        XCommand command = X.getCommandFactory().createRemoveModelCommand(
                this.persistence.getRepositoryId(), modelId, XCommand.FORCED, true);
        long commandResult = this.persistence.executeCommand(this.executingActorId, command);
        assert commandResult >= 0 : commandResult;
        return result;
    }
    
    @Override
    public XType getType() {
        return XType.XREPOSITORY;
    }
    
}
