package org.xydra.oo.testgen.tasks.shared;

import java.util.List;

import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.Field;

/** Generated on Fri Dec 27 13:29:45 CET 2013 by SpecWriter, a part of xydra.org:oo */
public interface ITaskList extends IBaseList {

    /** 
     * For GWT-internal use only [generated from: 'toClassSpec 1'] 
     *  
     * @param model  [generated from: 'toClassSpec 2'] 
     * @param id  [generated from: 'toClassSpec 3'] 
     */
    void init(XWritableModel model, XId id);

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.TaskList'] 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("tasks")
    List<ITask> tasks();

}
