package org.xydra.oo.testgen.alltypes.shared;

import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.XX;
import org.xydra.oo.runtime.shared.AbstractFactory;

/** Generated on Fri Dec 27 13:29:45 CET 2013 by SpecWriter, a part of xydra.org:oo */
public abstract class AbstractSharedFactory extends AbstractFactory {

    /** 
     *  [generated from: 'generateFactories-HqDUE'] 
     *  
     * @param model  [generated from: 'generateFactories-76emU'] 
     */
    public AbstractSharedFactory(XWritableModel model) {
        super(model);
    }

    /** 
     *  [generated from: 'generateFactories-gn0BV'] 
     *  
     * @param idStr  [generated from: 'generateFactories-myHOp'] 
     * @return ... 
     */
    public IHasAllType createHasAllType(String idStr) {
        return createHasAllType(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-2m6U1'] 
     *  
     * @param id  [generated from: 'generateFactories-n8c8H'] 
     * @return ... 
     */
    public IHasAllType createHasAllType(XId id) {
        if(!hasXObject(id)) { createXObject(id); }
        return getHasAllTypeInternal(this.model, id);
    }

    /** 
     *  [generated from: 'generateFactories-gn0BV'] 
     *  
     * @param idStr  [generated from: 'generateFactories-myHOp'] 
     * @return ... 
     */
    public IPerson createPerson(String idStr) {
        return createPerson(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-2m6U1'] 
     *  
     * @param id  [generated from: 'generateFactories-n8c8H'] 
     * @return ... 
     */
    public IPerson createPerson(XId id) {
        if(!hasXObject(id)) { createXObject(id); }
        return getPersonInternal(this.model, id);
    }

    /** 
     *  [generated from: 'generateFactories-n6Zvf'] 
     *  
     * @param model  [generated from: 'generateFactories-bnAB6'] 
     * @param id  [generated from: 'generateFactories-SEML4'] 
     * @return ... 
     */
    protected abstract IHasAllType getHasAllTypeInternal(XWritableModel model, XId id);

    /** 
     *  [generated from: 'generateFactories-UN0yH'] 
     *  
     * @param idStr  [generated from: 'generateFactories-2QL32'] 
     * @return ... 
     */
    public IHasAllType getHasAllType(String idStr) {
        return getHasAllType(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-Xz0r2'] 
     *  
     * @param id  [generated from: 'generateFactories-usnO3'] 
     * @return ... 
     */
    public IHasAllType getHasAllType(XId id) {
        if (!hasXObject(id)) { return null; }
        return getHasAllTypeInternal( this.model, id); 
    }

    /** 
     *  [generated from: 'generateFactories-n6Zvf'] 
     *  
     * @param model  [generated from: 'generateFactories-bnAB6'] 
     * @param id  [generated from: 'generateFactories-SEML4'] 
     * @return ... 
     */
    protected abstract IPerson getPersonInternal(XWritableModel model, XId id);

    /** 
     *  [generated from: 'generateFactories-UN0yH'] 
     *  
     * @param idStr  [generated from: 'generateFactories-2QL32'] 
     * @return ... 
     */
    public IPerson getPerson(String idStr) {
        return getPerson(XX.toId(idStr));
    }

    /** 
     *  [generated from: 'generateFactories-Xz0r2'] 
     *  
     * @param id  [generated from: 'generateFactories-usnO3'] 
     * @return ... 
     */
    public IPerson getPerson(XId id) {
        if (!hasXObject(id)) { return null; }
        return getPersonInternal( this.model, id); 
    }

}
