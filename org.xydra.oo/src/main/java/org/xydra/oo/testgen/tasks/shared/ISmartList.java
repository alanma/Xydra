package org.xydra.oo.testgen.tasks.shared;

import org.xydra.oo.testgen.tasks.shared.IBaseList;
import org.xydra.oo.Field;

/** Generated on Sun Feb 24 00:02:41 CET 2013 by SpecWriter, a part of xydra.org:oo */
public interface ISmartList extends IBaseList {

    /** 
     * Generated from org.xydra.oo.testspecs.TasksSpec.SmartList. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("querySpec")
    String getQuerySpec();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.TasksSpec.SmartList. 
     *  
     * @param querySpec the value to set 
     */
    @Field("querySpec")
    void setQuerySpec(String querySpec);

}
