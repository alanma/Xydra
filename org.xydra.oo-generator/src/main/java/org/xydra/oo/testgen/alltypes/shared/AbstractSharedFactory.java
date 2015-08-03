package org.xydra.oo.testgen.alltypes.shared;

import org.xydra.base.Base;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.XX;
import org.xydra.oo.runtime.shared.AbstractFactory;
import org.xydra.oo.testgen.alltypes.shared.IHasAllType;
import org.xydra.oo.testgen.alltypes.shared.IPerson;

/** Generated on Tue Oct 21 22:14:27 CEST 2014 by SpecWriter, a part of xydra.org:oo */
public abstract class AbstractSharedFactory extends AbstractFactory {

    /**
     *  [generated from: 'generateFactories-HqDUE']
     *
     * @param model  [generated from: 'generateFactories-76emU']
     */
    public AbstractSharedFactory(final XWritableModel model) {
        super(model);
    }

    /**
     *  [generated from: 'generateFactories-gn0BV']
     *
     * @param idStr  [generated from: 'generateFactories-myHOp']
     * @return ...
     */
    public IHasAllType createHasAllType(final String idStr) {
        return createHasAllType(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-2m6U1']
     *
     * @param id  [generated from: 'generateFactories-n8c8H']
     * @return ...
     */
    public IHasAllType createHasAllType(final XId id) {
        if(!hasXObject(id)) { createXObject(id); }
        return getHasAllTypeInternal(this.model, id);
    }

    /**
     *  [generated from: 'generateFactories-gn0BV']
     *
     * @param idStr  [generated from: 'generateFactories-myHOp']
     * @return ...
     */
    public IPerson createPerson(final String idStr) {
        return createPerson(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-2m6U1']
     *
     * @param id  [generated from: 'generateFactories-n8c8H']
     * @return ...
     */
    public IPerson createPerson(final XId id) {
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
    public IHasAllType getHasAllType(final String idStr) {
        return getHasAllType(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-Xz0r2']
     *
     * @param id  [generated from: 'generateFactories-usnO3']
     * @return ...
     */
    public IHasAllType getHasAllType(final XId id) {
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
    public IPerson getPerson(final String idStr) {
        return getPerson(Base.toId(idStr));
    }

    /**
     *  [generated from: 'generateFactories-Xz0r2']
     *
     * @param id  [generated from: 'generateFactories-usnO3']
     * @return ...
     */
    public IPerson getPerson(final XId id) {
        if (!hasXObject(id)) { return null; }
        return getPersonInternal( this.model, id);
    }

}
