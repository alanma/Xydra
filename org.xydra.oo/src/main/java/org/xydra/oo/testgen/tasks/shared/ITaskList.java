package org.xydra.oo.testgen.tasks.shared;

import java.util.List;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.Field;
import org.xydra.oo.testgen.tasks.shared.IBaseList;
import org.xydra.oo.testgen.tasks.shared.ITask;

/** Generated on Fri Jul 04 01:02:18 CEST 2014 by SpecWriter, a part of xydra.org:oo */
public interface ITaskList extends IBaseList {

    /** 
     * For GWT-internal use only [generated from: 'toClassSpec 1'] 
     *  
     * @param model  [generated from: 'toClassSpec 2'] 
     * @param id  [generated from: 'toClassSpec 3'] 
     */
    @Override
	void init(XWritableModel model, XId id);

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.TaskList'] 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("tasks")
    List<ITask> tasks();

}
