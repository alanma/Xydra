package org.xydra.oo.testgen.tasks.shared;

import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.XX;
import org.xydra.oo.runtime.shared.AbstractFactory;
import org.xydra.oo.testgen.tasks.shared.IBaseList;
import org.xydra.oo.testgen.tasks.shared.IHome;
import org.xydra.oo.testgen.tasks.shared.ISettings;
import org.xydra.oo.testgen.tasks.shared.ISmartList;
import org.xydra.oo.testgen.tasks.shared.ITask;
import org.xydra.oo.testgen.tasks.shared.ITaskList;
import org.xydra.oo.testgen.tasks.shared.IUser;

/** Generated on Fri Jul 04 01:02:18 CEST 2014 by SpecWriter, a part of xydra.org:oo */
public abstract class AbstractSharedFactory extends AbstractFactory {

    /** 
     *  [generated from: 'generateFactories-HqDUE'] 
     *  
     * @param model  [generated from: 'generateFactories-76emU'] 
     */
    public AbstractSharedFactory(XWritableModel model) {
        super(model);
    }

    /** 
     *  [generated from: 'generateFactories-gn0BV'] 
     *  
     * @param idStr  [generated from: 'generateFactories-myHOp'] 
     * @return ... 
     */
    public IBaseList createBaseList(String idStr) {
        return createBaseList(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-2m6U1'] 
     *  
     * @param id  [generated from: 'generateFactories-n8c8H'] 
     * @return ... 
     */
    public IBaseList createBaseList(XId id) {
        if(!hasXObject(id)) { createXObject(id); }
        return getBaseListInternal(this.model, id);
    }

    /** 
     *  [generated from: 'generateFactories-gn0BV'] 
     *  
     * @param idStr  [generated from: 'generateFactories-myHOp'] 
     * @return ... 
     */
    public IHome createHome(String idStr) {
        return createHome(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-2m6U1'] 
     *  
     * @param id  [generated from: 'generateFactories-n8c8H'] 
     * @return ... 
     */
    public IHome createHome(XId id) {
        if(!hasXObject(id)) { createXObject(id); }
        return getHomeInternal(this.model, id);
    }

    /** 
     *  [generated from: 'generateFactories-gn0BV'] 
     *  
     * @param idStr  [generated from: 'generateFactories-myHOp'] 
     * @return ... 
     */
    public ISettings createSettings(String idStr) {
        return createSettings(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-2m6U1'] 
     *  
     * @param id  [generated from: 'generateFactories-n8c8H'] 
     * @return ... 
     */
    public ISettings createSettings(XId id) {
        if(!hasXObject(id)) { createXObject(id); }
        return getSettingsInternal(this.model, id);
    }

    /** 
     *  [generated from: 'generateFactories-gn0BV'] 
     *  
     * @param idStr  [generated from: 'generateFactories-myHOp'] 
     * @return ... 
     */
    public ISmartList createSmartList(String idStr) {
        return createSmartList(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-2m6U1'] 
     *  
     * @param id  [generated from: 'generateFactories-n8c8H'] 
     * @return ... 
     */
    public ISmartList createSmartList(XId id) {
        if(!hasXObject(id)) { createXObject(id); }
        return getSmartListInternal(this.model, id);
    }

    /** 
     *  [generated from: 'generateFactories-gn0BV'] 
     *  
     * @param idStr  [generated from: 'generateFactories-myHOp'] 
     * @return ... 
     */
    public ITaskList createTaskList(String idStr) {
        return createTaskList(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-2m6U1'] 
     *  
     * @param id  [generated from: 'generateFactories-n8c8H'] 
     * @return ... 
     */
    public ITaskList createTaskList(XId id) {
        if(!hasXObject(id)) { createXObject(id); }
        return getTaskListInternal(this.model, id);
    }

    /** 
     *  [generated from: 'generateFactories-gn0BV'] 
     *  
     * @param idStr  [generated from: 'generateFactories-myHOp'] 
     * @return ... 
     */
    public ITask createTask(String idStr) {
        return createTask(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-2m6U1'] 
     *  
     * @param id  [generated from: 'generateFactories-n8c8H'] 
     * @return ... 
     */
    public ITask createTask(XId id) {
        if(!hasXObject(id)) { createXObject(id); }
        return getTaskInternal(this.model, id);
    }

    /** 
     *  [generated from: 'generateFactories-gn0BV'] 
     *  
     * @param idStr  [generated from: 'generateFactories-myHOp'] 
     * @return ... 
     */
    public IUser createUser(String idStr) {
        return createUser(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-2m6U1'] 
     *  
     * @param id  [generated from: 'generateFactories-n8c8H'] 
     * @return ... 
     */
    public IUser createUser(XId id) {
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
    public IBaseList getBaseList(String idStr) {
        return getBaseList(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-Xz0r2'] 
     *  
     * @param id  [generated from: 'generateFactories-usnO3'] 
     * @return ... 
     */
    public IBaseList getBaseList(XId id) {
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
    public IHome getHome(String idStr) {
        return getHome(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-Xz0r2'] 
     *  
     * @param id  [generated from: 'generateFactories-usnO3'] 
     * @return ... 
     */
    public IHome getHome(XId id) {
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
    public ISettings getSettings(String idStr) {
        return getSettings(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-Xz0r2'] 
     *  
     * @param id  [generated from: 'generateFactories-usnO3'] 
     * @return ... 
     */
    public ISettings getSettings(XId id) {
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
    public ISmartList getSmartList(String idStr) {
        return getSmartList(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-Xz0r2'] 
     *  
     * @param id  [generated from: 'generateFactories-usnO3'] 
     * @return ... 
     */
    public ISmartList getSmartList(XId id) {
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
    public ITaskList getTaskList(String idStr) {
        return getTaskList(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-Xz0r2'] 
     *  
     * @param id  [generated from: 'generateFactories-usnO3'] 
     * @return ... 
     */
    public ITaskList getTaskList(XId id) {
        if (!hasXObject(id)) { return null; }
        return getTaskListInternal( this.model, id); 
    }

    /** 
     *  [generated from: 'generateFactories-UN0yH'] 
     *  
     * @param idStr  [generated from: 'generateFactories-2QL32'] 
     * @return ... 
     */
    public ITask getTask(String idStr) {
        return getTask(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-Xz0r2'] 
     *  
     * @param id  [generated from: 'generateFactories-usnO3'] 
     * @return ... 
     */
    public ITask getTask(XId id) {
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
    public IUser getUser(String idStr) {
        return getUser(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-Xz0r2'] 
     *  
     * @param id  [generated from: 'generateFactories-usnO3'] 
     * @return ... 
     */
    public IUser getUser(XId id) {
        if (!hasXObject(id)) { return null; }
        return getUserInternal( this.model, id); 
    }

}
