package org.xydra.base.rmof.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.impl.XExistsRevWritableField;
import org.xydra.base.util.DumpUtilsBase;
import org.xydra.base.value.XValue;
import org.xydra.index.XI;
import org.xydra.sharedutils.XyAssert;


/**
 * A simple data container for {@link XWritableField}.
 * 
 * Minimal memory footprint, can be used as data transfer object.
 * 
 * @author xamde
 */
public class SimpleField extends SimpleEntity implements XExistsRevWritableField {
    
    private static final long serialVersionUID = -4704907115751969328L;
    
    // not final for GWT
    private XAddress address;
    
    private long revisionNumber;
    
    private XValue value;
    
    /* Just for GWT */
    protected SimpleField() {
    }
    
    public SimpleField(XAddress address) {
        this(address, XCommand.NEW);
    }
    
    public SimpleField(XAddress address, long rev) {
        this.address = address;
        this.revisionNumber = rev;
        this.value = null;
    }
    
    /**
     * @param address Caller is responsible to use an address that addresses a
     *            field.
     * @param revisionNumber will be initially set in this {@link SimpleField}
     * @param value can be null
     */
    public SimpleField(XAddress address, long revisionNumber, XValue value) {
        XyAssert.xyAssert(address.getAddressedType() == XType.XFIELD);
        this.address = address;
        this.revisionNumber = revisionNumber;
        this.value = value;
    }
    
    @Override
    public XAddress getAddress() {
        return this.address;
    }
    
    @Override
    public XId getId() {
        return this.address.getField();
    }
    
    @Override
    public long getRevisionNumber() {
        return this.revisionNumber;
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
    public void setRevisionNumber(long rev) {
        this.revisionNumber = rev;
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
        return o != null &&
        
        (
        
        (o.getValue() == null && this.getValue() == null)
        
        || (o.getValue().equals(getValue()))
        
        );
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof XReadableField)
                && ((XReadableField)o).getAddress().equals(getAddress())
                && ((XReadableField)o).getRevisionNumber() == getRevisionNumber()
                && sameState((XReadableField)o);
    }
    
}
