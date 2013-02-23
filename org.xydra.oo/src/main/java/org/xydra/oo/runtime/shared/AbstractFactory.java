package org.xydra.oo.runtime.shared;

import org.xydra.base.XID;
import org.xydra.base.rmof.XWritableModel;


public abstract class AbstractFactory {
    
    protected XWritableModel model;
    
    public AbstractFactory(XWritableModel model) {
        this.model = model;
    }
    
    protected void createXObject(XID id) {
        this.model.createObject(id);
    }
    
    protected boolean hasXObject(XID id) {
        return this.model.hasObject(id);
    }
    
}
