package org.xydra.store.impl.gae.ng;

import java.io.Serializable;

import org.xydra.sharedutils.XyAssert;


/**
 * <h2>changes order (highest change that meets listed requirements)</h2>
 * 
 * lastStableSuccess (state=Success, no pending below) = this is the current
 * revision
 * 
 * lastStableCommited (state=Success|Failed, no pending below)
 * 
 * lastSuccess (state=Success, can have pending below) = a tentative revision
 * 
 * lastTaken (state=any, can have pending below)
 * 
 * Invariants:
 * 
 * -1 <= lastStableSuccess <= lastStableCommited <= lastTaken
 * 
 * -1 <= lastStableSuccess <= lastSuccess <= lastTaken
 * 
 * @author xamde
 */
public class GaeModelRevInfo implements Serializable {
	
	private static final long serialVersionUID = -6269753819062518229L;
	
	public static GaeModelRevInfo createModelDoesNotExist() {
		return new GaeModelRevInfo(System.currentTimeMillis(), false, -1, -1, -1, -1,
		        Precision.None);
	}
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	/**
	 * All changes in range [0, lastCommited] are not 'Creating'.
	 * 
	 * The currentRev is always equal or less than lastCommited.
	 * 
	 * In other words: A pointer into the change item stack indicating the last
	 * such change item that has been processed.
	 * 
	 * Helps to find the next relevant change.
	 */
	private long lastStableCommitted;
	
	/**
	 * All changes lower than this one are committed, failed or noChange. None
	 * of them is creating.
	 */
	private long lastStableSuccess;
	
	/**
	 * This revision was successful and currently no other successful revs exist
	 * 
	 * Helps to estimate how many changes to grab from backend.
	 */
	private long lastSuccess;
	
	/**
	 * All revision numbers < this are taken.
	 * 
	 * This helps to grab faster a free revision number when executing commands.
	 */
	private long lastTaken;
	
	/**
	 * true iff the model exists at the lastStableSuccess revision
	 */
	private boolean modelExists;
	
	/**
	 * When have these informations been computed? Date of earliest relevant
	 * change log read access that contributes to the information in here.
	 */
	private long timestamp;
	
	private Precision precision = Precision.None;
	
	public Precision getPrecision() {
		return this.precision;
	}
	
	public static enum Precision {
		None, Loaded, Precise
	}
	
	GaeModelRevInfo() {
	}
	
	/**
	 * Caller must ensure that the model at least exists.
	 * 
	 * @param timestamp when the information used for lastStableSuccess was
	 *            obtained
	 * @param modelExists at revision lastStableSuccess
	 * @param lastStableSuccess
	 * @param lastStableCommitted
	 * @param lastSuccess
	 * @param lastTaken
	 * @param precision
	 */
	public GaeModelRevInfo(long timestamp, boolean modelExists, long lastStableSuccess,
	        long lastStableCommitted, long lastSuccess, long lastTaken, Precision precision) {
		this.timestamp = timestamp;
		XyAssert.xyAssert(-1 <= lastStableSuccess);
		XyAssert.xyAssert(lastStableSuccess <= lastStableCommitted);
		XyAssert.xyAssert(lastStableCommitted <= lastTaken);
		XyAssert.xyAssert(lastStableSuccess <= lastSuccess,
		        "lastStableSuccess=%s <= lastSuccess=%s", lastStableSuccess, lastSuccess);
		XyAssert.xyAssert(lastSuccess <= lastTaken);
		this.lastStableSuccess = lastStableSuccess;
		this.lastStableCommitted = lastStableCommitted;
		this.lastSuccess = lastSuccess;
		this.lastTaken = lastTaken;
		this.precision = precision;
	}
	
	public long getLastCommitted() {
		return this.lastStableCommitted;
	}
	
	public long getLastStableCommitted() {
		return this.lastStableCommitted;
	}
	
	public long getLastStableSuccessChange() {
		return this.lastStableSuccess;
	}
	
	public long getLastSuccessChange() {
		return this.lastSuccess;
	}
	
	public long getLastTaken() {
		return this.lastTaken;
	}
	
	public long getTimestamp() {
		return this.timestamp;
	}
	
	public void incrementFrom(GaeModelRevInfo other) {
		// start from highest numbers, to trigger the least changes
		incrementLastTaken(other.lastTaken);
		incrementLastTaken(other.lastSuccess);
		incrementLastTaken(other.lastStableCommitted);
		if(incrementLastTaken(other.lastStableSuccess)) {
			setTimestamp(other.timestamp);
			setModelExists(other.modelExists);
		}
	}
	
	public void incrementLastStableCommitted(long lastStableCommitted) {
		if(lastStableCommitted > this.lastStableCommitted) {
			this.lastStableCommitted = lastStableCommitted;
		}
		incrementLastTaken(lastStableCommitted);
	}
	
	public void incrementLastStableSuccessChange(long lastStableSuccess) {
		if(lastStableSuccess > this.lastStableSuccess) {
			this.lastStableSuccess = lastStableSuccess;
		}
		incrementLastStableCommitted(lastStableSuccess);
		incrementLastSuccessChange(lastStableSuccess);
	}
	
	public void incrementLastSuccessChange(long lastSuccess) {
		if(lastSuccess > this.lastSuccess) {
			this.lastSuccess = lastSuccess;
		}
		incrementLastTaken(lastSuccess);
	}
	
	/**
	 * @param lastTaken
	 * @return true iff anything changed
	 */
	public boolean incrementLastTaken(long lastTaken) {
		if(lastTaken > this.lastTaken) {
			this.lastTaken = lastTaken;
			return true;
		}
		return false;
	}
	
	public boolean isModelExists() {
		return this.modelExists;
	}
	
	public void setModelExists(boolean modelExists) {
		this.modelExists = modelExists;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	@Override
	public String toString() {
		return XyAssert
		        .format("%s: (precision: %s) stable: ( success: %s <= committed %s ) <= unstable: ( success %s <= taken %s )",
		                this.timestamp, this.precision, this.lastStableSuccess,
		                this.lastStableCommitted, this.lastSuccess, this.lastTaken);
	}
	
	public void setPrecision(Precision precision) {
		this.precision = precision;
	}
	
	public GaeModelRevInfo copy() {
		return new GaeModelRevInfo(this.timestamp, this.modelExists, this.lastStableSuccess,
		        this.lastStableCommitted, this.lastSuccess, this.lastTaken, this.precision);
	}
	
}
