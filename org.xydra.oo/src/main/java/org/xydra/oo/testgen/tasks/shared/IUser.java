package org.xydra.oo.testgen.tasks.shared;

import org.xydra.base.IHasXID;
import org.xydra.oo.Field;

/** Generated on Sun Feb 24 00:02:41 CET 2013 by SpecWriter, a part of xydra.org:oo */
public interface IUser extends IHasXID {

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.User. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("email")
    String getEmail();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.User. 
     *  
     * @param email the value to set 
     */
    @Field("email")
    void setEmail(String email);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.User. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("name")
    String getName();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.User. 
     *  
     * @param name the value to set 
     */
    @Field("name")
    void setName(String name);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.User. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("password")
    String getPassword();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.User. 
     *  
     * @param password the value to set 
     */
    @Field("password")
    void setPassword(String password);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.User. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("picture")
    String getPicture();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.User. 
     *  
     * @param picture the value to set 
     */
    @Field("picture")
    void setPicture(String picture);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.User. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("settings")
    ISettings getSettings();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.User. 
     *  
     * @param settings the value to set 
     */
    @Field("settings")
    void setSettings(ISettings settings);

}
