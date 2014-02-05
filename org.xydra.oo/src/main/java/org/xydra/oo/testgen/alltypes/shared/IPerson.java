package org.xydra.oo.testgen.alltypes.shared;

import org.xydra.base.IHasXId;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.Field;
import org.xydra.oo.testgen.alltypes.shared.IPerson;

/** Generated on Wed Feb 05 16:34:31 CET 2014 by SpecWriter, a part of xydra.org:oo */
public interface IPerson extends IHasXId {

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.Person'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("age")
    int getAge();

    /** 
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.Person'] 
     *  
     * @return the current value or null if not defined 
     */
    @Field("name")
    String getName();

    /** 
     * For GWT-internal use only [generated from: 'toClassSpec 1'] 
     *  
     * @param model  [generated from: 'toClassSpec 2'] 
     * @param id  [generated from: 'toClassSpec 3'] 
     */
    void init(XWritableModel model, XId id);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.AllTypesSpec.Person'] 
     *  
     * @param age the value to set [generated from:  
     * 'org.xydra.oo.testspecs.AllTypesSpec.Person'] 
     * @return ... 
     */
    @Field("age")
    IPerson setAge(int age);

    /** 
     * Set a value, silently overwriting existing values, if any. [generated from:  
     * 'org.xydra.oo.testspecs.AllTypesSpec.Person'] 
     *  
     * @param name the value to set [generated from:  
     * 'org.xydra.oo.testspecs.AllTypesSpec.Person'] 
     * @return ... 
     */
    @Field("name")
    IPerson setName(String name);

}
