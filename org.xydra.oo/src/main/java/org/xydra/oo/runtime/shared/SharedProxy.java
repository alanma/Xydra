package org.xydra.oo.runtime.shared;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.XX;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


/**
 * A runtime proxy object that is backed by a generic XObject
 * 
 * @author xamde
 */
@RunsInGWT(true)
public class SharedProxy {
    
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(SharedProxy.class);
    
    private XWritableModel model;
    private XId objectId;
    
    public SharedProxy(XWritableModel model, XId objectId) {
        this.model = model;
        this.objectId = objectId;
    }
    
    public XId getId() {
        return this.objectId;
    }
    
    // private void createXObject() {
    // this.model.createObject(this.objectId);
    // }
    
    public XWritableObject getXObject() {
        return this.model.getObject(this.objectId);
    }
    
    public boolean hasXObject() {
        return this.model.hasObject(this.objectId);
    }
    
    public XValue getValue(String fieldId) {
        if(fieldId == null)
            throw new IllegalArgumentException("field id was null");
        if(fieldId.length() == 0)
            throw new IllegalArgumentException("field id empty string");
        
        if(!hasXObject())
            return null;
        XWritableField field = this.getXObject().getField(XX.toId(fieldId));
        if(field == null)
            return null;
        return field.getValue();
    }
    
    public <X extends XValue> void setValue(String fieldId, X v) {
        if(fieldId == null)
            throw new IllegalArgumentException("field id was null");
        if(fieldId.length() == 0)
            throw new IllegalArgumentException("field id empty string");
        
        if(!hasXObject())
            throw new IllegalStateException("XObject '" + this.objectId + "' does not exist");
        
        XWritableField field = this.getXObject().getField(XX.toId(fieldId));
        if(field == null)
            field = this.getXObject().createField(XX.toId(fieldId));
        
        field.setValue(v);
    }
    
    public XWritableModel getXModel() {
        return this.model;
    }
}
