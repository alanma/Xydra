package org.xydra.store.impl.gae;

import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.core.model.impl.memory.UUID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.ModelRevision;
import org.xydra.store.impl.gae.DebugFormatter.Timing;
import org.xydra.store.impl.gae.changes.GaeModelRevision;
import org.xydra.store.impl.gae.changes.RevisionInfo;
import org.xydra.store.impl.gae.changes.ThreadRevisionState;


/**
 * This class is a facade and manager for the different revision caches. It is
 * passive, i.e. does not trigger any kind of recalculation.
 * 
 * There is one revision cache per {@link XModel}. The instance revision cache
 * is shared among all objects within one Java Virtual Machine via the
 * {@link InstanceContext}.
 * 
 * Four values are managed: LastTaken, Committed, and Current + modelExists.
 * 
 * A model has a <em>current revision number</em> (Current). It is incremented
 * every time a change operation succeeds. Not necessarily only one step.
 * 
 * The order of revision number is this (highest numbers first):
 * 
 * Reality:
 * 
 * <pre>
 * Possible Status: | Creating | SuccExe | SuccNoChg | FailPre | FailTimeout |
 * -----------------+----------+---------+-----------+---------+-------------+
 * ...              |    ...                                                 |
 * r99              |    No change entity exists for this revision number    |
 * r98              |    No change entity exists for this revision number    |
 * r97              |    No change entity exists for this revision number    |
 * 
 * LAST_TAKEN (L) = 95
 * 
 * r96              |   ????????????????????????????????????????????????     |
 * r95              |   ????????????????????????????????????????????????     |
 * 
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
 * ...              |   ---    |   ?????????????????????????????????????     |
 * r00              |   ---    |   ?????????????????????????????????????     |
 * -----------------+----------+---------+-----------+---------+-------------+
 * </pre>
 * 
 * Invariants: LAST_TAKEN >= COMMITTED >= CURRENT
 * 
 * For each value, the revision cache maintains a shared minimal value, which
 * can be re-used among all threads as a starting point to compute the
 * thread-local variables.
 * 
 * Each thread has its own view on currentRevision and modelExists. This ensures
 * a read-your-own-writes behaviour within one instance.
 * 
 * A typical invocation sequence can look like this:
 * 
 * Syntax:
 * 
 * <pre>
 * L_ex  = revCache.lastTaken.exactValue
 * L_min = revCache.lastTaken.sharedMinimalValue
 * C_ex  = revCache.committed.exactValue
 * C_min = revCache.committed.sharedMinimalValue
 * R_ex  = revCache.currentRev.exactValue
 * R_min = revCache.currentRev.sharedMinimalValue
 * </pre>
 * 
 * <ol>
 * <li>On GAE instance 1: find next free revision number (to execute a command)
 * <ol>
 * <li>
 * 
 * <pre>
 * L = L_ex
 * If none set:
 *   L = L_min
 *     If none set: L_loaded = Load L from memcache. L_min = L_loaded.
 *     If L still undefined, use -1.
 * </pre>
 * 
 * </li>
 * <li>Ask datastore for all changes [L,X] until change(X) == null</li>
 * <li>R_ex = ... compute while traversing changes ...</li>
 * <li>Set L_ex=X-1 as thread-local exact value of lastTaken</li>
 * <li>Create and commit change(L_ex+1)</li>
 * <li>For (X=C,L,R): X_min.setIfHigher(X_ex)</li>
 * <li>
 * 
 * <pre>
 * For (X=C,L,R): 
 *   X_delta = X_loaded - X_min;
 *   If (X_delta > 0): memcache(x).increment(X_delta);
 * </pre>
 * 
 * </li>
 * </ol>
 * </li>
 * </ol>
 * 
 * 
 * @author xamde
 */
/**
 * @author xamde
 * 
 */
public class RevisionManager {
	
	private static final Logger log = LoggerFactory.getLogger(RevisionManager.class);
	
	private static final String REVCACHE_NAME = "[.revm-" + UUID.uuid(4) + "]";
	
	private XAddress modelAddress;
	
	private RevisionInfo instanceRevInfo;
	
	/**
	 * @param modelAddress ..
	 * @return a reference to the shared {@link RevisionInfo} to manage the
	 *         given model's revision state
	 */
	// TODO ready for lazy init
	private static RevisionInfo getInstanceRevisionInfo(XAddress modelAddress) {
		String key = modelAddress + "/revisions";
		Map<String,Object> instanceContext = InstanceContext.getInstanceCache();
		RevisionInfo instanceRevInfo;
		synchronized(instanceContext) {
			instanceRevInfo = (RevisionInfo)instanceContext.get(key);
			if(instanceRevInfo == null) {
				instanceRevInfo = new RevisionInfo(".instance-rev");
				instanceContext.put(key, instanceRevInfo);
			}
		}
		return instanceRevInfo;
	}
	
	/**
	 * @param modelAddress ..
	 */
	@GaeOperation()
	public RevisionManager(XAddress modelAddress) {
		log.debug(DebugFormatter.init(REVCACHE_NAME));
		this.modelAddress = modelAddress;
		this.instanceRevInfo = getInstanceRevisionInfo(modelAddress);
	}
	
	/**
	 * @return a revision number such that all changes up to and including that
	 *         revision number are guaranteed to be committed. This is not
	 *         guaranteed to be the highest revision number that fits this
	 *         requirement.
	 */
	@GaeOperation(memcacheRead = true)
	public long getLastCommited() {
		long l = this.instanceRevInfo.getLastCommitted();
		if(l == RevisionInfo.NOT_SET) {
			l = -1;
		}
		log.trace(DebugFormatter.dataGet(REVCACHE_NAME, "lastCommitted", l, Timing.Now));
		return l;
	}
	
	/**
	 * @return the last known revision number that has been grabbed by a change.
	 *         No guarantees are made that no higher revision numbers aren't
	 *         taken already.
	 */
	public long getLastTaken() {
		long l = this.instanceRevInfo.getLastTaken();
		if(l == RevisionInfo.NOT_SET) {
			l = -1;
		}
		log.trace(DebugFormatter.dataGet(REVCACHE_NAME, "lastTaken", l, Timing.Now));
		return l;
	}
	
	public long getLastSilentCommitted() {
		long l = -1;
		if(this.instanceRevInfo != null && this.instanceRevInfo.getRevisionState() != null) {
			l = this.instanceRevInfo.getRevisionState().getLastSilentCommitted();
			if(l == RevisionInfo.NOT_SET) {
				l = -1;
			}
		}
		log.trace(DebugFormatter.dataGet(REVCACHE_NAME, "lastSilentCommitted", l, Timing.Now));
		return l;
	}
	
	// /**
	// * Set a new value to the instance cache. Does not influence already
	// * initialised threadLocal numbers.
	// *
	// * @param revisionState The value to set. It is ignored if the current
	// * cached value is bigger than this.
	// */
	// @Deprecated
	// public void setBothCurrentModelRev(ModelRevision revisionState) {
	// assert revisionState != null;
	// log.debug(DebugFormatter.dataPut(REVCACHE_NAME, "revState",
	// revisionState, Timing.Now));
	// this.instanceRevInfo.setCurrentRevisionStateIfRevIsHigher(revisionState);
	// /*
	// * Same request-bound thread is making changes and doing requests here.
	// * Update thread local.
	// */
	// if(this.hasThreadLocallyDefinedCurrentRevision()
	// && (revisionState.revision() >
	// this.getThreadRevState().getRevisionState()
	// .revision())) {
	// log.debug(DebugFormatter.dataPut(REVCACHE_NAME, "revisionState",
	// revisionState,
	// Timing.Now));
	// setThreadRevState(revisionState);
	// }
	// }
	
	/**
	 * Set a new value to be returned by {@link #getLastCommited()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	public void setLastCommited(long l) {
		log.debug(DebugFormatter.dataPut(REVCACHE_NAME, "lastCommited", l, Timing.Now));
		this.instanceRevInfo.setLastCommittedIfHigher(l);
	}
	
	/**
	 * Set a new value to be returned by {@link #getLastTaken()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	public void setLastTaken(long l) {
		log.debug(DebugFormatter.dataPut(REVCACHE_NAME, "lastTaken", l, Timing.Now));
		this.instanceRevInfo.setLastTakenIfHigher(l);
	}
	
	/**
	 * Clear instance cache for this model
	 */
	public void clearInstanceCache() {
		log.debug(DebugFormatter.clear(REVCACHE_NAME));
		this.instanceRevInfo.clear();
		
	}
	
	/**
	 * Return value is thread-local after first call.
	 * 
	 * @return a cached value of the current revision
	 * 
	 *         The returned value may be less that the actual "current" revision
	 *         number, but is guaranteed to never be greater.
	 * 
	 *         Non-existing models are signalled as -1.
	 */
	@GaeOperation(memcacheRead = true)
	@Deprecated
	protected long getCurrentRev() {
		assert getThreadRevState() != null;
		long l = this.getThreadRevState().getCurrentRev();
		if(l == RevisionInfo.NOT_SET) {
			l = -1;
		}
		log.trace(DebugFormatter.dataGet(REVCACHE_NAME, "current", l, Timing.Now));
		return l;
	}
	
	/**
	 * @return a thread-locally defined {@link ModelRevision} or null, if none
	 *         defined
	 */
	public GaeModelRevision getModelRevision() {
		ThreadRevisionState trs = getThreadRevState();
		if(trs == null) {
			return null;
		}
		return trs.getRevisionState();
	}
	
	@Deprecated
	public boolean hasThreadLocallyDefinedCurrentRevision() {
		return getThreadRevState() != null;
	}
	
	boolean isThreadLocallyDefined() {
		return getThreadRevState() != null;
	}
	
	/**
	 * @return ThreadRevisionState or null
	 */
	private ThreadRevisionState getThreadRevState() {
		return InstanceContext.getThreadContext().get(this.modelAddress.toString());
	}
	
	/**
	 * @param threadRevisionState can be null (to reset the tread local cache)
	 */
	private void setThreadRevState(ThreadRevisionState threadRevisionState) {
		InstanceContext.getThreadContext().put(this.modelAddress.toString(), threadRevisionState);
	}
	
	public RevisionInfo getRevisionInfo() {
		RevisionInfo ri = new RevisionInfo("revMan-return");
		ri.setLastTakenIfHigher(getLastTaken());
		ri.setLastCommittedIfHigher(getLastCommited());
		ri.setCurrentModelRevisionIfRevIsHigher(getModelRevision());
		return ri;
	}
	
	/**
	 * @return the instance-wide {@link GaeModelRevision}, can be null
	 */
	public GaeModelRevision getInstanceRevisionState() {
		return this.instanceRevInfo.getRevisionState();
	}
	
	public RevisionInfo getInstanceRevisionInfo() {
		return this.instanceRevInfo;
	}
	
	@Override
	public String toString() {
		return "instance:" + this.instanceRevInfo + "\nthreadLocal:" + this.getThreadRevState();
	}
	
	/**
	 * FIXME WARN Does not calculate a new revision. Just looks in instance if
	 * not thread-locally defined.
	 * 
	 * @return thread-local {@link ModelRevision}, can be null
	 */
	public ModelRevision getThreadLocalRevision() {
		ThreadRevisionState trs = getThreadRevState();
		if(trs == null) {
			// init
			GaeModelRevision modelRev = this.getInstanceRevisionState();
			trs = new ThreadRevisionState(this.modelAddress);
			trs.setRevisionStateIfRevIsHigherAndNotNull(modelRev);
			setThreadRevState(trs);
		}
		return trs.getRevisionState();
	}
	
	public void resetThreadLocalRevisionNumber() {
		ThreadRevisionState trs = getThreadRevState();
		if(trs == null) {
			// fine, keep it that way
		} else {
			setThreadRevState(null);
		}
	}
	
	public void setCurrenModelRevIfHigher(GaeModelRevision newCurrentRevState) {
		assert this.instanceRevInfo != null;
		this.instanceRevInfo.setCurrentModelRevisionIfRevIsHigher(newCurrentRevState);
	}
	
	public void setLastSilentCommittedIfHigher(long l) {
		log.debug(DebugFormatter.dataPut(REVCACHE_NAME, "lastSilentCommitted", l, Timing.Now));
		if(this.instanceRevInfo.getRevisionState() == null) {
			this.instanceRevInfo
			        .setCurrentModelRevisionIfRevIsHigher(GaeModelRevision.GAE_MODEL_DOES_NOT_EXIST_YET);
		}
		this.instanceRevInfo.getRevisionState().setLastSilentCommittedIfHigher(l);
	}
	
}
