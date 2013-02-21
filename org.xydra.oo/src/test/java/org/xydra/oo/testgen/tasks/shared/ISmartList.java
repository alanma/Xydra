package org.xydra.oo.testgen.tasks.shared;

import org.xydra.oo.Field;

/** 
 * Generated on Thu Feb 21 21:10:34 CET 2013 by  
 * PersistenceSpec2InterfacesGenerator, a part of Xydra.org  
 */
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
