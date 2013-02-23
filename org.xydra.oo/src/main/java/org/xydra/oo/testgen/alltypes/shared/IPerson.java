package org.xydra.oo.testgen.alltypes.shared;

import org.xydra.base.IHasXID;
import org.xydra.oo.Field;

/** Generated on Sun Feb 24 00:02:41 CET 2013 by SpecWriter, a part of xydra.org:oo */
public interface IPerson extends IHasXID {

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.Person. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("age")
    int getAge();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.Person. 
     *  
     * @param age the value to set 
     */
    @Field("age")
    void setAge(int age);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.Person. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("name")
    String getName();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.Person. 
     *  
     * @param name the value to set 
     */
    @Field("name")
    void setName(String name);

}
