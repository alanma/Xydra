package org.xydra.oo.testgen.tasks.java;

import java.lang.Override;
import java.lang.reflect.Proxy;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.runtime.java.OOJavaOnlyProxy;
import org.xydra.oo.testgen.tasks.shared.AbstractSharedFactory;
import org.xydra.oo.testgen.tasks.shared.IBaseList;
import org.xydra.oo.testgen.tasks.shared.IHome;
import org.xydra.oo.testgen.tasks.shared.ISettings;
import org.xydra.oo.testgen.tasks.shared.ISmartList;
import org.xydra.oo.testgen.tasks.shared.ITask;
import org.xydra.oo.testgen.tasks.shared.ITaskList;
import org.xydra.oo.testgen.tasks.shared.IUser;

/** Generated on Thu Mar 07 22:05:12 CET 2013 by SpecWriter, a part of xydra.org:oo */
public class JavaFactory extends AbstractSharedFactory {

    /** 
     *  [generated from: 'generateFactories-WaYjk'] 
     *  
     * @param model  [generated from: 'generateFactories-dctg4'] 
     */
    public JavaFactory(XWritableModel model) {
        super(model);
    }

    /** 
     *  [generated from: 'generateFactories-Kus1F'] 
     *  
     * @param model  [generated from: 'generateFactories-PIZOa'] 
     * @param id  [generated from: 'generateFactories-vxB4a'] 
     * @return ... 
     */
    @Override
    protected IBaseList getBaseListInternal(XWritableModel model, XId id) {
        IBaseList w = (IBaseList) Proxy.newProxyInstance(IBaseList.class.getClassLoader(),
            new Class<?>[] { IBaseList.class }, new OOJavaOnlyProxy(model, id));
        return w;
    }

    /** 
     *  [generated from: 'generateFactories-Kus1F'] 
     *  
     * @param model  [generated from: 'generateFactories-PIZOa'] 
     * @param id  [generated from: 'generateFactories-vxB4a'] 
     * @return ... 
     */
    @Override
    protected IHome getHomeInternal(XWritableModel model, XId id) {
        IHome w = (IHome) Proxy.newProxyInstance(IHome.class.getClassLoader(),
            new Class<?>[] { IHome.class }, new OOJavaOnlyProxy(model, id));
        return w;
    }

    /** 
     *  [generated from: 'generateFactories-Kus1F'] 
     *  
     * @param model  [generated from: 'generateFactories-PIZOa'] 
     * @param id  [generated from: 'generateFactories-vxB4a'] 
     * @return ... 
     */
    @Override
    protected ISettings getSettingsInternal(XWritableModel model, XId id) {
        ISettings w = (ISettings) Proxy.newProxyInstance(ISettings.class.getClassLoader(),
            new Class<?>[] { ISettings.class }, new OOJavaOnlyProxy(model, id));
        return w;
    }

    /** 
     *  [generated from: 'generateFactories-Kus1F'] 
     *  
     * @param model  [generated from: 'generateFactories-PIZOa'] 
     * @param id  [generated from: 'generateFactories-vxB4a'] 
     * @return ... 
     */
    @Override
    protected ISmartList getSmartListInternal(XWritableModel model, XId id) {
        ISmartList w = (ISmartList) Proxy.newProxyInstance(ISmartList.class.getClassLoader(),
            new Class<?>[] { ISmartList.class }, new OOJavaOnlyProxy(model, id));
        return w;
    }

    /** 
     *  [generated from: 'generateFactories-Kus1F'] 
     *  
     * @param model  [generated from: 'generateFactories-PIZOa'] 
     * @param id  [generated from: 'generateFactories-vxB4a'] 
     * @return ... 
     */
    @Override
    protected ITask getTaskInternal(XWritableModel model, XId id) {
        ITask w = (ITask) Proxy.newProxyInstance(ITask.class.getClassLoader(),
            new Class<?>[] { ITask.class }, new OOJavaOnlyProxy(model, id));
        return w;
    }

    /** 
     *  [generated from: 'generateFactories-Kus1F'] 
     *  
     * @param model  [generated from: 'generateFactories-PIZOa'] 
     * @param id  [generated from: 'generateFactories-vxB4a'] 
     * @return ... 
     */
    @Override
    protected ITaskList getTaskListInternal(XWritableModel model, XId id) {
        ITaskList w = (ITaskList) Proxy.newProxyInstance(ITaskList.class.getClassLoader(),
            new Class<?>[] { ITaskList.class }, new OOJavaOnlyProxy(model, id));
        return w;
    }

    /** 
     *  [generated from: 'generateFactories-Kus1F'] 
     *  
     * @param model  [generated from: 'generateFactories-PIZOa'] 
     * @param id  [generated from: 'generateFactories-vxB4a'] 
     * @return ... 
     */
    @Override
    protected IUser getUserInternal(XWritableModel model, XId id) {
        IUser w = (IUser) Proxy.newProxyInstance(IUser.class.getClassLoader(),
            new Class<?>[] { IUser.class }, new OOJavaOnlyProxy(model, id));
        return w;
    }

}
