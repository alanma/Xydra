package org.xydra.oo.testgen.tasks.shared;

import org.xydra.base.IHasXId;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.Field;

/** Generated on Tue Mar 05 11:13:29 CET 2013 by SpecWriter, a part of xydra.org:oo */
public interface ISettings extends IHasXId {

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("language")
    String getLanguage();

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("newItemsAtTop")
    boolean getNewItemsAtTop();

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("notifyByDesktop")
    boolean getNotifyByDesktop();

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("notifyByEmail")
    boolean getNotifyByEmail();

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("notifyByPush")
    boolean getNotifyByPush();

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("startOfWeek")
    int getStartOfWeek();

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("timeFormat")
    int getTimeFormat();

    /** 
     * For GWT-internal use only [generated from: 'toClassSpec 1'] 
     *  
     * @param model  [generated from: 'toClassSpec 2'] 
     * @param id  [generated from: 'toClassSpec 3'] 
     */
    void init(XWritableModel model, XId id);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @param language the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     */
    @Field("language")
    void setLanguage(String language);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @param newItemsAtTop the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     */
    @Field("newItemsAtTop")
    void setNewItemsAtTop(boolean newItemsAtTop);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @param notifyByDesktop the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     */
    @Field("notifyByDesktop")
    void setNotifyByDesktop(boolean notifyByDesktop);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @param notifyByEmail the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     */
    @Field("notifyByEmail")
    void setNotifyByEmail(boolean notifyByEmail);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @param notifyByPush the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     */
    @Field("notifyByPush")
    void setNotifyByPush(boolean notifyByPush);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @param startOfWeek the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     */
    @Field("startOfWeek")
    void setStartOfWeek(int startOfWeek);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @param timeFormat the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     */
    @Field("timeFormat")
    void setTimeFormat(int timeFormat);

}
