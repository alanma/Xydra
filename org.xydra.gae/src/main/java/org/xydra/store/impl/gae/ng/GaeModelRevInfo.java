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
	private long lastStableCommitted = -1;

	/**
	 * All changes lower than this one are committed, failed or noChange. None
	 * of them is creating.
	 */
	private long lastStableSuccess = -1;

	/**
	 * This revision was successful and currently no other successful revs exist
	 *
	 * Helps to estimate how many changes to grab from backend.
	 */
	private long lastSuccess = -1;

	/**
	 * All revision numbers < this are taken.
	 *
	 * This helps to grab faster a free revision number when executing commands.
	 */
	private long lastTaken = -1;

	/**
	 * true iff the model exists at the lastStableSuccess revision
	 */
	private boolean modelExists = false;

	/**
	 * Time-stamp for lastStableSuccess
	 *
	 * When have these informations been computed? Date of earliest relevant
	 * change log read access that contributes to the information in here.
	 */
	private long timestamp = -1;

	private transient String debugHint = "na";

	private Precision precision = Precision.None;

	public Precision getPrecision() {
		return this.precision;
	}

	public static enum Precision {
		/** Self-created */
		None,
		/** Was probably precise once */
		Loaded,
		/** Has been calculated and not knowingly invalidated */
		Precise,
		/** Changed after precise calculation. Still a good start. */
		Imprecise
	}

	GaeModelRevInfo() {
	}

	/**
	 * Caller must ensure that the model at least exists.
	 *
	 * @param timestamp
	 *            when the information used for lastStableSuccess was obtained
	 * @param modelExists
	 *            at revision lastStableSuccess
	 * @param lastStableSuccess
	 * @param lastStableCommitted
	 * @param lastSuccess
	 * @param lastTaken
	 * @param precision
	 */
	public GaeModelRevInfo(final long timestamp, final boolean modelExists, final long lastStableSuccess,
			final long lastStableCommitted, final long lastSuccess, final long lastTaken, final Precision precision) {
		this.timestamp = timestamp;
		XyAssert.xyAssert(-1 <= lastStableSuccess);
		XyAssert.xyAssert(lastStableSuccess <= lastStableCommitted);
		XyAssert.xyAssert(lastStableCommitted <= lastTaken);
		XyAssert.xyAssert(lastStableSuccess <= lastSuccess,
				"lastStableSuccess=%s <= lastSuccess=%s", lastStableSuccess, lastSuccess);
		XyAssert.xyAssert(lastSuccess <= lastTaken);
		this.modelExists = modelExists;
		this.timestamp = timestamp;
		this.lastStableSuccess = lastStableSuccess;
		this.lastStableCommitted = lastStableCommitted;
		this.lastSuccess = lastSuccess;
		this.lastTaken = lastTaken;
		this.precision = precision;
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

	/**
	 * Caller should degrade precision via {@link #setPrecisionToImprecise()}
	 *
	 * @param other
	 */
	public void incrementFrom(final GaeModelRevInfo other) {
		// trigger the least changes in this order
		incrementLastStableSuccessChange(other.lastStableSuccess, other.modelExists,
				other.timestamp);
		incrementLastStableCommitted(other.lastStableCommitted);
		incrementLastSuccessChange(other.lastSuccess);
		incrementLastTaken(other.lastTaken);
	}

	/**
	 * Caller should degrade precision via {@link #setPrecisionToImprecise()}
	 *
	 * @param lastStableCommitted
	 */
	public void incrementLastStableCommitted(final long lastStableCommitted) {
		if (lastStableCommitted > this.lastStableCommitted) {
			this.lastStableCommitted = lastStableCommitted;
		}
		incrementLastTaken(lastStableCommitted);
	}

	/**
	 * Caller should degrade precision via {@link #setPrecisionToImprecise()}
	 *
	 * @param lastStableSuccess
	 * @param modelExists
	 * @param timestamp
	 */
	public void incrementLastStableSuccessChange(final long lastStableSuccess, final boolean modelExists,
			final long timestamp) {
		final boolean changes = incrementLastStableSuccessChange_internal(lastStableSuccess);
		if (changes) {
			this.modelExists = modelExists;
			this.timestamp = timestamp;
		}
	}

	private boolean incrementLastStableSuccessChange_internal(final long lastStableSuccess) {
		boolean changedStableSuccess = false;
		if (lastStableSuccess > this.lastStableSuccess) {
			this.lastStableSuccess = lastStableSuccess;
			changedStableSuccess = true;
		}
		incrementLastStableCommitted(lastStableSuccess);
		incrementLastSuccessChange(lastStableSuccess);
		return changedStableSuccess;
	}

	/**
	 * Caller should degrade precision via {@link #setPrecisionToImprecise()}
	 *
	 * @param lastSuccess
	 */
	public void incrementLastSuccessChange(final long lastSuccess) {
		if (lastSuccess > this.lastSuccess) {
			this.lastSuccess = lastSuccess;
		}
		incrementLastTaken(lastSuccess);
	}

	/**
	 * @param lastTaken
	 * @return true iff anything changed
	 */
	public boolean incrementLastTaken(final long lastTaken) {
		if (lastTaken > this.lastTaken) {
			this.lastTaken = lastTaken;
			return true;
		}
		return false;
	}

	public boolean isModelExists() {
		return this.modelExists;
	}

	public void setModelExists(final boolean modelExists) {
		this.modelExists = modelExists;
	}

	public void setTimestamp(final long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return XyAssert
				.format("(prec: %s),stable(succ: %s <= comm %s) <= unstable(succ %s <= taken %s) modelExists?%s at %s. \n  (SOURCE='%s')",
						this.precision, this.lastStableSuccess, this.lastStableCommitted,
						this.lastSuccess, this.lastTaken, this.modelExists, this.timestamp,
						this.debugHint);
	}

	public void setPrecision(final Precision precision) {
		this.precision = precision;
	}

	public GaeModelRevInfo copy() {
		return new GaeModelRevInfo(this.timestamp, this.modelExists, this.lastStableSuccess,
				this.lastStableCommitted, this.lastSuccess, this.lastTaken, this.precision);
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof GaeModelRevInfo)) {
			return false;
		}

		final GaeModelRevInfo o = (GaeModelRevInfo) other;
		return this.modelExists == o.modelExists && this.timestamp == o.timestamp
				&& this.precision == o.precision
				&& this.lastStableCommitted == o.lastStableCommitted
				&& this.lastStableSuccess == o.lastStableSuccess
				&& this.lastSuccess == o.lastSuccess && this.lastTaken == o.lastTaken;
	}

	@Override
	public int hashCode() {
		return (int) this.timestamp + (int) this.lastStableSuccess + this.precision.hashCode()
				+ (this.modelExists ? 0 : 1);
	}

	public void setPrecisionToImprecise() {
		if (this.precision == Precision.Precise) {
			this.precision = Precision.Imprecise;
		}
	}

	public void setDebugHint(final String debugHint) {
		this.debugHint = debugHint;
	}

}
