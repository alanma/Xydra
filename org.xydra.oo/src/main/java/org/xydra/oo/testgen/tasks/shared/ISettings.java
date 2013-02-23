package org.xydra.oo.testgen.tasks.shared;

import org.xydra.base.IHasXID;
import org.xydra.oo.Field;

/** Generated on Sun Feb 24 00:02:41 CET 2013 by SpecWriter, a part of xydra.org:oo */
public interface ISettings extends IHasXID {

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Settings. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("language")
    String getLanguage();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Settings. 
     *  
     * @param language the value to set 
     */
    @Field("language")
    void setLanguage(String language);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Settings. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("newItemsAtTop")
    boolean getNewItemsAtTop();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Settings. 
     *  
     * @param newItemsAtTop the value to set 
     */
    @Field("newItemsAtTop")
    void setNewItemsAtTop(boolean newItemsAtTop);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Settings. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("notifyByDesktop")
    boolean getNotifyByDesktop();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Settings. 
     *  
     * @param notifyByDesktop the value to set 
     */
    @Field("notifyByDesktop")
    void setNotifyByDesktop(boolean notifyByDesktop);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Settings. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("notifyByEmail")
    boolean getNotifyByEmail();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Settings. 
     *  
     * @param notifyByEmail the value to set 
     */
    @Field("notifyByEmail")
    void setNotifyByEmail(boolean notifyByEmail);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Settings. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("notifyByPush")
    boolean getNotifyByPush();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Settings. 
     *  
     * @param notifyByPush the value to set 
     */
    @Field("notifyByPush")
    void setNotifyByPush(boolean notifyByPush);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Settings. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("startOfWeek")
    int getStartOfWeek();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Settings. 
     *  
     * @param startOfWeek the value to set 
     */
    @Field("startOfWeek")
    void setStartOfWeek(int startOfWeek);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Settings. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("timeFormat")
    int getTimeFormat();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Settings. 
     *  
     * @param timeFormat the value to set 
     */
    @Field("timeFormat")
    void setTimeFormat(int timeFormat);

}
