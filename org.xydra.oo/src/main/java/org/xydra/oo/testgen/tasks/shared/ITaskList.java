package org.xydra.oo.testgen.tasks.shared;

import java.util.List;
import org.xydra.oo.testgen.tasks.shared.IBaseList;
import org.xydra.oo.Field;

/** Generated on Sun Feb 24 00:02:41 CET 2013 by SpecWriter, a part of xydra.org:oo */
public interface ITaskList extends IBaseList {

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.TaskList. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("tasks")
    List<ITask> tasks();

}
