package org.xydra.store.impl.gae.changes;

public interface IRevisionInfo {
	
	/**
	 * Returned if a value is not set.
	 */
	public static final long NOT_SET = -2L;
	
	/**
	 * @param mayAsk true if method may ask a backing store for more info. False
	 *            to use only local data.
	 * @return lastCommited if defined {@link #NOT_SET} otherwise.
	 */
	long getLastCommitted(boolean mayAsk);
	
	/**
	 * @param mayAsk true if method may ask a backing store for more info. False
	 *            to use only local data.
	 * @return lastTaken if defined {@link #NOT_SET} otherwise.
	 */
	long getLastTaken(boolean mayAsk);
	
	/**
	 * @param mayAsk true if method may ask a backing store for more info. False
	 *            to use only local data.
	 * @return currentReb if defined {@link #NOT_SET} otherwise.
	 */
	long getCurrentRev(boolean mayAsk);
	
	void setLastCommitted(long lastCommitted);
	
	void setLastTaken(long lastTaken);
	
	void setCurrentRev(long currentRev);
	
	void setModelExists(boolean exists);
	
	/**
	 * @return true if model exists; false if not; null if not known.
	 */
	Boolean modelExists();
	
}
