package org.xydra.store.impl.gae;

import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.core.model.XModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.ModelRevision;
import org.xydra.store.impl.gae.changes.GaeModelRevision;
import org.xydra.store.impl.gae.changes.RevisionInfo;
import org.xydra.store.impl.gae.changes.ThreadLocalGaeModelRevision;


/**
 * The {@link RevisionManager} is
 * <ol>
 * <li>a facade and manager for the two revision caches: Instance-wide and
 * Thread-local.</li>
 * <li>passive, it does not trigger any kind of recalculation.</li>
 * <li>one instance per {@link XModel}.</li>
 * </ol>
 * 
 * The instance revision cache is shared among all objects within one Java
 * Virtual Machine via the {@link InstanceContext}. Instance-wide shared state
 * is managed as a {@link RevisionInfo}.
 * 
 * Thread-local state is a {@link ThreadLocalGaeModelRevision}.
 * 
 * ----
 * 
 * These values are managed:
 * <ul>
 * <li>LastTaken (shared on instance)</li>
 * <li>LastCommitted (shared on instance)</li>
 * <li>GaeModelRevision (shared on instance + each thread can have its own
 * copy). {@link GaeModelRevision} has
 * <ul>
 * <li>LastSilentCommited</li>
 * <li> {@link ModelRevision} (currentRevision + modelExists)</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * A model has a <em>current revision number</em> (Current). It is incremented
 * every time a change operation succeeds. Not necessarily only one step.
 * 
 * For each value, the revision cache maintains a shared minimal value, which
 * can be re-used among all threads as a starting point to compute the
 * thread-local variables.
 * 
 * Each thread has its own view on currentRevision and modelExists. This ensures
 * a read-your-own-writes behaviour within one instance.
 * 
 * These invariants are true (lowercase = estimated values, uppercase = real
 * values): currentRev <= CURRENT_REV; lastCommited <= LAST_COMMITED; lastTaken
 * <= LAST_TAKEN; currentRev <= lastSilentCommitted <= lastCommited <=
 * lastTaken; CURRENT_REV <= LAST_COMMITED <= LAST_TAKEN;
 * 
 * The following diagram shows an example:
 * 
 * <pre>
 * Possible Status: | Creating | SuccExe | SuccNoChg | FailPre | FailTimeout |
 * -----------------+----------+---------+-----------+---------+-------------+
 * ...              |    ...                                                 |
 * r99              |    No change entity exists for this revision number    |
 * r98              |    No change entity exists for this revision number    |
 * r97              |    No change entity exists for this revision number    |
 * 
 * LAST_TAKEN (L) = 96
 * 
 * r96              |   ????????????????????????????????????????????????     |
 * r95              |   ????????????????????????????????????????????????     |
 * r94              |   xxx?????????????????????????????????????????????     |
 * 
 * COMMITTED (C) = 93 (the highest commit with no creating under it)
 * 
 * r93              |   ---    |   ?????????????????????????????????????     |
 * r92              |   ---    |   ?????????????????????????????????????     |
 *  
 * r91              |   ---    |   ---??????????????????????????????????     |
 * 
 * CURRENT_REV (R) = 90 (the highest successful commit with no creating under it)
 * 
 * r90              |   ---    |   xxx   |    --------------------------     |
 * r89              |   ---    |   ?????????????????????????????????????     |
 * r88              |   ---    |   ?????????????????????????????????????     |
 * r87              |   ---    |   ?????????????????????????????????????     |
 * r86              |   ---    |   ?????????????????????????????????????     |
 * r85              |   ---    |   ?????????????????????????????????????     |
 * 
 * A potential lastSilentCommitted of the candidate A
 * 
 * r84              |   ---    |   ---??????????????????????????????????     |
 * r83              |   ---    |   ---??????????????????????????????????     |
 * r82              |   ---    |   ---??????????????????????????????????     |
 * 
 * A potential candidate A for a currentRev = 81
 * 
 * r81              |   ---    |   xxx??????????????????????????????????     |
 * r80              |   ---    |   ?????????????????????????????????????     |
 * r79              |   ---    |   ?????????????????????????????????????     |
 * r78              |   ---    |   ?????????????????????????????????????     |
 * ...              |   ---    |   ?????????????????????????????????????     |
 * r00              |   ---    |   ?????????????????????????????????????     |
 * -----------------+----------+---------+-----------+---------+-------------+
 * </pre>
 * 
 * @author xamde
 */
public class RevisionManager {
	
	private static final Logger log = LoggerFactory.getLogger(RevisionManager.class);
	
	private static final String REVMANAGER_NAME = "[.rev]";
	
	/** pointer to shared value, never null */
	private transient RevisionInfo instanceRevInfo;
	
	private final XAddress modelAddress;
	
	private boolean isInitialized = false;
	
	/**
	 * @param modelAddress ..
	 */
	@GaeOperation()
	public RevisionManager(XAddress modelAddress) {
		log.debug(DebugFormatter.init(REVMANAGER_NAME));
		this.modelAddress = modelAddress;
		String key = modelAddress + "/revisions";
		Map<String,Object> instanceContext = InstanceContext.getInstanceCache();
		synchronized(instanceContext) {
			this.instanceRevInfo = (RevisionInfo)instanceContext.get(key);
			if(this.instanceRevInfo == null) {
				this.instanceRevInfo = new RevisionInfo(".instance-rev" + modelAddress);
				instanceContext.put(key, this.instanceRevInfo);
			}
		}
		assert this.getInstanceRevisionInfo() != null;
		assert this.getInstanceRevisionInfo().getGaeModelRevision() != null;
		assert this.getInstanceRevisionInfo().getGaeModelRevision().getModelRevision() != null;
	}
	
	/**
	 * Does not calculate a new revision. Just returns thread-locally cached
	 * revision. If not defined, thread-local cache is initialised from instance
	 * cache, which can never be null
	 * 
	 * @return thread-local {@link ModelRevision}, never null
	 */
	public GaeModelRevision getThreadLocalGaeModelRev() {
		ThreadLocalGaeModelRevision tlgmr = getThreadLocalGaeModelRevision_internal();
		if(tlgmr == null) {
			// init
			ModelRevision modelRev = this.getInstanceRevisionInfo().getGaeModelRevision()
			        .getModelRevision();
			long silent = this.getInstanceRevisionInfo().getGaeModelRevision()
			        .getLastSilentCommitted();
			if(modelRev == null) {
				modelRev = ModelRevision.MODEL_DOES_NOT_EXIST_YET;
			}
			assert modelRev != null;
			GaeModelRevision gaeModelRev = new GaeModelRevision(silent, modelRev);
			tlgmr = new ThreadLocalGaeModelRevision(this.modelAddress);
			tlgmr.setGaeModelRev(gaeModelRev);
			setThreadRevState(tlgmr);
		}
		assert tlgmr.getGaeModelRev() != null;
		return tlgmr.getGaeModelRev();
	}
	
	/**
	 * @return ThreadRevisionState or null
	 */
	private ThreadLocalGaeModelRevision getThreadLocalGaeModelRevision_internal() {
		return InstanceContext.getThreadContext().get(this.modelAddress.toString());
	}
	
	boolean isThreadLocallyDefined() {
		return getThreadLocalGaeModelRevision_internal() != null;
	}
	
	public void resetThreadLocalRevisionNumber() {
		ThreadLocalGaeModelRevision trs = getThreadLocalGaeModelRevision_internal();
		if(trs == null) {
			// fine, keep it that way
		} else {
			setThreadRevState(null);
		}
	}
	
	public RevisionInfo getInstanceRevisionInfo() {
		return this.instanceRevInfo;
	}
	
	/**
	 * @param threadRevisionState can be null (to reset the tread local cache)
	 */
	private void setThreadRevState(ThreadLocalGaeModelRevision threadRevisionState) {
		InstanceContext.getThreadContext().put(this.modelAddress.toString(), threadRevisionState);
	}
	
	@Override
	public String toString() {
		return this.modelAddress + ":: instance:" + this.instanceRevInfo + "\nthreadLocal:"
		        + this.getThreadLocalGaeModelRevision_internal();
	}
	
	public boolean isInstanceModelRevisionInitialised() {
		return this.isInitialized;
	}
	
	public void markAsInitialised() {
		this.isInitialized = true;
	}
	
}
