package org.xydra.base.rmof.impl.memstate;

import java.io.Serializable;

import org.xydra.base.XAddress;
import org.xydra.base.rmof.XEntity;


public abstract class MemStateEntity implements Serializable, XEntity {

    private static final long serialVersionUID = -8935900909094851790L;

    // not final for GWT serialisation
    private XAddress address;

    /* Just for GWT */
    protected MemStateEntity() {
    }

    protected MemStateEntity(final XAddress address) {
        this.address = address;
    }

    @Override
    public int hashCode() {
        return getAddress().hashCode();
    }

    @Override
    public XAddress getAddress() {
        return this.address;
    }

}
