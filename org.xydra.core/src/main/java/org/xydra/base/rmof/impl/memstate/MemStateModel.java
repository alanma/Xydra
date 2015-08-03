package org.xydra.base.rmof.impl.memstate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.annotations.NeverNull;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XCompareUtils;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.rmof.XStateReadableModel;
import org.xydra.base.rmof.XStateWritableModel;
import org.xydra.base.rmof.XStateWritableObject;
import org.xydra.sharedutils.XyAssert;


/**
 * A simple data container for {@link XStateWritableModel}.
 *
 * Minimal memory footprint, can be used as data transfer object.
 *
 * @author xamde
 */
public class MemStateModel extends MemStateEntity implements XStateWritableModel {

    private static final long serialVersionUID = 5593443685935758227L;

    // not final for GWT serialisation
    private Map<XId,XStateWritableObject> objects;

    /* Just for GWT */
    protected MemStateModel() {
    }

    public MemStateModel(final XAddress address) {
        this(address, new HashMap<XId,XStateWritableObject>(2));
    }

    public MemStateModel(final XAddress address, final Map<XId,XStateWritableObject> objects) {
        super(address);
        assert address.getAddressedType() == XType.XMODEL : address;
        this.objects = objects;
    }

    private void addObject(@NeverNull final XStateWritableObject object) {
        XyAssert.xyAssert(object != null);
        assert object != null;
        this.objects.put(object.getId(), object);
    }

    @Override
    public XStateWritableObject createObject(@NeverNull final XId objectId) {
        final XStateWritableObject object = this.objects.get(objectId);
        if(object != null) {
            return object;
        }
        final XStateWritableObject newObject = new MemStateObject(Base.resolveObject(getAddress(),
                objectId));
        this.objects.put(objectId, newObject);
        return newObject;
    }

    @Override
    public XId getId() {
        return getAddress().getModel();
    }

    @Override
    public XStateWritableObject getObject(@NeverNull final XId objectId) {
        return this.objects.get(objectId);
    }

    @Override
    public boolean hasObject(@NeverNull final XId objectId) {
        return this.objects.containsKey(objectId);
    }

    @Override
    public boolean isEmpty() {
        return this.objects.isEmpty();
    }

    @Override
    public Iterator<XId> iterator() {
        return this.objects.keySet().iterator();
    }

    @Override
    public boolean removeObject(@NeverNull final XId objectId) {
        final XStateWritableObject oldObject = this.objects.remove(objectId);
        return oldObject != null;
    }

    @Override
    public XType getType() {
        return XType.XMODEL;
    }

    @Override
    public String toString() {
        return getAddress() + " , " + this.objects.size() + " objects";
    }

    /**
     * @param model A model to copy.
     * @return A copy of the model. Both model share the same objects and fields
     *         but not the same object list or revision number.
     */
    public static XStateWritableModel shallowCopy(final XStateWritableModel model) {
        if(model == null) {
            return null;
        }

        final MemStateModel result = new MemStateModel(model.getAddress());
        if(model instanceof MemStateModel) {
            result.objects.putAll(((MemStateModel)model).objects);
        } else {
            for(final XId xid : model) {
                result.addObject(model.getObject(xid));
            }
        }
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof XStateReadableModel
                && XCompareUtils.equalTree(this, (XStateReadableModel)other);
    }

}
