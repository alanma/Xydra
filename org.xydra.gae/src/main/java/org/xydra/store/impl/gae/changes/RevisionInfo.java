package org.xydra.store.impl.gae.changes;

import java.io.Serializable;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.ModelRevision;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.DebugFormatter.Timing;


/**
 * This class:
 * 
 * Maintains these invariants: currentRev >= lastCommitted >= lastTaken.
 * 
 * 2) Is {@link Serializable} and can be used in MemCache.
 * 
 * 3) Knows if it has internal knowledge that has not been saved yet. Methods:
 * {@link #hasUnsavedChanges()} and {@link #markChangesAsSaved()}.
 * 
 * 4) Is thread-safe.
 * 
 * 5) ... is only relevant within the GAE implementation.
 * 
 * A defined currentRev requires a defined value for modelExists.
 * 
 * @author xamde
 */
public class RevisionInfo implements Serializable {
	
	private static final Logger log = LoggerFactory.getLogger(RevisionInfo.class);
	
	private static final long serialVersionUID = -8537625185285087183L;
	
	/**
	 * Returned if a value is not set.
	 */
	public static final long NOT_SET = -2L;
	
	/**
	 * Create a revision info that knows nothing.
	 * 
	 * @param datasourceName for debugging purposes
	 */
	public RevisionInfo(String datasourceName) {
		this.datasourceName = datasourceName;
		log.debug(DebugFormatter.init(this.datasourceName));
		clear();
	}
	
	/**
	 * Reset to initial values that denote zero knowledge.
	 */
	public void clear() {
		log.debug(DebugFormatter.clear(this.datasourceName));
		this.revisionState = null;
		this.lastCommitted = NOT_SET;
		this.lastTaken = NOT_SET;
		this.unsavedChanges = 0;
	}
	
	/** can be null */
	private GaeModelRevision revisionState;
	private long lastCommitted;
	private long lastTaken;
	private int unsavedChanges;
	
	private String datasourceName;
	
	/**
	 * @return lastCommited if defined, {@link #NOT_SET} otherwise.
	 */
	public synchronized long getLastCommitted() {
		synchronized(this) {
			long result = this.lastCommitted;
			log.trace(DebugFormatter.dataGet(this.datasourceName, "lastCommitted", result,
			        Timing.Now));
			return result;
		}
	}
	
	/**
	 * @return lastTaken if defined, {@link #NOT_SET} otherwise.
	 */
	public synchronized long getLastTaken() {
		synchronized(this) {
			long result = this.lastTaken;
			log.trace(DebugFormatter.dataGet(this.datasourceName, "lastTaken", result, Timing.Now));
			return result;
		}
	}
	
	/**
	 * @return currentRev if defined, {@link #NOT_SET} otherwise.
	 */
	public synchronized long getCurrentRev() {
		synchronized(this) {
			long result = this.revisionState == null ? NOT_SET : this.revisionState.revision();
			log.trace(DebugFormatter.dataGet(this.datasourceName, "currentRev", result, Timing.Now));
			return result;
		}
	}
	
	/**
	 * @return true if model exists; false if not; null if not known.
	 */
	public Boolean modelExists() {
		synchronized(this) {
			Boolean result = this.revisionState == null ? null : this.revisionState.modelExists();
			log.debug(DebugFormatter
			        .dataGet(this.datasourceName, "modelExists", result, Timing.Now));
			return result;
		}
	}
	
	/**
	 * Set the given value as the new internal value only if it is higher than
	 * the current internal value.
	 * 
	 * @param revisionState Can not be null.
	 */
	public synchronized void setCurrentModelRevisionIfRevIsHigher(GaeModelRevision revisionState) {
		if(revisionState == null) {
			throw new IllegalArgumentException("revisionState can not be null");
		}
		if(this.revisionState == null || revisionState.revision() > this.revisionState.revision()) {
			log.debug(DebugFormatter.dataPut(this.datasourceName, "revisionState", revisionState,
			        Timing.Now));
			this.revisionState = revisionState;
			this.unsavedChanges++;
			// invariant: currentRev >= lastCommitted
			setLastCommittedIfHigher(revisionState.revision());
		}
	}
	
	/**
	 * Set the given value as the new internal value only if it is higher than
	 * the current internal value.
	 * 
	 * @param lastCommitted ..
	 */
	public synchronized void setLastCommittedIfHigher(long lastCommitted) {
		if(lastCommitted > this.lastCommitted) {
			log.debug(DebugFormatter.dataPut(this.datasourceName, "lastCommitted", lastCommitted,
			        Timing.Now));
			this.lastCommitted = lastCommitted;
			this.unsavedChanges++;
			// invariant: lastCommitted >= lastTaken
			setLastTakenIfHigher(lastCommitted);
		}
	}
	
	/**
	 * Set the given value as the new internal value only if it is higher than
	 * the current internal value.
	 * 
	 * @param lastTaken ..
	 */
	public synchronized void setLastTakenIfHigher(long lastTaken) {
		if(lastTaken > this.lastTaken) {
			log.debug(DebugFormatter.dataPut(this.datasourceName, "lastTaken", lastTaken,
			        Timing.Now));
			this.lastTaken = lastTaken;
			this.unsavedChanges++;
		}
	}
	
	public synchronized boolean hasUnsavedChanges() {
		return this.unsavedChanges > 0;
	}
	
	public synchronized void markChangesAsSaved() {
		this.unsavedChanges = 0;
	}
	
	/**
	 * @return a {@link ModelRevision} if known, null otherwise.
	 */
	public GaeModelRevision getRevisionState() {
		synchronized(this) {
			GaeModelRevision result = this.revisionState;
			log.trace(DebugFormatter.dataGet(this.datasourceName, "revisionState", result,
			        Timing.Now));
			return result;
		}
	}
	
	@Override
	public String toString() {
		return "{current:" + getCurrentRev() + ",lastTaken:" + getLastTaken() + ",lastCommitted:"
		        + getLastCommitted() + ",modelExists:" + modelExists() + "}; unsavedChanges="
		        + this.hasUnsavedChanges();
	}
	
	public void setDatasourceName(String datasourceName) {
		this.datasourceName = datasourceName;
	}
	
}
