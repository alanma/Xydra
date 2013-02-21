package org.xydra.oo.testgen.tasks.shared;

import org.xydra.base.IHasXID;
import java.util.List;
import org.xydra.oo.Field;

/** 
 * Generated on Thu Feb 21 21:10:34 CET 2013 by  
 * PersistenceSpec2InterfacesGenerator, a part of Xydra.org  
 */
public interface ITask extends IHasXID {

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("rECENTLY_COMPLETED")
    long getRECENTLY_COMPLETED();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task. 
     *  
     * @param rECENTLY_COMPLETED the value to set 
     */
    @Field("rECENTLY_COMPLETED")
    void setRECENTLY_COMPLETED(long rECENTLY_COMPLETED);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("checked")
    boolean getChecked();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task. 
     *  
     * @param checked the value to set 
     */
    @Field("checked")
    void setChecked(boolean checked);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("completionDate")
    long getCompletionDate();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task. 
     *  
     * @param completionDate the value to set 
     */
    @Field("completionDate")
    void setCompletionDate(long completionDate);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("dueDate")
    long getDueDate();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task. 
     *  
     * @param dueDate the value to set 
     */
    @Field("dueDate")
    void setDueDate(long dueDate);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task 
     *  
     * @return ... 
     */
    boolean isRecentlyCompleted();

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("note")
    String getNote();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task. 
     *  
     * @param note the value to set 
     */
    @Field("note")
    void setNote(String note);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("remindDate")
    long getRemindDate();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task. 
     *  
     * @param remindDate the value to set 
     */
    @Field("remindDate")
    void setRemindDate(long remindDate);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("starred")
    boolean getStarred();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task. 
     *  
     * @param starred the value to set 
     */
    @Field("starred")
    void setStarred(boolean starred);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("subTasks")
    List<ITask> subTasks();

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("title")
    String getTitle();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Task. 
     *  
     * @param title the value to set 
     */
    @Field("title")
    void setTitle(String title);

}
