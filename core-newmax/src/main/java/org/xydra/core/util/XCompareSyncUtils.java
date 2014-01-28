package org.xydra.core.util;

import org.xydra.base.XCompareUtils;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.core.model.impl.memory.sync.ISyncLog;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


public class XCompareSyncUtils extends XCompareUtils {
    
    private static final Logger log = LoggerFactory.getLogger(XCompareSyncUtils.class);
    
    /**
     * @param modelA
     * @param modelB
     * @return true if both models have the same sync log
     */
    public static boolean equalHistory(XReadableModel modelA, XReadableModel modelB) {
        boolean equalState = equalState(modelA, modelB);
        if(equalState) {
            ISyncLog syncLogA = (ISyncLog)((XSynchronizesChanges)modelA).getChangeLog();
            ISyncLog syncLogB = (ISyncLog)((XSynchronizesChanges)modelB).getChangeLog();
            
            return syncLogA.equals(syncLogB);
        } else
            return false;
    }
    
    /**
     * @param objectA
     * @param objectB
     * @return true if both objects have the same sync log
     */
    public static boolean equalHistory(XReadableObject objectA, XObject objectB) {
        boolean equalState = equalState(objectA, objectB);
        if(equalState) {
            ISyncLog syncLogA = (ISyncLog)((XSynchronizesChanges)objectA).getChangeLog();
            ISyncLog syncLogB = (ISyncLog)((XSynchronizesChanges)objectB).getChangeLog();
            
            return syncLogA.equals(syncLogB);
        } else
            return false;
    }
    
    /**
     * @param repoA
     * @param repoB
     * @return true if both repos models have the same sync log
     */
    public static boolean equalHistory(XReadableRepository repoA, XRepository repoB) {
        boolean equal = true;
        equal = equalState(repoA, repoB);
        if(!equal) {
            return false;
        }
        for(XId modelId : repoA) {
            XReadableModel modelA = repoA.getModel(modelId);
            XReadableModel modelB = repoB.getModel(modelId);
            
            if(!equalHistory(modelA, modelB)) {
                if(log.isDebugEnabled()) log.debug("Model " + modelId + " has different sync log");
                return false;
            }
        }
        return true;
    }
    
}
