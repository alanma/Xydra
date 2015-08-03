package org.xydra.oo.testgen.tasks.shared;

import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.Field;

/** Generated on Tue Oct 21 21:47:37 CEST 2014 by SpecWriter, a part of xydra.org:oo */
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
    @Override
	void init(XWritableModel model, XId id);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.TasksSpec.SmartList']
     *
     * @param querySpec the value to set [generated from:
     * 'org.xydra.oo.testspecs.TasksSpec.SmartList']
     * @return ...
     */
    @Field("querySpec")
    ISmartList setQuerySpec(String querySpec);

}
