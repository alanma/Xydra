package org.xydra.store.impl.memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.core.XX;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.RequestException;


/**
 * Stores state in memory. Each model is managed as a
 * {@link MemoryModelPersistence}.
 * 
 * Tentative methods fall back to standard methods as there are no race
 * conditions of any kind.
 * 
 * @author xamde
 * @author dscharrer
 */

public class MemoryPersistence implements XydraPersistence {
    
    private Map<XId,MemoryModelPersistence> models = new HashMap<XId,MemoryModelPersistence>();
    
    private XId repoId;
    
    /**
     * This method is used to instantiate the persistence via reflection in
     * SharedXydraPersistence.
     * 
     * @param repositoryId repository ID
     */
    public MemoryPersistence(XId repositoryId) {
        this.repoId = repositoryId;
    }
    
    @Override
    public void clear() {
        synchronized(this.models) {
            this.models.clear();
        }
    }
    
    @Override
    public long executeCommand(XId actorId, XCommand command) {
        XAddress address = command.getChangedEntity();
        // caller asserts repoId matches address
        MemoryModelPersistence modelPersistence = getModelPersistence(address.getModel());
        long result = modelPersistence.executeCommand(actorId, command);
        
        /*
         * DO NOT remove the MemoryModelPersistence from the model map, even if
         * the model has been removed
         * 
         * even if the model has been deleted the event log must be kept. If the
         * model gets re-created later the revision number must strictly
         * increase to serve users who synchronised with the previous model.
         */
        
        return result;
    }
    
    @Override
    public List<XEvent> getEvents(XAddress address, long beginRevision, long endRevision) {
        // caller asserts repoId matches address
        return getModelPersistence(address.getModel()).getEvents(address, beginRevision,
                endRevision);
    }
    
    @Override
    public Set<XId> getManagedModelIds() {
        Set<XId> modelIds = new HashSet<XId>();
        synchronized(this.models) {
            for(Map.Entry<XId,MemoryModelPersistence> p : this.models.entrySet()) {
                if(p.getValue().exists()) {
                    modelIds.add(p.getKey());
                }
            }
        }
        return modelIds;
    }
    
    private MemoryModelPersistence getModelPersistence(XId modelId) {
        if(modelId == null) {
            throw new IllegalArgumentException("modelId must not be null");
        }
        synchronized(this.models) {
            MemoryModelPersistence modelPersistence = this.models.get(modelId);
            if(modelPersistence == null) {
                /* return a model persistence that does not exist */
                XAddress modelAddr = XX.toAddress(this.repoId, modelId, null, null);
                modelPersistence = new MemoryModelPersistence(modelAddr);
                this.models.put(modelId, modelPersistence);
            }
            return modelPersistence;
        }
    }
    
    @Override
    public ModelRevision getModelRevision(GetWithAddressRequest addressRequest) {
        XAddress address = addressRequest.address;
        if(address.getAddressedType() != XType.XMODEL) {
            throw new RequestException("must use a model address to get a model revison, was "
                    + address);
        }
        // caller asserts repoId matches address
        return getModelPersistence(address.getModel()).getModelRevision();
    }
    
    @Override
    public XRevWritableModel getModelSnapshot(GetWithAddressRequest addressRequest) {
        XAddress address = addressRequest.address;
        if(address.getAddressedType() != XType.XMODEL) {
            throw new RequestException("must use a model address to get a model snapshot, was "
                    + address);
        }
        // caller asserts repoId matches address
        return getModelPersistence(address.getModel()).getModelSnapshot();
    }
    
    @Override
    public XRevWritableObject getObjectSnapshot(GetWithAddressRequest addressRequest) {
        XAddress address = addressRequest.address;
        if(address.getAddressedType() != XType.XOBJECT) {
            throw new RequestException("must use an object address to get an object snapshot, was "
                    + address);
        }
        // caller asserts repoId matches address
        return getModelPersistence(address.getModel()).getObjectSnapshot(address.getObject());
    }
    
    @Override
    public XId getRepositoryId() {
        return this.repoId;
    }
    
    @Override
    public boolean hasManagedModel(XId modelId) {
        synchronized(this.models) {
            MemoryModelPersistence modelPersistence = this.models.get(modelId);
            return modelPersistence != null;
            // we don't check if the modelPersistence exists right now, that
            // would be too slow
        }
    }
    
}
