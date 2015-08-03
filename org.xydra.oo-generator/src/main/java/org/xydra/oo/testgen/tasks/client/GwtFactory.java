package org.xydra.oo.testgen.tasks.client;

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

import com.google.gwt.core.client.GWT;

/**
 * Generated on Tue Oct 21 21:47:37 CEST 2014 Generated on Tue Oct 21 21:47:37 CEST
 * 2014 by SpecWriter, a part of xydra.org:oo
 */
public class GwtFactory extends AbstractSharedFactory {

    /**
     *  [generated from: 'generateFactories-oTz9R']
     *
     * @param model  [generated from: 'generateFactories-mqFtF']
     */
    public GwtFactory(final XWritableModel model) {
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
    protected IBaseList getBaseListInternal(final XWritableModel model, final XId id) {
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
    protected IHome getHomeInternal(final XWritableModel model, final XId id) {
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
    protected ISettings getSettingsInternal(final XWritableModel model, final XId id) {
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
    protected ISmartList getSmartListInternal(final XWritableModel model, final XId id) {
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
    protected ITask getTaskInternal(final XWritableModel model, final XId id) {
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
    protected ITaskList getTaskListInternal(final XWritableModel model, final XId id) {
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
    protected IUser getUserInternal(final XWritableModel model, final XId id) {
        return wrapUser(model, id);
    }

    /**
     *  [generated from: 'generateFactories-4C46E']
     *
     * @param model  [generated from: 'generateFactories-5uhaQ']
     * @param id  [generated from: 'generateFactories-fzf9t']
     * @return ...
     */
    public static IBaseList wrapBaseList(final XWritableModel model, final XId id) {
        final IBaseList w = GWT.create(IBaseList.class);
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
    public static IHome wrapHome(final XWritableModel model, final XId id) {
        final IHome w = GWT.create(IHome.class);
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
    public static ISettings wrapSettings(final XWritableModel model, final XId id) {
        final ISettings w = GWT.create(ISettings.class);
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
    public static ISmartList wrapSmartList(final XWritableModel model, final XId id) {
        final ISmartList w = GWT.create(ISmartList.class);
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
    public static ITaskList wrapTaskList(final XWritableModel model, final XId id) {
        final ITaskList w = GWT.create(ITaskList.class);
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
    public static ITask wrapTask(final XWritableModel model, final XId id) {
        final ITask w = GWT.create(ITask.class);
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
    public static IUser wrapUser(final XWritableModel model, final XId id) {
        final IUser w = GWT.create(IUser.class);
        w.init(model, id);
        return w;
    }

}
