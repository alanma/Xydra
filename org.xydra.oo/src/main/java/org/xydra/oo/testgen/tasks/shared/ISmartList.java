package org.xydra.oo.testgen.tasks.shared;

import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.Field;
import org.xydra.oo.testgen.tasks.shared.IBaseList;

/** Generated on Thu Jul 04 13:59:53 CEST 2013 by SpecWriter, a part of xydra.org:oo */
public interface ISmartList extends IBaseList {

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.TasksSpec.SmartList'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("querySpec")
    String getQuerySpec();

    /** 
     * For GWT-internal use only [generated from: 'toClassSpec 1'] 
     *  
     * @param model  [generated from: 'toClassSpec 2'] 
     * @param id  [generated from: 'toClassSpec 3'] 
     */
    void init(XWritableModel model, XId id);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.SmartList'] 
     *  
     * @param querySpec the value to set [generated from:  
     * 'org.xydra.oo.testspecs.TasksSpec.SmartList'] 
     */
    @Field("querySpec")
    void setQuerySpec(String querySpec);

}
