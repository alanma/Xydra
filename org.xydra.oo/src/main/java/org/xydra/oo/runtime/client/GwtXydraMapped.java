package org.xydra.oo.runtime.client;

import org.xydra.base.XID;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.runtime.shared.OOProxy;


public class GwtXydraMapped {
    
    protected OOProxy oop;
    
    public GwtXydraMapped(XWritableModel model, XID id) {
        this.oop = new OOProxy(model, id);
    }
    
    public XID getId() {
        return this.oop.getId();
    }
    
}
