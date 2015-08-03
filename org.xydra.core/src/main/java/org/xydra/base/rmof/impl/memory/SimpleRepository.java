package org.xydra.base.rmof.impl.memory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XCompareUtils;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.base.rmof.impl.XExistsRevWritableRepository;
import org.xydra.sharedutils.XyAssert;


/**
 * A simple data container for {@link XWritableRepository}.
 *
 * Minimal memory footprint, can be used as data transfer object.
 *
 * @author xamde
 */
public class SimpleRepository extends SimpleEntity implements XExistsRevWritableRepository {

    private static final long serialVersionUID = 5593443685935758227L;

    // not final for GWT serialisation
    private XAddress address;

    // not final for GWT serialisation
    private final Map<XId,XExistsRevWritableModel> models = new HashMap<XId,XExistsRevWritableModel>();

    /* Just for GWT */
    protected SimpleRepository() {
    }

    public SimpleRepository(final XAddress address) {
        XyAssert.xyAssert(address.getAddressedType() == XType.XREPOSITORY);
        this.address = address;
    }

    @Override
    public XExistsRevWritableModel createModel(final XId modelId) {
        final XExistsRevWritableModel model = this.models.get(modelId);
        if(model != null) {
            return model;
        }
        final SimpleModel newModel = new SimpleModel(Base.resolveModel(this.address, modelId));
        this.models.put(modelId, newModel);
        return newModel;
    }

    @Override
    public XAddress getAddress() {
        return this.address;
    }

    @Override
    public XId getId() {
        return this.address.getRepository();
    }

    @Override
    public XExistsRevWritableModel getModel(final XId modelId) {
        return this.models.get(modelId);
    }

    @Override
    public boolean hasModel(final XId modelId) {
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
    public boolean removeModel(final XId modelId) {
        if(this.models.remove(modelId) != null) {
            return true;
        }
        return false;
    }

    @Override
    public void addModel(final XExistsRevWritableModel model) {
        this.models.put(model.getId(), model);
    }

    @Override
    public XType getType() {
        return XType.XREPOSITORY;
    }

    /** Always returns 0 */
    @Override
    public long getRevisionNumber() {
        return 0;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof XReadableRepository
                && XCompareUtils.equalState(this, (XReadableRepository)other);
    }

}
