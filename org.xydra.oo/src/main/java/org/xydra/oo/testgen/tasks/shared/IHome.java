package org.xydra.oo.testgen.tasks.shared;

import org.xydra.base.IHasXID;
import org.xydra.oo.Field;

/** Generated on Sun Feb 24 00:02:41 CET 2013 by SpecWriter, a part of xydra.org:oo */
public interface IHome extends IHasXID {

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Home. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("inbox")
    IBaseList getInbox();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Home. 
     *  
     * @param inbox the value to set 
     */
    @Field("inbox")
    void setInbox(IBaseList inbox);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Home. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("starred")
    IBaseList getStarred();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Home. 
     *  
     * @param starred the value to set 
     */
    @Field("starred")
    void setStarred(IBaseList starred);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Home. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("today")
    IBaseList getToday();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Home. 
     *  
     * @param today the value to set 
     */
    @Field("today")
    void setToday(IBaseList today);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Home. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("week")
    IBaseList getWeek();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.Home. 
     *  
     * @param week the value to set 
     */
    @Field("week")
    void setWeek(IBaseList week);

}
