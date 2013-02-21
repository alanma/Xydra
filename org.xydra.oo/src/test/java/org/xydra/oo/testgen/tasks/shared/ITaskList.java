package org.xydra.oo.testgen.tasks.shared;

import java.util.List;
import org.xydra.oo.Field;

/** 
 * Generated on Thu Feb 21 21:10:34 CET 2013 by  
 * PersistenceSpec2InterfacesGenerator, a part of Xydra.org  
 */
public interface ITaskList extends IBaseList {

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.TaskList. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("tasks")
    List<ITask> tasks();

}
