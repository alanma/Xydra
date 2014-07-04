package org.xydra.core;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;


/**
 * A utility class for using {@link XId} and {@link XAddress}.
 * 
 * @author voelkel
 * @author Kaidel
 * @author dscharrer
 */
public class XX extends Base {
    
    /**
     * Use {@link XCopyUtils#copyObject(XId, String, XReadableObject)} if the
     * resulting object should not be backed by the XReadableObject.
     * 
     * @param actor The session actor to use for the returned object.
     * @param password The password corresponding to the given actor.
     * @param objectSnapshot
     * @return an object with the same initial state as the given object
     *         snapshot. The returned object may be backed by the provided
     *         XReadableObject instance, so it should no longer be modified
     *         directly or the behavior of the model is undefined.
     */
    public static XObject wrap(XId actor, String password, XReadableObject objectSnapshot) {
        if(objectSnapshot instanceof XRevWritableObject) {
            return new MemoryObject(actor, password, (XRevWritableObject)objectSnapshot);
        } else {
            return XCopyUtils.copyObject(actor, password, objectSnapshot);
        }
    }
    
    /**
     * Use {@link XCopyUtils#copyModel(XId, String, XReadableModel)} if the
     * resulting model should not be backed by the XReadableModel.
     * 
     * @param actor The session actor to use for the returned model.
     * @param password The password corresponding to the given actor.
     * @param modelSnapshot
     * @return a model with the same initial state as the given model snapshot.
     *         The returned model may be backed by the provided XReadableModel
     *         instance, so it should no longer be modified directly or the
     *         behavior of the model is undefined.
     */
    public static XModel wrap(XId actor, String password, XReadableModel modelSnapshot) {
        if(modelSnapshot instanceof XRevWritableModel) {
            return new MemoryModel(actor, password, (XRevWritableModel)modelSnapshot);
        } else {
            return XCopyUtils.copyModel(actor, password, modelSnapshot);
        }
    }
    
}
