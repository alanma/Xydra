package org.xydra.oo.testgen.alltypes.client;

import com.google.gwt.core.client.GWT;
import java.lang.Override;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.testgen.alltypes.shared.AbstractSharedFactory;
import org.xydra.oo.testgen.alltypes.shared.IHasAllType;
import org.xydra.oo.testgen.alltypes.shared.IPerson;

/** 
 * Generated on Fri Mar 01 21:10:13 CET 2013 Generated on Fri Mar 01 21:10:13 CET 2013  
 * by SpecWriter, a part of xydra.org:oo 
 */
public class GwtFactory extends AbstractSharedFactory {

    /** 
     *  [generated from: 'generateFactories-oTz9R'] 
     *  
     * @param model  [generated from: 'generateFactories-mqFtF'] 
     */
    public GwtFactory(XWritableModel model) {
        super(model);
    }

    /** 
     *  [generated from: 'generateFactories-21utp'] 
     *  
     * @param model  [generated from: 'generateFactories-Zl7Qm'] 
     * @param id  [generated from: 'generateFactories-aXRfh'] 
     * @return ... 
     */
    @Override
    protected IHasAllType getHasAllTypeInternal(XWritableModel model, XId id) {
        return wrapHasAllType(model, id);
    }

    /** 
     *  [generated from: 'generateFactories-21utp'] 
     *  
     * @param model  [generated from: 'generateFactories-Zl7Qm'] 
     * @param id  [generated from: 'generateFactories-aXRfh'] 
     * @return ... 
     */
    @Override
    protected IPerson getPersonInternal(XWritableModel model, XId id) {
        return wrapPerson(model, id);
    }

    /** 
     *  [generated from: 'generateFactories-4C46E'] 
     *  
     * @param model  [generated from: 'generateFactories-5uhaQ'] 
     * @param id  [generated from: 'generateFactories-fzf9t'] 
     * @return ... 
     */
    public static IHasAllType wrapHasAllType(XWritableModel model, XId id) {
        IHasAllType w = GWT.create(IHasAllType.class);
        w.init(model, id);
        return w;
    }

    /** 
     *  [generated from: 'generateFactories-4C46E'] 
     *  
     * @param model  [generated from: 'generateFactories-5uhaQ'] 
     * @param id  [generated from: 'generateFactories-fzf9t'] 
     * @return ... 
     */
    public static IPerson wrapPerson(XWritableModel model, XId id) {
        IPerson w = GWT.create(IPerson.class);
        w.init(model, id);
        return w;
    }

}
