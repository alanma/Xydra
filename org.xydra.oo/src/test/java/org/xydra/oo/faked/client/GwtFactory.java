package org.xydra.oo.faked.client;

import org.xydra.base.XID;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.faked.shared.AbstractFactory;
import org.xydra.oo.testgen.tasks.shared.ITask;


public class GwtFactory extends AbstractFactory {
    
    public GwtFactory(XWritableModel model) {
        super(model);
    }
    
    @Override
    protected ITask getTaskInternal(XWritableModel model, XID id) {
        return new GwtTask(model, id);
    }
    
}
