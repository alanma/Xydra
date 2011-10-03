package org.xydra.store.impl.gae.changes;

import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.core.model.XModel;
import org.xydra.core.model.impl.memory.UUID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.RevisionState;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.DebugFormatter.Timing;
import org.xydra.store.impl.gae.GaeOperation;
import org.xydra.store.impl.gae.InstanceContext;


/**
 * This class is a facade and manager for the different revision caches.
 * 
 * There is one revision cache per {@link XModel}. The revision cache is shared
 * among all objects within one Java Virtual Machine (by means of simple static
 * variables).
 * 
 * The lifetime of the {@link RevisionCache2} itself is managed in the
 * {@link InstanceContext}, so that it can be managed better.
 * 
 * Within the revision cache, four values are managed: LastTaken, Committed, and
 * Current + modelExists.
 * 
 * A model has a <em>current revision number</em> (Current). It is incremented
 * every time a change operation succeeds. Not necessarily only one step.
 * 
 * The order of revision number is this (highest numbers first):
 * 
 * <pre>
 * ...
 * LAST_TAKEN (L)
 * r98 +-------- real highest taken revision (in-progress-change)
 * r97 + ....... (taken revision)
 * r96 +-------- highest known taken revision (in-progress-change)
 * r95 +         (taken revision)
 * COMMITTED (C)
 * r94 +-------- real highest committed revision (in-progress or failed)  
 * r93 + ....... (committed)
 * r92 +-------- highest known committed revision (in-progress or failed)
 * r91 +         (committed) 
 * CURRENT_REV (R)
 * r90 +-------- real highest succeeded revision = current model version 
 * r89 + ....... (succeeded)
 * r88 +-------- highest known succeeded revision 
 * ...
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
public class RevisionCache2 {
	
	private static final Logger log = LoggerFactory.getLogger(RevisionCache2.class);
	
	private static final String REVCACHE_NAME = "[.rc-" + UUID.uuid(4) + "]";
	
	private SharedRevisionManager sharedRevisionManager;
	
	private XAddress modelAddress;
	
	public static SharedRevisionManager getSharedRevisionManagerInstance(XAddress modelAddress) {
		String key = SharedRevisionManager.getCacheName(modelAddress);
		Map<String,Object> instanceContext = InstanceContext.getInstanceCache();
		SharedRevisionManager sharedRevManager;
		synchronized(instanceContext) {
			sharedRevManager = (SharedRevisionManager)instanceContext.get(key);
			if(sharedRevManager == null) {
				sharedRevManager = new SharedRevisionManager(modelAddress);
				instanceContext.put(key, sharedRevManager);
			}
		}
		return sharedRevManager;
	}
	
	@GaeOperation()
	RevisionCache2(XAddress modelAddress) {
		log.debug(DebugFormatter.init(REVCACHE_NAME));
		this.modelAddress = modelAddress;
		this.sharedRevisionManager = getSharedRevisionManagerInstance(modelAddress);
	}
	
	/**
	 * @return a cached value of the current revision number as defined by
	 *         {@link IGaeChangesService#getCurrentRevisionNumber()}.
	 * 
	 *         The returned value may be less that the actual "current" revision
	 *         number, but is guaranteed to never be greater.
	 * 
	 *         Non-existing models are signalled as -1.
	 */
	@GaeOperation(memcacheRead = true)
	protected long getCurrentRev() {
		assertThreadRevisionInfoIsInitialised();
		long l = this.getThreadContext().getCurrentRev();
		if(l == IRevisionInfo.NOT_SET) {
			l = -1;
		}
		log.debug(DebugFormatter.dataGet(REVCACHE_NAME, "current", l, Timing.Now));
		return l;
	}
	
	/**
	 * @return a revision number such that all changes up to and including that
	 *         revision number are guaranteed to be committed. This is not
	 *         guaranteed to be the highest revision number that fits this
	 *         requirement.
	 */
	@GaeOperation(memcacheRead = true)
	protected long getLastCommited() {
		assertThreadRevisionInfoIsInitialised();
		long l = this.sharedRevisionManager.getLastCommitted();
		if(l == IRevisionInfo.NOT_SET) {
			l = -1;
		}
		log.debug(DebugFormatter.dataGet(REVCACHE_NAME, "lastCommitted", l, Timing.Now));
		return l;
	}
	
	/**
	 * @return the last known revision number that has been grabbed by a change.
	 *         No guarantees are made that no higher revision numbers aren't
	 *         taken already.
	 */
	protected long getLastTaken() {
		assertThreadRevisionInfoIsInitialised();
		long l = this.sharedRevisionManager.getLastTaken();
		if(l == IRevisionInfo.NOT_SET) {
			l = -1;
		}
		log.debug(DebugFormatter.dataGet(REVCACHE_NAME, "lastTaken", l, Timing.Now));
		return l;
	}
	
	/**
	 * Set a new value to be returned by {@link #getCurrentRev()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	protected void setCurrentModelRev(long l, boolean modelExists) {
		setCurrentModelRev(new RevisionState(l, modelExists));
	}
	
	protected void setCurrentModelRev(RevisionState revisionState) {
		assert revisionState != null;
		assertThreadRevisionInfoIsInitialised();
		log.debug(DebugFormatter.dataPut(REVCACHE_NAME, "revState", revisionState, Timing.Now));
		this.getThreadContext().setRevisionStateIfRevIsHigherAndNotNull(revisionState);
		this.sharedRevisionManager.setCurrentRevisionStateIfRevIsHigher(revisionState);
	}
	
	/**
	 * Set a new value to be returned by {@link #getLastCommited()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	protected void setLastCommited(long l) {
		assertThreadRevisionInfoIsInitialised();
		log.debug(DebugFormatter.dataPut(REVCACHE_NAME, "lastCommited", l, Timing.Now));
		this.sharedRevisionManager.setLastCommittedIfHigher(l);
	}
	
	/**
	 * Set a new value to be returned by {@link #getLastTaken()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	protected void setLastTaken(long l) {
		assertThreadRevisionInfoIsInitialised();
		log.debug(DebugFormatter.dataPut(REVCACHE_NAME, "lastTaken", l, Timing.Now));
		this.sharedRevisionManager.setLastTakenIfHigher(l);
	}
	
	protected void clear() {
		log.debug(DebugFormatter.clear(REVCACHE_NAME));
		this.getThreadContext().clear();
		this.sharedRevisionManager.clear();
	}
	
	protected Boolean modelExists() {
		return getThreadContext().modelExists();
	}
	
	protected RevisionState getRevisionState() {
		return getThreadContext().getRevisionState();
	}
	
	private ThreadRevisionInfo getThreadContext() {
		assertThreadRevisionInfoIsInitialised();
		return InstanceContext.getThreadContext().get(this.modelAddress.toString());
	}
	
	public void assertThreadRevisionInfoIsInitialised() {
		ThreadRevisionInfo threadRevInfo = InstanceContext.getThreadContext().get(
		        this.modelAddress.toString());
		if(threadRevInfo == null) {
			// none created for this model yet
			/* do we know something on this JVM instance we can use? Maybe null. */
			RevisionState revisionState = this.sharedRevisionManager.getRevisionState();
			threadRevInfo = new ThreadRevisionInfo(this.modelAddress);
			threadRevInfo.setRevisionStateIfRevIsHigherAndNotNull(revisionState);
			InstanceContext.getThreadContext().put(this.modelAddress.toString(), threadRevInfo);
		}
		assert InstanceContext.getThreadContext().get(this.modelAddress.toString()) != null;
	}
	
}
