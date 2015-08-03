package org.xydra.base.rmof.impl.memstate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XCompareUtils;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XStateReadableObject;
import org.xydra.base.rmof.XStateWritableField;
import org.xydra.base.rmof.XStateWritableObject;
import org.xydra.base.util.DumpUtilsBase;
import org.xydra.sharedutils.XyAssert;


/**
 * A simple data container for {@link XStateWritableObject}.
 *
 * Minimal memory footprint, can be used as data transfer object.
 *
 * @author xamde
 */
public class MemStateObject extends MemStateEntity implements XStateWritableObject {

    private static final long serialVersionUID = 5593443685935758227L;

    // not final for GWT serialisation
    private Map<XId,XStateWritableField> fields;

    /** For GWT only! */
    protected MemStateObject() {
    }

    public MemStateObject(final XAddress address) {
        super(address);
        XyAssert.xyAssert(address.getAddressedType() == XType.XOBJECT);
        this.fields = new HashMap<XId,XStateWritableField>(2);
    }

    @Override
    public XStateWritableField createField(final XId fieldId) {
        final XStateWritableField field = this.fields.get(fieldId);
        if(field != null) {
            return field;
        }
        final MemStateField newField = new MemStateField(Base.resolveField(getAddress(), fieldId));
        this.fields.put(fieldId, newField);
        return newField;
    }

    @Override
    public XStateWritableField getField(final XId fieldId) {
        return this.fields.get(fieldId);
    }

    @Override
    public XId getId() {
        return getAddress().getObject();
    }

    @Override
    public boolean hasField(final XId fieldId) {
        return this.fields.containsKey(fieldId);
    }

    @Override
    public boolean isEmpty() {
        return this.fields.isEmpty();
    }

    @Override
    public Iterator<XId> iterator() {
        return this.fields.keySet().iterator();
    }

    @Override
    public boolean removeField(final XId fieldId) {
        final XStateWritableField oldField = this.fields.remove(fieldId);
        return oldField != null;
    }

    @Override
    public XType getType() {
        return XType.XOBJECT;
    }

    @Override
    public String toString() {
        return DumpUtilsBase.toStringBuffer(this).toString();
    }

    /**
     * @param object An object to copy.
     * @return A copy of the object. Both objects share the same fields but not
     *         the same field list or revision number.
     */
    public static MemStateObject shallowCopy(final XRevWritableObject object) {

        if(object == null) {
            return null;
        }

        final MemStateObject result = new MemStateObject(object.getAddress());

        if(object instanceof MemStateModel) {
            result.fields.putAll(((MemStateObject)object).fields);
        } else {
            for(final XId xid : object) {
                result.addField(object.getField(xid));
            }
        }

        return result;
    }

    private void addField(final XRevWritableField field) {
        this.fields.put(field.getId(), field);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof XStateReadableObject
                && XCompareUtils.equalTree(this, (XStateReadableObject)other);
    }

}
