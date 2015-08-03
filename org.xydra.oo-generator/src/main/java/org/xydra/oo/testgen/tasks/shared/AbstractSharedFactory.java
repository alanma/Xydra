package org.xydra.oo.testgen.tasks.shared;

import org.xydra.base.Base;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.XX;
import org.xydra.oo.runtime.shared.AbstractFactory;

/** Generated on Tue Oct 21 21:47:37 CEST 2014 by SpecWriter, a part of xydra.org:oo */
public abstract class AbstractSharedFactory extends AbstractFactory {

    /**
     *  [generated from: 'generateFactories-HqDUE']
     *
     * @param model  [generated from: 'generateFactories-76emU']
     */
    public AbstractSharedFactory(final XWritableModel model) {
        super(model);
    }

    /**
     *  [generated from: 'generateFactories-gn0BV']
     *
     * @param idStr  [generated from: 'generateFactories-myHOp']
     * @return ...
     */
    public IBaseList createBaseList(final String idStr) {
        return createBaseList(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-2m6U1']
     *
     * @param id  [generated from: 'generateFactories-n8c8H']
     * @return ...
     */
    public IBaseList createBaseList(final XId id) {
        if(!hasXObject(id)) { createXObject(id); }
        return getBaseListInternal(this.model, id);
    }

    /**
     *  [generated from: 'generateFactories-gn0BV']
     *
     * @param idStr  [generated from: 'generateFactories-myHOp']
     * @return ...
     */
    public IHome createHome(final String idStr) {
        return createHome(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-2m6U1']
     *
     * @param id  [generated from: 'generateFactories-n8c8H']
     * @return ...
     */
    public IHome createHome(final XId id) {
        if(!hasXObject(id)) { createXObject(id); }
        return getHomeInternal(this.model, id);
    }

    /**
     *  [generated from: 'generateFactories-gn0BV']
     *
     * @param idStr  [generated from: 'generateFactories-myHOp']
     * @return ...
     */
    public ISettings createSettings(final String idStr) {
        return createSettings(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-2m6U1']
     *
     * @param id  [generated from: 'generateFactories-n8c8H']
     * @return ...
     */
    public ISettings createSettings(final XId id) {
        if(!hasXObject(id)) { createXObject(id); }
        return getSettingsInternal(this.model, id);
    }

    /**
     *  [generated from: 'generateFactories-gn0BV']
     *
     * @param idStr  [generated from: 'generateFactories-myHOp']
     * @return ...
     */
    public ISmartList createSmartList(final String idStr) {
        return createSmartList(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-2m6U1']
     *
     * @param id  [generated from: 'generateFactories-n8c8H']
     * @return ...
     */
    public ISmartList createSmartList(final XId id) {
        if(!hasXObject(id)) { createXObject(id); }
        return getSmartListInternal(this.model, id);
    }

    /**
     *  [generated from: 'generateFactories-gn0BV']
     *
     * @param idStr  [generated from: 'generateFactories-myHOp']
     * @return ...
     */
    public ITaskList createTaskList(final String idStr) {
        return createTaskList(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-2m6U1']
     *
     * @param id  [generated from: 'generateFactories-n8c8H']
     * @return ...
     */
    public ITaskList createTaskList(final XId id) {
        if(!hasXObject(id)) { createXObject(id); }
        return getTaskListInternal(this.model, id);
    }

    /**
     *  [generated from: 'generateFactories-gn0BV']
     *
     * @param idStr  [generated from: 'generateFactories-myHOp']
     * @return ...
     */
    public ITask createTask(final String idStr) {
        return createTask(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-2m6U1']
     *
     * @param id  [generated from: 'generateFactories-n8c8H']
     * @return ...
     */
    public ITask createTask(final XId id) {
        if(!hasXObject(id)) { createXObject(id); }
        return getTaskInternal(this.model, id);
    }

    /**
     *  [generated from: 'generateFactories-gn0BV']
     *
     * @param idStr  [generated from: 'generateFactories-myHOp']
     * @return ...
     */
    public IUser createUser(final String idStr) {
        return createUser(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-2m6U1']
     *
     * @param id  [generated from: 'generateFactories-n8c8H']
     * @return ...
     */
    public IUser createUser(final XId id) {
        if(!hasXObject(id)) { createXObject(id); }
        return getUserInternal(this.model, id);
    }

    /**
     *  [generated from: 'generateFactories-n6Zvf']
     *
     * @param model  [generated from: 'generateFactories-bnAB6']
     * @param id  [generated from: 'generateFactories-SEML4']
     * @return ...
     */
    protected abstract IBaseList getBaseListInternal(XWritableModel model, XId id);

    /**
     *  [generated from: 'generateFactories-UN0yH']
     *
     * @param idStr  [generated from: 'generateFactories-2QL32']
     * @return ...
     */
    public IBaseList getBaseList(final String idStr) {
        return getBaseList(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-Xz0r2']
     *
     * @param id  [generated from: 'generateFactories-usnO3']
     * @return ...
     */
    public IBaseList getBaseList(final XId id) {
        if (!hasXObject(id)) { return null; }
        return getBaseListInternal( this.model, id);
    }

    /**
     *  [generated from: 'generateFactories-n6Zvf']
     *
     * @param model  [generated from: 'generateFactories-bnAB6']
     * @param id  [generated from: 'generateFactories-SEML4']
     * @return ...
     */
    protected abstract IHome getHomeInternal(XWritableModel model, XId id);

    /**
     *  [generated from: 'generateFactories-UN0yH']
     *
     * @param idStr  [generated from: 'generateFactories-2QL32']
     * @return ...
     */
    public IHome getHome(final String idStr) {
        return getHome(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-Xz0r2']
     *
     * @param id  [generated from: 'generateFactories-usnO3']
     * @return ...
     */
    public IHome getHome(final XId id) {
        if (!hasXObject(id)) { return null; }
        return getHomeInternal( this.model, id);
    }

    /**
     *  [generated from: 'generateFactories-n6Zvf']
     *
     * @param model  [generated from: 'generateFactories-bnAB6']
     * @param id  [generated from: 'generateFactories-SEML4']
     * @return ...
     */
    protected abstract ISettings getSettingsInternal(XWritableModel model, XId id);

    /**
     *  [generated from: 'generateFactories-UN0yH']
     *
     * @param idStr  [generated from: 'generateFactories-2QL32']
     * @return ...
     */
    public ISettings getSettings(final String idStr) {
        return getSettings(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-Xz0r2']
     *
     * @param id  [generated from: 'generateFactories-usnO3']
     * @return ...
     */
    public ISettings getSettings(final XId id) {
        if (!hasXObject(id)) { return null; }
        return getSettingsInternal( this.model, id);
    }

    /**
     *  [generated from: 'generateFactories-n6Zvf']
     *
     * @param model  [generated from: 'generateFactories-bnAB6']
     * @param id  [generated from: 'generateFactories-SEML4']
     * @return ...
     */
    protected abstract ISmartList getSmartListInternal(XWritableModel model, XId id);

    /**
     *  [generated from: 'generateFactories-UN0yH']
     *
     * @param idStr  [generated from: 'generateFactories-2QL32']
     * @return ...
     */
    public ISmartList getSmartList(final String idStr) {
        return getSmartList(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-Xz0r2']
     *
     * @param id  [generated from: 'generateFactories-usnO3']
     * @return ...
     */
    public ISmartList getSmartList(final XId id) {
        if (!hasXObject(id)) { return null; }
        return getSmartListInternal( this.model, id);
    }

    /**
     *  [generated from: 'generateFactories-n6Zvf']
     *
     * @param model  [generated from: 'generateFactories-bnAB6']
     * @param id  [generated from: 'generateFactories-SEML4']
     * @return ...
     */
    protected abstract ITask getTaskInternal(XWritableModel model, XId id);

    /**
     *  [generated from: 'generateFactories-n6Zvf']
     *
     * @param model  [generated from: 'generateFactories-bnAB6']
     * @param id  [generated from: 'generateFactories-SEML4']
     * @return ...
     */
    protected abstract ITaskList getTaskListInternal(XWritableModel model, XId id);

    /**
     *  [generated from: 'generateFactories-UN0yH']
     *
     * @param idStr  [generated from: 'generateFactories-2QL32']
     * @return ...
     */
    public ITaskList getTaskList(final String idStr) {
        return getTaskList(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-Xz0r2']
     *
     * @param id  [generated from: 'generateFactories-usnO3']
     * @return ...
     */
    public ITaskList getTaskList(final XId id) {
        if (!hasXObject(id)) { return null; }
        return getTaskListInternal( this.model, id);
    }

    /**
     *  [generated from: 'generateFactories-UN0yH']
     *
     * @param idStr  [generated from: 'generateFactories-2QL32']
     * @return ...
     */
    public ITask getTask(final String idStr) {
        return getTask(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-Xz0r2']
     *
     * @param id  [generated from: 'generateFactories-usnO3']
     * @return ...
     */
    public ITask getTask(final XId id) {
        if (!hasXObject(id)) { return null; }
        return getTaskInternal( this.model, id);
    }

    /**
     *  [generated from: 'generateFactories-n6Zvf']
     *
     * @param model  [generated from: 'generateFactories-bnAB6']
     * @param id  [generated from: 'generateFactories-SEML4']
     * @return ...
     */
    protected abstract IUser getUserInternal(XWritableModel model, XId id);

    /**
     *  [generated from: 'generateFactories-UN0yH']
     *
     * @param idStr  [generated from: 'generateFactories-2QL32']
     * @return ...
     */
    public IUser getUser(final String idStr) {
        return getUser(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-Xz0r2']
     *
     * @param id  [generated from: 'generateFactories-usnO3']
     * @return ...
     */
    public IUser getUser(final XId id) {
        if (!hasXObject(id)) { return null; }
        return getUserInternal( this.model, id);
    }

}
