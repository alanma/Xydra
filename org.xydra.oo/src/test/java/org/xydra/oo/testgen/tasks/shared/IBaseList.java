package org.xydra.oo.testgen.tasks.shared;

import org.xydra.base.IHasXID;
import org.xydra.oo.Field;
import java.util.Set;

/** 
 * Generated on Thu Feb 21 21:10:34 CET 2013 by  
 * PersistenceSpec2InterfacesGenerator, a part of Xydra.org  
 */
public interface IBaseList extends IHasXID {

    /** 
     * A number to be displayed in the 'badge' of the list 
     *  
     * Generated from org.xydra.oo.testspecs.TasksSpec.BaseList 
     *  
     * @return ... 
     */
    int counter();

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.BaseList. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("icon")
    String getIcon();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.BaseList. 
     *  
     * @param icon the value to set 
     */
    @Field("icon")
    void setIcon(String icon);

    /** 
     * True if the list has no members 
     *  
     * Generated from org.xydra.oo.testspecs.TasksSpec.BaseList 
     *  
     * @return ... 
     */
    boolean isPrivate();

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.BaseList 
     *  
     * @return ... 
     */
    boolean isShared();

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.BaseList. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("members")
    Set<IUser> members();

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.BaseList. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("name")
    String getName();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.BaseList. 
     *  
     * @param name the value to set 
     */
    @Field("name")
    void setName(String name);

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.BaseList. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("owner")
    IUser getOwner();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.BaseList. 
     *  
     * @param owner the value to set 
     */
    @Field("owner")
    void setOwner(IUser owner);

}
