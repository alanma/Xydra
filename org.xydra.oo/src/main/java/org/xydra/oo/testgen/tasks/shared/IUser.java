package org.xydra.oo.testgen.tasks.shared;

import org.xydra.base.IHasXId;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.Field;
import org.xydra.oo.testgen.tasks.shared.ISettings;

/** Generated on Fri Mar 01 21:10:13 CET 2013 by SpecWriter, a part of xydra.org:oo */
public interface IUser extends IHasXId {

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.User'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("email")
    String getEmail();

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.User'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("name")
    String getName();

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.User'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("password")
    String getPassword();

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.User'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("picture")
    String getPicture();

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.User'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("settings")
    ISettings getSettings();

    /** 
     * For GWT-internal use only [generated from: 'toClassSpec 1'] 
     *  
     * @param model  [generated from: 'toClassSpec 2'] 
     * @param id  [generated from: 'toClassSpec 3'] 
     */
    void init(XWritableModel model, XId id);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.User'] 
     *  
     * @param email the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.User'] 
     */
    @Field("email")
    void setEmail(String email);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.User'] 
     *  
     * @param name the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.User'] 
     */
    @Field("name")
    void setName(String name);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.User'] 
     *  
     * @param password the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.User'] 
     */
    @Field("password")
    void setPassword(String password);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.User'] 
     *  
     * @param picture the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.User'] 
     */
    @Field("picture")
    void setPicture(String picture);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.User'] 
     *  
     * @param settings the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.User'] 
     */
    @Field("settings")
    void setSettings(ISettings settings);

}