package org.xydra.oo.runtime.client;

import org.xydra.base.IHasXId;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.runtime.shared.SharedProxy;


public class GwtXydraMapped implements IHasXId {
    
    protected SharedProxy oop;
    
    /** For GWT.create only */
    public GwtXydraMapped() {
    }
    
    public void init(XWritableModel model, XId id) {
        this.oop = new SharedProxy(model, id);
    }
    
    public GwtXydraMapped(XWritableModel model, XId id) {
        this.oop = new SharedProxy(model, id);
    }
    
    public XId getId() {
        return this.oop.getId();
    }
    
}
