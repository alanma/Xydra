package org.xydra.oo.testgen.alltypes.java;

import java.lang.Override;
import java.lang.reflect.Proxy;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.runtime.java.OOJavaOnlyProxy;
import org.xydra.oo.testgen.alltypes.shared.AbstractSharedFactory;
import org.xydra.oo.testgen.alltypes.shared.IHasAllType;
import org.xydra.oo.testgen.alltypes.shared.IPerson;

/** Generated on Fri Mar 01 21:10:13 CET 2013 by SpecWriter, a part of xydra.org:oo */
public class JavaFactory extends AbstractSharedFactory {

    /** 
     *  [generated from: 'generateFactories-WaYjk'] 
     *  
     * @param model  [generated from: 'generateFactories-dctg4'] 
     */
    public JavaFactory(XWritableModel model) {
        super(model);
    }

    /** 
     *  [generated from: 'generateFactories-Kus1F'] 
     *  
     * @param model  [generated from: 'generateFactories-PIZOa'] 
     * @param id  [generated from: 'generateFactories-vxB4a'] 
     * @return ... 
     */
    @Override
    protected IHasAllType getHasAllTypeInternal(XWritableModel model, XId id) {
        IHasAllType w = (IHasAllType) Proxy.newProxyInstance(IHasAllType.class.getClassLoader(),
            new Class<?>[] { IHasAllType.class }, new OOJavaOnlyProxy(model, id));
        return w;
    }

    /** 
     *  [generated from: 'generateFactories-Kus1F'] 
     *  
     * @param model  [generated from: 'generateFactories-PIZOa'] 
     * @param id  [generated from: 'generateFactories-vxB4a'] 
     * @return ... 
     */
    @Override
    protected IPerson getPersonInternal(XWritableModel model, XId id) {
        IPerson w = (IPerson) Proxy.newProxyInstance(IPerson.class.getClassLoader(),
            new Class<?>[] { IPerson.class }, new OOJavaOnlyProxy(model, id));
        return w;
    }

}
