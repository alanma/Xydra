package org.xydra.oo.testgen.tasks.shared;

import org.xydra.base.IHasXId;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.Field;
import org.xydra.oo.testgen.tasks.shared.ISettings;

/** Generated on Fri Jul 04 01:02:18 CEST 2014 by SpecWriter, a part of xydra.org:oo */
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
     * @return ... 
     */
    @Field("language")
    ISettings setLanguage(String language);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @param newItemsAtTop the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     * @return ... 
     */
    @Field("newItemsAtTop")
    ISettings setNewItemsAtTop(boolean newItemsAtTop);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @param notifyByDesktop the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     * @return ... 
     */
    @Field("notifyByDesktop")
    ISettings setNotifyByDesktop(boolean notifyByDesktop);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @param notifyByEmail the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     * @return ... 
     */
    @Field("notifyByEmail")
    ISettings setNotifyByEmail(boolean notifyByEmail);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @param notifyByPush the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     * @return ... 
     */
    @Field("notifyByPush")
    ISettings setNotifyByPush(boolean notifyByPush);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @param startOfWeek the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     * @return ... 
     */
    @Field("startOfWeek")
    ISettings setStartOfWeek(int startOfWeek);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     *  
     * @param timeFormat the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.Settings'] 
     * @return ... 
     */
    @Field("timeFormat")
    ISettings setTimeFormat(int timeFormat);

}
