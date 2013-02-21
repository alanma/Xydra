package org.xydra.oo.faked.java;

import java.lang.reflect.Proxy;

import org.xydra.base.XID;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.faked.shared.AbstractFactory;
import org.xydra.oo.runtime.java.OOJavaOnlyProxy;
import org.xydra.oo.testgen.tasks.shared.ITask;


public class JavaFactory extends AbstractFactory {
    
    public JavaFactory(XWritableModel model) {
        super(model);
    }
    
    @Override
    protected ITask getTaskInternal(XWritableModel model, XID id) {
        assert hasXObject(id);
        ITask task = (ITask)Proxy.newProxyInstance(ITask.class.getClassLoader(),
                new Class<?>[] { ITask.class }, new OOJavaOnlyProxy(model, id));
        return task;
    }
    
}
