package org.xydra.oo.testgen.tasks.shared;

import java.util.Set;
import org.xydra.base.IHasXId;
import org.xydra.oo.Field;
import org.xydra.oo.testgen.tasks.shared.IUser;

/** Generated on Thu Jul 04 11:34:29 CEST 2013 by SpecWriter, a part of xydra.org:oo */
public interface IBaseList extends IHasXId {

    /** 
     * A number to be displayed in the 'badge' of the list [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.BaseList'] 
     *  
     * @return ... 
     */
    int counter();

    /**  [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList'] */
    String icon;

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

    /**  [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList'] */
    Set<IUser> members;

    /**  [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList'] */
    String name;

    /**  [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList'] */
    IUser owner;

}
