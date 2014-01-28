package org.xydra.store.impl.gae;

import org.xydra.base.XAddress;
import org.xydra.core.model.XModel;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.ModelRevision;
import org.xydra.store.impl.gae.changes.GaeModelRevision;
import org.xydra.store.impl.gae.changes.RevisionInfo;
import org.xydra.xgae.annotations.XGaeOperation;
import org.xydra.xgae.util.XGaeDebugHelper;

import com.google.common.cache.Cache;


/**
 * The {@link InstanceRevisionManager} is
 * <ol>
 * <li>one instance per {@link XModel}.</li>
 * <li>a facade and manager for the Instance-wide cache</li>
 * <li>passive, it does not trigger any kind of recalculation.</li>
 * </ol>
 * 
 * The instance revision cache is shared among all objects within one Java
 * Virtual Machine via the {@link InstanceContext}. Instance-wide shared state
 * is managed as a {@link RevisionInfo}.
 * 
 * ----
 * 
 * These values are managed:
 * <ul>
 * <li>LastTaken (shared on instance)</li>
 * <li>LastCommitted (shared on instance)</li>
 * <li>GaeModelRevision (shared on instance). {@link GaeModelRevision} has
 * <ul>
 * <li>LastSilentCommited</li>
 * <li> {@link ModelRevision} (currentRevision, modelExists)</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * A model has a <em>current revision number</em> (Current). It is incremented
 * every time a change operation succeeds. Not necessarily only one step.
 * 
 * For each value, the revision cache maintains a shared minimal value, which
 * can be re-used among all threads as a starting point to compute the
 * thread-local variables. TODO rewrite
 * 
 * These invariants are true (lowercase = estimated values, uppercase = real
 * values):
 * 
 * Estimates are less or equal to the real value: currentRev <= CURRENT_REV;
 * lastCommited <= LAST_COMMITED; lastTaken <= LAST_TAKEN;
 * 
 * currentRev <= lastSilentCommitted <= lastCommited <= lastTaken;
 * 
 * CURRENT_REV <= LAST_COMMITED <= LAST_TAKEN;
 * 
 * The following diagram shows an example:
 * 
 * <pre>
 *                  | Possible Status:
 *                  |   Success = (SuccessExecuted)
 *                  |   Fail    = (SuccNoChg | FailPre | FailTimeout)
 * Revision         | Creating | Success |  Fail     |
 * -----------------+----------+---------+-----------+
 * ...              |    ...                         |
 * r102             |    No change entity exists for this revision number    |
 * r101             |    No change entity exists for this revision number    |
 * r100             |    No change entity exists for this revision number    |
 *    LAST_TAKEN (L) = 99
 * r99              |   ???    |   ----  | ????????? |
 * r98              |   ???    |   ----  | ????????? |
 * r97              |   ???    |   ----  | ????????? |
 *    TENTATIVE (T)  = 96 (this highest SuccExe with no SuccExe above)
 * r96              |   ---    |   xxx   |  -------- |
 * r96              |   ???????????????????????????? |
 * r95              |   ???????????????????????????? |
 * r94              |   xxx    | ------  | -------   |
 *    COMMITTED (C) = 93 (the highest commit with no creating below it)
 * r93              |   ---    |   ????????????????? |
 * r92              |   ---    |   ????????????????? |
 * r91              |   ---    |   ---   |   xxx     |
 *    CURRENT_REV (R) = 90 (the highest *Successful* commit with no 'Creating' below it)
 * r90              |   ---    |   xxx   |  ------   |
 * r89              |   ---    |   ????????????????? |
 * r88              |   ---    |   ????????????????? |
 * r87              |   ---    |   ????????????????? |
 * r86              |   ---    |   ????????????????? |
 * r85              |   ---    |   ????????????????? |
 *    A potential lastSilentCommitted of the candidate A = not successful
 * r84              |   ---    |   ---   |  xxx      |
 * r83              |   ---    |   ---   |  xxx      |
 * r82              |   ---    |   ---   |  xxx      |
 *    A potential candidate A for a currentRev = 81
 * r81              |   ---    |   xxx   |  ------   |
 * r80              |   ---    |   ????????????????? |
 * r79              |   ---    |   ????????????????? |
 * r78              |   ---    |   ????????????????? |
 * ...              |   ---    |   ????????????????? |
 * r00              |   ---    |   ????????????????? |
 * -----------------+----------+---------+-----------+
 * </pre>
 * 
 * TODO must be thread-safe
 * 
 * @author xamde
 */
public class InstanceRevisionManager {
    
    private static final Logger log = LoggerFactory.getLogger(InstanceRevisionManager.class);
    
    /** For debug strings */
    private static final String REVMANAGER_NAME = "[.rev]";
    
    private final XAddress modelAddress;
    
    /**
     * @param modelAddress ..
     */
    @XGaeOperation()
    public InstanceRevisionManager(XAddress modelAddress) {
        log.debug(XGaeDebugHelper.init(REVMANAGER_NAME));
        this.modelAddress = modelAddress;
        assert this.getInstanceRevisionInfo() != null;
        assert this.getInstanceRevisionInfo().getGaeModelRevision() != null;
        assert this.getInstanceRevisionInfo().getGaeModelRevision().getModelRevision() != null;
    }
    
    /**
     * @return the instance-wide cache {@link RevisionInfo} for the modelAddress
     *         of this {@link InstanceRevisionManager}. Never null. If no cached
     *         info was found, a "know nothing"-entry is created, locally
     *         cached, and returned.
     */
    public RevisionInfo getInstanceRevisionInfo() {
        Cache<String,Object> instanceContext = InstanceContext.getInstanceCache();
        String key = this.modelAddress + "/revisions";
        synchronized(instanceContext) {
            RevisionInfo instanceRevInfo = (RevisionInfo)instanceContext.getIfPresent(key);
            if(instanceRevInfo == null) {
                instanceRevInfo = new RevisionInfo(".instance-rev" + this.modelAddress);
                instanceContext.put(key, instanceRevInfo);
            }
            return instanceRevInfo;
        }
    }
    
    @Override
    public String toString() {
        return this.modelAddress + ":: instance:" + getInstanceRevisionInfo();
    }
    
}
