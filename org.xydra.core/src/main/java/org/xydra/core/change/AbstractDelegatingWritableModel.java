package org.xydra.core.change;

import java.util.Iterator;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.util.DumpUtils;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;


/**
 * A abstract helper class for the commonalities between {@link XWritableModel}
 * implementations that have a delegation strategy to an internal state.
 * 
 * 
 * @author xamde
 */
public abstract class AbstractDelegatingWritableModel implements XWritableModel {
    
    /**
     * State-less wrapper pulling all state from the cache index or base model
     * of the {@link AbstractDelegatingWritableModel}.
     */
    class WrappedField implements XWritableField {
        
        private final XId fieldId;
        private final XId objectId;
        
        public WrappedField(final XId objectId, final XId fieldId) {
            XyAssert.xyAssert(objectId != null);
            assert objectId != null;
            XyAssert.xyAssert(fieldId != null);
            assert fieldId != null;
            this.objectId = objectId;
            this.fieldId = fieldId;
        }
        
        @Override
        public XAddress getAddress() {
            XAddress xa = AbstractDelegatingWritableModel.this.getAddress();
            return XX.toAddress(xa.getRepository(), xa.getModel(), this.objectId, this.fieldId);
        }
        
        @Override
        public XId getId() {
            return this.fieldId;
        }
        
        public long getRevisionNumber() {
            return AbstractDelegatingWritableModel.this.field_getRevisionNumber(this.objectId,
                    this.fieldId);
        }
        
        @Override
        public XType getType() {
            return XType.XFIELD;
        }
        
        @Override
        public XValue getValue() {
            return AbstractDelegatingWritableModel.this.field_getValue(this.objectId, this.fieldId);
        }
        
        @Override
        public boolean isEmpty() {
            return AbstractDelegatingWritableModel.this.field_isEmpty(this.objectId, this.fieldId);
        }
        
        @Override
        public boolean setValue(final XValue value) {
            XyAssert.xyAssert(this.objectId != null);
            assert this.objectId != null;
            return AbstractDelegatingWritableModel.this.field_setValue(this.objectId, this.fieldId,
                    value);
        }
        
    }
    
    /**
     * State-less wrapper pulling all state from the cache index or base model
     * of the {@link AbstractDelegatingWritableModel}.
     */
    class WrappedObject implements XWritableObject {
        
        private final XId objectId;
        
        public WrappedObject(final XId objectId) {
            XyAssert.xyAssert(objectId != null);
            assert objectId != null;
            this.objectId = objectId;
        }
        
        @Override
        public XWritableField createField(final XId fieldId) {
            XyAssert.xyAssert(fieldId != null);
            assert fieldId != null;
            return AbstractDelegatingWritableModel.this.object_createField(this.objectId, fieldId);
        }
        
        @Override
        public XAddress getAddress() {
            return XX.resolveObject(AbstractDelegatingWritableModel.this.getAddress(),
                    this.objectId);
        }
        
        @Override
        public XWritableField getField(final XId fieldId) {
            return AbstractDelegatingWritableModel.this.object_getField(this.objectId, fieldId);
        }
        
        @Override
        public XId getId() {
            return this.objectId;
        }
        
        public long getRevisionNumber() {
            return object_getRevisionNumber(this.objectId);
        }
        
        @Override
        public XType getType() {
            return XType.XOBJECT;
        }
        
        @Override
        public boolean hasField(final XId fieldId) {
            return AbstractDelegatingWritableModel.this.object_hasField(this.objectId, fieldId);
        }
        
        @Override
        public boolean isEmpty() {
            return AbstractDelegatingWritableModel.this.object_isEmpty(this.objectId);
        }
        
        @Override
        public Iterator<XId> iterator() {
            return AbstractDelegatingWritableModel.this.object_iterator(this.objectId);
        }
        
        @Override
        public boolean removeField(final XId fieldId) {
            return AbstractDelegatingWritableModel.this.object_removeField(this.objectId, fieldId);
        }
        
        @Override
        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("XObject '").append(this.getAddress().toString()).append("' <br/>\n");
            if(!this.isEmpty()) {
                for(XId fieldId : this) {
                    buf.append("* '").append(fieldId.toString()).append("' = ");
                    XWritableField field = this.getField(fieldId);
                    if(field == null) {
                        buf.append("NULL");
                    } else {
                        String value = field.getValue() == null ? "null" : field.getValue()
                                .toString();
                        buf.append("'").append(value).append("'");
                    }
                    buf.append(" <br/>\n");
                }
            }
            return buf.toString();
        }
    }
    
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory
            .getLogger(AbstractDelegatingWritableModel.class);
    
    public static final long UNDEFINED = -2;
    
    @Override
    public abstract XWritableObject createObject(@NeverNull final XId objectId);
    
    protected abstract long field_getRevisionNumber(final XId objectId, final XId fieldId);
    
    protected abstract XValue field_getValue(final XId objectId, final XId fieldId);
    
    protected boolean field_isEmpty(final XId objectId, final XId fieldId) {
        XyAssert.xyAssert(hasObject(objectId));
        XyAssert.xyAssert(getObject(objectId).hasField(fieldId));
        
        return getObject(objectId).getField(fieldId).isEmpty();
    }
    
    protected abstract boolean field_setValue(final XId objectId, final XId fieldId,
            final XValue value);
    
    @Override
    public abstract XAddress getAddress();
    
    @Override
    public abstract XId getId();
    
    @Override
    public XWritableObject getObject(@NeverNull final XId objectId) {
        if(hasObject(objectId)) {
            return getObject_internal(objectId);
        } else {
            return null;
        }
    }
    
    protected XWritableObject getObject_internal(XId objectId) {
        return new WrappedObject(objectId);
    }
    
    @Override
    public XType getType() {
        return XType.XMODEL;
    }
    
    @Override
    public abstract boolean hasObject(@NeverNull final XId objectId);
    
    @Override
    public abstract Iterator<XId> iterator();
    
    protected abstract XWritableField object_createField(final XId objectId, final XId fieldId);
    
    protected XWritableField object_getField(final XId objectId, final XId fieldId) {
        XyAssert.xyAssert(objectId != null);
        assert objectId != null;
        XyAssert.xyAssert(fieldId != null);
        assert fieldId != null;
        if(object_hasField(objectId, fieldId)) {
            return object_getField_internal(objectId, fieldId);
        } else {
            return null;
        }
    }
    
    protected XWritableField object_getField_internal(XId objectId, XId fieldId) {
        return new WrappedField(objectId, fieldId);
    }
    
    /**
     * @param objectId
     * @return the revision number of the object with the given ID
     */
    protected abstract long object_getRevisionNumber(final XId objectId);
    
    protected abstract boolean object_hasField(final XId objectId, final XId fieldId);
    
    protected abstract boolean object_isEmpty(final XId objectId);
    
    protected abstract Iterator<XId> object_iterator(final XId objectId);
    
    protected abstract boolean object_removeField(final XId objectId, final XId fieldId);
    
    @Override
    public abstract boolean removeObject(@NeverNull final XId objectId);
    
    protected XAddress resolveField(final XId objectId, final XId fieldId) {
        XyAssert.xyAssert(objectId != null);
        assert objectId != null;
        XyAssert.xyAssert(fieldId != null);
        assert fieldId != null;
        return XX.toAddress(this.getAddress().getRepository(), this.getId(), objectId, fieldId);
    }
    
    protected XAddress resolveObject(final XId objectId) {
        XyAssert.xyAssert(objectId != null);
        assert objectId != null;
        return XX.toAddress(this.getAddress().getRepository(), this.getId(), objectId, null);
    }
    
    @Override
    public String toString() {
        return this.getId() + " (" + this.getClass().getName() + ") " + this.hashCode() + " "
                + DumpUtils.toStringBuffer(this);
    }
    
}
