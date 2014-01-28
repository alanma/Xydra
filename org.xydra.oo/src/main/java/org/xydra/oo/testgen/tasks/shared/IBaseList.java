package org.xydra.oo.testgen.tasks.shared;

import java.util.Set;

import org.xydra.base.IHasXId;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;


/**
 * Generated on Fri Dec 27 13:29:45 CET 2013 by SpecWriter, a part of
 * xydra.org:oo
 */
public interface IBaseList extends IHasXId {
    
    /**
     * A number to be displayed in the 'badge' of the list [generated from:
     * 'org.xydra.oo.testspecs.TasksSpec.BaseList']
     * 
     * @return ...
     */
    int counter();
    
    /** [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList'] */
    String icon = null;
    
    /**
     * True if the list has no members [generated from:
     * 'org.xydra.oo.testspecs.TasksSpec.BaseList']
     * 
     * @return ...
     */
    boolean isPrivate();
    
    /**
     * [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList']
     * 
     * @return ...
     */
    boolean isShared();
    
    /** [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList'] */
    Set<IUser> members = null;
    
    /** [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList'] */
    String name = null;
    
    /** [generated from: 'org.xydra.oo.testspecs.TasksSpec.BaseList'] */
    IUser owner = null;
    
    void init(XWritableModel model, XId id);
    
}
