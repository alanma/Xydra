package org.xydra.oo.testgen.tasks.client;

import com.google.gwt.core.client.GWT;
import java.lang.Override;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.testgen.tasks.shared.AbstractSharedFactory;
import org.xydra.oo.testgen.tasks.shared.IBaseList;
import org.xydra.oo.testgen.tasks.shared.IHome;
import org.xydra.oo.testgen.tasks.shared.ISettings;
import org.xydra.oo.testgen.tasks.shared.ISmartList;
import org.xydra.oo.testgen.tasks.shared.ITask;
import org.xydra.oo.testgen.tasks.shared.ITaskList;
import org.xydra.oo.testgen.tasks.shared.IUser;

/** 
 * Generated on Mon Feb 03 22:56:15 CET 2014 Generated on Mon Feb 03 22:56:15 CET 2014  
 * by SpecWriter, a part of xydra.org:oo 
 */
public class GwtFactory extends AbstractSharedFactory {

    /** 
     *  [generated from: 'generateFactories-oTz9R'] 
     *  
     * @param model  [generated from: 'generateFactories-mqFtF'] 
     */
    public GwtFactory(XWritableModel model) {
        super(model);
    }

    /** 
     *  [generated from: 'generateFactories-21utp'] 
     *  
     * @param model  [generated from: 'generateFactories-Zl7Qm'] 
     * @param id  [generated from: 'generateFactories-aXRfh'] 
     * @return ... 
     */
    @Override
    protected IBaseList getBaseListInternal(XWritableModel model, XId id) {
        return wrapBaseList(model, id);
    }

    /** 
     *  [generated from: 'generateFactories-21utp'] 
     *  
     * @param model  [generated from: 'generateFactories-Zl7Qm'] 
     * @param id  [generated from: 'generateFactories-aXRfh'] 
     * @return ... 
     */
    @Override
    protected IHome getHomeInternal(XWritableModel model, XId id) {
        return wrapHome(model, id);
    }

    /** 
     *  [generated from: 'generateFactories-21utp'] 
     *  
     * @param model  [generated from: 'generateFactories-Zl7Qm'] 
     * @param id  [generated from: 'generateFactories-aXRfh'] 
     * @return ... 
     */
    @Override
    protected ISettings getSettingsInternal(XWritableModel model, XId id) {
        return wrapSettings(model, id);
    }

    /** 
     *  [generated from: 'generateFactories-21utp'] 
     *  
     * @param model  [generated from: 'generateFactories-Zl7Qm'] 
     * @param id  [generated from: 'generateFactories-aXRfh'] 
     * @return ... 
     */
    @Override
    protected ISmartList getSmartListInternal(XWritableModel model, XId id) {
        return wrapSmartList(model, id);
    }

    /** 
     *  [generated from: 'generateFactories-21utp'] 
     *  
     * @param model  [generated from: 'generateFactories-Zl7Qm'] 
     * @param id  [generated from: 'generateFactories-aXRfh'] 
     * @return ... 
     */
    @Override
    protected ITask getTaskInternal(XWritableModel model, XId id) {
        return wrapTask(model, id);
    }

    /** 
     *  [generated from: 'generateFactories-21utp'] 
     *  
     * @param model  [generated from: 'generateFactories-Zl7Qm'] 
     * @param id  [generated from: 'generateFactories-aXRfh'] 
     * @return ... 
     */
    @Override
    protected ITaskList getTaskListInternal(XWritableModel model, XId id) {
        return wrapTaskList(model, id);
    }

    /** 
     *  [generated from: 'generateFactories-21utp'] 
     *  
     * @param model  [generated from: 'generateFactories-Zl7Qm'] 
     * @param id  [generated from: 'generateFactories-aXRfh'] 
     * @return ... 
     */
    @Override
    protected IUser getUserInternal(XWritableModel model, XId id) {
        return wrapUser(model, id);
    }

    /** 
     *  [generated from: 'generateFactories-4C46E'] 
     *  
     * @param model  [generated from: 'generateFactories-5uhaQ'] 
     * @param id  [generated from: 'generateFactories-fzf9t'] 
     * @return ... 
     */
    public static IBaseList wrapBaseList(XWritableModel model, XId id) {
        IBaseList w = GWT.create(IBaseList.class);
        w.init(model, id);
        return w;
    }

    /** 
     *  [generated from: 'generateFactories-4C46E'] 
     *  
     * @param model  [generated from: 'generateFactories-5uhaQ'] 
     * @param id  [generated from: 'generateFactories-fzf9t'] 
     * @return ... 
     */
    public static IHome wrapHome(XWritableModel model, XId id) {
        IHome w = GWT.create(IHome.class);
        w.init(model, id);
        return w;
    }

    /** 
     *  [generated from: 'generateFactories-4C46E'] 
     *  
     * @param model  [generated from: 'generateFactories-5uhaQ'] 
     * @param id  [generated from: 'generateFactories-fzf9t'] 
     * @return ... 
     */
    public static ISettings wrapSettings(XWritableModel model, XId id) {
        ISettings w = GWT.create(ISettings.class);
        w.init(model, id);
        return w;
    }

    /** 
     *  [generated from: 'generateFactories-4C46E'] 
     *  
     * @param model  [generated from: 'generateFactories-5uhaQ'] 
     * @param id  [generated from: 'generateFactories-fzf9t'] 
     * @return ... 
     */
    public static ISmartList wrapSmartList(XWritableModel model, XId id) {
        ISmartList w = GWT.create(ISmartList.class);
        w.init(model, id);
        return w;
    }

    /** 
     *  [generated from: 'generateFactories-4C46E'] 
     *  
     * @param model  [generated from: 'generateFactories-5uhaQ'] 
     * @param id  [generated from: 'generateFactories-fzf9t'] 
     * @return ... 
     */
    public static ITaskList wrapTaskList(XWritableModel model, XId id) {
        ITaskList w = GWT.create(ITaskList.class);
        w.init(model, id);
        return w;
    }

    /** 
     *  [generated from: 'generateFactories-4C46E'] 
     *  
     * @param model  [generated from: 'generateFactories-5uhaQ'] 
     * @param id  [generated from: 'generateFactories-fzf9t'] 
     * @return ... 
     */
    public static ITask wrapTask(XWritableModel model, XId id) {
        ITask w = GWT.create(ITask.class);
        w.init(model, id);
        return w;
    }

    /** 
     *  [generated from: 'generateFactories-4C46E'] 
     *  
     * @param model  [generated from: 'generateFactories-5uhaQ'] 
     * @param id  [generated from: 'generateFactories-fzf9t'] 
     * @return ... 
     */
    public static IUser wrapUser(XWritableModel model, XId id) {
        IUser w = GWT.create(IUser.class);
        w.init(model, id);
        return w;
    }

}
