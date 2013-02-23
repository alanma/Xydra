package org.xydra.oo.faked.shared;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.testgen.tasks.shared.ITask;


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
    
    public ITask createTask(String idStr) {
        return createTask(XX.toId(idStr));
    }
    
    public ITask getTask(String idStr) {
        return getTask(XX.toId(idStr));
    }
    
    public ITask createTask(XID id) {
        if(!hasXObject(id)) {
            createXObject(id);
        }
        return getTaskInternal(this.model, id);
    }
    
    public ITask getTask(XID id) {
        if(!hasXObject(id)) {
            return null;
        }
        return getTaskInternal(this.model, id);
    }
    
    protected abstract ITask getTaskInternal(XWritableModel model, XID id);
    
}
