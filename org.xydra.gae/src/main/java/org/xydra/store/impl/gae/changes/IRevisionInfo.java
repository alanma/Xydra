package org.xydra.store.impl.gae.changes;

import org.xydra.store.RevisionState;


/**
 * The invariant currentRev >= lastCommitted >= lastTaken is maintained at all
 * times.
 * 
 * A defined currentRev requires a defined value for modelExists.
 * 
 * @author xamde
 * 
 */
public interface IRevisionInfo {
	
	/**
	 * Returned if a value is not set.
	 */
	public static final long NOT_SET = -2L;
	
	/**
	 * @return lastCommited if defined, {@link #NOT_SET} otherwise.
	 */
	long getLastCommitted();
	
	/**
	 * @return lastTaken if defined, {@link #NOT_SET} otherwise.
	 */
	long getLastTaken();
	
	/**
	 * @return currentReb if defined, {@link #NOT_SET} otherwise.
	 */
	long getCurrentRev();
	
	/**
	 * @return a {@link RevisionState} if known, null otherwise.
	 */
	public RevisionState getRevisionState();
	
	/**
	 * Set the given value as the new internal value only if it is higher than
	 * the current internal value.
	 * 
	 * @param lastCommitted
	 */
	void setLastCommittedIfHigher(long lastCommitted);
	
	/**
	 * Set the given value as the new internal value only if it is higher than
	 * the current internal value.
	 * 
	 * @param lastTaken
	 */
	void setLastTakenIfHigher(long lastTaken);
	
	/**
	 * Set the given value as the new internal value only if it is higher than
	 * the current internal value.
	 * 
	 * @param revisionState Can not be null.
	 */
	void setCurrentRevisionStateIfRevIsHigher(RevisionState revisionState);
	
	/**
	 * @return true if model exists; false if not; null if not known.
	 */
	Boolean modelExists();
	
	/**
	 * Reset to initial values that denote zero knowledge.
	 */
	void clear();
	
}
