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
    public static boolean equalHistory(final XReadableModel modelA, final XReadableModel modelB) {
        final boolean equalState = equalState(modelA, modelB);
        if(equalState) {
            final ISyncLog syncLogA = (ISyncLog)((XSynchronizesChanges)modelA).getChangeLog();
            final ISyncLog syncLogB = (ISyncLog)((XSynchronizesChanges)modelB).getChangeLog();

            return syncLogA.equals(syncLogB);
        } else {
			return false;
		}
    }

    /**
     * @param objectA
     * @param objectB
     * @return true if both objects have the same sync log
     */
    public static boolean equalHistory(final XReadableObject objectA, final XObject objectB) {
        final boolean equalState = equalState(objectA, objectB);
        if(equalState) {
            final ISyncLog syncLogA = (ISyncLog)((XSynchronizesChanges)objectA).getChangeLog();
            final ISyncLog syncLogB = (ISyncLog)((XSynchronizesChanges)objectB).getChangeLog();

            return syncLogA.equals(syncLogB);
        } else {
			return false;
		}
    }

    /**
     * @param repoA
     * @param repoB
     * @return true if both repos models have the same sync log
     */
    public static boolean equalHistory(final XReadableRepository repoA, final XRepository repoB) {
        boolean equal = true;
        equal = equalState(repoA, repoB);
        if(!equal) {
            return false;
        }
        for(final XId modelId : repoA) {
            final XReadableModel modelA = repoA.getModel(modelId);
            final XReadableModel modelB = repoB.getModel(modelId);

            if(!equalHistory(modelA, modelB)) {
                if(log.isDebugEnabled()) {
					log.debug("Model " + modelId + " has different sync log");
				}
                return false;
            }
        }
        return true;
    }

}
