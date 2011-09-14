package org.xydra.store.impl.gae.changes;

import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.core.model.XModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.DebugFormatter.Timing;
import org.xydra.store.impl.gae.GaeOperation;
import org.xydra.store.impl.gae.InstanceContext;


/**
 * There is one revision cache per {@link XModel}. The revision cache is shared
 * among all objects within one Java Virtual Machine (by means of simple static
 * variables).
 * 
 * The lifetime of the {@link RevisionCache2} itself is managed in the
 * {@link InstanceContext}, so that it can be managed better.
 * 
 * Within the revision cache, three values are managed: LastTaken, Committed,
 * and Current.
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
 * thread-local variables. Once the thread-local exact values are set, the
 * shared minimal values should no longer be considered. This ensures a
 * read-your-own-writes behaviour within one instance.
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
	
	private static final String REVCACHE_NAME = "[.rc]";
	
	private ThreadLocalExactRevisionInfo threadLocalExactRevisionInfo;
	
	public static SharedMinimalRevisionInfo getSharedMinimalRevisionInfo(XAddress modelAddress) {
		String smriKey = SharedMinimalRevisionInfo.getCacheName(modelAddress);
		Map<String,Object> instanceContext = InstanceContext.getInstanceCache();
		SharedMinimalRevisionInfo sharedMinimalRevisionInfo;
		synchronized(instanceContext) {
			sharedMinimalRevisionInfo = (SharedMinimalRevisionInfo)instanceContext.get(smriKey);
			if(sharedMinimalRevisionInfo == null) {
				sharedMinimalRevisionInfo = new SharedMinimalRevisionInfo(modelAddress);
				instanceContext.put(smriKey, sharedMinimalRevisionInfo);
			}
		}
		return sharedMinimalRevisionInfo;
	}
	
	@GaeOperation()
	RevisionCache2(XAddress modelAddress) {
		SharedMinimalRevisionInfo sharedMinimalRevisionInfo = getSharedMinimalRevisionInfo(modelAddress);
		Map<String,Object> threadContext = InstanceContext.getTheadContext();
		synchronized(threadContext) {
			String tleriKey = ThreadLocalExactRevisionInfo.getCacheName(modelAddress);
			this.threadLocalExactRevisionInfo = (ThreadLocalExactRevisionInfo)threadContext
			        .get(tleriKey);
			if(this.threadLocalExactRevisionInfo == null) {
				this.threadLocalExactRevisionInfo = new ThreadLocalExactRevisionInfo(modelAddress,
				        sharedMinimalRevisionInfo);
				threadContext.put(tleriKey, this.threadLocalExactRevisionInfo);
			}
		}
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
		long l = this.threadLocalExactRevisionInfo.getCurrentRev(true);
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
		long l = this.threadLocalExactRevisionInfo.getLastCommitted(true);
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
		long l = this.threadLocalExactRevisionInfo.getLastTaken(true);
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
	protected void setCurrentModelRev(long l) {
		log.debug(DebugFormatter.dataPut(REVCACHE_NAME, "current", l, Timing.Now));
		this.threadLocalExactRevisionInfo.setCurrentRev(l);
	}
	
	/**
	 * Set a new value to be returned by {@link #getLastCommited()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	protected void setLastCommited(long l) {
		log.debug(DebugFormatter.dataPut(REVCACHE_NAME, "lastCommited", l, Timing.Now));
		this.threadLocalExactRevisionInfo.setLastCommitted(l);
	}
	
	/**
	 * Set a new value to be returned by {@link #getLastTaken()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	protected void setLastTaken(long l) {
		log.debug(DebugFormatter.dataPut(REVCACHE_NAME, "lastTaken", l, Timing.Now));
		this.threadLocalExactRevisionInfo.setLastTaken(l);
	}
	
	protected void clear() {
		log.debug("revCache cleared");
		this.threadLocalExactRevisionInfo.clear();
	}
	
	public long getExactCurrentRev() {
		// FIXME PERF !! renable getExactCurrentRev
		return IRevisionInfo.NOT_SET;
		// long l = this.threadLocalExactRevisionInfo.getCurrentRev(false);
		// log.debug(DebugFormatter.dataGet(REVCACHE_NAME, "current", l));
		// return l;
	}
	
	protected void setModelExists(boolean exists) {
		log.debug("set model exists to " + exists);
		this.threadLocalExactRevisionInfo.setModelExists(exists);
	}
	
	public Boolean modelExists() {
		return this.threadLocalExactRevisionInfo.modelExists();
	}
	
}
