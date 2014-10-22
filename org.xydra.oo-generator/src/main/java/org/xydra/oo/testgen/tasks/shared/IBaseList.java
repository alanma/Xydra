package org.xydra.oo.testgen.tasks.shared;

import java.util.Set;

import org.xydra.base.IHasXId;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.Field;

/** Generated on Tue Oct 21 21:47:37 CEST 2014 by SpecWriter, a part of xydra.org:oo */
public interface IBaseList extends IHasXId {

    /** 
     * A number to be displayed in the 'badge' of the list [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.BaseList'] 
     *  
     * @return ... 
     */
    int counter();

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("icon")
    String getIcon();

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("name")
    String getName();

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("owner")
    IUser getOwner();

    /**  [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList'] */
    String icon;

    /** 
     * For GWT-internal use only [generated from: 'toClassSpec 1'] 
     *  
     * @param model  [generated from: 'toClassSpec 2'] 
     * @param id  [generated from: 'toClassSpec 3'] 
     */
    void init(XWritableModel model, XId id);

    /** 
     * True if the list has no members [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.BaseList'] 
     *  
     * @return ... 
     */
    boolean isPrivate();

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList'] 
     *  
     * @return ... 
     */
    boolean isShared();

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList'] 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("members")
    Set<IUser> members();

    /**  [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList'] */
    Set<IUser> members;

    /**  [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList'] */
    String name;

    /**  [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList'] */
    IUser owner;

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.BaseList'] 
     *  
     * @param icon the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.BaseList'] 
     * @return ... 
     */
    @Field("icon")
    IBaseList setIcon(String icon);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.BaseList'] 
     *  
     * @param name the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.BaseList'] 
     * @return ... 
     */
    @Field("name")
    IBaseList setName(String name);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.BaseList'] 
     *  
     * @param owner the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.BaseList'] 
     * @return ... 
     */
    @Field("owner")
    IBaseList setOwner(IUser owner);

}
