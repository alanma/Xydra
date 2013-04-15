package org.xydra.base.rmof.impl.memstate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XCompareUtils;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.rmof.XStateReadableRepository;
import org.xydra.base.rmof.XStateWritableModel;
import org.xydra.base.rmof.XStateWritableRepository;
import org.xydra.sharedutils.XyAssert;


/**
 * A simple data container for {@link XStateWritableRepository}.
 * 
 * Minimal memory footprint, can be used as data transfer object.
 * 
 * @author voelkel
 */
public class MemStateRepository extends MemStateEntity implements XStateWritableRepository {
    
    private static final long serialVersionUID = 5593443685935758227L;
    
    // not final for GWT serialisation
    private Map<XId,XStateWritableModel> models = new HashMap<XId,XStateWritableModel>();
    
    /* Just for GWT */
    protected MemStateRepository() {
    }
    
    public MemStateRepository(XAddress address) {
        super(address);
        XyAssert.xyAssert(address.getAddressedType() == XType.XREPOSITORY);
    }
    
    @Override
    public XStateWritableModel createModel(XId modelId) {
        XStateWritableModel model = this.models.get(modelId);
        if(model != null) {
            return model;
        }
        MemStateModel newModel = new MemStateModel(Base.resolveModel(getAddress(), modelId));
        addModel(newModel);
        return newModel;
    }
    
    @Override
    public XId getId() {
        return getAddress().getRepository();
    }
    
    @Override
    public XStateWritableModel getModel(XId modelId) {
        return this.models.get(modelId);
    }
    
    @Override
    public boolean hasModel(XId modelId) {
        return this.models.containsKey(modelId);
    }
    
    @Override
    public boolean isEmpty() {
        return this.models.isEmpty();
    }
    
    @Override
    public Iterator<XId> iterator() {
        return this.models.keySet().iterator();
    }
    
    @Override
    public boolean removeModel(XId modelId) {
        if(this.models.remove(modelId) != null) {
            return true;
        }
        return false;
    }
    
    private void addModel(XStateWritableModel model) {
        this.models.put(model.getId(), model);
    }
    
    @Override
    public XType getType() {
        return XType.XREPOSITORY;
    }
    
    @Override
    public boolean equals(Object other) {
        return other instanceof XStateReadableRepository
                && XCompareUtils.equalTree(this, (XStateReadableRepository)other);
    }
    
}
