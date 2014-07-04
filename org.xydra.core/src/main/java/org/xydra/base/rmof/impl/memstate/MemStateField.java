package org.xydra.base.rmof.impl.memstate;

import org.xydra.base.XAddress;
import org.xydra.base.XCompareUtils;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XStateReadableField;
import org.xydra.base.rmof.XStateWritableField;
import org.xydra.base.util.DumpUtilsBase;
import org.xydra.base.value.XValue;
import org.xydra.index.XI;
import org.xydra.sharedutils.XyAssert;


/**
 * A simple data container for {@link XStateWritableField}.
 * 
 * Minimal memory footprint, can be used as data transfer object.
 * 
 * @author voelkel
 */
public class MemStateField extends MemStateEntity implements XStateWritableField {
    
    private static final long serialVersionUID = -4704907115751969328L;
    
    private XValue value;
    
    /* Just for GWT */
    protected MemStateField() {
    }
    
    public MemStateField(XAddress address) {
        this(address, null);
    }
    
    /**
     * @param address Caller is responsible to use an address that addresses a
     *            field.
     * @param value can be null
     */
    public MemStateField(XAddress address, XValue value) {
        super(address);
        XyAssert.xyAssert(address.getAddressedType() == XType.XFIELD);
        this.value = value;
    }
    
    @Override
    public XId getId() {
        return getAddress().getField();
    }
    
    @Override
    public XValue getValue() {
        return this.value;
    }
    
    @Override
    public boolean isEmpty() {
        return this.value == null;
    }
    
    @Override
    public boolean setValue(XValue value) {
        boolean changed = !XI.equals(this.value, value);
        this.value = value;
        return changed;
    }
    
    @Override
    public XType getType() {
        return XType.XFIELD;
    }
    
    @Override
    public String toString() {
        return DumpUtilsBase.toStringBuffer(this).toString();
    }
    
    boolean sameState(XReadableField o) {
        return o.getValue().equals(getValue());
    }
    
    @Override
    public boolean equals(Object o) {
        return o instanceof XStateReadableField
                && XCompareUtils.equalTree(this, (XStateReadableField)o);
    }
    
}
