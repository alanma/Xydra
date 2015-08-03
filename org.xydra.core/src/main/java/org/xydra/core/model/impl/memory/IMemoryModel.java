package org.xydra.core.model.impl.memory;

import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.core.model.XModel;


public interface IMemoryModel extends XModel, IMemoryEntity, IMemoryMOFEntity {

    /**
     * @return the internal state-holding XExistsRevWritableModel
     */
    XExistsRevWritableModel getState();

    /**
     * @return the father repository. maybe can be null TODO clarify
     */
    IMemoryRepository getFather();

    /**
     * @param event
     */
    void fireModelEvent(XModelEvent event);

    /**
     * @param event
     */
    void fireObjectEvent(XObjectEvent event);

    /**
     * @param event
     */
    void fireFieldEvent(XFieldEvent event);

    /**
     * @param event
     */
    void fireTransactionEvent(XTransactionEvent event);

}
